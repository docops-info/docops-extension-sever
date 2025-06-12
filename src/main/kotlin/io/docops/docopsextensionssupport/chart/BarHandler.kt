package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import java.net.URLDecoder

class BarHandler : DocOpsHandler{

    fun handleSVG(payload: String) : String {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val barChartImproved = BarChartImproved()
        val svg = barChartImproved.makeBarSvg(data)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}