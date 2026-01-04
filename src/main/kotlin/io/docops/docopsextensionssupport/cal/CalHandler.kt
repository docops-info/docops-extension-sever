package io.docops.docopsextensionssupport.cal


import io.docops.docopsextensionssupport.web.*
import kotlinx.serialization.json.Json
import org.joda.time.DateTime
import java.util.*


class CalHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    fun handleSVG(payload: String, useDark: Boolean) : String {

        val cal = CalMaker()
        val entry = Json.decodeFromString<CalEntry>(payload)
        entry.darkMode = useDark
        val svg = cal.makeCalendar(entry)
        val csv = entry.toCsv()
        csvResponse.update(csv)
        return svg

    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.useDark)
    }

}

