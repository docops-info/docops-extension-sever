package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import kotlinx.serialization.json.Json

class ComparisonChartHandler : DocOpsHandler{

    fun handleSVG(payload: String) : String {
        val maker = ComparisonTableMaker()
        val comparisonChart = Json.decodeFromString<ComparisonChart>(payload)
        val svg = maker.make(comparisonChart)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}