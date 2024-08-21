package io.docops.docopsextensionssupport.chart

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class PieSlice(val id: String = UUID.randomUUID().toString(), val label: String, val amount: Double = 0.0, val itemDisplay: SliceItemDisplay? = null)

fun PieSlice.displayColor(index: Int) : String {
    return if(null != itemDisplay?.color) {
        itemDisplay.color
    } else {
        DefaultChartColors[index]
    }
}

@Serializable
data class SliceDisplay(val id: String = UUID.randomUUID().toString(), val showLegend: Boolean = true, val legendRows: Int = 4)

@Serializable
data class SliceItemDisplay(val id: String = UUID.randomUUID().toString(), val color: String?)

@Serializable
data class PieSlices(val title: String,val slices: MutableList<PieSlice>, val display: SliceDisplay = SliceDisplay())

fun PieSlices.determineMaxLegendRows(): Int {
   val chunks = this.slices.chunked(this.display.legendRows)
   val max = chunks.maxOf { it.size }
   return max
}


