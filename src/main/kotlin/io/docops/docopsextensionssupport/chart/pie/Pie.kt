package io.docops.docopsextensionssupport.chart.pie

import io.docops.docopsextensionssupport.support.VisualDisplay
import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
class Pie(val percent: Float, val label: String = "")

@Serializable
data class PieDisplay @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = "pie_"+  Uuid.random().toHexString(),
    val baseColor: String = "#3ABEF9",
    val outlineColor: String = "#050C9C",
    val scale: Float = 1.0f,
    override val useDark: Boolean = false,
    override val visualVersion: Int = 1,
) : VisualDisplay

@Serializable
data class Pies(val pies: MutableList<Pie>, val pieDisplay: PieDisplay = PieDisplay()) {
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