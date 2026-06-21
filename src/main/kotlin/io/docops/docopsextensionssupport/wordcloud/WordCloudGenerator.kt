package io.docops.docopsextensionssupport.wordcloud


import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.util.BackgroundHelper
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
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
        val id = wordCloud.id
        val isDark = wordCloud.display.useDark
        val primary = if (isDark) "#00eaff" else "#2b6df8"
        val accent = if (isDark) "#ff8a3d" else "#e76f3c"
        val wordFont = if (isDark) "#d8f7ff" else "#122131"

        val sb = StringBuilder()
        sb.append("<defs>")

        sb.append(
            """
            <radialGradient id="bgRadialLight_$id" cx="20%" cy="18%" r="95%">
                <stop offset="0%" stop-color="#f9fcff"/>
                <stop offset="48%" stop-color="#f2f6fb"/>
                <stop offset="100%" stop-color="#e9eff6"/>
            </radialGradient>
            <radialGradient id="meshALight_$id" cx="24%" cy="18%" r="58%">
                <stop offset="0%" stop-color="#2b6df8" stop-opacity=".14"/>
                <stop offset="60%" stop-color="#2b6df8" stop-opacity=".04"/>
                <stop offset="100%" stop-color="#2b6df8" stop-opacity="0"/>
            </radialGradient>
            <radialGradient id="meshBLight_$id" cx="76%" cy="72%" r="52%">
                <stop offset="0%" stop-color="#e76f3c" stop-opacity=".13"/>
                <stop offset="75%" stop-color="#e76f3c" stop-opacity=".03"/>
                <stop offset="100%" stop-color="#e76f3c" stop-opacity="0"/>
            </radialGradient>

            <radialGradient id="bgRadialDark_$id" cx="18%" cy="22%" r="95%">
                <stop offset="0%" stop-color="#1b2534"/>
                <stop offset="48%" stop-color="#121a25"/>
                <stop offset="100%" stop-color="#0c121a"/>
            </radialGradient>
            <radialGradient id="meshADark_$id" cx="24%" cy="18%" r="58%">
                <stop offset="0%" stop-color="#00eaff" stop-opacity=".22"/>
                <stop offset="60%" stop-color="#00eaff" stop-opacity=".05"/>
                <stop offset="100%" stop-color="#00eaff" stop-opacity="0"/>
            </radialGradient>
            <radialGradient id="meshBDark_$id" cx="72%" cy="72%" r="52%">
                <stop offset="0%" stop-color="#ff8a3d" stop-opacity=".16"/>
                <stop offset="75%" stop-color="#ff8a3d" stop-opacity=".04"/>
                <stop offset="100%" stop-color="#ff8a3d" stop-opacity="0"/>
            </radialGradient>

            <filter id="noise_$id" x="-20%" y="-20%" width="140%" height="140%">
                <feTurbulence type="fractalNoise" baseFrequency="0.82" numOctaves="2" seed="7" result="n"/>
                <feColorMatrix in="n" type="saturate" values="0"/>
                <feComponentTransfer>
                    <feFuncA type="table" tableValues="0 0.04"/>
                </feComponentTransfer>
            </filter>

            <filter id="wordAtmosGlow_$id" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur stdDeviation="2.9" result="b"/>
                <feMerge>
                    <feMergeNode in="b"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>

            <filter id="wordShadow_$id">
                <feDropShadow dx="1.4" dy="1.4" stdDeviation="1.8" flood-color="rgba(0,0,0,0.18)"/>
            </filter>
            """.trimIndent()
        )

        sb.append(
            """
            <style>
                #wordcloud_$id .word-text {
                    font-family: 'Inter', 'Segoe UI', Roboto, Arial, sans-serif;
                    font-weight: 700;
                    cursor: pointer;
                    transition: all 0.28s ease;
                    fill: $wordFont;
                }
                #wordcloud_$id .word-text:hover {
                    filter: url(#wordAtmosGlow_$id);
                    transform: scale(1.06);
                }
                #wordcloud_$id .title-text {
                    font-family: 'Inter', 'Segoe UI', Roboto, Arial, sans-serif;
                    font-weight: 800;
                    letter-spacing: -0.01em;
                }
                #wordcloud_$id .accent-line {
                    fill: $primary;
                }
                #wordcloud_$id .accent-line-warm {
                    fill: $accent;
                }
            </style>
            """.trimIndent()
        )

        sb.append("</defs>")
        return sb.toString()
    }

    private fun makeBackground(wordCloud: WordCloud): String {
        val id = wordCloud.id
        val isDark = wordCloud.display.useDark

        val bgMain = if (isDark) "url(#bgRadialDark_$id)" else "url(#bgRadialLight_$id)"
        val bgA = if (isDark) "url(#meshADark_$id)" else "url(#meshALight_$id)"
        val bgB = if (isDark) "url(#meshBDark_$id)" else "url(#meshBLight_$id)"
        val contour = if (isDark) "#6fa2bd" else "#5b7b97"

        return """
            <rect width="100%" height="100%" fill="$bgMain"/>
            <rect width="100%" height="100%" fill="$bgA"/>
            <rect width="100%" height="100%" fill="$bgB"/>
            <g opacity=".35">
                <path d="M90,600 C230,480 420,470 590,540 C730,600 930,610 1110,470" fill="none" stroke="$contour" stroke-opacity=".17" stroke-width="1.2"/>
                <path d="M60,440 C220,300 390,270 560,330 C730,390 915,405 1140,260" fill="none" stroke="$contour" stroke-opacity=".17" stroke-width="1.2"/>
                <path d="M160,200 C310,120 470,120 640,170 C830,230 980,210 1120,120" fill="none" stroke="$contour" stroke-opacity=".17" stroke-width="1.2"/>
                <circle cx="220" cy="150" r="120" fill="none" stroke="$contour" stroke-opacity=".17" stroke-width="1.2"/>
                <circle cx="960" cy="560" r="170" fill="none" stroke="$contour" stroke-opacity=".17" stroke-width="1.2"/>
            </g>
            <rect width="100%" height="100%" filter="url(#noise_$id)"/>
        """.trimIndent()
    }

    private fun makeTitle(wordCloud: WordCloud): String {
        if (wordCloud.title.isEmpty()) return ""

        val titleColor = if (wordCloud.display.useDark) "#a9c1d4" else "#2c4158"

        return """
            <g transform="translate(24.0, 38)">
                <text fill="$titleColor" font-size="24" class="title-text">${escapeXml(wordCloud.title)}</text>
                <rect y="8" width="92" height="4" rx="2" class="accent-line"/>
                <rect x="98" y="8" width="28" height="4" rx="2" class="accent-line-warm" opacity=".9"/>
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
        val weightRange = if (maxWeight > minWeight) maxWeight - minWeight else 1.0

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

            // Calculate font size based on weight (hybrid log/linear)
            val fontSize = if (maxWeight > minWeight * 10.0 && minWeight > 0) {
                minFontSize + (ln(word.weight) - ln(minWeight)) / (ln(maxWeight) - ln(minWeight)) * (maxFontSize - minFontSize)
            } else if (maxWeight > minWeight) {
                minFontSize + ((word.weight - minWeight) / weightRange) * (maxFontSize - minFontSize)
            } else {
                (maxFontSize + minFontSize) / 2
            }

            // Get color from itemDisplay if available
            val color = display.baseColor ?: defaultColors[index % defaultColors.size]

            val rotation = if (position.rotated) "rotate(-90 ${position.x} ${position.y})" else ""
            sb.append("""
                <text class="word-text" x="${position.x}" y="${position.y}" 
                      text-anchor="middle" dominant-baseline="central" font-size="${round(fontSize)}" 
                      fill="$color" filter="url(#wordShadow_${wordCloud.id})"
                      ${if (position.rotated) "transform=\"$rotation\"" else ""}>
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
            val fontSize = if (maxWeight > minWeight * 10.0 && minWeight > 0) {
                minFontSize + (ln(word.weight) - ln(minWeight)) / (ln(maxWeight) - ln(minWeight)) * (maxFontSize - minFontSize)
            } else if (maxWeight > minWeight) {
                minFontSize + ((word.weight - minWeight) / weightRange) * (maxFontSize - minFontSize)
            } else {
                (maxFontSize + minFontSize) / 2
            }

            // Word dimensions estimation
            val wordWidth = word.text.length * fontSize * 0.65
            val wordHeight = fontSize * 1.2

            // Vertical rotation: approx 20% chance, but not for the most important words (first 3)
            val isRotated = words.indexOf(word) > 2 && Random.nextDouble() < 0.2
            val effectiveWidth = if (isRotated) wordHeight else wordWidth
            val effectiveHeight = if (isRotated) wordWidth else wordHeight

            // Try to place word using spiral algorithm with better spacing
            var isPlaced = false
            var angle = Random.nextDouble() * 2 * PI  // Start at random angle
            var radius = 0.0
            val radiusStep = 5.0  
            val angleStep = 0.1  

            var attempts = 0
            val maxAttempts = 5000

            while (!isPlaced && attempts < maxAttempts) {
                val x = centerX + radius * cos(angle)
                val y = centerY + radius * sin(angle)

                // Check if position is valid (within bounds with padding)
                if (x - effectiveWidth / 2 > 40 && x + effectiveWidth / 2 < 760 &&
                    y - effectiveHeight / 2 > 70 && y + effectiveHeight / 2 < 580) {

                    // Check for overlaps with better spacing
                    val overlaps = placedWords.any { placedWord ->
                        val dx = x - placedWord.x
                        val dy = y - placedWord.y

                        // Calculate bounding box overlap with padding
                        val padding = 8.0
                        val horizontalOverlap = abs(dx) < (effectiveWidth + placedWord.width) / 2 + padding
                        val verticalOverlap = abs(dy) < (effectiveHeight + placedWord.height) / 2 + padding

                        horizontalOverlap && verticalOverlap
                    }

                    if (!overlaps) {
                        positions.add(WordPosition(x, y, isRotated))
                        placedWords.add(PlacedWord(x, y, effectiveWidth, effectiveHeight))
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

                    if (x - effectiveWidth / 2 > 40 && x + effectiveWidth / 2 < 760 &&
                        y - effectiveHeight / 2 > 70 && y + effectiveHeight / 2 < 580) {

                        val overlaps = placedWords.any { placedWord ->
                            val dx = x - placedWord.x
                            val dy = y - placedWord.y
                            val padding = 8.0
                            val horizontalOverlap = abs(dx) < (effectiveWidth + placedWord.width) / 2 + padding
                            val verticalOverlap = abs(dy) < (effectiveHeight + placedWord.height) / 2 + padding
                            horizontalOverlap && verticalOverlap
                        }

                        if (!overlaps) {
                            positions.add(WordPosition(x, y, isRotated))
                            placedWords.add(PlacedWord(x, y, effectiveWidth, effectiveHeight))
                            isPlaced = true
                        }
                    }
                    fallbackAttempts++
                }
            }

            // Final fallback: place near center with dispersed jitter if spiral fails
            if (!isPlaced) {
                val jitterX = (Random.nextDouble() - 0.5) * 200.0
                val jitterY = (Random.nextDouble() - 0.5) * 150.0
                positions.add(WordPosition(centerX + jitterX, centerY + jitterY, false))
                placedWords.add(PlacedWord(centerX + jitterX, centerY + jitterY, wordWidth, wordHeight))
            }
        }

        return positions
    }

    private data class WordPosition(val x: Double, val y: Double, val rotated: Boolean = false)



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