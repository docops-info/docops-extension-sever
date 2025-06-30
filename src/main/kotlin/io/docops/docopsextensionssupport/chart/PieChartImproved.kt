package io.docops.docopsextensionssupport.chart

import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.gradientFromColor

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
        if (segments.isEmpty()) {
            return "<svg width='$width' height='$height'><text x='${width/2}' y='${height/2}' text-anchor='middle'>No data</text></svg>"
        }

        val total = segments.sumOf { it.value }
        val pieRadius = if (showLegend) minOf(width, height) * 0.35 else minOf(width, height) * 0.45
        val centerX = width * 0.5
        // Adjust centerY to move the pie chart down when no legend is shown to avoid title overlap
        val centerY = if (showLegend) height * 0.5 else height * 0.55

        val svgBuilder = StringBuilder()

        // Define colors based on dark mode
        val backgroundColor = if (darkMode) "#1e293b" else "transparent"
        val textColor = if (darkMode) "#f8fafc" else "#000000"
        val segmentStrokeColor = if (darkMode) "#1e293b" else "white"
        val donutHoleColor = if (darkMode) "#1e293b" else "white"
        val donutBorderColor = if (darkMode) "#334155" else "#f0f0f0"
        val legendBorderColor = if (darkMode) "#475569" else "#ccc"

        val id = UUID.randomUUID().toString()
        svgBuilder.append("<svg width='$width' height='$height' xmlns='http://www.w3.org/2000/svg' id='ID_$id' preserveAspectRatio=\"xMidYMid meet\" viewBox=\"0 0 $width $height\">")

        // Add background if in dark mode
        if (darkMode) {
            svgBuilder.append("<rect width='$width' height='$height' fill='$backgroundColor' />")
        }

        // Add glass effect definitions
        svgBuilder.append("""
            <defs>
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

                <!-- Enhanced drop shadow filter for glass elements -->
                <filter id="glassDropShadow" x="-30%" y="-30%" width="160%" height="160%">
                    <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="rgba(0,0,0,0.2)"/>
                </filter>

                <!-- Frosted glass blur filter -->
                <filter id="glassBlur" x="-10%" y="-10%" width="120%" height="120%">
                    <feGaussianBlur in="SourceGraphic" stdDeviation="1.5" />
                </filter>

                <!-- Glass border gradient -->
                <linearGradient id="glassBorder" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.6);stop-opacity:1" />
                    <stop offset="50%" style="stop-color:rgba(255,255,255,0.2);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                </linearGradient>
            </defs>
        """.trimIndent())

        // Add CSS styles for hover effects and glass styling if enabled
        if (enableHoverEffects) {
            svgBuilder.append("""
                <style>
                    .pie-segment {
                        transition: transform 0.2s, filter 0.2s;
                        transform-origin: ${centerX}px ${centerY}px;
                        filter: url(#glassDropShadow);
                    }
                    .pie-segment:hover {
                        transform: scale(1.05);
                        filter: url(#glassDropShadow) brightness(1.1);
                        cursor: pointer;
                    }
                    .pie-segment-overlay {
                        pointer-events: none;
                        opacity: 0.7;
                    }
                    .pie-segment-highlight {
                        pointer-events: none;
                        opacity: 0.5;
                        transition: opacity 0.2s ease;
                    }
                    .pie-segment:hover + .pie-segment-highlight {
                        opacity: 0.8;
                    }
                    .legend-item {
                        transition: all 0.2s ease;
                    }
                    .legend-item:hover {
                        cursor: pointer;
                        transform: translateY(-2px);
                    }
                    .legend-item:hover rect {
                        stroke-width: 2;
                        filter: brightness(1.1);
                    }
                    .legend-item:hover text {
                        font-weight: bold;
                    }
                    .legend-box {
                        filter: url(#glassDropShadow);
                        transition: all 0.2s ease;
                    }
                    .donut-hole {
                        filter: url(#glassBlur);
                    }
                </style>
            """.trimIndent())
        }

        // Add title with glass effect
        // Create a glass-style background for the title
        val titleWidth = min(title.length * 14 + 40, (width * 0.8).toInt())
        val titleHeight = 40
        val titleBgX = width/2 - titleWidth/2
        val titleBgY = 10

        // Title background with glass effect
        svgBuilder.append("<rect x='$titleBgX' y='$titleBgY' width='$titleWidth' height='$titleHeight' ")
        svgBuilder.append("rx='10' ry='10' fill='${if(darkMode) "#2a3a4a" else "rgba(245,245,247,0.85)"}' ")
        svgBuilder.append("filter='url(#glassDropShadow)' />")

        // Glass overlay for title background
        svgBuilder.append("<rect x='$titleBgX' y='$titleBgY' width='$titleWidth' height='$titleHeight' ")
        svgBuilder.append("rx='10' ry='10' fill='url(#glassOverlay)' class='glass-overlay' />")

        // Top highlight for title
        svgBuilder.append("<rect x='${titleBgX + 5}' y='${titleBgY + 5}' width='${titleWidth - 10}' height='15' ")
        svgBuilder.append("rx='5' ry='5' fill='url(#glassHighlight)' class='glass-highlight' opacity='0.4' />")

        // Title text with enhanced styling
        svgBuilder.append("<text x='${width/2}' y='35' font-family='Arial' font-size='20' text-anchor='middle' ")
        svgBuilder.append("font-weight='bold' fill='$textColor' style='letter-spacing: -0.02em;'>$title</text>")

        // Draw pie segments
        var startAngle = 0.0
        val segmentsWithAngles = mutableListOf<SegmentWithAngles>()

        segments.forEachIndexed { index, segment ->
            val percentage = segment.value / total
            val angle = percentage * 360.0
            val endAngle = startAngle + angle

            // Calculate coordinates for the arc
            val startRadians = Math.toRadians(startAngle)
            val endRadians = Math.toRadians(endAngle)

            val x1 = centerX + pieRadius * sin(startRadians)
            val y1 = centerY - pieRadius * cos(startRadians)
            val x2 = centerX + pieRadius * sin(endRadians)
            val y2 = centerY - pieRadius * cos(endRadians)

            // Determine if the arc should be drawn as a large arc (> 180 degrees)
            val largeArcFlag = if (angle > 180.0) 1 else 0

            // Choose color for this segment
            val colorIndex = index % colors.size
            val color = segment.color ?: colors[colorIndex]

            // Create path for the segment with glass effect
            svgBuilder.append("<g>")

            // Base segment with glass effect
            if (enableHoverEffects) {
                svgBuilder.append("<path id='segment-$index' class='pie-segment' d='M$centerX,$centerY L$x1,$y1 A$pieRadius,$pieRadius 0 $largeArcFlag,1 $x2,$y2 Z' ")
            } else {
                svgBuilder.append("<path d='M$centerX,$centerY L$x1,$y1 A$pieRadius,$pieRadius 0 $largeArcFlag,1 $x2,$y2 Z' ")
            }
            svgBuilder.append("fill='$color' stroke='$segmentStrokeColor' stroke-width='1'>")
            // Add title element for tooltip
            svgBuilder.append("<title>${segment.label}: ${segment.value} (${String.format("%.1f", percentage * 100)}%)</title>")
            svgBuilder.append("</path>")

            // Glass overlay for the segment
            svgBuilder.append("<path class='pie-segment-overlay' d='M$centerX,$centerY L$x1,$y1 A$pieRadius,$pieRadius 0 $largeArcFlag,1 $x2,$y2 Z' ")
            svgBuilder.append("fill='url(#glassOverlay)' stroke='url(#glassBorder)' stroke-width='1' />")

            // Calculate midpoint for highlight
            val midAngleRadians = Math.toRadians(startAngle + angle / 2)
            val highlightRadius = pieRadius * 0.85
            val highlightX = centerX + highlightRadius * sin(midAngleRadians)
            val highlightY = centerY - highlightRadius * cos(midAngleRadians)

            // Add radial highlight for glass effect
            svgBuilder.append("<circle class='pie-segment-highlight' cx='$highlightX' cy='$highlightY' r='${pieRadius * 0.15}' ")
            svgBuilder.append("fill='url(#glassRadial)' />")

            svgBuilder.append("</g>")

            // Store segment info for labels
            segmentsWithAngles.add(SegmentWithAngles(segment, startAngle, endAngle, percentage, color))

            // Add percentage label inside the segment if enabled
            if (showPercentages) {
                val midAngleRadians = Math.toRadians(startAngle + angle / 2)
                val labelRadius = pieRadius * 0.7
                val labelX = centerX + labelRadius * sin(midAngleRadians)
                val labelY = centerY - labelRadius * cos(midAngleRadians)

                val percentText = "%.1f%%".format(percentage * 100)
                svgBuilder.append("<text x='$labelX' y='$labelY' font-family='Arial' font-size='12' fill='white' ")
                svgBuilder.append("text-anchor='middle' dominant-baseline='middle' font-weight='bold'>$percentText</text>")
            }

            startAngle = endAngle
        }

        // Add donut hole if donut chart is enabled
        if (isDonut) {
            // Create a circle in the center to make the "hole" with glass effect
            val holeRadius = pieRadius * 0.55

            // Base donut hole
            svgBuilder.append("<circle cx='$centerX' cy='$centerY' r='$holeRadius' fill='$donutHoleColor' class='donut-hole' />")

            // Glass overlay for donut hole
            svgBuilder.append("<circle cx='$centerX' cy='$centerY' r='$holeRadius' fill='url(#glassOverlay)' class='donut-hole' />")

            // Border with glass effect
            svgBuilder.append("<circle cx='$centerX' cy='$centerY' r='$holeRadius' fill='none' stroke='url(#glassBorder)' stroke-width='1' />")

            // Top highlight for shine
            svgBuilder.append("<ellipse cx='${centerX - holeRadius * 0.2}' cy='${centerY - holeRadius * 0.2}' rx='${holeRadius * 0.7}' ry='${holeRadius * 0.3}' fill='url(#glassHighlight)' opacity='0.6' />")

            // Small radial highlight for realistic light effect
            svgBuilder.append("<circle cx='${centerX - holeRadius * 0.3}' cy='${centerY - holeRadius * 0.3}' r='${holeRadius * 0.15}' fill='url(#glassRadial)' opacity='0.7' />")
        }

        // Add legend if enabled
        if (showLegend) {
            // Calculate legend position to avoid overlap with pie chart
            // Position legend on the right side with adequate spacing
            val legendX = centerX + pieRadius + 30 // 30px margin between pie and legend
            val legendY = height * 0.5 - (segments.size * 25) / 2 // Center legend vertically

            // Create a glass-style background for the legend
            val legendWidth = 180
            val legendHeight = segments.size * 25 + 20
            val legendBgX = legendX - 25
            val legendBgY = legendY - 20

            // Legend background with glass effect
            svgBuilder.append("<rect x='$legendBgX' y='$legendBgY' width='$legendWidth' height='$legendHeight' ")
            svgBuilder.append("rx='10' ry='10' fill='${if(darkMode) "#2a3a4a" else "rgba(245,245,247,0.85)"}' ")
            svgBuilder.append("class='legend-box' />")

            // Glass overlay for legend background
            svgBuilder.append("<rect x='$legendBgX' y='$legendBgY' width='$legendWidth' height='$legendHeight' ")
            svgBuilder.append("rx='10' ry='10' fill='url(#glassOverlay)' class='glass-overlay' />")

            // Top highlight for legend
            svgBuilder.append("<rect x='${legendBgX + 5}' y='${legendBgY + 5}' width='${legendWidth - 10}' height='20' ")
            svgBuilder.append("rx='5' ry='5' fill='url(#glassHighlight)' class='glass-highlight' opacity='0.4' />")

            svgBuilder.append("<g class='legend'>")

            segmentsWithAngles.forEachIndexed { index, segmentWithAngles ->
                val segment = segmentWithAngles.segment
                val yPos = legendY + index * 25

                // Wrap each legend item in a group for hover effects if enabled
                if (enableHoverEffects) {
                    svgBuilder.append("<g class='legend-item' data-segment='segment-$index' onmouseover='highlightSegment(\"segment-$index\")' onmouseout='resetSegment(\"segment-$index\")'>")
                } else {
                    svgBuilder.append("<g>")
                }

                // Legend color box with glass effect and tooltip
                svgBuilder.append("<rect x='${legendX - 15}' y='${yPos - 10}' width='15' height='15' ")
                svgBuilder.append("rx='3' ry='3' fill='${segmentWithAngles.color}' stroke='url(#glassBorder)' filter='url(#glassDropShadow)'>")
                svgBuilder.append("<title>${segment.label}: ${segment.value} (${String.format("%.1f", segmentWithAngles.percentage * 100)}%)</title>")
                svgBuilder.append("</rect>")

                // Glass overlay for color box
                svgBuilder.append("<rect x='${legendX - 15}' y='${yPos - 10}' width='15' height='15' ")
                svgBuilder.append("rx='3' ry='3' fill='url(#glassOverlay)' class='glass-overlay' />")

                // Small highlight for color box
                svgBuilder.append("<rect x='${legendX - 13}' y='${yPos - 8}' width='11' height='5' ")
                svgBuilder.append("rx='2' ry='2' fill='url(#glassHighlight)' class='glass-highlight' opacity='0.5' />")

                // Legend text with enhanced styling
                val percentText = "%.1f%%".format(segmentWithAngles.percentage * 100)
                svgBuilder.append("<text x='$legendX' y='$yPos' font-family='Arial' font-size='12' text-anchor='start' fill='$textColor' ")
                svgBuilder.append("style='font-weight: 500; letter-spacing: -0.01em;'>")
                svgBuilder.append("${segment.label} ($percentText)</text>")

                svgBuilder.append("</g>")
            }

            svgBuilder.append("</g>")
        }

        // Add JavaScript for interactive effects if enabled
        if (enableHoverEffects) {
            svgBuilder.append("""
                <script type="text/javascript">
                    function highlightSegment(id) {
                        var segment = document.getElementById(id);
                        if (segment) {
                            segment.setAttribute('transform', 'scale(1.05)');
                            segment.setAttribute('filter', 'brightness(1.1)');
                        }
                    }

                    function resetSegment(id) {
                        var segment = document.getElementById(id);
                        if (segment) {
                            segment.setAttribute('transform', 'scale(1)');
                            segment.setAttribute('filter', 'none');
                        }
                    }

                    // Add hover effects to pie segments to highlight legend items
                    var segments = document.querySelectorAll('.pie-segment');
                    segments.forEach(function(segment) {
                        segment.addEventListener('mouseover', function() {
                            var id = segment.getAttribute('id');
                            var legendItem = document.querySelector('.legend-item[data-segment="' + id + '"]');
                            if (legendItem) {
                                legendItem.querySelector('text').setAttribute('font-weight', 'bold');
                                legendItem.querySelector('rect').setAttribute('stroke-width', '2');
                            }
                        });

                        segment.addEventListener('mouseout', function() {
                            var id = segment.getAttribute('id');
                            var legendItem = document.querySelector('.legend-item[data-segment="' + id + '"]');
                            if (legendItem) {
                                legendItem.querySelector('text').setAttribute('font-weight', 'normal');
                                legendItem.querySelector('rect').setAttribute('stroke-width', '1');
                            }
                        });
                    });
                </script>
            """.trimIndent())
        }

        svgBuilder.append("</svg>")
        return svgBuilder.toString()
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
