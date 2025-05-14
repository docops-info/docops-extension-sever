package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


/**
 * The `LineChartMaker` class is responsible for creating a line chart in SVG format. This class handles the
 * generation of the SVG elements required to display a line chart with various customization options.
 *
 * @property isPdf A boolean property to determine if the output should be in PDF format.
 */
class LineChartMaker(val isPdf: Boolean) {

    private val maxHeight = 360
    private val maxGraphHeight = 410
    private val maxWidth = 640
    private val maxGraphWidth = 730
    private var fontColor = "#111111"

    /**
     * Generates an SVG string representation of a line chart from the provided `LineChart` object.
     *
     * @param lineChart The `LineChart` object containing the data and display properties for the line chart.
     * @return A `String` containing the SVG representation of the line chart.
     */
    fun makeLineChart(lineChart: LineChart): String {
        val sb = StringBuilder()
        sb.append(makeHead(lineChart))
        sb.append(makeDefs(lineChart))
        fontColor = determineTextColor(lineChart.display.backgroundColor)
        sb.append("<rect width='100%' height='100%' fill='#f8f9fa' rx='15' ry='15' stroke='#ddd' stroke-width='1'/>")
        val toolTip = StringBuilder()
        lineChart.points.forEachIndexed { index, mutableList ->
            sb.append("<g>")
            sb.append(makePoints(mutableList, index, lineChart, toolTip))
            sb.append("</g>")
        }

        // Add title bar similar to bar.svg
        val titleWidth = 378
        val titleX = (maxGraphWidth - titleWidth) / 2
        sb.append("""<g>
            <rect x="$titleX" y="10" width="$titleWidth" height="40" rx="10" ry="10" fill="#f0f0f0" opacity="0.7"></rect>
            <text x="${maxGraphWidth/2}" y="38" style="font-family: Arial, sans-serif; fill: #333; text-anchor: middle; font-size: 24px; font-weight: bold; filter: drop-shadow(2px 2px 1px rgba(0, 0, 0, .2));">${lineChart.title}</text>
        </g>""")

        val points = lineChart.points[0]
        val xGap = maxGraphWidth / (points.points.size + 1)
        var num = xGap
        points.points.forEach{  point ->
            sb.append("""<text x="${num-8}" y="70" style="font-size:11px; font-family: Arial, Helvetica, sans-serif; font-weight:bold; fill:$fontColor;">${point.label}</text>""")
            num += xGap
        }
        sb.append(legend(lineChart))
        sb.append(addTicks(lineChart))
        if(!isPdf) {
            sb.append(toolTip)
        }
        sb.append(end())
        return joinXmlLines(sb.toString())
    }

    private fun legend(chart: LineChart): String {
        val sb = StringBuilder()

        // Calculate legend dimensions based on content
        val itemsPerRow = 4
        val numItems = chart.points.size
        val numRows = (numItems + itemsPerRow - 1) / itemsPerRow // Ceiling division

        // Calculate text height for proper spacing
        val textHeight = "Sample".textHeight("Arial", 12)
        val rowHeight = textHeight + 8 // Add some padding

        // Calculate total legend height needed
        val titleHeight = 30 // Space for "Legend" title
        val legendContentHeight = numRows * rowHeight
        val legendPadding = 20 // Padding at top and bottom
        val totalLegendHeight = titleHeight + legendContentHeight + legendPadding

        // Create a legend box with appropriate height
        val legendWidth = 600
        val legendX = (maxGraphWidth - legendWidth) / 2

        // Calculate the adjusted position for the legend
        val legendY = maxHeight + 30 // Position legend below the chart with more padding

        sb.append("""
  <g transform="translate(0, $legendY)">
    <rect x="$legendX" y="10" width="$legendWidth" height="$totalLegendHeight" rx="15" ry="15" fill="#f0f0f0" stroke="#ddd" stroke-width="1" opacity="0.9" filter="url(#dropShadow)"/>
    <text x="${maxGraphWidth/2}" y="30" text-anchor="middle" style="font-family: Arial, sans-serif; fill: #666; font-size: 16px; font-weight: bold;">Legend</text>
  """)

        // Position legend items in a grid layout
        var x = legendX + 30
        var y = 50
        var itemCount = 0

        // Calculate maximum width needed for each legend item
        val maxItemWidth = chart.points.maxOfOrNull { 
            it.label.textWidth("Arial", 12) + 30 // text width + color box + padding
        } ?: 150

        // Adjust items per row based on available width and item width
        val availableWidth = legendWidth - 60 // Subtract padding
        val calculatedItemsPerRow = maxOf(1, availableWidth / maxItemWidth)
        val effectiveItemsPerRow = minOf(itemsPerRow, calculatedItemsPerRow)

        // Calculate spacing between items
        val itemSpacing = availableWidth / effectiveItemsPerRow

        chart.points.forEachIndexed { index, mutableList ->
            // Move to next row if needed
            if (itemCount > 0 && itemCount % effectiveItemsPerRow == 0) {
                y += rowHeight
                x = legendX + 30
            }

            // Calculate text width for proper sizing
            val textWidth = mutableList.label.textWidth("Arial", 12)

            // Create a background rect that encompasses both the color box and text
            val rectWidth = textWidth + 30 // Color box (12) + spacing (18) + padding
            val rectHeight = rowHeight - 2 // Slightly smaller than row height

            sb.append("""
    <g class="legend-item">
      <rect x="${x - 5}" y="${y - 5}" width="$rectWidth" height="$rectHeight" rx="4" ry="4" fill="white" opacity="0" class="legend-item-bg"/>
      <rect x="$x" y="$y" width="12" height="12" rx="2" ry="2" fill="${STUNNINGPIE[index]}"/>
      <text x="${x + 18}" y="${y + 10}" style="font-family: Arial, sans-serif; fill: #666; font-size: 12px;">${mutableList.label}</text>
    </g>""")

            // Move to the next item position using calculated spacing
            x += itemSpacing
            itemCount++
        }

        sb.append("</g>")
        return sb.toString()

    }
    private fun addTicks( lineChart: LineChart): String {
        val sb = StringBuilder()

        val nice = lineChart.ticks()
        val minV = nice.getNiceMin()
        val maxV = nice.getNiceMax()
        val tickSpacing = nice.getTickSpacing()
        var i = minV
        val maxData = lineChart.points[0].points.maxOf { it.y } + 100
        val oneUnit = maxHeight / maxData

        // Add y-axis line
        sb.append("""
        <line x1="50" x2="50" y1="0" y2="$maxHeight" stroke="$fontColor" stroke-width="1.5"/>
        """.trimIndent())

        // Add tick marks and labels
        while(i <= maxV) {
            val y = maxHeight - (i * oneUnit)

            // Add tick mark
            sb.append("""
            <line x1="45" x2="55" y1="$y" y2="$y" stroke="$fontColor" stroke-width="1.5"/>
            """.trimIndent())

            // Add label
            sb.append("""
            <text x="40" y="${y+4}" text-anchor="end" style="font-family: Arial, Helvetica, sans-serif; fill: $fontColor; font-size:11px;">${lineChart.valueFmt(i)}</text>
            """.trimIndent())

            // Add horizontal grid line
            sb.append("""
            <line x1="50" x2="$maxGraphWidth" y1="$y" y2="$y" class="grid-line" stroke-dasharray="3,3" stroke="#dddddd" stroke-width="0.5"/>
            """.trimIndent())

            i += tickSpacing
        }

        return sb.toString()
    }

    private fun end() = "</svg>"
    private fun makeHead(lineChart: LineChart): String {
        // Calculate the additional height needed for the legend
        val itemsPerRow = 4
        val numItems = lineChart.points.size
        val numRows = (numItems + itemsPerRow - 1) / itemsPerRow // Ceiling division

        // Calculate text height for proper spacing
        val textHeight = "Sample".textHeight("Arial", 12)
        val rowHeight = textHeight + 8 // Add some padding

        // Calculate total legend height needed
        val titleHeight = 30 // Space for "Legend" title
        val legendContentHeight = numRows * rowHeight
        val legendPadding = 20 // Padding at top and bottom
        val totalLegendHeight = titleHeight + legendContentHeight + legendPadding

        // Adjust maxGraphHeight to accommodate the legend with extra padding
        val extraPadding = 40 // Additional padding to ensure legend fits
        val adjustedGraphHeight = maxGraphHeight + totalLegendHeight + extraPadding

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg height="${adjustedGraphHeight/DISPLAY_RATIO_16_9}" width="${maxGraphWidth/DISPLAY_RATIO_16_9}" viewBox="0 0 $maxGraphWidth $adjustedGraphHeight" id="ID_${lineChart.id}" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="none">
        """.trimIndent()
    }


    private fun makeDefs(lineChart: LineChart): String {
        val clr = SVGColor(lineChart.display.backgroundColor, "backGrad${lineChart.display.id}")
        val defGrad = StringBuilder()
        STUNNINGPIE.forEachIndexed { idx, item->
            val gradient = SVGColor(item, "defColor_$idx")
            defGrad.append(gradient.linearGradient)
            // Add area gradient for under the line
            defGrad.append("""
                <linearGradient id="areaGradient_$idx" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="${item}" stop-opacity="0.7" />
                    <stop offset="100%" stop-color="${item}" stop-opacity="0.1" />
                </linearGradient>
            """.trimIndent())
        }
        return """
            <defs>
             ${clr.linearGradient}
             $defGrad
             <filter id="dropShadow" height="130%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="3"/> 
                <feOffset dx="2" dy="2" result="offsetblur"/>
                <feComponentTransfer>
                    <feFuncA type="linear" slope="0.2"/>
                </feComponentTransfer>
                <feMerge> 
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/> 
                </feMerge>
             </filter>
             <filter id="glow" height="130%">
                <feGaussianBlur in="SourceGraphic" stdDeviation="2.5" result="blur"/>
                <feMerge>
                    <feMergeNode in="blur"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
             </filter>
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
            <style>
                .grid-line {
                    stroke: #dddddd;
                    stroke-width: 0.5;
                    stroke-dasharray: 3,3;
                }
                .chart-point {
                    transition: all 0.3s ease;
                }
                .chart-point:hover {
                    r: 7;
                    filter: url(#glow);
                }
                .chart-line {
                    stroke-linecap: round;
                    stroke-linejoin: round;
                }
                .chart-area {
                    opacity: 0.7;
                }
                .legend-item {
                    transition: all 0.3s ease;
                    cursor: pointer;
                }
                .legend-item:hover {
                    transform: translateX(5px);
                }
                .legend-item:hover text {
                    font-weight: bold;
                }
                .legend-item:hover .legend-item-bg {
                    opacity: 0.2;
                }
                .shadowed {
                    filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
                }
            </style>
            </defs>
        """.trimIndent()
    }
    private fun makePoints(points: Points, index: Int, lineChart: LineChart, toolTip: StringBuilder): String {
        val maxData = points.points.maxOf { it.y } + 100
        val oneUnit = maxHeight / maxData
        val xGap = maxGraphWidth / (points.points.size + 1)
        val yGap = maxHeight / (points.points.size + 1)
        var num = xGap
        var num2 = yGap
        val str = StringBuilder()
        val sb = StringBuilder()
        val elements = StringBuilder()
        val cMap = SVGColor(STUNNINGPIE[index])
        val toolTipGen = ToolTip()

        // Create grid lines first (only once for the first series)
        if (index == 0) {
            // Horizontal grid lines
            for (i in 1..5) {
                val y = (maxHeight / 5) * i
                elements.append("""<line x1="0" y1="$y" x2="$maxGraphWidth" y2="$y" class="grid-line" />""")
            }

            // Vertical grid lines
            for (i in 1..points.points.size) {
                val x = xGap * i
                elements.append("""<line x1="$x" y1="0" x2="$x" y2="$maxHeight" class="grid-line" />""")
            }
        }

        // Collect points for the line and area
        val areaPoints = StringBuilder(str)
        areaPoints.append(" $xGap,$maxHeight 0,$maxHeight") // Close the path for area fill

        points.points.forEachIndexed { itemIndex, item ->
            val y = maxHeight - (item.y * oneUnit)

            // Labels are already added in makeLineChart method

            if(isPdf) {
                elements.append("""<text x="${num-8}" y="${y - 10}" style="fill:$fontColor; font-size:12px;font-family: Arial, Helvetica, sans-serif;" id="text_${lineChart.id}_${index}_$itemIndex">${item.y}</text>""")
            }

            str.append(" $num,$y")
            areaPoints.append(" $num,$y")

            // Add interactive points with enhanced styling
            elements.append("""<circle class="chart-point" r="5" cx="$num" cy="$y" style="cursor: pointer; stroke: $fontColor; fill: url(#defColor_$index); filter: url(#dropShadow);" onmouseover="showText('text_${lineChart.id}_${index}_$itemIndex')" onmouseout="hideText('text_${lineChart.id}_${index}_$itemIndex')"/>""")

            // Enhanced tooltip
            val tipWidth = points.label.textWidth("Helvetica", 12) + 24
            var fillColor = determineTextColor(STUNNINGPIE[index])
            toolTip.append("""
                <g transform="translate(${num},${y -20})" visibility="hidden" id="text_${lineChart.id}_${index}_$itemIndex">
                <path d="${toolTipGen.getTopToolTip(ToolTipConfig(width = tipWidth, height = 60, radius = 8))}" 
                     fill="url(#defColor_$index)" stroke="${cMap.darker()}" stroke-width="2" 
                     opacity="0.9" filter="url(#dropShadow)" />
                <text x="0" y="-50" text-anchor="middle" style="fill:$fillColor; font-size:13px; font-weight:bold; font-family: Arial, Helvetica, sans-serif;">${points.label}</text>
                <text x="0" y="-30" text-anchor="middle" style="fill:$fillColor; font-size:14px; font-weight:bold; font-family: Arial, Helvetica, sans-serif;">${item.y}</text>
                <text x="0" y="-15" text-anchor="middle" style="fill:$fillColor; font-size:12px; font-family: Arial, Helvetica, sans-serif;">${item.label}</text>
                </g>
            """.trimIndent())

            num += xGap
            num2 += yGap
        }

        // Add the area fill under the line first (so it's behind the line)
        if (points.points.size > 1) {
            if(lineChart.display.smoothLines) {
                // For smooth lines, create a closed path for the area
                val ry = str.split(" ")
                val ary = mutableListOf<io.docops.docopsextensionssupport.svgsupport.Point>()

                ry.forEach { el ->
                    val items = el.split(",")
                    if (items.isNotEmpty() && items.size == 2) {
                        ary.add(
                            Point(
                                x = items[0].toDouble(),
                                items[1].toDouble()
                            )
                        )
                    }
                }

                val smootherCurve = makePath(ary)

                // Create area path by adding line to bottom and back to start
                val firstPoint = ary.firstOrNull()
                val lastPoint = ary.lastOrNull()
                if (firstPoint != null && lastPoint != null) {
                    val areaPath = "$smootherCurve L ${lastPoint.x},${maxHeight} L ${firstPoint.x},${maxHeight} Z"
                    sb.append("""<path d="$areaPath" class="chart-area" fill="url(#areaGradient_$index)" opacity="0.5" />""")
                }

                // Add the line with animation
                sb.append("""<path d="$smootherCurve" class="chart-line" style="stroke: url(#defColor_$index); stroke-width: 3; fill: none;">
                    <animate attributeName="stroke-dasharray" from="${smootherCurve.length},${smootherCurve.length}" to="${smootherCurve.length},0" dur="1.5s" fill="freeze" />
                </path>""")
            } else {
                // For straight lines
                // Add area fill
                sb.append("""<polygon points="$xGap,$maxHeight$str $num,$maxHeight" class="chart-area" fill="url(#areaGradient_$index)" opacity="0.5" />""")

                // Add the line with animation
                val lineLength = str.length * 2 // Approximate length for animation
                sb.append("""<polyline points="$str" class="chart-line" style="stroke: url(#defColor_$index); stroke-width: 3; fill: none; stroke-dasharray: $lineLength,$lineLength; stroke-dashoffset: $lineLength">
                    <animate attributeName="stroke-dashoffset" from="$lineLength" to="0" dur="1.5s" fill="freeze" />
                </polyline>""")
            }
        }

        sb.append(elements)
        return sb.toString()
    }

}

fun main() {
    val maker = LineChartMaker(false)
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
        points = mutableListOf(p1, p2,p3),
        display = LineChartDisplay(backgroundColor = "#f5f5f5")
    )
    val svg = maker.makeLineChart(lc)

    val str = Json.encodeToString(lc)
    println(str)
    val outfile2 = File("gen/line.svg")
    outfile2.writeBytes(svg.toByteArray())
}
