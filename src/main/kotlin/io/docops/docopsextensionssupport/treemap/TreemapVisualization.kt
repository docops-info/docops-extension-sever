package io.docops.docopsextensionssupport.chart.treemap

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable

/**
 * Represents a treemap visualization with hierarchical data
 */
@Serializable
data class Treemap(
    val title: String = "Budget Allocation",
    val subtitle: String = "",
    val items: List<TreemapItem> = emptyList(),
    val display: TreemapDisplay = TreemapDisplay()
) {
    fun toCsv(): CsvResponse {
        val headers = listOf("Category", "Value", "Percentage", "Description", "Metric")

        val total = items.sumOf { it.value }
        val rows = items.map { item ->
            val percentage = String.format("%.1f", (item.value / total * 100))
            listOf(
                item.label,
                item.value.toString(),
                percentage,
                item.description,
                item.metric.ifBlank { formatValue(item.value) }
            )
        }

        return CsvResponse(headers, rows)
    }

    private fun formatValue(value: Double): String {
        return when {
            value >= 1_000_000 -> "$%.1fM".format(value / 1_000_000)
            value >= 1_000 -> "$%.0fK".format(value / 1_000)
            else -> "$%.0f".format(value)
        }
    }
}

@Serializable
data class TreemapItem(
    val label: String,
    val value: Double,
    val description: String = "",
    val color: String? = null,
    val metric: String = ""
)

@Serializable
data class TreemapDisplay(
    val width: Int = 1200,
    val height: Int = 800,
    val useDark: Boolean = false,
    val theme: String = "modern", // modern, brutalist, neon, glassmorphic
    val paletteType: String = "", // ColorPaletteFactory types
    val fontFamily: String = "", // Empty = use theme default
    val showValues: Boolean = true,
    val showPercentages: Boolean = true,
    val animationDelay: Double = 0.1
)
