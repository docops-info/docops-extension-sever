package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import java.net.URLDecoder

class LineHandler : DocOpsHandler{

    fun handleSVG(payload: String) : String {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val lineChartImproved= LineChartImproved()
        val svg = lineChartImproved.makeLineSvg(data)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}