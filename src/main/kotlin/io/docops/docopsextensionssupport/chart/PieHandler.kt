package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.pie.Pie
import io.docops.docopsextensionssupport.chart.pie.PieDisplay
import io.docops.docopsextensionssupport.chart.pie.PieMaker
import io.docops.docopsextensionssupport.chart.pie.PieMakerImproved
import io.docops.docopsextensionssupport.chart.pie.Pies
import io.docops.docopsextensionssupport.chart.pie.piesToCsv
import io.docops.docopsextensionssupport.util.ParsingUtils
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.json.Json

class PieHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    private val json = Json { ignoreUnknownKeys = true }

    fun handleSVGInternal(payload: String, context: DocOpsContext): String {
        val pies = parseInput(payload, context.useDark)

        csvResponse.update(pies.piesToCsv())
        return if (pies.pieDisplay.visualVersion >= 3) {
            PieMakerImproved().makePies(pies.copy(pieDisplay = pies.pieDisplay))
        } else {
            PieMaker().makePies(pies.copy(pieDisplay = pies.pieDisplay))
        }
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVGInternal(payload, context)
    }


    /**
     * Parse input data supporting both JSON and tabular formats
     */
    private fun parseInput(payload: String, useDark: Boolean): Pies {
        val trimmedPayload = payload.trim()

        return if (trimmedPayload.startsWith("{") && trimmedPayload.endsWith("}")) {
            // JSON format
            parseJsonInput(trimmedPayload)
        } else {
            // Tabular format
            parseTabularInput(trimmedPayload, useDark)
        }
    }

    /**
     * Parse JSON input format
     * Example:
     * {
     *   "pies": [
     *     {"percent": 14, "label": "Toys"},
     *     {"percent": 43, "label": "Furniture"}
     *   ],
     *   "pieDisplay": {"baseColor": "#A6AEBF", "outlineColor": "#FA4032", "scale": 4, "useDark": true}
     * }
     */
    private fun parseJsonInput(jsonPayload: String): Pies {
        return try {
            json.decodeFromString<Pies>(jsonPayload)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid JSON format for pie data: ${e.message}", e)
        }
    }

    /**
     * Parse tabular input format
     * Format:
     * baseColor=#A6AEBF
     * outlineColor=#FA4032
     * scale=4
     * useDark=true
     * ---
     * Label | Percent
     * Toys | 14
     * Furniture | 43
     */
    private fun parseTabularInput(payload: String, useDark: Boolean): Pies {
        val (config, data) = ParsingUtils.parseConfigAndData(payload)
        val piesList = mutableListOf<Pie>()

        data.lines().forEach { line ->
            val parts = line.split("|").map { it.trim() }
            if (parts.size >= 2) {
                val label = parts[0]
                val percent = parts[1].toFloatOrNull() ?: 0f
                piesList.add(Pie(percent, label))
            }
        }

        val display = PieDisplay(
            useDark = useDark,
            baseColor = config.getOrDefault("baseColor", "#3ABEF9"),
            outlineColor = config.getOrDefault("outlineColor", "#050C9C"),
            theme = config.getOrDefault("theme", "classic"),
            scale = config.getOrDefault("scale", "1.0").toFloatOrNull() ?: 1.0f,
            visualVersion = config.getOrDefault("visualVersion", "1").toIntOrNull() ?: 1
        )

        return Pies(pies = piesList, pieDisplay = display)
    }
}