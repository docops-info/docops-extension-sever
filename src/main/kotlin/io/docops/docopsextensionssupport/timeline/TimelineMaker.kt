package io.docops.docopsextensionssupport.timeline


import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.support.getRandomColorHex
import io.docops.docopsextensionssupport.support.gradientFromColor
import java.io.File

class TimelineMaker {

    companion object {
         val DEFAULT_COLORS = mutableListOf("#45618E",
             "#A43B3B",
             "#FFD373",
             "#F7E67A",
             "#01FF90",
             "#FF6F36",
             "#EAA213",
             "#FFAF10",
             "#FF7F00",
             "#6D4F98")
    }
    fun makeTimelineSvg(source: String, title: String, scale: String, isPdf: Boolean, chars: String) : String {
        val entries = TimelineParser().parse(source)
        val sb = StringBuilder()
        val head = head(entries, scale)
        sb.append(head.first)
        val defs = defs(entries, isPdf)
        val colors = defs.second
        sb.append(defs.first)
        sb.append("<g transform=\"scale($scale)\">")
        sb.append("""<text x="460" y="24" text-anchor="middle" style="font-size: 24px;font-family: Arial, sans-serif;" class="edge">${title.escapeXml()}</text>""")
        sb.append("""<g transform="translate(0,24)">""")

        sb.append(buildRoad(head.second-100))
        entries.forEachIndexed { index, entry ->
            val color = colors[index]!!
            sb.append(makeEntry(index, entry, color, chars))

        }
        sb.append("</g>")
        sb.append("</g>")
        sb.append(tail())
        return sb.toString()
    }

    private fun makeEntry(index: Int, entry: Entry, color: String, chars: String): String {
        return if(index % 2 == 0) {
            odd(index,entry, color, chars)
        } else{
            even(index, entry, color, chars)
        }
    }
    private fun odd(index: Int, entry: Entry, color: String, chars: String): String {
        var x = 80
        if(index>0)
        {
            x = 140 * index + 80
        }
        val text = entry.toTextWithSpan(chars.toFloat(), -68, 42, "odd", 14)
        //language=svg
        return """
      <g transform="translate($x,200)" class="odd">
        <circle cx="0" cy="0" r="20" fill="#fcfcfc" class="cricleedge"/>
        <circle cx="0" cy="0" r="17" fill="url(#grad$index)" class="cricleedge"/>
        <line x1="0" x2="0" y1="-20" y2="-80" stroke="$color" stroke-width="2"/>
        <circle cx="0" cy="-80" r="3" fill="$color" />
        <text x="-36" y="-128" font-size="14px" fill='#000000' text-anchor='middle'>
        ${dateTotSpan(entry.date,0,14)}    
        </text>
        <rect x="-70" y="30" width="170" height="150" class="edge" fill="url(#headerTimeline$index)" stroke="$color" stroke-width="2"  rx="5"/>
        $text
        <rect id="button" x="-71" y="21" width="40" height="20" ry="5" rx="5" filter="url(#buttonBlur)" fill="$color" class="edge"/>

        <rect id="buttongrad" x="-71" y="21" width="40" height="20" ry="5" rx="5" fill="url(#overlayGrad)"/>
        <text id="label" x="-51.5" y="35" text-anchor="middle" font-size="14px" fill="#fcfcfc">${entry.index}</text>

        <rect id="buttontop" x="-69" y="22" width="36" height="5" ry="5" rx="5" fill="url(#topshineGrad)"
              filter="url(#topshineBlur)"/>
        <rect id="buttonbottom" x="-69" y="35" width="36" height="3" fill="#ffffff" ry="5" rx="5"
              fill-opacity="0.3" filter="url(#bottomshine)"/>
    </g>
    
        """.trimIndent()
    }
    private fun even(index: Int, entry: Entry, color: String, chars: String): String {
        var x = 80
        if(index>0)
        {
            x = 140 * index + 80
        }
        val text = entry.toTextWithSpan(chars.toFloat(), -68, -168, "even", dy=14)
        //language=svg
        return """
        <g transform="translate($x,200)" class="even">
        <circle cx="0" cy="0" r="20" fill="#fcfcfc" class="cricleedge"/>
        <circle cx="0" cy="0" r="17" fill="url(#grad$index)" class="cricleedge"/>
        <line x1="0" x2="0" y1="20" y2="80" stroke="$color" stroke-width="2"/>
        <circle cx="0" cy="80" r="3" fill="$color" />
        <text x="-30" y="96" font-size="14px" fill='#000000' text-anchor='middle'>
        ${dateTotSpan(entry.date,0,14)}
        </text>
        <rect x="-70" y="-180" width="170" height="150" class="edge" fill="url(#headerTimeline$index)" stroke="$color" stroke-width="2"  rx='5'/>
        $text
        <rect id="button" x="-71" y="-189" width="40" height="20" ry="5" rx="5" filter="url(#buttonBlur)" fill="$color" class="edge"/>

        <rect id="buttongrad" x="-71" y="-189" width="40" height="20" ry="5" rx="5" fill="url(#overlayGrad)"/>
        <text id="label" x="-51.5" y="-175" text-anchor="middle" font-size="14px" fill="#fcfcfc">${entry.index}</text>

        <rect id="buttontop" x="-69" y="-188" width="36" height="5" ry="5" rx="5" fill="url(#topshineGrad)"
              filter="url(#topshineBlur)"/>
        <rect id="buttonbottom" x="-69" y="-175" width="36" height="3" fill="#ffffff" ry="5" rx="5"
              fill-opacity="0.3" filter="url(#bottomshine)"/>
        
    </g>
    
        """.trimIndent()
    }
    private fun dateTotSpan(date: String, x: Int, dy: Int) : String {
        val sp = date.split(" ")
        val sb=StringBuilder()
        sp.forEach {
            sb.append("""<tspan x="$x" dy="$dy">${it.escapeXml()}</tspan>""")
        }
        return sb.toString()
    }
    private fun buildRoad(width: Int): String {
        return """
    <g transform="translate(30,200)">
        <path d="M0,0 h$width" stroke="#aaaaaa" stroke-width="28"/>
        <line x1="10" y1="0" x2="${width-10}" y2="0" stroke="#fcfcfc"
        stroke-width="10" fill="#ffffff" stroke-dasharray="24 24 24" marker-end="url(#triangle)"/>
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
        val height = 424 * scale.toFloat()
        return Pair("""
        <svg width="${width * scaleF}" height="$height" viewBox="0 0 ${width * scaleF} $height" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
        <desc>https://docops.io/extension</desc>
    """.trimIndent(),width)
    }

    private fun tail() : String = "</svg>"

    private fun defs(entries: MutableList<Entry>, isPdf: Boolean): Pair<String, MutableMap<Int, String>> {
        val colors = mutableMapOf<Int, String>()
        val sb = StringBuilder()
        entries.forEachIndexed { index, entry ->
            val color = if(index>9) {
                getRandomColorHex()
            } else {
                DEFAULT_COLORS[index]
            }
            colors[index] = color
            val colorMap = gradientFromColor(color)
            sb.append("""
         <radialGradient id="grad$index" cx="50%" cy="50%" r="50%" fx="50%" fy="20%">
            <stop offset="30%" style="stop-color:${colorMap["color1"]}; stop-opacity:1" />
            <stop offset="60%" style="stop-color:$color; stop-opacity:1" />
        </radialGradient>
        <linearGradient id="headerTimeline$index" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${colorMap["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${colorMap["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${colorMap["color3"]}"/>
        </linearGradient>
            """.trimIndent())
        }
        var style = """
<style>
    .edge { filter: drop-shadow(0 2mm 2mm #66557c); }
    .cricleedge { filter: drop-shadow(0 2mm 2mm #a899bd); }
    .odd { font-size:14px; font-family: Arial, sans-serif; fill: #000000;}
    .even { font-size:14px; font-family: Arial, sans-serif; fill: #000000;}
    .rmLink { fill: blue; text-decoration: underline; }
</style>
        """.trimIndent()
        if(isPdf) {
            style = ""
        }
        return Pair("""
        <defs>
        <filter id="buttonBlur">
            <feGaussianBlur in="SourceAlpha" stdDeviation="2" result="blur"/>
            <feOffset in="blur" dy="2" result="offsetBlur"/>
            <feMerge>
                <feMergeNode in="offsetBlur"/>
                <feMergeNode in="SourceGraphic"/>
            </feMerge>
        </filter>

        <linearGradient id="overlayGrad" gradientUnits="userSpaceOnUse" x1="95" y1="-20" x2="95" y2="70">
            <stop offset="0" stop-color="#000000" stop-opacity="0.5"/>
            <stop offset="1" stop-color="#000000" stop-opacity="0"/>
        </linearGradient>

        <filter id="topshineBlur">
            <feGaussianBlur stdDeviation="0.93"/>
        </filter>

        <linearGradient id="topshineGrad" gradientUnits="userSpaceOnUse" x1="95" y1="0" x2="95" y2="40">
            <stop offset="0" stop-color="#ffffff" stop-opacity="1"/>
            <stop offset="1" stop-color="#ffffff" stop-opacity="0"/>
        </linearGradient>

        <filter id="bottomshine">
            <feGaussianBlur stdDeviation="0.95"/>
        </filter>
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
    val maker = TimelineMaker()
    val svg = maker.makeTimelineSvg(entry, "Another day in the neighborhood", "1.5", false, "35")
    val f = File("gen/one.svg")
    f.writeBytes(svg.toByteArray())
}