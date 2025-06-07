package io.docops.docopsextensionssupport.diagram

import kotlinx.serialization.Serializable

/**
 * Represents a data point in a quadrant chart.
 *
 * @property label The label for the data point
 * @property x The x-coordinate (effort) value from 0 to 100
 * @property y The y-coordinate (impact) value from 0 to 100
 * @property size The size of the data point (default is 8)
 * @property color The color of the data point (optional)
 * @property description Additional description for the data point (optional)
 */
@Serializable
data class QuadrantPoint(
    val label: String,
    val x: Float,
    val y: Float,
    val size: Float = 8f,
    val color: String? = null,
    val description: String = ""
)

/**
 * Represents a quadrant chart with title, labels, and data points.
 *
 * @property title The title of the quadrant chart
 * @property subtitle The subtitle of the quadrant chart (optional)
 * @property xAxisLabel The label for the x-axis (default is "EFFORT REQUIRED")
 * @property yAxisLabel The label for the y-axis (default is "IMPACT LEVEL")
 * @property q1Label The label for quadrant 1 (top-right) (default is "HIGH IMPACT")
 * @property q2Label The label for quadrant 2 (top-left) (default is "STRATEGIC")
 * @property q3Label The label for quadrant 3 (bottom-left) (default is "FILL-INS")
 * @property q4Label The label for quadrant 4 (bottom-right) (default is "THANKLESS")
 * @property q1Description The description for quadrant 1 (default is "Low Effort")
 * @property q2Description The description for quadrant 2 (default is "High Effort")
 * @property q3Description The description for quadrant 3 (default is "Low Impact")
 * @property q4Description The description for quadrant 4 (default is "High Effort")
 * @property points The list of data points in the quadrant chart
 */
@Serializable
data class QuadrantChart(
    val title: String = "Strategic Priority Matrix",
    val subtitle: String = "Impact vs. Effort Analysis",
    val xAxisLabel: String = "EFFORT REQUIRED",
    val yAxisLabel: String = "IMPACT LEVEL",
    val q1Label: String = "HIGH IMPACT",
    val q2Label: String = "STRATEGIC",
    val q3Label: String = "FILL-INS",
    val q4Label: String = "THANKLESS",
    val q1Description: String = "Low Effort",
    val q2Description: String = "High Effort",
    val q3Description: String = "Low Impact",
    val q4Description: String = "High Effort",
    val points: List<QuadrantPoint> = emptyList()
)

/**
 * Container class for a list of quadrant charts.
 *
 * @property charts The list of quadrant charts
 */
@Serializable
data class QuadrantCharts(
    val charts: List<QuadrantChart> = emptyList()
)