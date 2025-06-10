package io.docops.docopsextensionssupport.scorecard

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class MigrationScoreCard(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val headerTitle: String,
    val beforeSection: BeforeSection,
    val afterSection: AfterSection,
    val performanceMetrics: List<MetricCategory>,
    val keyOptimizations: List<Optimization>,
    val migrationSummary: MigrationSummary,
    val footerText: String,
    val scale: Float = 1.0f,
    val theme: MigrationScoreCardTheme = MigrationScoreCardTheme()
)

@Serializable
class BeforeSection(
    val title: String,
    val items: List<InfrastructureItem>,
    val performanceBaseline: PerformanceMetric
)

@Serializable
class AfterSection(
    val title: String,
    val items: List<InfrastructureItem>,
    val performanceImprovement: PerformanceMetric
)

@Serializable
class InfrastructureItem(
    val title: String,
    val description: String,
    val status: String, // "critical", "warning", "good"
    val statusIcon: String // "!", "$", "✓", etc.
)

@Serializable
class PerformanceMetric(
    val label: String,
    val percentage: Int,
    val color: String
)

@Serializable
class MetricCategory(
    val title: String,
    val borderColor: String,
    val headerColor: String,
    val metrics: List<Metric>
)

@Serializable
class Metric(
    val label: String,
    val value: String
)

@Serializable
class Optimization(
    val number: Int,
    val title: String,
    val description: String
)

@Serializable
class MigrationSummary(
    val overallImprovement: Int,
    val status: String,
    val highlights: List<String>
)

@Serializable
class MigrationScoreCardTheme(
    val backgroundColor: String = "#f8f9fa",
    val titleColor: String = "#2c3e50",
    val subtitleColor: String = "#7f8c8d",
    val headerColor: String = "#8e44ad",
    val beforeSectionColor: String = "#e74c3c",
    val afterSectionColor: String = "#27ae60",
    val metricColors: Map<String, String> = mapOf(
        "performance" to "#e74c3c",
        "network" to "#3498db",
        "sql" to "#27ae60",
        "cost" to "#f39c12"
    )
)

// Extension functions for MigrationScoreCard
fun MigrationScoreCard.calcWidth(): Int {
    return (1400 * scale).toInt()
}

fun MigrationScoreCard.calcHeight(): Int {
    // Calculate the number of rows needed for key optimizations in two columns
    val itemsPerColumn = (keyOptimizations.size + 1) / 2
    val totalRows = Math.max(1, itemsPerColumn)

    // Calculate the footer position based on the content
    val footerY = 750 + (performanceMetrics.size + 1) / 2 * 180 + totalRows * 60

    // Add the footer height (40) and some buffer (50) to ensure it's fully visible
    val dynamicHeight = footerY + 90

    // Use the maximum of the dynamic height and the original fixed height to ensure backward compatibility
    return (Math.max(dynamicHeight, 1100) * scale).toInt()
}
