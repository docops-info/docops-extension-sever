package io.docops.docopsextensionssupport.diagram

import java.io.File

class QuadrantChartGenerator {

    fun generateSVG(data: List<QuadrantPoint>, config: QuadrantConfig = QuadrantConfig(), useDark: Boolean = false): String {
        val xRange = calculateRange(data.map { it.x })
        val yRange = calculateRange(data.map { it.y })
        val normalizedData = normalizeData(data, xRange, yRange, config)

        val svg = SvgBuilder()

        return buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
            append("width=\"${config.width}\" height=\"${config.height}\" ")
            appendLine("viewBox=\"0 0 ${config.width} ${config.height}\">")

            // Add styles
            appendLine(generateStyles(useDark))

            // Create chart elements
            svg.apply {
                // Background Layer
                element("rect", mapOf(
                    "width" to config.width,
                    "height" to config.height,
                    "class" to "chart-outer-bg",
                    "rx" to 24
                ))
                element("rect", mapOf(
                    "width" to config.width,
                    "height" to config.height,
                    "class" to "grid-pattern",
                    "rx" to 24
                ))

                // Background quadrants
                drawQuadrantBackgrounds(config)

                // Axes
                drawAxes(config, xRange, yRange)

                // Data points
                drawDataPoints(normalizedData, config)

                // Labels and title
                drawLabels(config)
            }

            appendLine(svg.build())
            appendLine("</svg>")
        }
    }

    private fun calculateRange(values: List<Double>): DataRange {
        val min = values.minOrNull() ?: 0.0
        val max = values.maxOrNull() ?: 1.0
        val padding = (max - min) * 0.1 // 10% padding
        return DataRange(min - padding, max + padding)
    }

    private fun normalizeData(
        data: List<QuadrantPoint>,
        xRange: DataRange,
        yRange: DataRange,
        config: QuadrantConfig
    ): List<QuadrantPoint> {
        val chartWidth = config.width - 2 * config.margin
        val chartHeight = config.height - 2 * config.margin

        return data.map { point ->
            val normalizedX = config.margin +
                    ((point.x - xRange.min) / (xRange.max - xRange.min)) * chartWidth
            val normalizedY = config.height - config.margin -
                    ((point.y - yRange.min) / (yRange.max - yRange.min)) * chartHeight

            point.copy(x = normalizedX, y = normalizedY)
        }
    }

    private fun SvgBuilder.drawQuadrantBackgrounds(config: QuadrantConfig) {
        val centerX = config.width / 2.0
        val centerY = config.height / 2.0
        val quadrantWidth = (config.width - 2 * config.margin) / 2.0
        val quadrantHeight = (config.height - 2 * config.margin) / 2.0



        group(mapOf("class" to "quadrant-backgrounds")) {
            // Top-left quadrant (Strategic)
            element("rect", mapOf(
                "x" to config.margin,
                "y" to config.margin,
                "width" to quadrantWidth,
                "height" to quadrantHeight,
                "rx" to 12,
                "class" to "quadrant-bg quadrant-top-left"
            ))

            // Top-right quadrant (High Impact)
            element("rect", mapOf(
                "x" to centerX,
                "y" to config.margin,
                "width" to quadrantWidth,
                "height" to quadrantHeight,
                "rx" to 12,
                "class" to "quadrant-bg quadrant-top-right"
            ))

            // Bottom-left quadrant (Fill-ins)
            element("rect", mapOf(
                "x" to config.margin,
                "y" to centerY,
                "width" to quadrantWidth,
                "height" to quadrantHeight,
                "rx" to 12,
                "class" to "quadrant-bg quadrant-bottom-left"
            ))

            // Bottom-right quadrant (Thankless)
            element("rect", mapOf(
                "x" to centerX,
                "y" to centerY,
                "width" to quadrantWidth,
                "height" to quadrantHeight,
                "rx" to 12,
                "class" to "quadrant-bg quadrant-bottom-right"
            ))
        }
    }
    private fun SvgBuilder.drawAxes(config: QuadrantConfig, xRange: DataRange, yRange: DataRange) {
        val centerX = config.width / 2.0
        val centerY = config.height / 2.0

        group(mapOf("class" to "axes")) {
            // Vertical center line
            element("line", mapOf(
                "x1" to centerX,
                "y1" to config.margin,
                "x2" to centerX,
                "y2" to config.height - config.margin,
                "class" to "axis-line"
            ))

            // Horizontal center line
            element("line", mapOf(
                "x1" to config.margin,
                "y1" to centerY,
                "x2" to config.width - config.margin,
                "y2" to centerY,
                "class" to "axis-line"
            ))

            // Axis labels
            element("text", mapOf(
                "x" to config.width / 2,
                "y" to config.height - 10,
                "text-anchor" to "middle",
                "class" to "axis-label"
            ), config.xAxisLabel)

            element("text", mapOf(
                "x" to 15,
                "y" to config.height / 2,
                "text-anchor" to "middle",
                "transform" to "rotate(-90, 15, ${config.height / 2})",
                "class" to "axis-label"
            ), config.yAxisLabel)
        }
    }

    private fun SvgBuilder.drawDataPoints(data: List<QuadrantPoint>, config: QuadrantConfig) {
        group(mapOf("class" to "data-points")) {
            data.forEachIndexed { index, point ->
                val quadrant = classifyQuadrant(point, config)

                // Group for each data point and its label to enable hover effects
                group(mapOf("class" to "point-group")) {
                    // Create iOS-style data point with larger radius and better positioning
                    element("circle", mapOf(
                        "cx" to String.format("%.2f", point.x),
                        "cy" to String.format("%.2f", point.y),
                        "r" to 9, // Slightly larger radius for better touch targets
                        "class" to "data-point $quadrant",
                        "data-label" to point.label,
                        "data-category" to (point.category ?: "")
                    ))

                    // Add text label with better positioning and iOS-style card background
                    val labelX = point.x + 14.0  // Ensure Double type
                    val labelY = point.y - 14.0  // Ensure Double type
                    val labelWidth = point.label.length * 6.5 + 20.0  // Ensure Double type
                    val labelHeight = 24.0  // Ensure Double type

                    // Label background card
                    element("rect", mapOf(
                        "x" to String.format("%.2f", labelX - 10.0),
                        "y" to String.format("%.2f", labelY - 16.0),
                        "width" to labelWidth,
                        "height" to labelHeight,
                        "class" to "quadrant-card point-label-card"
                    ))

                    // Label text
                    element("text", mapOf(
                        "x" to String.format("%.2f", labelX),
                        "y" to String.format("%.2f", labelY - 2.0),
                        "class" to "point-label"
                    ), point.label)

                    // Add category label if available
                    if (point.category != null && point.category.isNotEmpty()) {
                        element("text", mapOf(
                            "x" to String.format("%.2f", labelX),
                            "y" to String.format("%.2f", labelY + 12.0),
                            "class" to "point-category"
                        ), point.category)
                    }
                }
            }
        }
    }

    private fun SvgBuilder.drawLabels(config: QuadrantConfig) {
        // Title with iOS styling
        element("text", mapOf(
            "x" to config.width / 2,
            "y" to 50,
            "text-anchor" to "middle",
            "class" to "chart-title"
        ), config.title)

        // Quadrant labels with iOS-style cards
        val centerX = config.width / 2.0
        val centerY = config.height / 2.0
        val labelOffset = 24

        config.quadrantLabels.forEach { (quadrant, label) ->
            val (x, y) = when (quadrant) {
                "top-left" -> Pair(config.margin + labelOffset, config.margin + labelOffset + 5)
                "top-right" -> Pair(centerX + labelOffset, config.margin + labelOffset + 5)
                "bottom-left" -> Pair(config.margin + labelOffset, centerY + labelOffset + 5)
                "bottom-right" -> Pair(centerX + labelOffset, centerY + labelOffset + 5)
                else -> Pair(0.0, 0.0)  // Use Double literals for the else case
            }

            // Label background card
            val cardWidth = label.length * 7 + 20

            element("rect", mapOf(
                "x" to (x.toDouble() - 10),  // Ensure x is treated as Double
                "y" to (y.toDouble() - 12),  // Ensure y is treated as Double
                "width" to cardWidth,
                "height" to 18,
                "class" to "quadrant-card"
            ))

            // Label text
            element("text", mapOf(
                "x" to x,
                "y" to y,
                "class" to "quadrant-label"
            ), label)
        }
    }

    private fun classifyQuadrant(point: QuadrantPoint, config: QuadrantConfig): String {
        val centerX = config.width / 2.0
        val centerY = config.height / 2.0

        return when {
            point.x >= centerX && point.y <= centerY -> "quadrant-top-right"
            point.x < centerX && point.y <= centerY -> "quadrant-top-left"
            point.x < centerX && point.y > centerY -> "quadrant-bottom-left"
            else -> "quadrant-bottom-right"
        }
    }

    private fun generateStyles(useDark: Boolean): String {
        val bgColor = if (useDark) "#0B0E14" else "#FAFAFA"
        val titleColor = if (useDark) "#FFFFFF" else "#1C1C1E"
        val subtitleColor = if (useDark) "#8E9196" else "#636366"
        val gridOpacity = if (useDark) "0.15" else "0.05"
        val axisColor = if (useDark) "#2C2C2E" else "#E5E5EA"

        return """
        <defs>
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@700&amp;family=JetBrains+Mono:wght@500&amp;display=swap');
                
                .chart-outer-bg { fill: $bgColor; }
                .grid-pattern { fill: url(#dotGrid); opacity: $gridOpacity; }
                
                .chart-title { 
                    fill: $titleColor; 
                    font-family: 'Outfit', sans-serif; 
                    font-size: 32px; 
                    letter-spacing: -0.5px;
                }
                .chart-subtitle { 
                    fill: $subtitleColor; 
                    font-family: 'JetBrains Mono', monospace; 
                    font-size: 14px; 
                    text-transform: uppercase;
                    letter-spacing: 2px;
                }
                
                .quadrant-bg { stroke-width: 1.5; fill-opacity: 0.05; }
                .quadrant-top-left { stroke: #BF5AF2; fill: #BF5AF2; }
                .quadrant-top-right { stroke: #32D74B; fill: #32D74B; }
                .quadrant-bottom-left { stroke: #5AC8FA; fill: #5AC8FA; }
                .quadrant-bottom-right { stroke: #FF375F; fill: #FF375F; }
                
                .quadrant-label {
                    font-family: 'Outfit', sans-serif;
                    font-size: 12px;
                    font-weight: 700;
                    letter-spacing: 1px;
                }
                .top-left-text { fill: #BF5AF2; }
                .top-right-text { fill: #32D74B; }
                .bottom-left-text { fill: #5AC8FA; }
                .bottom-right-text { fill: #FF375F; }

                .axis-line {
                    stroke: $axisColor;
                    stroke-width: 1;
                    stroke-dasharray: 8, 4;
                }

                .axis-label {
                    fill: $subtitleColor;
                    font-family: 'JetBrains Mono', monospace;
                    font-size: 10px;
                    font-weight: bold;
                }

                .data-point {
                    stroke: #FFFFFF;
                    stroke-width: 2;
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                }
                .data-point.quadrant-top-left { fill: #BF5AF2; }
                .data-point.quadrant-top-right { fill: #32D74B; }
                .data-point.quadrant-bottom-left { fill: #5AC8FA; }
                .data-point.quadrant-bottom-right { fill: #FF375F; }

                .point-label {
                    fill: $titleColor;
                    font-family: 'JetBrains Mono', monospace;
                    font-size: 11px;
                }
            </style>

            <pattern id="dotGrid" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                <circle cx="2" cy="2" r="1" fill="${if (useDark) "#FFFFFF" else "#000000"}" />
            </pattern>

            <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur stdDeviation="4" result="blur" />
                <feComposite in="SourceGraphic" in2="blur" operator="over" />
            </filter>
        </defs>
        """.trimIndent()
    }

}

fun main() {
    val generator = QuadrantChartGenerator()

    val data = listOf(
        QuadrantPoint(0.8, 0.9, "High Performer"),
        QuadrantPoint(0.2, 0.8, "High Potential"),
        QuadrantPoint(0.1, 0.2, "Underperformer"),
        QuadrantPoint(0.9, 0.3, "Solid Contributor")
    )

    val config = QuadrantConfig(
        title = "Employee Performance Matrix",
        xAxisLabel = "Performance Score",
        yAxisLabel = "Potential Score"
    )

    val svgContent = generator.generateSVG(data, config)

    // Save to file or return in HTTP response
    File("gen/quadrant-chart.svg").writeText(svgContent)
}
