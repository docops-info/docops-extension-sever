package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CombinationChart(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val xLabel: String = "",
    val yLabel: String = "",
    val yLabelSecondary: String = "", // For dual Y-axis support
    val series: MutableList<CombinationSeries>,
    val display: CombinationChartDisplay = CombinationChartDisplay()
)

@Serializable
data class CombinationSeries(
    val name: String,
    val type: ChartType, // LINE or BAR
    val data: MutableList<CombinationDataPoint>,
    val color: String? = null,
    val yAxis: YAxisType = YAxisType.PRIMARY // For dual Y-axis
)

@Serializable
data class CombinationDataPoint(
    val x: String, // Category label
    val y: Double, // Value
    val xValue: Double? = null // For numeric X-axis positioning
)

@Serializable
enum class ChartType {
    LINE, BAR
}

@Serializable
enum class YAxisType {
    PRIMARY, SECONDARY
}

@Serializable
data class CombinationChartDisplay(
    val id: String = UUID.randomUUID().toString(),
    val baseColor: String = "#4361ee",
    val backgroundColor: String = "#f8f9fa",
    var useDark: Boolean = false,
    val scale: Double = 1.0,
    val showGrid: Boolean = true,
    val showLegend: Boolean = true,
    val smoothLines: Boolean = true,
    val showPoints: Boolean = true,
    val dualYAxis: Boolean = false,
    val useGlass: Boolean = false
)

/**
 * Converts a CombinationChart to CSV format
 * @return CsvResponse with headers and rows representing the combination chart data
 */
fun CombinationChart.toCsv(): CsvResponse {
    val headers = mutableListOf<String>()
    val rows = mutableListOf<List<String>>()

    // Create headers based on the chart structure
    headers.add("Series")
    headers.add("Type")
    headers.add("X")
    headers.add("Y")

    // Check if any series has color information
    if (series.any { it.color != null }) {
        headers.add("Color")
    }

    // Add data rows
    for (chartSeries in series) {
        for (point in chartSeries.data) {
            val row = mutableListOf<String>()
            row.add(chartSeries.name)
            row.add(chartSeries.type.name) // e.g., "line", "bar", "area"
            row.add(point.x.toString())
            row.add(point.y.toString())

            // Add color if the header exists
            if (headers.contains("Color")) {
                row.add(chartSeries.color ?: "")
            }

            rows.add(row)
        }
    }

    return CsvResponse(headers, rows)
}
