package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import io.docops.docopsextensionssupport.web.ShapeResponse
import kotlinx.serialization.json.Json
import org.springframework.http.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * QuadrantHandler class is responsible for handling requests related to quadrant chart SVG images.
 * Supports both JSON and table format for quadrant charts.
 */
class QuadrantHandler : DocOpsHandler{

    /**
     * Handles the SVG request and returns the SVG image as a byte array.
     *
     * @param payload The compressed and encoded SVG payload.
     * @param type The type of the SVG image.
     * @param scale The scale of the SVG image.
     * @param useDark A boolean indicating whether to use dark mode for the SVG image.
     * @param title The title of the quadrant chart.
     * @param numChars The number of characters to display in labels.
     * @param backend The backend to use for rendering.
     * @return The ResponseEntity containing the SVG image as a byte array.
     */
    fun handleSVG(
        payload: String, 
        type: String, 
        scale: String, 
        useDark: Boolean, 
        title: String = "",
        backend: String = "html"
    ): String {
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val svg = fromRequestToQuadrant(data, scale = scale.toFloat(), useDark = useDark, title = title, backend = backend)
        return svg.shapeSvg
    }

    /**
     * Converts the request payload to a quadrant chart and generates the SVG.
     *
     * @param contents The uncompressed payload content.
     * @param scale The scale of the SVG image.
     * @param useDark A boolean indicating whether to use dark mode for the SVG image.
     * @param title The title of the quadrant chart.
     * @param numChars The number of characters to display in labels.
     * @param backend The backend to use for rendering.
     * @return The ShapeResponse containing the SVG content.
     */
    fun fromRequestToQuadrant(
        contents: String, 
        scale: Float, 
        useDark: Boolean, 
        title: String = "",
        backend: String = "html"
    ): ShapeResponse {
        val quadrantChart = if (isTableFormat(contents)) {
            parseTableData(contents, title)
        } else {
            decodeFromJson(contents)
        }
        val maker = QuadrantMaker(quadrantChart, useDark, type = backend)
        return maker.makeQuadrantImage(scale)
    }

    /**
     * Determines if the data is in table format
     */
    private fun isTableFormat(data: String): Boolean {
        return data.contains("---") || (!data.trim().startsWith("{") && data.contains("|"))
    }

    /**
     * Parses table-like data into QuadrantChart object
     */
    private fun parseTableData(data: String, title: String): QuadrantChart {
        val lines = data.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val points = mutableListOf<QuadrantPoint>()
        var chartTitle = title.ifEmpty { "Strategic Priority Matrix" }
        var chartSubtitle = "Impact vs. Effort Analysis"
        var xAxisLabel = "EFFORT REQUIRED"
        var yAxisLabel = "IMPACT LEVEL"
        var q1Label = "HIGH IMPACT"
        var q2Label = "STRATEGIC"
        var q3Label = "FILL-INS"
        var q4Label = "THANKLESS"
        var q1Description = "Low Effort"
        var q2Description = "High Effort"
        var q3Description = "Low Impact"
        var q4Description = "High Effort"

        var inDataSection = false

        for (line in lines) {
            when {
                line == "---" -> {
                    inDataSection = true
                }
                !inDataSection && line.contains(":") -> {
                    // Process metadata before the --- separator
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val value = parts[1].trim()
                        when (key.lowercase()) {
                            "title" -> chartTitle = value
                            "subtitle" -> chartSubtitle = value
                            "xaxislabel" -> xAxisLabel = value
                            "yaxislabel" -> yAxisLabel = value
                            "q1label" -> q1Label = value
                            "q2label" -> q2Label = value
                            "q3label" -> q3Label = value
                            "q4label" -> q4Label = value
                            "q1description" -> q1Description = value
                            "q2description" -> q2Description = value
                            "q3description" -> q3Description = value
                            "q4description" -> q4Description = value
                        }
                    }
                }
                inDataSection && line.contains("|") && !isHeaderRow(line) -> {
                    // Process data rows
                    val parts = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                    if (parts.size >= 3) {
                        val label = parts[0]
                        val x = parts[1].toFloatOrNull() ?: 50f
                        val y = parts[2].toFloatOrNull() ?: 50f
                        val size = if (parts.size > 3) parts[3].toFloatOrNull() ?: 8f else 8f
                        val color = if (parts.size > 4) parts[4] else null
                        val description = if (parts.size > 5) parts[5] else ""
                        points.add(QuadrantPoint(label, x, y, size, color, description))
                    }
                }
            }
        }

        return QuadrantChart(
            title = chartTitle,
            subtitle = chartSubtitle,
            xAxisLabel = xAxisLabel,
            yAxisLabel = yAxisLabel,
            q1Label = q1Label,
            q2Label = q2Label,
            q3Label = q3Label,
            q4Label = q4Label,
            q1Description = q1Description,
            q2Description = q2Description,
            q3Description = q3Description,
            q4Description = q4Description,
            points = points
        )
    }

    /**
     * Helper function to detect header rows in table format
     */
    private fun isHeaderRow(line: String): Boolean {
        val lowerLine = line.lowercase()
        return lowerLine.contains("label") || 
               lowerLine.contains("x") || 
               lowerLine.contains("y") ||
               lowerLine.contains("size") ||
               lowerLine.contains("color") ||
               lowerLine.contains("description")
    }

    /**
     * Decodes JSON data into QuadrantChart object
     */
    private fun decodeFromJson(contents: String): QuadrantChart {
        return try {
            val charts = Json.decodeFromString<QuadrantCharts>(contents)
            if (charts.charts.isNotEmpty()) charts.charts[0] else QuadrantChart()
        } catch (e: Exception) {
            try {
                Json.decodeFromString<QuadrantChart>(contents)
            } catch (e: Exception) {
                QuadrantChart()
            }
        }
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.type, context.scale, context.useDark, context.title, context.backend)
    }
}
