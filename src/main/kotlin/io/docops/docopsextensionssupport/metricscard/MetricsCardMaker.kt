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

    /**
     * Creates an SVG for metrics cards from JSON or table-like data
     */
    fun createMetricsCardSvg(payload: String, width: Int = 800, height: Int = 400, useDark: Boolean): Pair<String, CsvResponse> {
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
        if(isPdf) {
            metricsCardData.useGlass = false
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
    private fun generateMetricsCardSvg(metricsCardData: MetricsCardData, width: Int, height: Int, useGlass: Boolean): String {
        // Calculate dynamic width based on number of metrics
        // Each metric card takes 180px width + 20px margin
        val metricsCount = metricsCardData.metrics.size
        val cardWidth = 180
        val cardMargin = 20

        val horizontalPadding = 40 // Add 20px padding on each side
        val totalCardWidth = metricsCount * (cardWidth + cardMargin) - cardMargin // Subtract last margin

        // Use the larger of calculated width or provided width, adding padding
        val finalWidth = (totalCardWidth + horizontalPadding).coerceAtLeast(width)


        return buildString {
            val id = Uuid.random().toHexString()
            append("""
            <svg id="ID_$id" width="$finalWidth" height="$height" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $finalWidth $height" preserveAspectRatio='xMidYMid meet'>
                <defs>
            """.trimIndent())

            var back = """<rect width="$finalWidth" height="$height" fill="#F2F2F7" rx="0" ry="0"/>"""
            // Add glass-specific definitions if useGlass is true
            if (metricsCardData.useGlass) {
                back = """<rect width="100%" height="100%" fill="url(#backgroundGradient_$id)" rx="12" ry="12"/>
                <rect width="100%" height="100%" rx="12" ry="12"
                      fill="rgba(0,122,255,0.1)"
                      stroke="url(#glassBorder_$id)" stroke-width="1.5"
                      filter="url(#glassDropShadow_$id)"
                />
                <rect width="100%" height="100%" rx="12" ry="12"
                      fill="url(#glassOverlay_$id)" opacity="0.7"
                />"""
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
                    <linearGradient id="glassBorder_${id}" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1"/>
            <stop offset="50%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1"/>
            <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1"/>
        </linearGradient>
        <filter id="glassDropShadow_${id}" x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="8" result="blur"/>
            <feOffset in="blur" dx="0" dy="8" result="offsetBlur"/>
            <feFlood flood-color="rgba(0,0,0,0.15)" result="shadowColor"/>
            <feComposite in="shadowColor" in2="offsetBlur" operator="in" result="shadow"/>
            <feMerge>
                <feMergeNode in="shadow"/>
                <feMergeNode in="SourceGraphic"/>
            </feMerge>
        </filter>
        <linearGradient id="glassOverlay_${id}" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" style="stop-color:rgba(255,255,255,0.25);stop-opacity:1"/>
            <stop offset="30%" style="stop-color:rgba(255,255,255,0.15);stop-opacity:1"/>
            <stop offset="70%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1"/>
            <stop offset="100%" style="stop-color:rgba(255,255,255,0.02);stop-opacity:1"/>
        </linearGradient>
        <linearGradient id="backgroundGradient_${id}" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#1a1a2e;stop-opacity:1"/>
            <stop offset="100%" style="stop-color:#16213e;stop-opacity:1"/>
        </linearGradient>
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
                $back

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
