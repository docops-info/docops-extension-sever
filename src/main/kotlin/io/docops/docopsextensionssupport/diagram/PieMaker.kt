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
            sb.append(makeGradient(pieDisplay = pies.pieDisplay))
            sb.append(filters())
        sb.append("</defs>")
        pies.pies.forEachIndexed { index, pie ->
            val x = index * 36
            sb.append("""<g transform="translate($x,0)">""")
            sb.append(makePieSvg(pie, pies.pieDisplay.outlineColor))
            sb.append(makeLabel(pie))
            sb.append("</g>")
        }
        sb.append(tail())

        return joinXmlLines(sb.toString())
    }

    private fun makeHead(width: Int, pies: Pies) : String {
        val height = pies.maxRows() * 10 + 40
        return """<svg xmlns="http://www.w3.org/2000/svg" height="192" width="256" viewBox="0 0 $width $height">
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">"""
    }

    private fun tail() = """</svg></svg>"""
    private fun makePieSvg(pie: Pie, outlineColor: String) : String {
        //language=svg
        return """
        <svg width="36" height="36"  style="display: block;margin: 10px auto; max-width: 80%; max-height: 250px;">
        <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" style="fill: none;stroke: url(#pieGrad);stroke-width: 3.8;; filter: url(#Bevel);"
        />
        <path stroke-dasharray="${pie.percent}, 100" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" stroke="url(#pieGrad1)" style="fill: none;stroke-width: 2.8; stroke-linecap: round; animation: progress 1s ease-out forwards; filter: url(#Bevel3);"
        />
        <text x="18" y="20.35" style="font-family: Arial, Helvetica,sans-serif;font-size: 0.5em;text-anchor: middle; fill: $outlineColor;">${pie.percent}%</text>
    </svg>
        """.trimIndent()
    }
    private fun makeLabel(pie: Pie): String {
        val sb = StringBuilder()
        sb.append("""<text x="18" y="48"  style="font-family: Arial, Helvetica,sans-serif;font-size: 0.3em;text-anchor: middle;">""")
        val labels = pie.label.split(" ")
        labels.forEachIndexed { index, s ->
            var dy = 6
            if(0 == index) {
                dy = 0
            }
            sb.append( """
            <tspan x="18" dy="$dy" style="font-family: Arial, Helvetica,sans-serif;">${s.escapeXml()}</tspan>
        """.trimIndent())
        }
        sb.append("</text>")
        return sb.toString()
    }
    private fun makeGradient(pieDisplay: PieDisplay): String {
        val gradient1 = gradientFromColor(pieDisplay.baseColor)
        val gradient2 = gradientFromColor(pieDisplay.outlineColor)
        return """
        <linearGradient id="pieGrad" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${gradient1["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient1["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient1["color3"]}"/>
        </linearGradient>
        <linearGradient id="pieGrad1" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${gradient2["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient2["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient2["color3"]}"/>
        </linearGradient>
        """.trimIndent()
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
    val pies = mutableListOf<Pie>(Pie(25f, "Hello"), Pie(75f, "World"))
    val svg = pieMaker.makePies(Pies(pies, PieDisplay(baseColor = "#FFEEA9", outlineColor = "#C40C0C")))
    val outfile2 = File("gen/pies.svg")
    outfile2.writeBytes(svg.toByteArray())
}