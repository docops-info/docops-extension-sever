package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.pie.*
import io.docops.docopsextensionssupport.util.ParsingUtils
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class PieHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {


    fun handleSVGInternal(payload: String, context: DocOpsContext): String {
        val pies = parseTabularInput(payload.trim(), context.useDark)

        val svg =  if (pies.pieDisplay.visualVersion >= 3) {
            PieMakerImproved().makePies(pies.copy(pieDisplay = pies.pieDisplay))
        } else {
            PieMaker().makePies(pies.copy(pieDisplay = pies.pieDisplay))
        }

        csvResponse.update(pies.piesToCsv())
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVGInternal(payload, context)
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