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

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.support.gradientFromColor
import io.docops.docopsextensionssupport.support.hexToHsl
import java.util.UUID

/**
 * The ReleaseTimelineMaker class is responsible for creating a release timeline SVG based on a given release strategy.
 * It provides methods to build the SVG document with the necessary elements and attributes.
 */
open class ReleaseTimelineMaker {

    protected var isPdf = false
    /**
     * Generates a formatted string representation of a release.
     *
     * @param releaseStrategy The release strategy to generate the string for.
     * @param isPdf Indicates whether the output is intended for PDF generation.
     *
     * @return The generated string representation of the release.
     */
    open fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean) : String{
        this.isPdf = isPdf
        val width = determineWidth(releaseStrategy = releaseStrategy)
        val id = UUID.randomUUID().toString()
        val str = StringBuilder(head(width, id, title= releaseStrategy.title, releaseStrategy.scale))
        str.append(defs(isPdf, id,releaseStrategy.scale, releaseStrategy))
        var titleFill = "#000000"
        if(releaseStrategy.useDark) {
            titleFill = "#fcfcfc"
        }
        str.append(title(releaseStrategy.title, width, titleFill))
        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(buildReleaseItem(release,index, isPdf, id))
        }
        str.append("</g>")
        str.append(tail())
        return str.toString()
    }


    private fun buildReleaseItem(release: Release, currentIndex: Int, isPdf: Boolean, id: String): String {
        var startX = 0
        if (currentIndex > 0) {
            startX = currentIndex * 425 -(20*currentIndex)
        }
        val lineText = StringBuilder()
        var lineStart = 25
        release.lines.forEachIndexed { index, s ->
            lineText.append(
                """
                <tspan x="$lineStart" dy="10" class="entry" font-size="10px" font-weight="normal"
                   font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" text-anchor="start">- ${s.escapeXml()}</tspan>
            """.trimIndent()
            )
            if (index <= 7) {
                lineStart += 10
            } else {
                lineStart -= 10
            }
        }
        var x = 200
        var anchor = "text-anchor=\"middle\""
        if (isPdf) {
            x = 15
            anchor = ""
        }
        var completed = ""
        if(release.completed) {
            completed = "<use xlink:href=\"#completedCheck\" x=\"405\" y=\"65\" width=\"24\" height=\"24\"/>"
        }
        //language=svg
        return """
         <g transform="translate($startX,60)" class="${shadeColor(release)}">
             <text text-anchor="middle" x="200" y="-12" class="milestoneTL">${release.date}</text>
             <path d="m 0,0 h 400 v 200 h -400 l 0,0 l 100,-100 z" stroke="${strokeColor(release)}" fill="#fcfcfc"/>
             <path d="m 400,0 v 200 l 100,-100 z" fill="${strokeColor(release)}" stroke="${strokeColor(release)}" />
            <text x="410" y="110" class="milestoneTL" font-size="36px" fill="#fcfcfc">${release.type}</text>
            $completed
            <text $anchor x="$x" y="12" class="milestoneTL lines" font-size="10px" font-family='Arial, "Helvetica Neue", Helvetica, sans-serif' font-weight="bold">${release.goal.escapeXml()}
                $lineText
            </text>
        </g>
        """.trimIndent()
    }
    protected fun shadeColor(release: Release): String = when {
        release.type.toString().startsWith("M") -> {
            "shadM"
        }

        release.type.toString().startsWith("R") -> {
            "shadR"
        }

        release.type.toString().startsWith("G") -> {
            "shadG"
        }

        else -> ""
    }


    protected fun determineWidth(releaseStrategy: ReleaseStrategy) = ((releaseStrategy.releases.size * 410) + (releaseStrategy.releases.size * 20) + 80) * releaseStrategy.scale


    private fun head(width: Float, id: String, title: String, scale: Float) : String{
        val height = 270 * scale
        //language=svg
        return """
            <svg width="$width" height="$height" viewBox='0 0 $width $height' xmlns='http://www.w3.org/2000/svg' xmlns:xlink="http://www.w3.org/1999/xlink" role='img'
            aria-label='Docops: Release Strategy' id="ID$id">
            <desc>https://docops.io/extension</desc>
            <title>${title.escapeXml()}</title>
        """.trimIndent()
    }
    protected fun title(title: String, width: Float, titleFill: String) = """
        <text x="${width/2}" y="18" fill="$titleFill" text-anchor="middle"  font-size="20px" font-family="Arial, Helvetica, sans-serif">${title.escapeXml()}</text>
    """.trimIndent()
    protected fun tail() = "</svg>"

    //language=svg
    protected fun defs(isPdf: Boolean, id: String, scale: Float, releaseStrategy: ReleaseStrategy): String {
        var style = ""
        if (!isPdf) {
            style = """
                <style>
            #ID${id} .shadM { fill: ${releaseStrategy.displayConfig.colors[0]}; filter: drop-shadow(0 1mm 1mm ${releaseStrategy.displayConfig.colors[0]}); }
            #ID${id} .shadR { fill: ${releaseStrategy.displayConfig.colors[1]}; filter: drop-shadow(0 1mm 1mm ${releaseStrategy.displayConfig.colors[1]}); }

            #ID${id} .shadG { fill: ${releaseStrategy.displayConfig.colors[2]}; filter: drop-shadow(0 1mm 1mm ${releaseStrategy.displayConfig.colors[2]}); }
            #ID${id} .milestoneTL { font-family: Arial, "Helvetica Neue", Helvetica, sans-serif; font-weight: bold; }
            #ID${id} .lines { font-size: 10px; }

            #ID${id} .milestoneTL > .entry { text-anchor: start; font-weight: normal; }
            .raise { pointer-events: bounding-box; opacity: 1; filter: drop-shadow(3px 1px 2px rgb(0 0 0 / 0.4)); }

            .raise:hover { stroke: gold; stroke-width: 3px; opacity: 0.9; }
            
        </style>
            <script>
             function strategyShowItem(item) {
                 var elem = document.querySelector("#"+item);
                 var display = elem.getAttribute("visibility");
                 if("hidden" === display) {
                     elem.setAttribute("visibility", "")
                 } else {
                     elem.setAttribute("visibility", "hidden")
                 }
             }
             </script>
            """.trimIndent()
        }
        val colors = StringBuilder()
        val shades = mutableMapOf<Int, String>(0 to "M", 1 to "R", 2 to "G")
        releaseStrategy.displayConfig.colors.forEachIndexed { index, s ->
            colors.append(buildGradientHslDef(s, "shad${shades[index]}_rect"))
        }
        return """
             <defs>
             <filter id="Bevel2" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                 <feGaussianBlur in="SourceAlpha" stdDeviation="0.5" result="blur"/>
                 <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut" lighting-color="white">
                     <fePointLight x="-5000" y="-10000" z="0000"/>
                 </feSpecularLighting>
                 <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                 <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
             </filter>
             <linearGradient id="ID0756d7d2-2648-4a67-89af-c133b3a8d4c9" x2="1" y2="1">
                 <stop class="stop1" offset="0%" stop-color="#a48bdb">
                     <animate attributeName="stop-color"
                              values="#a48bdb;#7651c9;#4918B8;#a48bdb;#7651c9;#4918B8;" dur="20s" repeatCount="indefinite">
                     </animate>
                 </stop>
                 <stop class="stop2" offset="50%" stop-color="#7651c9">
                     <animate attributeName="stop-color"
                              values="#a48bdb;#7651c9;#4918B8;#a48bdb;#7651c9;#4918B8;" dur="20s" repeatCount="indefinite">
                     </animate>
                 </stop>
                 <stop class="stop3" offset="100%" stop-color="#4918B8">
                     <animate attributeName="stop-color"
                              values="#a48bdb;#7651c9;#4918B8;#a48bdb;#7651c9;#4918B8;" dur="20s" repeatCount="indefinite">
                     </animate>
                 </stop>
                 <animateTransform attributeName="gradientTransform" type="rotate" values="360 .5 .5;0 .5 .5"
                                   dur="10s" repeatCount="indefinite" />
             </linearGradient>
             <linearGradient id="circlecheck" x2="1" y2="1">
                <stop class="stop1" offset="0%" stop-color="#a9d99a"/>
                <stop class="stop2" offset="50%" stop-color="#7ec667"/>
                <stop class="stop3" offset="100%" stop-color="#54B435"/>
            </linearGradient>
            <g id="completedCheck">
                <polyline points="10 25 22 40 50 10" fill="none" stroke="url(#circlecheck)" stroke-width="6"/>
            </g>

             $colors
             $style
         </defs>
         <g transform='scale($scale)' id='GID$id'>
         """.trimIndent()
    }

    fun buildGradientHslDef(color: String, id: String): String {
        val m = gradientFromColor(color)
        val hsl = hexToHsl(color, isPdf)
        return """
        <linearGradient x2="0%" y2="100%" id="$id">
            <stop stop-color="${m["color1"]}" stop-opacity="1" offset="0%"/>
            <stop stop-color="$hsl" stop-opacity="1" offset="100%"/>
        </linearGradient>
    """.trimIndent()
    }
}
fun strokeColor(release: Release): String = when {
    release.type.toString().startsWith("M") -> {
        "#6cadde"
    }

    release.type.toString().startsWith("R") -> {
        "#C766A0"
    }

    release.type.toString().startsWith("G") -> {
        "#136e33"
    }

    else -> ""
}
fun fishTailColor(release: Release, releaseStrategy: ReleaseStrategy): String = when {
    release.type.toString().startsWith("M") -> {
        releaseStrategy.displayConfig.colors[0]
    }

    release.type.toString().startsWith("R") -> {
        releaseStrategy.displayConfig.colors[1]
    }

    release.type.toString().startsWith("G") -> {
        releaseStrategy.displayConfig.colors[2]
    }

    else -> ""
}
fun gradientColorFromColor(color: String, id: String): String {

    val gradient = gradientFromColor(color)
    return """
        <linearGradient id="$id" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/>
        </linearGradient>"""
}

