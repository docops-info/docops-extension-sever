package io.docops.docopsextensionssupport.chart

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
}