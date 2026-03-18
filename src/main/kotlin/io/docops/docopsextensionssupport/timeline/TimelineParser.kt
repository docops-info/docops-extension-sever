package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.util.ParsingUtils
import kotlin.text.get


class TimelineParser {

    fun parse(input: String): TimelineConfig {
        // Use ParsingUtils to split config (front-matter style) and payload by '---'
        val (configMap, payload) = ParsingUtils.parseConfigAndData(input)

        // Extract optional visual config (e.g., light mode color) from configMap
        val lightColor = configMap["lightColor"] ?: configMap["color"] ?: configMap["light"]
        val lightColorIndex = configMap["lightColorIdx"] ?: configMap["lightColorIndex"] ?: configMap["colorIdx"]

        // Orientation parsing: default to vertical
        val typeRaw = configMap["type"]?.trim()?.uppercase()
        val orientation = when (typeRaw) {
            null -> Orientation.VERTICAL
            "H" -> Orientation.HORIZONTAL
            "V" -> Orientation.VERTICAL
            else -> {
                Orientation.VERTICAL
            }
        }

        val lines = payload.trim().lines()
        val events = mutableListOf<TimelineEvent>()
        var currentDate: String? = null
        var currentText: String? = null
        val currentBullets = mutableListOf<String>()

        fun flushEvent() {
            if (currentDate != null && currentText != null) {
                events.add(TimelineEvent(currentDate!!, currentText!!, currentBullets.toList()))
            }
        }

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            when {
                trimmedLine.startsWith("date=") -> {
                    flushEvent()
                    currentDate = trimmedLine.removePrefix("date=").trim()
                    currentText = null
                    currentBullets.clear()
                }
                trimmedLine.startsWith("text=") -> {
                    if (currentDate == null) continue
                    currentText = trimmedLine.removePrefix("text=").trim()
                }
                trimmedLine.startsWith("• ") || trimmedLine.startsWith("* ") -> {
                    if (currentText != null) {
                        currentBullets.add(trimmedLine.removePrefix("• ").removePrefix("* ").trim())
                    }
                }
                trimmedLine == "---" -> {
                    flushEvent()
                    currentDate = null
                    currentText = null
                    currentBullets.clear()
                }
                else -> {
                    if (currentText != null) {
                        currentText += " $trimmedLine"
                    }
                }
            }
        }

        // Flush final event
        flushEvent()

        // Apply parsed visual config into TimelineConfig (if supported)
        return TimelineConfig(
            events = events,
            lightColor = lightColor,
            lightColorIndex = lightColorIndex?.toIntOrNull(),
            orientation = orientation
        )
    }
}