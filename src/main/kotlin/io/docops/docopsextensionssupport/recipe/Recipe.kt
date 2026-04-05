package io.docops.docopsextensionssupport.recipe

import io.docops.docopsextensionssupport.web.CsvResponse

data class Recipe(
    val title: String? = null,
    val yield: String? = null,
    val prep: String? = null,
    val cook: String? = null,
    val tags: List<String> = emptyList(),
    val summary: String? = null,
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    val theme: String = "classic"
) {
    fun toCSV(): CsvResponse {
        val headers = listOf(
            "Title",
            "Yield",
            "Prep",
            "Cook",
            "Theme",
            "Tags",
            "Summary",
            "Ingredients",
            "Steps",
            "Notes"
        )

        val row = listOf(
            title.orEmpty(),
            yield.orEmpty(),
            prep.orEmpty(),
            cook.orEmpty(),
            theme,
            tags.joinToString("; "),
            summary.orEmpty(),
            ingredients.joinToString(" | "),
            steps.joinToString(" | "),
            notes.joinToString(" | ")
        )

        return CsvResponse(
            headers = headers,
            rows = listOf(row)
        )
    }
}
