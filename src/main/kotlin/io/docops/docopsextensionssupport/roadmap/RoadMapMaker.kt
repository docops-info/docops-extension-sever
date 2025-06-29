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

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.diagram.allGradientsKeys
import io.docops.docopsextensionssupport.support.generateRectanglePathData
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
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

    open fun paperColor() = "#f8fafb"
}

/**
 * This class represents a regular theme for a road map.
 *
 * @constructor Creates an instance of RegularTheme.
 */
class RegularTheme : RoadMapTheme() {
    override fun titleColor() = "#000000"
}

/**
 * This class represents a dark theme for a road map.
 *
 * @constructor Creates a new instance of DarkTheme.
 */
class DarkTheme : RoadMapTheme() {
    override fun displayText() = "#fcfcfc"
    override fun titleColor(): String = "#FCE6F4"
    override fun paperColor(): String = "#21252B"
}

private const val BoxMaxWidth = 182

private const val BoxFontSize = 12

/**
 * The RoadMapMaker class is responsible for generating a road map image based on a given input source.
 * @property useDark  A boolean flag indicating whether to use dark theme or not. Default is false.
 * @constructor Creates a new instance of the RoadMapMaker class.
 */
class RoadMapMaker(val useDark: Boolean = false, val index: Int = 26) {

    private var isPdf = false
    private var darkFilter = """filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, 1.0));"""
    private var lightDropShadow =
        """<feDropShadow dx="3" dy="3" stdDeviation="1" flood-color="#000000" flood-opacity="1" />"""
    private var darkDropShadow =
        """<feDropShadow dx="3" dy="3" stdDeviation="1" flood-color="#000000" flood-opacity="1" />"""

    /**
     * Generates a road map image based on the provided source, scale, title, and number of characters.
     *
     * @param source the source of the road map data
     * @param scale the scale to be applied to the road map image
     * @param title the title of the road map image
     * @param numChars the number of characters to be displayed on the road map image
     * @return the generated road map image as a string
     */
    fun makeRoadMapImage(
        source: String,
        scale: String,
        title: String,
        numChars: String,
        isPdf: Boolean = false
    ): String {
        this.isPdf = isPdf
        val roadmaps = RoadMapParser().parse(source)
        if (isPdf) {
            darkFilter = ""
            darkDropShadow = ""
            lightDropShadow = ""
        }
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
        val strokeColor = allGradientsKeys().elementAt(index)
        sb.append("<rect width=\"100%\" height=\"100%\" fill=\"${roadMapTheme.paperColor()}\" opacity=\"1.0\"  stroke=\"$strokeColor\" stroke-width=\"0\"/>")
        sb.append("<g transform='scale($scale)'>")

        sb.append(makeNow())
        repeat(roadmaps.maxLength()) { index ->
            sb.append(row(index, roadmaps, numChars))
        }
        sb.append(
            """
        <text x="105" y="100"  style="fill: #0D9276; font-family: Arial, Helvetica, sans-serif; stroke: #0D9276; text-anchor: middle; font-weight: bold;">NOW</text>
        <text x="324.5" y="100"  text-anchor="middle" style="fill: #D63484; font-family: Arial, Helvetica, sans-serif; stroke: #D63484; text-anchor: middle; font-weight: bold;">NEXT</text>
        <text x="534.5" y="100"  text-anchor="middle" style="fill: #FA1E0E; font-family: Arial, Helvetica, sans-serif; stroke: #FA1E0E; text-anchor: middle; font-weight: bold;">LATER</text>
        <rect x="0" y="0" stroke-width="0" fill="${roadMapTheme.paperColor()}"  height="80" width="662" opacity="1.0"/>
        <text x="331" y="60" style="font-family:Arial, Helvetica, sans-serif; font-size:24; fill:${roadMapTheme.titleColor()}; text-anchor:middle; font-variant:small-caps;">${title.escapeXml()}</text> 
        """.trimIndent()
        )
        if (roadmaps.done.isNotEmpty()) {
            sb.append(
                """
                <text x="331" y="${head.second + 20}" font-family=" Arial, Helvetica, sans-serif" font-size="20" fill="${roadMapTheme.titleColor()}" text-anchor="middle" style="fill: #4076ff; font-family: Arial, Helvetica, sans-serif; stroke: #4076ff;  font-weight: bold;">COMPLETED</text> 
            """.trimIndent()
            )
            sb.append(doDone(done = roadmaps.done, numChars = numChars, roadmaps = roadmaps, startingY = head.second))
        }
        sb.append("</g>")
        sb.append(tail())
        return sb.toString()
    }

    /*private fun joinXmlLines(str: String): String {
        val sb = StringBuilder()
        var previousLine = ""

        str.lines().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) {
                return@forEach
            }

            // Add a space if the previous line ends with a quote and the current line starts with an attribute
            if (previousLine.endsWith("\"") && 
                (trimmedLine.startsWith("style=") || 
                 trimmedLine.matches(Regex("^[a-zA-Z-]+=.*")))) {
                sb.append(" ")
            }

            // If the previous line doesn't end with a tag closing character, quote, or space,
            // and the current line doesn't start with a tag opening character, quote, or space,
            // then add a space to prevent content from running together
            else if (previousLine.isNotEmpty() && 
                    !previousLine.endsWith(">") && 
                    !previousLine.endsWith("\"") && 
                    !previousLine.endsWith("'") && 
                    !previousLine.endsWith(" ") &&
                    trimmedLine.isNotEmpty() && 
                    !trimmedLine.startsWith("<") && 
                    !trimmedLine.startsWith("\"") && 
                    !trimmedLine.startsWith("'") && 
                    !trimmedLine.startsWith(" ")) {
                sb.append(" ")
            }

            sb.append(trimmedLine)
            previousLine = trimmedLine
        }

        // Fix any remaining attribute issues by ensuring there's a space between quotes and attributes
        return sb.toString().replace("\"style=", "\" style=")
            .replace("\"class=", "\" class=")
            .replace("\"id=", "\" id=")
            .replace("\"width=", "\" width=")
            .replace("\"height=", "\" height=")
            .replace("\"x=", "\" x=")
            .replace("\"y=", "\" y=")
            .replace("\"rx=", "\" rx=")
            .replace("\"ry=", "\" ry=")
            .replace("\"fill=", "\" fill=")
            .replace("\"stroke=", "\" stroke=")
            .replace("\"d=", "\" d=")
            .replace("\"transform=", "\" transform=")
            .replace("\"viewBox=", "\" viewBox=")
            .replace("\"xmlns=", "\" xmlns=")
    }*/

    private fun doDone(
        done: MutableList<MutableList<String>>,
        numChars: String,
        roadmaps: RoadMaps,
        startingY: Int
    ): String {
        val doneChunks = done.chunked(3)
        val sb = StringBuilder()
        var c = startingY + 40
        var count = 0
        doneChunks.forEach { mutableLists ->
            val boxTitle = generateRectanglePathData(184f, 18f,5f,5f,0f,0f)
            sb.append("""<g transform="translate(26,$c)">""")

            mutableLists.forEachIndexed { index, item ->
                var t: String = "&#xa0;"
                val doneTitleVal = roadmaps.doneTitle[count++]
                if(doneTitleVal != null) {
                    t = doneTitleVal.trimStart().trim()
                    if(t.isEmpty()) {
                        t = "&#xa0;"
                    }
                }
                if (index == 0) {
                    sb.append("""<rect x="0" y="0" fill="#fcfcfc" height="120" width="184" rx="5" ry="5" style="fill: #fcfcfc; font-family: Arial, Helvetica, sans-serif; stroke: #3559E0; stroke-width: 2;"/>""")
                    sb.append("""<g transform="translate(0,0)"><path d="$boxTitle" fill="#3559E0"/></g>""")
                    var text = """<text x="2" y="2" fill="#421A56" style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #3559E0;">"""
                    text += """<tspan x="2" dy="12" fill="#fcfcfc">$t</tspan>"""
                    val lines =
                        linesToUrlIfExist(
                            itemTextWidth(item.joinToString(" "), BoxMaxWidth.toFloat(), BoxFontSize, "Helvetica"),
                            roadmaps.urlMap
                        )
                    val spans = linesToMultiLineText(lines, BoxFontSize, 2, null, 18)
                    text += spans
                    text += "</text>"
                    sb.append(text)
                }
                if (index == 1) {
                    sb.append("""<rect x="210" y="0" fill="#fcfcfc" height="120" width="184" rx="5" ry="5" style="fill: #fcfcfc; font-family: Arial, Helvetica, sans-serif; stroke: #3559E0; stroke-width: 2; "/>""")
                    sb.append("""<g transform="translate(210,0)"><path d="$boxTitle" fill="#3559E0"/></g>""")
                    var text = """<text x="212" y="2" fill="#421A56" style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #3559E0;">"""
                    text += """<tspan x="212" dy="12" fill="#fcfcfc">$t</tspan>"""
                    val lines =
                        linesToUrlIfExist(
                            itemTextWidth(item.joinToString(" "), BoxMaxWidth.toFloat(), BoxFontSize, "Helvetica"),
                            roadmaps.urlMap
                        )
                    val spans = linesToMultiLineText(lines, BoxFontSize, 212, null,18)
                    text += spans
                    text += "</text>"
                    sb.append(text)
                }
                if (index == 2) {
                    sb.append("""<rect x="420" y="0" fill="#fcfcfc"  height="120" width="184" rx="5" ry="5" style="fill: #fcfcfc; font-family: Arial, Helvetica, sans-serif; stroke: #3559E0; stroke-width: 2; "/>""")
                    sb.append("""<g transform="translate(420,0)"><path d="$boxTitle" fill="#3559E0"/></g>""")
                    var text = """<text x="422" y="2" fill="#421A56" style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #3559E0;">"""
                    text += """<tspan x="422" dy="12" fill="#fcfcfc">$t</tspan>"""
                    val lines =
                        linesToUrlIfExist(
                            itemTextWidth(item.joinToString(" "), BoxMaxWidth.toFloat(), BoxFontSize, "Helvetica"),
                            roadmaps.urlMap
                        )
                    val spans = linesToMultiLineText(lines, BoxFontSize, 422, null, 18)
                    text += spans
                    text += "</text>"
                    sb.append(text)
                }
            }
            sb.append("</g>")
            c += 126
        }
        return sb.toString()
    }

    private fun head(roadmaps: RoadMaps, scale: Float): Pair<String, Int> {
        val width = 662 * scale
        val originalHeight = (roadmaps.maxLength() * 125) + 106
        var totalHeight = originalHeight
        if (roadmaps.done.isNotEmpty()) {
            val remain = roadmaps.done.size % 3
            var rows = roadmaps.done.size / 3
            if (remain > 0) {
                rows++
            }
            totalHeight += (125 * rows) + 40
        }
        val height = totalHeight * scale + 30
        //val height = 791 * scale
        val str =
            """<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="$width" height="$height" viewBox="0 0 $width $height">"""
        return Pair(str, originalHeight)
    }

    private fun row(index: Int, roadmaps: RoadMaps, numChars: String): String {
        var inc = 0
        if(index>0) {
            inc = 20
        }
        val sb = StringBuilder("""<g transform="translate(26,${105 * (index + 1) + (inc * index)})">""")
        val now = """<rect x="0" y="0" fill="#fcfcfc" height="120" width="184" rx="5" ry="5" style="fill: #fcfcfc; font-family: Arial, Helvetica, sans-serif; stroke: #0D9276;stroke-width: 2;"/>"""
        val boxTitle = generateRectanglePathData(184f, 18f,5f,5f,0f,0f)
        val nowTitle = """<path d="$boxTitle"  fill="#0D9276"/>"""
        val next = """<rect x="210" y="0" fill="#fcfcfc" height="120" width="184" rx="5" ry="5" style="fill: #fcfcfc; font-family: Arial, Helvetica, sans-serif; stroke: #D63484; stroke-width: 2; text-anchor: middle; font-weight: bold; "/>"""
        val nextTitle = """<g transform="translate(210,0)"><path d="$boxTitle" fill="#D63484"/></g>"""
        val later = """<rect x="420" y="0" fill="#fcfcfc" height="120" width="184" rx="5" ry="5" style="fill: #fcfcfc; font-family: Arial, Helvetica, sans-serif; stroke: #FA1E0E; stroke-width: 2; text-anchor: middle; font-weight: bold; "/>"""
        val laterTitle = """<g transform="translate(420,0)"><path d="$boxTitle"  fill="#FA1E0E"/></g>"""
        if (roadmaps.now.size - 1 >= index) {
            sb.append(now)
            sb.append(nowTitle)
            var t: String? = "&#xa0;"
            val nowTitleVal = roadmaps.nowTitle[index]
            if(nowTitleVal != null) {
                t = nowTitleVal.trimStart().trim()
                if(t.isEmpty()) {
                    t = "&#xa0;"
                }
            }
            var text = """<text x="2" y="2" fill="#421A56" style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #0D9276; ">"""
            text += """<tspan x="2" dy="12" fill="#fcfcfc">$t</tspan>"""
            val lines = linesToUrlIfExist(
                itemTextWidth(roadmaps.now[index].joinToString(separator = " "), BoxMaxWidth.toFloat(), BoxFontSize, "Helvetica"),
                roadmaps.urlMap
            )
            val spans = linesToMultiLineText(lines, BoxFontSize, 2, null, 18)
            text += spans
            text += "</text>"
            sb.append(text)
        }
        if (roadmaps.next.size - 1 >= index) {
            sb.append(next)
            sb.append(nextTitle)
            var t: String? = "&#xa0;"
            val nextTitleVal = roadmaps.nextTitle[index]
            if(nextTitleVal != null) {
                t = nextTitleVal.trimStart().trim()
                if(t.isEmpty()) {
                    t = "&#xa0;"
                }
            }
            var text = """<text x="212" y="2" style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #D63484;">"""
            text += """<tspan x="212" dy="12" fill="#fcfcfc">$t</tspan>"""
            val lines = linesToUrlIfExist(
                itemTextWidth(roadmaps.next[index].joinToString(separator = " "), BoxMaxWidth.toFloat(), BoxFontSize, "Helvetica"),
                roadmaps.urlMap
            )
            val spans = linesToMultiLineText(lines, BoxFontSize, 212, null, 18)
            text += spans
            text += "</text>"
            sb.append(text)
            if (index == 0) {
                sb.append(
                    """
            <g transform="translate(200,47)">
                <use xlink:href="#ppoint" stroke-width="5" stroke="#D63484"/>
            </g>        
                <line x1="186" y1="50" x2="200" y2="50" stroke="#D63484" stroke-width="8" />
            """.trimIndent()
                )
            }
        }
        if (roadmaps.later.size - 1 >= index) {
            sb.append(later)
            sb.append(laterTitle)
            var t: String? = "&#xa0;"
            val laterTitleVal = roadmaps.laterTitle[index]
            if(laterTitleVal != null) {
                t = laterTitleVal.trimStart().trim()
                if(t.isEmpty()) {
                    t = "&#xa0;"
                }
            }
            var text = """<text x="422" y="2" style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #FA1E0E;">"""
            text += """<tspan x="422" dy="12" fill="#fcfcfc">$t</tspan>"""
            val lines = linesToUrlIfExist(
                itemTextWidth(roadmaps.later[index].joinToString(separator = " "), BoxMaxWidth.toFloat(), BoxFontSize, "Helvetica"),
                roadmaps.urlMap
            )
            val spans = linesToMultiLineText(lines, BoxFontSize, 422, null, 18)
            text += spans
            text += "</text>"
            sb.append(text)
            if (index == 0) {
                sb.append(
                    """
                 <g transform="translate(410,47)">
                <use xlink:href="#ppoint" stroke-width="5" stroke="#FA1E0E"/>
                </g>        
                <line x1="396" y1="50" x2="410" y2="50" stroke="#FA1E0E" stroke-width="8" />
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
            val regex = "\\[\\[(.*?)]]".toRegex()
            val matches = regex.findAll(input)

            // Process all matches in reverse order to avoid index shifting
            val matchResults = matches.toList().reversed()
            for (match in matchResults) {
                val displayText = match.groupValues[1]
                val output = urlMap["[[${displayText}]]"]
                if (output != null) {
                    val url = """<a xlink:href="$output" target="_blank" style="fill: blue; text-decoration: underline;">${displayText}</a>"""
                    // Replace only this specific occurrence
                    val startIndex = match.range.first
                    val endIndex = match.range.last + 1
                    line = line.substring(0, startIndex) + url + line.substring(endIndex)
                }
            }
        }
        newLines.add(line)
    }
    return newLines
}

fun linesToMultiLineText(lines: MutableList<String>, dy: Int, x: Int, fillColor: String?, initialY: Int = 0): String {
    var fill = ""
    fillColor?.let {
        if (fill.isNotEmpty()) {
            fill = "fill='$fillColor'"
        }
    }
    val text = StringBuilder()
    lines.forEachIndexed { i, item ->
            if (i == 0 && initialY > 0) {
                text.append("""<tspan x="$x" dy="$initialY" $fill>${item}</tspan>""")
            } else {
                text.append("""<tspan x="$x" dy="$dy" $fill>${item}</tspan>""")
            }
    }
    return text.toString()
}

fun main() {
    val str = """
- now Form Property Service
Determine consuming applications for Form Property Service & Customer inquiry Service
- later Docker
Use common docker image to streamline the process.
- now Form Property Service Dep
analyze Form Property Service for their dependencies - Patrick analyze customer inquiry search for cloud dependencies.
- next Governance
take these Form Property Service feature and customer inquiry service to governance
- next
start iBob process, Surekha for both application
- done
dockerize Form property service
build spring boot 3 version of application
analyze black duck results
- done Image
image embed rectangle
- now
image embed slim
- next
color background roadmap
- done Car
remove car from release strategy
- done
pass in theme (light,dark)
- later
refactor displayConfigUrl to displayTheme

    """.trimIndent()
    val rm = RoadMapMaker(false, 26)
    val output = rm.makeRoadMapImage(str, "1.0", "OKTA Progress", "30")
    val f = File("gen/roadmap.svg")
    val d = generateRectanglePathData(552f, 54f,5f,5f,0f,0f)
    println(d)
    f.writeBytes(output.toByteArray())
}
