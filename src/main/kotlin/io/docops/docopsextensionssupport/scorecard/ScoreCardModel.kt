package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Simplified MigrationScoreCard model to support the iOS-style design.
 * This model focuses on the core elements needed for the design while maintaining
 * backward compatibility with existing input formats.
 */
@Serializable
class MigrationScoreCard(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val headerTitle: String,
    val beforeSection: BeforeSection,
    val afterSection: AfterSection,
    val scale: Float = 1.0f,
    val theme: MigrationScoreCardTheme = MigrationScoreCardTheme(),
    val teamMembers: List<TeamMember> = emptyList()
)

/**
 * Represents the "before" section of the migration scorecard.
 */
@Serializable
class BeforeSection(
    val title: String,
    val items: List<InfrastructureItem>,
    val performanceBaseline: PerformanceMetric
)

/**
 * Represents the "after" section of the migration scorecard.
 */
@Serializable
class AfterSection(
    val title: String,
    val items: List<InfrastructureItem>,
    val performanceImprovement: PerformanceMetric
)

/**
 * Represents an item in the before or after section.
 */
@Serializable
class InfrastructureItem(
    val title: String,
    val description: String,
    val status: String, // "critical", "warning", "good"
    val statusIcon: String // "!", "$", "✓", etc.
)

/**
 * Represents a performance metric with a label, percentage, and color.
 */
@Serializable
class PerformanceMetric(
    val label: String,
    val percentage: Int,
    val color: String
)

/**
 * Represents a category of metrics.
 * This class is kept for backward compatibility but not used in the iOS-style design.
 */
@Serializable
class MetricCategory(
    val title: String,
    val headerColor: String,
    val metrics: List<Metric>
)

/**
 * Represents a single metric with a label and value.
 * This class is kept for backward compatibility but not used in the iOS-style design.
 */
@Serializable
class Metric(
    val label: String,
    val value: String
)

/**
 * Represents an optimization with a number, title, and description.
 * This class is kept for backward compatibility but not used in the iOS-style design.
 */
@Serializable
class Optimization(
    val number: Int,
    val title: String,
    val description: String
)

/**
 * Represents a summary of the migration with an overall improvement percentage and status.
 * The highlights property is kept for backward compatibility but not used in the iOS-style design.
 */
@Serializable
class MigrationSummary(
    val status: String
)

/**
 * Represents the theme settings for the scorecard.
 * Some properties are kept for backward compatibility but not used in the iOS-style design.
 */
@Serializable
class MigrationScoreCardTheme(
    val backgroundColor: String = "#f8f9fa",
    val titleColor: String = "#2c3e50",
    val headerColor: String = "#8e44ad"
)

/**
 * Represents a team member with initials, emoji, and color.
 */
@Serializable
class TeamMember(
    val initials: String,
    val emoji: String,
    val color: String
)

// Extension functions for MigrationScoreCard
fun MigrationScoreCard.calcWidth(): Int {
    // For the iOS-style design, we use a fixed width that accommodates the design
    return (800 * scale).toInt()
}

fun MigrationScoreCard.calcHeight(): Int {
    // Calculate the base height (without items)
    val baseHeight = 620

    // Calculate the height needed for the before section items
    val beforeItemsHeight = calculateItemsHeight(beforeSection.items)

    // Calculate the height needed for the after section items
    val afterItemsHeight = calculateItemsHeight(afterSection.items)

    // Get the maximum height needed for either section
    val maxItemsHeight = maxOf(beforeItemsHeight, afterItemsHeight)

    // The default card height is 380, which can accommodate about 4-5 items
    // If we need more height, add the difference to the base height
    val defaultCardHeight = 380
    val extraHeight = maxOf(0, maxItemsHeight - defaultCardHeight)

    // Return the scaled height
    return ((baseHeight + extraHeight) * scale).toInt()
}

/**
 * Calculates the height needed for a list of items.
 * 
 * @param items The list of items
 * @return The height needed in pixels
 */
fun calculateItemsHeight(items: List<InfrastructureItem>): Int {
    // Start with the initial offset (190px from the top of the card)
    var height = 190

    // For each item, calculate its height based on title and description
    items.forEach { item ->
        // Estimate the number of lines in the title (assuming 20 chars per line)
        val titleLines = (item.title.length / 20) + 1

        // Estimate the number of lines in the description (assuming 25 chars per line)
        val descLines = if (item.description.isBlank()) 0 else (item.description.length / 25) + 1

        // Add the height for this item (12px for the circle, 18px per title line, 16px per desc line, 30px spacing)
        height += 12 + (titleLines * 18) + (descLines * 16) + 30
    }

    // Add space for the performance label at the bottom (30px margin)
    height += 30

    return height
}

/**
 * Convert MigrationScoreCard to basic overview CSV
 */
fun MigrationScoreCard.toCsv(): CsvResponse {
    val headers = listOf("Field", "Value")

    val rows = mutableListOf<List<String>>()

    rows.add(listOf("ID", this.id))
    rows.add(listOf("Title", this.title))
    rows.add(listOf("Subtitle", this.subtitle))
    rows.add(listOf("Header Title", this.headerTitle))
    rows.add(listOf("Scale", this.scale.toString()))
    rows.add(listOf("Before Section Title", this.beforeSection.title))
    rows.add(listOf("After Section Title", this.afterSection.title))
    rows.add(listOf("Team Members Count", this.teamMembers.size.toString()))

    return CsvResponse(headers, rows)
}
