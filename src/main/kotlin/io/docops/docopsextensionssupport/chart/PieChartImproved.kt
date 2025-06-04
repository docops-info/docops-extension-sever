package io.docops.docopsextensionssupport.chart

import kotlin.math.cos
import kotlin.math.sin

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
            isDonut
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
        isDonut: Boolean
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
        svgBuilder.append("<svg width='$width' height='$height' xmlns='http://www.w3.org/2000/svg'>")

        // Add CSS styles for hover effects if enabled
        if (enableHoverEffects) {
            svgBuilder.append("""
                <style>
                    .pie-segment {
                        transition: transform 0.2s, filter 0.2s;
                        transform-origin: ${centerX}px ${centerY}px;
                    }
                    .pie-segment:hover {
                        transform: scale(1.05);
                        filter: brightness(1.1);
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
        svgBuilder.append("<text x='${width/2}' y='30' font-family='Arial' font-size='20' text-anchor='middle' font-weight='bold'>$title</text>")

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

            // Create path for the segment with class for hover effects if enabled
            svgBuilder.append("<g>")
            if (enableHoverEffects) {
                svgBuilder.append("<path id='segment-$index' class='pie-segment' d='M$centerX,$centerY L$x1,$y1 A$pieRadius,$pieRadius 0 $largeArcFlag,1 $x2,$y2 Z' ")
            } else {
                svgBuilder.append("<path d='M$centerX,$centerY L$x1,$y1 A$pieRadius,$pieRadius 0 $largeArcFlag,1 $x2,$y2 Z' ")
            }
            svgBuilder.append("fill='$color' stroke='white' stroke-width='1'>")
            // Add title element for tooltip
            svgBuilder.append("<title>${segment.label}: ${segment.value} (${String.format("%.1f", percentage * 100)}%)</title>")
            svgBuilder.append("</path>")
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
            // Create a white circle in the center to make the "hole"
            // The hole size is typically 50-60% of the pie radius
            val holeRadius = pieRadius * 0.55
            svgBuilder.append("<circle cx='$centerX' cy='$centerY' r='$holeRadius' fill='white' />")

            // Optional: Add a subtle border to the hole
            svgBuilder.append("<circle cx='$centerX' cy='$centerY' r='$holeRadius' fill='none' stroke='#f0f0f0' stroke-width='1' />")
        }

        // Add legend if enabled
        if (showLegend) {
            // Calculate legend position to avoid overlap with pie chart
            // Position legend on the right side with adequate spacing
            val legendX = centerX + pieRadius + 30 // 30px margin between pie and legend
            val legendY = height * 0.5 - (segments.size * 25) / 2 // Center legend vertically

            svgBuilder.append("<g class='legend'>")

            segmentsWithAngles.forEachIndexed { index, segmentWithAngles ->
                val segment = segmentWithAngles.segment
                val yPos = legendY + index * 25

                // Wrap each legend item in a group for hover effects if enabled
                if (enableHoverEffects) {
                    svgBuilder.append("<g class='legend-item' data-segment='segment-$index' onmouseover='highlightSegment(\"segment-$index\")' onmouseout='resetSegment(\"segment-$index\")'>");
                } else {
                    svgBuilder.append("<g>");
                }

                // Legend color box with tooltip
                svgBuilder.append("<rect x='${legendX - 15}' y='${yPos - 10}' width='15' height='15' ")
                svgBuilder.append("fill='${segmentWithAngles.color}' stroke='#ccc'>")
                svgBuilder.append("<title>${segment.label}: ${segment.value} (${String.format("%.1f", segmentWithAngles.percentage * 100)}%)</title>")
                svgBuilder.append("</rect>")

                // Legend text
                val percentText = "%.1f%%".format(segmentWithAngles.percentage * 100)
                svgBuilder.append("<text x='$legendX' y='$yPos' font-family='Arial' font-size='12' text-anchor='start'>")
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