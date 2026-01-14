package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.quadrant.MagicQuadrantConfig
import io.docops.docopsextensionssupport.chart.quadrant.MagicQuadrantSvgGenerator
import io.docops.docopsextensionssupport.chart.quadrant.QuadrantCompany
import io.docops.docopsextensionssupport.chart.quadrant.toCsvResponse
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class MagicQuadrantHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    fun parseTabularData(input: String, useDark: Boolean): MagicQuadrantConfig {
        var leadersLabel = "Leaders"
        var challengersLabel = "Challengers"
        var visionariesLabel = "Visionaries"
        var nichePlayersLabel = "Niche Players"

        val lines = input.trim().split('\n').filter { it.isNotBlank() }
        
        if (lines.isEmpty()) {
            throw IllegalArgumentException("Input cannot be empty")
        }

        var title = "Magic Quadrant"
        var xAxisLabel = "Ability to Execute"
        var xAxisLabelEnd = ""
        var yAxisLabel = "Completeness of Vision"
        var visualVersion = 1
        var yAxisLabelEnd = ""
        val companies = mutableListOf<QuadrantCompany>()

        var headerProcessed = false
        var currentLineIndex = 0

        // Process configuration lines (optional)
        while (currentLineIndex < lines.size && lines[currentLineIndex].startsWith("#")) {
            val configLine = lines[currentLineIndex].substring(1).trim()
            when {
                configLine.startsWith("title:") -> {
                    title = configLine.substring("title:".length).trim()
                }
                configLine.startsWith("xAxis:") -> {
                    val valStr = configLine.substring("xAxis:".length).trim()
                    if (valStr.contains("|")) {
                        val parts = valStr.split("|")
                        xAxisLabel = parts[0].trim()
                        xAxisLabelEnd = parts[1].trim()
                    } else {
                        xAxisLabel = valStr
                    }
                }
                configLine.startsWith("yAxis:") -> {
                    val valStr = configLine.substring("yAxis:".length).trim()
                    if (valStr.contains("|")) {
                        val parts = valStr.split("|")
                        yAxisLabel = parts[0].trim()
                        yAxisLabelEnd = parts[1].trim()
                    } else {
                        yAxisLabel = valStr
                    }
                }
                configLine.startsWith("visualVersion:") -> {
                    visualVersion = configLine.substring("visualVersion:".length).trim().toIntOrNull() ?: 1
                }
                configLine.startsWith("leaders:") -> leadersLabel = configLine.substring("leaders:".length).trim()
                configLine.startsWith("challengers:") -> challengersLabel = configLine.substring("challengers:".length).trim()
                configLine.startsWith("visionaries:") -> visionariesLabel = configLine.substring("visionaries:".length).trim()
                configLine.startsWith("niche:") -> nichePlayersLabel = configLine.substring("niche:".length).trim()

            }
            currentLineIndex++
        }

        // Find and process header
        if (currentLineIndex < lines.size) {
            val headerLine = lines[currentLineIndex]
            if (headerLine.contains("|")) {
                // Skip header line, we know the expected format
                currentLineIndex++
                
                // Skip separator line if present (e.g., |---|---|---|)
                if (currentLineIndex < lines.size && lines[currentLineIndex].contains("-")) {
                    currentLineIndex++
                }
                headerProcessed = true
            }
        }

        // Process data rows
        while (currentLineIndex < lines.size) {
            val line = lines[currentLineIndex].trim()
            
            if (line.contains("|")) {
                val parts = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                
                if (parts.size >= 3) {
                    try {
                        val name = parts[0]
                        val x = parts[1].toDouble().coerceIn(0.0, 100.0)
                        val y = parts[2].toDouble().coerceIn(0.0, 100.0)
                        
                        val description = if (parts.size > 3) parts[3] else ""
                        val size = if (parts.size > 4) {
                            parts[4].toIntOrNull()?.coerceIn(8, 25) ?: 12
                        } else 12
                        val url = if (parts.size > 5) parts[5] else ""

                        companies.add(QuadrantCompany(name, x, y, description, size, url))
                    } catch (e: NumberFormatException) {
                        // Skip invalid rows
                        println("Warning: Skipping invalid row: $line")
                    }
                }
            } else {
                // Try simple space/tab separated format
                val parts = line.split(Regex("\\s+")).filter { it.isNotEmpty() }
                if (parts.size >= 3) {
                    try {
                        val name = parts[0]
                        val x = parts[1].toDouble().coerceIn(0.0, 100.0)
                        val y = parts[2].toDouble().coerceIn(0.0, 100.0)
                        
                        companies.add(QuadrantCompany(name, x, y))
                    } catch (e: NumberFormatException) {
                        // Skip invalid rows
                        println("Warning: Skipping invalid row: $line")
                    }
                }
            }
            currentLineIndex++
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
