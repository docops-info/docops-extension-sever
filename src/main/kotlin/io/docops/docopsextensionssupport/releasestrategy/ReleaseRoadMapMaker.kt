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
import io.docops.docopsextensionssupport.roadmap.linesToUrlIfExist
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import java.util.*

/**
 * ReleaseRoadMapMaker is a class that creates a release roadmap SVG image based on a given release strategy.
 */
class ReleaseRoadMapMaker {

    /**
     * Creates a string representing an SVG image using the provided release strategy, PDF flag, and animation type.
     *
     * @param releaseStrategy The release strategy to use.
     * @param isPdf True if the SVG image should be in PDF format, false otherwise.
     * @param animate The type of animation to include in the SVG image.
     * @return A string representing the SVG image.
     */
    fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean, animate: String): String {
        return createSvg(releaseStrategy, isPdf, animate)
    }
    private fun createSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false, animate: String): String {
        val id = UUID.randomUUID().toString()
        val str = StringBuilder()

        // Calculate height based on number of releases
        // Each iOS card is 160px tall (140px + 20px margin)
        var startY = 0
        var height = 80 // Title area
        if (releaseStrategy.releases.isNotEmpty()) {
            height += (160 * releaseStrategy.releases.size) + 40 // Add padding at bottom
        }

        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(strat(release, startY, index, animate, id, releaseStrategy, isPdf))
            startY += 160 // Each card is 160px tall (140px + 20px margin)
        }

        // Determine colors based on dark mode
        var iosBgColor = if(releaseStrategy.useDark) "#000000" else "#f2f2f7"
        var iosTextColor = if(releaseStrategy.useDark) "#ffffff" else "#1c1c1e"

        return """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                 width="${releaseStrategy.scale * 1200}" height="${height * releaseStrategy.scale}"
                 viewBox="0 0 ${releaseStrategy.scale * 1200} ${height * releaseStrategy.scale}">
                 <desc>iOS Card Style Release Roadmap - https://docops.io/extension</desc>
                 ${svgDefs(isPdf,releaseStrategy)}
                 <g transform="scale(${releaseStrategy.scale})">
                 <!-- iOS-style background -->
                 <rect width="1200" height="$height" fill="$iosBgColor" class="ios-bg"/>

                 <!-- Title -->
                 <text x="600" text-anchor="middle" y="40" font-size="28px"
                       font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', Helvetica, Arial, sans-serif"
                       font-weight="600" fill="$iosTextColor" class="ios-text">${releaseStrategy.title.escapeXml()}</text>
                 $str
                 </g>
            </svg>
        """.trimIndent()
    }

    private fun strat(release: Release, startY: Int, index: Int, animate: String, id: String, releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String {
        // Determine colors based on dark mode
        val iosCardBg = if(releaseStrategy.useDark) "#1c1c1e" else "#ffffff"
        val iosCardStroke = if(releaseStrategy.useDark) "#38383a" else "#e5e5ea"
        val iosTextColor = if(releaseStrategy.useDark) "#ffffff" else "#1c1c1e"
        val iosSecondaryText = if(releaseStrategy.useDark) "#8e8e93" else "#8e8e93"

        // Build detail content with text wrapping and limit for PDF compatibility
        val detailContent = StringBuilder()
        val maxLines = if (isPdf) 8 else 12 // Limit number of lines for PDF output
        var lineCount = 0
        var hasMoreContent = false

        // Process each line
        outer@ for (line in release.lines) {
            // Apply text wrapping to each line
            val wrappedLines = itemTextWidth(line, 700F, 14)

            // First line with bullet point
            if (lineCount < maxLines) {
                detailContent.append("<tspan x=\"280\" dy=\"16\">• ${wrappedLines.firstOrNull() ?: ""}</tspan>")
                lineCount++
            } else {
                hasMoreContent = true
                break@outer
            }

            // Subsequent lines indented
            for (wrappedLine in wrappedLines.drop(1)) {
                if (lineCount < maxLines) {
                    detailContent.append("<tspan x=\"295\" dy=\"16\">$wrappedLine</tspan>")
                    lineCount++
                } else {
                    hasMoreContent = true
                    break@outer
                }
            }
        }

        // Add "show more" indicator if content was truncated
        if (hasMoreContent) {
            detailContent.append("<tspan x=\"280\" dy=\"20\" font-style=\"italic\" fill=\"${if(releaseStrategy.useDark) "#8e8e93" else "#8e8e93"}\">... (more details available)</tspan>")
        }

        // Prepare goal content
        val itemArray = itemTextWidth(release.goal, 900F, 24)
        val lines = linesToUrlIfExist(itemArray, mutableMapOf())

        // Determine milestone color based on release type
        val milestoneColor = releaseStroke(release, releaseStrategy)

        // Check if completed
        var completedMark = ""
        if(release.completed) {
            completedMark = "<use xlink:href=\"#iosCheck\" x=\"1020\" y=\"75\" width=\"20\" height=\"20\"/>"
        }

        // iOS-style card with toggle functionality
        return """
            <g id="card${id}_$index" transform="translate(0,${startY + 80})" class="ios-card" onclick="toggleCardDetail('card${id}_$index')">
                <rect x="100" y="20" height="140" width="1000" fill="$iosCardBg" stroke="$iosCardStroke" stroke-width="1"
                      rx="16" ry="16" filter="url(#iosCardShadow)" class="ios-card-bg"/>
                <!-- This rect expands when detail view is active -->
                <rect x="100" y="20" height="0" width="1000" fill="$iosCardBg" stroke="$iosCardStroke" stroke-width="1"
                      rx="16" ry="16" filter="url(#iosCardShadow)" class="ios-card-expand-bg"/>

                <!-- iOS-style milestone indicator -->
                <g class="ios-milestone">
                    <circle cx="200" cy="90" r="35" fill="$milestoneColor" opacity="0.1"/>
                    <circle cx="200" cy="90" r="30" fill="$milestoneColor" stroke="#ffffff" stroke-width="3"/>
                    <text x="200" y="96" dominant-baseline="middle" text-anchor="middle"
                          font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', Helvetica, Arial, sans-serif"
                          font-weight="600" font-size="12px" fill="white">${release.type}</text>
                </g>

                <!-- Release date -->
                <text x="200" y="140" text-anchor="middle"
                      font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', Helvetica, Arial, sans-serif"
                      font-size="12px" font-weight="500" fill="$iosSecondaryText" class="ios-secondary-text">${release.date}</text>

                <!-- Goal Content (Default View) -->
                <g class="goal-content">
                    <text x="280" y="75" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', Helvetica, Arial, sans-serif"
                          font-size="18px" font-weight="500" fill="$iosTextColor" class="ios-text">
                        <tspan x="280" dy="0">${lines.firstOrNull() ?: ""}</tspan>
                        ${if (lines.size > 1) "<tspan x=\"280\" dy=\"22\">${lines.getOrNull(1) ?: ""}</tspan>" else ""}
                    </text>

                    <!-- Completion indicator -->
                    $completedMark

                    <!-- iOS-style chevron -->
                    <path class="chevron-right" d="M1050 85 L1060 90 L1050 95" stroke="$iosSecondaryText" stroke-width="2"
                          fill="none" stroke-linecap="round" stroke-linejoin="round" opacity="0.6"/>
                </g>

                <!-- Detail Content (Hidden by default) -->
                <g class="detail-content">
                    <!-- Back button -->
                    <g class="back-button" transform="translate(1020, 75)">
                        <circle r="15" fill="$iosCardBg" stroke="$iosCardStroke" stroke-width="1"/>
                        <path d="M5 0 L-5 0 M0 -5 L-5 0 L0 5" stroke="#007aff" stroke-width="2"
                              fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                    </g>

                    <!-- Detail content using native SVG text for PDF compatibility -->
                    <text x="280" y="70" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', Helvetica, Arial, sans-serif"
                          font-size="14px" font-weight="400" fill="${if(releaseStrategy.useDark) "#ffffff" else "#1c1c1e"}" class="ios-text detail-text">
                        $detailContent
                    </text>
                </g>
            </g>
        """.trimIndent()
    }

    private fun svgDefs(isPdf: Boolean, releaseStrategy: ReleaseStrategy): String {
        val ani = """ fill: transparent; stroke-width: 10px; stroke-dasharray: 471; stroke-dashoffset: 471; animation: clock-animation 2s linear infinite;""".trimIndent()
        // Determine hover colors based on dark mode
        val hoverFill = if(releaseStrategy.useDark) "lightblue" else "#2563eb"
        val shadowOpacity = if(releaseStrategy.useDark) "0.4" else "0.2"

        // iOS-style colors
        val iosBgColor = if(releaseStrategy.useDark) "#000000" else "#f2f2f7"
        val iosCardBg = if(releaseStrategy.useDark) "#1c1c1e" else "#ffffff"
        val iosCardStroke = if(releaseStrategy.useDark) "#38383a" else "#e5e5ea"
        val iosTextColor = if(releaseStrategy.useDark) "#ffffff" else "#1c1c1e"
        val iosSecondaryText = if(releaseStrategy.useDark) "#8e8e93" else "#8e8e93"

        //language=html
        var style = """
            <style>
                    /* iOS-style card styling */
                    .ios-card {
                        transition: all 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
                        cursor: pointer;
                    }
                    .ios-card:hover .ios-card-bg {
                        filter: url(#iosCardHoverShadow);
                        transform: translateY(-1px);
                    }
                    .ios-card:hover .ios-milestone circle:last-child {
                        transform: scale(1.05);
                        transition: transform 0.2s ease;
                    }

                    /* Content transition states */
                    .goal-content {
                        opacity: 1;
                        transition: opacity 0.3s ease;
                    }
                    .detail-content {
                        opacity: 0;
                        transition: opacity 0.3s ease;
                        pointer-events: none;
                    }

                    /* When detail is active */
                    .detail-active .goal-content {
                        opacity: 0;
                        pointer-events: none;
                    }
                    .detail-active .detail-content {
                        opacity: 1;
                        pointer-events: auto;
                    }
                    .detail-active .chevron-right {
                        opacity: 0;
                    }
                    .detail-active .back-button {
                        opacity: 1;
                        pointer-events: auto;
                    }

                    /* Card expansion when detail is active */
                    .ios-card-expand-bg {
                        height: 0;
                        /*opacity: 0;*/
                        transition: height 0.3s ease, opacity 0.3s ease;
                    }
                    .detail-active .ios-card-expand-bg {
                        height: ${if(isPdf) "80px" else "120px"};
                        opacity: 1;
                    }
                    .detail-active .ios-card-bg {
                        height: ${if(isPdf) "220px" else "260px"};
                    }

                    /* Detail text styles for PDF compatibility */
                    .detail-text {
                        opacity: 0;
                        transition: opacity 0.3s ease;
                    }
                    .detail-active .detail-text {
                        opacity: 1;
                    }

                    /* Back button states */
                    .back-button {
                        opacity: 0;
                        pointer-events: none;
                        transition: opacity 0.3s ease;
                        cursor: pointer;
                    }
                    .back-button:hover circle {
                        fill: ${if(releaseStrategy.useDark) "#2c2c2e" else "#f0f0f0"};
                    }

                    /* Chevron states */
                    .chevron-right {
                        transition: opacity 0.3s ease;
                    }

                    /* Legacy styling with improved readability */
                    .milestone:hover { cursor: pointer; stroke-width: 16; stroke-opacity: 1; fill: $hoverFill; }
                    .milestone { font-size: 60px; font-weight: bold; font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', Helvetica, Arial, sans-serif; }
                    .milestoneDate { font-size: 18px; font-weight: bold; font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', Helvetica, Arial, sans-serif; }
                    .bev:hover { $ani stroke: ${releaseStrategy.displayConfig.circleColors[0]}; } 
                    .bev2:hover { $ani stroke: ${releaseStrategy.displayConfig.circleColors[1]}; } 
                    .bev3:hover { $ani stroke: ${releaseStrategy.displayConfig.circleColors[2]}; }
                    .row { filter: drop-shadow(3px 5px 2px rgb(0 0 0 / $shadowOpacity)); transition: all 0.3s ease; }
                    .row:hover { filter: drop-shadow(4px 6px 3px rgb(0 0 0 / ${shadowOpacity.toDouble() + 0.1})); }

                    /* Support for light/dark mode */
                    @media (prefers-color-scheme: dark) {
                        .auto-dark-text { fill: #fcfcfc !important; }
                        .auto-dark-bg { fill: url(#dmode1) !important; }
                        .auto-dark-stroke { stroke: #444444 !important; }
                        .auto-dark-circle { fill: #2c3033 !important; }
                        .ios-bg { fill: #000000 !important; }
                        .ios-card-bg { fill: #1c1c1e !important; stroke: #38383a !important; }
                        .ios-text { fill: #ffffff !important; }
                        .ios-secondary-text { fill: #8e8e93 !important; }
                    }

                    @media (prefers-color-scheme: light) {
                        .auto-dark-text { fill: #073763 !important; }
                        .auto-dark-bg { fill: url(#lmode1) !important; }
                        .auto-dark-stroke { stroke: #cccccc !important; }
                        .auto-dark-circle { fill: #ffffff !important; }
                        .ios-bg { fill: #f2f2f7 !important; }
                        .ios-card-bg { fill: #ffffff !important; stroke: #e5e5ea !important; }
                        .ios-text { fill: #1c1c1e !important; }
                        .ios-secondary-text { fill: #8e8e93 !important; }
                    }

                    @keyframes clock-animation {
                        0% {
                            stroke-dashoffset: 471;
                        }
                        100% {
                            stroke-dashoffset: 0;
                        }
                    }
                    .box1Clicked { transition-timing-function: ease-out; transition: 1.25s; transform: translateX(0%); }
                    .box2Clicked { transition-timing-function: ease-out; transition: 2.25s; transform: translateX(-330%); }
                    </style>
                    <script>
                     function toggleItem(item1, item2) {
                        var elem2 = document.querySelector("#"+item2);
                        elem2.classList.toggle("box2Clicked");
                        var elem = document.querySelector("#"+item1);
                        elem.classList.toggle("box1Clicked");
                    }

                    function toggleCardDetail(cardId) {
                        const card = document.getElementById(cardId);
                        card.classList.toggle('detail-active');
                    }

                    // Function to toggle between light and dark mode manually
                    function toggleDarkMode() {
                        document.body.classList.toggle('dark-mode');
                        const elements = document.querySelectorAll('.auto-dark-text, .auto-dark-bg, .auto-dark-stroke, .auto-dark-circle, .ios-bg, .ios-card-bg, .ios-text, .ios-secondary-text');
                        elements.forEach(el => {
                            el.classList.toggle('dark-active');
                        });
                    }
             </script>
        """.trimIndent()
        if(isPdf) {
            style = ""
        }
        val colors = StringBuilder()
        val shades = mutableMapOf(0 to "M", 1 to "R", 2 to "G")
        releaseStrategy.displayConfig.colors.forEachIndexed { index, s ->
            colors.append(SVGColor(s, "release${shades[index]}").linearGradient)
        }
        //language=svg
        return """
            <defs>
                    <filter xmlns="http://www.w3.org/2000/svg" id="filter1">
                        <feGaussianBlur stdDeviation="1.75"/>
                    </filter>
                    <filter xmlns="http://www.w3.org/2000/svg" id="filter2">
                        <feGaussianBlur stdDeviation="0.35"/>
                    </filter>
                    <filter id="Bevel" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                        <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                        <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut"
                                            lighting-color="white">
                            <fePointLight x="-5000" y="-10000" z="20000"/>
                        </feSpecularLighting>
                        <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                        <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                                     result="litPaint"/>
                    </filter>
                    <filter id="Bevel3" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                        <feGaussianBlur in="SourceAlpha" stdDeviation="0.2" result="blur"/>
                        <feSpecularLighting in="blur" surfaceScale="10" specularConstant="3.5" specularExponent="10"
                                            result="specOut" lighting-color="#ffffff">
                            <fePointLight x="-5000" y="-10000" z="0000"/>
                        </feSpecularLighting>
                        <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                        <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                                     result="litPaint"/>
                    </filter>
                    <!-- Modern drop shadow filter -->
                    <filter id="modern-shadow" x="-20%" y="-20%" width="140%" height="140%">
                        <feDropShadow dx="2" dy="4" stdDeviation="3" flood-opacity="0.3" flood-color="#000000"/>
                    </filter>

                    <!-- iOS-style shadows -->
                    <filter id="iosCardShadow" x="-50%" y="-50%" width="200%" height="200%">
                        <feGaussianBlur in="SourceAlpha" stdDeviation="4"/>
                        <feOffset dx="0" dy="1" result="offset"/>
                        <feFlood flood-color="#000000" flood-opacity="0.04"/>
                        <feComposite in2="offset" operator="in"/>
                        <feMerge>
                            <feMergeNode/>
                            <feMergeNode in="SourceGraphic"/>
                        </feMerge>
                    </filter>

                    <filter id="iosCardHoverShadow" x="-50%" y="-50%" width="200%" height="200%">
                        <feGaussianBlur in="SourceAlpha" stdDeviation="8"/>
                        <feOffset dx="0" dy="4" result="offset"/>
                        <feFlood flood-color="#000000" flood-opacity="0.08"/>
                        <feComposite in2="offset" operator="in"/>
                        <feMerge>
                            <feMergeNode/>
                            <feMergeNode in="SourceGraphic"/>
                        </feMerge>
                    </filter>

                    <!-- iOS Check Icon -->
                    <g id="iosCheck">
                        <circle r="10" fill="#34c759"/>
                        <polyline points="4 10 8 14 16 6" fill="none" stroke="#ffffff" stroke-width="2"
                                  stroke-linecap="round" stroke-linejoin="round"/>
                    </g>

                    <!-- Improved circle check gradient -->
                    <linearGradient id="circlecheck" x2="1" y2="1">
                        <stop class="stop1" offset="0%" stop-color="#a9d99a"/>
                        <stop class="stop2" offset="50%" stop-color="#7ec667"/>
                        <stop class="stop3" offset="100%" stop-color="#54B435"/>
                    </linearGradient>
                    <!-- Dark mode background gradient - modernized -->
                    <linearGradient id="dmode1" x2="0%" y2="100%">
                        <stop class="stop1" offset="0%" stop-color="#2d3748"/>
                        <stop class="stop2" offset="50%" stop-color="#1a202c"/>
                        <stop class="stop3" offset="100%" stop-color="#171923"/>
                    </linearGradient>
                    <!-- Light mode background gradient - modernized -->
                    <linearGradient id="lmode1" x2="0%" y2="100%">
                        <stop class="stop1" offset="0%" stop-color="#ffffff"/>
                        <stop class="stop2" offset="50%" stop-color="#f8f9fa"/>
                        <stop class="stop3" offset="100%" stop-color="#f1f3f5"/>
                    </linearGradient>
                    <filter id="filter-2">
                        <feMorphology in="SourceAlpha" operator="dilate" radius="2" result="OUTLINE"/>
                        <feComposite operator="out" in="OUTLINE" in2="SourceAlpha"/>
                    </filter>
                    <path id="curve" fill="transparent" d="M267,317a56,56 0 1,0 112,0a56,56 0 1,0 -112,0" />
                    <g id="completedCheck">
                        <polyline points="10 25 22 40 50 10" fill="none" stroke="url(#circlecheck)" stroke-width="6"/>
                    </g>
                    $colors
                    $style
                </defs>
        """.trimIndent()
    }

     private fun linearColor(release: Release): String = when {
        release.type.toString().startsWith("M") -> {
            "releaseM"
        }

        release.type.toString().startsWith("R") -> {
            "releaseR"
        }

        release.type.toString().startsWith("G") -> {
            "releaseG"
        }

        else -> ""
    }
    private fun linesToSpanText(lines: MutableList<String>, dy: Int, x: Int): String {
        val text = StringBuilder()
        lines.forEach {
            text.append("""<tspan x="$x" dy="$dy" text-anchor="middle" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif" font-size="24" font-weight="normal" class="auto-dark-text">$it</tspan>""")
        }
        return text.toString()
    }
}
fun releaseStroke(release: Release, releaseStrategy: ReleaseStrategy): String = when {
    release.type.toString().startsWith("M") -> {
        releaseStrategy.displayConfig.circleColors[0]
    }

    release.type.toString().startsWith("R") -> {
        releaseStrategy.displayConfig.circleColors[1]
    }

    release.type.toString().startsWith("G") -> {
        releaseStrategy.displayConfig.circleColors[2]
    }

    else -> ""
}
fun carColor(release: Release, releaseStrategy: ReleaseStrategy): String = when {
    release.type.toString().startsWith("M") -> {
        releaseStrategy.displayConfig.carColors[0]
    }

    release.type.toString().startsWith("R") -> {
        releaseStrategy.displayConfig.carColors[1]
    }

    release.type.toString().startsWith("G") -> {
        releaseStrategy.displayConfig.carColors[2]
    }

    else -> ""
}
