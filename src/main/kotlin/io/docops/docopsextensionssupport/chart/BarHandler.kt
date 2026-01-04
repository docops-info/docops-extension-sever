package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext

class BarHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    fun handleSVGInternal(payload: String, useDark: Boolean, backend: String) : String {
        val barChartImproved = BarChartImproved(useDark)
        val svg = barChartImproved.makeBarSvg(payload, csvResponse, backend)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVGInternal(payload, context.useDark, context.backend)
    }
}