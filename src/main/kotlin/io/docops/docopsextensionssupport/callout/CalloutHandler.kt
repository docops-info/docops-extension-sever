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
        return extractString(payload, "type", "systematic")
    }

    private fun extractString(payload: String, key: String, default: String): String {
        for (line in payload.split("\n")) {
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("$key=")) {
                return trimmedLine.substring(key.length + 1).trim()
            }
        }
        return default
    }

    private fun extractInt(payload: String, key: String, default: Int): Int {
        for (line in payload.split("\n")) {
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("$key=")) {
                return trimmedLine.substring(key.length + 1).trim().toIntOrNull() ?: default
            }
        }
        return default
    }

    /**
     * Helper method to create callout SVGs
     */
    fun makeCalloutSvg(
        payload: String,
        outputFormat: String = "SVG",
        width: Int = 800,
        height: Int = 600,
        useDark: Boolean = false,
        scale: String = "1.0"
    ): Pair<String, CsvResponse> {

        return makeSvgPlainText(payload, width, height, useDark, scale)
    }

    fun makeSvgPlainText(uncompressedPayload: String, width: Int, height: Int, useDark: Boolean, scale: String): Pair<String, CsvResponse> {
        val maker = CalloutMaker(useDark)
        val calloutType = extractCalloutType(uncompressedPayload)
        val svg = when (calloutType) {
            "metrics" -> maker.createMetricsFromTable(uncompressedPayload, width, height, scale)
            "timeline" -> maker.createTimelineFromTable(uncompressedPayload, width, height, scale)
            else -> maker.createSystematicApproachFromTable(uncompressedPayload, width, height, scale)
        }
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val width = extractInt(payload, "width", 800)
        val height = extractInt(payload, "height", 600)
        val svgOut =  makeCalloutSvg(payload=payload, outputFormat= context.type, width = width, height = height, useDark = context.useDark, scale = context.scale)
        csvResponse.update(svgOut.second)
        return svgOut.first
    }


}
