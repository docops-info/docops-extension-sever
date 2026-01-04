package io.docops.docopsextensionssupport.cal

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable

@Serializable
class CalEntry (val year:Int, val month: String, var darkMode: Boolean = false)

fun CalEntry.toCsv(): CsvResponse {
    val headers = listOf<String>("Month", "Year", "Dark Mode")
    val rows = mutableListOf<List<String>>()
    rows.add(listOf(month, year.toString(), darkMode.toString()))
    return CsvResponse(headers, rows)
}