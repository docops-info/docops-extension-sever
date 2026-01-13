package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.line.LineChartImproved
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext

class LineHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    fun handleSVG(payload: String, useDark: Boolean) : String {
        val lineChartImproved= LineChartImproved()
        val svg = lineChartImproved.makeLineSvg(payload, csvResponse, useDark)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.useDark)
    }
}