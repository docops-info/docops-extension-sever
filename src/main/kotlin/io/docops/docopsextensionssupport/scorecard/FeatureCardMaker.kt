package io.docops.docopsextensionssupport.scorecard

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component


class FeatureCardMaker(val useDark: Boolean) {

    private val logger = KotlinLogging.logger {}

    fun parseTabularInput(input: String): ParsedFeatureCards {
        logger.info { "Parsing tabular input for feature cards" }

        val lines = input.trim().split("\n").map { it.trim() }
        val cards = mutableListOf<FeatureCard>()
        val theme = if(useDark) {
            CardTheme.DARK
        } else {
            CardTheme.LIGHT
        }
        var layout = CardLayout.GRID

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                i++
                continue
            }

            // Parse configuration directives
            if (line.startsWith("@")) {
                when {
                    line.startsWith("@layout:") -> {
                        layout = CardLayout.valueOf(line.substringAfter("@layout:").trim().uppercase())
                    }
                }
                i++
                continue
            }

            // Parse table headers (skip them)
            if (line.contains("|") && (line.contains("Title") || line.contains("---"))) {
                i++
                continue
            }

            // Parse card data from table format
            if (line.contains("|")) {
                val parts = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }

                if (parts.size >= 3) {
                    val title = parts[0]
                    val description = parts[1]
                    val emoji = parts[2]
                    val colorScheme = if (parts.size > 3 && parts[3].isNotEmpty()) {
                        try {
                            ColorScheme.valueOf(parts[3].uppercase())
                        } catch (e: Exception) {
                            ColorScheme.BLUE
                        }
                    } else {
                        ColorScheme.BLUE
                    }

                    // Parse details (if provided in next lines starting with >>)
                    val details = mutableListOf<String>()
                    var j = i + 1
                    while (j < lines.size && lines[j].startsWith(">>")) {
                        details.add(lines[j].substring(2).trim())
                        j++
                    }

                    cards.add(FeatureCard(
                        title = title,
                        description = description,
                        emoji = emoji,
                        details = details,
                        colorScheme = colorScheme
                    ))

                    i = j
                } else {
                    i++
                }
            } else {
                i++
            }
        }

        return ParsedFeatureCards(cards, theme, layout)
    }

    fun createFeatureCardsSvg(parsedCards: ParsedFeatureCards): String {
        logger.info { "Creating SVG for ${parsedCards.cards.size} feature cards" }

        val cards = parsedCards.cards
        val theme = parsedCards.theme
        val layout = parsedCards.layout

        if (cards.isEmpty()) {
            return createEmptyCardsSvg()
        }

        return when (layout) {
            CardLayout.GRID -> createGridLayout(cards, theme)
            CardLayout.COLUMN -> createColumnLayout(cards, theme)
            CardLayout.ROW -> createRowLayout(cards, theme)
        }
    }

    private fun createGridLayout(cards: List<FeatureCard>, theme: CardTheme): String {
        val cardsPerRow = minOf(3, cards.size)
        val rows = (cards.size + cardsPerRow - 1) / cardsPerRow
        val cardWidth = 280
        val cardHeight = 400
        val gap = 20
        val totalWidth = cardsPerRow * cardWidth + (cardsPerRow - 1) * gap
        val totalHeight = rows * cardHeight + (rows - 1) * gap

        val svgBuilder = StringBuilder()
        svgBuilder.append("""
        <?xml version="1.0" encoding="UTF-8"?>
        <svg height="$totalHeight" width="$totalWidth" viewBox="0 0 $totalWidth $totalHeight" xmlns="http://www.w3.org/2000/svg">
        <defs>
            ${generateGradients(cards, theme)}
        </defs>
        <style>
            ${generateStyles(theme)}
        </style>
    """.trimIndent())

        cards.forEachIndexed { index, card ->
            val row = index / cardsPerRow
            val col = index % cardsPerRow
            val x = col * (cardWidth + gap)
            val y = row * (cardHeight + gap)

            svgBuilder.append(generateCardSvg(card, x, y, cardWidth, cardHeight, index, theme))
        }

        // Add JavaScript for interactivity
        svgBuilder.append(generateJavaScript())
        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }

    private fun createColumnLayout(cards: List<FeatureCard>, theme: CardTheme): String {
        val cardWidth = 280
        val gap = 20
        val totalWidth = cardWidth

        // Calculate dynamic heights for each card
        val cardHeights = cards.map { calculateCardHeight(it) }
        val totalHeight = cardHeights.sum() + (cards.size - 1) * gap

        val svgBuilder = StringBuilder()
        svgBuilder.append("""
        <?xml version="1.0" encoding="UTF-8"?>
        <svg height="$totalHeight" width="$totalWidth" viewBox="0 0 $totalWidth $totalHeight" xmlns="http://www.w3.org/2000/svg">
        <defs>
            ${generateGradients(cards, theme)}
        </defs>
        <style>
            ${generateStyles(theme)}
        </style>
    """.trimIndent())

        var currentY = 0
        cards.forEachIndexed { index, card ->
            val cardHeight = cardHeights[index]
            svgBuilder.append(generateCardSvg(card, 0, currentY, cardWidth, cardHeight, index, theme))
            currentY += cardHeight + gap
        }

        svgBuilder.append(generateJavaScript())
        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }
    private fun createRowLayout(cards: List<FeatureCard>, theme: CardTheme): String {
        val cardWidth = 280
        val cardHeight = 400
        val gap = 20
        val totalWidth = cards.size * cardWidth + (cards.size - 1) * gap
        val totalHeight = cardHeight

        val svgBuilder = StringBuilder()
        svgBuilder.append("""
        <?xml version="1.0" encoding="UTF-8"?>
        <svg height="$totalHeight" width="$totalWidth" viewBox="0 0 $totalWidth $totalHeight" xmlns="http://www.w3.org/2000/svg">
        <defs>
            ${generateGradients(cards, theme)}
        </defs>
        <style>
            ${generateStyles(theme)}
        </style>
    """.trimIndent())

        cards.forEachIndexed { index, card ->
            val x = index * (cardWidth + gap)
            svgBuilder.append(generateCardSvg(card, x, 0, cardWidth, cardHeight, index, theme))
        }

        svgBuilder.append(generateJavaScript())
        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }

    private fun generateJavaScript(): String {
        return """
        <script type="text/javascript">
        <![CDATA[
            document.addEventListener('DOMContentLoaded', function() {
                /* Add click handlers to all feature cards */
                const cards = document.querySelectorAll('.feature-card');
                
                cards.forEach(function(card) {
                    let isExpanded = false;
                    
                    card.addEventListener('click', function() {
                        const details = card.querySelector('.card-details');
                        const description = card.querySelector('.card-description');
                        const emoji = card.querySelector('text[dominant-baseline="middle"]');
                        
                        if (!isExpanded) {
                            /* Hide description and show details */
                            if (description) {
                                description.style.opacity = '0';
                                description.style.transition = 'opacity 0.2s ease';
                            }
                            
                            /* Show details after description fades out */
                            setTimeout(function() {
                                details.style.opacity = '1';
                                details.style.transform = 'translateY(0)';
                            }, 200);
                            
                            /* Animate emoji */
                            if (emoji) {
                                emoji.style.transform = 'translateY(-10px)';
                                emoji.style.transition = 'transform 0.3s ease';
                            }
                            
                            /* Add expanded class for styling */
                            card.classList.add('expanded');
                            
                            isExpanded = true;
                        } else {
                            /* Show description and hide details */
                            details.style.opacity = '0';
                            details.style.transform = 'translateY(20px)';
                            
                            /* Show description after details fade out */
                            setTimeout(function() {
                                if (description) {
                                    description.style.opacity = '1';
                                }
                            }, 200);
                            
                            /* Reset emoji position */
                            if (emoji) {
                                emoji.style.transform = 'translateY(0)';
                            }
                            
                            /* Remove expanded class */
                            card.classList.remove('expanded');
                            
                            isExpanded = false;
                        }
                    });
                    
                    /* Add hover effects programmatically if needed */
                    card.addEventListener('mouseenter', function() {
                        if (!card.classList.contains('expanded')) {
                            card.style.filter = 'brightness(1.05) drop-shadow(0 6px 20px rgba(0, 0, 0, 0.15))';
                        }
                    });
                    
                    card.addEventListener('mouseleave', function() {
                        if (!card.classList.contains('expanded')) {
                            card.style.filter = '';
                        }
                    });
                });
            });
        ]]>
        </script>
    """.trimIndent()
    }

    // Helper function to create wrapped text in SVG
    private fun createWrappedTextSvg(
        text: String,
        x: Int,
        y: Int,
        maxWidth: Int,
        fontSize: Int,
        textColor: String,
        textAnchor: String = "start",
        lineHeight: Int = fontSize + 4
    ): String {
        // Approximate characters that fit in maxWidth (rough estimate)
        val avgCharWidth = fontSize * 0.6
        val charsPerLine = (maxWidth / avgCharWidth).toInt()

        // Split text into words
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 <= charsPerLine) {
                // Word fits on current line
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            } else {
                // Word doesn't fit, start a new line
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder()
                }

                // If the word is too long for a single line, split it
                if (word.length > charsPerLine) {
                    var remainingWord = word
                    while (remainingWord.length > charsPerLine) {
                        val splitPoint = charsPerLine.coerceAtMost(remainingWord.length)
                        lines.add(remainingWord.substring(0, splitPoint))
                        remainingWord = remainingWord.substring(splitPoint)
                    }
                    currentLine.append(remainingWord)
                } else {
                    currentLine.append(word)
                }
            }
        }

        // Add the last line if not empty
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        // Create SVG text with tspan elements for each line
        val svgBuilder = StringBuilder()
        svgBuilder.append("""<text x="$x" y="$y" text-anchor="$textAnchor" font-size="$fontSize" fill="$textColor">""")

        lines.forEachIndexed { index, line ->
            val dy = if (index == 0) "0" else "$lineHeight"
            svgBuilder.append("""<tspan x="$x" dy="$dy">${escapeXml(line)}</tspan>""")
        }

        svgBuilder.append("</text>")
        return svgBuilder.toString()
    }

    private fun generateCardSvg(card: FeatureCard, x: Int, y: Int, width: Int, height: Int, index: Int, theme: CardTheme): String {
        val colors = getColorScheme(card.colorScheme, theme)

        return """
        <g transform="translate($x, $y)" class="feature-card" data-card="$index">
            <!-- Card background -->
            <rect class="card-background" x="0" y="0" width="$width" height="$height" rx="20" ry="20" 
                  fill="url(#cardGradient${index})" stroke="${colors.border}" stroke-width="1"/>
            
            <!-- Emoji circle background -->
            <path d="M 60 0 L 220 0 Q 220 60 140 60 Q 60 60 60 0 Z" 
                  fill="url(#emojiCircle${index})"/>
            
            <!-- Emoji -->
            <text x="140" y="40" text-anchor="middle" font-size="32" dominant-baseline="middle">${card.emoji}</text>
            
            <!-- Title -->
            <text x="140" y="120" text-anchor="middle" font-size="24" font-weight="bold" fill="${colors.title}">${escapeXml(card.title)}</text>
            
            <!-- Description (will be hidden when expanded) -->
            <g class="card-description">
                ${generateDescriptionText(card.description, colors.text)}
            </g>
            
            <!-- Details (initially hidden) -->
            <g class="card-details" style="opacity: 0; transform: translateY(20px);">
                ${generateDetailsText(card.details, colors.text)}
            </g>
        </g>
    """.trimIndent()
    }

    private fun generateDescriptionText(description: String, textColor: String): String {
        val maxCharsPerLine = 32 // Slightly longer for description
        val wrappedLines = if (description.length > maxCharsPerLine) {
            wrapText(description, maxCharsPerLine)
        } else {
            listOf(description)
        }

        return wrappedLines.mapIndexed { index, line ->
            val y = 160 + index * 18 // Start at Y=160 with 18px line spacing
            """<text x="140" y="$y" text-anchor="middle" font-size="16" fill="$textColor" font-family="system-ui, -apple-system, sans-serif">${escapeXml(line.trim())}</text>"""
        }.joinToString("\n")
    }

    private fun generateDetailsText(details: List<String>, textColor: String): String {
        if (details.isEmpty()) return ""

        var currentY = 190 // Start a bit lower to account for wrapped descriptions
        val items = mutableListOf<String>()

        details.forEach { detail ->
            val bulletY = currentY - 2 // Slightly offset bullet vertically

            // Create bullet point
            val bullet = """<circle cx="25" cy="$bulletY" r="3" fill="$textColor" opacity="0.7"/>"""

            // Handle long text by wrapping at reasonable length
            val maxCharsPerLine = 28
            val wrappedText = if (detail.length > maxCharsPerLine) {
                wrapText(detail, maxCharsPerLine)
            } else {
                listOf(detail)
            }

            // Generate text lines
            val textLines = wrappedText.mapIndexed { lineIndex, line ->
                val lineY = currentY + lineIndex * 16 // Line spacing within wrapped text
                val xOffset = 35 // Consistent indentation for all lines
                """<text x="$xOffset" y="$lineY" font-size="13" fill="$textColor" font-family="system-ui, -apple-system, sans-serif">${escapeXml(line.trim())}</text>"""
            }.joinToString("\n")

            items.add(bullet + "\n" + textLines)

            // Move Y position for next bullet item
            // Base spacing + extra spacing for wrapped lines
            currentY += 25 + (wrappedText.size - 1) * 16 + 10 // Added 10px extra spacing between bullets
        }

        return items.joinToString("\n")
    }


    private fun wrapText(text: String, maxChars: Int): List<String> {
        if (text.length <= maxChars) return listOf(text)

        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            // If adding this word would exceed the limit
            if (currentLine.length + word.length + 1 > maxChars) {
                // If current line is not empty, add it to lines
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder()
                }

                // If the word itself is longer than maxChars, split it
                if (word.length > maxChars) {
                    var remainingWord = word
                    while (remainingWord.length > maxChars) {
                        lines.add(remainingWord.substring(0, maxChars))
                        remainingWord = remainingWord.substring(maxChars)
                    }
                    if (remainingWord.isNotEmpty()) {
                        currentLine.append(remainingWord)
                    }
                } else {
                    currentLine.append(word)
                }
            } else {
                // Add word to current line
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            }
        }

        // Add the last line if it's not empty
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }

    private fun calculateCardHeight(card: FeatureCard): Int {
        val baseHeight = 400
        if (card.details.isEmpty()) return baseHeight

        // Calculate additional height needed for details
        val maxCharsPerLine = 28
        val totalLines = card.details.sumOf { detail ->
            if (detail.length > maxCharsPerLine) {
                wrapText(detail, maxCharsPerLine).size
            } else {
                1
            }
        }

        // Each detail item needs 25px base + 16px per additional line
        val detailsHeight = card.details.size * 25 + (totalLines - card.details.size) * 16

        return baseHeight + minOf(detailsHeight, 150) // Cap the additional height
    }

    private fun generateGradients(cards: List<FeatureCard>, theme: CardTheme): String {
        return cards.mapIndexed { index, card ->
            val colors = getColorScheme(card.colorScheme, theme)
            """
                <linearGradient id="cardGradient$index" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:${colors.gradientStart}"/>
                    <stop offset="100%" style="stop-color:${colors.gradientEnd}"/>
                </linearGradient>
                <radialGradient id="emojiCircle$index" cx="50%" cy="50%" r="50%">
                    <stop offset="0%" style="stop-color:${colors.emojiStart}"/>
                    <stop offset="100%" style="stop-color:${colors.emojiEnd}"/>
                </radialGradient>
            """.trimIndent()
        }.joinToString("\n")
    }

    private fun generateStyles(theme: CardTheme): String {
        return """
        .feature-card {
            cursor: pointer;
            transition: filter 0.2s ease, opacity 0.2s ease;
        }
        .feature-card:hover:not(.expanded) {
            filter: brightness(1.05) drop-shadow(0 6px 20px rgba(0, 0, 0, 0.15));
        }
        .feature-card.expanded {
            filter: brightness(1.08) drop-shadow(0 8px 25px rgba(0, 0, 0, 0.2));
        }
        .feature-card:hover .card-background {
            opacity: 0.95;
        }
        .card-details {
            transition: opacity 0.3s ease, transform 0.3s ease;
        }
        .card-description {
            transition: opacity 0.2s ease;
        }
        .card-background {
            transition: opacity 0.2s ease;
        }
        ${if (theme == CardTheme.DARK) """
        .feature-card:hover:not(.expanded) {
            filter: brightness(1.1) drop-shadow(0 6px 20px rgba(0, 0, 0, 0.3));
        }
        .feature-card.expanded {
            filter: brightness(1.15) drop-shadow(0 8px 25px rgba(0, 0, 0, 0.4));
        }
        """ else ""}
    """.trimIndent()
    }

    private fun getColorScheme(scheme: ColorScheme, theme: CardTheme): CardColors {
        return when (theme) {
            CardTheme.LIGHT -> getLightColorScheme(scheme)
            CardTheme.DARK -> getDarkColorScheme(scheme)
            CardTheme.AUTO -> getLightColorScheme(scheme) // Default to light, can be enhanced
        }
    }

    private fun getLightColorScheme(scheme: ColorScheme): CardColors {
        return when (scheme) {
            ColorScheme.BLUE -> CardColors(
                gradientStart = "#E3F2FD", gradientEnd = "#BBDEFB",
                emojiStart = "#81C784", emojiEnd = "#4CAF50",
                title = "#1976D2", text = "#424242", border = "#E1E1E1"
            )
            ColorScheme.GREEN -> CardColors(
                gradientStart = "#E8F5E8", gradientEnd = "#C8E6C9",
                emojiStart = "#66BB6A", emojiEnd = "#4CAF50",
                title = "#388E3C", text = "#424242", border = "#E1E1E1"
            )
            ColorScheme.ORANGE -> CardColors(
                gradientStart = "#FFF3E0", gradientEnd = "#FFCC80",
                emojiStart = "#FFB74D", emojiEnd = "#FF9800",
                title = "#E65100", text = "#424242", border = "#E1E1E1"
            )
            ColorScheme.PURPLE -> CardColors(
                gradientStart = "#F3E5F5", gradientEnd = "#CE93D8",
                emojiStart = "#BA68C8", emojiEnd = "#9C27B0",
                title = "#7B1FA2", text = "#424242", border = "#E1E1E1"
            )
            ColorScheme.RED -> CardColors(
                gradientStart = "#FFEBEE", gradientEnd = "#FFCDD2",
                emojiStart = "#EF5350", emojiEnd = "#F44336",
                title = "#D32F2F", text = "#424242", border = "#E1E1E1"
            )
            ColorScheme.TEAL -> CardColors(
                gradientStart = "#E0F2F1", gradientEnd = "#B2DFDB",
                emojiStart = "#4DB6AC", emojiEnd = "#009688",
                title = "#00695C", text = "#424242", border = "#E1E1E1"
            )
            ColorScheme.GRAY -> CardColors(
                gradientStart = "#FAFAFA", gradientEnd = "#E0E0E0",
                emojiStart = "#90A4AE", emojiEnd = "#607D8B",
                title = "#455A64", text = "#424242", border = "#E1E1E1"
            )
        }
    }

    private fun getDarkColorScheme(scheme: ColorScheme): CardColors {
        return when (scheme) {
            ColorScheme.BLUE -> CardColors(
                gradientStart = "#2C3E50", gradientEnd = "#34495E",
                emojiStart = "#388E3C", emojiEnd = "#2E7D32",
                title = "#64B5F6", text = "#BDBDBD", border = "#444"
            )
            ColorScheme.GREEN -> CardColors(
                gradientStart = "#1B5E20", gradientEnd = "#2E7D32",
                emojiStart = "#4CAF50", emojiEnd = "#388E3C",
                title = "#81C784", text = "#BDBDBD", border = "#444"
            )
            ColorScheme.ORANGE -> CardColors(
                gradientStart = "#8D4E03", gradientEnd = "#A0522D",
                emojiStart = "#F57C00", emojiEnd = "#E65100",
                title = "#FFB74D", text = "#BDBDBD", border = "#444"
            )
            ColorScheme.PURPLE -> CardColors(
                gradientStart = "#4A148C", gradientEnd = "#6A1B9A",
                emojiStart = "#8E24AA", emojiEnd = "#7B1FA2",
                title = "#BA68C8", text = "#BDBDBD", border = "#444"
            )
            ColorScheme.RED -> CardColors(
                gradientStart = "#B71C1C", gradientEnd = "#C62828",
                emojiStart = "#E53935", emojiEnd = "#D32F2F",
                title = "#EF5350", text = "#BDBDBD", border = "#444"
            )
            ColorScheme.TEAL -> CardColors(
                gradientStart = "#004D40", gradientEnd = "#00695C",
                emojiStart = "#00796B", emojiEnd = "#00695C",
                title = "#4DB6AC", text = "#BDBDBD", border = "#444"
            )
            ColorScheme.GRAY -> CardColors(
                gradientStart = "#263238", gradientEnd = "#37474F",
                emojiStart = "#546E7A", emojiEnd = "#455A64",
                title = "#90A4AE", text = "#BDBDBD", border = "#444"
            )
        }
    }

    private fun createEmptyCardsSvg(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg viewBox="0 0 400 200" xmlns="http://www.w3.org/2000/svg">
                <rect x="0" y="0" width="400" height="200" rx="10" fill="#f8f9fa" stroke="#dee2e6"/>
                <text x="200" y="100" text-anchor="middle" font-size="18" fill="#6c757d">No cards to display</text>
            </svg>
        """.trimIndent()
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}

data class CardColors(
    val gradientStart: String,
    val gradientEnd: String,
    val emojiStart: String,
    val emojiEnd: String,
    val title: String,
    val text: String,
    val border: String
)
