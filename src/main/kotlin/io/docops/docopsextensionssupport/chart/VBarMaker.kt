package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.support.generateRectanglePathData
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import kotlin.math.max

class VBarMaker {
    private var fontColor = ""
    private var height = 400
    private var width = 800
    private val xAxisStart = 80
    private val yAxisStart = 80
    private val xAxisEnd = 720
    private val yAxisEnd = 320
    private val barWidth = 60
    private val barSpacing = 100 // Center-to-center spacing between bars

    fun makeVerticalBar(bar: Bar): String {
        bar.sorted()
        fontColor = determineTextColor(bar.display.baseColor)
        val sb = StringBuilder()
        sb.append(head(bar))
        sb.append(addDefs(bar))
        sb.append(makeBackground(bar))
        sb.append(addGrid(bar))
        sb.append(addAxes(bar))
        sb.append(addAxisLabels(bar))
        sb.append(addBars(bar))
        sb.append(addTitle(bar))
        sb.append(addLegend(bar))
        sb.append(tail())
        return joinXmlLines(sb.toString())
    }

    private fun addBars(bar: Bar): String {
        val sb = StringBuilder()

        // Calculate the maximum value for scaling
        val maxValue = bar.series.maxOf { it.value }

        // Define text colors based on dark mode
        val labelColor = if (bar.display.useDark) "#e5e7eb" else "#666"
        val valueColor = if (bar.display.useDark) "#f9fafb" else "#333"

        // Create each bar with its own gradient
        bar.series.forEachIndexed { index, series ->
            val barHeight = (series.value / maxValue) * (yAxisEnd - yAxisStart)
            val barX = xAxisStart + (index * barSpacing) + 40
            val barY = yAxisEnd - barHeight
            val gradientId = "gradient${index + 1}"

            // Add the gradient definition
            sb.append("""
                <defs>
                    <linearGradient id="$gradientId" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stop-color="${getColorForIndex(index, 0)}"/>
                        <stop offset="100%" stop-color="${getColorForIndex(index, 1)}"/>
                    </linearGradient>
                </defs>
            """.trimIndent())

            // Add the bar with animation
            sb.append("""
                <rect x="$barX" y="$barY" width="$barWidth" height="$barHeight" rx="6" ry="6" fill="url(#$gradientId)">
                    <animate attributeName="height" from="0" to="$barHeight" dur="1s" fill="freeze"/>
                    <animate attributeName="y" from="$yAxisEnd" to="$barY" dur="1s" fill="freeze"/>
                </rect>
                <text x="${barX + barWidth/2}" y="${yAxisEnd + 20}" font-family="Arial, sans-serif" font-size="14" text-anchor="middle" fill="$labelColor">${series.label}</text>
                <text x="${barX + barWidth/2}" y="${barY - 10}" font-family="Arial, sans-serif" font-size="12" font-weight="bold" text-anchor="middle" fill="$valueColor">${series.value.toInt()}</text>
            """.trimIndent())
        }

        return sb.toString()
    }

    private fun getColorForIndex(index: Int, position: Int): String {
        // Return colors that match the bar.svg gradient pairs
        return when (index % 6) {
            0 -> if (position == 0) "#4361ee" else "#3a0ca3" // Blue to dark blue
            1 -> if (position == 0) "#4cc9f0" else "#4361ee" // Light blue to blue
            2 -> if (position == 0) "#f72585" else "#b5179e" // Pink to purple
            3 -> if (position == 0) "#7209b7" else "#560bad" // Purple to dark purple
            4 -> if (position == 0) "#f77f00" else "#d62828" // Orange to red
            5 -> if (position == 0) "#2a9d8f" else "#264653" // Teal to dark teal
            else -> if (position == 0) "#4361ee" else "#3a0ca3" // Default
        }
    }

    private fun addGrid(bar: Bar): String {
        val sb = StringBuilder()

        // Define grid line color based on dark mode
        val gridLineColor = if (bar.display.useDark) "#374151" else "#eee"

        // Add horizontal grid lines
        for (y in 260 downTo 80 step 60) {
            sb.append("""
                <line x1="$xAxisStart" y1="$y" x2="$xAxisEnd" y2="$y" stroke="$gridLineColor" stroke-width="1" stroke-dasharray="5,5"/>
            """.trimIndent())
        }

        return sb.toString()
    }

    private fun addAxes(bar: Bar): String {
        val axisColor = if (bar.display.useDark) "#9ca3af" else "#ccc"
        return """
            <!-- Y-axis -->
            <line x1="$xAxisStart" y1="$yAxisStart" x2="$xAxisStart" y2="$yAxisEnd" stroke="$axisColor" stroke-width="2"/>

            <!-- X-axis -->
            <line x1="$xAxisStart" y1="$yAxisEnd" x2="$xAxisEnd" y2="$yAxisEnd" stroke="$axisColor" stroke-width="2"/>
        """.trimIndent()
    }

    private fun addAxisLabels(bar: Bar): String {
        val sb = StringBuilder()

        // Define text color based on dark mode
        val textColor = if (bar.display.useDark) "#e5e7eb" else "#666"

        // Y-axis labels
        sb.append("""
            <text x="${xAxisStart - 10}" y="$yAxisEnd" font-family="Arial, sans-serif" font-size="12" text-anchor="end" fill="$textColor">0</text>
            <text x="${xAxisStart - 10}" y="260" font-family="Arial, sans-serif" font-size="12" text-anchor="end" fill="$textColor">25</text>
            <text x="${xAxisStart - 10}" y="200" font-family="Arial, sans-serif" font-size="12" text-anchor="end" fill="$textColor">50</text>
            <text x="${xAxisStart - 10}" y="140" font-family="Arial, sans-serif" font-size="12" text-anchor="end" fill="$textColor">75</text>
            <text x="${xAxisStart - 10}" y="80" font-family="Arial, sans-serif" font-size="12" text-anchor="end" fill="$textColor">100</text>
        """.trimIndent())

        return sb.toString()
    }

    private fun makeBackground(bar: Bar): String {
        val backgroundColor = if (bar.display.useDark) "#1f2937" else "#f8f9fa"
        return """
            <rect width="100%" height="100%" fill="$backgroundColor" rx="15" ry="15"/>
        """.trimIndent()
    }

    private fun head(bar: Bar): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="${width/DISPLAY_RATIO_16_9}" height="${height/DISPLAY_RATIO_16_9}" viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg">
        """.trimIndent()
    }

    private fun tail(): String {
        return "</svg>"
    }

    private fun addTitle(bar: Bar): String {
        val titleColor = if (bar.display.useDark) "#f9fafb" else "#333"
        return """
            <!-- Title -->
            <text x="${width/2}" y="40" font-family="Arial, sans-serif" font-size="24" font-weight="bold" text-anchor="middle" fill="$titleColor">${bar.title.escapeXml()}</text>
        """.trimIndent()
    }

    private fun addLegend(bar: Bar): String {
        val legendBgColor = if (bar.display.useDark) "#374151" else "#f8f9fa"
        val legendStrokeColor = if (bar.display.useDark) "#4b5563" else "#ddd"
        val legendTextColor = if (bar.display.useDark) "#e5e7eb" else "#666"
        return """
            <!-- Legend -->
            <rect x="300" y="360" width="200" height="30" rx="15" ry="15" fill="$legendBgColor" stroke="$legendStrokeColor" stroke-width="1"/>
            <text x="400" y="380" font-family="Arial, sans-serif" font-size="14" text-anchor="middle" fill="$legendTextColor">${bar.xLabel ?: "Performance"}</text>
        """.trimIndent()
    }

    private fun addDefs(bar: Bar): String {
        return """
            <defs>
                <!-- Drop shadow filter for bars -->
                <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                    <feOffset in="blur" dx="3" dy="3" result="offsetBlur"/>
                    <feComponentTransfer in="offsetBlur" result="shadow">
                        <feFuncA type="linear" slope="0.3"/>
                    </feComponentTransfer>
                    <feMerge>
                        <feMergeNode in="shadow"/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>

                <!-- Glow filter for hover effect -->
                <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
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
                    rect {
                        transition: all 0.3s ease;
                    }
                    rect:hover {
                        filter: url(#glow);
                        transform: scale(1.05);
                        cursor: pointer;
                    }
                </style>
            </defs>
        """.trimIndent()
    }
}

// Main function to test the VBarMaker implementation
fun main() {
    // Create sample data
    val bar = Bar(
        title = "Quarterly Performance",
        yLabel = "Revenue ($)",
        xLabel = "Quarter",
        series = mutableListOf(
            Series("Q1", 65.0),
            Series("Q2", 85.0),
            Series("Q3", 55.0),
            Series("Q4", 78.0),
            Series("Q5", 62.0),
            Series("Q6", 90.0)
        ),
        display = BarDisplay(baseColor = "#4361ee", useDark = false)
    )

    // Generate the vertical bar chart
    val svg = VBarMaker().makeVerticalBar(bar)

    // Save the chart to a file
    val outfile = java.io.File("gen/vertical_bar_chart.svg")
    outfile.writeBytes(svg.toByteArray())

    println("Vertical bar chart saved to ${outfile.absolutePath}")
}
