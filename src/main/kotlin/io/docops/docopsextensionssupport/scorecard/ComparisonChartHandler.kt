package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import kotlinx.serialization.json.Json
import java.net.URLDecoder

class ComparisonChartHandler : DocOpsHandler{

    fun handleSVG(payload: String) : String {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val maker = ComparisonTableMaker()
        val comparisonChart = Json.decodeFromString<ComparisonChart>(data)
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