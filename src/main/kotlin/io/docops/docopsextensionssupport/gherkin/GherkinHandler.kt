
package io.docops.docopsextensionssupport.gherkin

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.json.Json


class GherkinHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {


    private fun parseGherkinContent(payload: String): GherkinSpec {
        return when {
            payload.trim().startsWith("{") -> {
                // JSON format
                Json.decodeFromString<GherkinSpec>(payload)
            }
            else -> {
                // Plain text Gherkin format
                parseGherkinText(payload)
            }
        }
    }

    private fun parseGherkinText(content: String): GherkinSpec {
        val lines = content.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        var featureTitle = ""
        val scenarios = mutableListOf<GherkinScenario>()
        var currentScenario: GherkinScenario? = null
        var currentSteps = mutableListOf<GherkinStep>()

        for (line in lines) {
            when {
                line.startsWith("Feature:") -> {
                    featureTitle = line.substringAfter("Feature:").trim()
                }
                line.startsWith("Scenario:") -> {
                    // Save previous scenario if exists
                    currentScenario?.let { 
                        scenarios.add(it.copy(steps = currentSteps.toList()))
                    }
                    // Start new scenario
                    currentScenario = GherkinScenario(
                        title = line.substringAfter("Scenario:").trim(),
                        steps = emptyList()
                    )
                    currentSteps.clear()
                }
                line.matches(Regex("^(Given|When|Then|And|But)\\s+.*")) -> {
                    val keyword = line.split(" ")[0]
                    val text = line.substringAfter("$keyword ").trim()
                    val stepType = when (keyword) {
                        "Given" -> GherkinStepType.GIVEN
                        "When" -> GherkinStepType.WHEN
                        "Then" -> GherkinStepType.THEN
                        "And" -> GherkinStepType.AND
                        "But" -> GherkinStepType.BUT
                        else -> GherkinStepType.GIVEN
                    }
                    currentSteps.add(GherkinStep(stepType, text, GherkinStepStatus.PASSING))
                }
            }
        }

        // Don't forget the last scenario
        currentScenario?.let {
            scenarios.add(it.copy(steps = currentSteps.toList()))
        }

        return GherkinSpec(
            feature = featureTitle.ifEmpty { "Feature" },
            scenarios = scenarios
        )
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return try {
            val gherkinMaker = GherkinMaker()
            val gherkinSpec = parseGherkinContent(payload)
            // Update CSV response similar to other handlers
            csvResponse.update(gherkinSpec.toCsv())
            val svg = gherkinMaker.makeGherkin(gherkinSpec, context.useDark)
            svg
        } catch (e: Exception) {
            """
            <svg width="400" height="200" xmlns="http://www.w3.org/2000/svg">
                <rect width="400" height="200" fill="#ffebee" stroke="#f44336" stroke-width="2" rx="8"/>
                <text x="200" y="100" text-anchor="middle" font-family="Arial" font-size="14" fill="#d32f2f">
                    Error generating Gherkin visualization: ${e.message}
                </text>
            </svg>
            """.trimIndent()
        }
    }
}
