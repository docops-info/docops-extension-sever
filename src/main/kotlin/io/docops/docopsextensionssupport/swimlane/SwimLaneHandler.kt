package io.docops.docopsextensionssupport.swimlane

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import kotlinx.serialization.json.Json

/**
 * Handler for swimlane diagrams
 */
class SwimLaneHandler : DocOpsHandler {

    private val json = Json { ignoreUnknownKeys = true }
    private val maker = SwimLaneMaker()

    /**
     * Main handler method that processes the payload and generates SVG
     */
    fun handleSVG(payload: String): String {

        // Determine if the data is in JSON or table format
        val swimLaneData = if (payload.trim().startsWith("{") && payload.trim().endsWith("}")) {
            // JSON format
            try {
                json.decodeFromString<SwimLaneData>(payload)
            } catch (e: Exception) {
                maker.createDefaultSwimLaneData()
            }
        } else {
            // Table format
            parseTableData(payload)
        }

        return maker.generateSvg(swimLaneData)
    }

    /**
     * Parse table format data into SwimLaneData
     */
    private fun parseTableData(data: String): SwimLaneData {
        val lines = data.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        var title = "Swimlanes"
        var useDarkTheme = false
        var width = 900
        var height = 450

        // Maps to store lanes and their items during parsing
        val lanesMap = mutableMapOf<String, MutableList<SwimLaneItem>>()

        // Current lane and item being processed
        var currentLane: String? = null
        var currentItemTitle: String? = null
        val currentItemContent = mutableListOf<String>()

        // Parse configuration and content
        var inDataSection = false

        for (line in lines) {
            when {
                line.startsWith("title=") -> title = line.substring(6).trim()
                line.startsWith("title:") -> title = line.substring(6).trim()
                line.startsWith("dark=") -> useDarkTheme = line.substring(5).trim().toBoolean()
                line.startsWith("dark:") -> useDarkTheme = line.substring(5).trim().toBoolean()
                line.startsWith("width=") -> width = line.substring(6).trim().toIntOrNull() ?: 900
                line.startsWith("width:") -> width = line.substring(6).trim().toIntOrNull() ?: 900
                line.startsWith("height=") -> height = line.substring(7).trim().toIntOrNull() ?: 450
                line.startsWith("height:") -> height = line.substring(7).trim().toIntOrNull() ?: 450
                line == "---" -> inDataSection = true
                inDataSection && line.startsWith("## ") -> {
                    // New lane
                    if (currentLane != null && currentItemTitle != null && currentItemContent.isNotEmpty()) {
                        // Save the current item before moving to a new lane
                        val items = lanesMap.getOrPut(currentLane) { mutableListOf() }
                        items.add(SwimLaneItem(currentItemTitle, currentItemContent.toList()))
                        currentItemContent.clear()
                    }
                    currentLane = line.substring(3).trim()
                    currentItemTitle = null
                }
                inDataSection && line.startsWith("### ") -> {
                    // New item in current lane
                    if (currentLane != null && currentItemTitle != null && currentItemContent.isNotEmpty()) {
                        // Save the current item before moving to a new item
                        val items = lanesMap.getOrPut(currentLane) { mutableListOf() }
                        items.add(SwimLaneItem(currentItemTitle, currentItemContent.toList()))
                        currentItemContent.clear()
                    }
                    currentItemTitle = line.substring(4).trim()
                }
                inDataSection && currentLane != null && currentItemTitle != null -> {
                    // Content for current item
                    if (line.startsWith("- ")) {
                        currentItemContent.add(line.substring(2).trim())
                    } else if (line.startsWith("* ")) {
                        currentItemContent.add(line.substring(2).trim())
                    } else {
                        currentItemContent.add(line)
                    }
                }
            }
        }

        // Add the last item if there is one
        if (currentLane != null && currentItemTitle != null && currentItemContent.isNotEmpty()) {
            val items = lanesMap.getOrPut(currentLane) { mutableListOf() }
            items.add(SwimLaneItem(currentItemTitle, currentItemContent.toList()))
        }

        // Convert the map to a list of SwimLane objects
        val lanes = lanesMap.map { (title, items) ->
            SwimLane(title, items)
        }

        return SwimLaneData(title, lanes, useDarkTheme, width, height)
    }



    override fun handleSVG(payload: String, context: DocOpsContext): String {
        return handleSVG(payload)
    }
}
