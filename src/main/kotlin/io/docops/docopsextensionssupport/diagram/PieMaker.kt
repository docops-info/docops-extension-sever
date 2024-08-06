package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.support.gradientFromColor
import java.io.File

class PieMaker {



    fun makePies(pies: MutableList<Pie>) : String {
        val width = pies.size * 36
        val sb = StringBuilder()
        sb.append(makeHead(width))
        sb.append("<defs>")
        pies.forEach{
            sb.append(makeGradient(it, pies.indexOf(it)))
        }
        sb.append("</defs>")
        pies.forEachIndexed { index, pie ->
            val x = index * 36
            sb.append("""<g transform="translate($x,0)">""")
            sb.append(makePieSvg(pie, index))
            sb.append(makeLabel(pie, index))
            sb.append("</g>")
        }
        sb.append(tail())

        return sb.toString()
    }

    private fun makeHead(width: Int) = """<svg xmlns="http://www.w3.org/2000/svg"
     width="96" height="96" viewBox="0 0 $width 40">"""

    private fun tail() = """</svg>"""
    private fun makePieSvg(pie: Pie, index: Int) : String {
        //language=svg
        return """
        <svg width="36" height="36"  style="display: block;margin: 10px auto; max-width: 80%; max-height: 250px;">
        <path
              d="M18 2.0845
          a 15.9155 15.9155 0 0 1 0 31.831
          a 15.9155 15.9155 0 0 1 0 -31.831" style="fill: none;stroke: #eee;stroke-width: 3.8;"
        />
        <path
              stroke-dasharray="${pie.percent}, 100"
              d="M18 2.0845
          a 15.9155 15.9155 0 0 1 0 31.831
          a 15.9155 15.9155 0 0 1 0 -31.831" stroke="url(#pieGrad$index)" style="fill: none;stroke-width: 2.8; stroke-linecap: round; animation: progress 1s ease-out forwards;"
        />
        <text x="18" y="20.35" style="font-family: Arial, Helvetica,sans-serif;font-size: 0.5em;text-anchor: middle;stroke: url(#pieGrad$index)">${pie.percent}%</text>
    </svg>
        """.trimIndent()
    }
    private fun makeLabel(pie: Pie, index: Int): String {
        return """
            <text x="18" y="48"  style="font-family: Arial, Helvetica,sans-serif;font-size: 0.5em;text-anchor: middle;stroke: url(#pieGrad$index); font-variant: small-caps;">${pie.label}</text>
        """.trimIndent()
    }
    private fun makeGradient(pie: Pie,index: Int): String {
        val gradient = gradientFromColor(pie.color)
        return """
            <linearGradient id="pieGrad$index" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/>
            </linearGradient>
        """.trimIndent()
    }
}

fun main() {
    val pieMaker = PieMaker()
    val pies = mutableListOf<Pie>(Pie(25f, "Hello", color = "#088395"), Pie(75f, "World"))
    val svg = pieMaker.makePies(pies)
    val outfile2 = File("gen/pies.svg")
    outfile2.writeBytes(svg.toByteArray())
}