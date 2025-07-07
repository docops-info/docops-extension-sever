package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.util.ParsingUtils
import java.util.UUID

class BarChartImproved {

    // Modern color palette for bar chart
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

    fun makeBarSvg(payload: String): String {
        // Parse configuration and data from content
        val (config, chartData) = parseConfigAndData(payload)

        // Create Bar object from parsed data
        val bar = createBarFromData(config, chartData)

        // Use existing BarMaker to generate SVG
        val barMaker = BarMaker()
        return barMaker.makeVerticalBar(bar)
    }

    fun makeGroupBarSvg(payload: String): String {
        // Parse configuration and data from content
        val (config, chartData) = parseConfigAndData(payload)

        // Create BarGroup object from parsed data
        val barGroup = createBarGroupFromData(config, chartData)

        // Use existing BarGroupMaker to generate SVG
        val barGroupMaker = BarGroupMaker()
        return if (barGroup.display.vBar) {
            barGroupMaker.makeVGroupBar(barGroup)
        } else if (barGroup.display.condensed) {
            barGroupMaker.makeCondensed(barGroup)
        } else {
            barGroupMaker.makeBar(barGroup)
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
     * Creates a Bar object from the parsed configuration and data.
     *
     * @param config The configuration map
     * @param chartData The chart data string
     * @return A Bar object
     */
    private fun createBarFromData(config: Map<String, String>, chartData: String): Bar {
        val title = config.getOrDefault("title", "Bar Chart")
        val yLabel = config.getOrDefault("yLabel", "")
        val xLabel = config.getOrDefault("xLabel", "")
        val baseColor = config.getOrDefault("baseColor", "#4361ee")
        val vBar = config["vBar"]?.toBoolean() ?: false
        val useDark = config["darkMode"]?.toBoolean() ?: false
        val sorted = config["sorted"]?.toBoolean() ?: false
        val scale = config["scale"]?.toFloatOrNull() ?: 1.0f
        val type = config.getOrDefault("type", "R")

        // Parse series data
        val series = mutableListOf<Series>()
        chartData.lines().forEach { line ->
            if (line.isNotBlank()) {
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 2) {
                    val label = parts[0]
                    val value = parts[1].toDoubleOrNull() ?: 0.0
                    val color = if (parts.size > 2 && parts[2].isNotBlank()) parts[2] else null

                    // Create itemDisplay if color is provided
                    val itemDisplay = if (color != null) {
                        BarDisplay(
                            id = UUID.randomUUID().toString(),
                            baseColor = color,
                            type = type,
                            vBar = vBar,
                            useDark = useDark,
                            sorted = sorted,
                            scale = scale
                        )
                    } else null

                    series.add(Series(label, value, itemDisplay))
                }
            }
        }

        // Create display object
        val display = BarDisplay(
            baseColor = baseColor,
            type = type,
            vBar = vBar,
            useDark = useDark,
            sorted = sorted,
            scale = scale
        )

        return Bar(title, yLabel, xLabel, series, display)
    }

    /**
     * Creates a BarGroup object from the parsed configuration and data.
     *
     * @param config The configuration map
     * @param chartData The chart data string
     * @return A BarGroup object
     */
    private fun createBarGroupFromData(config: Map<String, String>, chartData: String): BarGroup {
        val title = config.getOrDefault("title", "Bar Group Chart")
        val yLabel = config.getOrDefault("yLabel", "")
        val xLabel = config.getOrDefault("xLabel", "")
        val baseColor = config.getOrDefault("baseColor", "#D988B9")
        val vBar = config["vBar"]?.toBoolean() ?: false
        val useDark = config["darkMode"]?.toBoolean() ?: false
        val condensed = config["condensed"]?.toBoolean() ?: false
        val scale = config["scale"]?.toDoubleOrNull() ?: 1.0

        // Parse group data
        val groupMap = mutableMapOf<String, MutableList<Series>>()

        chartData.lines().forEach { line ->
            if (line.isNotBlank()) {
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 3) {
                    val groupName = parts[0]
                    val label = parts[1]
                    val value = parts[2].toDoubleOrNull() ?: 0.0
                    val color = if (parts.size > 3 && parts[3].isNotBlank()) parts[3] else null

                    // Create itemDisplay if color is provided
                    val itemDisplay = if (color != null) {
                        BarDisplay(
                            id = UUID.randomUUID().toString(),
                            baseColor = color,
                            vBar = vBar,
                            useDark = useDark
                        )
                    } else null

                    if (!groupMap.containsKey(groupName)) {
                        groupMap[groupName] = mutableListOf()
                    }

                    groupMap[groupName]?.add(Series(label, value, itemDisplay))
                }
            }
        }

        // Convert groupMap to groups list
        val groups = groupMap.map { (name, series) -> 
            Group(name, series)
        }.toMutableList()

        // Create display object
        val display = BarGroupDisplay(
            baseColor = baseColor,
            vBar = vBar,
            condensed = condensed,
            useDark = useDark,
            scale = scale
        )

        return BarGroup(
            title = title,
            yLabel = yLabel,
            xLabel = xLabel,
            groups = groups,
            display = display
        )
    }
}
