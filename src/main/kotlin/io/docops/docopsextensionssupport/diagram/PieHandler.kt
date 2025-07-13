package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.CsvRequest
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class PieHandler : DocOpsHandler {

    private val json = Json { ignoreUnknownKeys = true }

    fun handleSVG(payload: String): String {
        val pies = parseInput(payload)
        val pieMaker = PieMaker()
        return pieMaker.makePies(pies)
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }

    override fun toCsv(request: CsvRequest): CsvResponse {
        val pies = parseInput(request.content)
        return pies.piesToCsv()
    }


    /**
     * Parse input data supporting both JSON and tabular formats
     */
    private fun parseInput(payload: String): Pies {
        val trimmedPayload = payload.trim()

        return if (trimmedPayload.startsWith("{") && trimmedPayload.endsWith("}")) {
            // JSON format
            parseJsonInput(trimmedPayload)
        } else {
            // Tabular format
            parseTabularInput(trimmedPayload)
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
    private fun parseTabularInput(payload: String): Pies {
        val lines = payload.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var baseColor = "#3ABEF9"
        var outlineColor = "#050C9C"
        var scale = 1.0f
        var useDark = false

        val pies = mutableListOf<Pie>()
        var inDataSection = false
        var headerProcessed = false

        for (line in lines) {
            when {
                line == "---" -> {
                    inDataSection = true
                    continue
                }

                !inDataSection && line.contains("=") -> {
                    // Parse configuration parameters
                    val (key, value) = line.split("=", limit = 2).map { it.trim() }
                    when (key.lowercase()) {
                        "basecolor" -> baseColor = value
                        "outlinecolor" -> outlineColor = value
                        "scale" -> scale = value.toFloatOrNull() ?: 1.0f
                        "usedark" -> useDark = value.toBoolean()
                    }
                }

                inDataSection && !headerProcessed && line.contains("|") -> {
                    // Skip header row
                    headerProcessed = true
                    continue
                }

                inDataSection && line.contains("|") -> {
                    // Parse data rows
                    val parts = line.split("|").map { it.trim() }
                    if (parts.size >= 2) {
                        val label = parts[0]
                        val percent = parts[1].toFloatOrNull() ?: 0f
                        pies.add(Pie(percent, label))
                    }
                }
            }
        }

        val pieDisplay = PieDisplay(
            baseColor = baseColor,
            outlineColor = outlineColor,
            scale = scale,
            useDark = useDark
        )

        return Pies(pies, pieDisplay)
    }
}