package io.docops.docopsextensionssupport.chart

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class LineChart (val id: String = UUID.randomUUID().toString(), val title: String, val points: MutableList<MutableList<Point>>)

@Serializable
data class Point(val label: String, val y: Double)

fun LineChart.peakHeight(): Double {
    return points.maxOf { it.maxOf { it.y} } + 100

}