package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext

class PieSliceHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    fun handleSVGInternal(payload: String, backend: String) : String {
        val isPdf = backend == "pdf"
        val pieChartImproved = PieChartImproved()
        val svg = pieChartImproved.makePieSvg(payload, csvResponse, isPdf)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVGInternal(payload, context.backend)
    }

}