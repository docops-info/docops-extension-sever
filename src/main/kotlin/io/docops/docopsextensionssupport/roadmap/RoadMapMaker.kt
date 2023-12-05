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

package io.docops.docopsextensionssupport.roadmap

import io.docops.asciidoc.utils.escapeXml
import java.io.File

/**
 * RoadMapTheme is an abstract class that represents a theme for a road map.
 * It provides methods to define the colors used for displaying text, title, and paper on the road map.
 *
 * To create a custom theme, you can extend this class and override the relevant methods to provide the desired colors.
 */
abstract class RoadMapTheme {
    open fun displayText() = "#000000"
    open fun titleColor() = "#45618E"

    open fun paperColor() = "#fcfcfc"
}

/**
 * This class represents a regular theme for a road map.
 *
 * @constructor Creates an instance of RegularTheme.
 */
class RegularTheme : RoadMapTheme() {
    override fun titleColor() = "#fcfcfc"
}

/**
 * This class represents a dark theme for a road map.
 *
 * @constructor Creates a new instance of DarkTheme.
 */
class DarkTheme : RoadMapTheme() {
    override fun displayText() = "#fcfcfc"
    override fun titleColor(): String = "#FCE6F4"
    override fun paperColor(): String = "#17242b"
}

/**
 * The RoadMapMaker class is responsible for generating a road map image based on a given input source.
 * @property useDark  A boolean flag indicating whether to use dark theme or not. Default is false.
 * @constructor Creates a new instance of the RoadMapMaker class.
 */
class RoadMapMaker(val useDark: Boolean = false) {

    /**
     * Generates a road map image based on the provided source, scale, title, and number of characters.
     *
     * @param source the source of the road map data
     * @param scale the scale to be applied to the road map image
     * @param title the title of the road map image
     * @param numChars the number of characters to be displayed on the road map image
     * @return the generated road map image as a string
     */
    fun makeRoadMapImage(source: String, scale: String, title: String, numChars: String): String {
        val roadmaps = RoadMapParser().parse(source)
        return draw(roadmaps, scale, title, numChars)
    }

    fun parseToRoadMap(source: String, scale: String, title: String, numChars: String): RoadMaps {
        return RoadMapParser().parse(source)

    }
    private fun draw(roadmaps: RoadMaps, scale: String, title: String, numChars: String): String {
        val sb = StringBuilder()
        val head = head(roadmaps, scale.toFloat())
        sb.append(head.first)
        sb.append(defs())
        var roadMapTheme: RoadMapTheme = RegularTheme()
        var headerColor = "headerLight"
        if (useDark) {
            roadMapTheme = DarkTheme()
            headerColor = "headerDark"
        }
        sb.append("<rect width=\"100%\" height=\"100%\" fill=\"${roadMapTheme.paperColor()}\" opacity=\"1.0\"/>")
        sb.append("<g transform='scale($scale)' class=\"shadowed\">")
        sb.append(makeNow())
        repeat(roadmaps.maxLength()) { index ->
            sb.append(row(index, roadmaps, numChars))
        }
        sb.append(
            """
        <text x="105" y="100" class="now">NOW</text>
        <text x="324.5" y="100" class="next" text-anchor="middle">NEXT</text>
        <text x="534.5" y="100" class="later" text-anchor="middle">LATER</text>
        <rect x="0" y="0" stroke-width="0" fill="url(#$headerColor)"  height="80" width="662" opacity="1.0"/>
        <text x="306" y="60" font-family=" Arial, Helvetica, sans-serif" font-size="46" class="glass" fill="${roadMapTheme.titleColor()}" text-anchor="middle">${title.escapeXml()}</text> 
        """.trimIndent()
        )
        if (roadmaps.done.isNotEmpty()) {
            sb.append(
                """
                <rect x="26" y="${head.second}" height="25" width="600" fill="url(#$headerColor)" opacity="1.0" stroke-width="0"/>
                <text x="306" y="${head.second+20}" font-family=" Arial, Helvetica, sans-serif" font-size="20" class="doneTitle" fill="${roadMapTheme.titleColor()}" text-anchor="middle">COMPLETED</text> 
            """.trimIndent()
            )
            sb.append(doDone(done = roadmaps.done, numChars = numChars, roadmaps = roadmaps, startingY = head.second))
        }
        sb.append("</g>")
        sb.append(tail())
        return joinXmlLines(sb.toString())
    }

    private fun joinXmlLines(str: String): String {
        val sb = StringBuilder()
        str.lines().forEach {
            sb.append(it.trim())
        }
        return sb.toString()
    }

    private fun doDone(
        done: MutableList<MutableList<String>>,
        numChars: String,
        roadmaps: RoadMaps,
        startingY: Int
    ): String {
        val doneChunks = done.chunked(3)
        val sb = StringBuilder()
        var c = startingY + 40
        doneChunks.forEach { mutableLists ->
            sb.append("""<g transform="translate(26,$c)">""")
            mutableLists.forEachIndexed { index, item ->
                if (index == 0) {
                    sb.append("""<rect x="0" y="0" fill="#fcfcfc" class="doneBox" height="100" width="184"/>""")
                    var text = """<text x="2" y="2" class="doneRoad" fill="#421A56">"""
                    val lines =
                        linesToUrlIfExist(
                            wrapText(item.joinToString(separator = " "), numChars.toFloat()),
                            roadmaps.urlMap
                        )
                    val spans = linesToMultiLineText(lines, 12, 2, null)
                    text += spans
                    text += "</text>"
                    sb.append(text)
                }
                if (index == 1) {
                    sb.append("""<rect x="210" y="0" fill="#fcfcfc" class="doneBox" height="100" width="184"/>""")
                    var text = """<text x="212" y="2" class="doneRoad" fill="#421A56">"""
                    val lines =
                        linesToUrlIfExist(
                            wrapText(item.joinToString(separator = " "), numChars.toFloat()),
                            roadmaps.urlMap
                        )
                    val spans = linesToMultiLineText(lines, 12, 212, null)
                    text += spans
                    text += "</text>"
                    sb.append(text)
                }
                if (index == 2) {
                    sb.append("""<rect x="420" y="0" fill="#fcfcfc" class="doneBox" height="100" width="184"/>""")
                    var text = """<text x="422" y="2" class="doneRoad" fill="#421A56">"""
                    val lines =
                        linesToUrlIfExist(
                            wrapText(item.joinToString(separator = " "), numChars.toFloat()),
                            roadmaps.urlMap
                        )
                    val spans = linesToMultiLineText(lines, 12, 422, null)
                    text += spans
                    text += "</text>"
                    sb.append(text)
                }
            }
            sb.append("</g>")
            c += 106
        }
        return sb.toString()
    }

    private fun head(roadmaps: RoadMaps, scale: Float): Pair<String, Int> {
        val width = 662 * scale
        val originalHeight = (roadmaps.maxLength() * 105) + 106
        var totalHeight = originalHeight
        if (roadmaps.done.isNotEmpty()) {
            val remain = roadmaps.done.size % 3
            var rows = roadmaps.done.size / 3
            if (remain > 0) {
                rows++
            }
            totalHeight += (105 * rows) + 40
        }
        val height = totalHeight * scale
        //val height = 791 * scale
        val str =
            """<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="$width" height="$height" viewBox="0 0 $width $height">"""
        return Pair(str, originalHeight)
    }

    private fun row(index: Int, roadmaps: RoadMaps, numChars: String): String {
        val sb = StringBuilder("""<g transform="translate(26,${105 * (index + 1)})">""")
        val now = """<rect x="0" y="0" fill="#fcfcfc" class="nowBox" height="100" width="184"/>"""
        val next = """<rect x="210" y="0" fill="#fcfcfc" class="nextBox" height="100" width="184"/>"""
        val later = """<rect x="420" y="0" fill="#fcfcfc" class="laterBox" height="100" width="184"/>"""
        if (roadmaps.now.size - 1 >= index) {
            sb.append(now)
            var text = """<text x="2" y="2" class="primaryRoad" fill="#421A56">"""
            val lines = linesToUrlIfExist(
                wrapText(roadmaps.now[index].joinToString(separator = " "), numChars.toFloat()),
                roadmaps.urlMap
            )
            val spans = linesToMultiLineText(lines, 12, 2, null)
            text += spans
            text += "</text>"
            sb.append(text)
        }
        if (roadmaps.next.size - 1 >= index) {
            sb.append(next)
            var text = """<text x="212" y="2" class="secondaryRoad">"""
            val lines = linesToUrlIfExist(
                wrapText(roadmaps.next[index].joinToString(separator = " "), numChars.toFloat()),
                roadmaps.urlMap
            )
            val spans = linesToMultiLineText(lines, 12, 212, null)
            text += spans
            text += "</text>"
            sb.append(text)
            if (index == 0) {
                sb.append(
                    """
            <g transform="translate(200,47)">
                <use xlink:href="#ppoint" stroke-width="5" stroke="#e0349c"/>
            </g>        
                <line x1="186" y1="50" x2="200" y2="50" stroke="#e0349c" stroke-width="8" />
            """.trimIndent()
                )
            }
        }
        if (roadmaps.later.size - 1 >= index) {
            sb.append(later)
            var text = """<text x="422" y="2" class="tertiaryRoad">"""
            val lines = linesToUrlIfExist(
                wrapText(roadmaps.later[index].joinToString(separator = " "), numChars.toFloat()),
                roadmaps.urlMap
            )
            val spans = linesToMultiLineText(lines, 12, 422, null)
            text += spans
            text += "</text>"
            sb.append(text)
            if (index == 0) {
                sb.append(
                    """
                 <g transform="translate(410,47)">
                <use xlink:href="#ppoint" stroke-width="5" stroke="#e56516"/>
                </g>        
                <line x1="396" y1="50" x2="410" y2="50" stroke="#e56516" stroke-width="8" />
            """.trimIndent()
                )
            }
        }
        sb.append("</g>")
        return sb.toString()
    }


    private fun makeNow(): String {
        return ""
    }

    private fun tail() = "</svg>"

    //language=html
    private fun defs() = """
        <defs>
        <linearGradient id="headerTitleBar" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#f8d8c4"/>
            <stop class="stop2" offset="50%" stop-color="#f5c5a7"/>
            <stop class="stop3" offset="100%" stop-color="#f2b28a"/>
        </linearGradient>
        <linearGradient id="headerEight" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#ffb79a"/>
            <stop class="stop2" offset="50%" stop-color="#ff9368"/>
            <stop class="stop3" offset="100%" stop-color="#FF6F36"/>
        </linearGradient>
        <linearGradient x1="62.342285" y1="8.8261719" x2="62.342281" y2="61.276535" id="linearGradient4619" gradientUnits="userSpaceOnUse">
            <stop id="stop4621" style="stop-color:#ffffff;stop-opacity:1" offset="0"/>
            <stop id="stop4623" style="stop-color:#45a98f;stop-opacity:1" offset="1"/>
        </linearGradient>
        <linearGradient id="blackPurple" x1="62.342285" y1="8.8261719" x2="62.342281" y2="61.276535" gradientUnits="userSpaceOnUse">
        <stop class="stop1" stop-color="#ffffff" offset="0"/><stop class="stop3" offset="1" stop-color="#6b587d"/></linearGradient>
        <linearGradient id="headerDark" x1="62.342285" y1="8.8261719" x2="62.342281" y2="61.276535"  gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#8b9195"/>
            <stop class="stop2" offset="50%" stop-color="#515a60"/>
            <stop class="stop3" offset="100%" stop-color="#17242b"/>
        </linearGradient>
        <linearGradient id="headerLight" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#ddd1bf"/>
            <stop class="stop2" offset="50%" stop-color="#ccba9f"/>
            <stop class="stop3" offset="100%" stop-color="#BCA37F"/>
        </linearGradient>
        
    
        <style>
        .now { fill: #45a98f; font-family: Arial, Helvetica, sans-serif; stroke: #45a98f; text-anchor: middle; font-weight: bold; }
        .nowBox { fill: none; font-family: Arial, Helvetica, sans-serif; stroke: #45a98f; }
        .next { fill: #e0349c; font-family: Arial, Helvetica, sans-serif; stroke: #e0349c; text-anchor: middle; font-weight: bold; }
        .nextBox { fill: none; font-family: Arial, Helvetica, sans-serif; stroke: #e0349c; text-anchor: middle; font-weight: bold; }
        .later { fill: #e56516; font-family: Arial, Helvetica, sans-serif; stroke: #e56516; text-anchor: middle; font-weight: bold; }
        .laterBox { fill: none; font-family: Arial, Helvetica, sans-serif; stroke: #e56516; text-anchor: middle; font-weight: bold; }
        .doneBox { fill: none; font-family: Arial, Helvetica, sans-serif; stroke: #4076ff; }
        .primaryRoad { font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #45a98f; }
        .secondaryRoad{ font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #e0349c; }
        .tertiaryRoad { font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #e56516; }
        .doneRoad { font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #4076ff; }
        .doneTitle { fill: #4076ff; font-family: Arial, Helvetica, sans-serif; stroke: #4076ff;  font-weight: bold; }
        .rmLink { fill: blue; text-decoration: underline; }
        .glass:after, .glass:before {
        content: "";
        display: block;
        position: absolute
    }

    .glass {
        overflow: hidden;
        color: #fff;
        text-shadow: 0 1px 2px rgba(0, 0, 0, .7);
        background-image: radial-gradient(circle at center, rgba(0, 167, 225, .25), rgba(0, 110, 149, .5));
        box-shadow: 0 5px 10px rgba(0, 0, 0, .75), inset 0 0 0 2px rgba(0, 0, 0, .3), inset 0 -6px 6px -3px rgba(0, 129, 174, .2);
        position: relative
    }

    .glass:after {
        background: rgba(0, 167, 225, .2);
        z-index: 0;
        height: 100%;
        width: 100%;
        top: 0;
        left: 0;
        backdrop-filter: blur(3px) saturate(400%);
        -webkit-backdrop-filter: blur(3px) saturate(400%)
    }

    .glass:before {
        width: calc(100% - 4px);
        height: 35px;
        background-image: linear-gradient(rgba(255, 255, 255, .7), rgba(255, 255, 255, 0));
        top: 2px;
        left: 2px;
        border-radius: 30px 30px 200px 200px;
        opacity: .7
    }

    .glass:hover {
        text-shadow: 0 1px 2px rgba(0, 0, 0, .9)
    }

    .glass:hover:before {
        opacity: 1
    }

    .glass:active {
        text-shadow: 0 0 2px rgba(0, 0, 0, .9);
        box-shadow: 0 3px 8px rgba(0, 0, 0, .75), inset 0 0 0 2px rgba(0, 0, 0, .3), inset 0 -6px 6px -3px rgba(0, 129, 174, .2)
    }

    .glass:active:before {
        height: 25px
    }
     .shadowed {
            -webkit-filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
            filter: drop-shadow(6px 6px 5px rgba(0, 0, 0, .3));
        }
    </style>
    <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="5"/>
    </defs>
    """.trimIndent()

}


fun wrapText(text: String, width: Float): MutableList<String> {
    val words = text.trim().escapeXml().split(" ")
    var rowText = ""
    val lines = mutableListOf<String>()
    words.forEachIndexed { index, s ->
        if (rowText.length + s.length > width) {
            lines.add(rowText)
            rowText = s.trim()
        } else {
            rowText += " ${s.trim()}"
        }
    }
    if (rowText.trim().isNotEmpty()) {
        lines.add(rowText)
    }
    return lines

}

fun linesToUrlIfExist(lines: MutableList<String>, urlMap: MutableMap<String, String>): MutableList<String> {
    val newLines = mutableListOf<String>()
    lines.forEach { input ->
        var line = input
        if (input.contains("[[") && input.contains("]]")) {
            val regex = "(?<=\\[\\[)(.*?)(?=]])".toRegex()
            val matches = regex.findAll(input)
            matches.forEach {
                val output = urlMap["[[${it.value}]]"]
                val url = """<a xlink:href="$output" target="_blank" class="rmLink">${it.value}</a>"""
                line = input.replace("[[${it.value}]]", url)
            }
        }
        newLines.add(line)
    }
    return newLines
}

fun linesToMultiLineText(lines: MutableList<String>, dy: Int, x: Int, fillColor: String?): String {
    var fill = ""
    fillColor?.let {
        fill = "fill='$fillColor'"
    }
    val text = StringBuilder()
    lines.forEach {
        text.append("""<tspan x="$x" dy="$dy" $fill>$it</tspan>""")
    }
    return text.toString()
}

fun main() {
    val str = """
 - now
Determine consuming applications for Form Property Service & Customer inquiry Service
- later
Use common docker image to streamline the process.
- now
analyze Form Property Service for their dependencies - Patrick analyze customer inquiry search for cloud dependencies.
- next
take these Form Property Service feature and customer inquiry service to governance
- next
start iBob process, Surekha for both application
- done
dockerize Form property service
build spring boot 3 version of application
analyze black duck results
- done
image embed rectangle
- now
image embed slim
- next
color background roadmap
- done
remove car from release strategy
- done
pass in theme (light,dark)
- later
refactor displayConfigUrl to displayTheme

    """.trimIndent()
    val rm = RoadMapMaker(false)
    val output = rm.makeRoadMapImage(str, "1.5", "OKTA Progress", "30")
    val f = File("gen/roadmap.svg")
    f.writeBytes(output.toByteArray())
}