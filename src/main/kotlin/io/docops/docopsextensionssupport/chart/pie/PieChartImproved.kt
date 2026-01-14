package io.docops.docopsextensionssupport.chart.pie

import io.docops.docopsextensionssupport.chart.ChartColors
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.formatDecimal
import io.docops.docopsextensionssupport.util.BackgroundHelper
import io.docops.docopsextensionssupport.util.ParsingUtils
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.update
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PieChartImproved {

    private var theme = ThemeFactory.getTheme(false)
    fun makePieSvg(payload: String, csvResponse: CsvResponse, isPdf: Boolean, useDark: Boolean): String {
        // Parse configuration and data from content
        val (config, chartData) = parseConfigAndData(payload)
        val display = SliceDisplay(
            useDark = useDark,
            visualVersion = config["visualVersion"]?.toIntOrNull() ?: 1,
            donut = config["donut"]?.toBoolean() ?: false,
            showLegend = config["legend"]?.toBoolean() ?: true
        )
        theme = ThemeFactory.getTheme(display)
        // Parse colors from config or attributes
        val configColors = config["colors"]?.split(",")?.map { it.trim() }
        val customColors = configColors

        val title = config["title"] ?: "Pie Chart"
        val width = config["width"] ?: "500"
        val height = config["height"] ?: "500"
        val showLegend = config["legend"]?.toBoolean() ?: true
        val showPercentages = config["percentages"]?.toBoolean() ?: true
        val enableHoverEffects = config["hover"]?.toBoolean() ?: true
        val isDonut = config["donut"]?.toBoolean() ?: false

        var darkMode = config["darkMode"]?.toBoolean() ?: false
        if(useDark) {
            darkMode = useDark
        }
        // Parse the pie chart data
        val pieData = parsePieChartData(chartData)
        var colors = ChartColors.Companion.modernColors
        if (customColors != null) {
            colors = mutableListOf<SVGColor>()
            customColors.forEach {
                colors.add(SVGColor(it))
            }
        }
        if(isDonut && display.visualVersion ==2) {
            val maker = DonutMakerImproved()
            val slices = pieData.map { segment ->
                PieSlice(
                    label = segment.label,
                    amount = segment.value,
                    itemDisplay = SliceItemDisplay(color = segment.color)
                )
            }.toMutableList()
            // Construct the PieSlices wrapper
            val pieSlicesObj = PieSlices(title = title, slices = slices, display = display)
            return maker.makeDonut(pieSlices = pieSlicesObj)
        }
        if(!isDonut) {
            val maker = PieSliceMakerImproved()
            val slices = pieData.map { segment ->
                PieSlice(
                    label = segment.label,
                    amount = segment.value,
                    itemDisplay = SliceItemDisplay(color = segment.color)
                )
            }.toMutableList()
            val pieSlicesObj = PieSlices(title = title, slices = slices, display = display)
            return maker.makePie(pieSlices = pieSlicesObj)
        }
        // Generate SVG
        val svg = generatePieChartSvg(
            pieData,
            title,
            width.toInt(),
            height.toInt(),
            showLegend,
            showPercentages,
            colors.map { it.color },
            enableHoverEffects,
            isDonut, darkMode, isPdf = isPdf, display = display
        )

        csvResponse.update(payloadToSimpleCsv(pieData))
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

    @OptIn(ExperimentalUuidApi::class)
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
        darkMode: Boolean = false,
        isPdf: Boolean,
        display: SliceDisplay
    ): String {
        val svgBuilder = StringBuilder()
        val id = display.id
        val darkMode = theme.canvas != "#ffffff"

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

        // Set background based on dark mode (Midnight IDE aesthetic)
        val backgroundColor = if (darkMode) "#020617" else "#ffffff"
        val textColorPrimary = if (darkMode) "#f8fafc" else "#0f172a"
        val textColorSecondary = if (darkMode) "#94a3b8" else "#475569"

        svgBuilder.append("<svg width='$width' height='$height' xmlns='http://www.w3.org/2000/svg' id='ID_$id' preserveAspectRatio=\"xMidYMid meet\" viewBox=\"0 0 $width $height\">")

        val darkModeDefs = BackgroundHelper.getBackgroundGradient(useDark = darkMode, id)
        //svgBuilder.append(BackgroundHelper.getBackgroundGradient(darkMode, id))
        // Enhanced atmospheric definitions
        svgBuilder.append("""
        <defs>
        <!-- Geometric Atmosphere Pattern -->
                <pattern id="dotPattern_$id" x="0" y="0" width="30" height="30" patternUnits="userSpaceOnUse">
                    <circle cx="2" cy="2" r="1" fill="${if (darkMode) "#334155" else "#cbd5e1"}" fill-opacity="0.4" />
                </pattern>
                
                <!-- High-Impact Glow Filter -->
                <filter id="glow_$id" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur stdDeviation="3" result="blur" />
                    <feComposite in="SourceGraphic" in2="blur" operator="over" />
                </filter>
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
            $darkModeDefs
        </defs>
    """.trimIndent())

        // Enhanced CSS with better glass effects
        svgBuilder.append("""
        <style>
             @keyframes revealPie {
                    from { transform: scale(0.85); opacity: 0; }
                    to { transform: scale(1); opacity: 1; }
            }
            .pie-segment { 
                        transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), filter 0.3s ease; 
                        transform-origin: ${centerX}px ${centerY}px; 
                        filter: url(#glassDropShadow_$id);
                        opacity: ${if (isPdf) "1" else "0"};
                        animation: ${if (isPdf) "none" else "revealPie 0.6s cubic-bezier(0.22, 1, 0.36, 1) forwards"};
                    }
                .pie-segment:hover { 
                    transform: scale(1.05); 
                    filter: url(#glassDropShadow_$id) url(#glow_$id) brightness(1.1);
                    cursor: pointer; 
                }
                .chart-title {
                    font-family: ${theme.fontFamily};
                    text-transform: uppercase;
                    letter-spacing: 1px;
                }
                .legend-text {
                    font-family: ${theme.fontFamily};
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

        svgBuilder.append(BackgroundHelper.getBackGroundPath(darkMode, id, width = width.toFloat(), height = height.toFloat()))
// Apply atmospheric pattern overlay
        svgBuilder.append("<rect width='$width' height='$height' fill='url(#dotPattern_$id)' rx='12' pointer-events='none'/>")

        // Generate pie segments with improved glass effects
        segmentsWithAngles.forEachIndexed { index, segmentData ->
            val pathData = createPieSegmentPath(
                centerX, centerY, radius, innerRadius,
                segmentData.startAngle, segmentData.endAngle
            )
            val delay = 0.4 + (index * 0.1) // Staggered reveal delay

            // Calculate label position for percentage display
            val midAngle = (segmentData.startAngle + segmentData.endAngle) / 2.0
            val labelRadius = if (isDonut) (radius + innerRadius) / 2.0 else radius * 0.75
            val labelX = centerX + labelRadius * cos(midAngle * PI / 180.0)
            val labelY = centerY + labelRadius * sin(midAngle * PI / 180.0)

            if(!isPdf) {
                svgBuilder.append(
                    """
                <g style="animation-delay: ${delay}s" class="pie-segment">
                    <path id="segment-$index" 
                          d="$pathData" 
                          fill="${segmentData.color}" 
                      stroke="rgba(255,255,255,${if (darkMode) "0.15" else "0.2"})" 
                      stroke-width="1">
                    <title>${segmentData.segment.label}: ${segmentData.segment.value} (${
                        formatDecimal(
                            segmentData.percentage,
                            1
                        )
                    }%)</title>
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
        """.trimIndent()
                )
            }else {
                svgBuilder.append("""
                    <g>
                <path id="segment-$index" class="pie-segment" 
                      d="$pathData" 
                      fill="${segmentData.color}" 
                      stroke="rgba(255,255,255,${if (darkMode) "0.15" else "0.2"})" 
                      stroke-width="1">
                    <title>${segmentData.segment.label}: ${segmentData.segment.value} (${
                    formatDecimal(
                        segmentData.percentage,
                        1
                    )
                }%)</title>
                </path>
                </g>
                """.trimIndent())
            }

            // Add percentage labels if enabled
            if (showPercentages) {
                svgBuilder.append("""
                    <text x="$labelX" y="$labelY" 
                          font-family="${theme.fontFamily}" 
                          font-size="12" 
                          font-weight="800"
                          text-anchor="middle" 
                          dominant-baseline="middle"
                          fill="white" 
                          style="text-shadow: 0 1px 2px rgba(0,0,0,0.8); pointer-events: none;">
                        ${formatDecimal(segmentData.percentage, 1)}%
                    </text>
                """.trimIndent())
            }
        }

        // Add donut hole if needed
        if (isDonut) {
            svgBuilder.append("""
            <circle cx="$centerX" cy="$centerY" r="$innerRadius" 
                    fill="${theme.canvas}" 
                    stroke="url(#glassBorder_$id)" 
                    stroke-width="1"/>
            <circle cx="$centerX" cy="$centerY" r="$innerRadius" 
                    fill="url(#glassOverlay_$id)" 
                    opacity="0.5"/>
        """.trimIndent())
        }

        // Enhanced title with glass effect and typography
        svgBuilder.append("""
            <text x="$centerX" y="35" 
                  font-size="22" 
                  font-weight="800"
                  text-anchor="middle" 
                  fill="${theme.primaryText}"
                  class="glass-title chart-title">$title</text>
        """.trimIndent())

        // Add legend if enabled - positioned properly on the right
        if (showLegend) {
            val legendX = chartWidth + 20
            val legendY = 80
            svgBuilder.append("<g transform='translate(-10,0)'>")
            svgBuilder.append(generateLegend(segmentsWithAngles, legendX, legendY, theme, id, isPdf = isPdf))
            svgBuilder.append("</g>")
        }

        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }

    private fun generateLegend(
        segments: List<SegmentWithAngles>,
        x: Int,
        y: Int,
        theme: DocOpsTheme,
        id: String,
        isPdf: Boolean
    ): String {
        val legendBuilder = StringBuilder()
        val darkMode = theme.canvas != "#ffffff"
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

            var colorIndicator = """
                <!-- Color Indicator with iOS styling -->
                <rect x="${x + 8}" y="${cardY + 8}" width="20" height="20" 
                      fill="${segment.color}" 
                      rx="4" ry="4"/>
                <rect x="${x + 8}" y="${cardY + 8}" width="20" height="20" 
                      fill="url(#iosColorIndicator_$id)" 
                      rx="4" ry="4"/>
            """.trimIndent()
            var fill = "url(#iosCardBg_$id)"
            if(isPdf) {
                if(darkMode) {
                    fill = "#374151"
                } else {
                    fill = "#fcfcfc"
                }
                colorIndicator = """
                    <rect x="${x + 8}" y="${cardY + 8}" width="20" height="20" 
                      fill="${segment.color}" 
                      rx="4" ry="4"/>
                """.trimIndent()
            }

            legendBuilder.append("""
            <g class="ios-legend-card" data-segment="segment-$index">
                <!-- iOS Card Background -->
                <rect x="$x" y="$cardY" width="$cardWidth" height="$cardHeight" 
                      fill="$fill" 
                      stroke="url(#iosCardBorder_$id)" 
                      stroke-width="0.5"
                      rx="8" ry="8"
                      filter="url(#iosCardShadow_$id)"/>

                $colorIndicator

                <!-- Label Text -->
                <text x="${x + 36}" y="${cardY + 14}" 
                      font-family="${theme.fontFamily}" 
                      font-size="13" 
                      font-weight="500"
                      dominant-baseline="middle"
                      fill="${theme.primaryText}">
                    ${segment.segment.label}
                </text>

                <!-- Percentage Text -->
                <text x="${x + 36}" y="${cardY + 26}" 
                      font-family="${theme.fontFamily}" 
                      font-size="11" 
                      font-weight="400"
                      dominant-baseline="middle"
                      fill="${theme.secondaryText}">
                    ${formatDecimal(segment.percentage, 1)}%
                </text>

                <!-- Value Text (right aligned) -->
                <text x="${x + cardWidth - 8}" y="${cardY + 20}" 
                      font-family="${theme.fontFamily}" 
                      font-size="12" 
                      font-weight="600"
                      text-anchor="end"
                      dominant-baseline="middle"
                      fill="${theme.secondaryText}">
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
        val startAngleRad = startAngle * PI / 180.0
        val endAngleRad = endAngle * PI / 180.0

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

    /**
     * Convert input payload to simple CSV format (just label and value)
     */
    private fun payloadToSimpleCsv(pieData: List<PieSegment>): CsvResponse {
        val headers = listOf("Label", "Value")
        val rows = pieData.map { segment ->
            listOf(segment.label, segment.value.toString())
        }
        return CsvResponse(headers, rows)
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

fun main() {
    val payload = """legend=false
---
Product A | 30
Product B | 25
Product C | 20
Product D | 15
Product E | 10""".trimIndent()
    val pieChartImproved = PieChartImproved()
    val csvResponse = CsvResponse(mutableListOf(), mutableListOf())
    val svg = pieChartImproved.makePieSvg(payload, csvResponse, false, false)

    val f = File("gen/pie_not_donut.svg")
    f.writeBytes(svg.toByteArray())
}
