package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.CsvResponse
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


    fun Pies.piesToCsv(): CsvResponse {
    // Define the headers for the CSV
    val headers = listOf("Label", "Percent", "Base Color", "Outline Color", "Scale", "Dark Mode")

    // Convert each pie to a row
    val rows = pies.map { pie ->
        listOf(
            pie.label,
            pie.percent.toString(),
            pieDisplay.baseColor,
            pieDisplay.outlineColor,
            pieDisplay.scale.toString(),
            pieDisplay.useDark.toString()
        )
    }

    return CsvResponse(headers, rows)
}