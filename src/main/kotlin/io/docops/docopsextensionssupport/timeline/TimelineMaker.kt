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

package io.docops.docopsextensionssupport.timeline


import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.hexToHsl
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import java.io.File
import java.util.*

/**
 * The `TimelineMaker` class is used to create a SVG timeline based on the provided parameters.
 * @constructor Creates a new instance of the `TimelineMaker` class.
 * @param useDark A boolean value indicating whether to use the dark theme.
 */
class TimelineMaker(val useDark: Boolean, val outlineColor: String, var pdf: Boolean = false, val id: String = UUID.randomUUID().toString()) {
    private var textColor: String = "#000000"
    private var fillColor = ""
    init {
        if(useDark) {
            textColor = "#fcfcfc"
            fillColor ="#21252B"
        } else {
            fillColor = "#fcfcfc"
        }
    }
    companion object {
         val DEFAULT_COLORS = mutableListOf(
             "#45618E",
             "#A43B3B",
             "#FFD373",
             "#F7E67A",
             "#01FF90",
             "#FF6F36",
             "#EAA213",
             "#FFAF10",
             "#FF7F00",
             "#6D4F98")
        val DEFAULT_HEIGHT: Float = 660.0F
    }

    /**
     * Generates an SVG timeline for the given source.
     *
     * @param source The timeline source.
     * @param title The title of the timeline.
     * @param scale The scale of the timeline.
     * @param isPdf Indicates whether the timeline is for PDF output.
     * @param chars The character set to be used.
     * @return The SVG representation of the timeline.
     */
    fun makeTimelineSvg(source: String, title: String, scale: String, isPdf: Boolean, chars: String) : String {

        this.pdf = isPdf
        val entries = TimelineParser().parse(source)
        val sb = StringBuilder()
        val head = head(entries, scale)
        sb.append(head.first)
        val defs = defs(isPdf)
        val colors = defs.second
        sb.append(defs.first)


        sb.append("<rect fill='$fillColor' width='100%' height='100%'/>")

        sb.append("<g transform=\"scale($scale)\">")

        sb.append("""<text x="${head.second/2}" y="30" text-anchor="middle" style="font-size: 24px;font-family: Arial, Helvetica, sans-serif; font-variant:small-caps" class="edge" fill="$textColor" >${title.escapeXml()}</text>""")
        sb.append("""<g transform="translate(0,24) scale(1.0)">""")

        sb.append(buildRoad(head.second-100))
        val gradIndex = (0 until entries.size).random()

        entries.forEachIndexed { index, entry ->
            val color  = outlineColor.ifBlank {
                DEFAULT_COLORS[gradIndex]
            }

            sb.append(makeEntry(index, entry, color= color, chars = chars, gradIndex =gradIndex, id= id))

        }
        sb.append("</g>")
        sb.append("</g>")
        sb.append(tail())
        return sb.toString()
    }

    private fun makeEntry(index: Int, entry: Entry, color: String, chars: String, gradIndex: Int, id: String): String {
        return if(index % 2 == 0) {
            odd(index,entry, color, chars, gradIndex, id)
        } else{
            even(index, entry, color, chars, gradIndex, id)
        }
    }
    private fun odd(index: Int, entry: Entry, color: String, chars: String, gradIndex: Int, id: String): String {

        var x = 0
        if(index>0)
        {
            x +=  125 * index
        }
        val text = entry.toTextWithSpan(chars.toFloat(), 20, 70, "odd", 14, "#21252B")
        var fill = "url(#topBar_$id)"

        //language=svg
        return """
      <g transform="translate($x,0)" class="odd">
        <g transform="translate(125,320)">
            <circle cx="0" cy="0" r="20" fill="#fcfcfc" />
            <circle cx="0" cy="0" r="17" fill="url(#outlineGradient_$id)" />
             <g transform="translate(-135,-182)">
                <path d="M135,100 v62"  stroke-width="4" stroke-linecap="round" stroke-linejoin="round" stroke="$outlineColor"/>
            </g>
            <g transform="translate(-3,-86),rotate(-90)">
                <polygon points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="7" stroke="url(#outlineGradient_$id)"/>
            </g>
        </g>
        <rect x="10" y="20" width="225" height="200" fill='#fcfcfc' stroke="$color" stroke-width="2" />
        <rect x="10" y="20" width="225" height="40" fill="$fill" stroke="$color" stroke-width="2" />
        <text x="125" y="50" fill='#000000' text-anchor='middle'
                  style="font-family: Arial, Helvetica, sans-serif;  text-anchor:middle; font-size: 20px; fill: #fcfcfc; letter-spacing: normal;font-weight: bold;font-variant: small-caps;"
                  class="glass raiseText">
                  ${entry.date}
        </text>
        $text
    </g>
    
        """.trimIndent()
    }
    private fun even(index: Int, entry: Entry, color: String, chars: String, gradIndex: Int, id: String): String {
        var x = 0
        if(index>0)
        {
            x += 125 * index
        }

        val text = entry.toTextWithSpan(chars.toFloat(), 20, 470, "even", dy=14, "#21252B")
        var fill = "url(#topBar_$id)"


        //language=svg
        return """
        <g transform="translate($x,0)" class="even">
        <g transform="translate(125,320)">
            <circle cx="0" cy="0" r="20" fill="#fcfcfc" />
            <circle cx="0" cy="0" r="17" fill="url(#outlineGradient_$id)" />
             <g transform="translate(-134,-80)">
                <path d="M135,100 v62"  stroke-width="4" stroke-linecap="round" stroke-linejoin="round" stroke="$outlineColor"/>
            </g>
            <g transform="translate(3,86),rotate(90)">
                <polygon points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="7" stroke="url(#outlineGradient_$id)"/>
            </g>
        </g>
        
        <rect x="10" y="420" width="225" height="200" fill='#fcfcfc' stroke="$color" stroke-width="2" />
        <rect x="10" y="420" width="225" height="40" fill="$fill" stroke-width="2"/>
        
        <text x="125" y="450" fill='#000000' text-anchor='middle'
                  style="font-family: Arial, Helvetica, sans-serif;  text-anchor:middle; font-size: 20px; fill: #fcfcfc; letter-spacing: normal;font-weight: bold;font-variant: small-caps;"
                  class="glass raiseText">
                  ${entry.date}
        </text>
        $text
    </g>
    
        """.trimIndent()
    }
    private fun dateTotSpan(date: String, x: Int, dy: Int, textColor: String) : String {
        val sp = date.split(" ")
        val sb=StringBuilder()
        sp.forEach {
            sb.append("""<tspan x="$x" dy="$dy" fill="$textColor">${it.escapeXml()}</tspan>""")
        }
        return sb.toString()
    }
    private fun buildRoad(width: Int): String {
        return """
            <g transform="translate(30,320)">
            <path d="M0,0 h$width" stroke="#aaaaaa" stroke-width="28" fill="url(#arrowColor_$id)" class="raise"
                  stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="10" y1="0" x2="$width" y2="0" stroke="#fcfcfc"
                  stroke-width="10" fill="#ffffff" stroke-dasharray="24 24 24" stroke-linecap="round"
                  stroke-linejoin="round"/>
            <g transform="translate($width,-2)">
                <polygon points="0,5 0,0 5,2.5" stroke="url(#arrowColor_$id)" stroke-width="35" fill="url(#arrowColor_$id)"
                />
            </g>
        </g>
        """.trimIndent()
    }
    private fun head(entries: MutableList<Entry>, scale: String) : Pair<String, Int> {
        var width = 0
        entries.forEachIndexed { index, entry ->
            width = 140 * index + 80
        }
        width += 140
        val scaleF = scale.toFloat()
        val height = DEFAULT_HEIGHT * scale.toFloat()
        return Pair("""
        <svg width="${(width * scaleF) / DISPLAY_RATIO_16_9}" height="${height/ DISPLAY_RATIO_16_9}" viewBox="0 0 ${width * scaleF} $height"
        preserveAspectRatio="xMidYMin slice"
        xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
        <desc>https://docops.io/extension</desc>
    """.trimIndent(),width)
    }

    private fun tail() : String = "</svg>"

    private fun defs(isPdf: Boolean): Pair<String, MutableMap<Int, String>> {
        val colors = mutableMapOf<Int, String>()
        val sb = StringBuilder()

        val colorMap = SVGColor(outlineColor)
        val hsl = hexToHsl(outlineColor, pdf)
        sb.append("""<radialGradient id="outlineGradient_$id" cx="50%" cy="50%" r="50%" fx="50%" fy="20%">
            <stop offset="30%" style="stop-color:${colorMap.lighter()}; stop-opacity:1" />
            <stop offset="60%" style="stop-color:$outlineColor; stop-opacity:1" />
        </radialGradient>""")
        //language=html
        var style = """
        <style>
            .edge { filter: drop-shadow(0 2mm 2mm #66557c); }
            .cricleedge { filter: drop-shadow(0 2mm 2mm #a899bd); }
            .odd { font-size:14px; font-family: Arial, Helvetica, sans-serif; fill: #000000;}
            .even { font-size:14px; font-family: Arial, Helvetica, sans-serif; fill: #000000;}
            .rmLink { fill: #0000bb; text-decoration: underline; }
            .main_pane { fill: #f1f5f8; }
            .each_tm { fill: #fcfcfc; }
            .tm_title { fill: rgba(0, 0, 0, 0.96); }
        </style>
        """.trimIndent()
        if(isPdf) {
            style = ""
        }
        return Pair("""
        <defs>
        
        <linearGradient id="panelBack" x2="1" y2="1">
            <stop class="stop1" offset="0%" stop-color="#939393"/>
            <stop class="stop2" offset="50%" stop-color="#5d5d5d"/>
            <stop class="stop3" offset="100%" stop-color="#282828"/>
        </linearGradient>
        <linearGradient id="arrowColor_$id" x2="0%" y2="100%">
            <stop stop-color="${colorMap.lighter()}" stop-opacity="1" offset="0%"/>
            <stop stop-color="$hsl" stop-opacity="1" offset="100%"/>
        </linearGradient>
        <linearGradient id="topBar_$id" x2="0%" y2="100%">
            <stop stop-color="${colorMap.lighter()}" stop-opacity="1" offset="0%"/>
            <stop stop-color="$hsl" stop-opacity="1" offset="100%"/>
        </linearGradient>
        <marker
                id="triangle"
                viewBox="0 0 10 10"
                refX="1"
                refY="5"
                markerUnits="strokeWidth"
                markerWidth="5"
                markerHeight="5"
                orient="auto">
            <path d="M 0 0 L 10 5 L 0 10 z" fill="#aaaaaa" />
         </marker>   
        $sb
        $style
        

    </defs>
    
    """.trimIndent(),colors)
    }

}

fun main() {
    val entry = """
-
date: 1891
text: Mailbox, invented by Phillip Downing       
-
date: July 23rd, 2023
text: DocOps extension Server releases a new feature, Timeline Maker
for [[https://github.com/asciidoctor/asciidoctorj asciidoctorj]]. With a simple text markup block you can
create very powerful timeline images. Enjoy!
-
date: August 15th, 2023
text: DocOps.io revamping website with updated documentation. All 
our work will be updated with latest documentation for Panels,
for extension server are the various plug-ing for asciidoctorj.
-
date: September 1st, 2023
text: DocOps.io will begin work on revamping the asciidoctorj converter.
with the asciidoctorj 3.0.0 release coming we will need to migrate
custom plug-ins to the new version of the extensions as they will bring breaking changes.
- 
date: October 18th, 2023
text: Time to reimagine the future. Is it possible
to write a lexer parser for custom language?
- 
date: November 16th, 2023
text: Another year been on this earth.
Time to celebrate. Good times.
- 
date: December 11th, 2023
text: Annual start of vacation, time to relax
and plugin the controller.
-
date: 01/01/2024
text: First entry where we show text is wrapping or not and it's [[https://roach.gy roach.gy]] aligning properly
    """.trimIndent()
    val maker = TimelineMaker(true, "#a1d975")
    val svg = maker.makeTimelineSvg(entry, "Another day in the neighborhood", "1", false, "30")
    val f = File("gen/one.svg")
    f.writeBytes(svg.toByteArray())
}