package io.docops.docopsextensionssupport.chart

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.math.abs

@Serializable
class BarGroup(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val yLabel: String? = "",
    val xLabel: String? = "",
    val groups: MutableList<Group>,
    val display: BarGroupDisplay = BarGroupDisplay()
)

@Serializable
class BarGroupDisplay(
    val id: String = UUID.randomUUID().toString(),
    val baseColor: String = "#D988B9",
    val barSeriesFontStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:14px; text-anchor:middle",
    val barSeriesLabelFontStyle: String = "font-family: Arial,Helvetica, sans-serif;  font-size:12px; text-anchor:start;",
    val barFontValueStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:12px;",
    val titleStyle: String = "font-family: Arial,Helvetica, sans-serif;  font-size:14px; text-anchor:middle",
    val xLabelStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:12px; text-anchor:middle",
    val yLabelStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:12px; text-anchor:middle",
    val lineColor: String = "#FFBB5C",
    val vBar : Boolean = false,
    val condensed: Boolean = false,
    val useDark : Boolean = false,
    val scale: Double = 1.0
)

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
    val sum = count * 40 + 2 + (5 * (count-1))
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
            String.format("%.1f", (value / 1000000)) + "m"


        }
        abs(value / 1000) > 1 -> {
            String.format("%.1f", (value / 1000)) + "k"

        }
        else -> {
            value.toInt().toString()
        }
    }
    return numberString
}

fun BarGroup.ticks(): NiceScale {
    val min = this.groups.minOf { it.series.minOf { it.value } }
    val max = this.groups.maxOf { it.series.maxOf{it.value} }
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
