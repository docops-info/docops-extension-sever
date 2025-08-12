
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
        var currentScenarioTitle: String? = null
        var currentSteps = mutableListOf<GherkinStep>()
        var currentOutline = false
        var inExamples = false
        var exampleHeaders: List<String>? = null
        val exampleRows = mutableListOf<List<String>>()

        fun flushCurrentScenario() {
            val title = currentScenarioTitle ?: return
            val examples = if (currentOutline && exampleHeaders != null) {
                GherkinExamples(exampleHeaders!!, exampleRows.toList())
            } else null
            scenarios.add(
                GherkinScenario(
                    title = title,
                    steps = currentSteps.toList(),
                    outline = currentOutline,
                    examples = examples
                )
            )
            // reset
            currentScenarioTitle = null
            currentSteps = mutableListOf()
            currentOutline = false
            inExamples = false
            exampleHeaders = null
            exampleRows.clear()
        }

        for (line in lines) {
            when {
                // Feature line: allow optional spaces around colon
                line.matches(Regex("^Feature\\s*:\\s*(.*)$")) -> {
                    val match = Regex("^Feature\\s*:\\s*(.*)$").find(line)
                    featureTitle = match?.groupValues?.get(1)?.trim().orEmpty()
                }
                // Start of a new Scenario or Scenario Outline
                line.matches(Regex("^Scenario(?: Outline)?\\s*:\\s*(.*)$")) -> {
                    // Save previous scenario if exists
                    if (currentScenarioTitle != null) {
                        flushCurrentScenario()
                    }
                    val outline = line.startsWith("Scenario Outline")
                    val titleText = Regex("^Scenario(?: Outline)?\\s*:\\s*(.*)$").find(line)?.groupValues?.get(1)?.trim().orEmpty()
                    currentScenarioTitle = titleText
                    currentOutline = outline
                    currentSteps.clear()
                    inExamples = false
                    exampleHeaders = null
                    exampleRows.clear()
                }
                // Examples block start
                line.matches(Regex("^Examples\\s*:\\s*$")) -> {
                    inExamples = true
                    exampleHeaders = null
                    exampleRows.clear()
                }
                // Examples table rows (only when inExamples)
                inExamples && line.startsWith("|") -> {
                    val cells = line.trim().trim('|').split('|').map { it.trim() }
                    if (exampleHeaders == null) {
                        exampleHeaders = cells
                    } else {
                        exampleRows.add(cells)
                    }
                }
                // Step lines: support optional colon after keyword and optional spaces
                line.matches(Regex("^(Given|When|Then|And|But)\\s*:?\\s*(.*)$")) -> {
                    val m = Regex("^(Given|When|Then|And|But)\\s*:?\\s*(.*)$").find(line)
                    val keyword = m?.groupValues?.get(1) ?: "Given"
                    val text = m?.groupValues?.get(2)?.trim().orEmpty()
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
        if (currentScenarioTitle != null) {
            flushCurrentScenario()
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
