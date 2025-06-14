package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class BarHandler : DocOpsHandler{

    fun handleSVG(payload: String) : String {
        val barChartImproved = BarChartImproved()
        val svg = barChartImproved.makeBarSvg(payload)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}