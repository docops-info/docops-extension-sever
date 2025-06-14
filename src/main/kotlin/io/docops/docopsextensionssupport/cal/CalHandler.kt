package io.docops.docopsextensionssupport.cal


import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import kotlinx.serialization.json.Json

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
}