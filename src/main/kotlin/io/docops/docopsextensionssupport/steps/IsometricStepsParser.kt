package io.docops.docopsextensionssupport.steps

class IsometricStepsParser {

    fun parse(content: String): IsometricSteps {
        val parts = content.split("---")
        if (parts.size < 2) {
            throw IllegalArgumentException("Payload must contain '---' separator")
        }

        val configBlock = parts[0]
        val dataBlock = parts[1]

        val config = parseConfig(configBlock)
        val steps = parseSteps(dataBlock)

        if (steps.isEmpty()) {
            throw IllegalArgumentException("Empty step list")
        }

        val sortedSteps = steps.sortedBy { it.order }

        // Validate duplicate order
        val orders = sortedSteps.map { it.order }
        if (orders.distinct().size != orders.size) {
            throw IllegalArgumentException("Duplicate order values found")
        }

        return IsometricSteps(config, sortedSteps)
    }

    private fun parseConfig(block: String): IsometricStepsConfig {
        val map = mutableMapOf<String, String>()
        block.lines().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isNotEmpty() && !line.startsWith("#")) {
                val kv = line.split("=", limit = 2)
                if (kv.size == 2) {
                    map[kv[0].trim()] = kv[1].trim()
                }
            }
        }

        return IsometricStepsConfig(
            infographic = map["infographic"] ?: throw IllegalArgumentException("infographic is required"),
            version = map["version"] ?: "1",
            title = map["title"] ?: throw IllegalArgumentException("title is required"),
            subtitle = map["subtitle"],
            view = map["view"] ?: "isometric",
            canvasWidth = map["canvasWidth"]?.toIntOrNull() ?: 1220,
            canvasHeight = map["canvasHeight"]?.toIntOrNull() ?: 760,
            theme = map["theme"] ?: "classic",
            palette = map["palette"] ?: "vibrant",
            startX = map["startX"]?.toIntOrNull(),
            startY = map["startY"]?.toIntOrNull(),
            dx = map["dx"]?.toIntOrNull(),
            dy = map["dy"]?.toIntOrNull(),
            labelOffsetX = map["labelOffsetX"]?.toIntOrNull() ?: 130,
            labelOffsetY = map["labelOffsetY"]?.toIntOrNull() ?: 65,
            maxDescLines = map["maxDescLines"]?.toIntOrNull() ?: 2
        )
    }

    private fun parseSteps(block: String): List<IsometricStep> {
        val steps = mutableListOf<IsometricStep>()
        val lines = block.lines().filter { it.trim().isNotEmpty() && !it.trim().startsWith("#") }
        if (lines.isEmpty()) return steps

        val firstLine = lines[0].trim()
        val isLegacy = firstLine.contains("=")

        if (isLegacy) {
            lines.forEach { line ->
                val tokens = line.split("|")
                val stepMap = mutableMapOf<String, String>()
                tokens.forEach { token ->
                    val kv = token.split("=", limit = 2)
                    if (kv.size == 2) {
                        stepMap[kv[0].trim()] = kv[1].trim()
                    }
                }

                val order = stepMap["order"]?.toIntOrNull() ?: throw IllegalArgumentException("order is required and must be an integer")
                val title = stepMap["title"] ?: throw IllegalArgumentException("title is required for each step")
                val desc = stepMap["desc"] ?: ""
                val color = stepMap["color"]
                val icon = stepMap["icon"]

                steps.add(IsometricStep(order, title, desc, color, icon))
            }
        } else {
            val header = firstLine.split("|").map { it.trim().lowercase() }
            val orderIdx = header.indexOfFirst { it == "order" }
            val titleIdx = header.indexOfFirst { it == "title" }
            val descIdx = header.indexOfFirst { it == "description" || it == "desc" }
            val colorIdx = header.indexOfFirst { it == "color" }
            val iconIdx = header.indexOfFirst { it == "icon" }

            if (orderIdx == -1 || titleIdx == -1) {
                throw IllegalArgumentException("Header must contain 'Order' and 'Title' columns. Found: $header")
            }

            for (i in 1 until lines.size) {
                val row = lines[i].split("|").map { it.trim() }
                if (row.size <= maxOf(orderIdx, titleIdx)) continue

                val orderStr = if (orderIdx < row.size) row[orderIdx] else ""
                val order = orderStr.toIntOrNull() ?: throw IllegalArgumentException("Row $i: order must be an integer, found '$orderStr'")
                val title = if (titleIdx < row.size) row[titleIdx] else throw IllegalArgumentException("Row $i: title is required")
                val desc = if (descIdx != -1 && descIdx < row.size) row[descIdx] else ""
                val color = if (colorIdx != -1 && colorIdx < row.size) row[colorIdx] else null
                val icon = if (iconIdx != -1 && iconIdx < row.size) row[iconIdx] else null

                steps.add(IsometricStep(order, title, desc, color, icon))
            }
        }
        return steps
    }
}
