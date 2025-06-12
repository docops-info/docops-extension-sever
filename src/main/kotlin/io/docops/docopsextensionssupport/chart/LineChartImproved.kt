package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.svgsupport.addSvgMetadata
import java.util.UUID

class LineChartImproved {

    // Modern color palette for line chart
    private val defaultColors = listOf(
        "#3498db", // Blue
        "#2ecc71", // Green
        "#e74c3c", // Red
        "#f39c12", // Orange
        "#9b59b6", // Purple
        "#1abc9c", // Turquoise
        "#34495e", // Dark Blue
        "#e67e22", // Dark Orange
        "#27ae60", // Dark Green
        "#d35400"  // Burnt Orange
    )
    fun makeLineSvg(payload: String): String {

        // Parse configuration and data from content
        val (config, chartData) = parseConfigAndData(payload)
        // Parse colors from config or attributes
        val configColors = config["colors"]?.split(",")?.map { it.trim() }
        val customColors = configColors

        val title = config.getOrDefault("title", "Line Chart")
        val width = config.getOrDefault("width", "500")
        val height = config.getOrDefault("height", "500")
        val showLegend = config["legend"]?.toBoolean() ?: true
        val enableHoverEffects = config["hover"]?.toBoolean() ?: true
        val smoothLines = config["smooth"]?.toBoolean() ?: true
        val showPoints = config["points"]?.toBoolean() ?: true
        val showGrid = config["grid"]?.toBoolean() ?: true
        val xAxisLabel = config.getOrDefault("xAxisLabel", "")
        val yAxisLabel = config.getOrDefault("yAxisLabel", "")
        val darkMode = config["darkMode"]?.toBoolean() ?: false

        val lineData = parseLineChartData(chartData)


        // Generate SVG
        val svg = generateLineChartSvg(
            lineData,
            title,
            width.toInt(),
            height.toInt(),
            showLegend,
            customColors ?: defaultColors,
            enableHoverEffects,
            smoothLines,
            showPoints,
            showGrid,
            xAxisLabel,
            yAxisLabel,
            darkMode
        )
        return addSvgMetadata(svg)
    }

    /**
     * Parses the content to extract configuration parameters and chart data.
     * Configuration parameters are specified at the beginning of the content in the format "key=value",
     * followed by a separator line "---", and then the actual chart data.
     *
     * @param content The full content of the block
     * @return A Pair containing the configuration map and the chart data string
     */
    private fun parseConfigAndData(content: String): Pair<Map<String, String>, String> {
        val lines = content.lines()
        val config = mutableMapOf<String, String>()
        var separatorIndex = -1

        // Find the separator line and parse configuration
        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line == "---") {
                separatorIndex = i
                break
            }

            // Parse key=value pairs
            val keyValuePair = line.split("=", limit = 2)
            if (keyValuePair.size == 2) {
                val key = keyValuePair[0].trim()
                val value = keyValuePair[1].trim()
                if (key.isNotEmpty()) {
                    config[key] = value
                }
            }
        }

        // Extract chart data
        val chartData = if (separatorIndex >= 0) {
            lines.subList(separatorIndex + 1, lines.size).joinToString("\n")
        } else {
            // If no separator is found, assume the entire content is chart data
            content
        }

        return Pair(config, chartData)
    }

    private fun parseLineChartData(content: String): List<LineDataSeries> {
        val seriesMap = mutableMapOf<String, MutableList<DataPoint>>()

        content.lines().forEach { line ->
            if (line.isNotBlank()) {
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 3) {
                    val seriesName = parts[0]
                    val xLabel = parts[1].trim()
                    val x = parts[1].toDoubleOrNull() ?: seriesMap[seriesName]?.size?.toDouble() ?: 0.0
                    val y = parts[2].toDoubleOrNull() ?: 0.0
                    val color = if (parts.size > 3 && parts[3].isNotBlank()) parts[3] else null

                    if (!seriesMap.containsKey(seriesName)) {
                        seriesMap[seriesName] = mutableListOf()
                    }

                    seriesMap[seriesName]?.add(DataPoint(x, y, null, xLabel))

                    // If this is the first point in the series and it has a color, store it
                    if (seriesMap[seriesName]?.size == 1 && color != null) {
                        seriesMap[seriesName]?.get(0)?.color = color
                    }
                }
            }
        }

        // Convert the map to a list of LineDataSeries
        return seriesMap.map { (name, points) ->
            // Use the color from the first point if available
            val seriesColor = points.firstOrNull()?.color
            LineDataSeries(name, points.sortedBy { it.x }, seriesColor)
        }
    }

    private fun generateLineChartSvg(
        seriesList: List<LineDataSeries>,
        title: String,
        width: Int,
        height: Int,
        showLegend: Boolean,
        colors: List<String>,
        enableHoverEffects: Boolean,
        smoothLines: Boolean,
        showPoints: Boolean,
        showGrid: Boolean,
        xAxisLabel: String,
        yAxisLabel: String,
        darkMode: Boolean = false
    ): String {
        if (seriesList.isEmpty()) {
            return "<svg width='$width' height='$height'><text x='${width/2}' y='${height/2}' text-anchor='middle'>No data</text></svg>"
        }

        // Calculate chart dimensions and margins
        val margin = 50
        val legendWidth = if (showLegend) 150 else 0
        val chartWidth = width - margin * 2 - legendWidth
        val chartHeight = height - margin * 2

        // Find min and max values for x and y axes
        val allPoints = seriesList.flatMap { it.points }
        val minX = allPoints.minByOrNull { it.x }?.x ?: 0.0
        val maxX = allPoints.maxByOrNull { it.x }?.x ?: 10.0
        val minY = allPoints.minByOrNull { it.y }?.y ?: 0.0
        val maxY = allPoints.maxByOrNull { it.y }?.y ?: 10.0

        // Add some padding to the min/max values
        val xRange = maxX - minX
        val yRange = maxY - minY
        val paddedMinX = if (xRange > 0) minX - xRange * 0.05 else minX - 0.5
        val paddedMaxX = if (xRange > 0) maxX + xRange * 0.05 else maxX + 0.5
        val paddedMinY = if (yRange > 0) minY - yRange * 0.05 else minY - 0.5
        val paddedMaxY = if (yRange > 0) maxY + yRange * 0.05 else maxY + 0.5

        val svgBuilder = StringBuilder()

        // Define colors based on dark mode
        val backgroundColor = if (darkMode) "#1e293b" else "transparent"
        val textColor = if (darkMode) "#f8fafc" else "#000000"
        val gridColor = if (darkMode) "#334155" else "#e0e0e0"
        val axisColor = if (darkMode) "#cbd5e1" else "#000000"
        val pointStrokeColor = if (darkMode) "#1e293b" else "white"

        val id = UUID.randomUUID().toString()
        svgBuilder.append("<svg width='$width' height='$height' xmlns='http://www.w3.org/2000/svg' id='ID_$id' preserveAspectRatio=\"xMidYMid meet\" viewBox=\"0 0 $width $height\">")

        // Add background if in dark mode
        if (darkMode) {
            svgBuilder.append("<rect width='$width' height='$height' fill='$backgroundColor' />")
        }

        // Add CSS styles for hover effects if enabled
        if (enableHoverEffects) {
            svgBuilder.append("""
                <style>
                    .line-path {
                        transition: stroke-width 0.2s;
                    }
                    .line-path:hover {
                        stroke-width: 4;
                    }
                    .data-point {
                        transition: r 0.2s;
                    }
                    .data-point:hover {
                        r: 8;
                        cursor: pointer;
                    }
                    .legend-item:hover {
                        cursor: pointer;
                    }
                    .legend-item:hover rect {
                        stroke-width: 2;
                    }
                    .legend-item:hover text {
                        font-weight: bold;
                    }
                </style>
            """.trimIndent())
        }

        // Add title
        svgBuilder.append("<text x='${width/2}' y='25' font-family='Arial' font-size='20' text-anchor='middle' font-weight='bold' fill='$textColor'>$title</text>")

        // Define chart area
        val chartX = margin
        val chartY = margin + 10 // Add space for title

        // Function to convert data coordinates to SVG coordinates
        fun xToSvg(x: Double): Double = chartX + (x - paddedMinX) * chartWidth / (paddedMaxX - paddedMinX)
        fun yToSvg(y: Double): Double = chartY + chartHeight - (y - paddedMinY) * chartHeight / (paddedMaxY - paddedMinY)

        // Draw grid if enabled
        if (showGrid) {
            svgBuilder.append("<g class='grid' stroke='$gridColor' stroke-width='1'>")

            // Horizontal grid lines (y-axis)
            val yStep = calculateAxisStep(paddedMinY, paddedMaxY)
            var y = Math.ceil(paddedMinY / yStep) * yStep
            while (y <= paddedMaxY) {
                val yPos = yToSvg(y)
                svgBuilder.append("<line x1='$chartX' y1='$yPos' x2='${chartX + chartWidth}' y2='$yPos' />")
                y += yStep
            }

            // Vertical grid lines (x-axis)
            val xStep = calculateAxisStep(paddedMinX, paddedMaxX)
            var x = Math.ceil(paddedMinX / xStep) * xStep
            while (x <= paddedMaxX) {
                val xPos = xToSvg(x)
                svgBuilder.append("<line x1='$xPos' y1='$chartY' x2='$xPos' y2='${chartY + chartHeight}' />")
                x += xStep
            }

            svgBuilder.append("</g>")
        }

        // Draw axes
        svgBuilder.append("<g class='axes' stroke='$axisColor' stroke-width='1'>")
        // X-axis
        svgBuilder.append("<line x1='$chartX' y1='${chartY + chartHeight}' x2='${chartX + chartWidth}' y2='${chartY + chartHeight}' />")
        // Y-axis
        svgBuilder.append("<line x1='$chartX' y1='$chartY' x2='$chartX' y2='${chartY + chartHeight}' />")

        // X-axis ticks and labels
        // Check if we have string labels
        val hasStringLabels = seriesList.flatMap { it.points }.any { it.xLabel != null }

        if (hasStringLabels) {
            // Get unique x-values and their labels
            val uniqueXPoints = seriesList.flatMap { it.points }
                .distinctBy { it.x }
                .sortedBy { it.x }

            // Draw ticks and labels for each unique x-value
            uniqueXPoints.forEach { point ->
                val xPos = xToSvg(point.x)
                svgBuilder.append("<line x1='$xPos' y1='${chartY + chartHeight}' x2='$xPos' y2='${chartY + chartHeight + 5}' />")
                svgBuilder.append("<text x='$xPos' y='${chartY + chartHeight + 20}' font-family='Arial' font-size='12' text-anchor='middle' fill='$textColor'>${point.xLabel ?: formatNumber(point.x)}</text>")
            }
        } else {
            // Use the original numeric approach
            val xStep = calculateAxisStep(paddedMinX, paddedMaxX)
            var x = Math.ceil(paddedMinX / xStep) * xStep
            while (x <= paddedMaxX) {
                val xPos = xToSvg(x)
                svgBuilder.append("<line x1='$xPos' y1='${chartY + chartHeight}' x2='$xPos' y2='${chartY + chartHeight + 5}' />")
                svgBuilder.append("<text x='$xPos' y='${chartY + chartHeight + 20}' font-family='Arial' font-size='12' text-anchor='middle' fill='$textColor'>${formatNumber(x)}</text>")
                x += xStep
            }
        }

        // Y-axis ticks and labels
        val yStep = calculateAxisStep(paddedMinY, paddedMaxY)
        var y = Math.ceil(paddedMinY / yStep) * yStep
        while (y <= paddedMaxY) {
            val yPos = yToSvg(y)
            svgBuilder.append("<line x1='$chartX' y1='$yPos' x2='${chartX - 5}' y2='$yPos' />")
            svgBuilder.append("<text x='${chartX - 10}' y='$yPos' font-family='Arial' font-size='12' text-anchor='end' dominant-baseline='middle' fill='$textColor'>${formatNumber(y)}</text>")
            y += yStep
        }

        // Add axis labels if provided
        if (xAxisLabel.isNotEmpty()) {
            svgBuilder.append("<text x='${chartX + chartWidth / 2}' y='${chartY + chartHeight + 40}' font-family='Arial' font-size='14' text-anchor='middle' fill='$textColor'>$xAxisLabel</text>")
        }
        if (yAxisLabel.isNotEmpty()) {
            svgBuilder.append("<text x='${chartX - 40}' y='${chartY + chartHeight / 2}' font-family='Arial' font-size='14' text-anchor='middle' transform='rotate(-90, ${chartX - 40}, ${chartY + chartHeight / 2})' fill='$textColor'>$yAxisLabel</text>")
        }

        svgBuilder.append("</g>")

        // Draw data series
        seriesList.forEachIndexed { index, series ->
            if (series.points.isEmpty()) return@forEachIndexed

            // Choose color for this series
            val colorIndex = index % colors.size
            val color = series.color ?: colors[colorIndex]

            // Create a group for this series
            svgBuilder.append("<g class='data-series' id='series-$index'>")

            // Draw the line
            svgBuilder.append("<path class='line-path' ")
            if (enableHoverEffects) {
                svgBuilder.append("id='line-$index' ")
            }
            svgBuilder.append("d='")

            // Sort points by x value to ensure proper line drawing
            val sortedPoints = series.points.sortedBy { it.x }

            // Move to the first point
            val firstX = xToSvg(sortedPoints[0].x)
            val firstY = yToSvg(sortedPoints[0].y)
            svgBuilder.append("M$firstX,$firstY ")

            if (smoothLines) {
                // Create a smooth curve using cubic bezier curves
                for (i in 1 until sortedPoints.size) {
                    val x1 = xToSvg(sortedPoints[i-1].x)
                    val y1 = yToSvg(sortedPoints[i-1].y)
                    val x2 = xToSvg(sortedPoints[i].x)
                    val y2 = yToSvg(sortedPoints[i].y)

                    // Calculate control points for smooth curve
                    val controlX1 = x1 + (x2 - x1) / 3
                    val controlY1 = y1
                    val controlX2 = x2 - (x2 - x1) / 3
                    val controlY2 = y2

                    svgBuilder.append("C$controlX1,$controlY1 $controlX2,$controlY2 $x2,$y2 ")
                }
            } else {
                // Create straight lines between points
                for (i in 1 until sortedPoints.size) {
                    val x = xToSvg(sortedPoints[i].x)
                    val y = yToSvg(sortedPoints[i].y)
                    svgBuilder.append("L$x,$y ")
                }
            }

            svgBuilder.append("' fill='none' stroke='$color' stroke-width='2' />")

            // Draw data points if enabled
            if (showPoints) {
                sortedPoints.forEachIndexed { pointIndex, point ->
                    val cx = xToSvg(point.x)
                    val cy = yToSvg(point.y)
                    svgBuilder.append("<circle class='data-point' cx='$cx' cy='$cy' r='5' fill='$color' stroke='$pointStrokeColor' stroke-width='1'>")
                    val xDisplay = point.xLabel ?: formatNumber(point.x)
                    svgBuilder.append("<title>${series.name}: ($xDisplay, ${formatNumber(point.y)})</title>")
                    svgBuilder.append("</circle>")
                }
            }

            svgBuilder.append("</g>")
        }

        // Add legend if enabled
        if (showLegend) {
            val legendX = chartX + chartWidth + 20
            val legendY = chartY + 20

            svgBuilder.append("<g class='legend'>")

            seriesList.forEachIndexed { index, series ->
                val yPos = legendY + index * 25
                val colorIndex = index % colors.size
                val color = series.color ?: colors[colorIndex]

                // Wrap each legend item in a group for hover effects if enabled
                if (enableHoverEffects) {
                    svgBuilder.append("<g class='legend-item' data-series='series-$index' onmouseover='highlightSeries(\"line-$index\")' onmouseout='resetSeries(\"line-$index\")'>");
                } else {
                    svgBuilder.append("<g>");
                }

                // Legend color line
                svgBuilder.append("<line x1='${legendX - 15}' y1='$yPos' x2='${legendX}' y2='$yPos' stroke='$color' stroke-width='2' />")

                // Legend point if showing points
                if (showPoints) {
                    svgBuilder.append("<circle cx='${legendX - 7}' cy='$yPos' r='4' fill='$color' stroke='$pointStrokeColor' stroke-width='1' />")
                }

                // Legend text
                svgBuilder.append("<text x='${legendX + 10}' y='$yPos' font-family='Arial' font-size='12' dominant-baseline='middle' fill='$textColor'>${series.name}</text>")

                svgBuilder.append("</g>")
            }

            svgBuilder.append("</g>")
        }

        // Add JavaScript for interactive effects if enabled
        if (enableHoverEffects) {
            svgBuilder.append("""
                <script type="text/javascript">
                    function highlightSeries(id) {
                        var line = document.getElementById(id);
                        if (line) {
                            line.setAttribute('stroke-width', '4');
                        }
                    }

                    function resetSeries(id) {
                        var line = document.getElementById(id);
                        if (line) {
                            line.setAttribute('stroke-width', '2');
                        }
                    }
                </script>
            """.trimIndent())
        }

        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }

    private fun calculateAxisStep(min: Double, max: Double): Double {
        val range = max - min
        val magnitude = Math.pow(10.0, Math.floor(Math.log10(range)))

        return when {
            range / magnitude < 2 -> magnitude / 5
            range / magnitude < 5 -> magnitude / 2
            else -> magnitude
        }
    }

    private fun formatNumber(value: Double): String {
        return if (value == value.toInt().toDouble()) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }

    private data class DataPoint(val x: Double, val y: Double, var color: String? = null, val xLabel: String? = null)

    private data class LineDataSeries(val name: String, val points: List<DataPoint>, val color: String? = null)
}