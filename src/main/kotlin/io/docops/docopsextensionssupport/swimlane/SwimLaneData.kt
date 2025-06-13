package io.docops.docopsextensionssupport.swimlane

import kotlinx.serialization.Serializable

/**
 * Represents an item in a swimlane
 */
@Serializable
data class SwimLaneItem(
    val title: String = "",
    val content: List<String> = emptyList()
)

/**
 * Represents a swimlane with a title and items
 */
@Serializable
data class SwimLane(
    val title: String = "",
    val items: List<SwimLaneItem> = emptyList()
)

/**
 * Represents the complete swimlane diagram data
 */
@Serializable
data class SwimLaneData(
    val title: String = "Swimlanes",
    val lanes: List<SwimLane> = emptyList(),
    val useDarkTheme: Boolean = false,
    val width: Int = 900,
    val height: Int = 450
)