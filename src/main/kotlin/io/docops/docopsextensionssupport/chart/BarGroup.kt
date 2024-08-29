package io.docops.docopsextensionssupport.chart

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

@Serializable
class BarGroup(
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
    val barSeriesFontStyle: String = "font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:9px; text-anchor:middle",
    val barSeriesLabelFontStyle: String = "font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:9px; text-anchor:start;",
    val barFontValueStyle: String = "font-family: Arial,Helvetica, sans-serif; fill: #111111; font-size:9px;",
    val titleStyle: String = "font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:14px; text-anchor:middle",
    val xLabelStyle: String = "font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:12px; text-anchor:middle",
    val yLabelStyle: String = "font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:12px; text-anchor:middle",
    val lineColor: String = "#FFBB5C"
)

@Serializable
class Group(val label: String, val series: MutableList<Series>)


fun BarGroup.calcWidth(): Int {
    val count = this.groups.sumOf { it.series.size }
    val sum = count * 24 + 5 + (3 * count)
    if (sum <= 512) {
        return 512
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
fun BarGroup.scaleUp(item: Double): Double {
    val m = groups.maxOf { it.series.maxOf { it.value } }
    return (450 * item) / m
}

fun BarGroup.valueFmt(value: Double): String {
    var numberString : String = ""
    numberString = when {
        abs(value / 1000000) > 1 -> {
            (value / 1000000).toString() + "m"

        }
        abs(value / 1000) > 1 -> {
            (value / 1000).toString() + "k"

        }
        else -> {
            value.toInt().toString()
        }
    }
    return numberString
}