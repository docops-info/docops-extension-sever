package io.docops.docopsextensionssupport.roadmap

import io.docops.asciidoc.utils.escapeXml
import java.io.File

class RoadMapMaker {




    fun makeRoadMapImage(source: String, scale: String, title: String, numChars: String): String {
        val roadmaps = RoadMapParser().parse(source)
        return draw(roadmaps, scale, title, numChars)
    }
    private fun draw(roadmaps: RoadMaps, scale: String, title: String, numChars: String): String {
        val sb = StringBuilder()
        sb.append(head(roadmaps, scale.toFloat()))
        sb.append(defs())
        sb.append("<g transform='scale($scale)'>")
        sb.append("<rect width=\"100%\" height=\"100%\" fill=\"#fbfbfb\"/>")
        sb.append(makeNow())
        repeat(roadmaps.maxLength()) {
            index -> sb.append(row(index, roadmaps, numChars))
        }
        sb.append("""
        <text x="105" y="100" class="now">NOW</text>
        <text x="304.5" y="100" class="next" text-anchor="middle">NEXT</text>
        <text x="504.5" y="100" class="later" text-anchor="middle">LATER</text>
         <rect x="0" y="0" stroke-width="0" fill="url(#headerSix)"  height="80" width="100%" opacity="0.4"/>
        <text x="306" y="60" font-family=" Arial, Helvetica, sans-serif" font-size="46" class="glass" fill="#45618E" text-anchor="middle">$title</text>
        """.trimIndent())
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
    private fun head(roadmaps: RoadMaps, scale: Float) : String {
        val width = 612 * scale
        val height = ((roadmaps.maxLength() * 105) + 106) * scale
        //val height = 791 * scale
        return """<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="$width" height="$height" viewBox="0 0 $width $height">"""
    }

    private fun row(index: Int, roadmaps: RoadMaps, numChars: String) : String {
        var sb = StringBuilder("""<g transform="translate(26,${105 * (index+1)})">""")
        val now = """<rect x="0" y="0" fill="#fcfcfc" class="nowBox" height="100" width="184"/>"""
        val next = """<rect x="190" y="0" fill="#fcfcfc" class="nextBox" height="100" width="184"/>"""
        val later = """<rect x="380" y="0" fill="#fcfcfc" class="laterBox" height="100" width="184"/>"""
        if(roadmaps.now.size-1 >= index ){
            sb.append(now)
            var text = """<text x="2" y="2" class="primaryRoad" fill="#421A56">"""
            val lines = linesToUrlIfExist(wrapText(roadmaps.now[index].joinToString(separator = ""), numChars.toFloat()), roadmaps.urlMap)
            val spans = linesToMultiLineText(lines,12, 2)
            text += spans
            text += "</text>"
            sb.append(text)
        }
        if(roadmaps.next.size-1 >= index ){
            sb.append(next)
            var text = """<text x="192" y="2" class="secondaryRoad">"""
            val lines = linesToUrlIfExist(wrapText(roadmaps.next[index].joinToString(separator = ""), numChars.toFloat()), roadmaps.urlMap)
            val spans = linesToMultiLineText(lines,12, 192)
            text += spans
            text += "</text>"
            sb.append(text)
        }
        if(roadmaps.later.size-1 >= index ){
            sb.append(later)
            var text = """<text x="382" y="2" class="tertiaryRoad">"""
            val lines = linesToUrlIfExist(wrapText(roadmaps.later[index].joinToString(separator = ""), numChars.toFloat()), roadmaps.urlMap)
            val spans = linesToMultiLineText(lines,12, 382)
            text += spans
            text += "</text>"
            sb.append(text)
        }
        sb.append("</g>")
        return sb.toString()
    }


    private fun makeNow() : String {
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
        <linearGradient id="headerSix" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#e5c6fc"/>
            <stop class="stop2" offset="50%" stop-color="#d8aafb"/>
            <stop class="stop3" offset="100%" stop-color="#cc8efa"/>
        </linearGradient>
        <linearGradient id="headerSeven" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#ffe5f9"/>
            <stop class="stop2" offset="50%" stop-color="#ffd8f6"/>
            <stop class="stop3" offset="100%" stop-color="#ffccf4"/>
        </linearGradient>
        <filter id="Bevel2" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="0.5" result="blur"/>
            <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut" lighting-color="white">
                <fePointLight x="-5000" y="-10000" z="0000"/>
            </feSpecularLighting>
            <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
            <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
        </filter>
    
        <style>
        .now { fill: #45a98f; font-family: Arial, Helvetica, sans-serif; stroke: #45a98f; text-anchor: middle; font-weight: bold; }
        .nowBox { fill: none; font-family: Arial, Helvetica, sans-serif; stroke: #45a98f; }
        .next { fill: #e0349c; font-family: Arial, Helvetica, sans-serif; stroke: #e0349c; text-anchor: middle; font-weight: bold; }
        .nextBox { fill: none; font-family: Arial, Helvetica, sans-serif; stroke: #e0349c; text-anchor: middle; font-weight: bold; }
        .later { fill: #e56516; font-family: Arial, Helvetica, sans-serif; stroke: #e56516; text-anchor: middle; font-weight: bold; }
        .laterBox { fill: none; font-family: Arial, Helvetica, sans-serif; stroke: #e56516; text-anchor: middle; font-weight: bold; }
        .primaryRoad { font-family: Arial, Helvetica, sans-serif; font-size: 12px; color: #45a98f; }
        .secondaryRoad{ font-family: Arial, Helvetica, sans-serif; font-size: 12px; color: #e0349c; }
        .tertiaryRoad { font-family: Arial, Helvetica, sans-serif; font-size: 12px; color: #e56516; }
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
    </style>
    </defs>
    """.trimIndent()

}

fun wrapText(text: String, width: Float) : MutableList<String> {
    val words = text.trim().escapeXml().split(" ")
    var rowText = ""
    val lines = mutableListOf<String>()
    words.forEachIndexed { index, s ->
        if(rowText.length + s.length > width) {
            lines.add(rowText)
            rowText = s.trim()
            //close tspan, initialize rowtext and set s
        } else {
            rowText += " ${s.trim()}"
        }
    }
    if(rowText.trim().isNotEmpty()) {
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
fun linesToMultiLineText(lines: MutableList<String>, dy: Int, x: Int): String {
    val text = StringBuilder()
    lines.forEach {
        text.append("""<tspan x="$x" dy="$dy">$it</tspan>""")
    }
    return text.toString()
}
fun main() {
    val str = """
        - now
PI Portal is in Model Office. OPLLite & QuickPAF are in Model Office. 
Deployment for Portal to UAT, MO like it is production.
- now
 Liberty Server M2 facing challenges in MO. This is why we have release strategies.
        - now 
eService is in Sys. PQS is in SYS. Version Support for OPLLite & QuickPaf in SYS
- now
- now
more to do here
- now
we are now max size
- now
over the max size
Liberty server setup in production for OplLite & quick PAF
        - next
 eService is targeted for UAT. PQS is targeted for UAT. Version Support for OPLLite & QuickPaf in UAT      
        - next
emergency messages in SYS     
        - next
 eService is targeted for UAT. PQS is targeted for UAT. Version Support for OPLLite & QuickPaf in UAT      
        - next
emergency messages in SYS    
           - next
 eService is targeted for UAT. PQS is targeted for UAT. Version Support for OPLLite & QuickPaf in UAT      
        - next
emergency messages in SYS    
        - next
 eService is targeted for UAT. PQS is targeted for UAT. Version Support for OPLLite & QuickPaf in UAT      
        - next
emergency messages in SYS    
        - later
        - next
 eService is targeted for UAT. PQS is targeted for UAT. Version Support for OPLLite & QuickPaf in UAT      
        - next
emergency messages in SYS    
        - next
 eService is targeted for UAT. PQS is targeted for UAT. Version Support for OPLLite & QuickPaf in UAT      
        - next
emergency messages in SYS    
        - next
 eService is targeted for UAT. PQS is targeted for UAT. Version Support for OPLLite & QuickPaf in UAT      
        - next
emergency messages in SYS    
        - next
 eService is targeted for UAT. PQS is targeted for UAT. Version Support for OPLLite & QuickPaf in UAT      
        - next
emergency messages in SYS    
Contact View needed for Arch runway. NXT for arch runway.

    """.trimIndent()
    val rm = RoadMapMaker()
    val output = rm.makeRoadMapImage(str, "1.5", "OKTA Progress", "30")
    val f = File("gen/roadmapout.svg")
    f.writeBytes(output.toByteArray())
}