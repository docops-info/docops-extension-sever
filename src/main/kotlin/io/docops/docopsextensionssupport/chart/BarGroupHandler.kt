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
        val barChartImproved = BarChartImproved(useDark)
        val svgPair = barChartImproved.makeGroupBarSvg(payload, isPdf)
        csvResponse.update(svgPair.second)
        return svgPair.first
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVGInternal(payload, context.backend, context.useDark)
    }
}
