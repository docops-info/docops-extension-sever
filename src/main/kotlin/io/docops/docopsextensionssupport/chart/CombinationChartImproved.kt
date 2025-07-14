
package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.util.ParsingUtils
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.update
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*

class CombinationChartImproved {

    private val logger = KotlinLogging.logger {}
    // Modern color palette for combination chart
    private val defaultColors = listOf(
        "#3498db", // Blue
        "#2ecc71", // Green
        "#e74c3c", // Red
        "#f39c12", // Orange
        "#9b59b6", // Purple
        "#1abc9c", // Turquoise
        "#34495e", // Dark Blue
        "#e67e22", // Dark Orange
        "#27ae60", // Dark Green
        "#d35400"  // Burnt Orange
    )

    fun makeCombinationChartSvg(payload: String, csvResponse: CsvResponse): String {
        try {// Parse configuration and data from content
            val (config, chartData) = parseConfigAndData(payload)

            // Create CombinationChart object from parsed data
            val combinationChart = createCombinationChartFromData(config, chartData)
            csvResponse.update(combinationChart.toCsv())
            // Use CombinationChartMaker to generate SVG
            val combinationChartMaker = CombinationChartMaker()
            return combinationChartMaker.makeChart(combinationChart)
        } catch (e: Exception) {
            logger.error(e) { "Error creating chart: $payload" }
            throw e
        }
    }

    /**
     * Parses the content to extract configuration parameters and chart data.
     * Uses the shared ParsingUtils for consistent parsing across the application.
     *
     * @param content The full content of the block
     * @return A Pair containing the configuration map and the chart data string
     */
    private fun parseConfigAndData(content: String): Pair<Map<String, String>, String> {
        return ParsingUtils.parseConfigAndData(content)
    }

    /**
     * Creates a CombinationChart object from the parsed configuration and data.
     *
     * @param config The configuration map
     * @param chartData The chart data string
     * @return A CombinationChart object
     */
    private fun createCombinationChartFromData(config: Map<String, String>, chartData: String): CombinationChart {
        val title = config.getOrDefault("title", "Combination Chart")
        val xLabel = config.getOrDefault("xLabel", "")
        val yLabel = config.getOrDefault("yLabel", "")
        val yLabelSecondary = config.getOrDefault("yLabelSecondary", "")
        val baseColor = config.getOrDefault("baseColor", "#3498db")
        val backgroundColor = config.getOrDefault("backgroundColor", "#f8f9fa")
        val useDark = config["darkMode"]?.toBoolean() ?: false
        val scale = config["scale"]?.toDoubleOrNull() ?: 1.0
        val showGrid = config["showGrid"]?.toBoolean() ?: true
        val showLegend = config["showLegend"]?.toBoolean() ?: true
        val smoothLines = config["smoothLines"]?.toBoolean() ?: true
        val showPoints = config["showPoints"]?.toBoolean() ?: true
        val dualYAxis = config["dualYAxis"]?.toBoolean() ?: false
        val useGlass = config["useGlass"]?.toBoolean() ?: false

        // Parse series data
        val seriesMap = mutableMapOf<String, CombinationSeries>()

        chartData.lines().forEach { line ->
            if (line.isNotBlank()) {
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 4) {
                    val seriesName = parts[0]
                    val type = when (parts[1].uppercase()) {
                        "BAR" -> ChartType.BAR
                        "LINE" -> ChartType.LINE
                        else -> ChartType.BAR
                    }
                    val xValue = parts[2]
                    val yValue = parts[3].toDoubleOrNull() ?: 0.0
                    val color = if (parts.size > 4 && parts[4].isNotBlank()) parts[4] else null
                    val yAxis = if (parts.size > 5 && parts[5].uppercase() == "SECONDARY") {
                        YAxisType.SECONDARY
                    } else {
                        YAxisType.PRIMARY
                    }

                    // Get or create series
                    val series = seriesMap.getOrPut(seriesName) {
                        CombinationSeries(
                            name = seriesName,
                            type = type,
                            data = mutableListOf(),
                            color = color,
                            yAxis = yAxis
                        )
                    }

                    // Add data point
                    series.data.add(CombinationDataPoint(xValue, yValue))
                }
            }
        }

        // Create display object
        val display = CombinationChartDisplay(
            baseColor = baseColor,
            backgroundColor = backgroundColor,
            useDark = useDark,
            scale = scale,
            showGrid = showGrid,
            showLegend = showLegend,
            smoothLines = smoothLines,
            showPoints = showPoints,
            dualYAxis = dualYAxis,
            useGlass = useGlass
        )

        return CombinationChart(
            title = title,
            xLabel = xLabel,
            yLabel = yLabel,
            yLabelSecondary = yLabelSecondary,
            series = seriesMap.values.toMutableList(),
            display = display
        )
    }
}
