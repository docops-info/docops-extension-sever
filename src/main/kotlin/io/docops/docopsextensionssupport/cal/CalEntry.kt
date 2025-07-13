package io.docops.docopsextensionssupport.cal

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable

@Serializable
class CalEntry (val year:Int, val month: String)

fun CalEntry.toCsv(): CsvResponse {
    val headers = listOf<String>("Month", "Year")
    val rows = mutableListOf<List<String>>()
    rows.add(listOf(month, year.toString()))
    return CsvResponse(headers, rows)
}