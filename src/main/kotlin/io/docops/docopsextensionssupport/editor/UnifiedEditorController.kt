package io.docops.docopsextensionssupport.editor

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/editor")
class UnifiedEditorController {

    private val objectMapper = ObjectMapper()

    @GetMapping("")
    fun editorHome(): String {
        return "forward:/unified-editor.html"
    }

    @GetMapping("/api/types")
    @ResponseBody
    fun getVisualizationTypes(): ResponseEntity<List<VisualizationType>> {
        val types = listOf(
            VisualizationType(
                id = "featurecard",
                name = "Feature Cards",
                description = "Interactive cards for showcasing features",
                icon = "🎯",
                endpoint = "/api/featurecard",
                color = "bg-blue-100 text-blue-800"
            ),
            VisualizationType(
                id = "roadmap",
                name = "Roadmaps",
                description = "Timeline-based project planning",
                icon = "🗺️",
                endpoint = "/api/roadmap",
                color = "bg-green-100 text-green-800"
            ),
            VisualizationType(
                id = "timeline",
                name = "Timelines",
                description = "Linear progression visualizations",
                icon = "📅",
                endpoint = "/api/timeline",
                color = "bg-purple-100 text-purple-800"
            ),
            VisualizationType(
                id = "scorecard",
                name = "Scorecards",
                description = "Metrics and KPI dashboards",
                icon = "📊",
                endpoint = "/api/scorecard",
                color = "bg-orange-100 text-orange-800"
            ),
            VisualizationType(
                id = "chart",
                name = "Charts",
                description = "Various chart types",
                icon = "📈",
                endpoint = "/api/chart",
                color = "bg-red-100 text-red-800"
            ),
            VisualizationType(
                id = "swimlane",
                name = "Swimlanes",
                description = "Process flow diagrams",
                icon = "🏊",
                endpoint = "/api/swimlane",
                color = "bg-teal-100 text-teal-800"
            ),
            VisualizationType(
                id = "wordcloud",
                name = "Word Clouds",
                description = "Text-based visualizations",
                icon = "☁️",
                endpoint = "/api/wordcloud",
                color = "bg-indigo-100 text-indigo-800"
            ),
            VisualizationType(
                id = "planner",
                name = "Planners",
                description = "Project planning layouts",
                icon = "📋",
                endpoint = "/api/planner",
                color = "bg-pink-100 text-pink-800"
            )
        )
        return ResponseEntity.ok(types)
    }

    @GetMapping("/api/templates/{type}")
    @ResponseBody
    fun getTemplatesForType(@PathVariable type: String): ResponseEntity<List<VisualizationTemplate>> {
        val templates = when (type) {
            "featurecard" -> getFeatureCardTemplates()
            "roadmap" -> getRoadmapTemplates()
            "timeline" -> getTimelineTemplates()
            "scorecard" -> getScorecardTemplates()
            "chart" -> getChartTemplates()
            else -> emptyList()
        }
        return ResponseEntity.ok(templates)
    }

    private fun getFeatureCardTemplates(): List<VisualizationTemplate> {
        return listOf(
            VisualizationTemplate(
                id = "featurecard-basic",
                name = "Basic Feature Cards",
                description = "Simple feature cards with grid layout",
                config = mapOf(
                    "theme" to "light",
                    "layout" to "grid",
                    "cardData" to "Title | Description | Emoji | ColorScheme\nCloud Storage | Secure, scalable storage | ☁️ | BLUE\nReal-time Collaboration | Work together seamlessly | 👥 | GREEN\nAdvanced Analytics | Powerful insights | 📊 | PURPLE"
                )
            ),
            VisualizationTemplate(
                id = "featurecard-dark",
                name = "Dark Theme Cards",
                description = "Feature cards with dark theme",
                config = mapOf(
                    "theme" to "dark",
                    "layout" to "grid",
                    "cardData" to "Title | Description | Emoji | ColorScheme\nAI Assistant | Intelligent automation | 🤖 | PURPLE\nCustom Integrations | Connect your tools | 🔌 | GREEN\nPremium Support | 24/7 assistance | 🎯 | TEAL"
                )
            )
        )
    }

    private fun getRoadmapTemplates(): List<VisualizationTemplate> {
        return listOf(
            VisualizationTemplate(
                id = "roadmap-product",
                name = "Product Roadmap",
                description = "Standard product development roadmap",
                config = mapOf(
                    "style" to "horizontal",
                    "theme" to "light",
                    "roadmapData" to "Quarter | Item | Status | Priority\nQ1 2024 | Feature A | In Progress | High\nQ2 2024 | Feature B | Planned | Medium\nQ3 2024 | Feature C | Planned | Low"
                )
            )
        )
    }

    private fun getTimelineTemplates(): List<VisualizationTemplate> {
        return listOf(
            VisualizationTemplate(
                id = "timeline-project",
                name = "Project Timeline",
                description = "Standard project timeline",
                config = mapOf(
                    "orientation" to "horizontal",
                    "style" to "modern",
                    "timelineData" to "Date | Event | Description\n2024-01-15 | Project Start | Project kickoff\n2024-02-01 | Milestone 1 | First milestone\n2024-03-15 | Milestone 2 | Second milestone"
                )
            )
        )
    }

    private fun getScorecardTemplates(): List<VisualizationTemplate> {
        return listOf(
            VisualizationTemplate(
                id = "scorecard-kpi",
                name = "KPI Scorecard",
                description = "Key performance indicators dashboard",
                config = mapOf(
                    "theme" to "light",
                    "layout" to "grid",
                    "scorecardData" to "Metric | Value | Target | Status\nSales | 85% | 90% | Warning\nQuality | 95% | 90% | Success\nPerformance | 78% | 80% | Danger"
                )
            )
        )
    }

    private fun getChartTemplates(): List<VisualizationTemplate> {
        return listOf(
            VisualizationTemplate(
                id = "chart-sales",
                name = "Sales Chart",
                description = "Monthly sales performance chart",
                config = mapOf(
                    "chartType" to "bar",
                    "theme" to "light",
                    "chartData" to "Category | Value | Color\nJan | 120 | #3B82F6\nFeb | 80 | #10B981\nMar | 95 | #F59E0B\nApr | 110 | #EF4444"
                )
            )
        )
    }
}

data class VisualizationType(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val endpoint: String,
    val color: String
)

data class VisualizationTemplate(
    val id: String,
    val name: String,
    val description: String,
    val config: Map<String, Any>
)
