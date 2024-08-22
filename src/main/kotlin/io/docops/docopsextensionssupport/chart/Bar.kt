package io.docops.docopsextensionssupport.chart

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class Bar(val title: String, val yLabel: String? = "", val xLabel: String? = "", val series: MutableList<Series>, val display: BarDisplay = BarDisplay())

@Serializable
class Series(val label: String? = "", val value: Float, val itemDisplay: BarDisplay? = null)

@Serializable
class BarDisplay(val id: String = UUID.randomUUID().toString(), val showGrid: Boolean = true, val baseColor: String = "#FE7A36", val barFontColor: String = "#111111", val type: String = "R")


fun Bar.seriesTotal(): Float {
    var sum = 0.0f
    this.series.forEach {
        sum += it.value
    }
    return sum
}

fun Bar.weightedPercentage(series: Series, maxHeight: Int): Float {
    return (series.value / this.seriesTotal() * maxHeight )
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