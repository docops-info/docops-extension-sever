package io.docops.docopsextensionssupport.chart

import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.gradientFromColor
import io.docops.docopsextensionssupport.util.ParsingUtils

class PieChartImproved {
    // Modern color palette for pie chart
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
    fun makePieSvg(payload: String): String {
        // Parse configuration and data from content
        val (config, chartData) = parseConfigAndData(payload)
        // Parse colors from config or attributes
        val configColors = config["colors"]?.split(",")?.map { it.trim() }
        val customColors = configColors

        val title = config.getOrDefault("title", "Pie Chart")
        val width = config.getOrDefault("width", "500")
        val height = config.getOrDefault("height", "500")
        val showLegend = config["legend"]?.toBoolean() ?: true
        val showPercentages = config["percentages"]?.toBoolean() ?: true
        val enableHoverEffects = config["hover"]?.toBoolean() ?: true
        val isDonut = config["donut"]?.toBoolean() ?: true

        val darkMode = config["darkMode"]?.toBoolean() ?: false
        // Parse the pie chart data
        val pieData = parsePieChartData(chartData)

        // Generate SVG
        val svg = generatePieChartSvg(
            pieData,
            title,
            width.toInt(),
            height.toInt(),
            showLegend,
            showPercentages,
            customColors ?: defaultColors,
            enableHoverEffects,
            isDonut, darkMode
        )
        return svg.trimIndent()
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

    private fun parsePieChartData(content: String): List<PieSegment> {
        val segments = mutableListOf<PieSegment>()

        content.lines().forEach { line ->
            if (line.isNotBlank()) {
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 2) {
                    val label = parts[0]
                    val value = parts[1].toDoubleOrNull() ?: 0.0
                    val color = if (parts.size > 2 && parts[2].isNotBlank()) parts[2] else null

                    if (value > 0) {
                        segments.add(PieSegment(label, value, color))
                    }
                }
            }
        }

        return segments
    }

    private fun generatePieChartSvg(
        segments: List<PieSegment>,
        title: String,
        width: Int,
        height: Int,
        showLegend: Boolean,
        showPercentages: Boolean,
        colors: List<String>,
        enableHoverEffects: Boolean,
        isDonut: Boolean,
        darkMode: Boolean = false
    ): String {
        val svgBuilder = StringBuilder()
        val id = UUID.randomUUID().toString()

        // Calculate chart dimensions considering legend
        val legendWidth = if (showLegend) 200 else 0
        val chartWidth = width - legendWidth
        val centerX = chartWidth / 2.0
        val centerY = height / 2.0
        val radius = min(chartWidth, height) / 2.0 - 80.0  // More padding for title
        val innerRadius = if (isDonut) radius * 0.55 else 0.0

        val total = segments.sumOf { it.value }
        val segmentsWithAngles = mutableListOf<SegmentWithAngles>()

        var currentAngle = 0.0
        segments.forEachIndexed { index, segment ->
            val percentage = (segment.value / total) * 100
            val angleSize = (segment.value / total) * 360.0
            val color = segment.color ?: colors[index % colors.size]

            segmentsWithAngles.add(
                SegmentWithAngles(
                    segment = segment,
                    startAngle = currentAngle,
                    endAngle = currentAngle + angleSize,
                    percentage = percentage,
                    color = color
                )
            )
            currentAngle += angleSize
        }

        // Set background based on dark mode
        val backgroundColor = if (darkMode) "#1e293b" else "#ffffff"

        svgBuilder.append("<svg width='$width' height='$height' xmlns='http://www.w3.org/2000/svg' id='ID_$id' preserveAspectRatio=\"xMidYMid meet\" viewBox=\"0 0 $width $height\" style=\"background-color: $backgroundColor;\">")

        // Enhanced glass effect definitions
        svgBuilder.append("""
        <defs>
            <!-- Dark mode compatible glass overlay gradient -->
            <linearGradient id="glassOverlay_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.2" else "0.3"});stop-opacity:1"/>
                <stop offset="25%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.1" else "0.15"});stop-opacity:1"/>
                <stop offset="50%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.03" else "0.05"});stop-opacity:1"/>
                <stop offset="75%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.01" else "0.02"});stop-opacity:1"/>
                <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1"/>
            </linearGradient>

            <!-- Dark mode compatible highlight gradient -->
            <linearGradient id="glassHighlight_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.4" else "0.6"});stop-opacity:1"/>
                <stop offset="40%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.15" else "0.2"});stop-opacity:1"/>
                <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1"/>
            </linearGradient>

            <!-- Dark mode compatible radial light reflection -->
            <radialGradient id="glassRadial_$id" cx="35%" cy="25%" r="60%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.3" else "0.4"});stop-opacity:1"/>
                <stop offset="30%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.15" else "0.2"});stop-opacity:1"/>
                <stop offset="60%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.05" else "0.1"});stop-opacity:1"/>
                <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1"/>
            </radialGradient>

            <!-- Enhanced drop shadow -->
            <filter id="glassDropShadow_$id" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="3"/>
                <feOffset dx="0" dy="2" result="offset"/>
                <feComponentTransfer>
                    <feFuncA type="linear" slope="${if (darkMode) "0.5" else "0.3"}"/>
                </feComponentTransfer>
                <feMerge>
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>

            <!-- Dark mode compatible glass border gradient -->
            <linearGradient id="glassBorder_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.3" else "0.4"});stop-opacity:1"/>
                <stop offset="50%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.1" else "0.15"});stop-opacity:1"/>
                <stop offset="100%" style="stop-color:rgba(255,255,255,${if (darkMode) "0.03" else "0.05"});stop-opacity:1"/>
            </linearGradient>
        </defs>
    """.trimIndent())

        // Enhanced CSS with better glass effects
        svgBuilder.append("""
        <style>
            .pie-segment { 
                transition: transform 0.3s ease, filter 0.3s ease; 
                transform-origin: ${centerX}px ${centerY}px; 
                filter: url(#glassDropShadow_$id);
            }
            .pie-segment:hover { 
                transform: scale(1.03); 
                filter: url(#glassDropShadow_$id) brightness(1.1);
                cursor: pointer; 
            }
            .pie-segment-overlay { 
                pointer-events: none; 
                mix-blend-mode: ${if (darkMode) "soft-light" else "screen"};
                opacity: ${if (darkMode) "0.6" else "0.7"};
            }
            .pie-segment-highlight { 
                pointer-events: none; 
                opacity: ${if (darkMode) "0.4" else "0.5"};
                mix-blend-mode: overlay;
            }
            .legend-item { 
                transition: all 0.2s ease; 
            }
            .legend-item:hover { 
                cursor: pointer; 
                transform: translateY(-1px); 
            }
            .ios-legend-card { 
                transition: all 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94); 
            }
            .ios-legend-card:hover { 
                cursor: pointer; 
                transform: translateY(-2px) scale(1.02); 
                filter: brightness(${if (darkMode) "1.1" else "1.05"});
            }
            .ios-legend-card:active { 
                transform: translateY(-1px) scale(1.01); 
                transition: all 0.1s cubic-bezier(0.25, 0.46, 0.45, 0.94);
            }
            .glass-title {
                filter: url(#glassDropShadow_$id);
                text-shadow: 0 1px 2px rgba(0,0,0,${if (darkMode) "0.8" else "0.1"});
            }
        </style>
    """.trimIndent())

        // Generate pie segments with improved glass effects
        segmentsWithAngles.forEachIndexed { index, segmentData ->
            val pathData = createPieSegmentPath(
                centerX, centerY, radius, innerRadius,
                segmentData.startAngle, segmentData.endAngle
            )

            // Calculate label position for percentage display
            val midAngle = (segmentData.startAngle + segmentData.endAngle) / 2.0
            val labelRadius = if (isDonut) (radius + innerRadius) / 2.0 else radius * 0.75
            val labelX = centerX + labelRadius * cos(Math.toRadians(midAngle))
            val labelY = centerY + labelRadius * sin(Math.toRadians(midAngle))

            svgBuilder.append("""
            <g>
                <path id="segment-$index" class="pie-segment" 
                      d="$pathData" 
                      fill="${segmentData.color}" 
                      stroke="rgba(255,255,255,${if (darkMode) "0.15" else "0.2"})" 
                      stroke-width="1">
                    <title>${segmentData.segment.label}: ${segmentData.segment.value} (${String.format("%.1f", segmentData.percentage)}%)</title>
                </path>

                <!-- Improved glass overlay -->
                <path class="pie-segment-overlay" 
                      d="$pathData" 
                      fill="url(#glassOverlay_$id)" 
                      stroke="url(#glassBorder_$id)" 
                      stroke-width="0.5"/>

                <!-- Subtle highlight positioned at top-left -->
                <path class="pie-segment-highlight" 
                      d="$pathData" 
                      fill="url(#glassRadial_$id)"/>
            </g>
        """.trimIndent())

            // Add percentage labels if enabled
            if (showPercentages) {
                svgBuilder.append("""
                <text x="$labelX" y="$labelY" 
                      font-family="Arial, sans-serif" 
                      font-size="12" 
                      font-weight="bold"
                      text-anchor="middle" 
                      dominant-baseline="middle"
                      fill="white" 
                      style="text-shadow: 0 1px 2px rgba(0,0,0,0.8);">
                    ${String.format("%.1f", segmentData.percentage)}%
                </text>
            """.trimIndent())
            }
        }

        // Add donut hole if needed
        if (isDonut) {
            svgBuilder.append("""
            <circle cx="$centerX" cy="$centerY" r="$innerRadius" 
                    fill="$backgroundColor" 
                    stroke="url(#glassBorder_$id)" 
                    stroke-width="1"/>
            <circle cx="$centerX" cy="$centerY" r="$innerRadius" 
                    fill="url(#glassOverlay_$id)" 
                    opacity="0.5"/>
        """.trimIndent())
        }

        // Enhanced title with glass effect
        svgBuilder.append("""
        <text x="$centerX" y="30" 
              font-family="Arial, sans-serif" 
              font-size="20" 
              font-weight="600"
              text-anchor="middle" 
              fill="${if (darkMode) "#f8fafc" else "#1a1a1a"}"
              class="glass-title">$title</text>
    """.trimIndent())

        // Add legend if enabled - positioned properly on the right
        if (showLegend) {
            val legendX = chartWidth + 20  // 20px padding from chart area
            val legendY = 80  // Start below title
            svgBuilder.append(generateLegend(segmentsWithAngles, legendX, legendY, darkMode, id))
        }

        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }

    private fun generateLegend(
        segments: List<SegmentWithAngles>,
        x: Int,
        y: Int,
        darkMode: Boolean,
        id: String
    ): String {
        val legendBuilder = StringBuilder()

        // Add iOS card theme definitions
        legendBuilder.append("""
            <!-- iOS Card Theme Definitions -->
            <defs>
                <!-- iOS Card Background Gradient -->
                <linearGradient id="iosCardBg_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:${if (darkMode) "rgba(55, 65, 81, 0.95)" else "rgba(255, 255, 255, 0.95)"};stop-opacity:1"/>
                    <stop offset="100%" style="stop-color:${if (darkMode) "rgba(31, 41, 55, 0.95)" else "rgba(248, 250, 252, 0.95)"};stop-opacity:1"/>
                </linearGradient>

                <!-- iOS Card Border -->
                <linearGradient id="iosCardBorder_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:${if (darkMode) "rgba(75, 85, 99, 0.6)" else "rgba(229, 231, 235, 0.8)"};stop-opacity:1"/>
                    <stop offset="100%" style="stop-color:${if (darkMode) "rgba(55, 65, 81, 0.4)" else "rgba(209, 213, 219, 0.6)"};stop-opacity:1"/>
                </linearGradient>

                <!-- iOS Card Shadow Filter -->
                <filter id="iosCardShadow_$id" x="-50%" y="-50%" width="200%" height="200%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="2"/>
                    <feOffset dx="0" dy="1" result="offset"/>
                    <feComponentTransfer>
                        <feFuncA type="linear" slope="${if (darkMode) "0.3" else "0.15"}"/>
                    </feComponentTransfer>
                    <feMerge>
                        <feMergeNode/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>

                <!-- iOS Color Indicator Gradient -->
                <linearGradient id="iosColorIndicator_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1"/>
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1"/>
                </linearGradient>
            </defs>
        """.trimIndent())

        segments.forEachIndexed { index, segment ->
            val cardY = y + (index * 45)  // More spacing for card layout
            val cardWidth = 180
            val cardHeight = 36

            legendBuilder.append("""
            <g class="ios-legend-card" data-segment="segment-$index">
                <!-- iOS Card Background -->
                <rect x="$x" y="$cardY" width="$cardWidth" height="$cardHeight" 
                      fill="url(#iosCardBg_$id)" 
                      stroke="url(#iosCardBorder_$id)" 
                      stroke-width="0.5"
                      rx="8" ry="8"
                      filter="url(#iosCardShadow_$id)"/>

                <!-- Color Indicator with iOS styling -->
                <rect x="${x + 8}" y="${cardY + 8}" width="20" height="20" 
                      fill="${segment.color}" 
                      rx="4" ry="4"/>
                <rect x="${x + 8}" y="${cardY + 8}" width="20" height="20" 
                      fill="url(#iosColorIndicator_$id)" 
                      rx="4" ry="4"/>

                <!-- Label Text -->
                <text x="${x + 36}" y="${cardY + 14}" 
                      font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" 
                      font-size="13" 
                      font-weight="500"
                      dominant-baseline="middle"
                      fill="${if (darkMode) "#f9fafb" else "#111827"}">
                    ${segment.segment.label}
                </text>

                <!-- Percentage Text -->
                <text x="${x + 36}" y="${cardY + 26}" 
                      font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" 
                      font-size="11" 
                      font-weight="400"
                      dominant-baseline="middle"
                      fill="${if (darkMode) "#9ca3af" else "#6b7280"}">
                    ${String.format("%.1f", segment.percentage)}%
                </text>

                <!-- Value Text (right aligned) -->
                <text x="${x + cardWidth - 8}" y="${cardY + 20}" 
                      font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" 
                      font-size="12" 
                      font-weight="600"
                      text-anchor="end"
                      dominant-baseline="middle"
                      fill="${if (darkMode) "#d1d5db" else "#374151"}">
                    ${segment.segment.value.toInt()}
                </text>
            </g>
        """.trimIndent())
        }

        return legendBuilder.toString()
    }



    private fun createPieSegmentPath(
        centerX: Double,
        centerY: Double,
        radius: Double,
        innerRadius: Double,
        startAngle: Double,
        endAngle: Double
    ): String {
        val startAngleRad = Math.toRadians(startAngle)
        val endAngleRad = Math.toRadians(endAngle)

        val x1 = centerX + radius * cos(startAngleRad)
        val y1 = centerY + radius * sin(startAngleRad)
        val x2 = centerX + radius * cos(endAngleRad)
        val y2 = centerY + radius * sin(endAngleRad)

        val largeArcFlag = if (endAngle - startAngle > 180) 1 else 0

        return if (innerRadius > 0) {
            // Donut chart path
            val ix1 = centerX + innerRadius * cos(startAngleRad)
            val iy1 = centerY + innerRadius * sin(startAngleRad)
            val ix2 = centerX + innerRadius * cos(endAngleRad)
            val iy2 = centerY + innerRadius * sin(endAngleRad)

            "M $x1 $y1 " +
                    "A $radius $radius 0 $largeArcFlag 1 $x2 $y2 " +
                    "L $ix2 $iy2 " +
                    "A $innerRadius $innerRadius 0 $largeArcFlag 0 $ix1 $iy1 " +
                    "Z"
        } else {
            // Regular pie chart path
            "M $centerX $centerY " +
                    "L $x1 $y1 " +
                    "A $radius $radius 0 $largeArcFlag 1 $x2 $y2 " +
                    "Z"
        }
    }



    private data class PieSegment(val label: String, val value: Double, val color: String? = null)

    private data class SegmentWithAngles(
        val segment: PieSegment,
        val startAngle: Double,
        val endAngle: Double,
        val percentage: Double,
        val color: String
    )
}
