package io.docops.docopsextensionssupport.callout

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext

/**
 * Generic handler for callout visualizations
 */

class CalloutHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    /**
     * Extracts the callout type from the uncompressed payload
     * Looks for "type=metrics", "type=timeline", etc. in text format
     * or "type": "metrics" in JSON format
     */
    private fun extractCalloutType(payload: String): String {
        // Default type is systematic
        var type = "systematic"

        val trimmedPayload = payload.trim()

        // Check if payload is in JSON format
        if (trimmedPayload.startsWith("{") && trimmedPayload.endsWith("}")) {
            // Look for "type": "value" pattern in JSON
            val typeRegex = """"type"\s*:\s*"([^"]+)"""".toRegex()
            val matchResult = typeRegex.find(trimmedPayload)
            if (matchResult != null && matchResult.groupValues.size > 1) {
                type = matchResult.groupValues[1].trim()
                return type
            }
        }

        // If not JSON or type not found in JSON, look for type= in the payload
        val lines = payload.split("\n")
        for (line in lines) {
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
    ): String {

        return makeSvgPlainText(payload, width, height, useDark)
    }

    fun makeSvgPlainText(uncompressedPayload: String, width: Int, height: Int, useDark: Boolean): String {
        val maker = CalloutMaker(csvResponse)

        // Parse the callout type from the uncompressed payload
        val calloutType = extractCalloutType(uncompressedPayload)

        // Check if the data is in table format (contains "---" separator)
        val svg = if (uncompressedPayload.contains("---") || !uncompressedPayload.trim().startsWith("{")) {
            // Use table format parsing
            when (calloutType) {
                "metrics" -> maker.createMetricsFromTable(uncompressedPayload, width, height, useDark)
                "timeline" -> maker.createTimelineFromTable(uncompressedPayload, width, height, useDark)
                else -> maker.createSystematicApproachFromTable(uncompressedPayload, width, height, useDark)
            }
        } else {
            // Use traditional JSON format
            when (calloutType) {
                "metrics" -> maker.createMetricsSvg(uncompressedPayload, width, height)
                "timeline" -> maker.createTimelineSvg(uncompressedPayload, width, height)
                else -> maker.createSystematicApproachSvg(uncompressedPayload, width, height)
            }
        }

        return svg.first
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return makeCalloutSvg(payload=payload, outputFormat= context.type, useDark = context.useDark)
    }


}
