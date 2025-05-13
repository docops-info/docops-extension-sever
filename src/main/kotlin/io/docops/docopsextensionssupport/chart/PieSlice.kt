package io.docops.docopsextensionssupport.chart

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.math.abs

@Serializable
class PieSlice(val id: String = UUID.randomUUID().toString(), val label: String, val amount: Double = 0.0, val itemDisplay: SliceItemDisplay? = null)

fun PieSlice.displayColor(index: Int) : String {
    return if(null != itemDisplay?.color) {
        itemDisplay.color
    } else {
        "url(#svgGradientColor_$index)"
    }
}

@Serializable
data class SliceDisplay(val id: String = UUID.randomUUID().toString(), val showLegend: Boolean = true, val legendRows: Int = 4, val donut: Boolean = false, val scale: Float = 1.0f, val useDark: Boolean = false)

@Serializable
data class SliceItemDisplay(val id: String = UUID.randomUUID().toString(), val color: String?)

@Serializable
data class PieSlices(val title: String,val slices: MutableList<PieSlice>, val display: SliceDisplay = SliceDisplay())

fun PieSlices.determineMaxLegendRows(): Int {
   val chunks = this.slices.chunked(this.display.legendRows)
   val max = chunks.maxOf { it.size }
   return max
}

fun PieSlices.sum(): Double {
    return slices.sumOf { it.amount }
}


data class DonutSlice(
    val id: String,
    val percent: Double,
    val amount: Double,
    val color: String,
    val label: String = ""
)

fun PieSlices.toDonutSlices() : MutableList<DonutSlice> {
    val donutSlices = mutableListOf<DonutSlice>()
    slices.forEachIndexed { index, pieSlice ->
        donutSlices.add(pieSlice.toDonutSlice(sum(), index))
    }
    //donutSlices.sortBy{it.percent}
    return donutSlices
}
fun PieSlice.toDonutSlice(sum: Double, idx: Int): DonutSlice {
    return DonutSlice(id = id,
        percent = (amount / sum) * 100,
        amount = amount,
        color = "url(#svgGradientColor_$idx)",
        label = label
        )
}
fun DonutSlice.valueFmt(value: Double): String {
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
