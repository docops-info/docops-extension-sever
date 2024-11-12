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
    val barSeriesFontStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:9px; text-anchor:middle",
    val barSeriesLabelFontStyle: String = "font-family: Arial,Helvetica, sans-serif;  font-size:9px; text-anchor:start;",
    val barFontValueStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:9px;",
    val titleStyle: String = "font-family: Arial,Helvetica, sans-serif;  font-size:14px; text-anchor:middle",
    val xLabelStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:12px; text-anchor:middle",
    val yLabelStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:12px; text-anchor:middle",
    val lineColor: String = "#FFBB5C",
    val vBar : Boolean = true,
    val useDark : Boolean = false,
    val scale: Double = 1.0
)

@Serializable
class Group(val label: String, val series: MutableList<Series>)


fun BarGroup.calcWidth(): Int {
    val count = this.groups.sumOf { it.series.size }
    val sum = count * 24 + 5 + (21 * (count-1))
    if (sum <= 400) {
        return 512
    }
    return sum
}

fun BarGroup.calcHeight(): Int {
    val count = this.groups.sumOf { it.series.size }
    val sum = count * 24 + 5 + (21 * (count-1))
    if (sum <= 400) {
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