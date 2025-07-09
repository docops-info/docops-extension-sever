package io.docops.docopsextensionssupport.roadmap

import kotlinx.serialization.Serializable

@Serializable
data class RoadmapConfig(
    val title: String = "Product Roadmap",
    val subtitle: String = "",
    val width: Int = 1200,
    val height: Int = 800,
    val theme: String = "light",
    val quarters: List<String> = listOf("Q1", "Q2", "Q3", "Q4"),
    val displayConfig: RoadmapDisplayConfig = RoadmapDisplayConfig(),
    val categories: Map<String, CategoryConfig> = defaultCategories(),
    val showLegend: Boolean = true,
    val animationEnabled: Boolean = true
)

@Serializable
data class RoadmapDisplayConfig(
    val fontColor: String = "#1f2937",
    val backgroundColor: String = "#f8fafc",
    val scale: Float = 1.0f,
    val cardSpacing: Int = 20,
    val quarterSpacing: Int = 300,
    val cardWidth: Int = 160,
    val cardHeight: Int = 120,
    val animationEnabled: Boolean = true,
    val showLegend: Boolean = true
)

@Serializable
data class CategoryConfig(
    val name: String,
    val color: String,
    val visible: Boolean = true
)

@Serializable
data class RoadmapFeature(
    val id: String = "",
    val title: String,
    val category: String,
    val quarter: String,
    val status: FeatureStatus,
    val releaseDate: String,
    val description: List<String>,
    val priority: Priority = Priority.MEDIUM,
    val effort: Effort = Effort.MEDIUM,
    val assignee: String = "",
    val dependencies: List<String> = emptyList()
)

@Serializable
enum class FeatureStatus {
    COMPLETED,
    CURRENT,
    PLANNED,
    FUTURE,
    CANCELLED,
    ON_HOLD
}

@Serializable
enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

@Serializable
enum class Effort {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

// Default categories
fun defaultCategories(): Map<String, CategoryConfig> = mapOf(
    "core" to CategoryConfig("Core API", "#3b82f6"),
    "security" to CategoryConfig("Security", "#ef4444"),
    "performance" to CategoryConfig("Performance", "#10b981"),
    "integration" to CategoryConfig("Integration", "#f59e0b"),
    "ai" to CategoryConfig("AI/ML", "#8b5cf6"),
    "ui" to CategoryConfig("UI/UX", "#06b6d4"),
    "infrastructure" to CategoryConfig("Infrastructure", "#84cc16"),
    "analytics" to CategoryConfig("Analytics", "#a855f7")
)
