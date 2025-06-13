package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.joinXmlLines

class VBarMaker {
    private var fontColor = ""
    private var height = 500  // Increased height to accommodate labels
    private var width = 900   // Increased width to provide more space
    private val xAxisStart = 100  // Increased to provide more space for y-axis labels
    private val yAxisStart = 80
    private val xAxisEnd = 800  // Adjusted to maintain proportions
    private val yAxisEnd = 380  // Increased to provide more space for x-axis labels
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

        // Calculate available width for bars
        val availableWidth = xAxisEnd - xAxisStart

        // Adjust spacing based on number of bars to prevent overlapping
        val adjustedBarSpacing = if (bar.series.size > 6) {
            availableWidth / (bar.series.size + 1)
        } else {
            barSpacing
        }

        // Create each bar with its own gradient
        bar.series.forEachIndexed { index, series ->
            val barHeight = (series.value / maxValue) * (yAxisEnd - yAxisStart)
            val barX = xAxisStart + (index * adjustedBarSpacing) + (adjustedBarSpacing - barWidth) / 2
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
                <!-- Create wrapped label text -->
                <text x="${barX + barWidth/2}" y="${yAxisEnd + 25}" font-family="Arial, sans-serif" font-size="12" text-anchor="middle" fill="$labelColor">
                    ${createWrappedLabel(series.label, barWidth)}
                </text>
                <text x="${barX + barWidth/2}" y="${barY - 10}" font-family="Arial, sans-serif" font-size="12" font-weight="bold" text-anchor="middle" fill="$valueColor">${series.value.toInt()}</text>
            """.trimIndent())
        }

        // Add X-axis label
        if (bar.xLabel != null && bar.xLabel.isNotEmpty()) {
            sb.append("""
                <text x="${(xAxisStart + xAxisEnd) / 2}" y="${yAxisEnd + 50}" font-family="Arial, sans-serif" font-size="14" font-weight="bold" text-anchor="middle" fill="$labelColor">${bar.xLabel.escapeXml()}</text>
            """.trimIndent())
        }

        return sb.toString()
    }

    private fun getColorForIndex(index: Int, position: Int): String {
        // Define color palettes exactly matching bar.svg gradients
        val modernColors = listOf(
            Triple("#4361ee", "#3a0ca3", "#4361ee"), // Bar 1: Blue to Purple
            Triple("#4cc9f0", "#4361ee", "#4cc9f0"), // Bar 2: Light Blue to Blue
            Triple("#f72585", "#b5179e", "#f72585"), // Bar 3: Pink to Purple
            Triple("#7209b7", "#560bad", "#7209b7"), // Bar 4: Purple to Dark Purple
            Triple("#f77f00", "#d62828", "#f77f00"), // Bar 5: Orange to Red
            Triple("#2a9d8f", "#264653", "#2a9d8f")  // Bar 6: Teal to Dark Blue
        )

        val colorSet = modernColors[index % modernColors.size]
        return if (position == 0) colorSet.first else colorSet.second
    }

    private fun addGrid(bar: Bar): String {
        val sb = StringBuilder()

        // Define grid line color based on dark mode
        val gridLineColor = if (bar.display.useDark) "#374151" else "#eee"

        // Calculate y-axis tick positions
        val yAxisHeight = yAxisEnd - yAxisStart
        val tickCount = 5
        val tickSpacing = yAxisHeight / (tickCount - 1)

        // Add horizontal grid lines
        for (i in 0 until tickCount) {
            val yPos = yAxisEnd - (i * tickSpacing)
            sb.append("""
                <line x1="$xAxisStart" y1="$yPos" x2="$xAxisEnd" y2="$yPos" stroke="$gridLineColor" stroke-width="1" stroke-dasharray="5,5"/>
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

        // Calculate y-axis tick positions
        val yAxisHeight = yAxisEnd - yAxisStart
        val tickCount = 5
        val tickSpacing = yAxisHeight / (tickCount - 1)

        // Y-axis labels
        for (i in 0 until tickCount) {
            val yPos = yAxisEnd - (i * tickSpacing)
            val value = (i * 25) // 0, 25, 50, 75, 100
            sb.append("""
                <text x="${xAxisStart - 15}" y="${yPos + 4}" font-family="Arial, sans-serif" font-size="12" text-anchor="end" fill="$textColor">$value</text>
                <line x1="${xAxisStart - 5}" y1="$yPos" x2="$xAxisStart" y2="$yPos" stroke="$textColor" stroke-width="1"/>
            """.trimIndent())
        }

        // Add Y-axis label
        if (bar.yLabel != null && bar.yLabel.isNotEmpty()) {
            sb.append("""
                <text x="30" y="${(yAxisStart + yAxisEnd) / 2}" font-family="Arial, sans-serif" font-size="14" font-weight="bold" text-anchor="middle" fill="$textColor" transform="rotate(-90, 30, ${(yAxisStart + yAxisEnd) / 2})">${bar.yLabel.escapeXml()}</text>
            """.trimIndent())
        }

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
            <svg width="${width/DISPLAY_RATIO_16_9}" height="${height/DISPLAY_RATIO_16_9}" viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg" id="id_${bar.display.id}">
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
        // We're now adding the x-axis label directly in the addBars method
        // This method is kept for potential future enhancements like adding a color legend
        return ""
    }

    /**
     * Creates wrapped label text using tspan elements for SVG
     * @param label The original label text
     * @param maxWidth The maximum width to determine when to wrap
     * @return SVG tspan elements with wrapped text
     */
    private fun createWrappedLabel(label: String?, maxWidth: Int): String {
        // Handle null or empty labels
        if (label.isNullOrEmpty()) {
            return ""
        }

        // If label is short enough, return it as is
        if (label.length <= 10) {
            return label.escapeXml()
        }

        // Split label into words
        val words = label.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        // Create lines with approximately equal length, targeting 2 lines for most labels
        val targetLineLength = (label.length / 2) + 2

        for (word in words) {
            if (currentLine.length + word.length > targetLineLength && currentLine.isNotEmpty()) {
                lines.add(currentLine.toString().trim())
                currentLine = StringBuilder(word)
            } else {
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            }
        }

        // Add the last line if not empty
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString().trim())
        }

        // Create tspan elements
        val result = StringBuilder()
        lines.forEachIndexed { index, line ->
            val dy = if (index == 0) "0" else "1.2em"
            // Don't specify x attribute to inherit from parent text element
            result.append("<tspan dy=\"$dy\" text-anchor=\"middle\">${line.escapeXml()}</tspan>")
        }

        return result.toString()
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
                   #id_${bar.display.id} rect {
                        transition: all 0.3s ease;
                    }
                    #id_${bar.display.id} rect:hover {
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
    // Create sample data with long labels to test overlapping
    val bar = Bar(
        title = "Quarterly Performance with Long Labels",
        yLabel = "Revenue in Thousands of Dollars ($)",
        xLabel = "Quarters with Extended Descriptions",
        series = mutableListOf(
            Series("Q1 - First Quarter", 65.0),
            Series("Q2 - Second Quarter", 85.0),
            Series("Q3 - Third Quarter", 55.0),
            Series("Q4 - Fourth Quarter", 78.0),
            Series("Q5 - Fifth Quarter", 62.0),
            Series("Q6 - Sixth Quarter", 90.0)
        ),
        display = BarDisplay(baseColor = "#4361ee", useDark = false)
    )

    // Generate the vertical bar chart
    val svg = VBarMaker().makeVerticalBar(bar)

    // Save the chart to a file
    val outfile = java.io.File("gen/vertical_bar_chart.svg")
    outfile.writeBytes(svg.toByteArray())

    println("Vertical bar chart saved to ${outfile.absolutePath}")

    // Create another test with more bars to test spacing
    val barWithMoreSeries = Bar(
        title = "Monthly Performance with Many Bars",
        yLabel = "Revenue in Thousands of Dollars ($)",
        xLabel = "Months of the Year",
        series = mutableListOf(
            Series("January", 65.0),
            Series("February", 85.0),
            Series("March", 55.0),
            Series("April", 78.0),
            Series("May", 62.0),
            Series("June", 90.0),
            Series("July", 75.0),
            Series("August", 80.0),
            Series("September", 70.0),
            Series("October", 85.0),
            Series("November", 60.0),
            Series("December", 95.0)
        ),
        display = BarDisplay(baseColor = "#4361ee", useDark = false)
    )

    // Generate the vertical bar chart with more bars
    val svgWithMoreBars = VBarMaker().makeVerticalBar(barWithMoreSeries)

    // Save the chart to a file
    val outfileWithMoreBars = java.io.File("gen/vertical_bar_chart_more_bars.svg")
    outfileWithMoreBars.writeBytes(svgWithMoreBars.toByteArray())

    println("Vertical bar chart with more bars saved to ${outfileWithMoreBars.absolutePath}")
}
