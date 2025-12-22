package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.math.abs

@Serializable
class Bar(val title: String, val yLabel: String? = "", val xLabel: String? = "", val series: MutableList<Series>, val display: BarDisplay = BarDisplay())

@Serializable
class Series(val label: String? = "", val value: Double, val itemDisplay: BarDisplay? = null, val id: String = UUID.randomUUID().toString())

@Serializable
class BarDisplay(val id: String = UUID.randomUUID().toString(), val baseColor: String = "#FE7A36", val type: String = "R", val vBar : Boolean = false, var useDark: Boolean = false, val sorted: Boolean=false, val theme: String = "classic", val scale: Float = 1.0f)


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
fun Bar.centerWidth() = calcWidth() / 2

fun Bar.calcLeftPadding() : Int {
    return (500 - (this.series.size * 44))/2
}

fun Bar.innerX(): Int {
    var innerx = 20
    if(calcWidth() > 512)
    {
        innerx = 80
    }
    return innerx
}

fun Bar.ticks(): NiceScale {
    val min = this.series.minOf { scaleUp(it.value) }
    val max = this.series.maxOf { scaleUp(it.value) }
    val nice = NiceScale(min, max)
    return nice


}
fun Bar.sorted() {
    if(this.display.sorted) {
        series.sortBy { it.value }
    }
}
fun Bar.valueFmt(value: Double): String {
    var numberString : String = ""
    numberString = when {
        abs(value / 1000000) > 1 -> {
            "${(value / 1000000)} m"

        }
        abs(value / 1000) > 1 -> {
            "${(value / 1000)} k"

        }
        else -> {
            value.toInt().toString()
        }
    }
    return numberString
}

fun Bar.toCsv(): CsvResponse {
    val headers = listOf<String>("Category", "Value")
    val rows = mutableListOf<List<String>>()
    series.forEach {
        rows.add(listOf(it.label ?: "", it.value.toString()))
    }
    return CsvResponse(headers, rows)
}