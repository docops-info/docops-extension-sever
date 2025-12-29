package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import io.docops.docopsextensionssupport.web.ShapeResponse
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * QuadrantHandler class is responsible for handling requests related to quadrant chart SVG images.
 * Supports both JSON and table format for quadrant charts.
 * Updated to use QuadrantChartGenerator for better chart generation.
 */
class QuadrantHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    /**
     * Handles the SVG request and returns the SVG image as a byte array.
     *
     * @param payload The compressed and encoded SVG payload.
     * @param type The type of the SVG image.
     * @param scale The scale of the SVG image.
     * @param useDark A boolean indicating whether to use dark mode for the SVG image.
     * @param title The title of the quadrant chart.
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
        val svg = fromRequestToQuadrant(payload, scale = scale.toFloat(), useDark = useDark, title = title, backend = backend)
        return svg.shapeSvg
    }

    /**
     * Converts the request payload to a quadrant chart and generates the SVG using QuadrantChartGenerator.
     *
     * @param contents The uncompressed payload content.
     * @param scale The scale of the SVG image.
     * @param useDark A boolean indicating whether to use dark mode for the SVG image.
     * @param title The title of the quadrant chart.
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
        val (points, config) = if (isTableFormat(contents)) {
            parseTableData(contents, title, useDark)
        } else {
            parseJsonData(contents, title, useDark)
        }

        val generator = QuadrantChartGenerator()
        val svgContent = generator.generateSVG(points, config, useDark)
        csvResponse.update(points.toCsv())
        return ShapeResponse(svgContent, width=800f, height= 1000f)
    }

    /**
     * Determines if the data is in table format
     */
    private fun isTableFormat(data: String): Boolean {
        return data.contains("---") || (!data.trim().startsWith("{") && data.contains("|"))
    }

    /**
     * Parses table-like data into QuadrantPoint list and QuadrantConfig
     */
    private fun parseTableData(data: String, title: String, useDark: Boolean): Pair<List<QuadrantPoint>, QuadrantConfig> {
        val lines = data.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val points = mutableListOf<QuadrantPoint>()

        // Default configuration
        var chartTitle = title.ifEmpty { "Strategic Priority Matrix" }
        var chartSubtitle = "Impact vs. Effort Analysis"
        var xAxisLabel = "EFFORT REQUIRED"
        var yAxisLabel = "IMPACT LEVEL"
        var quadrantLabels = mapOf(
            "top-right" to "HIGH IMPACT\nHigh Effort",
            "top-left" to "STRATEGIC\nLow Effort",
            "bottom-left" to "FILL-INS\nLow Impact",
            "bottom-right" to "THANKLESS\nHigh Effort"
        )

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
                            "q1label" -> quadrantLabels = quadrantLabels + ("top-right" to value)
                            "q2label" -> quadrantLabels = quadrantLabels + ("top-left" to value)
                            "q3label" -> quadrantLabels = quadrantLabels + ("bottom-left" to value)
                            "q4label" -> quadrantLabels = quadrantLabels + ("bottom-right" to value)
                        }
                    }
                }
                inDataSection && line.contains("|") && !isHeaderRow(line) -> {
                    // Process data rows
                    val parts = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                    if (parts.size >= 3) {
                        val label = parts[0]
                        val x = parts[1].toDoubleOrNull() ?: 50.0
                        val y = parts[2].toDoubleOrNull() ?: 50.0
                        val category = if (parts.size > 3) parts[3] else null
                        points.add(QuadrantPoint(x, y, label, category))
                    }
                }
            }
        }

        val config = QuadrantConfig(
            title = chartTitle,
            xAxisLabel = xAxisLabel,
            yAxisLabel = yAxisLabel,
            quadrantLabels = quadrantLabels
        )

        return Pair(points, config)
    }

    /**
     * Parses JSON data into QuadrantPoint list and QuadrantConfig
     */
    private fun parseJsonData(contents: String, title: String, useDark: Boolean): Pair<List<QuadrantPoint>, QuadrantConfig> {
        return try {
            // Try to parse as QuadrantCharts first (multiple charts)
            val charts = Json.decodeFromString<QuadrantCharts>(contents)
            val chart = if (charts.charts.isNotEmpty()) charts.charts[0] else QuadrantChart()
            convertToPointsAndConfig(chart, title, useDark)
        } catch (e: Exception) {
            try {
                // Try to parse as single QuadrantChart
                val chart = Json.decodeFromString<QuadrantChart>(contents)
                convertToPointsAndConfig(chart, title, useDark)
            } catch (e: Exception) {
                // Fallback to empty data
                val defaultConfig = QuadrantConfig(title = title.ifEmpty { "Quadrant Chart" })
                Pair(emptyList(), defaultConfig)
            }
        }
    }

    /**
     * Converts QuadrantChart to QuadrantPoint list and QuadrantConfig
     */
    private fun convertToPointsAndConfig(chart: QuadrantChart, title: String, useDark: Boolean): Pair<List<QuadrantPoint>, QuadrantConfig> {
        // Convert old QuadrantChart points to new QuadrantPoint format
        val points = chart.points.map { oldPoint ->
            QuadrantPoint(
                x = oldPoint.x.toDouble(),
                y = oldPoint.y.toDouble(),
                label = oldPoint.label,
            )
        }

        val config = QuadrantConfig(
            title = title.ifEmpty { chart.title },
            xAxisLabel = chart.xAxisLabel,
            yAxisLabel = chart.yAxisLabel,
            quadrantLabels = mapOf(
                "top-right" to "${chart.q1Label}\n${chart.q1Description}",
                "top-left" to "${chart.q2Label}\n${chart.q2Description}",
                "bottom-left" to "${chart.q3Label}\n${chart.q3Description}",
                "bottom-right" to "${chart.q4Label}\n${chart.q4Description}"
            )
        )

        return Pair(points, config)
    }

    /**
     * Helper function to detect header rows in table format
     */
    private fun isHeaderRow(line: String): Boolean {
        val trimmedLine = line.trim()

        // Check for markdown table separator rows
        if (trimmedLine.matches(Regex("^\\|[\\s\\-\\|]+\\|$"))) {
            return true
        }

        // Check if this is an actual header row (contains common header keywords AND no numeric data)
        val parts = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.size >= 3) {
            val lowerLine = line.lowercase()
            val hasHeaderKeywords = lowerLine.contains("label") ||
                    lowerLine.contains("category") ||
                    (lowerLine.contains("x") && lowerLine.contains("y"))

            // If it has header keywords and no numeric values in expected positions, it's a header
            if (hasHeaderKeywords) {
                val secondPart = parts.getOrNull(1)?.toDoubleOrNull()
                val thirdPart = parts.getOrNull(2)?.toDoubleOrNull()
                return secondPart == null && thirdPart == null
            }
        }

        return false
    }



    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.type, context.scale, context.useDark, context.title, context.backend)
    }
}