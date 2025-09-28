package io.docops.docopsextensionssupport.callout

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Generic data class for callout steps
 */
@Serializable
data class CalloutStep(
    val phase: String,
    val action: String,
    val result: String,
    val improvement: String? = null
)

/**
 * Generic data class for callout data
 */
@Serializable
data class CalloutData @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toHexString(),
    val title: String = "Callout",
    val steps: List<CalloutStep> = emptyList(),
    val metrics: Map<String, String> = emptyMap(),
    val useDark: Boolean = true
)


/**
 * Converts a CalloutData to CSV format
 * @return CsvResponse with headers and rows representing the callout data
 */
fun CalloutData.toCsv(): CsvResponse {
    val headers = mutableListOf("Title", "Step Number", "Phase", "Action", "Result", "Improvement")
    val csvRows = mutableListOf<List<String>>()

    // Add title row if there are steps
    if (steps.isNotEmpty()) {
        steps.forEachIndexed { index, step ->
            csvRows.add(listOf(
                if (index == 0) title else "", // Only show title in first row
                (index + 1).toString(),
                step.phase,
                step.action,
                step.result,
                step.improvement ?: ""
            ))
        }
    } else {
        // If no steps, just add title row
        csvRows.add(listOf(title, "0", "", "", "", ""))
    }

    // Add metrics as additional rows if present
    metrics.forEach { (key, value) ->
        csvRows.add(listOf("", "Metric", key, value, "", ""))
    }

    return CsvResponse(headers, csvRows)
}

// For backward compatibility
typealias MigrationStep = CalloutStep
typealias MigrationData = CalloutData
