package io.docops.docopsextensionssupport.chart.bar


import io.docops.docopsextensionssupport.chart.NiceScale
import io.docops.docopsextensionssupport.support.VisualDisplay
import io.docops.docopsextensionssupport.svgsupport.formatDecimal
import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
class BarGroup @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toHexString(),
    val title: String,
    val yLabel: String? = "",
    val xLabel: String? = "",
    val groups: MutableList<Group>,
    val display: BarGroupDisplay = BarGroupDisplay()
)

@Serializable
class BarGroupDisplay @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toHexString(),
    val baseColor: String = "#D988B9",
    val barSeriesFontStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:14px; text-anchor:middle",
    val barSeriesLabelFontStyle: String = "font-family: Arial,Helvetica, sans-serif;  font-size:12px; text-anchor:start;",
    val barFontValueStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:12px;",
    val titleStyle: String = "font-family: Arial,Helvetica, sans-serif;  font-size:14px; text-anchor:middle",
    val xLabelStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:12px; text-anchor:middle",
    val yLabelStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:12px; text-anchor:middle",
    val lineColor: String = "#FFBB5C",
    val vBar: Boolean = false,
    val condensed: Boolean = false,
    override var useDark: Boolean = false,
    override val visualVersion: Int = 1,
    val theme: String = "classic",
    val scale: Double = 1.0,
    val paletteType: String = "" // Empty string means use defaults, otherwise specify palette name
) : VisualDisplay

@Serializable
class Group(val label: String, val series: MutableList<Series>)


fun BarGroup.calcWidth(): Int {
    val count = this.groups.sumOf { it.series.size }
    // Calculate total width based on:
    // - Initial offset: 110 pixels (increased from 90 to accommodate y-axis label and tick marks)
    // - Bar width: 40 pixels
    // - Spacing between bars: 45 pixels (includes the bar width)
    // - Group spacing: 2 pixels
    val totalBarsWidth = count * 45.0
    val totalGroupSpacing = (groups.size - 1) * 2.0
    val sum = (110.0 + totalBarsWidth + totalGroupSpacing).toInt()

    // Ensure minimum width of 800 pixels
    if (sum <= 800) {
        return 800
    }
    return sum
}


fun BarGroup.calcHeight(): Int {
    val count = this.groups.sumOf { it.series.size }
    val sum = count * 40 + 2 + (5 * (count - 1))
    if (sum <= 800) {
        return 800
    }
    return sum
}

fun BarGroup.maxData(): Double {
    return this.groups.maxOf { it.series.maxOf { it.value } }
}


fun BarGroup.maxGroup(): Group {
    var countMax = 0
    var maxGroup = Group("", mutableListOf())
    groups.forEach {
        if (countMax < it.series.size) {
            maxGroup = it
            countMax = it.series.size
        }
    }
    return maxGroup
}

fun BarGroup.uniqueLabels(): List<String> {
    val uniqueLabels = mutableListOf<String>()
    this.groups.forEach {
        it.series.forEach { l ->
            l.label?.let { it1 -> uniqueLabels.add(it1) }
        }
    }
    return uniqueLabels.distinct().toList()
}

fun BarGroup.scaleUp(item: Double): Double {
    val m = groups.maxOf { it.series.maxOf { it.value } }
    return (450 * item) / m
}

fun BarGroup.maxValue(): Double {
    val m = groups.maxOf { it.series.maxOf { it.value } }
    return m / 3
}

fun BarGroup.valueFmt(value: Double): String {
    var numberString = ""

    numberString = when {
        abs(value / 1000000) > 1 -> {
            formatDecimal((value / 1000000),1) + "m"
        }

        abs(value / 1000) > 1 -> {
            formatDecimal((value / 1000), 1) + "k"
        }
        else -> {
            value.toInt().toString()
        }
    }
    return numberString
}

fun BarGroup.ticks(): NiceScale {
    val min = this.groups.minOf { it.series.minOf { it.value } }
    val max = this.groups.maxOf { it.series.maxOf { it.value } }
    val nice = NiceScale(min, max)
    return nice
}

fun BarGroup.legendLabel(): MutableList<String> {
    val uniqueLabels = mutableListOf<String>()
    this.groups.forEach {
        it.series.forEach { l ->
            l.label?.let { it1 -> uniqueLabels.add(it1) }

        }
    }
    return uniqueLabels
}

fun BarGroup.toCsv(): CsvResponse {
    val headers = mutableListOf<String>()
    val rows = mutableListOf<List<String>>()

    // Create headers based on the data structure
    headers.add("Group")
    headers.add("Label")
    headers.add("Value")
    if (groups.any { group -> group.series.any { it.itemDisplay != null } }) {
        headers.add("Color")
    }

    // Add data rows
    for (group in groups) {
        for (series in group.series) {
            val row = mutableListOf<String>()
            row.add(group.label)
            series.label?.let { row.add(it) }
            row.add(series.value.toString())

            // Add color if available
            if (headers.contains("Color")) {
                row.add(series.itemDisplay?.baseColor ?: display.baseColor)
            }

            rows.add(row)
        }
    }

    return CsvResponse(headers, rows)


}