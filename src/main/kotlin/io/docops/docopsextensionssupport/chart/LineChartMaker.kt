package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class LineChartMaker {

    private val maxHeight = 360
    private val maxGraphHeight = 410
    private val maxWidth = 640
    private val maxGraphWidth = 730
    fun makeLineChart(lineChart: LineChart): String {
        val sb = StringBuilder()
        sb.append(makeHead(lineChart))
        sb.append(makeDefs())
        sb.append("<rect width='100%' height='100%' fill='url(#backGrad1)' stroke='#111111'/>")
        sb.append("""<text x="${maxGraphWidth/2}" y="14" fill="#fcfcfc" font-size="12pt" font-family="Arial, Helvetica, sans-serif" text-anchor="middle">${lineChart.title}</text>""")
        lineChart.points.forEachIndexed { index, mutableList ->
            sb.append("<g>")
            sb.append(makePoints(mutableList, index, lineChart))
            sb.append("</g>")
        }
        sb.append("""<rect width="100%" height="37" x="0" y="0" fill="url(#backGrad1)"/>""")
        val points = lineChart.points[0]
        val xGap = maxGraphWidth / (points.points.size + 1)
        var num = xGap
        points.points.forEach{  point ->
            sb.append("""<text x="${num-8}" y="30" style="font-size:11px; font-family: Arial, Helvetica, sans-serif; font-weight:bold; fill:#fcfcfc;">${point.label}</text>""")
            num += xGap
        }
        sb.append(legend(lineChart))
        sb.append(end())
        return joinXmlLines(sb.toString())
    }

    private fun legend(chart: LineChart): String {
        val sb = StringBuilder()
        sb.append("""
  <rect width="100%" height="50" x="0" y="360" fill="url(#backGrad1)"/>
  <g transform="translate(0, 10)">""")
        var x = 8
        var textX = 15
        chart.points.forEachIndexed { index, mutableList ->
            sb.append(
                """<circle r="5" cx="$x" cy="366" fill="${DefaultChartColors[index]}"/>
    <text x="$textX" y="370" font-family="Arial, Helvetica, sans-serif" font-size="10px" fill="#fcfcfc">${mutableList.label} </text>
        """.trimIndent()
            )
            x += mutableList.textWidth() + 15
            textX += mutableList.textWidth() + 15
        }
        sb.append("</g>")
        return sb.toString()

    }

    private fun end() = "</svg>"
    private fun makeHead(lineChart: LineChart): String {

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg height="$maxGraphHeight" width="$maxGraphWidth" viewBox="0 0 $maxGraphWidth $maxGraphHeight" id="ID_${lineChart.id}" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="none">
        """.trimIndent()
    }


    private fun makeDefs(): String {
        return """
            <defs>
             <linearGradient id="backGrad1" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="#5c5c5c"/>
                <stop class="stop2" offset="30%" stop-color="#4c4c4c"/>
                <stop class="stop3" offset="100%" stop-color="#111111"/>
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
    private fun makePoints(points: Points, index: Int, lineChart: LineChart): String {
        val maxData = points.points.maxOf { it.y } + 100
        val oneUnit = maxHeight / maxData
        val xGap = maxGraphWidth / (points.points.size + 1)
        val yGap = maxHeight / (points.points.size + 1)
        var num = xGap
        var num2 = yGap
        val str = StringBuilder()
        val sb = StringBuilder()
        val elements = StringBuilder()
        points.points.forEachIndexed { itemIndex, item ->
            val y = maxHeight - (item.y * oneUnit)
            if (index == 0) {
                elements.append("""<polyline points="$num,0 $num,$maxHeight" style="stroke: #aaaaaa"/>""")
                elements.append("""<polyline points="0,$num2 $maxGraphWidth,$num2" style="stroke: #aaaaaa"/>""")
                elements.append("""<text x="${num-8}" y="30" style="font-size:11px; font-family: Arial, Helvetica, sans-serif; font-weight:bold; fill:#fcfcfc;">${item.label}</text>""")
            }
            elements.append("""<text x="${num-8}" y="${y - 10}" style="fill:#fcfcfc; font-size:12px;font-family: Arial, Helvetica, sans-serif;" visibility="hidden" id="text_${lineChart.id}_${index}_$itemIndex">${item.y}</text>""")
            str.append(" $num,$y")
            elements.append("""<circle r="5" cx="$num" cy="$y" style="cursor: pointer; stroke: #fcfcfc; fill: ${DefaultChartColors[index]};" onmouseover="showText('text_${lineChart.id}_${index}_$itemIndex')" onmouseout="hideText('text_${lineChart.id}_${index}_$itemIndex')"/>""")
            num += xGap
            num2 += yGap
        }
        sb.append("""<polyline points="$str" style="stroke: ${DefaultChartColors[index]}; stroke-width: 2; fill: none;"/>""")
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
    val p1 = Points("Sales", points)
    val p2 = Points("Marketing", points2)
    val p3 = Points("Development", points3)
    val lc = LineChart(
        title = "Point on graph",
        points = mutableListOf(p1, p2,p3)
    )
    val svg = maker.makeLineChart(lc)

    val str = Json.encodeToString(lc)
    println(str)
    val outfile2 = File("gen/line.svg")
    outfile2.writeBytes(svg.toByteArray())
}