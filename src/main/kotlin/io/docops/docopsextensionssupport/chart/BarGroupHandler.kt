package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import kotlinx.serialization.json.Json

class BarGroupHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){
    fun handleSVGInternal(payload: String, backend: String): String {
        val isPdf = backend == "pdf"
        // Check if the data is in table format (contains "---" separator)
        val svg = if (payload.contains("---") || !payload.trim().startsWith("{")) {
            // Use BarChartImproved for table format
            val barChartImproved = BarChartImproved()
            barChartImproved.makeGroupBarSvg(payload, csvResponse, isPdf)
        } else {
            // Use traditional JSON format
            val maker = BarGroupMaker()
            val bar = Json.decodeFromString<BarGroup>(payload)
            if(bar.display.vBar) {
                maker.makeVGroupBar(bar, isPdf)
            } else if (bar.display.condensed) {
                maker.makeCondensed(bar)
            } else {
                maker.makeBar(bar, isPdf)
            }
        }

        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVGInternal(payload, context.backend)
    }
}
