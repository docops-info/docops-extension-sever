package io.docops.docopsextensionssupport.wordcloud

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
class WordCloud @OptIn(ExperimentalUuidApi::class) constructor(
    val title: String,
    val words: MutableList<Word>,
    val display: WordCloudDisplay = WordCloudDisplay(baseColor = null),
    val id: String = Uuid.random().toHexString()
)

@Serializable
class Word @OptIn(ExperimentalUuidApi::class) constructor(
    val text: String,
    val weight: Double,
    val itemDisplay: WordCloudDisplay? = null,
    val id: String = Uuid.random().toHexString()
)

@Serializable
class WordCloudDisplay @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toHexString(),
    val baseColor: String?,
    val useDark: Boolean = false,
    val width: Int = 800,
    val height: Int = 600,
    val minFontSize: Int = 10,
    val maxFontSize: Int = 60,
    val shape: String = "rectangle", // rectangle, circle, etc.
    val scale: Float = 1.0f
)

// Extension functions for WordCloud
fun WordCloud.calcFontSize(word: Word): Int {
    val weights = words.map { it.weight }
    val minWeight = weights.minOrNull() ?: 1.0
    val maxWeight = weights.maxOrNull() ?: 100.0

    // Normalize the weight to a value between 0 and 1
    val normalizedWeight = if (maxWeight == minWeight) {
        0.5 // If all weights are the same, use middle font size
    } else {
        (word.weight - minWeight) / (maxWeight - minWeight)
    }

    // Calculate font size using logarithmic scale for better distribution
    val fontSize = display.minFontSize + (normalizedWeight * (display.maxFontSize - display.minFontSize))
    return fontSize.toInt()
}

fun WordCloud.calcWidth(): Int {
    return (display.width * display.scale).toInt()
}

fun WordCloud.calcHeight(): Int {
    return (display.height * display.scale).toInt()
}

fun WordCloud.centerX(): Int {
    return calcWidth() / 2
}

fun WordCloud.centerY(): Int {
    return calcHeight() / 2
}

/**
 * Converts a WordCloud to CSV format
 * @return CsvResponse with headers and rows representing the word cloud data
 */
fun WordCloud.toCsv(): CsvResponse {
    val headers = listOf("Title", "Word Number", "Text", "Weight", "ID", "Base Color", "Use Dark", "Width", "Height", "Min Font Size", "Max Font Size", "Shape", "Scale")
    val csvRows = mutableListOf<List<String>>()

    if (words.isNotEmpty()) {
        words.forEachIndexed { index, word ->
            csvRows.add(listOf(
                if (index == 0) title else "", // Only show title in first row
                (index + 1).toString(),
                word.text,
                word.weight.toString(),
                word.id,
                if (index == 0) display.useDark.toString() else "", // Only show display settings in first row
                if (index == 0) display.width.toString() else "",
                if (index == 0) display.height.toString() else "",
                if (index == 0) display.minFontSize.toString() else "",
                if (index == 0) display.maxFontSize.toString() else "",
                if (index == 0) display.shape else "",
                if (index == 0) display.scale.toString() else ""
            ))
        }
    } else {
        // If no words, just add title row with display configuration
        csvRows.add(listOf(
            title,
            "0",
            "",
            "",
            "",
            display.useDark.toString(),
            display.width.toString(),
            display.height.toString(),
            display.minFontSize.toString(),
            display.maxFontSize.toString(),
            display.shape,
            display.scale.toString()
        ))
    }

    return CsvResponse(headers, csvRows)
}