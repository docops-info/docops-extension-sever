package io.docops.docopsextensionssupport.cal


import io.docops.docopsextensionssupport.web.CsvRequest
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import kotlinx.serialization.json.Json
import org.joda.time.DateTime
import java.util.*


class CalHandler : DocOpsHandler {

    fun handleSVG(payload: String) : String {
        val calMaker = CalMaker()
        var svg = ""
        if(payload.trim().isEmpty()) {
            svg = calMaker.makeCalendar(null)
        } else {
            val calEntry = Json.decodeFromString<CalEntry>(payload)
            svg = calMaker.makeCalendar(calEntry)
        }
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }

    override fun toCsv(request: CsvRequest): CsvResponse {
        val now = Calendar.getInstance()
        val monthName = DateTime.now().withMonthOfYear(now.get(Calendar.MONTH)).toString("MMM")
        var calEntry  = CalEntry(now.get(Calendar.YEAR),monthName)
        if (request.content.trim().isNotEmpty()) {
            calEntry = Json.decodeFromString<CalEntry>(request.content)
        }
        return calEntry.toCsv()
    }
}