package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.textWidth
import io.docops.docopsextensionssupport.web.ShapeResponse
import java.util.*

/**
 * QuadrantMaker class is responsible for generating SVG for quadrant charts.
 *
 * @property chart The quadrant chart to render
 * @property useDark Whether to use dark mode
 * @property type The type of output (SVG, PDF, etc.)
 */
class QuadrantMaker(val chart: QuadrantChart, val useDark: Boolean = false, val type: String = "html") {
    private var bgColor = "#F8F9FA"
    private var textColor = "#374151"
    private var subtitleColor = "#6b7280"
    private var gridColor = "#e2e8f0"
    private var axisColor = "#475569"

    // Quadrant colors
    private val q1GradientStart = "#10b981"
    private val q1GradientEnd = "#059669"
    private val q2GradientStart = "#3b82f6"
    private val q2GradientEnd = "#1d4ed8"
    private val q3GradientStart = "#f59e0b"
    private val q3GradientEnd = "#d97706"
    private val q4GradientStart = "#ef4444"
    private val q4GradientEnd = "#dc2626"

    // Default dimensions
    private val width = 800f
    private val height = 600f
    private val margin = 80f
    private val chartWidth = width - 2 * margin
    private val chartHeight = height - 2 * margin
    private val halfWidth = chartWidth / 2
    private val halfHeight = chartHeight / 2

    /**
     * Generates the SVG for the quadrant chart.
     *
     * @param scale The scale factor for the SVG
     * @return A ShapeResponse containing the SVG
     */
    fun makeQuadrantImage(scale: Float = 1.0f): ShapeResponse {
        if (useDark) {
            bgColor = "#17242b"
            textColor = "#F3F4F6"
            subtitleColor = "#9CA3AF"
            gridColor = "#374151"
            axisColor = "#9CA3AF"
        }

        val sb = StringBuilder()
        val id = UUID.randomUUID().toString()

        // Start SVG
        sb.append(head(scale, id))

        // Add definitions (gradients, filters)
        sb.append(defs())

        // Background
        sb.append("""<rect width="$width" height="$height" fill="url(#bgGradient)"/>""")

        // Chart container with shadow
        sb.append("""<rect x="$margin" y="$margin" width="$chartWidth" height="$chartHeight" fill="white" rx="12" filter="url(#shadow)"/>""")

        // Quadrant backgrounds
        sb.append(quadrantBackgrounds())

        // Grid lines
        sb.append(gridLines())

        // Main axes
        sb.append(mainAxes())

        // Quadrant labels
        sb.append(quadrantLabels())

        // Data points
        sb.append(dataPoints())

        // Axis labels
        sb.append(axisLabels())

        // Chart title
        sb.append(chartTitle())

        // Legend
        sb.append(legend())

        // End SVG
        sb.append("</svg>")

        return ShapeResponse(shapeSvg = sb.toString(), height = height, width = width)
    }

    /**
     * Generates the SVG header.
     */
    private fun head(scale: Float, id: String): String {
        val id = UUID.randomUUID().toString()
        return """
            <svg id="quad_$id" width="${width * scale}" height="${height * scale}" viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMidYMid meet">
        """.trimIndent()
    }

    /**
     * Generates the SVG definitions (gradients, filters).
     */
    private fun defs(): String {
        return """
            <defs>
                <linearGradient id="bgGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#f8fafc;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#e2e8f0;stop-opacity:1" />
                </linearGradient>

                <linearGradient id="q1Gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:$q1GradientStart;stop-opacity:0.15" />
                    <stop offset="100%" style="stop-color:$q1GradientEnd;stop-opacity:0.25" />
                </linearGradient>

                <linearGradient id="q2Gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:$q2GradientStart;stop-opacity:0.15" />
                    <stop offset="100%" style="stop-color:$q2GradientEnd;stop-opacity:0.25" />
                </linearGradient>

                <linearGradient id="q3Gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:$q3GradientStart;stop-opacity:0.15" />
                    <stop offset="100%" style="stop-color:$q3GradientEnd;stop-opacity:0.25" />
                </linearGradient>

                <linearGradient id="q4Gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:$q4GradientStart;stop-opacity:0.15" />
                    <stop offset="100%" style="stop-color:$q4GradientEnd;stop-opacity:0.25" />
                </linearGradient>

                <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="#64748b" flood-opacity="0.15"/>
                </filter>

                <filter id="glow">
                    <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                    <feMerge>
                        <feMergeNode in="coloredBlur"/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>
            </defs>
        """.trimIndent()
    }

    /**
     * Generates the quadrant backgrounds.
     */
    private fun quadrantBackgrounds(): String {
        val midX = margin + halfWidth
        val midY = margin + halfHeight

        return """
            <rect x="$midX" y="$margin" width="$halfWidth" height="$halfHeight" fill="url(#q1Gradient)" rx="12" ry="0"/>

            <rect x="$margin" y="$margin" width="$halfWidth" height="$halfHeight" fill="url(#q2Gradient)" rx="12" ry="0"/>

            <rect x="$margin" y="$midY" width="$halfWidth" height="$halfHeight" fill="url(#q3Gradient)" rx="12" ry="0"/>

            <rect x="$midX" y="$midY" width="$halfWidth" height="$halfHeight" fill="url(#q4Gradient)" rx="12" ry="0"/>
        """.trimIndent()
    }

    /**
     * Generates the grid lines.
     */
    private fun gridLines(): String {
        val sb = StringBuilder()
        sb.append("""
            <g stroke="$gridColor" stroke-width="1" opacity="0.6">
        """.trimIndent())

        // Vertical grid lines
        val vStep = halfWidth / 3
        for (i in 1..2) {
            val x = margin + i * vStep
            sb.append("""
                <line x1="$x" y1="$margin" x2="$x" y2="${margin + chartHeight}"/>
            """.trimIndent())
        }

        for (i in 1..2) {
            val x = margin + halfWidth + i * vStep
            sb.append("""
                <line x1="$x" y1="$margin" x2="$x" y2="${margin + chartHeight}"/>
            """.trimIndent())
        }

        sb.append("""
        """.trimIndent())

        // Horizontal grid lines
        val hStep = halfHeight / 3
        for (i in 1..2) {
            val y = margin + i * hStep
            sb.append("""
                <line x1="$margin" y1="$y" x2="${margin + chartWidth}" y2="$y"/>
            """.trimIndent())
        }

        for (i in 1..2) {
            val y = margin + halfHeight + i * hStep
            sb.append("""
                <line x1="$margin" y1="$y" x2="${margin + chartWidth}" y2="$y"/>
            """.trimIndent())
        }

        sb.append("""
            </g>
        """.trimIndent())

        return sb.toString()
    }

    /**
     * Generates the main axes.
     */
    private fun mainAxes(): String {
        val midX = margin + halfWidth
        val midY = margin + halfHeight

        return """
            <g stroke="$axisColor" stroke-width="3">
                <line x1="$midX" y1="$margin" x2="$midX" y2="${margin + chartHeight}"/>
                <line x1="$margin" y1="$midY" x2="${margin + chartWidth}" y2="$midY"/>
            </g>
        """.trimIndent()
    }

    /**
     * Generates the quadrant labels.
     */
    private fun quadrantLabels(): String {
        val midX = margin + halfWidth
        val midY = margin + halfHeight
        val q1X = midX + halfWidth / 2
        val q2X = margin + halfWidth / 2
        val q3X = margin + halfWidth / 2
        val q4X = midX + halfWidth / 2
        val labelY = margin + 30
        val descY = margin + 60
        val bottomLabelY = margin + chartHeight - 30
        val bottomDescY = midY + 60

        return """
            <rect x="${q1X - 80}" y="${labelY - 20}" width="160" height="32" fill="$q1GradientStart" rx="16" opacity="0.9"/>
            <text x="$q1X" y="$labelY" text-anchor="middle" fill="white" font-family="Arial, sans-serif" font-size="14" font-weight="bold">${chart.q1Label}</text>
            <text x="$q1X" y="$descY" text-anchor="middle" fill="$q1GradientEnd" font-family="Arial, sans-serif" font-size="12" font-weight="500">${chart.q1Description}</text>

            <rect x="${q2X - 80}" y="${labelY - 20}" width="160" height="32" fill="$q2GradientStart" rx="16" opacity="0.9"/>
            <text x="$q2X" y="$labelY" text-anchor="middle" fill="white" font-family="Arial, sans-serif" font-size="14" font-weight="bold">${chart.q2Label}</text>
            <text x="$q2X" y="$descY" text-anchor="middle" fill="$q2GradientEnd" font-family="Arial, sans-serif" font-size="12" font-weight="500">${chart.q2Description}</text>

            <rect x="${q3X - 80}" y="${bottomLabelY - 20}" width="160" height="32" fill="$q3GradientStart" rx="16" opacity="0.9"/>
            <text x="$q3X" y="$bottomLabelY" text-anchor="middle" fill="white" font-family="Arial, sans-serif" font-size="14" font-weight="bold">${chart.q3Label}</text>
            <text x="$q3X" y="$bottomDescY" text-anchor="middle" fill="$q3GradientEnd" font-family="Arial, sans-serif" font-size="12" font-weight="500">${chart.q3Description}</text>

            <rect x="${q4X - 80}" y="${bottomLabelY - 20}" width="160" height="32" fill="$q4GradientStart" rx="16" opacity="0.9"/>
            <text x="$q4X" y="$bottomLabelY" text-anchor="middle" fill="white" font-family="Arial, sans-serif" font-size="14" font-weight="bold">${chart.q4Label}</text>
            <text x="$q4X" y="$bottomDescY" text-anchor="middle" fill="$q4GradientEnd" font-family="Arial, sans-serif" font-size="12" font-weight="500">${chart.q4Description}</text>
        """.trimIndent()
    }

    /**
     * Generates the data points.
     */
    private fun dataPoints(): String {
        val sb = StringBuilder()

        // Data structure to track label positions for collision detection
        data class LabelPosition(val x: Float, val y: Float, val width: Int, val height: Int)
        val labelPositions = mutableListOf<LabelPosition>()

        // Sort points by y-coordinate to prioritize points at the top
        val sortedPoints = chart.points.sortedByDescending { it.y }

        // Map x and y values from 0-100 to chart coordinates
        sortedPoints.forEach { point ->
            val x = margin + (point.x / 100f) * chartWidth
            // Invert y-axis (0 is bottom, 100 is top)
            val y = margin + chartHeight - (point.y / 100f) * chartHeight

            // Determine color based on quadrant if not specified
            val color = point.color ?: getColorForPosition(point.x, point.y)

            sb.append("""
                <circle cx="$x" cy="$y" r="${point.size}" fill="$color" filter="url(#glow)"/>
            """.trimIndent())

            // Add label if provided
            if (point.label.isNotEmpty()) {
                // Calculate label width
                val labelWidth = point.label.textWidth("Arial", 12)
                val labelHeight = 14 // Approximate height of the text

                // Default position
                var labelX = x + point.size + 5
                var labelY = y + 5

                // Check for collisions with existing labels
                var collision = true
                val positions = listOf(
                    Pair(x + point.size + 5, y + 5),           // Right
                    Pair(x - point.size - 5 - labelWidth, y + 5), // Left
                    Pair(x - labelWidth / 2, y - point.size - 10), // Top
                    Pair(x - labelWidth / 2, y + point.size + 15)  // Bottom
                )

                // Try different positions to avoid collisions
                for (pos in positions) {
                    val testX = pos.first
                    val testY = pos.second

                    // Check if this position collides with any existing label
                    collision = labelPositions.any { existing ->
                        testX < existing.x + existing.width &&
                        testX + labelWidth > existing.x &&
                        testY - labelHeight < existing.y + existing.height &&
                        testY > existing.y - labelHeight
                    }

                    // Also check if the label would go outside the chart area
                    val outsideChart = testX < margin || 
                                      testX + labelWidth > margin + chartWidth ||
                                      testY - labelHeight < margin ||
                                      testY > margin + chartHeight

                    if (!collision && !outsideChart) {
                        labelX = testX
                        labelY = testY
                        break
                    }
                }

                // Add the label position to our tracking list
                labelPositions.add(LabelPosition(labelX, labelY - labelHeight, labelWidth, labelHeight))

                // Determine text-anchor based on position
                val textAnchor = when {
                    labelX < x -> "end"
                    labelX > x + point.size -> "start"
                    else -> "middle"
                }

                // Add the label
                sb.append("""
                    <text x="$labelX" y="$labelY" text-anchor="$textAnchor" fill="$textColor" font-family="Arial, sans-serif" font-size="12">${point.label}</text>
                """.trimIndent())

                // Add description if available
                if (point.description.isNotEmpty()) {
                    val descY = labelY + 14 // Position description below label
                    sb.append("""
                        <text x="$labelX" y="$descY" text-anchor="$textAnchor" fill="$subtitleColor" font-family="Arial, sans-serif" font-size="10" font-style="italic">${point.description}</text>
                    """.trimIndent())
                }
            }
        }

        return sb.toString()
    }

    /**
     * Determines the color for a point based on its position in the quadrant.
     */
    private fun getColorForPosition(x: Float, y: Float): String {
        return when {
            x >= 50 && y >= 50 -> q1GradientEnd  // Q1: High Impact, Low Effort
            x < 50 && y >= 50 -> q2GradientEnd   // Q2: High Impact, High Effort
            x < 50 && y < 50 -> q3GradientEnd    // Q3: Low Impact, Low Effort
            else -> q4GradientEnd                // Q4: Low Impact, High Effort
        }
    }

    /**
     * Generates the axis labels.
     */
    private fun axisLabels(): String {
        return """
            <text x="${width / 2}" y="${height - 25}" text-anchor="middle" fill="$textColor" font-family="Arial, sans-serif" font-size="16" font-weight="600">${chart.xAxisLabel}</text>
            <text x="20" y="${height / 2}" text-anchor="middle" fill="$textColor" font-family="Arial, sans-serif" font-size="16" font-weight="600" transform="rotate(-90, 20, ${height / 2})">${chart.yAxisLabel}</text>

            <text x="${margin + halfWidth / 2}" y="${height - 35}" text-anchor="middle" fill="$subtitleColor" font-family="Arial, sans-serif" font-size="12">Low</text>
            <text x="${margin + halfWidth + halfWidth / 2}" y="${height - 35}" text-anchor="middle" fill="$subtitleColor" font-family="Arial, sans-serif" font-size="12">High</text>

            <text x="45" y="${margin + chartHeight}" text-anchor="middle" fill="$subtitleColor" font-family="Arial, sans-serif" font-size="12">Low</text>
            <text x="45" y="${margin + 15}" text-anchor="middle" fill="$subtitleColor" font-family="Arial, sans-serif" font-size="12">High</text>
        """.trimIndent()
    }

    /**
     * Generates the chart title.
     */
    private fun chartTitle(): String {
        return """
            <text x="${width / 2}" y="40" text-anchor="middle" fill="$textColor" font-family="Arial, sans-serif" font-size="24" font-weight="bold">${chart.title}</text>
            <text x="${width / 2}" y="60" text-anchor="middle" fill="$subtitleColor" font-family="Arial, sans-serif" font-size="14">${chart.subtitle}</text>
        """.trimIndent()
    }

    /**
     * Generates the legend.
     */
    private fun legend(): String {
        return """
            <g transform="translate(50, 350)">
                <rect x="0" y="0" width="20" height="60" fill="white" rx="4" stroke="#e5e7eb" stroke-width="1"/>
                <text x="30" y="15" fill="$textColor" font-family="Arial, sans-serif" font-size="11" font-weight="600">Legend:</text>
                <circle cx="10" cy="25" r="3" fill="$q1GradientStart"/>
                <text x="25" y="29" fill="$textColor" font-family="Arial, sans-serif" font-size="10">Quick Wins</text>
                <circle cx="10" cy="38" r="3" fill="$q2GradientStart"/>
                <text x="25" y="42" fill="$textColor" font-family="Arial, sans-serif" font-size="10">Major Projects</text>
                <circle cx="10" cy="51" r="3" fill="$q3GradientStart"/>
                <text x="25" y="55" fill="$textColor" font-family="Arial, sans-serif" font-size="10">Minor Tasks</text>
            </g>
        """.trimIndent()
    }
}
