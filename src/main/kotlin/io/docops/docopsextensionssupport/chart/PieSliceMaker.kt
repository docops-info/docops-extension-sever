package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import java.io.File
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class PieSliceMaker {

    fun makePie(pieSlices: PieSlices) : String {

        val sb = StringBuilder()
        sb.append(startSvg(pieSlices))
        sb.append(makeDefs(pieSlices))
        sb.append("<g transform=\"translate(0,20)\">")
        sb.append("""<text x="100" y="10" text-anchor="middle" style="font-size: 12px; font-family: Arial, Helvetica, sans-serif;">${pieSlices.title}</text>""")
        sb.append("""<g transform="translate(100,120) rotate(-90)" style="stroke: #fcfcfc; stroke-width: 2px;">""")
        sb.append(makePaths(pieSlices))
        sb.append("</g>")
        sb.append("</g>")
        sb.append(makeLabels(pieSlices))
        sb.append(endSvg())
        return joinXmlLines(sb.toString())
    }

    private fun makePaths(pieSlices: PieSlices) : String {
        val sb = StringBuilder()
        var prevX = 100.0
        var prevY = 0.0
        var count = 0
        var cumulative = 0.0
        val sum = pieSlices.slices.sumOf { it.amount }
        var largeArc: Int
        pieSlices.slices.forEachIndexed { index, pieSlice ->
            cumulative += pieSlice.amount
            val pct = pieSlice.amount / sum * 100
            if(pct > 50.0) {
                largeArc = 1
            } else {
                largeArc = 0
            }
            val arc = arc(cumulative, sum)
            val x = cos(arc) * 100
            val y = sin(arc) * 100
            val path = """
            <path d = "M0,0 L${prevX.round(1)}, ${prevY.round(1)} A100,100 0 $largeArc,1 ${x.round(1)},${y.round(1)} Z" style="fill: ${pieSlice.displayColor(index)};">
            <title>${pieSlice.label} - ${pieSlice.amount}</title>
            </path>
        """.trimIndent()
            sb.append(path)
            prevX = x
            prevY = y
            count++
        }
        return sb.toString()
    }
    private fun startSvg(pieSlices: PieSlices) : String {
        val buffer = 80
        val baseHeight = 200
        val h = pieSlices.determineMaxLegendRows() * 12 + baseHeight + buffer

        return """<?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" height="$h" width="220" viewBox='0 0 220.0 $h'>
          """
    }
    private fun endSvg() = "</svg>"

    private fun makeLabels(pieSlices: PieSlices): String {
        val sb = StringBuilder()
        sb.append("<g transform='translate(10, 240)'>")
        val chunks = pieSlices.slices.chunked(pieSlices.display.legendRows)
        var startX = 0
        var count = 0
        chunks.forEach {
            var startY= 2
            it.forEachIndexed { index, pieSlice ->
                sb.append("""<rect x="$startX" y="$startY" height="8" width="8" fill="${pieSlice.displayColor(count++)}"/>""")
                startY += 10
            }
            startX += 200 / chunks.size
        }

        startX  = 10
        chunks.forEach {
            sb.append("<text style=\"font-size: 10px; font-family: Arial, Helvetica, sans-serif;\">")
            it.forEachIndexed { index, pieSlice ->
                sb.append("<tspan x='$startX' dy='10' style=\"font-size: 8px; font-family: Arial, Helvetica, sans-serif;\">${pieSlice.label} - ${pieSlice.amount}</tspan>")
            }
            startX += 200 / chunks.size
            sb.append("</text>")
        }

        sb.append("</g>")
        return sb.toString()
    }
    private fun makeDefs(pieSlices: PieSlices) : String {
        //language=svg
        return """
            <defs/>
        """.trimIndent()
    }
}

fun main() {
    val maker= PieSliceMaker()
    val pieSlices = PieSlices(title = "Favorite Anime",mutableListOf(PieSlice(label= "Naruto", amount = 5.0),
        PieSlice(label = "Bleach", amount = 4.0),
        PieSlice(label = "One Piece", amount = 9.0),
        PieSlice(label = "One Punch Man", amount = 7.0),
        PieSlice(label = "My Hero Academia", amount = 6.0),
        PieSlice(label = "Demon Slayer", amount = 10.0),
    ))
    val svg = maker.makePie(pieSlices)

    val str = Json.encodeToString(pieSlices)
    println(str)
    val outfile2 = File("gen/pieslice.svg")
    outfile2.writeBytes(svg.toByteArray())
}