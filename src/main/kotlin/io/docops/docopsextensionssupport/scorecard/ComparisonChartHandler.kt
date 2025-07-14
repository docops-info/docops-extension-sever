package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.json.Json

class ComparisonChartHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    fun handleSVG(payload: String) : String {
        val maker = ComparisonTableMaker()
        val comparisonChart = Json.decodeFromString<ComparisonChart>(payload)
        val svg = maker.make(comparisonChart)
        csvResponse.update(comparisonChart.toCsv())
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}