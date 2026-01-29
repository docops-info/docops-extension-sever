package io.docops.docopsextensionssupport.chart.treemap

import io.docops.docopsextensionssupport.util.ParsingUtils
import kotlin.text.get

/**
 * Parser for treemap textual markup format
 * Supports configuration section followed by data rows
 */
class TreemapParser {

    fun parse(payload: String, useDark: Boolean): Treemap {
        val (config, chartData) = ParsingUtils.parseConfigAndData(payload)

        val title = config["title"] ?: "Budget Allocation"
        val subtitle = config["subtitle"] ?: ""
        val width = config["width"]?.toIntOrNull() ?: 1200
        val height = config["height"]?.toIntOrNull() ?: 800
        val theme = config["theme"] ?: "modern"
        val paletteType = config["paletteType"] ?: ""
        val fontFamily = config["fontFamily"] ?: ""
        val showValues = config["showValues"]?.toBoolean() ?: true
        val showPercentages = config["showPercentages"]?.toBoolean() ?: true
        val animationDelay = config["animationDelay"]?.toDoubleOrNull() ?: 0.1

        // Parse data rows: Label | Value | Description | Color | Metric
        val items = mutableListOf<TreemapItem>()
        chartData.lines().filter { it.isNotBlank() }.forEach { line ->
            val parts = line.split("|").map { it.trim() }
            if (parts.size >= 2) {
                val label = parts[0]
                val value = parts[1].toDoubleOrNull() ?: 0.0
                val description = if (parts.size > 2) parts[2] else ""
                val color = if (parts.size > 3 && parts[3].isNotBlank()) parts[3] else null
                val metric = if (parts.size > 4) parts[4] else ""

                items.add(TreemapItem(label, value, description, color, metric))
            }
        }

        val display = TreemapDisplay(
            width = width,
            height = height,
            useDark = useDark,
            theme = theme,
            paletteType = paletteType,
            fontFamily = fontFamily,
            showValues = showValues,
            showPercentages = showPercentages,
            animationDelay = animationDelay
        )

        return Treemap(title, subtitle, items, display)
    }
}
