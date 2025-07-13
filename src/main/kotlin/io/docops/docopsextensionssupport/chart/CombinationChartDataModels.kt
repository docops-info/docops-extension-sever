package io.docops.docopsextensionssupport.chart

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
    val useDark: Boolean = false,
    val scale: Double = 1.0,
    val showGrid: Boolean = true,
    val showLegend: Boolean = true,
    val smoothLines: Boolean = true,
    val showPoints: Boolean = true,
    val dualYAxis: Boolean = false,
    val useGlass: Boolean = false
)
