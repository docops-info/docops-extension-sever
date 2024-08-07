package io.docops.docopsextensionssupport.diagram

import io.docops.asciidoc.utils.escapeXml
import java.io.File

class PieMaker {



    fun makePies(pies: Pies) : String {
        val width = pies.pies.size * 36
        val sb = StringBuilder()
        sb.append(makeHead(width, pies))
        sb.append("<defs>")
            sb.append(makeGradient())
        sb.append("</defs>")
        pies.pies.forEachIndexed { index, pie ->
            val x = index * 36
            sb.append("""<g transform="translate($x,0)">""")
            sb.append(makePieSvg(pie))
            sb.append(makeLabel(pie))
            sb.append("</g>")
        }
        sb.append(tail())

        return sb.toString()
    }

    private fun makeHead(width: Int, pies: Pies) : String {
        println(pies.maxRows())
        val height = pies.maxRows() * 10 + 40
        return """<svg xmlns="http://www.w3.org/2000/svg"
     width="$width" height="$height" viewBox="0 0 $width $height">"""
    }

    private fun tail() = """</svg>"""
    private fun makePieSvg(pie: Pie) : String {
        //language=svg
        return """
        <svg width="36" height="36"  style="display: block;margin: 10px auto; max-width: 80%; max-height: 250px;">
        <path
              d="M18 2.0845
          a 15.9155 15.9155 0 0 1 0 31.831
          a 15.9155 15.9155 0 0 1 0 -31.831" style="fill: none;stroke: url(#pieGrad);stroke-width: 3.8;"
        />
        <path
              stroke-dasharray="${pie.percent}, 100"
              d="M18 2.0845
          a 15.9155 15.9155 0 0 1 0 31.831
          a 15.9155 15.9155 0 0 1 0 -31.831" stroke="url(#pieGrad1)" style="fill: none;stroke-width: 2.8; stroke-linecap: round; animation: progress 1s ease-out forwards;"
        />
        <text x="18" y="20.35" style="font-family: Arial, Helvetica,sans-serif;font-size: 0.5em;text-anchor: middle;fill: fill: #9cdefc;">${pie.percent}%</text>
    </svg>
        """.trimIndent()
    }
    private fun makeLabel(pie: Pie): String {
        val sb = StringBuilder()
        sb.append("""<text x="18" y="48"  style="font-family: Arial, Helvetica,sans-serif;font-size: 0.5em;text-anchor: middle;stroke: url(#pieGrad0); ">""")
        val labels = pie.label.split(" ")
        labels.forEachIndexed { index, s ->
            var dy = "10"
            if(0 == index) {
                dy = "0"
            }
            sb.append( """
            <tspan x="18" dy="$dy">${s.escapeXml()}</tspan>
        """.trimIndent())
        }
        sb.append("</text>")
        return sb.toString()
    }
    private fun makeGradient(): String {
        return """
            <linearGradient id="pieGrad" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="#9cdefc"/>
                <stop class="stop2" offset="50%" stop-color="#6bcefa"/>
                <stop class="stop3" offset="100%" stop-color="#3ABEF9"/>
            </linearGradient>
            <linearGradient id="pieGrad1" x2="0%" y2="100%">
            <stop class="stop2" offset="50%" stop-color="#4348b4"/>
            <stop class="stop3" offset="100%" stop-color="#050C9C"/>
        </linearGradient>
        """.trimIndent()
    }
}

fun main() {
    val pieMaker = PieMaker()
    val pies = mutableListOf<Pie>(Pie(25f, "Hello", color = "#088395"), Pie(75f, "World"))
    val svg = pieMaker.makePies(Pies(pies))
    val outfile2 = File("gen/pies.svg")
    outfile2.writeBytes(svg.toByteArray())
}