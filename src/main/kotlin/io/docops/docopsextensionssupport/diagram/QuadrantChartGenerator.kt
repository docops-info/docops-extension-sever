package io.docops.docopsextensionssupport.diagram

import java.io.File

class QuadrantChartGenerator {

    fun generateSVG(data: List<QuadrantPoint>, config: QuadrantConfig = QuadrantConfig()): String {
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
            appendLine(generateStyles())

            // Create chart elements
            svg.apply {
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

        // Add overall background with iOS-style rounded corners
        element("rect", mapOf(
            "x" to 30,  // Keep as Int since it's a literal
            "y" to 30,  // Keep as Int since it's a literal
            "width" to (config.width - 60),  // Both Int - works fine
            "height" to (config.height - 60), // Both Int - works fine
            "class" to "chart-background",
            "filter" to "url(#ios-shadow)"
        ))

        group(mapOf("class" to "quadrant-backgrounds")) {
            // Top-left quadrant (Critical)
            element("rect", mapOf(
                "x" to config.margin,
                "y" to config.margin,
                "width" to quadrantWidth,
                "height" to quadrantHeight,
                "class" to "quadrant-bg quadrant-top-left"
            ))

            // Top-right quadrant (High Priority)
            element("rect", mapOf(
                "x" to centerX,
                "y" to config.margin,
                "width" to quadrantWidth,
                "height" to quadrantHeight,
                "class" to "quadrant-bg quadrant-top-right"
            ))

            // Bottom-left quadrant (Low Priority)
            element("rect", mapOf(
                "x" to config.margin,
                "y" to centerY,
                "width" to quadrantWidth,
                "height" to quadrantHeight,
                "class" to "quadrant-bg quadrant-bottom-left"
            ))

            // Bottom-right quadrant (Medium Priority)
            element("rect", mapOf(
                "x" to centerX,
                "y" to centerY,
                "width" to quadrantWidth,
                "height" to quadrantHeight,
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

    private fun generateStyles(): String = """
    <defs>
        <!-- Modern iOS-style gradients with updated colors -->
        <linearGradient id="quadrant-tl-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#FF2D55;stop-opacity:0.85"/>
            <stop offset="100%" style="stop-color:#FF5E80;stop-opacity:0.7"/>
        </linearGradient>
        <linearGradient id="quadrant-tr-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#5856D6;stop-opacity:0.85"/>
            <stop offset="100%" style="stop-color:#7A78E2;stop-opacity:0.7"/>
        </linearGradient>
        <linearGradient id="quadrant-bl-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#34C759;stop-opacity:0.85"/>
            <stop offset="100%" style="stop-color:#5DDE7C;stop-opacity:0.7"/>
        </linearGradient>
        <linearGradient id="quadrant-br-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#007AFF;stop-opacity:0.85"/>
            <stop offset="100%" style="stop-color:#5AC8FA;stop-opacity:0.7"/>
        </linearGradient>

        <!-- Enhanced glassmorphism blur filter -->
        <filter id="glass-blur" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur in="SourceGraphic" stdDeviation="2"/>
            <feColorMatrix type="matrix" values="1 0 0 0 1   0 1 0 0 1   0 0 1 0 1   0 0 0 0.3 0"/>
        </filter>

        <!-- Modern iOS drop shadow for depth -->
        <filter id="ios-shadow" x="-50%" y="-50%" width="200%" height="200%">
            <feDropShadow dx="0" dy="1" stdDeviation="2" flood-color="rgba(0,0,0,0.15)"/>
        </filter>

        <!-- Enhanced glow effect for data points -->
        <filter id="glow" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
            <feFlood flood-color="rgba(255,255,255,0.7)" result="floodFill"/>
            <feComposite in="floodFill" in2="coloredBlur" operator="in" result="coloredBlurAlpha"/>
            <feMerge>
                <feMergeNode in="coloredBlurAlpha"/>
                <feMergeNode in="SourceGraphic"/>
            </feMerge>
        </filter>

        <!-- Subtle inner shadow for depth -->
        <filter id="inner-shadow" x="-50%" y="-50%" width="200%" height="200%">
            <feOffset dx="0" dy="1"/>
            <feGaussianBlur stdDeviation="1" result="offset-blur"/>
            <feComposite operator="out" in="SourceGraphic" in2="offset-blur" result="inverse"/>
            <feFlood flood-color="rgba(0,0,0,0.05)" flood-opacity="1" result="color"/>
            <feComposite operator="in" in="color" in2="inverse" result="shadow"/>
            <feComposite operator="over" in="shadow" in2="SourceGraphic"/>
        </filter>
    </defs>

    <style>
    <![CDATA[
        .chart-background {
            fill: #F2F2F7;
            rx: 20;
            ry: 20;
            filter: url(#ios-shadow);
        }

        .quadrant-bg {
            stroke: rgba(255,255,255,0.5);
            stroke-width: 1;
            filter: url(#glass-blur);
            rx: 16;
            ry: 16;
            transition: all 0.3s ease;
        }

        .quadrant-bg:hover {
            filter: url(#glass-blur);
            stroke: rgba(255,255,255,0.8);
            stroke-width: 1.5;
        }

        .quadrant-top-right { 
            fill: url(#quadrant-tr-gradient);
        }
        .quadrant-top-left { 
            fill: url(#quadrant-tl-gradient);
        }
        .quadrant-bottom-left { 
            fill: url(#quadrant-bl-gradient);
        }
        .quadrant-bottom-right { 
            fill: url(#quadrant-br-gradient);
        }

        .axis-line {
            stroke: rgba(142,142,147,0.5);
            stroke-width: 1;
            stroke-linecap: round;
            stroke-dasharray: 4, 4;
        }

        .axis-label {
            fill: #3A3A3C;
            font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', system-ui, sans-serif;
            font-size: 12px;
            font-weight: 500;
            letter-spacing: -0.01em;
        }

        .chart-title {
            fill: #1C1C1E;
            font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', system-ui, sans-serif;
            font-size: 24px;
            font-weight: 700;
            letter-spacing: -0.02em;
            text-shadow: 0px 1px 2px rgba(0,0,0,0.05);
        }

        .data-point {
            stroke: rgba(255,255,255,0.95);
            stroke-width: 2;
            cursor: pointer;
            filter: url(#ios-shadow);
            transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
        }

        .data-point:hover {
            filter: url(#glow);
            transform: scale(1.15);
            stroke-width: 3;
        }

        .data-point.quadrant-top-right { 
            fill: #5856D6;
        }
        .data-point.quadrant-top-left { 
            fill: #FF2D55;
        }
        .data-point.quadrant-bottom-left { 
            fill: #34C759;
        }
        .data-point.quadrant-bottom-right { 
            fill: #007AFF;
        }

        .point-group .point-label,
        .point-group .point-category,
        .point-group .point-label-card {
            opacity: 0;
            transition: opacity 0.3s ease, transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
            transform: translateY(5px);
            pointer-events: none;
        }

        .point-group:hover .point-label,
        .point-group:hover .point-category,
        .point-group:hover .point-label-card {
            opacity: 1;
            transform: translateY(0);
        }

        .point-label {
            fill: #1C1C1E;
            font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif;
            font-size: 12px;
            font-weight: 600;
            letter-spacing: -0.01em;
        }

        .point-category {
            fill: rgba(60,60,67,0.6);
            font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif;
            font-size: 10px;
            font-weight: 500;
            letter-spacing: -0.01em;
        }

        .quadrant-label {
            fill: rgba(28,28,30,0.8);
            font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif;
            font-size: 11px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .quadrant-card {
            fill: rgba(255,255,255,0.35);
            stroke: rgba(255,255,255,0.5);
            stroke-width: 0.5;
            rx: 10;
            ry: 10;
            filter: url(#glass-blur);
        }

        .point-label-card {
            fill: rgba(255,255,255,0.7);
            stroke: rgba(255,255,255,0.8);
            stroke-width: 0.5;
            rx: 12;
            ry: 12;
            filter: url(#ios-shadow);
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }

        @media (prefers-reduced-motion: reduce) {
            .data-point, .quadrant-bg, .point-label, .point-category, .point-label-card, .point-group * {
                transition: none !important;
                animation: none !important;
                transform: none !important;
            }

            .point-group:hover .point-label,
            .point-group:hover .point-category,
            .point-group:hover .point-label-card {
                opacity: 1;
            }

            .data-point:hover {
                transform: scale(1.05) !important;
            }
        }
    ]]>
    </style>
""".trimIndent()

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
