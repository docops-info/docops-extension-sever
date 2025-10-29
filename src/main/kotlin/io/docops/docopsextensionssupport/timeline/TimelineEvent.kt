// TimelineHandler.kt
package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.web.CsvResponse


data class TimelineEvent(
    val date: String,
    val text: String
)

enum class Orientation {
    HORIZONTAL, VERTICAL
}

data class TimelineConfig(
    val width: Int = 980,
    val events: List<TimelineEvent>,
    val lightColor: String? = null,
    val lightColorIndex: Int? = null,
    val orientation: Orientation = Orientation.VERTICAL
) {
    fun timelineEventsToCsv(): CsvResponse {
        val headers = listOf("Date", "Text")
        val rows = events.map { event ->
            listOf(event.date, event.text)
        }
        return CsvResponse(headers = headers, rows = rows)
    }
}


