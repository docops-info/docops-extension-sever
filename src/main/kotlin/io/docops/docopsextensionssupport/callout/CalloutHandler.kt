package io.docops.docopsextensionssupport.callout

import io.docops.docopsextensionssupport.svgsupport.addSvgMetadata
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit

/**
 * Generic handler for callout visualizations
 */

class CalloutHandler {

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
        height: Int = 600
    ): String {
        val uncompressedPayload = uncompressString(payload)
        return makeSvgPlainText(uncompressedPayload, width, height)
    }

    fun makeSvgPlainText(uncompressedPayload: String, width: Int, height: Int): String {
        val maker = CalloutMaker()

        // Parse the callout type from the uncompressed payload
        val calloutType = extractCalloutType(uncompressedPayload)

        // Check if the data is in table format (contains "---" separator)
        val svg = if (uncompressedPayload.contains("---") || !uncompressedPayload.trim().startsWith("{")) {
            // Use table format parsing
            when (calloutType) {
                "metrics" -> maker.createMetricsFromTable(uncompressedPayload, width, height)
                "timeline" -> maker.createTimelineFromTable(uncompressedPayload, width, height)
                else -> maker.createSystematicApproachFromTable(uncompressedPayload, width, height)
            }
        } else {
            // Use traditional JSON format
            when (calloutType) {
                "metrics" -> maker.createMetricsSvg(uncompressedPayload, width, height)
                "timeline" -> maker.createTimelineSvg(uncompressedPayload, width, height)
                else -> maker.createSystematicApproachSvg(uncompressedPayload, width, height)
            }
        }

        return addSvgMetadata(svg)
    }


}
