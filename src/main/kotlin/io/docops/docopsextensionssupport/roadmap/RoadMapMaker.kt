package io.docops.docopsextensionssupport.roadmap

import io.docops.asciidoc.utils.escapeXml
import java.io.File

class RoadMapMaker {


        private  val CORD = mutableMapOf(0 to 120, 1 to 225, 2 to 330, 3 to 435, 4 to 540, 5 to 645)


    fun makeRoadMapImage(source: String, scale: String, title: String): String {
        val roadmaps = RoadMapParser().parse(source)
        return draw(roadmaps, scale, title)
    }
    private fun draw(roadmaps: RoadMaps, scale: String, title: String): String {
        val sb = StringBuilder()
        sb.append(head(roadmaps, scale.toFloat()))
        sb.append(style())
        sb.append("<g transform='scale($scale)'>")
        sb.append(title(title))
        sb.append(makeNow())
        sb.append(row(0, roadmaps))
        sb.append(row(1, roadmaps))
        sb.append(row(2, roadmaps))
        sb.append(row(3, roadmaps))
        sb.append(row(4, roadmaps))
        sb.append(row(5, roadmaps))
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
        val width = 612* scale
        val height = 791 * scale
        return """<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="$width" height="$height" viewBox="0 0 $width $height">"""
    }

    private fun row(index: Int, roadmaps: RoadMaps) : String {
        var sb = StringBuilder("""<g transform="translate(26,${CORD[index]})">""")
        val now = """<rect x="0" y="0" fill="#fcfcfc" class="nowBox" height="100" width="184"/>"""
        val next = """<rect x="190" y="0" fill="#fcfcfc" class="nextBox" height="100" width="184"/>"""
        val later = """<rect x="380" y="0" fill="#fcfcfc" class="laterBox" height="100" width="184"/>"""
        if(roadmaps.now.size-1 >= index ){
            sb.append(now)
            var text = """<text x="2" y="2" class="primary">"""
            val lines = linesToUrlIfExist(wrapText(roadmaps.now[index].joinToString(separator = ""), 32f), roadmaps.urlMap)
            val spans = linesToMultiLineText(lines,12, 2)
            text += spans
            text += "</text>"
            sb.append(text)
        }
        if(roadmaps.next.size-1 >= index ){
            sb.append(next)
            var text = """<text x="192" y="2" class="secondary">"""
            val lines = linesToUrlIfExist(wrapText(roadmaps.next[index].joinToString(separator = ""), 32f), roadmaps.urlMap)
            val spans = linesToMultiLineText(lines,12, 192)
            text += spans
            text += "</text>"
            sb.append(text)
        }
        if(roadmaps.later.size-1 >= index ){
            sb.append(later)
            var text = """<text x="382" y="2" class="tertiary">"""
            val lines = linesToUrlIfExist(wrapText(roadmaps.later[index].joinToString(separator = ""), 32f), roadmaps.urlMap)
            val spans = linesToMultiLineText(lines,12, 382)
            text += spans
            text += "</text>"
            sb.append(text)
        }
        sb.append("</g>")
        return sb.toString()
    }
    private fun title(title: String) = """
        <rect width="100%" height="100%" fill="#fcfcfc"/>
        <text x="26" y="60" font-family=" Arial, Helvetica, sans-serif" font-size="46">$title</text>
        <text x="105" y="100" class="now">NOW</text>
        <text x="304.5" y="100" class="next" text-anchor="middle">NEXT</text>
        <text x="504.5" y="100" class="later" text-anchor="middle">LATER</text>
    """.trimIndent()

    private fun makeNow() : String {
        return ""
    }
    private fun tail() = "</svg>"

    //language=html
    private fun style() = """
        <style>
        .now { fill: #45a98f; font-family: Arial, Helvetica, sans-serif; stroke: #45a98f; text-anchor: middle; font-weight: bold; }
        .nowBox { fill: none; font-family: Arial, Helvetica, sans-serif; stroke: #45a98f; }
        .next { fill: #e0349c; font-family: Arial, Helvetica, sans-serif; stroke: #e0349c; text-anchor: middle; font-weight: bold; }
        .nextBox { fill: none; font-family: Arial, Helvetica, sans-serif; stroke: #e0349c; text-anchor: middle; font-weight: bold; }
        .later { fill: #e56516; font-family: Arial, Helvetica, sans-serif; stroke: #e56516; text-anchor: middle; font-weight: bold; }
        .laterBox { fill: none; font-family: Arial, Helvetica, sans-serif; stroke: #e56516; text-anchor: middle; font-weight: bold; }
        .primary { font-family: Arial, Helvetica, sans-serif; font-size: 12px; color: #45a98f; }
        .secondary{ font-family: Arial, Helvetica, sans-serif; font-size: 12px; color: #e0349c; }
        .tertiary { font-family: Arial, Helvetica, sans-serif; font-size: 12px; color: #e56516; }
        .rmLink { fill: blue; text-decoration: underline; }
    </style>
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
    var text = StringBuilder()
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
Liberty server setup in production for OplLite & quick PAF
        - next
 eService is targeted for UAT. PQS is targeted for UAT. Version Support for OPLLite & QuickPaf in UAT      
        - next
emergency messages in SYS        
        - later
Contact View needed for Arch runway. NXT for arch runway.
    """.trimIndent()
    val rm = RoadMapMaker()
    val output = rm.makeRoadMapImage(str, "1.0", "OKTA Progress")
    val f = File("gen/roadmapout.svg")
    f.writeBytes(output.toByteArray())
}