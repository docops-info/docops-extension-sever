package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import java.io.File

class PieMaker {



    fun makePies(pies: Pies) : String {
        val width = pies.pies.size * 36 + 20
        val sb = StringBuilder()
        sb.append(makeHead(width, pies))
        sb.append("<defs>")
            sb.append(filters())
        sb.append("</defs>")
        pies.pies.forEachIndexed { index, pie ->
            val x = index * 36
            sb.append("""<g transform="translate($x,0)">""")
            sb.append(makePieSvg(pie, pies.pieDisplay))
            sb.append(makeLabel(pie, pies.pieDisplay))
            sb.append("</g>")
        }
        sb.append(tail())

        return joinXmlLines(sb.toString())
    }

    private fun makeHead(width: Int, pies: Pies) : String {
        val height = pies.maxRows() * 10 + 40
        val outerHeight = (1+pies.pieDisplay.scale) * height
        val outerWidth = (1+pies.pieDisplay.scale) * width
        var backgroundColor = ""
        if(pies.pieDisplay.useDark) {
            backgroundColor = """<rect width="100%" height="100%" fill="#374151"/>"""
        }
        return """<svg xmlns="http://www.w3.org/2000/svg" height="${outerHeight/ DISPLAY_RATIO_16_9}" width="${outerWidth/DISPLAY_RATIO_16_9}" viewBox="0 0 $width $height">
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">
            $backgroundColor
            """
    }

    private fun tail() = """</svg></svg>"""
    private fun makePieSvg(pie: Pie, display: PieDisplay) : String {
        var fill = "#fcfcfc"
        if(display.useDark) {
            fill = "none"
        }
        //language=svg
        return """
        <svg class="shadowed" width="36" height="36"  style="display: block;margin: 10px auto; max-width: 80%; max-height: 250px;">
        <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" style="fill: ${fill};stroke: ${display.baseColor};stroke-width: 1;"/>
        <path stroke-dasharray="${pie.percent}, 100" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" stroke="${display.outlineColor}" style="fill: none;stroke-width: 1.2; stroke-linecap: round; animation: progress 1s ease-out forwards;">
            <animate attributeName="stroke-dashoffset" values="${pie.percent};0" dur="2s" repeatCount="1"/>
        </path>
        <text x="18" y="20.35" style="font-family: Arial, Helvetica,sans-serif;font-size: 7px;text-anchor: middle; fill: ${display.outlineColor};">${pie.percent}%</text>
    </svg>
        """.trimIndent()
    }
    private fun makeLabel(pie: Pie, display: PieDisplay): String {
        /*var fill = "#111111"
        if(display.useDark) {
            fill = "#B6FFFA"
        }*/
        val sb = StringBuilder()
        sb.append("""<text x="18" y="48"  style="font-family: Arial, Helvetica,sans-serif;font-size: 6px;text-anchor: middle;">""")
        val labels = pie.label.split(" ")
        labels.forEachIndexed { index, s ->
            var dy = 6
            if(0 == index) {
                dy = 0
            }
            sb.append( """
            <tspan x="18" dy="$dy" style="font-family: Arial, Helvetica,sans-serif; fill: ${display.baseColor};">${s.escapeXml()}</tspan>
        """.trimIndent())
        }
        sb.append("</text>")
        return sb.toString()
    }

    private fun filters() =
         """
             <style>
                .shadowed {
                    -webkit-filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
                    filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
                }
            </style>
         """.trimIndent()

}

fun main() {
    val pieMaker = PieMaker()
    val pies = mutableListOf(Pie(percent = 40f, label = "Mathematics"), Pie(percent = 20f, label = "English"), Pie(
        percent = 30f,
        label = "French"
    ), Pie(percent = 10f, label = "Science"))
    val svg = pieMaker.makePies(Pies(
        pies = pies,
        pieDisplay = PieDisplay(baseColor = "#B9B4C7", outlineColor = "#DA0C81", scale = 2f, useDark = false)
    ))
    val outfile2 = File("gen/pies.svg")
    outfile2.writeBytes(svg.toByteArray())
}