package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.diagram.piesToCsv
import io.docops.docopsextensionssupport.web.CsvRequest
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class PieSliceHandler : DocOpsHandler{

    fun handleSVG(payload: String) : String {

        val pieChartImproved = PieChartImproved()
        val svg = pieChartImproved.makePieSvg(payload)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }

    override fun toCsv(request: CsvRequest): CsvResponse {
        val pieChartImproved = PieChartImproved()
        return pieChartImproved.payloadToCsv(request.content)
    }
}