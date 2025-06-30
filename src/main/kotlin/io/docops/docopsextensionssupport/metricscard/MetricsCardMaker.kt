package io.docops.docopsextensionssupport.metricscard

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import kotlinx.serialization.json.Json
import java.util.*

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
class MetricsCardMaker {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Creates an SVG for metrics cards from JSON or table-like data
     */
    fun createMetricsCardSvg(data: String, width: Int = 800, height: Int = 400): String {


        return createCards(data, width, height)
    }

    fun createCards(uncompressedData: String, width: Int, height: Int): String {
        // Determine if the data is in table format or JSON format
        val metricsCardData = if (isTableFormat(uncompressedData)) {
            // Parse table format
            parseTableData(uncompressedData)
        } else {
            // Parse JSON format
            try {
                json.decodeFromString<MetricsCardData>(uncompressedData)
            } catch (e: Exception) {
                // If parsing fails, create a default data object
                createDefaultMetricsCardData()
            }
        }
        val svg = generateMetricsCardSvg(metricsCardData, width, height)
        return svg
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
    private fun parseTableData(data: String): MetricsCardData {
        val lines = data.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        var title = "Metrics"
        var useGlass = true // Default value
        val metrics = mutableListOf<MetricCard>()
        var inDataSection = false

        for (line in lines) {
            when {
                line.startsWith("title:") -> title = line.substring(6).trim()
                line.startsWith("title=") -> title = line.substring(6).trim()
                line.startsWith("useGlass:") -> useGlass = parseBoolean(line.substring(9).trim())
                line.startsWith("useGlass=") -> useGlass = parseBoolean(line.substring(9).trim())
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

        return MetricsCardData(title = title, metrics = metrics, useGlass = useGlass)
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
    private fun generateMetricsCardSvg(metricsCardData: MetricsCardData, width: Int, height: Int): String {
        // Calculate dynamic width based on number of metrics
        // Each metric card takes 180px width + 20px margin
        val metricsCount = metricsCardData.metrics.size
        val cardWidth = 180
        val cardMargin = 20
        val totalCardWidth = metricsCount * (cardWidth + cardMargin) - cardMargin // Subtract last margin

        // Use the larger of calculated width or provided width
        val finalWidth = totalCardWidth.coerceAtLeast(width)

        return buildString {
            val id = UUID.randomUUID().toString()
            append("""
            <svg id="ID_$id" width="$width" height="$height" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $finalWidth $height" preserveAspectRatio='xMidYMid meet'>
                <defs>
            """.trimIndent())

            // Add glass-specific definitions if useGlass is true
            if (metricsCardData.useGlass) {
                append("""
                    <!-- Glass gradients -->
                    <linearGradient id="glassGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                        <stop offset="50%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                        <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
                    </linearGradient>
                    <radialGradient id="glassRadial" cx="30%" cy="30%" r="70%">
                        <stop offset="0%" style="stop-color:rgba(255,255,255,0.4);stop-opacity:1" />
                        <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                        <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
                    </radialGradient>
                    <linearGradient id="highlight" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" style="stop-color:rgba(255,255,255,0.6);stop-opacity:1" />
                        <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
                    </linearGradient>

                    <!-- Glass filters -->
                    <filter id="blur" x="-50%" y="-50%" width="200%" height="200%">
                        <feGaussianBlur in="SourceGraphic" stdDeviation="3" />
                    </filter>
                    <filter id="shadow" x="-50%" y="-50%" width="200%" height="200%">
                        <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="rgba(0,0,0,0.3)"/>
                    </filter>
                    <filter id="innerShadow" x="-50%" y="-50%" width="200%" height="200%">
                        <feOffset dx="0" dy="2"/>
                        <feGaussianBlur stdDeviation="3" result="offset-blur"/>
                        <feFlood flood-color="rgba(0,0,0,0.3)"/>
                        <feComposite in2="offset-blur" operator="in"/>
                        <feComposite in2="SourceGraphic" operator="over"/>
                    </filter>
                """.trimIndent())
            }

            // Always include original iOS shadow and gradient
            append("""
                    <filter id="iosShadow">
                        <feDropShadow dx="0" dy="2" stdDeviation="3" flood-opacity="0.15"/>
                    </filter>
                    <linearGradient id="iosGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                        <stop offset="0%" style="stop-color:#34C759;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#30D158;stop-opacity:1" />
                    </linearGradient>
                </defs>

                <!-- Background -->
                <rect width="$finalWidth" height="$height" fill="${if (metricsCardData.useGlass) "#1d4ed8" else "#F2F2F7"}" rx="0" ry="0"/>

                <!-- Metrics Container -->
                <g class="metrics">
            """.trimIndent())

            // Calculate starting position for centering the cards
            val totalWidth = metricsCount * (cardWidth + cardMargin) - cardMargin
            val startX = (finalWidth - totalWidth) / 2

            // Generate each metric card
            metricsCardData.metrics.forEachIndexed { index, metric ->
                val x = startX + index * (cardWidth + cardMargin)
                val y = (height - 200) / 2 // Center vertically

                // Conditionally apply glass or original styling to cards
                if (metricsCardData.useGlass) {
                    append("""
                    <!-- Metric Card ${index + 1} -->
                    <g class="metric-card" transform="translate($x, $y)">
                        <!-- Card Background -->
                        <rect width="$cardWidth" height="200" rx="16" ry="16" 
                              fill="url(#glassGradient)" stroke="rgba(255,255,255,0.3)" stroke-width="1" 
                              filter="url(#shadow)"/>
                        <!-- Card highlight -->
                        <rect x="5" y="5" width="${cardWidth - 10}" height="30" rx="10" 
                              fill="url(#highlight)"/>

                        <!-- Metric Value -->
                        <text x="${cardWidth/2}" y="80" 
                              font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                              font-size="36" font-weight="700" text-anchor="middle" 
                              fill="white">${metric.value.escapeXml()}</text>

                        <!-- Metric Label -->
                        <text x="${cardWidth/2}" y="120" 
                              font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                              font-size="16" font-weight="600" text-anchor="middle" 
                              fill="rgba(255,255,255,0.9)">${metric.label.escapeXml()}</text>
                    """.trimIndent())
                } else {
                    append("""
                    <!-- Metric Card ${index + 1} -->
                    <g class="metric-card" transform="translate($x, $y)">
                        <!-- Card Background -->
                        <rect width="$cardWidth" height="200" rx="16" ry="16" 
                              fill="white" stroke="#E5E5EA" stroke-width="1" 
                              filter="url(#iosShadow)"/>

                        <!-- Metric Value -->
                        <text x="${cardWidth/2}" y="80" 
                              font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                              font-size="36" font-weight="700" text-anchor="middle" 
                              fill="#007AFF">${metric.value.escapeXml()}</text>

                        <!-- Metric Label -->
                        <text x="${cardWidth/2}" y="120" 
                              font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                              font-size="16" font-weight="600" text-anchor="middle" 
                              fill="#1C1C1E">${metric.label.escapeXml()}</text>
                    """.trimIndent())
                }

                // Add sublabel if present
                if (metric.sublabel != null) {
                    if (metricsCardData.useGlass) {
                        append("""

                        <!-- Metric Sublabel -->
                        <text x="${cardWidth/2}" y="145" 
                              font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                              font-size="14" font-weight="400" text-anchor="middle" 
                              fill="rgba(255,255,255,0.7)">${metric.sublabel.escapeXml()}</text>
                        """.trimIndent())
                    } else {
                        append("""

                        <!-- Metric Sublabel -->
                        <text x="${cardWidth/2}" y="145" 
                              font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                              font-size="14" font-weight="400" text-anchor="middle" 
                              fill="#8E8E93">${metric.sublabel.escapeXml()}</text>
                        """.trimIndent())
                    }
                }

                append("""
                    </g>
                """.trimIndent())
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
}
