package io.docops.docopsextensionssupport.metricscard

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

/**
 * Handler for metrics card visualizations
 */
class MetricsCardHandler : DocOpsHandler{

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
        val maker = MetricsCardMaker()
        val svg = maker.createMetricsCardSvg(payload, width, height)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.type, context.scale, context.useDark)
    }
}