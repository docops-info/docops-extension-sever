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
 * ReleaseRoadMapMaker creates a distinctive, mechanical release roadmap SVG.
 * It features a "push-down" reveal interaction and follows high-end typography rules.
 */
class ReleaseRoadMapMaker {

    fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean, animate: String): String {
        return createSvg(releaseStrategy, isPdf, animate)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false, animate: String): String {
        val id = Uuid.random().toHexString().replace("-", "")

        // Calculate heights dynamically - NOW 8-POINT GRID COMPLIANT
        val itemBaseHeight = 120      // 15 × 8 ✓
        val headerHeight = 144        // 18 × 8 (was 140)
        val footerPadding = 96        // 12 × 8 (was 100)

        val totalExpansionHeight = releaseStrategy.releases.sumOf {
            val trayHeight = 80 + (it.lines.size * 24)  // 24 instead of 26 (3 × 8)
            trayHeight - 16   // 16 instead of 15 (2 × 8)
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
                    <rect width="1200" height="$height" fill="url(#roadmap_bg)" opacity="${if (releaseStrategy.useDark) "0.8" else "1"}"/>
                    <rect width="1200" height="$height" fill="url(#roadmap_glow)" opacity="${if (releaseStrategy.useDark) "0.3" else "0.5"}"/>
                    <rect width="1200" height="$height" fill="url(#pattern_Roadmap_Dots)" opacity="${if (releaseStrategy.useDark) "0.18" else "0.35"}"/>
                    <rect width="1200" height="$height" fill="url(#roadmap_vignette)" opacity="${if (releaseStrategy.useDark) "0.45" else "0.35"}"/>

                    <!-- Header Section -->
                    <g transform="translate(96, 56)">
                        <text class="roadmap-header-title" fill="$titleColor">${releaseStrategy.title.escapeXml()}</text>
                        <rect y="16" width="48" height="4" fill="#3b82f6" rx="2"/>
                    </g>
                
                    <!-- Roadmap Items Container -->
                    <g transform="translate(96, $headerHeight)">
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
        val cardBg = if(releaseStrategy.useDark) "#18181b" else "#fafafa"  // Changed from #ffffff
        val textColor = if(releaseStrategy.useDark) "#f4f4f5" else "#1e293b"
        val secondaryText = "#71717a"

        // Check if this is the GA release for emphasis
        val isGaRelease = release.type.toString().startsWith("G")
        val cardClass = if (isGaRelease) "mechanical-card ga-card" else "mechanical-card"

        val trayHeight = 80 + (release.lines.size * 24)  // 24 instead of 26
        val expansionShift = 120 + trayHeight - 16       // 16 instead of 15

        val detailContent = StringBuilder()
        release.lines.forEach { line ->
            detailContent.append("""<tspan x="40" dy="24">• ${line.escapeXml()}</tspan>""")
        }

        return """
                <g class="roadmap-group" style="--expansion-shift: ${expansionShift}px; animation-delay: ${index * 0.1}s">
                    <g class="item-display">
                        <g class="details-tray">
                            <rect width="1000" height="$trayHeight" fill="${if(releaseStrategy.useDark) "#0c0c0e" else "#f8fafc"}" rx="16" stroke="#27272a" stroke-dasharray="4 4"/>
                            <text class="details-content" fill="${if(releaseStrategy.useDark) "#d4d4d8" else "#475569"}">
                                <tspan x="40" dy="48" font-weight="700" fill="$accentColor" text-transform="uppercase" font-size="11">Implementation Details</tspan>
                                $detailContent
                            </text>
                        </g>
                        <g class="$cardClass" onclick="this.closest('.roadmap-group').classList.toggle('collapsed')">
                            <rect class="card-rect" width="1000" height="96" rx="16" fill="$cardBg"/>                        <g transform="translate(32, 24)">
                            <rect width="48" height="48" rx="12" fill="$accentColor" opacity="0.1"/>
                            <text x="24" y="32" text-anchor="middle" class="id-badge" fill="$accentColor">${release.type.toString().take(2).uppercase()}</text>
                        </g>
                        <g transform="translate(96, 48)">
                            <text class="release-goal" fill="$textColor">${release.goal.escapeXml()}</text>
                            <text y="24" class="release-date" fill="$secondaryText">${release.date.escapeXml()}</text>
                        </g>
                        <path class="chevron" d="M0,0 L8,8 L0,16" fill="none" stroke="$secondaryText" stroke-width="2.5" stroke-linecap="round"/>
                    </g>
                </g>
                ${if (nextItems.isNotEmpty()) """
                <g class="roadmap-nest" transform = "translate(0, ${expansionShift})">
                $nextItems
                </g>
                """.trimIndent() else ""}
            </g>
        """.trimIndent()
    }



    private fun svgDefs(releaseStrategy: ReleaseStrategy): String {
        val fontUrl = "https://fonts.googleapis.com/css2?family=Bebas+Neue&amp;family=Alegreya+Sans:wght@400;700;800&amp;display=swap"

        return """
                <defs>
                    <style>
                        @import url('$fontUrl');
                
                        .roadmap-header-title { font-family: 'Bebas Neue', sans-serif; font-size: 42px; font-weight: 400; letter-spacing: 0.02em; }
                        .id-badge { font-family: 'Alegreya Sans', sans-serif; font-size: 14px; font-weight: 800; letter-spacing: 0.08em; }
                        .release-goal { font-family: 'Alegreya Sans', sans-serif; font-size: 20px; font-weight: 800; letter-spacing: 0.01em; }
                        .release-date { font-family: 'Alegreya Sans', sans-serif; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.12em; }
                        .details-content { font-family: 'Alegreya Sans', sans-serif; font-size: 14px; }

                        @keyframes slideIn {
                            from { opacity: 0; transform: translateY(16px); }
                            to { opacity: 1; transform: translateY(0); }
                        }
                    
                        .roadmap-group { 
                            transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
                            animation: slideIn 0.5s ease-out forwards;
                            opacity: 0;
                        }
                        .roadmap-nest { transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1); }
                        .mechanical-card { cursor: pointer; pointer-events: auto; }
                        .card-rect { stroke: #27272a; stroke-opacity: 0.3; transition: all 0.3s ease; }
                
                        /* Expanded by default - details tray visible */
                        .details-tray { 
                            opacity: 1; 
                            visibility: visible; 
                            transition: all 0.4s ease; 
                            transform: translateY(104px); 
                            pointer-events: auto;
                        }
                
                        /* Chevron pointing down when expanded */
                        .chevron { transition: all 0.3s ease; transform-origin: center; transform: translate(944px, 40px) rotate(90deg); }

                        /* Nested items pushed down by default */
                        .roadmap-nest {
                            transform: translateY(var(--expansion-shift));
                        }

                        /* Collapsed States - When user clicks to collapse */
                        .collapsed > .item-display .details-tray { 
                            opacity: 0; 
                            visibility: hidden; 
                            transform: translateY(80px); 
                            pointer-events: none;
                        }
                        .collapsed > .item-display .chevron { 
                            transform: translate(944px, 40px) rotate(0deg); 
                        }
                        .collapsed > .roadmap-nest {
                            transform: translateY(120px);
                        }
                    
                        /* GA Card Emphasis - Make it 3x more obvious */
                        .ga-card .card-rect { 
                            stroke: ${releaseStrategy.displayConfig.circleColors[2]}; 
                            stroke-opacity: 0.6;
                            stroke-width: 2;
                        }
                        .ga-card:hover .card-rect {
                            filter: drop-shadow(0 0 8px ${releaseStrategy.displayConfig.circleColors[2]});
                        }
                    </style>
                    <linearGradient id="roadmap_bg" x1="0" y1="0" x2="1" y2="1">
                        <stop offset="0%" stop-color="${if(releaseStrategy.useDark) "#0b0b12" else "#f9fafb"}"/>
                        <stop offset="45%" stop-color="${if(releaseStrategy.useDark) "#111827" else "#eef2f7"}"/>
                        <stop offset="100%" stop-color="${if(releaseStrategy.useDark) "#09090b" else "#f7f8fb"}"/>
                    </linearGradient>
                    <radialGradient id="roadmap_glow" cx="0.2" cy="0.1" r="0.6">
                        <stop offset="0%" stop-color="${if(releaseStrategy.useDark) "#1d4ed8" else "#93c5fd"}" stop-opacity="0.35"/>
                        <stop offset="70%" stop-color="${if(releaseStrategy.useDark) "#0b0b12" else "#f9fafb"}" stop-opacity="0"/>
                    </radialGradient>
                    <radialGradient id="roadmap_vignette" cx="0.5" cy="0.5" r="0.8">
                        <stop offset="0%" stop-color="#000000" stop-opacity="0"/>
                        <stop offset="100%" stop-color="#000000" stop-opacity="${if(releaseStrategy.useDark) "0.35" else "0.18"}"/>
                    </radialGradient>
                    <pattern id="pattern_Roadmap_Dots" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                        <circle cx="2" cy="2" r="1" fill="${if(releaseStrategy.useDark) "#ffffff" else "#000000"}" opacity="${if (releaseStrategy.useDark) "0.12" else "0.2"}"/>
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
