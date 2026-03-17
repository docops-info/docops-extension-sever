package io.docops.docopsextensionssupport.callout

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

/**
 * Generic handler for callout visualizations
 */

class CalloutHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    fun extractCalloutType(payload: String): String {
        var type = "systematic"
        for (line in payload.split("\n")) {
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("type=")) {
                type = trimmedLine.substring(5).trim()
                break
            }
        }
        return type
    }

    /**
     * Helper method to create callout SVGs
     */
    fun makeCalloutSvg(
        payload: String,
        outputFormat: String = "SVG",
        width: Int = 800,
        height: Int = 600,
        useDark: Boolean = false
    ): Pair<String, CsvResponse> {

        return makeSvgPlainText(payload, width, height, useDark)
    }

    fun makeSvgPlainText(uncompressedPayload: String, width: Int, height: Int, useDark: Boolean): Pair<String, CsvResponse> {
        val maker = CalloutMaker(useDark)
        val calloutType = extractCalloutType(uncompressedPayload)
        val svg = when (calloutType) {
            "metrics" -> maker.createMetricsFromTable(uncompressedPayload, width, height)
            "timeline" -> maker.createTimelineFromTable(uncompressedPayload, width, height)
            else -> maker.createSystematicApproachFromTable(uncompressedPayload, width, height)
        }
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val svgOut =  makeCalloutSvg(payload=payload, outputFormat= context.type, useDark = context.useDark)
        csvResponse.update(svgOut.second)
        return svgOut.first
    }


}
