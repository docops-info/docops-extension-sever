package io.docops.docopsextensionssupport.support

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import kotlin.math.max

/**
 * Wraps text into lines using an approximate width model.
 * This is more suitable for SVG rendering than raw character-count wrapping.
 */
fun wrapTextToWidth(text: String, maxWidth: Float, avgCharWidth: Float = 7.2f): List<String> {
    val maxCharsPerLine = max(1, (maxWidth / avgCharWidth).toInt())
    val words = text.trim().escapeXml().split(Regex("\\s+")).filter { it.isNotBlank() }

    if (words.isEmpty()) return emptyList()

    val lines = mutableListOf<String>()
    var currentLine = StringBuilder()

    fun flushLine() {
        val line = currentLine.toString().trim()
        if (line.isNotBlank()) lines.add(line)
        currentLine = StringBuilder()
    }

    for (word in words) {
        val currentLength = currentLine.length
        val nextLength = if (currentLength == 0) word.length else currentLength + 1 + word.length

        if (nextLength > maxCharsPerLine && currentLine.isNotEmpty()) {
            flushLine()
        }

        if (currentLine.isNotEmpty()) currentLine.append(" ")
        currentLine.append(word)
    }

    flushLine()
    return lines
}