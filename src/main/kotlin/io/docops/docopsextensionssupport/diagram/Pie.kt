package io.docops.docopsextensionssupport.diagram

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.math.max

@Serializable
class Pie (val percent: Float, val label: String = "")

@Serializable
class PieDisplay(val id: String = UUID.randomUUID().toString(), val baseColor: String= "#3ABEF9", val outlineColor: String= "#050C9C", val scale: Float = 1.0f, val useDark: Boolean = false)

@Serializable
data class Pies(val pies: MutableList<Pie>, val pieDisplay: PieDisplay = PieDisplay()){
    fun maxRows(): Int {
        return pies.maxOfOrNull { pie ->
            pie.label.split(" ").size
        } ?: 1
    }

}