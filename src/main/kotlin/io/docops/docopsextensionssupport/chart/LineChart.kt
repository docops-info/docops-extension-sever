package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.svgsupport.textWidth
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class LineChart (val id: String = UUID.randomUUID().toString(), val title: String, val points: MutableList<Points>)

@Serializable
data class Points(val label: String, val points: MutableList<Point>)

@Serializable
data class Point(val label: String, val y: Double)

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