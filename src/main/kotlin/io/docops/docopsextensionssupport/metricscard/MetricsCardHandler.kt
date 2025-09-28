package io.docops.docopsextensionssupport.metricscard

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

/**
 * Handler for metrics card visualizations
 */
class MetricsCardHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    /**
     * Handles the creation of metrics card SVGs
     */
    fun handleSVG(
        payload: String,
        type: String = "SVG",
        scale: String = "1.0",
        useDark: Boolean = false,
        width: Int = 800,
        height: Int = 400
    ): String {
        val maker = MetricsCardMaker(csvResponse)
        val svg = maker.createMetricsCardSvg(payload = payload, width = width, height = height, useDark = useDark)
        return svg.first
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.type, context.scale, context.useDark)
    }
}