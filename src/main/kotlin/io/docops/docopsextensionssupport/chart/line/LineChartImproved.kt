package io.docops.docopsextensionssupport.chart.line

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.util.ParsingUtils
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.update


class LineChartImproved {


    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)

    fun makeLineSvg(payload: String, csvResponse: CsvResponse, useDark: Boolean, isPdf: Boolean = false): String {
        // Parse configuration and data from content
        val (config, chartData) = parseConfigAndData(payload)
        val visualVersion = config["visualVersion"]?.toIntOrNull() ?: 1

        // Accept both legacy + documented keys, pick Signal defaults (precise + restrained).
        val smoothLines = config["smoothLines"]?.toBoolean()
            ?: config["smooth"]?.toBoolean()
            ?: false

        val showArea = config["showArea"]?.toBoolean()
            ?: config["area"]?.toBoolean()
            ?: false

        val display = LineChartDisplay(
            useDark = useDark,
            visualVersion = visualVersion,
            smoothLines = smoothLines,
            showArea = showArea,
            theme = config["theme"] ?: "classic"
        )
        theme = ThemeFactory.getThemeByName(display.theme, useDark)

        // Parse colors from config or attributes
        val configColors = config["colors"]?.split(",")?.map { it.trim() }
        val customColors = configColors

        val title = config.getOrDefault("title", "Line Chart")
        val width = config.getOrDefault("width", "500")
        val height = config.getOrDefault("height", "500")
        val showLegend = config["legend"]?.toBoolean() ?: true
        val forceLegend = config["forceLegend"]?.toBoolean() ?: false
        val enableHoverEffects = config["hover"]?.toBoolean() ?: true
        val showPoints = config["points"]?.toBoolean() ?: true
        val showGrid = config["grid"]?.toBoolean() ?: true
        val xAxisLabel = config.getOrDefault("xAxisLabel", "")
        val yAxisLabel = config.getOrDefault("yAxisLabel", "")

        val lineData = parseLineChartData(chartData)
        csvResponse.update(convertLineDataSeriesToCsv(lineData))

        var colors = theme.chartPalette
        if (customColors != null) {
            colors = mutableListOf()
            customColors.forEach { colors.add(SVGColor(it)) }
        }

        // Generate SVG
        return generateLineChartSvg(
            lineData,
            title,
            width.toInt(),
            height.toInt(),
            showLegend,
            forceLegend,
            colors,
            enableHoverEffects,
            smoothLines,
            showPoints,
            showGrid,
            xAxisLabel,
            yAxisLabel,
            theme,
            display,
            isPdf
        )
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
        forceLegend: Boolean,
        colors: List<SVGColor>,
        enableHoverEffects: Boolean,
        smoothLines: Boolean,
        showPoints: Boolean,
        showGrid: Boolean,
        xAxisLabel: String,
        yAxisLabel: String,
        theme: DocOpsTheme,
        display: LineChartDisplay,
        isPdf: Boolean
    ): String {
        val darkMode = theme.canvas.lowercase() != "#ffffff"
        if (seriesList.isEmpty()) {
            return "<svg width='$width' height='$height'><text x='${width / 2}' y='${height / 2}' text-anchor='middle'>No data</text></svg>"
        }

        // Single-series legend suppression by default
        val effectiveShowLegend = showLegend && (forceLegend || seriesList.size > 1)

        // Typography + spacing tokens
        val titleFontSize = 24
        val axisLabelFontSize = 14
        val tickFontSize = 11
        val legendFontSize = 12
        val legendRowHeight = 24
        val legendInnerPadX = 12
        val legendInnerPadY = 10
        val legendMarkerWidth = 16
        val legendGap = 10
        val legendTextMaxChars = 26

        val legendLabels = if (effectiveShowLegend) {
            seriesList.map { truncateWithEllipsis(it.name, legendTextMaxChars) }
        } else {
            emptyList()
        }
        val maxLegendChars = legendLabels.maxOfOrNull { it.length } ?: 0
        val estimatedLegendTextWidth = maxLegendChars * legendFontSize * 0.60
        val computedLegendBoxWidth = legendInnerPadX * 2 + legendMarkerWidth + legendGap + estimatedLegendTextWidth
        val adaptiveLegendMargin = (computedLegendBoxWidth + 36).toInt()
        val maxAllowedLegendMargin = (width * 0.42).toInt()

        val marginLeft = 85
        val marginRight = if (effectiveShowLegend) {
            kotlin.math.max(140, kotlin.math.min(adaptiveLegendMargin, maxAllowedLegendMargin))
        } else {
            40
        }
        val marginTop = 70
        val marginBottom = 80

        val chartWidth = kotlin.math.max(120, width - marginLeft - marginRight)
        val chartHeight = kotlin.math.max(120, height - marginTop - marginBottom)

        val allPoints = seriesList.flatMap { it.points }
        val minX = allPoints.minByOrNull { it.x }?.x ?: 0.0
        val maxX = allPoints.maxByOrNull { it.x }?.x ?: 10.0
        val minY = allPoints.minByOrNull { it.y }?.y ?: 0.0
        val maxY = allPoints.maxByOrNull { it.y }?.y ?: 10.0

        val xRange = maxX - minX
        val yRange = maxY - minY
        val paddedMinX = if (xRange > 0) minX - xRange * 0.05 else minX - 0.5
        val paddedMaxX = if (xRange > 0) maxX + xRange * 0.05 else maxX + 0.5
        val paddedMinY = if (yRange > 0) minY - yRange * 0.05 else minY - 0.5
        val paddedMaxY = if (yRange > 0) maxY + yRange * 0.05 else maxY + 0.5

        val svgBuilder = StringBuilder()

        val backgroundColor = theme.canvas
        val textColor = theme.primaryText
        val secondaryTextColor = theme.secondaryText
        val gridColor = theme.primaryText
        val axisColor = theme.primaryText
        val pointStrokeColor = theme.canvas

        val id = display.id
        svgBuilder.append("<svg width='$width' height='$height' xmlns='http://www.w3.org/2000/svg' id='ID_$id' preserveAspectRatio='xMidYMid meet' viewBox='0 0 $width $height'>")
        svgBuilder.append("<defs>")
        svgBuilder.append(
            """
        <linearGradient id="${id}_plotSurface" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="${if (darkMode) "rgba(255,255,255,0.06)" else "rgba(255,255,255,0.72)"}"/>
            <stop offset="100%" stop-color="${if (darkMode) "rgba(255,255,255,0.02)" else "rgba(255,255,255,0.42)"}"/>
        </linearGradient>
        """.trimIndent()
        )

        val chartStyle = """
        #ID_$id .chart-text { font-family: ${theme.fontFamily}; fill: $textColor; letter-spacing: 0; }
        #ID_$id .chart-background { fill: $backgroundColor; }
        #ID_$id .plot-surface {
            fill: url(#${id}_plotSurface);
            stroke: $axisColor;
            stroke-opacity: ${if (darkMode) "0.16" else "0.10"};
            stroke-width: 1;
        }
        #ID_$id .chart-grid { stroke: $gridColor; stroke-dasharray: 3,3; stroke-opacity: ${if (darkMode) "0.14" else "0.12"}; }
        #ID_$id .chart-axis { stroke: $axisColor; stroke-width: 1.25; stroke-opacity: ${if (darkMode) "0.45" else "0.35"}; }

        #ID_$id .chart-title { fill: $textColor; font-size: ${titleFontSize}px; font-weight: 700; letter-spacing: 0; }
        #ID_$id .axis-label { fill: $textColor; font-size: ${axisLabelFontSize}px; font-weight: 600; letter-spacing: 0; }
        #ID_$id .tick-label { fill: $secondaryTextColor; font-size: ${tickFontSize}px; font-weight: 500; opacity: ${if (darkMode) "0.82" else "0.78"}; }

        #ID_$id .legend-box { fill: ${theme.glassEffect}; stroke: $axisColor; stroke-width: 1; stroke-opacity: 0.22; }
        #ID_$id .legend-label { fill: $textColor; font-size: ${legendFontSize}px; font-weight: 600; }

        #ID_$id .line-reveal { opacity: ${if (isPdf) "1" else "0"}; animation: ${if (isPdf) "none" else "lineReveal_$id 520ms cubic-bezier(.2,.85,.2,1) forwards"}; }
        #ID_$id .point-reveal { opacity: ${if (isPdf) "1" else "0"}; animation: ${if (isPdf) "none" else "pointReveal_$id 320ms cubic-bezier(.2,.85,.2,1) forwards"}; }

        @keyframes lineReveal_$id {
            from { opacity: 0; stroke-dashoffset: 1; }
            to { opacity: 1; stroke-dashoffset: 0; }
        }
        @keyframes pointReveal_$id {
            from { opacity: 0; }
            to { opacity: 1; }
        }
    """.trimIndent()

        svgBuilder.append(
            """
        <style>
            <![CDATA[
            ${theme.fontImport}
            $chartStyle

            ${if (enableHoverEffects) """
            #ID_$id .plot .data-series { opacity: 0.95; transition: opacity 140ms cubic-bezier(0.16, 1, 0.3, 1); }
            #ID_$id .plot:hover .data-series { opacity: 0.32; }
            #ID_$id .plot:hover .data-series:hover { opacity: 1; }

            #ID_$id .line-path { transition: stroke-width 140ms cubic-bezier(0.16, 1, 0.3, 1); }
            #ID_$id .data-point { transition: r 140ms cubic-bezier(0.16, 1, 0.3, 1); }

            #ID_$id .data-series:hover .line-path,
            #ID_$id .data-series:focus-visible .line-path { stroke-width: 5; }

            #ID_$id .data-series:hover .data-point,
            #ID_$id .data-series:focus-visible .data-point { r: 7; }

            #ID_$id .legend-item:hover { opacity: 0.92; }
            """ else ""}

            @media (prefers-reduced-motion: reduce) {
                #ID_$id * {
                    transition: none !important;
                    animation: none !important;
                }
            }
            ]]>
        </style>
        """.trimIndent()
        )
        svgBuilder.append("</defs>")

        svgBuilder.append("<rect width='$width' height='$height' class='chart-background' rx='12'/>")
        svgBuilder.append("<text x='${width / 2}' y='38' text-anchor='middle' class='chart-text chart-title'>${escapeXml(title)}</text>")

        val chartX = marginLeft
        val chartY = marginTop

        fun xToSvg(x: Double): Double = chartX + (x - paddedMinX) * chartWidth / (paddedMaxX - paddedMinX)
        fun yToSvg(y: Double): Double = chartY + chartHeight - (y - paddedMinY) * chartHeight / (paddedMaxY - paddedMinY)

        // Subtle plot depth panel
        svgBuilder.append("<rect x='$chartX' y='$chartY' width='$chartWidth' height='$chartHeight' rx='10' class='plot-surface'/>")

        if (showGrid) {
            svgBuilder.append("<g class='chart-grid'>")
            val yStep = calculateAxisStep(paddedMinY, paddedMaxY)
            var y = Math.ceil(paddedMinY / yStep) * yStep
            while (y <= paddedMaxY) {
                val yPos = yToSvg(y)
                svgBuilder.append("<line x1='$chartX' y1='$yPos' x2='${chartX + chartWidth}' y2='$yPos' />")
                y += yStep
            }

            val xStep = calculateAxisStep(paddedMinX, paddedMaxX)
            var x = Math.ceil(paddedMinX / xStep) * xStep
            while (x <= paddedMaxX) {
                val xPos = xToSvg(x)
                svgBuilder.append("<line x1='$xPos' y1='$chartY' x2='$xPos' y2='${chartY + chartHeight}' />")
                x += xStep
            }
            svgBuilder.append("</g>")
        }

        svgBuilder.append("<g class='axes'>")
        svgBuilder.append("<line x1='$chartX' y1='${chartY + chartHeight}' x2='${chartX + chartWidth}' y2='${chartY + chartHeight}' class='chart-axis' />")
        svgBuilder.append("<line x1='$chartX' y1='$chartY' x2='$chartX' y2='${chartY + chartHeight}' class='chart-axis' />")

        // X-axis ticks and labels
        val hasStringLabels = seriesList.flatMap { it.points }.any { it.xLabel != null }

        if (hasStringLabels) {
            val uniqueXPoints = seriesList.flatMap { it.points }
                .distinctBy { it.x }
                .sortedBy { it.x }

            // Adaptive dense label handling: skip + rotate + stagger
            val labelCount = uniqueXPoints.size
            val idealPxPerLabel = 72.0
            val minPxPerLabelForNoSkip = 48.0
            val pxPerLabel = if (labelCount > 0) chartWidth.toDouble() / labelCount else chartWidth.toDouble()

            val skipEvery = when {
                pxPerLabel >= idealPxPerLabel -> 1
                pxPerLabel >= minPxPerLabelForNoSkip -> 2
                else -> kotlin.math.ceil(minPxPerLabelForNoSkip / pxPerLabel).toInt().coerceAtLeast(2)
            }

            val rotateLabels = pxPerLabel < 56.0
            val baseY = chartY + chartHeight + 20

            uniqueXPoints.forEachIndexed { i, point ->
                if (i % skipEvery != 0) return@forEachIndexed

                val xPos = xToSvg(point.x)
                svgBuilder.append("<line x1='$xPos' y1='${chartY + chartHeight}' x2='$xPos' y2='${chartY + chartHeight + 5}' class='chart-axis' />")

                val label = point.xLabel ?: formatNumber(point.x)
                if (rotateLabels) {
                    val textY = baseY + 10
                    svgBuilder.append("<text x='$xPos' y='$textY' text-anchor='end' transform='rotate(-38, $xPos, $textY)' class='chart-text tick-label'>${escapeXml(label)}<title>${escapeXml(label)}</title></text>")
                } else {
                    val staggerY = if (labelCount > 10 && i % 2 == 1) baseY + 12 else baseY
                    svgBuilder.append("<text x='$xPos' y='$staggerY' text-anchor='middle' class='chart-text tick-label'>${escapeXml(label)}<title>${escapeXml(label)}</title></text>")
                }
            }
        } else {
            val xStep = calculateAxisStep(paddedMinX, paddedMaxX)
            var x = Math.ceil(paddedMinX / xStep) * xStep
            while (x <= paddedMaxX) {
                val xPos = xToSvg(x)
                svgBuilder.append("<line x1='$xPos' y1='${chartY + chartHeight}' x2='$xPos' y2='${chartY + chartHeight + 5}' class='chart-axis' />")
                svgBuilder.append("<text x='$xPos' y='${chartY + chartHeight + 20}' text-anchor='middle' class='chart-text tick-label'>${formatNumber(x)}</text>")
                x += xStep
            }
        }

        val yStep = calculateAxisStep(paddedMinY, paddedMaxY)
        var y = Math.ceil(paddedMinY / yStep) * yStep
        while (y <= paddedMaxY) {
            val yPos = yToSvg(y)
            svgBuilder.append("<line x1='$chartX' y1='$yPos' x2='${chartX - 5}' y2='$yPos' class='chart-axis' />")
            svgBuilder.append("<text x='${chartX - 10}' y='$yPos' text-anchor='end' dominant-baseline='middle' class='chart-text tick-label'>${formatNumber(y)}</text>")
            y += yStep
        }

        if (xAxisLabel.isNotEmpty()) {
            val xLabelY = chartY + chartHeight + 55
            svgBuilder.append("<text x='${chartX + chartWidth / 2}' y='$xLabelY' text-anchor='middle' class='chart-text axis-label'>${escapeXml(xAxisLabel)}</text>")
        }
        if (yAxisLabel.isNotEmpty()) {
            val yLabelX = chartX - 65
            svgBuilder.append("<text x='$yLabelX' y='${chartY + chartHeight / 2}' text-anchor='middle' transform='rotate(-90, $yLabelX, ${chartY + chartHeight / 2})' class='chart-text axis-label'>${escapeXml(yAxisLabel)}</text>")
        }
        svgBuilder.append("</g>")

        svgBuilder.append("<g class='plot'>")
        seriesList.forEachIndexed { index, series ->
            if (series.points.isEmpty()) return@forEachIndexed

            val colorIndex = index % colors.size
            val color = series.color ?: colors[colorIndex].color
            val sortedPoints = series.points.sortedBy { it.x }

            // staged reveal timing
            val lineDelaySec = 0.08 * index
            val pointsStartSec = lineDelaySec + 0.18

            svgBuilder.append("<g class='data-series' id='series-$index' tabindex='0'>")
            val strokeDashOffset = if (isPdf) "0" else "1"
            svgBuilder.append("<path class='line-path line-reveal' style='animation-delay:${"%.2f".format(lineDelaySec)}s' pathLength='1' stroke-dasharray='1' stroke-dashoffset='$strokeDashOffset' d='")

            val firstX = xToSvg(sortedPoints[0].x)
            val firstY = yToSvg(sortedPoints[0].y)
            svgBuilder.append("M$firstX,$firstY ")

            if (smoothLines) {
                for (i in 1 until sortedPoints.size) {
                    val x1 = xToSvg(sortedPoints[i - 1].x)
                    val y1 = yToSvg(sortedPoints[i - 1].y)
                    val x2 = xToSvg(sortedPoints[i].x)
                    val y2 = yToSvg(sortedPoints[i].y)

                    val controlX1 = x1 + (x2 - x1) / 3
                    val controlY1 = y1
                    val controlX2 = x2 - (x2 - x1) / 3
                    val controlY2 = y2
                    svgBuilder.append("C$controlX1,$controlY1 $controlX2,$controlY2 $x2,$y2 ")
                }
            } else {
                for (i in 1 until sortedPoints.size) {
                    val xPos = xToSvg(sortedPoints[i].x)
                    val yPos = yToSvg(sortedPoints[i].y)
                    svgBuilder.append("L$xPos,$yPos ")
                }
            }

            svgBuilder.append("' fill='none' stroke='$color' stroke-width='4' stroke-linecap='round' stroke-linejoin='round' />")

            if (showPoints) {
                sortedPoints.forEachIndexed { pointIndex, point ->
                    val cx = xToSvg(point.x)
                    val cy = yToSvg(point.y)
                    val pointDelaySec = pointsStartSec + (pointIndex * 0.05)

                    svgBuilder.append("<circle class='data-point point-reveal' style='animation-delay:${"%.2f".format(pointDelaySec)}s' cx='$cx' cy='$cy' r='6' fill='$color' stroke='$pointStrokeColor' stroke-width='2'>")
                    val xDisplay = point.xLabel ?: formatNumber(point.x)
                    svgBuilder.append("<title>${escapeXml(series.name)}: (${escapeXml(xDisplay)}, ${formatNumber(point.y)})</title>")
                    svgBuilder.append("</circle>")
                }
            }

            svgBuilder.append("</g>")
        }
        svgBuilder.append("</g>")

        if (effectiveShowLegend) {
            val legendX = chartX + chartWidth + 20
            val legendY = chartY + 20
            val legendBoxWidth = computedLegendBoxWidth
            val legendBoxHeight = (legendInnerPadY * 2 + seriesList.size * legendRowHeight).toDouble()
            val legendBoxX = legendX - 22.0
            val legendBoxY = legendY - legendInnerPadY - 8.0

            svgBuilder.append("<g class='legend'>")
            svgBuilder.append("<rect x='$legendBoxX' y='$legendBoxY' width='$legendBoxWidth' height='$legendBoxHeight' rx='10' class='legend-box'/>")

            seriesList.forEachIndexed { index, series ->
                val rowY = legendY + index * legendRowHeight
                val colorIndex = index % colors.size
                val color = series.color ?: colors[colorIndex].color
                val label = truncateWithEllipsis(series.name, legendTextMaxChars)

                // Keep legend text consistently readable in dark mode
                val legendTextColor = if (darkMode) {
                    textColor
                } else {
                    chooseLegendTextColor(color, backgroundColor, textColor)
                }

                svgBuilder.append("<g class='legend-item'>")
                svgBuilder.append("<line x1='${legendX - 10}' y1='$rowY' x2='${legendX + 6}' y2='$rowY' stroke='$color' stroke-width='3' stroke-linecap='round' />")
                if (showPoints) {
                    svgBuilder.append("<circle cx='${legendX - 2}' cy='$rowY' r='4' fill='$color' stroke='$pointStrokeColor' stroke-width='1' />")
                }
                svgBuilder.append("<text x='${legendX + 14}' y='$rowY' dominant-baseline='middle' style='fill: $legendTextColor !important;' class='chart-text legend-label'>${escapeXml(label)}</text>")
                svgBuilder.append("</g>")
            }

            svgBuilder.append("</g>")
        }


        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }

    private fun truncateWithEllipsis(text: String, maxChars: Int): String {
        if (maxChars <= 0) return ""
        return if (text.length <= maxChars) text else text.take(maxChars - 1) + "…"
    }

    private fun normalizeHexColor(color: String): String? {
        val raw = color.trim().removePrefix("#")
        if (raw.length == 3 && raw.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
            return buildString {
                append("#")
                raw.forEach { c -> append(c).append(c) }
            }.uppercase()
        }
        if (raw.length == 6 && raw.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
            return "#${raw.uppercase()}"
        }
        return null
    }

    private fun relativeLuminance(hex: String): Double {
        val normalized = normalizeHexColor(hex) ?: return 0.0
        val r = normalized.substring(1, 3).toInt(16) / 255.0
        val g = normalized.substring(3, 5).toInt(16) / 255.0
        val b = normalized.substring(5, 7).toInt(16) / 255.0

        fun linearize(v: Double): Double =
            if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)

        val rl = linearize(r)
        val gl = linearize(g)
        val bl = linearize(b)
        return 0.2126 * rl + 0.7152 * gl + 0.0722 * bl
    }

    private fun contrastRatio(hexA: String, hexB: String): Double {
        val l1 = relativeLuminance(hexA)
        val l2 = relativeLuminance(hexB)
        val lighter = kotlin.math.max(l1, l2)
        val darker = kotlin.math.min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun chooseLegendTextColor(seriesColor: String, bgColor: String, fallback: String): String {
        val safeSeries = normalizeHexColor(seriesColor)
        val safeBg = normalizeHexColor(bgColor)
        val safeFallback = normalizeHexColor(fallback) ?: fallback

        if (safeSeries != null && safeBg != null) {
            val ratio = contrastRatio(safeSeries, safeBg)
            if (ratio >= 4.5) return safeSeries
        }
        return safeFallback
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
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

