package io.docops.docopsextensionssupport.wordcloud

import io.docops.docopsextensionssupport.util.ParsingUtils
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.update
import java.util.UUID

class WordCloudImproved(val useDark: Boolean) {

    private val lightPalette = listOf(
        "#2b6df8", "#e76f3c", "#1f4fbf", "#355f91", "#4b5f73",
        "#8c5338", "#2d7a9f", "#405c84", "#d76a3b", "#254a72"
    )

    private val darkPalette = listOf(
        "#00eaff", "#ff8a3d", "#9ff6ff", "#6ed3ff", "#93a9bb",
        "#ffb07a", "#48c8ff", "#5fa7c9", "#ffa770", "#7dbed8"
    )

    fun makeWordCloudSvg(payload: String, csvResponse: CsvResponse): String {
        val (config, cloudData) = parseConfigAndData(payload)
        val wordCloud = createWordCloudFromData(config, cloudData)
        csvResponse.update(wordCloud.toCsv())
        val wordCloudMaker = WordCloudMaker()
        return wordCloudMaker.makeWordCloud(wordCloud)
    }

    private fun parseConfigAndData(content: String): Pair<Map<String, String>, String> {
        return ParsingUtils.parseConfigAndData(content)
    }

    private fun createWordCloudFromData(config: Map<String, String>, wordData: String): WordCloud {
        val title = config.getOrDefault("title", "TECH ATLAS — SIGNAL CLUSTERS")
        val baseColorDefault = if (useDark) "#00eaff" else "#2b6df8"
        val baseColor = config.getOrDefault("baseColor", baseColorDefault)
        val width = config["width"]?.toIntOrNull() ?: 1200
        val height = config["height"]?.toIntOrNull() ?: 760
        val minFontSize = config["minFontSize"]?.toIntOrNull() ?: 22
        val maxFontSize = config["maxFontSize"]?.toIntOrNull() ?: 86
        val shape = config.getOrDefault("shape", "rectangle")
        val scale = config["scale"]?.toFloatOrNull() ?: 1.0f

        val palette = if (useDark) darkPalette else lightPalette

        val words = mutableListOf<Word>()
        wordData.lines().forEachIndexed { index, line ->
            if (line.isNotBlank()) {
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 2) {
                    val text = parts[0]
                    val weight = parts[1].toDoubleOrNull() ?: 1.0
                    val explicitColor = if (parts.size > 2 && parts[2].isNotBlank()) parts[2] else null
                    val resolvedColor = explicitColor ?: palette[index % palette.size]

                    val itemDisplay = WordCloudDisplay(
                        id = UUID.randomUUID().toString(),
                        baseColor = resolvedColor,
                        useDark = useDark,
                        width = width,
                        height = height,
                        minFontSize = minFontSize,
                        maxFontSize = maxFontSize,
                        shape = shape,
                        scale = scale
                    )

                    words.add(Word(text, weight, itemDisplay))
                }
            }
        }

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