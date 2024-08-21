package io.docops.docopsextensionssupport.chart

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class PieSlice(val id: String = UUID.randomUUID().toString(), val label: String, val amount: Double = 0.0, val itemDisplay: SliceItemDisplay? = null)

fun PieSlice.displayColor(index: Int) : String {
    return if(null != itemDisplay?.color) {
        itemDisplay.color
    } else {
        DefaultSliceColors[index]
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

val DefaultSliceColors = mutableListOf<String>(
    "#e60049", "#0bb4ff", "#50e991", "#e6d800",
    "#9b19f5", "#ffa300", "#dc0ab4", "#b3d4ff",
    "#00bfa0", "#8bd3c7", "#fdcce5", "#beb9db",
    "#ffee65", "#ffb55a", "#bd7ebe", "#b2e061",
    "#7eb0d5", "#fd7f6f")
