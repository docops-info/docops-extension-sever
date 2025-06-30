package io.docops.docopsextensionssupport.callout

import kotlinx.serialization.Serializable

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
data class CalloutData(
    val title: String = "Callout",
    val steps: List<CalloutStep> = emptyList(),
    val metrics: Map<String, String> = emptyMap(),
    val useGlass: Boolean = true
)

// For backward compatibility
typealias MigrationStep = CalloutStep
typealias MigrationData = CalloutData
