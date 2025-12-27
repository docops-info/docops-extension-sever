package io.docops.docopsextensionssupport.metricscard

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
class MetricsCardMaker(val csvResponse: CsvResponse, val isPdf: Boolean) {
    private val json = Json { ignoreUnknownKeys = true }
    private interface MetricsTheme {
        val bg: String
        val cardBgStart: String
        val cardBgEnd: String
        val gridColor: String
        val titleColor: String
        val valueColor: String
        val labelColor: String
        val cardStroke: String
    }

    private class DarkTheme : MetricsTheme {
        override val bg = "#020617"
        override val cardBgStart = "#0F172A"
        override val cardBgEnd = "#1E293B"
        override val gridColor = "#334155"
        override val titleColor = "#FFFFFF"
        override val valueColor = "#F8FAFC"
        override val labelColor = "#94A3B8"
        override val cardStroke = "#334155"
    }

    private class LightTheme : MetricsTheme {
        override val bg = "#F8FAFC"
        override val cardBgStart = "#FFFFFF"
        override val cardBgEnd = "#F1F5F9"
        override val gridColor = "#CBD5E1"
        override val titleColor = "#0F172A"
        override val valueColor = "#1E293B"
        override val labelColor = "#64748B"
        override val cardStroke = "#E2E8F0"
    }
    /**
     * Creates an SVG for metrics cards from JSON or table-like data
     */
    fun createMetricsCardSvg(payload:String, width: Int = 800, height: Int = 400, useDark: Boolean = false): Pair<String, CsvResponse> {
        return createCards(payload, width, height, useDark)
    }

    fun createCards(uncompressedData: String, width: Int, height: Int, useDark: Boolean): Pair<String, CsvResponse> {
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

        val metrics = mutableListOf<MetricCard>()
        var inDataSection = false

        for (line in lines) {
            when {
                line.startsWith("title:") -> title = line.substring(6).trim()
                line.startsWith("title=") -> title = line.substring(6).trim()
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

        return MetricsCardData(title = title, metrics = metrics, useGlass = useDark)
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
        val cardWidth = 200
        val cardMargin = 30

        val horizontalPadding = 80
        val totalCardWidth = metricsCount * (cardWidth + cardMargin) - cardMargin

        val finalWidth = (totalCardWidth + horizontalPadding).coerceAtLeast(width)

        return buildString {
            val id = Uuid.random().toHexString()
            val theme = if (useDark) DarkTheme() else LightTheme()

            append("""
                    <svg id="ID_$id" width="$finalWidth" height="$height" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $finalWidth $height" preserveAspectRatio='xMidYMid meet'>
                        <defs>
                            <style>
                                @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@400;700&amp;family=JetBrains+Mono&amp;display=swap');
                                .metric-value_$id { font-family: 'Outfit', sans-serif; font-weight: 700; fill: ${theme.valueColor}; }
                                .metric-label_$id { font-family: 'Outfit', sans-serif; font-weight: 400; fill: ${theme.labelColor}; letter-spacing: 0.05em; text-transform: uppercase; }
                                .metric-sub_$id { font-family: 'JetBrains Mono', monospace; font-size: 12px; fill: #A855F7; }
                                .title-text_$id { font-family: 'Outfit', sans-serif; font-weight: 700; font-size: 24px; fill: ${theme.titleColor}; }
                            </style>
                        
                            <pattern id="grid_$id" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                                <path d="M 20 0 L 0 0 0 20" fill="none" stroke="${theme.gridColor}" stroke-width="0.5"/>
                            </pattern>

                            <filter id="glow_$id">
                                <feGaussianBlur stdDeviation="2" result="blur" />
                                <feComposite in="SourceGraphic" in2="blur" operator="over" />
                            </filter>
                            
                            <filter id="shadow_$id" x="-20%" y="-20%" width="140%" height="140%">
                                <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="rgba(0,0,0,${if (useDark) "0.4" else "0.08"})"/>
                            </filter>

                            <linearGradient id="cardGrad_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                                <stop offset="0%" stop-color="${theme.cardBgStart}" />
                                <stop offset="100%" stop-color="${theme.cardBgEnd}" />
                            </linearGradient>
                        </defs>

                        <!-- Background Layering -->
                        <rect width="100%" height="100%" fill="${theme.bg}" rx="12"/>
                        <rect width="100%" height="100%" fill="url(#grid_$id)" opacity="0.5"/>
                    
                        <!-- Title Section -->
                        <g transform="translate(40, 50)">
                            <text x="0" y="0" class="title-text_$id">${metricsCardData.title.escapeXml()}</text>
                            <rect x="0" y="15" width="60" height="4" fill="#A855F7" rx="2" filter="url(#glow_$id)"/>
                        </g>

                        <g class="metrics">
                    """.trimIndent())

            val totalWidth = metricsCount * (cardWidth + cardMargin) - cardMargin
            val startX = (finalWidth - totalWidth) / 2
            val accentColors = listOf("#A855F7", "#2DD4BF", "#F43F5E", "#3B82F6", "#EAB308")

            metricsCardData.metrics.forEachIndexed { index, metric ->
                val x = startX + index * (cardWidth + cardMargin)
                val y = (height - 180) / 2 + 20
                val accentColor = accentColors[index % accentColors.size]

                append("""
                            <g class="metric-card" transform="translate($x, $y)">
                                <!-- Card Background -->
                                <rect width="$cardWidth" height="180" rx="16" 
                                      fill="url(#cardGrad_$id)" stroke="${theme.cardStroke}" stroke-width="1.5"
                                      filter="url(#shadow_$id)"/>
                            
                                <!-- Cyber Accent Tab -->
                                <path d="M 0 16 Q 0 0 16 0 L 60 0 L 45 15 L 0 15 Z" fill="$accentColor" opacity="0.9"/>

                                <!-- Metric Value -->
                                <text x="${cardWidth/2}" y="85" text-anchor="middle" 
                                      font-size="42" class="metric-value_$id">${metric.value.escapeXml()}</text>

                                <!-- Metric Label -->
                                <text x="${cardWidth/2}" y="115" text-anchor="middle" 
                                      font-size="13" class="metric-label_$id">${metric.label.escapeXml()}</text>
                        """.trimIndent())

                if (metric.sublabel != null) {
                    append("""
                                <!-- Metric Sublabel -->
                                <text x="${cardWidth/2}" y="145" text-anchor="middle" 
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
    /**
     * Converts a MetricsCardData to CSV format
     * @return CsvResponse with headers and rows representing the metrics card data
     */
    fun MetricsCardData.toCsv(): CsvResponse {
        val headers = listOf("Title", "Metric Number", "Value", "Label", "Sublabel", "Theme", "Use Glass")
        val csvRows = mutableListOf<List<String>>()

        // Add metrics rows
        if (metrics.isNotEmpty()) {
            metrics.forEachIndexed { index, metric ->
                csvRows.add(listOf(
                    if (index == 0) title else "", // Only show title in first row
                    (index + 1).toString(),
                    metric.value,
                    metric.label,
                    metric.sublabel ?: "",
                    if (index == 0) theme else "", // Only show theme in first row
                    if (index == 0) useGlass.toString() else "" // Only show useGlass in first row
                ))
            }
        } else {
            // If no metrics, just add title row with configuration
            csvRows.add(listOf(title, "0", "", "", "", theme, useGlass.toString()))
        }

        return CsvResponse(headers, csvRows)
    }

}
