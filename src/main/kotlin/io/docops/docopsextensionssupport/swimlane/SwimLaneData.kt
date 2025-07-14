package io.docops.docopsextensionssupport.swimlane

import io.docops.docopsextensionssupport.web.CsvResponse
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
/**
 * Converts a SwimLaneData to CSV format
 * @return CsvResponse with headers and rows representing the swimlane data
 */
fun SwimLaneData.toCsv(): CsvResponse {
    val headers = listOf("Title", "Lane Number", "Lane Title", "Item Number", "Item Title", "Content", "Use Dark Theme", "Width", "Height")
    val csvRows = mutableListOf<List<String>>()

    if (lanes.isNotEmpty()) {
        var itemCount = 0
        lanes.forEachIndexed { laneIndex, lane ->
            if (lane.items.isNotEmpty()) {
                lane.items.forEachIndexed { itemIndex, item ->
                    // For items with multiple content lines, create separate rows
                    if (item.content.isNotEmpty()) {
                        item.content.forEachIndexed { contentIndex, content ->
                            csvRows.add(listOf(
                                if (itemCount == 0) title else "", // Only show title in first row
                                (laneIndex + 1).toString(),
                                if (itemIndex == 0) lane.title else "", // Only show lane title in first item
                                (itemIndex + 1).toString(),
                                if (contentIndex == 0) item.title else "", // Only show item title in first content line
                                content,
                                if (itemCount == 0) useDarkTheme.toString() else "", // Only show settings in first row
                                if (itemCount == 0) width.toString() else "",
                                if (itemCount == 0) height.toString() else ""
                            ))
                            itemCount++
                        }
                    } else {
                        // Item with no content
                        csvRows.add(listOf(
                            if (itemCount == 0) title else "",
                            (laneIndex + 1).toString(),
                            if (itemIndex == 0) lane.title else "",
                            (itemIndex + 1).toString(),
                            item.title,
                            "",
                            if (itemCount == 0) useDarkTheme.toString() else "",
                            if (itemCount == 0) width.toString() else "",
                            if (itemCount == 0) height.toString() else ""
                        ))
                        itemCount++
                    }
                }
            } else {
                // Lane with no items
                csvRows.add(listOf(
                    if (itemCount == 0) title else "",
                    (laneIndex + 1).toString(),
                    lane.title,
                    "0",
                    "",
                    "",
                    if (itemCount == 0) useDarkTheme.toString() else "",
                    if (itemCount == 0) width.toString() else "",
                    if (itemCount == 0) height.toString() else ""
                ))
                itemCount++
            }
        }
    } else {
        // If no lanes, just add configuration row
        csvRows.add(listOf(
            title,
            "0",
            "",
            "0",
            "",
            "",
            useDarkTheme.toString(),
            width.toString(),
            height.toString()
        ))
    }

    return CsvResponse(headers, csvRows)
}