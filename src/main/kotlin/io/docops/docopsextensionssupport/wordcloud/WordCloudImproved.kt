package io.docops.docopsextensionssupport.wordcloud

import io.docops.docopsextensionssupport.util.ParsingUtils
import java.util.UUID

class WordCloudImproved {

    // Modern color palette for word cloud
    private val defaultColors = listOf(
        "#3498db", // Blue
        "#2ecc71", // Green
        "#e74c3c", // Red
        "#f39c12", // Orange
        "#9b59b6", // Purple
        "#1abc9c", // Turquoise
        "#34495e", // Dark Blue
        "#e67e22", // Dark Orange
        "#27ae60", // Dark Green
        "#d35400"  // Burnt Orange
    )

    fun makeWordCloudSvg(payload: String): String {
        // Parse configuration and data from content
        val (config, cloudData) = parseConfigAndData(payload)

        // Create WordCloud object from parsed data
        val wordCloud = createWordCloudFromData(config, cloudData)

        // Use WordCloudMaker to generate SVG
        val wordCloudMaker = WordCloudMaker()
        return wordCloudMaker.makeWordCloud(wordCloud)
    }

    /**
     * Parses the content to extract configuration parameters and word cloud data.
     * Uses the shared ParsingUtils for consistent parsing across the application.
     *
     * @param content The full content of the block
     * @return A Pair containing the configuration map and the word data string
     */
    private fun parseConfigAndData(content: String): Pair<Map<String, String>, String> {
        return ParsingUtils.parseConfigAndData(content)
    }

    /**
     * Creates a WordCloud object from the parsed configuration and data.
     *
     * @param config The configuration map
     * @param wordData The word data string
     * @return A WordCloud object
     */
    private fun createWordCloudFromData(config: Map<String, String>, wordData: String): WordCloud {
        val title = config.getOrDefault("title", "Word Cloud")
        val baseColor = config.getOrDefault("baseColor", "#3498db")
        val useDark = config["darkMode"]?.toBoolean() ?: false
        val width = config["width"]?.toIntOrNull() ?: 800
        val height = config["height"]?.toIntOrNull() ?: 600
        val minFontSize = config["minFontSize"]?.toIntOrNull() ?: 10
        val maxFontSize = config["maxFontSize"]?.toIntOrNull() ?: 60
        val shape = config.getOrDefault("shape", "rectangle")
        val scale = config["scale"]?.toFloatOrNull() ?: 1.0f

        // Parse word data
        val words = mutableListOf<Word>()
        wordData.lines().forEach { line ->
            if (line.isNotBlank()) {
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 2) {
                    val text = parts[0]
                    val weight = parts[1].toDoubleOrNull() ?: 1.0
                    val color = if (parts.size > 2 && parts[2].isNotBlank()) parts[2] else null

                    // Create itemDisplay if color is provided
                    val itemDisplay = if (color != null) {
                        WordCloudDisplay(
                            id = UUID.randomUUID().toString(),
                            baseColor = color,
                            useDark = useDark,
                            width = width,
                            height = height,
                            minFontSize = minFontSize,
                            maxFontSize = maxFontSize,
                            shape = shape,
                            scale = scale
                        )
                    } else null

                    words.add(Word(text, weight, itemDisplay))
                }
            }
        }

        // Create display object
        val display = WordCloudDisplay(
            baseColor = baseColor,
            useDark = useDark,
            width = width,
            height = height,
            minFontSize = minFontSize,
            maxFontSize = maxFontSize,
            shape = shape,
            scale = scale
        )

        return WordCloud(title, words, display)
    }
}
