package io.docops.docopsextensionssupport.metricscard

import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Class responsible for creating metrics card SVGs
 * 
 * Supports two input formats:
 * 1. JSON format: {"title": "Title", "metrics": [{"value": "97%", "label": "Label", "sublabel": "Sublabel"}]}
 * 2. Table format:
 *    title= Title
 *    ---
 *    Metric | Value | Sublabel
 *    Label1 | Value1 | Sublabel1
 *    Label2 | Value2 | Sublabel2
 *    
 * The table format can also be used without the sublabel column:
 *    title= Title
 *    ---
 *    Metric | Value
 *    Label1 | Value1
 *    Label2 | Value2
 */
class MetricsCardMaker(val csvResponse: CsvResponse, val isPdf: Boolean, val useDark: Boolean = false) {
    private val json = Json { ignoreUnknownKeys = true }

    private var theme = ThemeFactory.getThemeByName("corporate",useDark)
    /**
     * Creates an SVG for metrics cards from JSON or table-like data
     */
    fun createMetricsCardSvg(payload:String, width: Int = 800, height: Int = 400): Pair<String, CsvResponse> {
        return createCards(payload, width, height)
    }

    fun createCards(uncompressedData: String, width: Int, height: Int): Pair<String, CsvResponse> {
        // Determine if the data is in table format or JSON format
        val metricsCardData = if (isTableFormat(uncompressedData)) {
            // Parse table format
            parseTableData(uncompressedData,useDark)
        } else {
            // Parse JSON format
            try {
                json.decodeFromString<MetricsCardData>(uncompressedData)
            } catch (e: Exception) {
                // If parsing fails, create a default data object
                createDefaultMetricsCardData()
            }
        }
        theme = ThemeFactory.getThemeByName(metricsCardData.theme,useDark)

        val svg = generateMetricsCardSvg(metricsCardData, width, height, useDark)
        return Pair(svg, metricsCardData.toCsv())
    }

    /**
     * Determines if the data is in table format
     */
    private fun isTableFormat(data: String): Boolean {
        return data.contains("---") || (!data.trim().startsWith("{") && data.contains("|"))
    }

    /**
     * Parses table-like data into MetricsCardData
     */
    private fun parseTableData(data: String, useDark: Boolean): MetricsCardData {
        val lines = data.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        var title = "Metrics"
        var visualVersion = 1
        var themeName = "classic"
        val metrics = mutableListOf<MetricCard>()
        var inDataSection = false

        for (line in lines) {
            when {
                line.startsWith("title:") -> title = line.substring(6).trim()
                line.startsWith("visualVersion:") || line.startsWith("visualVersion=") -> visualVersion = line.substring(8).trim().toIntOrNull() ?: 3
                line.startsWith("title=") -> title = line.substring(6).trim()
                line.startsWith("theme=") -> themeName = line.substring(6).trim()
                line == "---" -> inDataSection = true
                inDataSection && line.contains("|") && !isHeaderRow(line) -> {
                    val parts = line.split("|").map { it.trim() }
                    if (parts.size >= 2) {
                        val label = parts[0]
                        val value = parts[1]
                        val sublabel = if (parts.size > 2) parts[2] else null
                        metrics.add(MetricCard(value = value, label = label, sublabel = sublabel))
                    }
                }
            }
        }

        return MetricsCardData(title = title, metrics = metrics, useGlass = useDark, useDark = useDark, visualVersion = visualVersion, theme = themeName)
    }

    /**
     * Helper function to detect header rows in table format
     */
    private fun isHeaderRow(line: String): Boolean {
        val lowerLine = line.lowercase()
        return lowerLine.contains("metric") ||
                lowerLine.contains("label") ||
                lowerLine.contains("value")
    }

    /**
     * Helper function to parse boolean values from string
     */
    private fun parseBoolean(value: String): Boolean {
        val lowerValue = value.lowercase().trim()
        return lowerValue == "true" || lowerValue == "yes" || lowerValue == "on" || lowerValue == "1"
    }

    /**
     * Generates the SVG for metrics cards
     */
    @OptIn(ExperimentalUuidApi::class)
    private fun generateMetricsCardSvg(metricsCardData: MetricsCardData, width: Int, height: Int, useDark: Boolean): String {
        // Calculate dynamic width based on number of metrics
        val metricsCount = metricsCardData.metrics.size
        val cardWidth = 196
        val cardMargin = 28

        val horizontalPadding = 56
        val totalCardWidth = metricsCount * (cardWidth + cardMargin) - cardMargin

        val idealWidth = totalCardWidth + horizontalPadding
        val finalWidth = if (width == 800) idealWidth else width.coerceAtLeast(idealWidth)
        val finalHeight = if (height == 400) 336 else height

        return buildString {
            val id = Uuid.random().toHexString()

            append("""
                    <svg id="ID_$id" width="$finalWidth" height="$finalHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $finalWidth $finalHeight" preserveAspectRatio='xMidYMid meet'>
                        <defs>
                            <style>
                                ${theme.fontImport}
                                .metric-value_$id { font-family: ${theme.fontFamily}; font-weight: 700; fill: ${theme.primaryText}; }
                                .metric-label_$id { font-family: ${theme.fontFamily}; font-weight: 400; fill: ${theme.secondaryText}; letter-spacing: 0.05em; text-transform: uppercase; }
                                .metric-sub_$id { font-family: 'JetBrains Mono', monospace; font-size: 11px; fill: ${theme.accentColor}; }
                                .title-text_$id { font-family: ${theme.fontFamily}; font-weight: 700; font-size: ${32/theme.fontWidthMultiplier}px; fill: ${theme.primaryText}; }
                            </style>
                    
                            <pattern id="grid_$id" x="0" y="0" width="28" height="28" patternUnits="userSpaceOnUse">
                                <path d="M 28 0 L 0 0 0 28" fill="none" stroke="${theme.primaryText}" stroke-width="0.3" opacity="0.1"/>
                            </pattern>
                        
                            <filter id="shadow_$id" x="-20%" y="-20%" width="140%" height="140%">
                                <feDropShadow dx="6" dy="6" stdDeviation="0" flood-color="${theme.primaryText}" flood-opacity="0.3"/>
                            </filter>
                        </defs>

                        <!-- Background Layering: Preservation of depth -->
                        <rect width="100%" height="100%" fill="${theme.canvas}" rx="16"/>
                        <rect width="100%" height="100%" fill="url(#grid_$id)"/>
                
                        <!-- Title Section -->
                        <g transform="translate(28, 56)">
                            <text x="0" y="0" class="title-text_$id">${metricsCardData.title.escapeXml()}</text>
                            <rect x="0" y="18" width="60" height="8" fill="${theme.accentColor}" rx="2"/>
                        </g>

                        <g class="metrics">
                    """.trimIndent())

            val totalWidth = metricsCount * (cardWidth + cardMargin) - cardMargin
            val startX = (finalWidth - totalWidth) / 2
            val accentColors = theme.chartPaletteHex

            metricsCardData.metrics.forEachIndexed { index, metric ->
                val x = startX + index * (cardWidth + cardMargin)
                val y = 112
                val accentColor = accentColors[index % accentColors.size]

                // Adjust max length based on the font's width multiplier
                val baseMaxChars = 24
                val adjustedMaxChars = (baseMaxChars / theme.fontWidthMultiplier)

                val wrappedLabel = wrapText(metric.label, adjustedMaxChars.toInt()) // Wrap roughly around 22 chars

                append("""
                                <g class="metric-card" transform="translate($x, $y)">
                                    <!-- Card Background -->
                                    <rect width="$cardWidth" height="196" rx="${theme.cornerRadius}" 
                                          fill="${theme.glassEffect}" stroke="${theme.primaryText}" stroke-opacity="0.1" stroke-width="1"
                                          filter="url(#shadow_$id)"/>
                        
                                    <!-- Cyber Accent Tab -->
                                    <path d="M 0 16 Q 0 0 16 0 L 60 0 L 45 15 L 0 15 Z" fill="$accentColor" opacity="0.9"/>

                                    <!-- Metric Value -->
                                    <text x="20" y="85" text-anchor="start" 
                                          font-size="${42/theme.fontWidthMultiplier}" class="metric-value_$id">${metric.value.escapeXml()}</text>

                                    <!-- Metric Label (Wrapped) -->
                                    <text x="20" y="110" text-anchor="start" font-size="${13 / theme.fontWidthMultiplier}" class="metric-label_$id">
                            """.trimIndent())

                wrappedLabel.forEachIndexed { i, line ->
                    append("""<tspan x="20" dy="${if (i == 0) 0 else 14}">${line.escapeXml()}</tspan>""")
                }

                append("</text>")

                if (metric.sublabel != null) {
                    // Adjust Y based on if the label wrapped to 2 lines
                    val sublabelY = if (wrappedLabel.size > 1) 155 else 145
                    append("""
                                    <!-- Metric Sublabel -->
                                    <text x="20" y="$sublabelY" text-anchor="start" 
                                          class="metric-sub_$id">> ${metric.sublabel.escapeXml()}</text>
                                """.trimIndent())
                }

                append("</g>")
            }

            append("""
                        </g>
                    </svg>
                    """.trimIndent())
        }
    }

    /**
     * Simple utility to wrap text for SVG tspans
     */
    private fun wrapText(text: String, maxLength: Int): List<String> {
        if (text.length <= maxLength) return listOf(text)
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 <= maxLength) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        lines.add(currentLine.toString())
        return lines
    }

    /**
     * Creates a default metrics card data object
     */
    private fun createDefaultMetricsCardData(): MetricsCardData {
        return MetricsCardData(
            title = "Metrics",
            metrics = listOf(
                MetricCard(
                    value = "97%",
                    label = "Query Cost Reduction",
                    sublabel = "(12,000 → 405)"
                ),
                MetricCard(
                    value = "32%",
                    label = "CPU Utilization Drop",
                    sublabel = "(88% → 60%)"
                ),
                MetricCard(
                    value = "3",
                    label = "Optimization Phases",
                    sublabel = "Systematic Approach"
                )
            )
        )
    }

}
