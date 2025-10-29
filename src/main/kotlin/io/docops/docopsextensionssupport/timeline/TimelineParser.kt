package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.util.ParsingUtils
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

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
                log.warn { "Unknown timeline type '$typeRaw'; defaulting to vertical" }
                Orientation.VERTICAL
            }
        }

        val lines = payload.trim().lines()
        val events = mutableListOf<TimelineEvent>()
        var currentDate: String? = null
        var currentText: String? = null

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            when {
                trimmedLine.startsWith("date:") -> {
                    // Save previous event if exists; if incomplete, warn and drop
                    if (currentDate != null && currentText != null) {
                        events.add(TimelineEvent(currentDate!!, currentText!!))
                    } else if (currentDate != null && currentText == null) {
                        log.warn { "Skipping event with date='$currentDate' because 'text' is missing" }
                    }
                    currentDate = trimmedLine.removePrefix("date:").trim()
                    currentText = null
                }
                trimmedLine.startsWith("text:") -> {
                    if (currentDate == null) {
                        // Text without a preceding date - cannot associate; warn and skip
                        log.warn { "Encountered 'text' without a preceding 'date'; skipping line" }
                        continue
                    }
                    currentText = trimmedLine.removePrefix("text:").trim()
                }
                trimmedLine == "---" -> {
                    // Separator between events
                    if (currentDate != null && currentText != null) {
                        events.add(TimelineEvent(currentDate!!, currentText!!))
                    } else if (currentDate != null && currentText == null) {
                        log.warn { "Skipping event with date='$currentDate' because 'text' is missing before separator" }
                    }
                    currentDate = null
                    currentText = null
                }
                else -> {
                    // Continuation of text
                    if (currentText != null) {
                        currentText += " $trimmedLine"
                    } else if (currentDate == null) {
                        // Stray content; ignore softly
                        log.debug { "Ignoring unrecognized line outside of an event: '$trimmedLine'" }
                    }
                }
            }
        }

        // Add last event, warn if incomplete
        if (currentDate != null && currentText != null) {
            events.add(TimelineEvent(currentDate, currentText))
        } else if (currentDate != null && currentText == null) {
            log.warn { "Skipping final event with date='$currentDate' because 'text' is missing" }
        }

        // Apply parsed visual config into TimelineConfig (if supported)
        return TimelineConfig(
            events = events,
            lightColor = lightColor,
            lightColorIndex = lightColorIndex?.toIntOrNull(),
            orientation = orientation
        )
    }
}