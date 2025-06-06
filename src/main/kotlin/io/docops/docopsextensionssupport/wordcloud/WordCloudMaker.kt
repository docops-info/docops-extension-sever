package io.docops.docopsextensionssupport.wordcloud

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.support.gradientFromColor
import org.apache.catalina.manager.JspHelper.escapeXml
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class WordCloudMaker {

    fun makeWordCloud(wordCloud: WordCloud): String {
        val sb = StringBuilder()
        
        // Create SVG header
        sb.append(makeHeader(wordCloud))
        
        // Add definitions (gradients, filters, styles)
        sb.append(makeDefs(wordCloud))
        
        // Add background
        sb.append(makeBackground(wordCloud))
        
        // Add title
        sb.append(addTitle(wordCloud))
        
        // Add words
        sb.append(addWords(wordCloud))
        
        // Close SVG
        sb.append("</svg>")
        
        return sb.toString()
    }
    
    private fun makeHeader(wordCloud: WordCloud): String {
        val width = wordCloud.calcWidth()
        val height = wordCloud.calcHeight()
        
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg id="id_${wordCloud.display.id}" width="$width" height="$height" viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg" aria-label='Docops: WordCloud' preserveAspectRatio='xMidYMid meet'>
        """.trimIndent()
    }
    
    private fun makeDefs(wordCloud: WordCloud): String {
        val baseColor = wordCloud.display.baseColor
        val backColor = SVGColor(baseColor, "backGrad_${wordCloud.display.id}")
        
        // Create gradients for each word
        val wordGradients = StringBuilder()
        wordCloud.words.forEach { word ->
            val color = word.itemDisplay?.baseColor ?: wordCloud.display.baseColor
            val gradientId = "wordGrad_${word.id}"
            
            wordGradients.append("""
                <linearGradient id="$gradientId" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="${lightenColor(color, 0.3)}"/>
                    <stop offset="50%" stop-color="$color"/>
                    <stop offset="100%" stop-color="${darkenColor(color, 0.3)}"/>
                </linearGradient>
            """.trimIndent())
        }
        
        return """
            <defs>
                ${backColor.linearGradient}
                $wordGradients
                
                <!-- Drop shadow filter -->
                <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="1" result="blur"/>
                    <feOffset in="blur" dx="1" dy="1" result="offsetBlur"/>
                    <feComponentTransfer in="offsetBlur" result="shadow">
                        <feFuncA type="linear" slope="0.3"/>
                    </feComponentTransfer>
                    <feMerge>
                        <feMergeNode in="shadow"/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>
                
                <!-- Hover effect -->
                <style>
                    #id_${wordCloud.display.id} .word {
                        transition: all 0.3s ease;
                    }
                    #id_${wordCloud.display.id} .word:hover {
                        transform: scale(1.1);
                        filter: brightness(1.2);
                        cursor: pointer;
                    }
                </style>
            </defs>
        """.trimIndent()
    }
    
    private fun makeBackground(wordCloud: WordCloud): String {
        val width = wordCloud.calcWidth()
        val height = wordCloud.calcHeight()
        val backgroundColor = if (wordCloud.display.useDark) "#1f2937" else "#f8f9fa"
        
        return """
            <rect width="100%" height="100%" fill="$backgroundColor" rx="15" ry="15"/>
        """.trimIndent()
    }
    
    private fun addTitle(wordCloud: WordCloud): String {
        val centerX = wordCloud.centerX()
        val titleBgColor = if (wordCloud.display.useDark) "#374151" else "#f0f0f0"
        val titleTextColor = if (wordCloud.display.useDark) "#f9fafb" else "#333"
        
        return """
            <g>
                <rect x="${centerX - 150}" y="10" width="300" height="40" rx="10" ry="10" fill="$titleBgColor" opacity="0.7"/>
                <text x="$centerX" y="38" style="font-family: Arial, sans-serif; fill: $titleTextColor; text-anchor: middle; font-size: 24px; font-weight: bold; filter: drop-shadow(2px 2px 1px rgba(0, 0, 0, .2));">${wordCloud.title}</text>
            </g>
        """.trimIndent()
    }
    
    private fun addWords(wordCloud: WordCloud): String {
        val sb = StringBuilder()
        val centerX = wordCloud.centerX()
        val centerY = wordCloud.centerY()
        
        // Sort words by weight (descending) to place larger words first
        val sortedWords = wordCloud.words.sortedByDescending { it.weight }
        
        // Simple spiral placement algorithm
        val placedWords = mutableListOf<PlacedWord>()
        val random = Random(42) // Fixed seed for reproducibility
        
        sortedWords.forEach { word ->
            val fontSize = wordCloud.calcFontSize(word)
            val textColor = determineTextColor(word.itemDisplay?.baseColor ?: wordCloud.display.baseColor)
            val gradientId = "wordGrad_${word.id}"
            
            // Estimate word dimensions (rough approximation)
            val wordWidth = word.text.length * fontSize * 0.6
            val wordHeight = fontSize * 1.2
            
            // Try to find a position for the word
            var angle = 0.0
            var radius = 10.0
            var placed = false
            var attempts = 0
            var x = 0.0
            var y = 0.0
            
            while (!placed && attempts < 200) {
                // Calculate position on spiral
                x = centerX + radius * cos(angle)
                y = centerY + radius * sin(angle)
                
                // Check if this position overlaps with any placed word
                val newWord = PlacedWord(x, y, wordWidth, wordHeight)
                val overlaps = placedWords.any { it.overlaps(newWord) }
                
                if (!overlaps && 
                    x - wordWidth/2 > 0 && 
                    x + wordWidth/2 < wordCloud.calcWidth() && 
                    y - wordHeight/2 > 50 && // Leave space for title
                    y + wordHeight/2 < wordCloud.calcHeight()) {
                    placed = true
                    placedWords.add(newWord)
                } else {
                    // Move along spiral
                    angle += 0.5
                    radius += 0.5
                    attempts++
                }
            }
            
            if (placed) {
                // Random rotation between -30 and 30 degrees for visual interest
                val rotation = if (random.nextBoolean()) random.nextDouble(-30.0, 30.0) else 0.0
                
                sb.append("""
                    <g class="word" transform="translate($x, $y) rotate($rotation)">
                        <text text-anchor="middle" dominant-baseline="middle" 
                              font-family="Arial, sans-serif" font-size="${fontSize}px" font-weight="bold"
                              fill="url(#$gradientId)" filter="url(#dropShadow)">
                            ${escapeXml(word.text)}
                        </text>
                    </g>
                """.trimIndent())
            }
        }
        
        return sb.toString()
    }
    
    // Helper class for word placement
    private data class PlacedWord(val x: Double, val y: Double, val width: Double, val height: Double) {
        fun overlaps(other: PlacedWord): Boolean {
            return !(x + width/2 < other.x - other.width/2 ||
                    x - width/2 > other.x + other.width/2 ||
                    y + height/2 < other.y - other.height/2 ||
                    y - height/2 > other.y + other.height/2)
        }
    }
    
    // Helper function to lighten a color
    private fun lightenColor(hexColor: String, factor: Double): String {
        return adjustColor(hexColor, factor, true)
    }
    
    // Helper function to darken a color
    private fun darkenColor(hexColor: String, factor: Double): String {
        return adjustColor(hexColor, factor, false)
    }
    
    // Helper function to adjust a color's brightness
    private fun adjustColor(hexColor: String, factor: Double, brighten: Boolean): String {
        val hex = hexColor.replace("#", "")
        val r = Integer.parseInt(hex.substring(0, 2), 16)
        val g = Integer.parseInt(hex.substring(2, 4), 16)
        val b = Integer.parseInt(hex.substring(4, 6), 16)
        
        val adjustment = if (brighten) factor else -factor
        
        val newR = (r + (255 - r) * adjustment).toInt().coerceIn(0, 255)
        val newG = (g + (255 - g) * adjustment).toInt().coerceIn(0, 255)
        val newB = (b + (255 - b) * adjustment).toInt().coerceIn(0, 255)
        
        return String.format("#%02x%02x%02x", newR, newG, newB)
    }
}