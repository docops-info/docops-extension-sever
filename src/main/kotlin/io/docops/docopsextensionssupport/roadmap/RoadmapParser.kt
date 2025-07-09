
package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.util.ParsingUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import java.util.*

/**
 * RoadmapParser handles parsing of roadmap data from various input formats
 */
class RoadmapParser {
    private val logger = KotlinLogging.logger {}
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    fun parseRoadmapData(content: String): Pair<RoadmapConfig, List<RoadmapFeature>> {
        return when {
            content.trim().startsWith("{") -> parseJsonFormat(content)
            else -> parseTableFormat(content)
        }
    }
    
    private fun parseJsonFormat(content: String): Pair<RoadmapConfig, List<RoadmapFeature>> {
        val roadmapData = json.decodeFromString<RoadmapData>(content)
        val config = RoadmapConfig(
            title = roadmapData.title,
            subtitle = roadmapData.subtitle,
            width = roadmapData.width,
            height = roadmapData.height,
            theme = roadmapData.theme,
            quarters = roadmapData.quarters,
            displayConfig = roadmapData.displayConfig,
            categories = roadmapData.categories,
            showLegend = roadmapData.displayConfig.showLegend,
            animationEnabled = roadmapData.displayConfig.animationEnabled
        )
        return config to roadmapData.features
    }

    private fun parseTableFormat(content: String): Pair<RoadmapConfig, List<RoadmapFeature>> {
        val (configSection, dataSection) = ParsingUtils.parseConfigAndData(content)

        val config = parseConfig(configSection)
        val features = parseFeatures(dataSection, config)

        return config to features
    }
    
    private fun parseConfig(configSection: Map<String, String>): RoadmapConfig {
        val displayConfig = RoadmapDisplayConfig(
            fontColor = configSection["fontColor"] ?: "#1f2937",
            backgroundColor = configSection["backgroundColor"] ?: "#f8fafc",
            scale = configSection["scale"]?.toFloatOrNull() ?: 1.0f,
            showLegend = configSection["showLegend"]?.toBoolean() ?: true,
            animationEnabled = configSection["animationEnabled"]?.toBoolean() ?: true
        )
        
        return RoadmapConfig(
            title = configSection["title"] ?: "Product Roadmap",
            subtitle = configSection["subtitle"] ?: "",
            width = configSection["width"]?.toIntOrNull() ?: 1200,
            height = configSection["height"]?.toIntOrNull() ?: 800,
            theme = configSection["theme"] ?: "light",
            quarters = configSection["quarters"]?.split(",")?.map { it.trim() } 
                ?: listOf("Q1 2024", "Q2 2024", "Q3 2024", "Q4 2024"),
            displayConfig = displayConfig,
            categories = parseCategories(configSection),
            showLegend = displayConfig.showLegend,
            animationEnabled = displayConfig.animationEnabled
        )
    }
    
        private fun parseCategories(configSection: Map<String, String>): Map<String, CategoryConfig> {
            val categories = defaultCategories().toMutableMap()

            // Allow overriding default categories
            configSection.forEach { (key, value) ->
                if (key.startsWith("category.")) {
                    val categoryKey = key.removePrefix("category.")

                    // Support both comma and pipe separators
                    val parts = if (value.contains("|")) {
                        value.split("|")
                    } else {
                        value.split(",")
                    }

                    if (parts.size >= 2) {
                        categories[categoryKey] = CategoryConfig(
                            name = parts[0].trim(),
                            color = parts[1].trim(),
                            visible = parts.getOrNull(2)?.toBoolean() ?: true
                        )
                    }
                }
            }

            return categories
        }


        private fun parseFeatures(dataSection: String, config: RoadmapConfig): List<RoadmapFeature> {
        val tableData = ParsingUtils.parseTableData(dataSection)
        val features = mutableListOf<RoadmapFeature>()

        if (tableData.isEmpty()) {
            return features
        }


        // Skip header row and process feature rows
        tableData.drop(1).forEachIndexed { index, row ->

            // Skip empty rows and table end markers
            if (row.isNotEmpty() && row.size > 1) {
                // Clean the first cell by removing any leading/trailing pipes and whitespace
                val firstCell = row[0].trim().removePrefix("|").removeSuffix("|").trim()

                // Skip if it's a table delimiter, empty, or header row
                if (firstCell.isNotBlank() &&
                    !firstCell.startsWith("===") &&
                    !firstCell.startsWith("---") &&
                    !firstCell.equals("Title", ignoreCase = true)) {

                    try {
                        // Clean all cells by removing pipes and trimming
                        val cleanedRow = row.map { it.trim().removePrefix("|").removeSuffix("|").trim() }

                        val feature = RoadmapFeature(
                            id = generateId(firstCell),
                            title = firstCell,
                            category = cleanedRow.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "core",
                            quarter = cleanedRow.getOrNull(2)?.takeIf { it.isNotBlank() } ?: config.quarters.first(),
                            status = parseStatus(cleanedRow.getOrNull(3) ?: "planned"),
                            releaseDate = cleanedRow.getOrNull(4) ?: "",
                            description = parseDescription(cleanedRow.getOrNull(5) ?: ""),
                            priority = parsePriority(cleanedRow.getOrNull(6) ?: "medium"),
                            effort = parseEffort(cleanedRow.getOrNull(7) ?: "medium"),
                            assignee = cleanedRow.getOrNull(8) ?: "",
                            dependencies = parseDependencies(cleanedRow.getOrNull(9) ?: "")
                        )

                        features.add(feature)
                    } catch (e: Exception) {
                        logger.error(e) {"Error creating feature from row $index: ${e.message}"}
                    }
                }
            }
        }

        return features
    }


    private fun parseDescription(description: String): List<String> {
        return if (description.contains(";")) {
            description.split(";").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            listOf(description).filter { it.isNotEmpty() }
        }
    }
    
    private fun parseDependencies(dependencies: String): List<String> {
        return if (dependencies.contains(",")) {
            dependencies.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            listOf(dependencies).filter { it.isNotEmpty() }
        }
    }
    
    private fun parseStatus(status: String): FeatureStatus {
        return when (status.lowercase()) {
            "completed", "done", "released" -> FeatureStatus.COMPLETED
            "current", "in-progress", "active", "in progress" -> FeatureStatus.CURRENT
            "planned", "scheduled" -> FeatureStatus.PLANNED
            "future", "backlog" -> FeatureStatus.FUTURE
            "cancelled", "canceled" -> FeatureStatus.CANCELLED
            "on-hold", "paused", "on hold" -> FeatureStatus.ON_HOLD
            else -> FeatureStatus.PLANNED
        }
    }
    
    private fun parsePriority(priority: String): Priority {
        return when (priority.lowercase()) {
            "low" -> Priority.LOW
            "medium", "med" -> Priority.MEDIUM
            "high" -> Priority.HIGH
            "critical", "urgent" -> Priority.CRITICAL
            else -> Priority.MEDIUM
        }
    }
    
    private fun parseEffort(effort: String): Effort {
        return when (effort.lowercase()) {
            "small", "s", "xs" -> Effort.SMALL
            "medium", "med", "m" -> Effort.MEDIUM
            "large", "l" -> Effort.LARGE
            "extra-large", "xl", "xxl" -> Effort.EXTRA_LARGE
            else -> Effort.MEDIUM
        }
    }
    
    private fun generateId(title: String): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), "-")
            .take(50)
    }
    
    private fun defaultCategories(): Map<String, CategoryConfig> = mapOf(
        "core" to CategoryConfig("Core API", "#3b82f6"),
        "security" to CategoryConfig("Security", "#ef4444"),
        "performance" to CategoryConfig("Performance", "#10b981"),
        "integration" to CategoryConfig("Integration", "#f59e0b"),
        "ai" to CategoryConfig("AI/ML", "#8b5cf6"),
        "ui" to CategoryConfig("UI/UX", "#06b6d4"),
        "infrastructure" to CategoryConfig("Infrastructure", "#84cc16"),
        "analytics" to CategoryConfig("Analytics", "#a855f7")
    )
}

@kotlinx.serialization.Serializable
data class RoadmapData(
    val title: String = "Product Roadmap",
    val subtitle: String = "",
    val width: Int = 1200,
    val height: Int = 800,
    val theme: String = "light",
    val quarters: List<String> = listOf("Q1", "Q2", "Q3", "Q4"),
    val displayConfig: RoadmapDisplayConfig = RoadmapDisplayConfig(),
    val categories: Map<String, CategoryConfig> = emptyMap(),
    val features: List<RoadmapFeature> = emptyList()
)
