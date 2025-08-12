package io.docops.docopsextensionssupport.gherkin

import kotlinx.serialization.Serializable

@Serializable
data class GherkinSpec(
    val feature: String,
    val scenarios: List<GherkinScenario>,
    val theme: GherkinTheme = GherkinTheme()
)

@Serializable
data class GherkinScenario(
    val title: String,
    val steps: List<GherkinStep>,
    val status: GherkinScenarioStatus = GherkinScenarioStatus.PASSING
)

@Serializable
data class GherkinStep(
    val type: GherkinStepType,
    val text: String,
    val status: GherkinStepStatus = GherkinStepStatus.PASSING
)

@Serializable
data class GherkinTheme(
    val colors: GherkinColors = GherkinColors(),
    val layout: GherkinLayout = GherkinLayout(),
    val typography: GherkinTypography = GherkinTypography()
)

@Serializable
data class GherkinColors(
    val feature: String = "#4361ee",
    val scenario: String = "#f8f9fa",
    val given: String = "#4361ee",
    val `when`: String = "#ff6b35",
    val then: String = "#06d6a0",
    val and: String = "#6c757d",
    val but: String = "#6c757d",
    val passing: String = "#28a745",
    val failing: String = "#dc3545",
    val pending: String = "#ffc107",
    val skipped: String = "#6c757d"
)

@Serializable
data class GherkinLayout(
    val width: Int = 600,
    val height: Int = 400,
    val padding: Int = 20,
    val scenarioSpacing: Int = 30,
    val stepSpacing: Int = 25
)

@Serializable
data class GherkinTypography(
    val featureSize: Int = 18,
    val scenarioSize: Int = 16,
    val stepSize: Int = 12,
    val descriptionSize: Int = 10,
    val fontFamily: String = "Arial, sans-serif"
)

@Serializable
enum class GherkinStepType {
    GIVEN, WHEN, THEN, AND, BUT
}

@Serializable
enum class GherkinStepStatus {
    PASSING, FAILING, PENDING, SKIPPED
}

@Serializable
enum class GherkinScenarioStatus {
    PASSING, FAILING, PENDING, SKIPPED
}
