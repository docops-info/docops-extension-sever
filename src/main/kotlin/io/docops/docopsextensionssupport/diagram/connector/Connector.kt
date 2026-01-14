package io.docops.docopsextensionssupport.diagram.connector

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable

@Serializable
class Connector (val text: String, val baseColor: String? = "#E14D2A", val description: String = "") {

     var start = 0

    fun textToLines(): MutableList<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        words.forEach {
            lines.add(it)
        }
        start = if(lines.size == 2) {
            45
        }else if(lines.size==3) {
            36
        } else {
            65
        }
        return lines
    }
}

@Serializable
data class Connectors(val connectors: MutableList<Connector>)

/**
 * Convert a list of Connectors to CsvResponse
 */
fun List<Connector>.toCsv(): CsvResponse {
    val headers = listOf("Text", "Description", "Base Color")

    val rows = this.map { connector ->
        listOf(
            connector.text,
            connector.description,
            connector.baseColor ?: "#E14D2A"
        )
    }

    return CsvResponse(headers, rows)
}
