package io.docops.docopsextensionssupport.wordcloud


import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.util.BackgroundHelper
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random


class WordCloudMaker {

    private val defaultColors = listOf(
        "#3498db", "#2ecc71", "#e74c3c", "#f39c12", "#9b59b6",
        "#1abc9c", "#34495e", "#e67e22", "#27ae60", "#d35400",
        "#2980b9", "#d68910", "#8e44ad", "#16a085", "#c0392b"
    )

    fun makeWordCloud(wordCloud: WordCloud): String {
        val sb = StringBuilder()

        sb.append(makeHead(wordCloud))
        sb.append(makeDefs(wordCloud))
        sb.append(makeBackground(wordCloud))
        sb.append(makeTitle(wordCloud))
        sb.append(makeWords(wordCloud))

        sb.append("</svg>")
        return sb.toString()
    }

    private fun makeHead(wordCloud: WordCloud): String {
        val width = 800
        val height = 600
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg id="wordcloud_${wordCloud.id}" width="${(width * wordCloud.display.scale) / DISPLAY_RATIO_16_9}" 
                 height="${(height * wordCloud.display.scale) / DISPLAY_RATIO_16_9}" 
                 viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg" 
                 aria-label='DocOps: Word Cloud'>
        """.trimIndent()
    }

    private fun makeDefs(wordCloud: WordCloud): String {
        val sb = StringBuilder()
        sb.append("<defs>")

        // Add background gradient from helper
        sb.append(BackgroundHelper.getBackgroundGradient(wordCloud.display.useDark, wordCloud.id))

        // Drop shadow for words
        sb.append("""
            <filter id="wordShadow_${wordCloud.id}">
                <feDropShadow dx="2" dy="2" stdDeviation="2" flood-color="rgba(0,0,0,0.2)"/>
            </filter>
        """.trimIndent())

        // Glow effect for hover
        sb.append("""
            <filter id="wordGlow_${wordCloud.id}">
                <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                <feMerge>
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
        """.trimIndent())

        // Styles
        sb.append("""
            <style>
                #wordcloud_${wordCloud.id} .word-text {
                    font-family: 'Arial', sans-serif;
                    font-weight: bold;
                    cursor: pointer;
                    transition: all 0.3s ease;
                }
                #wordcloud_${wordCloud.id} .word-text:hover {
                    filter: url(#wordGlow_${wordCloud.id});
                    transform: scale(1.1);
                }
            </style>
        """.trimIndent())

        sb.append("</defs>")
        return sb.toString()
    }

    private fun makeBackground(wordCloud: WordCloud): String {
        val sb = StringBuilder()

        // Base background
        sb.append(BackgroundHelper.getBackGroundPath(wordCloud.display.useDark, wordCloud.id, wordCloud.display.width.toFloat(), wordCloud.display.height.toFloat()))

        return sb.toString()
    }

    private fun makeTitle(wordCloud: WordCloud): String {
        if (wordCloud.title.isEmpty()) return ""

        val titleColor = if (wordCloud.display.useDark) "#f9fafb" else "#333"
        val titleBgColor = if (wordCloud.display.useDark) "#374151" else "#f0f0f0"

        // Calculate title width based on text length
        val fontSize = 24
        val estimatedWidth = wordCloud.title.length * fontSize * 0.6
        val titleWidth = (estimatedWidth + 40).coerceAtLeast(200.0)  // Add padding, minimum 200
        val titleX = (800 - titleWidth) / 2  // Center the box

        return """
            <g>
                <rect x="$titleX" y="20" width="$titleWidth" height="50" rx="10" ry="10" 
                      fill="$titleBgColor" opacity="0.8"/>
                <text x="400" y="50" text-anchor="middle" font-size="$fontSize" 
                      font-weight="bold" fill="$titleColor" font-family="Arial, sans-serif">
                    ${escapeXml(wordCloud.title)}
                </text>
            </g>
        """.trimIndent()
    }

    private fun makeWords(wordCloud: WordCloud): String {
        val sb = StringBuilder()

        if (wordCloud.words.isEmpty()) {
            return ""
        }

        // Sort words by weight (largest first for better placement)
        val sortedWords = wordCloud.words.sortedByDescending { it.weight }

        // Calculate font sizes
        val maxWeight = sortedWords.maxOf { it.weight }
        val minWeight = sortedWords.minOf { it.weight }
        val weightRange = maxWeight - minWeight

        val maxFontSize = wordCloud.display.maxFontSize.toDouble()
        val minFontSize = wordCloud.display.minFontSize.toDouble()

        // Position words using spiral algorithm
        val positions = calculateWordPositions(sortedWords, maxWeight, minWeight,
            maxFontSize, minFontSize, wordCloud)

        sb.append("""<g id="words">""")

        // Render each word
        sortedWords.forEachIndexed { index, word ->
            val display = word.itemDisplay ?: wordCloud.display
            val position = positions[index]

            // Calculate font size based on weight
            val fontSize = if (weightRange > 0) {
                minFontSize + ((word.weight - minWeight) / weightRange) * (maxFontSize - minFontSize)
            } else {
                (maxFontSize + minFontSize) / 2
            }

            // Get color from itemDisplay if available
            val color = display.baseColor ?: defaultColors[index % defaultColors.size]

            sb.append("""
                <text class="word-text" x="${position.x}" y="${position.y}" 
                      text-anchor="middle" font-size="${round(fontSize)}" 
                      fill="$color" filter="url(#wordShadow_${wordCloud.id})">
                    ${escapeXml(word.text)}
                    <title>${escapeXml(word.text)}: weight ${formatDecimal(word.weight, 2)}</title>
                </text>
            """.trimIndent())
        }

        sb.append("</g>")
        return sb.toString()
    }


    private data class PlacedWord(
        val x: Double,
        val y: Double,
        val width: Double,
        val height: Double
    )

    private fun calculateWordPositions(
        words: List<Word>,
        maxWeight: Double,
        minWeight: Double,
        maxFontSize: Double,
        minFontSize: Double,
        wordCloud: WordCloud
    ): List<WordPosition> {
        val positions = mutableListOf<WordPosition>()
        val placedWords = mutableListOf<PlacedWord>()

        val centerX = 400.0
        val centerY = if (wordCloud.title.isEmpty()) 300.0 else 330.0
        val weightRange = if (maxWeight > minWeight) maxWeight - minWeight else 1.0

        words.forEach { word ->
            val fontSize = if (weightRange > 0) {
                minFontSize + ((word.weight - minWeight) / weightRange) * (maxFontSize - minFontSize)
            } else {
                (maxFontSize + minFontSize) / 2
            }

            // More accurate word dimensions estimation
            val wordWidth = word.text.length * fontSize * 0.55
            val wordHeight = fontSize * 1.2

            // Try to place word using spiral algorithm with better spacing
            var isPlaced = false
            var angle = Random.nextDouble() * 2 * PI  // Start at random angle
            var radius = 0.0
            val radiusStep = 3.0  // Smaller step for tighter packing
            val angleStep = 0.5   // Larger angle step for better distribution

            var attempts = 0
            val maxAttempts = 2000

            while (!isPlaced && attempts < maxAttempts) {
                val x = centerX + radius * cos(angle)
                val y = centerY + radius * sin(angle)

                // Check if position is valid (within bounds with padding)
                if (x - wordWidth / 2 > 60 && x + wordWidth / 2 < 740 &&
                    y - wordHeight / 2 > 90 && y + wordHeight / 2 < 570) {

                    // Check for overlaps with better spacing
                    val overlaps = placedWords.any { placedWord ->
                        val dx = x - placedWord.x
                        val dy = y - placedWord.y

                        // Calculate bounding box overlap with padding
                        val padding = 8.0
                        val horizontalOverlap = abs(dx) < (wordWidth + placedWord.width) / 2 + padding
                        val verticalOverlap = abs(dy) < (wordHeight + placedWord.height) / 2 + padding

                        horizontalOverlap && verticalOverlap
                    }

                    if (!overlaps) {
                        positions.add(WordPosition(x, y))
                        placedWords.add(PlacedWord(x, y, wordWidth, wordHeight))
                        isPlaced = true
                    }
                }

                // Move along spiral
                angle += angleStep
                radius += radiusStep * angleStep / (2 * PI)
                attempts++
            }

            // Fallback: try random positions if spiral fails
            if (!isPlaced) {
                var fallbackAttempts = 0
                while (!isPlaced && fallbackAttempts < 100) {
                    val x = centerX + (Random.nextDouble() - 0.5) * 600
                    val y = centerY + (Random.nextDouble() - 0.5) * 400

                    if (x - wordWidth / 2 > 60 && x + wordWidth / 2 < 740 &&
                        y - wordHeight / 2 > 90 && y + wordHeight / 2 < 570) {

                        val overlaps = placedWords.any { placedWord ->
                            val dx = x - placedWord.x
                            val dy = y - placedWord.y
                            val padding = 8.0
                            val horizontalOverlap = abs(dx) < (wordWidth + placedWord.width) / 2 + padding
                            val verticalOverlap = abs(dy) < (wordHeight + placedWord.height) / 2 + padding
                            horizontalOverlap && verticalOverlap
                        }

                        if (!overlaps) {
                            positions.add(WordPosition(x, y))
                            placedWords.add(PlacedWord(x, y, wordWidth, wordHeight))
                            isPlaced = true
                        }
                    }
                    fallbackAttempts++
                }
            }

            // Final fallback: place at center (should rarely happen)
            if (!isPlaced) {
                positions.add(WordPosition(centerX, centerY))
                placedWords.add(PlacedWord(centerX, centerY, wordWidth, wordHeight))
            }
        }

        return positions
    }

    private data class WordPosition(val x: Double, val y: Double)



    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun formatDecimal(value: Double, decimals: Int): String {
        val multiplier = when (decimals) {
            0 -> 1.0
            1 -> 10.0
            2 -> 100.0
            else -> decimals.toDouble().pow(10.0)
        }
        val rounded = round(value * multiplier) / multiplier
        return if (decimals == 0) {
            rounded.toInt().toString()
        } else {
            val str = rounded.toString()
            val dotIndex = str.indexOf('.')
            if (dotIndex == -1) {
                str + "." + "0".repeat(decimals)
            } else {
                val currentDecimals = str.length - dotIndex - 1
                when {
                    currentDecimals < decimals -> str + "0".repeat(decimals - currentDecimals)
                    currentDecimals > decimals -> str.take(dotIndex + decimals + 1)
                    else -> str
                }
            }
        }
    }
}