package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.Point
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Deprecated("Use PieChartImproved instead")
open class PieSliceMaker {

    protected var width: Double = 600.0
    protected var height: Double = 600.0
    fun makePie(pieSlices: PieSlices) : String {

        val sb = StringBuilder()
        sb.append(startSvg(pieSlices))
        sb.append(makeDefs(pieSlices))
        sb.append("<g transform=\"translate(${(width * pieSlices.display.scale)/2},30)\">")
        sb.append("""<text x="0" y="0" text-anchor="middle" style="font-size: 24px; font-family: Arial, Helvetica, sans-serif;">${pieSlices.title.escapeXml()}</text>""")
        sb.append("""<g transform="translate(0,40)">""")
        val paths = StringBuilder()
        val labels = StringBuilder()
        generateSvgPieChart(pieSlices, width, height,300.0, paths,labels)
        sb.append(paths)
        sb.append(labels)
        //sb.append(makePaths(pieSlices))
        sb.append("</g>")
        sb.append("</g>")
        //sb.append(makeLabels(pieSlices))
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
            val id = pieSlice.label.lowercase().filterNot{it.isWhitespace()}
            val path = """
            <path class="pie" id="$id" d="M0,0 L${prevX.round(1)}, ${prevY.round(1)} A100,100 0 $largeArc,1 ${x.round(1)},${y.round(1)} Z" style="fill: ${pieSlice.displayColor(index)};">
            <title>${pieSlice.label} - ${pieSlice.amount}</title>
            </path>
            <text fill='#000000' text-anchor='middle' alignment-baseline='middle' font-size="8" font-family="Arial, Helvetica" stroke='black' stroke-width="0.2">
                <textPath href="#$id" startOffset="50%" text-anchor="middle">${pieSlice.label}</textPath>
            </text>
        """.trimIndent()
            sb.append(path)
            prevX = x
            prevY = y
            count++
        }
        return sb.toString()
    }
    fun generateSvgPieChart(pieSlices: PieSlices, width: Double, height: Double, radius: Double, paths : StringBuilder, labels : StringBuilder) {
        val total = pieSlices.sum()
        val centerX = 0.0 // Center is at 0 because we've translated the entire SVG
        val centerY = height / 2
        var startAngle = 0.0


        pieSlices.slices.forEachIndexed{index, segment ->

            val value = segment.amount
            val endAngle = startAngle + (value / total) * 360.0

            val start = polarToCartesian(centerX, centerY, radius, startAngle)
            val end = polarToCartesian(centerX, centerY, radius, endAngle)

            val largeArcFlag = if (endAngle - startAngle <= 180) "0" else "1"

            // SVG Path for the arc
            val pathData = listOf(
                "M $centerX $centerY",
                "L ${start.x} ${start.y}",
                "A $radius $radius 0 $largeArcFlag 1 ${end.x} ${end.y}",
                "Z"
            ).joinToString(" ")

            paths.append("<path class=\"pie\" d=\"$pathData\" fill=\"${segment.displayColor(index)}\" />\n")

            // Midpoint for label
            val midAngle = startAngle + (endAngle - startAngle) / 2
            val labelPoint = polarToCartesian(centerX, centerY, radius / 2, midAngle)
            //val outerLabelPoint = findArcCenter(radius, Point(start.x, start.y), Point(end.x, end.y), centerX.toInt(), centerY.toInt())
            //val labelPoint = findArcCenter(radius, Point(start.x, start.y), Point(end.x, end.y), centerX, centerY)
            val textColor = determineTextColor(STUNNINGPIE[index])
            labels.append("<text x=\"${labelPoint.x}\" y=\"${labelPoint.y}\" text-anchor=\"middle\" fill=\"$textColor\" style=\"font-size: 20px; font-family: Arial, Helvetica, sans-serif;\">")
            val fontSize = 20
            val spans = itemTextWidth(segment.label, 60F, fontSize)
            spans.add(segment.amount.toString())
            spans.forEachIndexed { index, it ->
                var dy = 0
                if(index != 0) {
                    dy = fontSize
                }
                labels.append("<tspan x=\"${labelPoint.x}\" dy=\"$dy\">${it.escapeXml()}</tspan>")

            }
            labels.append("</text>\n")
            startAngle = endAngle
        }


    }
    fun polarToCartesian(centerX: Double, centerY: Double, radius: Double, angleInDegrees: Double): Point {
        val angleInRadians = Math.toRadians(angleInDegrees)
        return Point(
            centerX + (radius * cos(angleInRadians)),
            centerY + (radius * sin(angleInRadians))
        )
    }

    protected fun startSvg(pieSlices: PieSlices) : String {
        val buffer = 120
        val baseHeight = 600
        val baseWidth = 600
        val h = pieSlices.determineMaxLegendRows() * 12 + baseHeight + buffer
        height = h.toDouble()
        width = baseWidth.toDouble()
        return """<?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" height="${(h * pieSlices.display.scale)}" width="${(baseWidth * pieSlices.display.scale)}" viewBox='0 0 ${baseWidth * pieSlices.display.scale} ${h * pieSlices.display.scale}' id="id_${pieSlices.display.id}">
          """
    }
    protected fun endSvg() = "</svg>"

    private fun makeLabels(pieSlices: PieSlices): String {
        val sb = StringBuilder()
        sb.append("<g transform='translate(10, 260)'>")
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
    protected fun makeDefs(pieSlices: PieSlices) : String {
        val defGrad = StringBuilder()
        val clrs = chartColorAsSVGColor()
        val sz = pieSlices.slices.size
        for(i in 0 until sz) {
            defGrad.append(clrs[i].linearGradient)
        }

        //language=svg
        return """
            <defs>
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
            $defGrad
            <style>
            #id_${pieSlices.display.id} .pie {
                    transition: transform 0.3s ease;
                    cursor: pointer;
                    filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, 0.2));
                }
            #id_${pieSlices.display.id} .pie:hover {
                transform: scale(1.05);
                filter: grayscale(100%) sepia(100%);
            }
            </style>
            </defs>
        """.trimIndent()
    }
    fun arc(cummulative: Double, sum: Double) = 2 * PI * (cummulative /sum)

}
fun Double.round(decimals: Int): Double {
    return String.format("%.${decimals}f", this).toDouble()

}


