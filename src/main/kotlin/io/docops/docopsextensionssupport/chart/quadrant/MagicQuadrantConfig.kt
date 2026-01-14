package io.docops.docopsextensionssupport.chart.quadrant

import io.docops.docopsextensionssupport.support.VisualDisplay
import io.docops.docopsextensionssupport.web.CsvResponse

data class MagicQuadrantConfig(
    val title: String = "Magic Quadrant",
    val xAxisLabel: String = "Ability to Execute",
    val xAxisLabelEnd: String = "", // Added for split axis
    val yAxisLabel: String = "Completeness of Vision",
    val yAxisLabelEnd: String = "", // Added for split axis
    val companies: List<QuadrantCompany>,

    // Add custom quadrant labels
    val leadersLabel: String = "Leaders",
    val challengersLabel: String = "Challengers",
    val visionariesLabel: String = "Visionaries",
    val nichePlayersLabel: String = "Niche Players",
    override var useDark: Boolean = false,
    override val visualVersion: Int = 1
): VisualDisplay

data class QuadrantCompany(
    val name: String,
    val x: Double, // 0-100 scale
    val y: Double, // 0-100 scale
    val description: String = "",
    val size: Int = 12, // Bubble size for market share/importance
    val url: String = "" // Optional link
)

enum class QuadrantType(val displayName: String) {
    LEADERS("Leaders"),
    CHALLENGERS("Challengers"),
    VISIONARIES("Visionaries"),
    NICHE_PLAYERS("Niche Players")
}

fun MagicQuadrantConfig.toCsvResponse(): CsvResponse {
    val headers = listOf("Company", "X", "Y", "Description", "Size", "URL")

    val csvData = companies.map { company ->
        listOf(
            company.name,
            company.x.toString(),
            company.y.toString(),
            company.description,
            company.size.toString(),
            company.url
        )
    }

    return CsvResponse(headers, csvData)
}