/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.releasestrategy

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Deterministic top-to-bottom roadmap renderer.
 * Each release card is followed by its details tray (no nested collapsing layout).
 * CSS and defs are fully scoped to the SVG id to prevent cross-SVG collisions in shared DOM.
 */
class ReleaseRoadMapMaker {

    fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean, animate: String): String {
        return createSvg(releaseStrategy, isPdf, animate)
    }

    private data class ItemLayout(
        val trayHeight: Int,
        val blockHeight: Int
    )

    @OptIn(ExperimentalUuidApi::class)
    private fun createSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false, animate: String): String {
        val rawId = Uuid.random().toHexString().replace("-", "")
        val svgId = "release_rm_${slug(releaseStrategy.title)}_${rawId.take(10)}"
        val idPrefix = svgId.replace("-", "_")

        val titleId = "${idPrefix}_title"
        val descId = "${idPrefix}_desc"

        val animated = !isPdf && animate.uppercase() != "OFF" && animate.uppercase() != "STATIC"
        val headerHeight = 152
        val footerPadding = 88
        val contentWidth = 1008
        val contentX = 96
        val cardHeight = 96
        val trayGap = 16
        val itemGap = 24

        val itemLayouts = releaseStrategy.releases.map { r ->
            val linesCount = r.lines.size
            val trayHeight = if (linesCount <= 0) 104 else 100 + ((linesCount - 1) * 24)
            val blockHeight = cardHeight + trayGap + trayHeight + itemGap
            ItemLayout(trayHeight = trayHeight, blockHeight = blockHeight)
        }

        val contentHeight = itemLayouts.sumOf { it.blockHeight }
        val minHeight = 320
        val height = maxOf(minHeight, headerHeight + contentHeight + footerPadding)

        val defs = if (!isPdf) svgDefs(releaseStrategy, svgId, idPrefix) else ""
        val body = buildItems(
            releases = releaseStrategy.releases,
            layouts = itemLayouts,
            releaseStrategy = releaseStrategy,
            contentWidth = contentWidth,
            cardHeight = cardHeight,
            trayGap = trayGap,
            animated = animated
        )

        val bgBase = if (releaseStrategy.useDark) "#09090b" else "#f5f7fb"
        val titleColor = if (releaseStrategy.useDark) "#f4f4f5" else "#0f172a"
        val gradientBgId = "${idPrefix}_bg"
        val glowId = "${idPrefix}_glow"
        val vignetteId = "${idPrefix}_vignette"
        val dotsId = "${idPrefix}_dots"

        return """
            <svg xmlns="http://www.w3.org/2000/svg"
                 id="$svgId"
                 width="${releaseStrategy.scale * 1200}" height="${height * releaseStrategy.scale}"
                 viewBox="0 0 1200 $height"
                 role="img"
                 aria-labelledby="$titleId $descId">
                <title id="$titleId">${releaseStrategy.title.escapeXml()}</title>
                <desc id="$descId">Release roadmap laid out top-to-bottom with deterministic card and details sizing.</desc>
                $defs
                <g transform="scale(${releaseStrategy.scale})">
                    <rect width="1200" height="$height" fill="$bgBase"/>
                    <rect width="1200" height="$height" fill="url(#$gradientBgId)"/>
                    <rect width="1200" height="$height" fill="url(#$glowId)" opacity="${if (releaseStrategy.useDark) "0.35" else "0.45"}"/>
                    <rect width="1200" height="$height" fill="url(#$dotsId)" opacity="${if (releaseStrategy.useDark) "0.14" else "0.16"}"/>
                    <rect width="1200" height="$height" fill="url(#$vignetteId)" opacity="${if (releaseStrategy.useDark) "0.38" else "0.16"}"/>

                    <g transform="translate($contentX, 72)">
                        <text class="rm-title" fill="$titleColor">${releaseStrategy.title.escapeXml()}</text>
                        <rect y="18" width="56" height="4" rx="2" fill="#3b82f6"/>
                    </g>

                    <g transform="translate($contentX, $headerHeight)">
                        $body
                    </g>
                </g>
            </svg>
        """.trimIndent()
    }

    private fun buildItems(
        releases: List<Release>,
        layouts: List<ItemLayout>,
        releaseStrategy: ReleaseStrategy,
        contentWidth: Int,
        cardHeight: Int,
        trayGap: Int,
        animated: Boolean
    ): String {
        if (releases.isEmpty()) {
            val emptyText = if (releaseStrategy.useDark) "#a1a1aa" else "#64748b"
            return """
                <g>
                    <rect width="$contentWidth" height="120" rx="16" fill="${if (releaseStrategy.useDark) "#18181b" else "#ffffff"}" stroke="${if (releaseStrategy.useDark) "#27272a" else "#cbd5e1"}"/>
                    <text x="32" y="68" fill="$emptyText" class="rm-date">No releases found.</text>
                </g>
            """.trimIndent()
        }

        val sb = StringBuilder()
        var y = 0

        releases.forEachIndexed { index, release ->
            val layout = layouts[index]
            val accent = releaseStroke(release, releaseStrategy)
            val cardBg = if (releaseStrategy.useDark) "#18181b" else "#ffffff"
            val trayBg = if (releaseStrategy.useDark) "#0c0c0e" else "#f8fafc"
            val textColor = if (releaseStrategy.useDark) "#f4f4f5" else "#1e293b"
            val dateColor = if (releaseStrategy.useDark) "#a1a1aa" else "#64748b"
            val trayText = if (releaseStrategy.useDark) "#d4d4d8" else "#475569"
            val stroke = if (releaseStrategy.useDark) "#27272a" else "#cbd5e1"

            val isGa = release.type.toString().startsWith("G")
            val animClass = if (animated) " rm-enter" else ""
            val animDelay = if (animated) "style=\"animation-delay:${(index * 90)}ms\"" else ""

            val detailsLines = StringBuilder()
            var lineY = 72
            release.lines.forEach { line ->
                detailsLines.append("""<text x="40" y="$lineY" class="rm-details" fill="$trayText">• ${line.escapeXml()}</text>""")
                lineY += 24
            }

            sb.append(
                """
                <g transform="translate(0,$y)" class="rm-item$animClass" $animDelay>
                    <rect width="$contentWidth" height="$cardHeight" rx="16" fill="$cardBg" stroke="${if (isGa) accent else stroke}" stroke-width="${if (isGa) "2" else "1.2"}"/>

                    <g transform="translate(28,24)">
                        <rect width="48" height="48" rx="12" fill="$accent" opacity="0.12"/>
                        <text x="24" y="32" text-anchor="middle" class="rm-badge" fill="$accent">${release.type.toString().take(2).uppercase()}</text>
                    </g>

                    <g transform="translate(96,50)">
                        <text class="rm-goal" fill="$textColor">${release.goal.escapeXml()}</text>
                        <text y="24" class="rm-date" fill="$dateColor">${release.date.escapeXml()}</text>
                    </g>

                    <g transform="translate(0,${cardHeight + trayGap})">
                        <rect width="$contentWidth" height="${layout.trayHeight}" rx="16" fill="$trayBg" stroke="$stroke" stroke-dasharray="4 4"/>
                        <text x="40" y="40" class="rm-detail-head" fill="$accent">Implementation Details</text>
                        $detailsLines
                    </g>
                </g>
                """.trimIndent()
            )

            y += layout.blockHeight
        }

        return sb.toString()
    }

    private fun svgDefs(releaseStrategy: ReleaseStrategy, svgId: String, idPrefix: String): String {
        val fontUrl = "https://fonts.googleapis.com/css2?family=Bebas+Neue&amp;family=Alegreya+Sans:wght@400;700;800&amp;display=swap"
        val bgStart = if (releaseStrategy.useDark) "#0b0b12" else "#f8fafc"
        val bgMid = if (releaseStrategy.useDark) "#111827" else "#eef2f7"
        val bgEnd = if (releaseStrategy.useDark) "#09090b" else "#f1f5f9"
        val glowColor = if (releaseStrategy.useDark) "#1d4ed8" else "#7fb3ff"
        val dotColor = if (releaseStrategy.useDark) "#ffffff" else "#0f172a"
        val enterName = "rm_in_$idPrefix"

        return """
            <defs>
                <style>
                    @import url('$fontUrl');

                    #$svgId .rm-title { font-family:'Bebas Neue', sans-serif; font-size:44px; font-weight:400; letter-spacing:-0.01em; }
                    #$svgId .rm-badge { font-family:'Alegreya Sans', sans-serif; font-size:14px; font-weight:800; letter-spacing:0.08em; }
                    #$svgId .rm-goal { font-family:'Alegreya Sans', sans-serif; font-size:21px; font-weight:800; letter-spacing:0.005em; }
                    #$svgId .rm-date { font-family:'Alegreya Sans', sans-serif; font-size:13px; font-weight:700; letter-spacing:0.05em; }
                    #$svgId .rm-detail-head { font-family:'Alegreya Sans', sans-serif; font-size:12px; font-weight:800; letter-spacing:0.08em; text-transform:uppercase; }
                    #$svgId .rm-details { font-family:'Alegreya Sans', sans-serif; font-size:15px; font-weight:400; }

                    @keyframes $enterName {
                        from { opacity: 0; transform: translateY(10px); }
                        to { opacity: 1; transform: translateY(0); }
                    }

                    #$svgId .rm-enter {
                        animation: $enterName 420ms cubic-bezier(0.22, 1, 0.36, 1) both;
                    }
                </style>

                <linearGradient id="${idPrefix}_bg" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="$bgStart"/>
                    <stop offset="50%" stop-color="$bgMid"/>
                    <stop offset="100%" stop-color="$bgEnd"/>
                </linearGradient>

                <radialGradient id="${idPrefix}_glow" cx="0.18" cy="0.08" r="0.65">
                    <stop offset="0%" stop-color="$glowColor" stop-opacity="${if (releaseStrategy.useDark) "0.28" else "0.24"}"/>
                    <stop offset="75%" stop-color="$bgStart" stop-opacity="0"/>
                </radialGradient>

                <radialGradient id="${idPrefix}_vignette" cx="0.5" cy="0.5" r="0.85">
                    <stop offset="0%" stop-color="#000000" stop-opacity="0"/>
                    <stop offset="100%" stop-color="#000000" stop-opacity="${if (releaseStrategy.useDark) "0.34" else "0.14"}"/>
                </radialGradient>

                <pattern id="${idPrefix}_dots" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                    <circle cx="2" cy="2" r="1" fill="$dotColor" opacity="${if (releaseStrategy.useDark) "0.11" else "0.12"}"/>
                </pattern>
            </defs>
        """.trimIndent()
    }

    private fun slug(value: String): String {
        return value.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
            .ifBlank { "roadmap" }
            .take(36)
    }
}

fun releaseStroke(release: Release, releaseStrategy: ReleaseStrategy): String = when {
    release.type.toString().startsWith("M") -> releaseStrategy.displayConfig.circleColors[0]
    release.type.toString().startsWith("R") -> releaseStrategy.displayConfig.circleColors[1]
    release.type.toString().startsWith("G") -> releaseStrategy.displayConfig.circleColors[2]
    else -> "#3b82f6"
}