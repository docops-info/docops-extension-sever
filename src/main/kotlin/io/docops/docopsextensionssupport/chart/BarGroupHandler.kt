package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.bar.BarChartImproved
import io.docops.docopsextensionssupport.chart.bar.BarGroup
import io.docops.docopsextensionssupport.chart.bar.BarGroupMaker
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.json.Json

class BarGroupHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){
    fun handleSVGInternal(payload: String, backend: String, useDark: Boolean): String {
        val isPdf = backend == "pdf"
        // Check if the data is in table format (contains "---" separator)
        val svg = if (payload.contains("---") || !payload.trim().startsWith("{")) {
            // Use BarChartImproved for table format
            val barChartImproved = BarChartImproved(useDark)
            barChartImproved.makeGroupBarSvg(payload,  isPdf)
        } else {
            // Use traditional JSON format
            val maker = BarGroupMaker(useDark)
            val bar = Json.decodeFromString<BarGroup>(payload)
            if(bar.display.vBar) {
                maker.makeVGroupBar(bar, isPdf)
            } else if (bar.display.condensed) {
                maker.makeCondensed(bar)
            } else {
                maker.makeBar(bar, isPdf)
            }
        }
        csvResponse.update(svg.second)

        return svg.first
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVGInternal(payload, context.backend, context.useDark)
    }
}
