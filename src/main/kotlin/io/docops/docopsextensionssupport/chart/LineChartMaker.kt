package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class LineChartMaker {

    private val maxHeight = 360
    private val maxWidth = 640
    fun makeLineChart(lineChart: LineChart): String {

        val sb = StringBuilder()
        sb.append(makeHead(lineChart))
        sb.append(makeDefs())
        sb.append("<rect width='100%' height='100%' fill='url(#backGrad1)' stroke='#111111'/>")
        lineChart.points.forEachIndexed { index, mutableList ->
            sb.append("<g>")
            sb.append(makePoints(mutableList, index, lineChart))
            sb.append("</g>")
        }
        sb.append(end())
        return joinXmlLines(sb.toString())
    }

    private fun end() = "</svg>"
    private fun makeHead(lineChart: LineChart): String {

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg height="$maxHeight" width="$maxWidth" viewBox="0 0 $maxWidth $maxHeight" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="none">
        """.trimIndent()
    }


    private fun makeDefs(): String {
        return """
            <defs>
             <linearGradient id="backGrad1" x2="0%" y2="100%">
                 <stop class="stop1" offset="0%" stop-color="#9ea1a8"/>
                <stop class="stop2" offset="50%" stop-color="#6d727c"/>
                <stop class="stop3" offset="100%" stop-color="#3d4451"/>
            </linearGradient>
            <script>
            function showText(id) {
                var tooltip = document.getElementById(id);
                tooltip.style.visibility = "visible";
            }
            function hideText(id) {
                var tooltip = document.getElementById(id);
                tooltip.style.visibility = "hidden";
            }
            </script>
            </defs>
        """.trimIndent()
    }
    private fun makePoints(points: List<Point>, index: Int, lineChart: LineChart): String {
        val maxData = points.maxOf { it.y } + 100
        val oneUnit = maxHeight / maxData
        val xGap = maxWidth / (points.size + 1)
        val yGap = maxHeight / (points.size + 1)
        var num = xGap
        var num2 = yGap
        val str = StringBuilder()
        val sb = StringBuilder()
        val elements = StringBuilder()
        points.forEachIndexed { itemIndex, item ->
            val y = maxHeight - (item.y * oneUnit)
            if (index == 0) {
                elements.append("""<polyline points="$num,0 $num,$maxHeight" style="stroke: #aaaaaa"/>""")
                elements.append("""<polyline points="0,$num2 $maxWidth,$num2" style="stroke: #aaaaaa"/>""")
                elements.append("""<text x="$num" y="12" style="font-size:11px; font-family: Arial, Helvetica, sans-serif; font-weight:bold;">${item.label}</text>""")
            }
            elements.append("""<text x="$num" y="${y - 10}" style="font-size:12px;font-family: Arial, Helvetica, sans-serif;" visibility="hidden" id="text_${lineChart.id}_${index}_$itemIndex">${item.y}</text>""")

            str.append(" $num,$y")
            sb.append("""<polyline points="$str" style="stroke: ${DefaultChartColors[index]}; stroke-width: 2; fill: none;"/>""")
            str.append(" $num,$y")
            elements.append("""<circle r="5" cx="$num" cy="$y" style="cursor: pointer; stroke: #ffffff; fill: ${DefaultChartColors[index]};" onmouseover="showText('text_${lineChart.id}_${index}_$itemIndex')" onmouseout="hideText('text_${lineChart.id}_${index}_$itemIndex')"/>""")
            num += xGap
            num2 += yGap
        }
        sb.append(elements)
        return sb.toString()
    }
}

fun main() {
    val maker = LineChartMaker()
    val points = mutableListOf<Point>(
        Point(label = "Jan", y = 40.0), Point(label = "Feb", y = 70.0), Point(label = "Mar", 90.0),
        Point(label = "Apr", 70.0), Point(label = "May", 40.0), Point(label = "Jun", 30.0),
        Point(label = "Jul", 60.0), Point(label = "Aug", 90.0), Point(label = "Sept", 70.0)
    )
    val points2 = mutableListOf<Point>(
        Point("Jan", y = 22.0), Point(label = "Feb", y = 33.0), Point(label = "Mar", 44.0),
        Point(label = "Apr", 55.0), Point(label = "May", 66.0), Point(label = "Jun", 77.0),
        Point(label = "Jul", 88.0), Point(label = "Aug", 109.0), Point(label = "Sept", 110.0)
    )
    val points3 = mutableListOf<Point>(
        Point("Jan", y = 56.0), Point(label = "Feb", y = 65.0), Point(label = "Mar", 78.0),
        Point(label = "Apr", 22.0), Point(label = "May", 160.0), Point(label = "Jun", 94.0),
        Point(label = "Jul", 56.0), Point(label = "Aug", 23.0), Point(label = "Sept", 201.0)
    )
    val lc = LineChart(
        title = "Point on graph",
        points = mutableListOf<MutableList<Point>>(points, points2, points3)
    )
    val svg = maker.makeLineChart(lc)

    val str = Json.encodeToString(lc)
    println(str)
    val outfile2 = File("gen/line.svg")
    outfile2.writeBytes(svg.toByteArray())
}