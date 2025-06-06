package io.docops.docopsextensionssupport.wordcloud

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

@Serializable
class WordCloud(
    val title: String,
    val words: MutableList<Word>,
    val display: WordCloudDisplay = WordCloudDisplay()
)

@Serializable
class Word(
    val text: String,
    val weight: Double,
    val itemDisplay: WordCloudDisplay? = null,
    val id: String = UUID.randomUUID().toString()
)

@Serializable
class WordCloudDisplay(
    val id: String = UUID.randomUUID().toString(),
    val baseColor: String = "#3498db",
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