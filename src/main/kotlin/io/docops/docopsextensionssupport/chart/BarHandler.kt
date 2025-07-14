package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext

class BarHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    fun handleSVG(payload: String) : String {
        val barChartImproved = BarChartImproved()
        val svg = barChartImproved.makeBarSvg(payload, csvResponse)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}