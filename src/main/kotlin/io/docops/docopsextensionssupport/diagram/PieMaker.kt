package io.docops.docopsextensionssupport.diagram

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.button.gradientFromColor
import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import java.io.File

class PieMaker {



    fun makePies(pies: Pies) : String {
        val width = pies.pies.size * 36
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
        return """<svg xmlns="http://www.w3.org/2000/svg" height="$outerHeight" width="$outerWidth" viewBox="0 0 $width $height">
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">
            $backgroundColor
            """
    }

    private fun tail() = """</svg></svg>"""
    private fun makePieSvg(pie: Pie, display: PieDisplay) : String {

        //language=svg
        return """
        <svg width="36" height="36"  style="display: block;margin: 10px auto; max-width: 80%; max-height: 250px;">
        <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" style="fill: none;stroke: ${display.baseColor};stroke-width: 3.8; filter: url(#Bevel);"
        />
        <path stroke-dasharray="${pie.percent}, 100" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" stroke="${display.outlineColor}" style="fill: none;stroke-width: 2.8; stroke-linecap: round; animation: progress 1s ease-out forwards; filter: url(#Bevel3);"
        />
        <text x="18" y="20.35" style="font-family: Arial, Helvetica,sans-serif;font-size: 7px;text-anchor: middle; fill: ${display.outlineColor};">${pie.percent}%</text>
    </svg>
        """.trimIndent()
    }
    private fun makeLabel(pie: Pie, display: PieDisplay): String {
        var fill = "#111111"
        if(display.useDark) {
            fill = "#B6FFFA"
        }
        val sb = StringBuilder()
        sb.append("""<text x="18" y="48"  style="font-family: Arial, Helvetica,sans-serif;font-size: 6px;text-anchor: middle;">""")
        val labels = pie.label.split(" ")
        labels.forEachIndexed { index, s ->
            var dy = 6
            if(0 == index) {
                dy = 0
            }
            sb.append( """
            <tspan x="18" dy="$dy" style="font-family: Arial, Helvetica,sans-serif; fill: $fill;">${s.escapeXml()}</tspan>
        """.trimIndent())
        }
        sb.append("</text>")
        return sb.toString()
    }

    private fun filters() =
         """
            <filter id="Bevel" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut" lighting-color="white">
                    <fePointLight x="-5000" y="-10000" z="20000"/>
                </feSpecularLighting>
                <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint"/>
            </filter>
            <filter id="Bevel3" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="0.2" result="blur"/>
                <feSpecularLighting in="blur" surfaceScale="10" specularConstant="3.5" specularExponent="10" result="specOut" lighting-color="#ffffff">
                    <fePointLight x="-5000" y="-10000" z="0000"/>
                </feSpecularLighting>
                <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint"/>
            </filter>
        """.trimIndent()

}

fun main() {
    val pieMaker = PieMaker()
    val pies = mutableListOf<Pie>(Pie(40f, "Mathematics"), Pie(20f, "English"), Pie(30f, "French"), Pie(10f, "Science"))
    val svg = pieMaker.makePies(Pies(pies, PieDisplay(baseColor = "#B9B4C7", outlineColor = "#DA0C81", scale = 2f, useDark = false)))
    val outfile2 = File("gen/pies.svg")
    outfile2.writeBytes(svg.toByteArray())
}