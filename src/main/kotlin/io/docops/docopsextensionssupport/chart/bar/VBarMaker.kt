package io.docops.docopsextensionssupport.chart.bar

import io.docops.docopsextensionssupport.chart.ChartColors
import io.docops.docopsextensionssupport.chart.NiceScale
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.util.BackgroundHelper
import java.io.File
import kotlin.math.min

class VBarMaker {
    private var isModern = false
    private var fontColor = ""
    private var height = 500  // Increased height to accommodate labels
    private var width = 900   // Increased width to provide more space
    private var xAxisStart = 100  // Increased to provide more space for y-axis labels
    private var yAxisStart = 80
    private var xAxisEnd = 800  // Adjusted to maintain proportions
    private var yAxisEnd = 380  // Increased to provide more space for x-axis labels
    private var barWidth = 60
    private var barSpacing = 100 // Center-to-center spacing between bars
    private var theme: DocOpsTheme = ThemeFactory.getThemeByName("modern_editorial", false)

    fun makeVerticalBar(bar: Bar, isPDf: Boolean): String {
        theme = if (bar.display.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(bar.display.theme, bar.display.useDark)
        } else {
            ThemeFactory.getThemeByName("modern_editorial", bar.display.useDark)
        }

        isModern = !isPDf && !theme.name.contains("Classic") && !theme.name.contains("Pro")

        if (isModern) {
            width = 960
            height = 560
            xAxisStart = 120
            xAxisEnd = 860
            yAxisStart = 150
            yAxisEnd = 450
        } else {
            width = 900
            height = 500
            xAxisStart = 100
            xAxisEnd = 800
            yAxisStart = 80
            yAxisEnd = 380
        }

        bar.sorted()
        fontColor = determineTextColor(bar.display.baseColor)
        val sb = StringBuilder()
        sb.append(head(bar))
        sb.append(addDefs(bar))
        if(isModern) {
            sb.append(makeModernBackground(bar))
        } else {
            sb.append(makeBackground(bar))
        }
        sb.append(addGrid(bar))
        sb.append(addAxes(bar))
        sb.append(addAxisLabels(bar))
        sb.append(addBars(bar, isPDf))
        sb.append(addTitle(bar))
        sb.append(addLegend(bar))
        sb.append(tail())
        return sb.toString()
    }

    private fun makeModernBackground(bar: Bar): String {
        return """
            <!-- Atmosphere -->
            <rect width="100%" height="100%" fill="var(--bg)"/>
            <circle cx="140" cy="80" r="220" fill="url(#bgGlow)"/>
            <rect x="36" y="36" width="${width - 72}" height="${height - 72}" rx="18" fill="var(--surface)"/>
        """.trimIndent()
    }

    private fun addModernBars(bar: Bar): String {
        val sb = StringBuilder()
        val maxValue = bar.series.maxOf { it.value }
        val niceScale = NiceScale(0.0, maxValue)
        val niceMax = niceScale.getNiceMax()

        val availableWidth = xAxisEnd - xAxisStart
        val barSpacing = availableWidth / bar.series.size
        val barWidth = barSpacing * 0.7

        bar.series.forEachIndexed { index, series ->
            val barHeight = (series.value / niceMax) * (yAxisEnd - yAxisStart)
            val barX = xAxisStart + (index * barSpacing) + (barSpacing - barWidth) / 2
            val isPeak = series.value == maxValue
            val fillColor = if (isPeak) "var(--accent)" else "var(--bar-${(index % theme.chartPalette.size) + 1})"

            sb.append("""
                <g class="bar-wrap" tabindex="0" aria-label="${series.label?.escapeXml() ?: ""}: ${bar.valueFmt(series.value)}">
                    <g transform="translate($barX $yAxisEnd)">
                        <g class="bar-inner anim-${index + 1}">
                            <rect x="0" y="-$barHeight" width="$barWidth" height="$barHeight" rx="${theme.cornerRadius}" fill="$fillColor"/>
                        </g>
                    </g>
                    <text class="x-label" x="${barX + barWidth / 2}" y="${yAxisEnd + 24}" text-anchor="middle">${series.label?.escapeXml() ?: ""}</text>
                    <text class="value-label val-${index + 1}" x="${barX + barWidth / 2}" y="${yAxisEnd - barHeight - 12}" text-anchor="middle">${bar.valueFmt(series.value)}</text>
                </g>
            """.trimIndent())
        }
        return sb.toString()
    }

    private fun addBars(bar: Bar, isPDf: Boolean): String {
        if (isModern && !isPDf) {
            return addModernBars(bar)
        }
        val sb = StringBuilder()

        // Calculate the maximum value for scaling
        val maxValue = bar.series.maxOf { it.value }
        val niceScale = NiceScale(0.0, maxValue)
        val niceMax = niceScale.getNiceMax()

        // Define text colors based on theme
        val labelColor = theme.secondaryText
        val valueColor = theme.primaryText

        // Calculate available width for bars
        val availableWidth = xAxisEnd - xAxisStart

        // Adjust spacing based on number of bars to prevent overlapping
        val adjustedBarSpacing = if (bar.series.size > 6) {
            availableWidth / (bar.series.size + 1)
        } else {
            barSpacing
        }

        // Calculate total width occupied by all bars and center them
        val totalBarsWidth = (bar.series.size - 1) * adjustedBarSpacing + barWidth
        val centerOffset = (availableWidth - totalBarsWidth) / 2

        // Create each bar with its own gradient
        bar.series.forEachIndexed { index, series ->
            val barHeight = (series.value / niceMax) * (yAxisEnd - yAxisStart)
            val barX = xAxisStart + centerOffset + (index * adjustedBarSpacing)
            val barY = yAxisEnd - barHeight
            val gradientId = "id_${bar.display.id}_$index"

            // Add the gradient definition
            /*val svgColor = theme.chartPalette[index % theme.chartPalette.size]
            sb.append("""
                <defs>
                    <linearGradient id="$gradientId" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stop-color="${svgColor.lighter()}"/>
                        <stop offset="100%" stop-color="${svgColor.darker()}"/>
                    </linearGradient>
                </defs>
            """.trimIndent())*/

            // Add the bar with glass effect
            if(!isPDf) {
                sb.append(
                    """
                    <g class="glass-bar">
                        <!-- Base rectangle with gradient -->
                        <rect x="$barX" y="$barY" width="$barWidth" height="$barHeight" rx="6" ry="6" 
                              fill="url(#$gradientId)"
                              filter="url(#glassDropShadow)"
                              stroke="rgba(255,255,255,0.3)" stroke-width="1">
                            <animate attributeName="height" from="0" to="$barHeight" dur="1s" fill="freeze"/>
                            <animate attributeName="y" from="$yAxisEnd" to="$barY" dur="1s" fill="freeze"/>
                        </rect>

                        <!-- Glass overlay with transparency -->
                        <rect x="$barX" y="$barY" width="$barWidth" height="$barHeight" rx="6" ry="6"
                              fill="url(#glassOverlay)"
                              filter="url(#glassBlur)">
                            <animate attributeName="height" from="0" to="$barHeight" dur="1s" fill="freeze"/>
                            <animate attributeName="y" from="$yAxisEnd" to="$barY" dur="1s" fill="freeze"/>
                        </rect>
                        <!-- Top highlight for shine -->
                        <rect x="${barX + 3}" y="${barY + 3}" width="${barWidth - 6}" height="${
                        min(
                            barHeight / 4,
                            20.0
                        )
                    }" rx="4" ry="4"
                              fill="url(#glassHighlight)">
                            <animate attributeName="y" from="${yAxisEnd - 3}" to="${barY + 3}" dur="1s" fill="freeze"/>
                        </rect>
                    </g>

                    <!-- Create wrapped label text -->
                    <text x="${barX + barWidth / 2}" y="${yAxisEnd + 25}" font-family="${theme.fontFamily}" font-size="12" text-anchor="middle" fill="$labelColor">
                        ${createWrappedLabel(series.label, barWidth)}
                    </text>
                    <text x="${barX + barWidth / 2}" y="${barY - 10}" font-family="${theme.fontFamily}" font-size="12" font-weight="bold" text-anchor="middle" fill="$valueColor">${series.value.toInt()}</text>
                """.trimIndent()
                )
            } else {
                val svgColor = theme.chartPalette[index % theme.chartPalette.size]
                sb.append("""
                        <g class="glass-bar">
                        <!-- Base rectangle with static PDF-safe fill -->
                        <rect x="$barX" y="$barY" width="$barWidth" height="$barHeight" rx="6" ry="6" 
                              fill="${svgColor.lighter()}"
                              stroke="${theme.accentColor}" stroke-opacity="0.3" stroke-width="1"/>
                        </g>
                        <!-- Create wrapped label text -->
                    <text x="${barX + barWidth / 2}" y="${yAxisEnd + 25}" font-family="${theme.fontFamily}" font-size="12" text-anchor="middle" fill="$labelColor">
                        ${createWrappedLabel(series.label, barWidth)}
                    </text>
                    <text x="${barX + barWidth / 2}" y="${barY - 10}" font-family="${theme.fontFamily}" font-size="12" font-weight="bold" text-anchor="middle" fill="$valueColor">${series.value.toInt()}</text>
                    """.trimIndent())
            }

        }

        // Add X-axis label
        if (bar.xLabel != null && bar.xLabel.isNotEmpty()) {
            sb.append("""
                    <text x="${(xAxisStart + xAxisEnd) / 2}" y="${yAxisEnd + 50}" font-family="${theme.fontFamily}" font-size="14" font-weight="bold" text-anchor="middle" fill="$labelColor">${bar.xLabel.escapeXml()}</text>
                """.trimIndent())
        }

        return sb.toString()
    }



    private fun addGrid(bar: Bar): String {
        val sb = StringBuilder()
        // Define grid line color based on theme
        val gridLineColor = theme.accentColor

        val maxValue = bar.series.maxOf { it.value }
        val niceScale = NiceScale(0.0, maxValue)
        val tickSpacing = niceScale.getTickSpacing()
        val niceMax = niceScale.getNiceMax()

        // Calculate y-axis tick positions
        val yAxisHeight = yAxisEnd - yAxisStart

        var currentVal = 0.0
        while (currentVal <= niceMax) {
            val yPos = yAxisEnd - (currentVal / niceMax * yAxisHeight)
            if (isModern) {
                sb.append("""<line class="grid" x1="$xAxisStart" y1="$yPos" x2="$xAxisEnd" y2="$yPos"/>""")
            } else {
                sb.append(
                    """
                    <line x1="$xAxisStart" y1="$yPos" x2="$xAxisEnd" y2="$yPos" stroke="$gridLineColor" stroke-width="1" stroke-dasharray="5,5" stroke-opacity="0.2"/>
                """.trimIndent()
                )
            }
            currentVal += tickSpacing
        }

        return sb.toString()
    }

    private fun addAxes(bar: Bar): String {
        if (isModern) {
            return """
                <!-- Axes -->
                <line class="axis" x1="$xAxisStart" y1="$yAxisStart" x2="$xAxisStart" y2="$yAxisEnd"/>
                <line class="axis" x1="$xAxisStart" y1="$yAxisEnd" x2="$xAxisEnd" y2="$yAxisEnd"/>
            """.trimIndent()
        }
        val axisColor = theme.accentColor
        return """
                <!-- Y-axis -->
                <line x1="$xAxisStart" y1="$yAxisStart" x2="$xAxisStart" y2="$yAxisEnd" stroke="$axisColor" stroke-width="2" stroke-opacity="0.5"/>

                <!-- X-axis -->
                <line x1="$xAxisStart" y1="$yAxisEnd" x2="$xAxisEnd" y2="$yAxisEnd" stroke="$axisColor" stroke-width="2" stroke-opacity="0.5"/>
            """.trimIndent()
    }

    private fun addAxisLabels(bar: Bar): String {
        val sb = StringBuilder()

        // Define text color based on theme
        val textColor = theme.secondaryText

        val maxValue = bar.series.maxOf { it.value }
        val niceScale = NiceScale(0.0, maxValue)
        val tickSpacing = niceScale.getTickSpacing()
        val niceMax = niceScale.getNiceMax()

        // Calculate y-axis tick positions
        val yAxisHeight = yAxisEnd - yAxisStart

        // Y-axis labels
        var currentVal = 0.0
        while (currentVal <= niceMax) {
            val yPos = yAxisEnd - (currentVal / niceMax * yAxisHeight)
            val label = bar.valueFmt(currentVal)
            if (isModern) {
                sb.append("""<text class="tick-text" x="${xAxisStart - 12}" y="${yPos + 4}" text-anchor="end">$label</text>""")
            } else {
                sb.append(
                    """
                    <text x="${xAxisStart - 15}" y="${yPos + 4}" font-family="${theme.fontFamily}" font-size="12" text-anchor="end" fill="$textColor">$label</text>
                    <line x1="${xAxisStart - 5}" y1="$yPos" x2="$xAxisStart" y2="$yPos" stroke="$textColor" stroke-width="1" stroke-opacity="0.6"/>
                """.trimIndent()
                )
            }
            currentVal += tickSpacing
        }

        // Add Y-axis label
        if (bar.yLabel != null && bar.yLabel.isNotEmpty()) {
            if (isModern) {
                sb.append("""<text class="y-label" x="48" y="${(yAxisStart + yAxisEnd) / 2}" text-anchor="middle" transform="rotate(-90 48 ${(yAxisStart + yAxisEnd) / 2})">${bar.yLabel.escapeXml()}</text>""")
            } else {
                sb.append(
                    """
                    <text x="30" y="${(yAxisStart + yAxisEnd) / 2}" font-family="${theme.fontFamily}" font-size="14" font-weight="bold" text-anchor="middle" fill="$textColor" transform="rotate(-90, 30, ${(yAxisStart + yAxisEnd) / 2})">${bar.yLabel.escapeXml()}</text>
                """.trimIndent()
                )
            }
        }
        if (isModern) {
            sb.append("""<text class="x-label" x="${(xAxisStart + xAxisEnd) / 2}" y="${yAxisEnd + 42}" text-anchor="middle">${bar.xLabel?.escapeXml() ?: ""}</text>""")
        }

        return sb.toString()
    }

    private fun makeBackground(bar: Bar): String {
        return """<rect width="100%" height="100%" fill="${theme.canvas}" rx="15" ry="15"/>"""
    }

    private fun head(bar: Bar): String {
        val svgWidth = if (isModern) width.toString() else (width / DISPLAY_RATIO_16_9).toString()
        val svgHeight = if (isModern) height.toString() else (height / DISPLAY_RATIO_16_9).toString()
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg width="$svgWidth" height="$svgHeight" viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg" role="img" aria-labelledby="title desc" id="id_${bar.display.id}">
                <title id="title">${bar.title.escapeXml()}</title>
                <desc id="desc">Animated bar chart with refined layout and cohesive palette.</desc>
                ${if (!isModern) theme.fontImport else ""}
            """.trimIndent()
    }

    private fun tail(): String {
        return "</svg>"
    }

    private fun addTitle(bar: Bar): String {
        if (isModern) {
            val subtitle = if (!bar.yLabel.isNullOrEmpty()) bar.yLabel else ""
            return """
                <!-- Header -->
                <text class="title" x="78" y="94">${bar.title.escapeXml()}</text>
                ${if (subtitle.isNotEmpty()) """<text class="subtitle" x="78" y="118">${subtitle.escapeXml()}</text>""" else ""}
            """.trimIndent()
        }
        val titleColor = theme.primaryText
        return """
                <!-- Title -->
                <text x="${width / 2}" y="40" font-family="${theme.fontFamily}" font-size="24" font-weight="bold" text-anchor="middle" fill="$titleColor">${bar.title.escapeXml()}</text>
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
        // Add the gradient definition
        val barDefs= StringBuilder()
        bar.series.forEachIndexed { index, series ->
            val svgColor = theme.chartPalette[index % theme.chartPalette.size]
            barDefs.append(
                """
                    <linearGradient id="id_${bar.display.id}_$index" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stop-color="${svgColor.lighter()}"/>
                        <stop offset="100%" stop-color="${svgColor.darker()}"/>
                    </linearGradient>
            """.trimIndent()
            )
        }

        val modernStyle = if (isModern) {
            """
            @import url('https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@400;500;600;700&amp;display=swap');
            :root {
            --bg: ${theme.canvas};
            --surface: ${if (bar.display.useDark) "#161b22" else "#ffffff"};
            --text: ${theme.primaryText};
            --text-soft: ${theme.secondaryText};
            --grid: ${theme.surfaceImpact};
            --axis: ${theme.secondaryText};
            --accent: ${theme.accentColor};
            ${theme.chartPalette.mapIndexed { i, c -> "--bar-${i + 1}: ${c.color};" }.joinToString("\n            ")}
            --bar-radius: ${theme.cornerRadius};
            }
            text { font-family: ${theme.fontFamily}; }
            .title { font-size: 30px; font-weight: 700; fill: var(--text); }
            .subtitle { font-size: 14px; font-weight: 500; fill: var(--text-soft); }
            .grid { stroke: var(--grid); stroke-width: 1; stroke-opacity: 0.12; stroke-dasharray: 4 8; }
            .axis { stroke: var(--axis); stroke-width: 1.4; stroke-opacity: 0.35; }
            .tick-text { font-size: 12px; font-weight: 500; fill: var(--text-soft); }
            .x-label { font-size: 13px; font-weight: 500; fill: var(--text-soft); }
            .y-label { font-size: 14px; font-weight: 600; fill: var(--text-soft); }
            .value-label { font-size: 12px; font-weight: 600; fill: var(--text); opacity: 0.48; transition: opacity 180ms ease, transform 180ms ease; pointer-events: none; }
            .bar-wrap:focus .value-label, .bar-wrap:hover .value-label { opacity: 1; transform: translateY(-2px); }
            .bar-inner { transform-box: fill-box; transform-origin: 50% 100%; transition: transform 220ms ease, filter 220ms ease; }
            .bar-wrap:focus .bar-inner, .bar-wrap:hover .bar-inner { transform: scale(1.03); filter: saturate(1.08); }
            @keyframes growBar { from { transform: scaleY(0); } to { transform: scaleY(1); } }
            @keyframes revealValue { from { opacity: 0; transform: translateY(6px); } to { opacity: 0.48; transform: translateY(0); } }
            ${bar.series.mapIndexed { i, _ -> ".anim-${i + 1} { animation: growBar 700ms cubic-bezier(.2,.8,.2,1) ${80 + i * 90}ms both; }" }.joinToString("\n            ")}
            ${bar.series.mapIndexed { i, _ -> ".val-${i + 1} { animation: revealValue 360ms ease ${720 + i * 90}ms both; }" }.joinToString("\n            ")}
            """.trimIndent()
        } else {
            """
                   #id_${bar.display.id} .glass-bar { transition: all 0.3s ease; }
                   #id_${bar.display.id} .glass-bar:hover { filter: url(#glow); transform: scale(1.02); cursor: pointer; }
                   #id_${bar.display.id} text { font-family: ${theme.fontFamily}; }
            """.trimIndent()
        }

        return """
            <defs>
                ${if (isModern) """
                <linearGradient id="bgGlow" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="${if (bar.display.useDark) "#1f2937" else "#dbe8f8"}" stop-opacity="0.65"/>
                    <stop offset="100%" stop-color="${theme.canvas}" stop-opacity="0"/>
                </linearGradient>
                """ else barDefs}
                
                ${if (!isModern) """
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

                <!-- Inner shadow for depth -->
                <filter id="innerShadow" x="-50%" y="-50%" width="200%" height="200%">
                    <feOffset dx="0" dy="2"/>
                    <feGaussianBlur stdDeviation="2" result="offset-blur"/>
                    <feFlood flood-color="rgba(0,0,0,0.2)"/>
                    <feComposite in2="offset-blur" operator="in"/>
                    <feComposite in2="SourceGraphic" operator="over"/>
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
                """ else ""}
                
                <style>
                $modernStyle
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
        display = BarDisplay(baseColor = "#4361ee", useDark = false, theme = "autumn")
    )

    // Generate the vertical bar chart
    val svg = VBarMaker().makeVerticalBar(bar, false)

    // Save the chart to a file
    val outfile = File("gen/vertical_bar_chart.svg")
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
        display = BarDisplay(baseColor = "#4361ee", useDark = false, theme="tallinn")
    )

    // Generate the vertical bar chart with more bars
    val svgWithMoreBars = VBarMaker().makeVerticalBar(barWithMoreSeries, false)

    // Save the chart to a file
    val outfileWithMoreBars = File("gen/vertical_bar_chart_more_bars.svg")
    outfileWithMoreBars.writeBytes(svgWithMoreBars.toByteArray())

    println("Vertical bar chart with more bars saved to ${outfileWithMoreBars.absolutePath}")
}
