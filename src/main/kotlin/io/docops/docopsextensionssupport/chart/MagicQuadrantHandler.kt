package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.quadrant.MagicQuadrantConfig
import io.docops.docopsextensionssupport.chart.quadrant.MagicQuadrantSvgGenerator
import io.docops.docopsextensionssupport.chart.quadrant.QuadrantCompany
import io.docops.docopsextensionssupport.chart.quadrant.toCsvResponse
import io.docops.docopsextensionssupport.util.ParsingUtils
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class MagicQuadrantHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    fun parseTabularData(input: String, useDark: Boolean): MagicQuadrantConfig {
        val (configMap, dataBlock) = ParsingUtils.parseConfigAndData(input)

        val title = configMap["title"] ?: "Magic Quadrant"
        var xAxisLabel = "Ability to Execute"
        var xAxisLabelEnd = ""
        var yAxisLabel = "Completeness of Vision"
        var yAxisLabelEnd = ""
        val visualVersion = configMap["visualVersion"]?.toIntOrNull() ?: 1
        val leadersLabel = configMap["leaders"] ?: "Leaders"
        val challengersLabel = configMap["challengers"] ?: "Challengers"
        val visionariesLabel = configMap["visionaries"] ?: "Visionaries"
        val nichePlayersLabel = configMap["niche"] ?: "Niche Players"

        configMap["xAxis"]?.let { valStr ->
            if (valStr.contains("|")) {
                val parts = valStr.split("|")
                xAxisLabel = parts[0].trim()
                xAxisLabelEnd = parts[1].trim()
            } else {
                xAxisLabel = valStr
            }
        }
        configMap["yAxis"]?.let { valStr ->
            if (valStr.contains("|")) {
                val parts = valStr.split("|")
                yAxisLabel = parts[0].trim()
                yAxisLabelEnd = parts[1].trim()
            } else {
                yAxisLabel = valStr
            }
        }

        val companies = mutableListOf<QuadrantCompany>()
        val dataLines = dataBlock.trim().split('\n').filter { it.isNotBlank() }
        var skipNext = false
        for (line in dataLines) {
            val trimmed = line.trim()
            // Skip header rows (non-numeric second field) and separator rows
            if (skipNext) { skipNext = false; continue }
            if (trimmed.contains("|")) {
                val parts = trimmed.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                if (parts.size >= 3) {
                    val xVal = parts[1].toDoubleOrNull()
                    if (xVal == null) {
                        // This is a header row; skip it and any following separator
                        skipNext = parts.any { it.trim().startsWith("-") }
                        continue
                    }
                    try {
                        val name = parts[0]
                        val x = xVal.coerceIn(0.0, 100.0)
                        val y = parts[2].toDouble().coerceIn(0.0, 100.0)
                        val description = if (parts.size > 3) parts[3] else ""
                        val size = if (parts.size > 4) parts[4].toIntOrNull()?.coerceIn(8, 25) ?: 12 else 12
                        val url = if (parts.size > 5) parts[5] else ""
                        companies.add(QuadrantCompany(name, x, y, description, size, url))
                    } catch (e: NumberFormatException) {
                        println("Warning: Skipping invalid row: $trimmed ${e.message}")
                    }
                }
            }
        }

        if (companies.isEmpty()) {
            throw IllegalArgumentException("No valid company data found. Expected format: Name | X | Y | Description | Size | URL")
        }

        return MagicQuadrantConfig(
            title,
            xAxisLabel = xAxisLabel,
            xAxisLabelEnd = xAxisLabelEnd,
            yAxisLabel = yAxisLabel,
            yAxisLabelEnd = yAxisLabelEnd,
            companies = companies,
            leadersLabel = leadersLabel,
            challengersLabel = challengersLabel,
            visionariesLabel = visionariesLabel,
            nichePlayersLabel = nichePlayersLabel,
            visualVersion = visualVersion,
            useDark = useDark
        )
    }

    fun generateSvg(
        config: MagicQuadrantConfig,
        scale: String = "1.0"
    ): String {

        val generator = MagicQuadrantSvgGenerator()

        return generator.generateMagicQuadrant(config,  scale)
    }



    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val config = parseTabularData(payload, context.useDark)
        csvResponse.update(config.toCsvResponse())
        val svg = generateSvg(config, context.scale)
        return svg
    }
}
