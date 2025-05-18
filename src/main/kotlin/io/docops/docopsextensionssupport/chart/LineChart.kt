package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.svgsupport.textWidth
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.math.abs

@Serializable
class LineChart (val id: String = UUID.randomUUID().toString(), val title: String, val points: MutableList<Points>, val display: LineChartDisplay = LineChartDisplay(backgroundColor = "#4c4c4c"))

@Serializable
data class Points(val label: String, val points: MutableList<Point>)

@Serializable
data class Point(val label: String, val y: Double)

@Serializable
data class LineChartDisplay (val id: String = UUID.randomUUID().toString(), val backgroundColor: String = "#F5F5F5", val smoothLines: Boolean = false, val showArea: Boolean = true)
fun LineChart.peakHeight(): Double {
    val points = this.points
    val max = points.maxBy{it.points.maxOf { it.y }}
    return max.points.maxOf { it.y } + 100
    //return points.maxOf { it.maxOf { it.points.y} } + 100

}
fun Points.textWidth(): Int {
    val label = this.label
    return label.textWidth("Arial", 10)
}

fun Points.toDoubleArray(): DoubleArray {
    val arr = mutableListOf<Double>()
    points.forEachIndexed{index, point ->
        arr.add(index, point.y)
    }
    return arr.toDoubleArray()
}
fun LineChart.ticks(): NiceScale {
    val min = this.points.minOf { it.points.minOf { it.y }}
    val max = this.points.maxOf { it.points.maxOf{it.y} }
    val nice = NiceScale(min, max)
    return nice
}
fun LineChart.valueFmt(value: Double): String {
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
fun LineChart.scaleUp(item: Double): Double {
    val m = points.maxOf { it.points.maxOf { it.y } }
    return (500 * item) / m
}
