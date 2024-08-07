package io.docops.docopsextensionssupport.diagram

import kotlinx.serialization.Serializable
import kotlin.math.max

@Serializable
class Pie (val percent: Float, val label: String = "", val color: String = "#E14D2A")
@Serializable
data class Pies(val pies: MutableList<Pie>)

fun Pies.maxRows() : Int {
    var maxRows = 1
    pies.forEach { pie ->
        val sp = pie.label.split(" ")
         maxRows = max(maxRows, sp.size)
    }
    return maxRows
}