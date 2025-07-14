package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable

@Serializable
data class FeatureCard(
    val title: String,
    val description: String,
    val emoji: String,
    val details: List<String> = emptyList(),
    val colorScheme: ColorScheme = ColorScheme.BLUE
)



@Serializable
data class FeatureCardRequest(
    val cards: List<FeatureCard>,
    val theme: CardTheme = CardTheme.LIGHT,
    val layout: CardLayout = CardLayout.GRID
)

@Serializable
enum class CardTheme {
    LIGHT, DARK, AUTO
}

@Serializable
enum class CardLayout {
    GRID, COLUMN, ROW
}

@Serializable
enum class ColorScheme {
    BLUE, GREEN, ORANGE, PURPLE, RED, TEAL, GRAY
}

@Serializable
data class ParsedFeatureCards(
    val cards: List<FeatureCard>,
    val theme: CardTheme = CardTheme.LIGHT,
    val layout: CardLayout = CardLayout.GRID
)

/**
 * Converts a ParsedFeatureCards to CSV format
 * @return CsvResponse with headers and rows representing the feature cards data
 */
fun ParsedFeatureCards.toCsv(): CsvResponse {
    val headers = listOf("Card Number", "Title", "Description", "Emoji", "Details", "Color Scheme", "Theme", "Layout")
    val csvRows = mutableListOf<List<String>>()

    if (cards.isNotEmpty()) {
        cards.forEachIndexed { index, card ->
            // Handle cards with multiple details by creating separate rows for each detail
            if (card.details.isNotEmpty()) {
                card.details.forEachIndexed { detailIndex, detail ->
                    csvRows.add(listOf(
                        (index + 1).toString(),
                        if (detailIndex == 0) card.title else "", // Only show card info in first detail row
                        if (detailIndex == 0) card.description else "",
                        if (detailIndex == 0) card.emoji else "",
                        detail,
                        if (detailIndex == 0) card.colorScheme.name else "",
                        if (index == 0 && detailIndex == 0) theme.name else "", // Only show theme/layout in first row
                        if (index == 0 && detailIndex == 0) layout.name else ""
                    ))
                }
            } else {
                // Card with no details
                csvRows.add(listOf(
                    (index + 1).toString(),
                    card.title,
                    card.description,
                    card.emoji,
                    "",
                    card.colorScheme.name,
                    if (index == 0) theme.name else "",
                    if (index == 0) layout.name else ""
                ))
            }
        }
    } else {
        // If no cards, just add configuration row
        csvRows.add(listOf(
            "0",
            "",
            "",
            "",
            "",
            "",
            theme.name,
            layout.name
        ))
    }

    return CsvResponse(headers, csvRows)
}