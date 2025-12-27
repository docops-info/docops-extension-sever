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
import java.util.*

/**
 * ReleaseRoadMapMaker creates a distinctive, mechanical release roadmap SVG.
 * It features a "push-down" reveal interaction and follows high-end typography rules.
 */
class ReleaseRoadMapMaker {

    fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean, animate: String): String {
        return createSvg(releaseStrategy, isPdf, animate)
    }

    private fun createSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false, animate: String): String {
        val id = UUID.randomUUID().toString().replace("-", "")

        // Calculate heights dynamically
        val itemBaseHeight = 120
        val headerHeight = 140
        val footerPadding = 100
        
        val totalExpansionHeight = releaseStrategy.releases.sumOf { 
            val trayHeight = 80 + (it.lines.size * 26)
            trayHeight - 15 
        }
        val totalBaseItemsHeight = releaseStrategy.releases.size * itemBaseHeight
        
        val height = if (releaseStrategy.releases.isEmpty()) 200 else headerHeight + totalBaseItemsHeight + totalExpansionHeight + footerPadding

        val cardsStr = buildNestedItems(releaseStrategy.releases, 0, id, releaseStrategy)

        val bgColor = if(releaseStrategy.useDark) "#09090b" else "#f8fafc"
        val titleColor = if(releaseStrategy.useDark) "#f4f4f5" else "#0f172a"

        return """
            <svg xmlns="http://www.w3.org/2000/svg" 
                 width="${releaseStrategy.scale * 1200}" height="${height * releaseStrategy.scale}"
                 viewBox="0 0 1200 $height">
                 <desc>Mechanical Roadmap - DocOps Professional</desc>
                 ${if (!isPdf) svgDefs(releaseStrategy) else ""}
                 <g transform="scale(${releaseStrategy.scale})">
                    <!-- Atmospheric Background -->
                    <rect width="1200" height="$height" fill="$bgColor"/>
                    <rect width="1200" height="$height" fill="url(#pattern_Roadmap_Dots)" opacity="0.4"/>

                    <!-- Header Section -->
                    <g transform="translate(100, 60)">
                        <text class="roadmap-header-title" fill="$titleColor">${releaseStrategy.title.escapeXml()}</text>
                        <rect y="15" width="40" height="4" fill="#3b82f6" rx="2"/>
                    </g>
                    
                    <!-- Roadmap Items Container -->
                    <g transform="translate(100, 140)">
                        $cardsStr
                    </g>
                 </g>
            </svg>
        """.trimIndent()
    }

    private fun buildNestedItems(releases: List<Release>, index: Int, id: String, releaseStrategy: ReleaseStrategy): String {
        if (index >= releases.size) return ""
        val release = releases[index]
        val nextItems = buildNestedItems(releases, index + 1, id, releaseStrategy)

        val accentColor = releaseStroke(release, releaseStrategy)
        val cardBg = if(releaseStrategy.useDark) "#18181b" else "#ffffff"
        val textColor = if(releaseStrategy.useDark) "#f4f4f5" else "#1e293b"
        val secondaryText = "#71717a"

        val trayHeight = 80 + (release.lines.size * 26)
        val expansionShift = 120 + trayHeight - 15

        val detailContent = StringBuilder()
        release.lines.forEach { line ->
            detailContent.append("""<tspan x="40" dy="26">• ${line.escapeXml()}</tspan>""")
        }

        return """
            <g class="roadmap-group" style="--expansion-shift: ${expansionShift}px">
                <g class="item-display">
                    <g class="details-tray">
                        <rect width="1000" height="$trayHeight" fill="${if(releaseStrategy.useDark) "#0c0c0e" else "#f8fafc"}" rx="20" stroke="#27272a" stroke-dasharray="4 4"/>
                        <text class="details-content" fill="${if(releaseStrategy.useDark) "#d4d4d8" else "#475569"}">
                            <tspan x="40" dy="45" font-weight="700" fill="$accentColor" text-transform="uppercase" font-size="11">Implementation Details</tspan>
                            $detailContent
                        </text>
                    </g>
                    <g class="mechanical-card" onclick="this.closest('.roadmap-group').classList.toggle('active')">
                        <rect class="card-rect" width="1000" height="100" rx="20" fill="$cardBg"/>
                        <g transform="translate(30, 25)">
                            <rect width="50" height="50" rx="14" fill="$accentColor" opacity="0.1"/>
                            <text x="25" y="32" text-anchor="middle" class="id-badge" fill="$accentColor">${release.type.toString().take(2).uppercase()}</text>
                        </g>
                        <g transform="translate(100, 45)">
                            <text class="release-goal" fill="$textColor">${release.goal.escapeXml()}</text>
                            <text y="24" class="release-date" fill="$secondaryText">${release.date.escapeXml()}</text>
                        </g>
                        <path class="chevron" d="M0,0 L8,8 L0,16" fill="none" stroke="$secondaryText" stroke-width="2.5" stroke-linecap="round"/>
                    </g>
                </g>
                ${if (nextItems.isNotEmpty()) """
                <g class="roadmap-nest" transform="translate(0, 120)">
                    $nextItems
                </g>
                """.trimIndent() else ""}
            </g>
        """.trimIndent()
    }

    private fun svgDefs(releaseStrategy: ReleaseStrategy): String {
        val fontUrl = "https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@800&amp;family=Outfit:wght@400;600&amp;display=swap"

        return """
            <defs>
                <style>
                    @import url('$fontUrl');
                    
                    .roadmap-header-title { font-family: 'Plus Jakarta Sans', sans-serif; font-size: 32px; font-weight: 800; letter-spacing: -0.04em; }
                    .id-badge { font-family: 'Outfit', sans-serif; font-size: 14px; font-weight: 700; }
                    .release-goal { font-family: 'Plus Jakarta Sans', sans-serif; font-size: 19px; font-weight: 700; }
                    .release-date { font-family: 'Outfit', sans-serif; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
                    .details-content { font-family: 'Outfit', sans-serif; font-size: 14px; }

                    .roadmap-group { transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1); }
                    .roadmap-nest { transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1); }
                    .mechanical-card { cursor: pointer; pointer-events: auto; }
                    .card-rect { stroke: #27272a; stroke-opacity: 0.3; transition: all 0.3s ease; }
                    
                    .details-tray { 
                        opacity: 0; 
                        visibility: hidden; 
                        transition: all 0.4s ease; 
                        transform: translateY(85px); 
                    }
                    
                    .chevron { transition: all 0.3s ease; transform-origin: center; transform: translate(950px, 45px); }

                    /* Active States */
                    .active > .item-display .details-tray { opacity: 1; visibility: visible; transform: translateY(105px); pointer-events: auto; }
                    .active > .item-display .chevron { transform: translate(950px, 45px) rotate(90deg); }
                    .active > .item-display .card-rect { stroke: #3b82f6; stroke-opacity: 0.8; }
                    
                    /* NESTED MECHANICAL PUSH:
                       When a group is active, its nested child group (roadmap-nest) shifts down.
                       This naturally stacks when multiple levels are active. */
                    .roadmap-group.active > .roadmap-nest {
                        transform: translateY(var(--expansion-shift));
                    }
                    
                    /* Handle multiple active items by summing the transform (Note: limited in CSS-only SVG) 
                       To support multiple open items perfectly, we shift by the index * base + expansion */
                </style>
                <!-- ... patterns ... -->
                <pattern id="pattern_Roadmap_Dots" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                    <circle cx="2" cy="2" r="1" fill="${if(releaseStrategy.useDark) "#ffffff" else "#000000"}" opacity="0.2"/>
                </pattern>
            </defs>
        """.trimIndent()
    }

}
fun releaseStroke(release: Release, releaseStrategy: ReleaseStrategy): String = when {
    release.type.toString().startsWith("M") -> releaseStrategy.displayConfig.circleColors[0]
    release.type.toString().startsWith("R") -> releaseStrategy.displayConfig.circleColors[1]
    release.type.toString().startsWith("G") -> releaseStrategy.displayConfig.circleColors[2]
    else -> "#3b82f6"
}

fun carColor(release: Release, releaseStrategy: ReleaseStrategy): String = when {
    release.type.toString().startsWith("M") -> releaseStrategy.displayConfig.carColors[0]
    release.type.toString().startsWith("R") -> releaseStrategy.displayConfig.carColors[1]
    release.type.toString().startsWith("G") -> releaseStrategy.displayConfig.carColors[2]
    else -> "#1e293b"
}