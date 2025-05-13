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

import io.docops.docopsextensionssupport.adr.model.escapeXml
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
        var startY = -125
        var height = 350
        if (releaseStrategy.releases.size > 1) {
            height += (220 * (releaseStrategy.releases.size - 1))
        }
        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(strat(release, startY, index, animate, id, releaseStrategy))
            startY += 225
        }
        var titleFill = "#000000"
        var backgroundFill = "#f8f9fa"
        if(releaseStrategy.useDark) {
            titleFill = "#fcfcfc"
            backgroundFill = "url(#dmode1)"
        }
        return """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                 width="${releaseStrategy.scale * 1200}" height="${height * releaseStrategy.scale}"
                 viewBox="0 0 ${releaseStrategy.scale * 1200} ${height * releaseStrategy.scale}">
                 <desc>https://docops.io/extension</desc>
                 ${svgDefs(isPdf,releaseStrategy)}
                 <g transform="scale(${releaseStrategy.scale})">
                 <rect width="1200" height="$height" fill="$backgroundFill"/>
                <text x="600" text-anchor="middle" y="44" font-size="32px" font-family="Arial, Helvetica, sans-serif" fill="$titleFill">${releaseStrategy.title.escapeXml()}</text>
                $str
                </g>
            </svg>
        """.trimIndent()
    }

    private fun strat(release: Release, startY: Int, index: Int, animate: String, id: String, releaseStrategy: ReleaseStrategy): String {
        var yAni = 236
        var ani = ""
        if("ON".equals(animate, true)) {
            yAni = 340
            ani =  """<animateMotion dur="${release.type.speed(release.type)}" repeatCount="indefinite"
                        path="M 110 60 L 1200 60"/>"""
        }
        val str = StringBuilder(
            """<g id="detail_${id}_$index" transform="translate(-1000,0)">
                <text x="420" y="208" font-family="Arial, Helvetica, sans-serif" font-size="16px" fill="${releaseStrategy.displayConfig.fontColor}">""".trimIndent()
        )
        release.lines.forEach {
            str.append("<tspan x=\"420\" dy=\"18\">* $it</tspan>")
        }
        str.append("</text></g>")
        val itemArray = itemTextWidth(release.goal, 900F, 24)
        val lines = linesToUrlIfExist(itemArray, mutableMapOf())
        val tspans = linesToSpanText(lines,24,400)
        val startTextY = 300 - (lines.size * 12)
        var completed = ""
        if(release.completed) {
            completed = "<use xlink:href=\"#completedCheck\" x=\"300\" y=\"315\" fill=\"#fcfcfc\" width=\"24\" height=\"24\"/>"
        }
        
        // Determine colors based on dark mode
        val rectStroke = if(releaseStrategy.useDark) "#444444" else "#cccccc"
        val circleFill = if(releaseStrategy.useDark) "#2c3033" else "#ffffff"
        val textColor = if(releaseStrategy.useDark) "#e6e6e6" else "#073763"
        
        //language=svg
        return """<g transform="translate(-200,$startY)" cursor="pointer" onclick="toggleItem('detail_${id}_$index', 'goal_${id}_$index')">
            <rect x="0" y="200" height="235" width="1400" fill="url(#${linearColor(release)})" stroke='$rectStroke' class='row'/>
            <g>
            <circle cx="325" cy="310" r="84.5" fill-opacity="0.15" filter="url(#filter1)"/>
            <circle class="${release.type.clazz(release.type)}" cx="323" cy="307" r="73" fill="${releaseStroke(release, releaseStrategy)}" filter="url(#Bevel)"/>
            <circle cx="323" cy="307" r="66" fill="$circleFill"/>
             <text class="milestoneDate" fill="$textColor"><textPath text-anchor="middle" startOffset="25%" xlink:href="#curve">${release.date}</textPath></text>
            <text x="325" y="315" dominant-baseline="middle" stroke-width="1px" text-anchor="middle" class="milestone"
            fill="$textColor">${release.type}
            </text>
            </g>
            
            $str
            <g id="goal_${id}_$index" transform="translate(450,$startTextY)" text-anchor="middle">
                <text x="400" y="0" font-family="Arial, Helvetica, sans-serif" font-size="25px" fill="${releaseStrategy.displayConfig.fontColor}">
                    $tspans
                </text>
            </g>
            $completed
            <path d="M 420 430 L 1400 430" stroke="none" stroke-width="1"/>
            </g>
        """.trimMargin()
    }

    private fun svgDefs(isPdf: Boolean, releaseStrategy: ReleaseStrategy): String {
        val ani = """ fill: transparent; stroke-width: 10px; stroke-dasharray: 471; stroke-dashoffset: 471; animation: clock-animation 2s linear infinite;""".trimIndent()
        var style = """
            <style>
                    .milestone:hover { cursor: pointer; /* calculate using: (2 * PI * R) */ stroke-width: 16; stroke-opacity: 1; fill: lightblue; }
                    .milestone { font-size: 60px; font-weight: bold; font-family: Arial, Helvetica, sans-serif; }
                    .milestoneDate { font-size: 18px; font-weight: bold; font-family: Arial, Helvetica, sans-serif; }
                    .bev:hover { $ani stroke: ${releaseStrategy.displayConfig.circleColors[0]}; } .bev2:hover { $ani stroke: ${releaseStrategy.displayConfig.circleColors[1]}; } .bev3:hover { $ani stroke: ${releaseStrategy.displayConfig.circleColors[2]}; }
                    .row { filter: drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4)); }
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
                    <linearGradient id="circlecheck" x2="1" y2="1">
                        <stop class="stop1" offset="0%" stop-color="#a9d99a"/>
                        <stop class="stop2" offset="50%" stop-color="#7ec667"/>
                        <stop class="stop3" offset="100%" stop-color="#54B435"/>
                    </linearGradient>
                    <linearGradient id="dmode1" x2="0%" y2="100%">
                        <stop class="stop1" offset="0%" stop-color="#222627"/>
                        <stop class="stop2" offset="50%" stop-color="#1d2021"/>
                        <stop class="stop3" offset="100%" stop-color="#17191a"/>
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
            text.append("""<tspan x="$x" dy="$dy" text-anchor="middle" font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" font-size="24" font-weight="normal">$it</tspan>""")
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