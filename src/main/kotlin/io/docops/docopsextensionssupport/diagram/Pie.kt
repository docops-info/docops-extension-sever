package io.docops.docopsextensionssupport.diagram

import kotlinx.serialization.Serializable
import kotlin.math.max

@Serializable
class Pie (val percent: Float, val label: String = "")

@Serializable
class PieDisplay(val baseColor: String= "#3ABEF9", val outlineColor: String= "#050C9C")

@Serializable
data class Pies(val pies: MutableList<Pie>, val pieDisplay: PieDisplay = PieDisplay())

fun Pies.maxRows() : Int {
    var maxRows = 1
    pies.forEach { pie ->
        val sp = pie.label.split(" ")
         maxRows = max(maxRows, sp.size)
    }
    return maxRows
}