package io.docops.docopsextensionssupport.chart

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class Bar(val title: String, val yLabel: String? = "", val xLabel: String? = "", val series: MutableList<Series>, val display: BarDisplay = BarDisplay())

@Serializable
class Series(val label: String? = "", val value: Double, val itemDisplay: BarDisplay? = null, val id: String = UUID.randomUUID().toString())

@Serializable
class BarDisplay(val id: String = UUID.randomUUID().toString(), val showGrid: Boolean = true, val baseColor: String = "#FE7A36", val barFontColor: String = "#111111", val type: String = "R")


fun Bar.seriesTotal() = series.sumOf { it.value }



fun Bar.weightedPercentage(series: Series, maxHeight: Int): Double {
    return (series.value / this.seriesTotal() * maxHeight )
}

fun Bar.scaleUp(item: Double): Double {
    val m = series.maxOf { it.value }
    return (450 * item) / m
}
fun Bar.calcWidth(): Int {
    return if(this.series.size <= 8) {
        512
    } else {
        32 + (this.series.size * 60)
    }
}

fun Bar.calcLeftPadding() : Int {
    return (512 - (this.series.size * 60))/2
}