package io.docops.docopsextensionssupport.scorecard

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
