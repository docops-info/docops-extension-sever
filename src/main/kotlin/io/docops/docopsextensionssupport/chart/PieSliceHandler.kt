package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.pie.PieChartImproved
import io.docops.docopsextensionssupport.chart.pie.PieChartImproved.PieSegment
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class PieSliceHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    fun handleSVGInternal(payload: String, backend: String, useDark: Boolean) : String {
        val isPdf = backend == "pdf"
        val pieChartImproved = PieChartImproved()
        val svg = pieChartImproved.makePieSvg(payload, isPdf, useDark)
        csvResponse.update(payloadToSimpleCsv(svg.second))
        return svg.first
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVGInternal(payload, context.backend, context.useDark)
    }

    private fun payloadToSimpleCsv(pieData: List<PieSegment>): CsvResponse {
        val headers = listOf("Label", "Value")
        val rows = pieData.map { segment ->
            listOf(segment.label, segment.value.toString())
        }
        return CsvResponse(headers, rows)
    }
}