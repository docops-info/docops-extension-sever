package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import java.util.*

class CombinationChartMaker {

    private val defaultColors = listOf(
        "#3498db", "#2ecc71", "#e74c3c", "#f39c12", "#9b59b6",
        "#1abc9c", "#34495e", "#e67e22", "#27ae60", "#d35400"
    )

    fun makeChart(chart: CombinationChart): String {
        val sb = StringBuilder()

        sb.append(makeHead(chart))
        sb.append(makeDefs(chart))
        sb.append(makeBackground(chart))
        sb.append(makeTitle(chart))
        sb.append(makeGrid(chart))
        sb.append(makeAxes(chart))
        sb.append(makeAxisLabels(chart))
        sb.append(makeAxisTicks(chart))
        sb.append(makeData(chart))

        if (chart.display.showLegend) {
            sb.append(makeLegend(chart))
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun makeHead(chart: CombinationChart): String {
        // Increase width to accommodate legend on the right side
        val width = if (chart.display.showLegend) 1050 else 800
        val height = 600
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg id="combo_chart_${chart.id}" width="${(width * chart.display.scale) / DISPLAY_RATIO_16_9}" 
                 height="${(height * chart.display.scale) / DISPLAY_RATIO_16_9}" 
                 viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg" 
                 aria-label='DocOps: Combination Chart'>
        """.trimIndent()
    }

    private fun makeDefs(chart: CombinationChart): String {
        val sb = StringBuilder()
        sb.append("<defs>")

        // Create gradients for bars
        chart.series.filter { it.type == ChartType.BAR }.forEachIndexed { index, series ->
            val colorIndex = index % defaultColors.size
            val color = series.color ?: ChartColors.modernColors[colorIndex].color
            val darkerColor = darkenColor(color, 0.3)

            sb.append("""
                <linearGradient id="barGradient_${series.name.replace(" ", "_")}" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="$color"/>
                    <stop offset="100%" stop-color="$darkerColor"/>
                </linearGradient>
            """.trimIndent())
        }

        // Create gradients for line areas
        chart.series.filter { it.type == ChartType.LINE }.forEachIndexed { index, series ->
            val colorIndex = index % defaultColors.size
            val color = series.color ?: ChartColors.modernColors[colorIndex]

            sb.append("""
                <linearGradient id="lineGradient_${series.name.replace(" ", "_")}" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="$color" stop-opacity="0.3"/>
                    <stop offset="100%" stop-color="$color" stop-opacity="0.1"/>
                </linearGradient>
            """.trimIndent())
        }

        // Add filters
        sb.append("""
            <filter id="dropShadow">
                <feDropShadow dx="2" dy="2" stdDeviation="3" flood-color="rgba(0,0,0,0.3)"/>
            </filter>
            <filter id="glow">
                <feGaussianBlur stdDeviation="4" result="coloredBlur"/>
                <feMerge>
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
        """.trimIndent())

        // Add glass effect gradients and filters if enabled
        if (chart.display.useGlass) {
            sb.append("""
                <!-- Glass effect gradients -->
                <linearGradient id="glassOverlay" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.4);stop-opacity:1" />
                    <stop offset="30%" style="stop-color:rgba(255,255,255,0.2);stop-opacity:1" />
                    <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
                </linearGradient>

                <!-- Highlight gradient -->
                <linearGradient id="glassHighlight" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                    <stop offset="60%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
                </linearGradient>

                <!-- Radial gradient for realistic light reflections -->
                <radialGradient id="glassRadial" cx="30%" cy="30%" r="70%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.5);stop-opacity:1" />
                    <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
                </radialGradient>

                <!-- Enhanced drop shadow filter for glass bars -->
                <filter id="glassDropShadow" x="-30%" y="-30%" width="160%" height="160%">
                    <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="rgba(0,0,0,0.2)"/>
                </filter>

                <!-- Frosted glass blur filter -->
                <filter id="glassBlur" x="-10%" y="-10%" width="120%" height="120%">
                    <feGaussianBlur in="SourceGraphic" stdDeviation="1.5" />
                </filter>

                <!-- Glow filter for hover effect -->
                <filter id="glassGlow" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur in="SourceGraphic" stdDeviation="5" result="blur"/>
                    <feColorMatrix in="blur" type="matrix" values="
                        1 0 0 0 0
                        0 1 0 0 0
                        0 0 1 0 0
                        0 0 0 18 -7
                    " result="glow"/>
                    <feMerge>
                        <feMergeNode in="glow"/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>

                <style>
                    #combo_chart_${chart.id} .glass-bar {
                        transition: all 0.3s ease;
                    }
                    #combo_chart_${chart.id} .glass-bar:hover {
                        filter: url(#glassGlow);
                        transform: scale(1.02);
                        cursor: pointer;
                    }
                </style>
            """.trimIndent())
        }

        sb.append("</defs>")
        return sb.toString()
    }

    private fun makeBackground(chart: CombinationChart): String {
        val bgColor = if (chart.display.useDark) "#1f2937" else chart.display.backgroundColor
        return """<rect width="100%" height="100%" fill="$bgColor" rx="10" ry="10"/>"""
    }

    private fun makeTitle(chart: CombinationChart): String {
        val titleColor = if (chart.display.useDark) "#f9fafb" else "#333"
        val titleBgColor = if (chart.display.useDark) "#374151" else "#f0f0f0"

        return """
            <g>
                <rect x="200" y="10" width="400" height="40" rx="10" ry="10" fill="$titleBgColor" opacity="0.8"/>
                <text x="400" y="35" text-anchor="middle" font-size="20" font-weight="bold" 
                      fill="$titleColor" font-family="Arial, sans-serif">
                    ${chart.title}
                </text>
            </g>
        """.trimIndent()
    }

    private fun makeGrid(chart: CombinationChart): String {
        if (!chart.display.showGrid) return ""

        val gridColor = if (chart.display.useDark) "#374151" else "#e5e7eb"
        val sb = StringBuilder()

        sb.append("""<g class="grid" stroke="$gridColor" stroke-width="1" opacity="0.7">""")

        // Horizontal grid lines
        for (i in 1..8) {
            val y = 100 + i * 50
            sb.append("""<line x1="80" y1="$y" x2="720" y2="$y" stroke-dasharray="3,3"/>""")
        }

        // Vertical grid lines
        for (i in 1..6) {
            val x = 80 + i * 100
            sb.append("""<line x1="$x" y1="100" x2="$x" y2="500" stroke-dasharray="3,3"/>""")
        }

        sb.append("</g>")
        return sb.toString()
    }

    private fun makeAxes(chart: CombinationChart): String {
        val axisColor = if (chart.display.useDark) "#9ca3af" else "#374151"
        val sb = StringBuilder()

        // Primary Y-axis (left)
        sb.append("""<line x1="80" y1="100" x2="80" y2="500" stroke="$axisColor" stroke-width="2"/>""")

        // X-axis
        sb.append("""<line x1="80" y1="500" x2="720" y2="500" stroke="$axisColor" stroke-width="2"/>""")

        // Secondary Y-axis (right) if dual axis is enabled
        if (chart.display.dualYAxis) {
            sb.append("""<line x1="720" y1="100" x2="720" y2="500" stroke="$axisColor" stroke-width="2"/>""")
        }

        return sb.toString()
    }

    private fun makeAxisLabels(chart: CombinationChart): String {
        val labelColor = if (chart.display.useDark) "#d1d5db" else "#6b7280"
        val sb = StringBuilder()

        // X-axis label
        if (chart.xLabel.isNotEmpty()) {
            sb.append("""
                <text x="400" y="550" text-anchor="middle" font-size="14" font-weight="bold" 
                      fill="$labelColor" font-family="Arial, sans-serif">
                    ${chart.xLabel}
                </text>
            """.trimIndent())
        }

        // Primary Y-axis label
        if (chart.yLabel.isNotEmpty()) {
            sb.append("""
                <text x="30" y="300" text-anchor="middle" font-size="14" font-weight="bold" 
                      fill="$labelColor" font-family="Arial, sans-serif" 
                      transform="rotate(-90, 30, 300)">
                    ${chart.yLabel}
                </text>
            """.trimIndent())
        }

        // Secondary Y-axis label
        if (chart.display.dualYAxis && chart.yLabelSecondary.isNotEmpty()) {
            sb.append("""
                <text x="770" y="300" text-anchor="middle" font-size="14" font-weight="bold" 
                      fill="$labelColor" font-family="Arial, sans-serif" 
                      transform="rotate(90, 770, 300)">
                    ${chart.yLabelSecondary}
                </text>
            """.trimIndent())
        }

        return sb.toString()
    }

    private fun makeAxisTicks(chart: CombinationChart): String {
        val tickColor = if (chart.display.useDark) "#d1d5db" else "#6b7280"
        val sb = StringBuilder()

        // Get unique X values for ticks
        val xValues = chart.series.flatMap { it.data }.map { it.x }.distinct().sorted()
        val xStep = 640.0 / (xValues.size - 1)

        // X-axis ticks
        xValues.forEachIndexed { index, xValue ->
            val x = 80 + index * xStep
            sb.append("""<line x1="$x" y1="500" x2="$x" y2="510" stroke="$tickColor" stroke-width="2"/>""")
            sb.append("""
                <text x="$x" y="525" text-anchor="middle" font-size="12" fill="$tickColor" 
                      font-family="Arial, sans-serif">$xValue</text>
            """.trimIndent())
        }

        // Y-axis ticks for primary axis
        val primarySeriesData = chart.series.filter { it.yAxis == YAxisType.PRIMARY }.flatMap { it.data }
        if (primarySeriesData.isNotEmpty()) {
            val maxY = primarySeriesData.maxOf { it.y }
            val minY = primarySeriesData.minOf { it.y }
            val yRange = maxY - minY
            val yStep = yRange / 8

            for (i in 0..8) {
                val yValue = minY + i * yStep
                val y = 500 - (i * 50)
                sb.append("""<line x1="75" y1="$y" x2="80" y2="$y" stroke="$tickColor" stroke-width="2"/>""")
                sb.append("""
                    <text x="70" y="$y" text-anchor="end" font-size="12" fill="$tickColor" 
                          font-family="Arial, sans-serif" dominant-baseline="middle">
                        ${formatNumber(yValue)}
                    </text>
                """.trimIndent())
            }
        }

        // Y-axis ticks for secondary axis if dual axis is enabled
        if (chart.display.dualYAxis) {
            val secondarySeriesData = chart.series.filter { it.yAxis == YAxisType.SECONDARY }.flatMap { it.data }
            if (secondarySeriesData.isNotEmpty()) {
                val maxY = secondarySeriesData.maxOf { it.y }
                val minY = secondarySeriesData.minOf { it.y }
                val yRange = maxY - minY
                val yStep = yRange / 8

                for (i in 0..8) {
                    val yValue = minY + i * yStep
                    val y = 500 - (i * 50)
                    sb.append("""<line x1="720" y1="$y" x2="725" y2="$y" stroke="$tickColor" stroke-width="2"/>""")
                    sb.append("""
                        <text x="730" y="$y" text-anchor="start" font-size="12" fill="$tickColor" 
                              font-family="Arial, sans-serif" dominant-baseline="middle">
                            ${formatNumber(yValue)}
                        </text>
                    """.trimIndent())
                }
            }
        }

        return sb.toString()
    }

    private fun makeData(chart: CombinationChart): String {
        val sb = StringBuilder()

        // Get unique X values and their positions
        val xValues = chart.series.flatMap { it.data }.map { it.x }.distinct().sorted()

        // Calculate bar width first to determine proper spacing
        val barWidth = if (xValues.size > 1) (640.0 / xValues.size) * 0.6 else 60.0

        // Add padding after y-axis line to prevent overlap
        // Y-axis is at x=80, so start bars with padding of half bar width + 10 pixels
        val yAxisPadding = barWidth / 2 + 10
        val startX = 80 + yAxisPadding

        // Calculate available width, accounting for right y-axis when dual axis is enabled
        val rightBoundary = if (chart.display.dualYAxis) {
            // When dual y-axis is enabled, bars should not extend beyond the right y-axis line at x=720
            // Add padding to prevent bars from overlapping with right y-axis elements
            720.0 - (barWidth / 2 + 10)  // Leave space for half bar width + padding before right y-axis
        } else {
            720.0  // Normal right boundary when no dual y-axis
        }
        val availableWidth = rightBoundary - startX

        val xPositions = xValues.mapIndexed { index, xValue -> 
            xValue to (startX + index * (availableWidth / (xValues.size - 1)))
        }.toMap()

        // Draw bars first (so lines appear on top)
        val barSeries = chart.series.filter { it.type == ChartType.BAR }
        barSeries.forEachIndexed { seriesIndex, series ->
            val colorIndex = seriesIndex % defaultColors.size
            val color = series.color ?: defaultColors[colorIndex]
            val gradientId = "barGradient_${series.name.replace(" ", "_")}"

            // Calculate Y scaling for this series
            val yData = if (series.yAxis == YAxisType.PRIMARY) {
                chart.series.filter { it.yAxis == YAxisType.PRIMARY }.flatMap { it.data }
            } else {
                chart.series.filter { it.yAxis == YAxisType.SECONDARY }.flatMap { it.data }
            }

            if (yData.isNotEmpty()) {
                val maxY = yData.maxOf { it.y }
                val minY = yData.minOf { it.y }
                val yRange = maxY - minY

                series.data.forEach { dataPoint ->
                    val x = xPositions[dataPoint.x]!! - barWidth / 2
                    val yNormalized = if (yRange > 0) (dataPoint.y - minY) / yRange else 0.5
                    val barHeight = yNormalized * 400
                    val y = 500 - barHeight

                    if (chart.display.useGlass) {
                        // Create glass effect bar with layered structure
                        sb.append("""
                            <g class="glass-bar">
                                <!-- Base rectangle with gradient -->
                                <rect x="$x" 
                                      y="$y" 
                                      width="$barWidth" 
                                      height="$barHeight" 
                                      rx="6" 
                                      ry="6" 
                                      fill="url(#$gradientId)"
                                      filter="url(#glassDropShadow)"
                                      stroke="rgba(255,255,255,0.3)" stroke-width="1">
                                    <title>${series.name}: ${dataPoint.x} = ${formatNumber(dataPoint.y)}</title>
                                </rect>

                                <!-- Glass overlay with transparency -->
                                <rect x="$x" 
                                      y="$y" 
                                      width="$barWidth" 
                                      height="$barHeight" 
                                      rx="6" 
                                      ry="6"
                                      fill="url(#glassOverlay)"
                                      filter="url(#glassBlur)">
                                </rect>

                                <!-- Radial highlight for realistic light effect -->
                                <ellipse cx="${x + barWidth/4}" 
                                         cy="${y + barHeight/5}" 
                                         rx="${barWidth/3}" 
                                         ry="${Math.min(barHeight/6, 15.0)}"
                                         fill="url(#glassRadial)"
                                         opacity="0.7">
                                </ellipse>

                                <!-- Top highlight for shine -->
                                <rect x="${x + 3}" 
                                      y="${y + 3}" 
                                      width="${barWidth - 6}" 
                                      height="${Math.min(barHeight/4, 20.0)}" 
                                      rx="4" 
                                      ry="4"
                                      fill="url(#glassHighlight)">
                                </rect>
                            </g>
                        """.trimIndent())
                    } else {
                        // Standard bar rendering
                        sb.append("""
                            <rect x="$x" y="$y" width="$barWidth" height="$barHeight" 
                                  fill="url(#$gradientId)" filter="url(#dropShadow)" rx="3" ry="3">
                                <title>${series.name}: ${dataPoint.x} = ${formatNumber(dataPoint.y)}</title>
                            </rect>
                        """.trimIndent())
                    }

                    // Add value label on top of bar
                    val labelColor = if (chart.display.useDark) "#f9fafb" else "#333"
                    if (barHeight > 20) {
                        sb.append("""
                            <text x="${x + barWidth / 2}" y="${y - 5}" text-anchor="middle" 
                                  font-size="10" fill="$labelColor" font-family="Arial, sans-serif">
                                ${formatNumber(dataPoint.y)}
                            </text>
                        """.trimIndent())
                    }
                }
            }
        }

        // Draw lines
        val lineSeries = chart.series.filter { it.type == ChartType.LINE }
        lineSeries.forEachIndexed { seriesIndex, series ->
            val colorIndex = seriesIndex % defaultColors.size
            val color = series.color ?: defaultColors[colorIndex]
            val gradientId = "lineGradient_${series.name.replace(" ", "_")}"

            // Calculate Y scaling for this series
            val yData = if (series.yAxis == YAxisType.PRIMARY) {
                chart.series.filter { it.yAxis == YAxisType.PRIMARY }.flatMap { it.data }
            } else {
                chart.series.filter { it.yAxis == YAxisType.SECONDARY }.flatMap { it.data }
            }

            if (yData.isNotEmpty() && series.data.isNotEmpty()) {
                val maxY = yData.maxOf { it.y }
                val minY = yData.minOf { it.y }
                val yRange = maxY - minY

                val sortedData = series.data.sortedBy { it.x }
                val pathData = StringBuilder()
                val areaData = StringBuilder()

                // Build path for line
                sortedData.forEachIndexed { index, dataPoint ->
                    val x = xPositions[dataPoint.x]!!
                    val yNormalized = if (yRange > 0) (dataPoint.y - minY) / yRange else 0.5
                    val y = 500 - (yNormalized * 400)

                    if (index == 0) {
                        pathData.append("M $x $y")
                        areaData.append("M $x $y")
                    } else {
                        if (chart.display.smoothLines) {
                            val prevX = xPositions[sortedData[index - 1].x]!!
                            val prevYNormalized = if (yRange > 0) (sortedData[index - 1].y - minY) / yRange else 0.5
                            val prevY = 500 - (prevYNormalized * 400)

                            val controlX1 = prevX + (x - prevX) / 3
                            val controlY1 = prevY
                            val controlX2 = x - (x - prevX) / 3
                            val controlY2 = y

                            pathData.append(" C $controlX1 $controlY1 $controlX2 $controlY2 $x $y")
                            areaData.append(" C $controlX1 $controlY1 $controlX2 $controlY2 $x $y")
                        } else {
                            pathData.append(" L $x $y")
                            areaData.append(" L $x $y")
                        }
                    }
                }

                // Close area path
                val lastX = xPositions[sortedData.last().x]!!
                val firstX = xPositions[sortedData.first().x]!!
                areaData.append(" L $lastX 500 L $firstX 500 Z")

                // Draw area fill
                sb.append("""
                    <path d="$areaData" fill="url(#$gradientId)" opacity="0.3"/>
                """.trimIndent())

                // Draw line
                sb.append("""
                    <path d="$pathData" fill="none" stroke="$color" stroke-width="3" 
                          stroke-linejoin="round" stroke-linecap="round"/>
                """.trimIndent())

                // Draw points if enabled
                if (chart.display.showPoints) {
                    sortedData.forEach { dataPoint ->
                        val x = xPositions[dataPoint.x]!!
                        val yNormalized = if (yRange > 0) (dataPoint.y - minY) / yRange else 0.5
                        val y = 500 - (yNormalized * 400)

                        val pointStroke = if (chart.display.useDark) "#1f2937" else "#ffffff"
                        sb.append("""
                            <circle cx="$x" cy="$y" r="5" fill="$color" stroke="$pointStroke" 
                                    stroke-width="2" filter="url(#dropShadow)">
                                <title>${series.name}: ${dataPoint.x} = ${formatNumber(dataPoint.y)}</title>
                            </circle>
                        """.trimIndent())

                        // Add value label
                        val labelColor = if (chart.display.useDark) "#f9fafb" else "#333"
                        sb.append("""
                            <text x="$x" y="${y - 10}" text-anchor="middle" font-size="10" 
                                  fill="$labelColor" font-family="Arial, sans-serif">
                                ${formatNumber(dataPoint.y)}
                            </text>
                        """.trimIndent())
                    }
                }
            }
        }

        return sb.toString()
    }

    private fun makeLegend(chart: CombinationChart): String {
        val sb = StringBuilder()
        val legendBgColor = if (chart.display.useDark) "#374151" else "#ffffff"
        val legendBorderColor = if (chart.display.useDark) "#4b5563" else "#d1d5db"
        val legendTextColor = if (chart.display.useDark) "#f9fafb" else "#374151"

        // Position legend on the right side, after secondary axis label
        val legendX = 820
        val legendY = 60
        val legendWidth = 200
        val legendHeight = 20 + (chart.series.size * 25)

        sb.append("""
            <rect x="$legendX" y="$legendY" width="$legendWidth" height="$legendHeight" 
                  fill="$legendBgColor" stroke="$legendBorderColor" stroke-width="1" 
                  rx="8" ry="8" opacity="0.95"/>
        """.trimIndent())

        chart.series.forEachIndexed { index, series ->
            val colorIndex = index % defaultColors.size
            val color = series.color ?: defaultColors[colorIndex]
            val yPos = legendY + 20 + (index * 25)

            if (series.type == ChartType.BAR) {
                sb.append("""
                    <rect x="${legendX + 10}" y="${yPos - 8}" width="15" height="15" 
                          fill="$color" rx="2" ry="2"/>
                """.trimIndent())
            } else {
                sb.append("""
                    <line x1="${legendX + 10}" y1="$yPos" x2="${legendX + 25}" y2="$yPos" 
                          stroke="$color" stroke-width="3"/>
                    <circle cx="${legendX + 17}" cy="$yPos" r="3" fill="$color"/>
                """.trimIndent())
            }

            val axisLabel = if (chart.display.dualYAxis && series.yAxis == YAxisType.SECONDARY) " (R)" else ""
            sb.append("""
                <text x="${legendX + 35}" y="${yPos + 4}" font-size="12" fill="$legendTextColor" 
                      font-family="Arial, sans-serif">${series.name}$axisLabel</text>
            """.trimIndent())
        }

        return sb.toString()
    }

    private fun darkenColor(hexColor: String, factor: Double): String {
        val hex = hexColor.replace("#", "")

        // Validate hex color format
        if (hex.length != 6 || !hex.matches(Regex("[0-9A-Fa-f]{6}"))) {
            // Return a default dark color if input is invalid
            return "#333333"
        }

        val r = Integer.parseInt(hex.substring(0, 2), 16)
        val g = Integer.parseInt(hex.substring(2, 4), 16)
        val b = Integer.parseInt(hex.substring(4, 6), 16)

        val newR = (r * (1 - factor)).toInt().coerceIn(0, 255)
        val newG = (g * (1 - factor)).toInt().coerceIn(0, 255)
        val newB = (b * (1 - factor)).toInt().coerceIn(0, 255)

        return String.format("#%02x%02x%02x", newR, newG, newB)
    }

    private fun formatNumber(value: Double): String {
        return if (value == value.toInt().toDouble()) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }
}
