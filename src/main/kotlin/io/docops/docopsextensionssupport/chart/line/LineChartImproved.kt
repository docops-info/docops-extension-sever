package io.docops.docopsextensionssupport.chart.line

import io.docops.docopsextensionssupport.chart.ChartColors
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.util.ParsingUtils
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.update
import java.util.UUID

class LineChartImproved {


    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)
    fun makeLineSvg(payload: String, csvResponse: CsvResponse, useDark: Boolean): String {

        // Parse configuration and data from content
        val (config, chartData) = parseConfigAndData(payload)
        val visualVersion = config["visualVersion"]?.toIntOrNull() ?: 1

        val display = LineChartDisplay(
            useDark = useDark,
            visualVersion = visualVersion,
            smoothLines = config["smooth"]?.toBoolean() ?: true,
            showArea = config["area"]?.toBoolean() ?: false,
            theme = config["theme"] ?: "classic"
        )
        theme = ThemeFactory.getThemeByName(display.theme)

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
        val darkMode = useDark

        val lineData = parseLineChartData(chartData)

        csvResponse.update(convertLineDataSeriesToCsv(lineData))

        var colors = theme.chartPalette
        if (customColors != null) {
            colors = mutableListOf()
            customColors.forEach {
                colors.add(SVGColor(it))
            }
        }
        // Generate SVG
        val svg = generateLineChartSvg(
            lineData,
            title,
            width.toInt(),
            height.toInt(),
            showLegend,
            colors,
            enableHoverEffects,
            smoothLines,
            showPoints,
            showGrid,
            xAxisLabel,
            yAxisLabel,
            theme, display
        )
        return svg
    }

    /**
     * Parses the content to extract configuration parameters and chart data.
     * Uses the shared ParsingUtils for consistent parsing across the application.
     *
     * @param content The full content of the block
     * @return A Pair containing the configuration map and the chart data string
     */
    private fun parseConfigAndData(content: String): Pair<Map<String, String>, String> {
        return ParsingUtils.parseConfigAndData(content)
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
        colors: List<SVGColor>,
        enableHoverEffects: Boolean,
        smoothLines: Boolean,
        showPoints: Boolean,
        showGrid: Boolean,
        xAxisLabel: String,
        yAxisLabel: String,
        theme: DocOpsTheme,
        display: LineChartDisplay
    ): String {
        val darkMode = theme.canvas != "#ffffff"
        if (seriesList.isEmpty()) {
            return "<svg width='$width' height='$height'><text x='${width/2}' y='${height/2}' text-anchor='middle'>No data</text></svg>"
        }

        // Define asymmetrical margins to prevent label clipping
        val marginLeft = 85
        val marginRight = if (showLegend) 220 else 40
        val marginTop = 70
        val marginBottom = 80

        val chartWidth = width - marginLeft - marginRight
        val chartHeight = height - marginTop - marginBottom



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

        // Define colors based on dark mode (Midnight IDE aesthetic)
        // Define colors based on ThemeFactory
        val backgroundColor = theme.canvas
        val textColor = theme.primaryText
        val gridColor = theme.accentColor
        val axisColor = theme.accentColor
        val pointStrokeColor = theme.canvas
        val legendBg = theme.glassEffect


        val id = display.id
        svgBuilder.append("<svg width='$width' height='$height' xmlns='http://www.w3.org/2000/svg' id='ID_$id' preserveAspectRatio=\"xMidYMid meet\" viewBox=\"0 0 $width $height\">")


        svgBuilder.append("<defs>")
        svgBuilder.append(theme.fontImport)
        svgBuilder.append("""
                <filter id="legendGlass_$id" x="0" y="0" width="100%" height="100%">
                    <feGaussianBlur in="SourceGraphic" stdDeviation="2" />
                </filter>
            """.trimIndent())

        // Layered Gradient for Atmosphere
        if (darkMode) {
            svgBuilder.append("""
                    <radialGradient id="bgGlow_$id" cx="50%" cy="50%" r="50%" fx="50%" fy="50%">
                        <stop offset="0%" style="stop-color:#1e293b;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#020617;stop-opacity:1" />
                    </radialGradient>
                """.trimIndent())
        }
        // Atmospheric Patterns and Glow Filter
        svgBuilder.append("""
                <pattern id="dotPattern_$id" x="0" y="0" width="30" height="30" patternUnits="userSpaceOnUse">
                    <circle cx="2" cy="2" r="1" fill="${if (darkMode) "#334155" else "#cbd5e1"}" fill-opacity="0.4" />
                </pattern>
                <filter id="glow_$id" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur stdDeviation="4" result="blur" />
                    <feComposite in="SourceGraphic" in2="blur" operator="over" />
                </filter>
            """.trimIndent())
        // Add responsive CSS that adapts to system color scheme
        val chartStyle = """
                #ID_$id .chart-text { fill: ${theme.primaryText}; font-family: ${theme.fontFamily}; letter-spacing: -0.5px; }
                #ID_$id .chart-grid { stroke: ${theme.accentColor}; stroke-dasharray: 3,3; stroke-opacity: 0.2; }
                #ID_$id .chart-axis { stroke: ${theme.accentColor}; stroke-width: 1.5; stroke-opacity: 0.5; }
                #ID_$id .chart-background { fill: ${theme.canvas}; }
                #ID_$id .legend-box { fill: ${theme.glassEffect}; stroke: ${theme.accentColor}; stroke-width: 1; }
            """.trimIndent()

        svgBuilder.append("""
        <style>
            <![CDATA[
            $chartStyle
             @keyframes revealPoint {
                from { r: 0; opacity: 0; }
                to { r: 5; opacity: 1; }
            }
            
            .data-point-reveal {
                opacity: 0;
                animation: revealPoint 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
            }
            /* Hover effects */
            ${if (enableHoverEffects) """
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
            """ else ""}
            ]]>
        </style>
    """.trimIndent())

        svgBuilder.append("</defs>")

        // ENFORCE BACKGROUND: Full coverage rect ensures no blending into page
        svgBuilder.append("<rect width='$width' height='$height' class='chart-background' rx='12'/>")
        svgBuilder.append("<rect width='$width' height='$height' fill='url(#dotPattern_$id)' rx='12' pointer-events='none'/>")

        // Add title with distinct typography
        svgBuilder.append("<text x='${width/2}' y='30' font-size='22' text-anchor='middle' font-weight='800' class='chart-text' style='text-transform: uppercase; letter-spacing: 1px;'>$title</text>")


        // Define chart area
        // Define chart area
        val chartX = marginLeft
        val chartY = marginTop

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
        svgBuilder.append("<g class='axes'>")
        //svgBuilder.append("<g class='axes' stroke='$axisColor' stroke-width='1'>")
        // X-axis
        svgBuilder.append("<line x1='$chartX' y1='${chartY + chartHeight}' x2='${chartX + chartWidth}' y2='${chartY + chartHeight}' stroke='$axisColor' stroke-width='1' />")
        // Y-axis
        svgBuilder.append("<line x1='$chartX' y1='$chartY' x2='$chartX' y2='${chartY + chartHeight}' stroke='$axisColor' stroke-width='1' />")

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
                svgBuilder.append("<line x1='$xPos' y1='${chartY + chartHeight}' x2='$xPos' y2='${chartY + chartHeight + 5}' class='chart-axis' />")
                svgBuilder.append("<text x='$xPos' y='${chartY + chartHeight + 20}' font-size='11' text-anchor='middle' class='chart-text'>${point.xLabel ?: formatNumber(point.x)}</text>")
            }
        } else {
            // Use the original numeric approach
            val xStep = calculateAxisStep(paddedMinX, paddedMaxX)
            var x = Math.ceil(paddedMinX / xStep) * xStep
            while (x <= paddedMaxX) {
                val xPos = xToSvg(x)
                svgBuilder.append("<line x1='$xPos' y1='${chartY + chartHeight}' x2='$xPos' y2='${chartY + chartHeight + 5}' class='chart-axis' />")
                svgBuilder.append("<text x='$xPos' y='${chartY + chartHeight + 20}' font-size='11' text-anchor='middle' class='chart-text'>${formatNumber(x)}</text>")
                x += xStep
            }
        }

        val yStep = calculateAxisStep(paddedMinY, paddedMaxY)
        var y = Math.ceil(paddedMinY / yStep) * yStep
        while (y <= paddedMaxY) {
            val yPos = yToSvg(y)
            svgBuilder.append("<line x1='$chartX' y1='$yPos' x2='${chartX - 5}' y2='$yPos' class='chart-axis' />")
            svgBuilder.append("<text x='${chartX - 10}' y='$yPos' font-size='11' text-anchor='end' dominant-baseline='middle' class='chart-text'>${formatNumber(y)}</text>")
            y += yStep
        }

        // Add axis labels if provided
        if (xAxisLabel.isNotEmpty()) {
            val xLabelY = chartY + chartHeight + 55
            svgBuilder.append("<text x='${chartX + chartWidth / 2}' y='$xLabelY' font-size='14' text-anchor='middle' font-weight='600' class='chart-text'>$xAxisLabel</text>")
        }
        if (yAxisLabel.isNotEmpty()) {
            val yLabelX = chartX - 65
            svgBuilder.append("<text x='$yLabelX' y='${chartY + chartHeight / 2}' font-size='14' text-anchor='middle' font-weight='600' transform='rotate(-90, $yLabelX, ${chartY + chartHeight / 2})' class='chart-text'>$yAxisLabel</text>")
        }

        svgBuilder.append("</g>")

        // Draw data series
        seriesList.forEachIndexed { index, series ->
            if (series.points.isEmpty()) return@forEachIndexed

            // Choose color for this series
            val colorIndex = index % colors.size
            val color = series.color ?: colors[colorIndex].color

            // Create a group for this series with a high-impact glow
            svgBuilder.append("<g class='data-series' id='series-$index' filter='url(#glow_$id)'>")

            // Draw the line with sharp, vibrant stroke
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

            svgBuilder.append("' fill='none' stroke='$color' stroke-width='4' stroke-linecap='round' stroke-linejoin='round' />")


            // Draw data points if enabled
            if (showPoints) {
                sortedPoints.forEachIndexed { pointIndex, point ->
                    val cx = xToSvg(point.x)
                    val cy = yToSvg(point.y)
                    val delay = 0.6 + (pointIndex * 0.08)
                    svgBuilder.append("<circle class='data-point data-point-reveal' cx='$cx' cy='$cy' r='6' fill='$color' stroke='$pointStrokeColor' stroke-width='2' style='animation-delay: ${delay}s'>")
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

            // Legend Backdrop Card (Atmosphere & Depth)
            svgBuilder.append("<g class='legend'>")
            // Dynamic height for legend box
            //val legBoxHeight = seriesList.size * 25 + 20
            //svgBuilder.append("<rect x='${legendX - 10}' y='${legendY - 15}' width='${legendWidth - 20}' height='$legBoxHeight' rx='10' class='legend-box' filter='url(#glassDropShadow_$id)' />")


            seriesList.forEachIndexed { index, series ->
                val yPos = legendY + index * 25
                val colorIndex = index % colors.size
                val color = series.color ?: colors[colorIndex].color

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

                // Legend text - Forced contrast and monospace
                svgBuilder.append("<text x='${legendX + 15}' y='$yPos' font-size='11' font-weight='600' dominant-baseline='middle' fill='$color' style='pointer-events: none;'>${series.name}</text>")

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

    /**
     * Converts a List<LineDataSeries> to CSV format
     * @param lineDataSeries The list of line data series to convert
     * @return CsvResponse with headers and rows representing the line data series
     */
    private fun convertLineDataSeriesToCsv(lineDataSeries: List<LineDataSeries>): CsvResponse {
        val headers = mutableListOf<String>()
        val rows = mutableListOf<List<String>>()

        // Create headers
        headers.add("Series")
        headers.add("X")
        headers.add("Y")
        if (lineDataSeries.any { it.color != null }) {
            headers.add("Color")
        }

        // Add data rows
        for (series in lineDataSeries) {
            for (point in series.points) {
                val row = mutableListOf<String>()
                row.add(series.name)
                row.add(point.x.toString())
                row.add(point.y.toString())

                // Add color if the header exists
                if (headers.contains("Color")) {
                    row.add(series.color ?: "")
                }

                rows.add(row)
            }
        }

        return CsvResponse(headers, rows)
    }

    private data class DataPoint(val x: Double, val y: Double, var color: String? = null, val xLabel: String? = null)

    private data class LineDataSeries(val name: String, val points: List<DataPoint>, val color: String? = null)
}

