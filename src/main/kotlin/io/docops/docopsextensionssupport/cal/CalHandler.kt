package io.docops.docopsextensionssupport.cal


import io.docops.docopsextensionssupport.web.*
import kotlinx.serialization.json.Json
import org.joda.time.DateTime
import java.util.*


class CalHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    fun handleSVG(payload: String) : String {
        val calMaker = CalMaker()
        var svg = ""
        val now = Calendar.getInstance()
        val monthName = DateTime.now().withMonthOfYear(now.get(Calendar.MONTH)).toString("MMM")
        var calEntry  = CalEntry(now.get(Calendar.YEAR),monthName)
        if (payload.trim().isNotEmpty()) {
            calEntry = Json.decodeFromString<CalEntry>(payload)
        }
        svg = calMaker.makeCalendar(calEntry)
        val csv = calEntry.toCsv()
        csvResponse.update(csv)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }

}