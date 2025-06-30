package io.docops.docopsextensionssupport.metricscard

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
    val useGlass: Boolean = true // Default is to use glass styling
)
