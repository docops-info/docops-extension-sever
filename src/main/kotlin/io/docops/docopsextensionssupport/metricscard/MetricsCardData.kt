package io.docops.docopsextensionssupport.metricscard

import io.docops.docopsextensionssupport.support.VisualDisplay
import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable

/**
 * Data class for a single metric card
 */
@Serializable
data class MetricCard(
    val value: String,
    val label: String,
    val sublabel: String? = null
)

/**
 * Data class for metrics card data
 */
@Serializable
data class MetricsCardData(
    val title: String = "Metrics",
    val metrics: List<MetricCard> = emptyList(),
    val theme: String = "ios", // Default theme is iOS style
    var useGlass: Boolean = true, // Default is to use glass styling
    override val visualVersion: Int = 1,
    override val useDark: Boolean = false
) : VisualDisplay

fun MetricsCardData.toCsv(): CsvResponse {
    val headers = listOf(title, "Metric Number", "Value", "Label", "Sublabel", "Theme", "Use Glass")
    val csvRows = mutableListOf<List<String>>()

    // Add metrics rows
    if (metrics.isNotEmpty()) {
        metrics.forEachIndexed { index, metric ->
            csvRows.add(listOf(
                "", // Empty title column for rows after header
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
        csvRows.add(listOf("", "0", "", "", "", theme, useGlass.toString()))
    }

    return CsvResponse(headers, csvRows)
}