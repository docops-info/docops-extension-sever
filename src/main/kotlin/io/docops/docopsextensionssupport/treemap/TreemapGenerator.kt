package io.docops.docopsextensionssupport.chart.treemap


import io.docops.docopsextensionssupport.chart.ColorPaletteFactory
import io.docops.docopsextensionssupport.chart.ColorPaletteFactory.getColorCyclic
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.determineTextColor
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Generates SVG treemap visualizations with multiple theme support
 */
class TreemapGenerator(private val useDark: Boolean = false) {

    private val svgColor = SVGColor("#fcfcfc")
    @OptIn(ExperimentalUuidApi::class)
    private val guid = Uuid.random().toHexString()

    fun generate(treemap: Treemap): String {
        val theme = ThemeFactory.getTheme(useDark)
        val paletteType = getPaletteType(treemap.display)

        return when (treemap.display.theme.lowercase()) {
            "brutalist" -> generateBrutalistTheme(treemap, paletteType)
            "neon" -> generateNeonTheme(treemap, paletteType)
            "glassmorphic" -> generateGlassmorphicTheme(treemap, paletteType)
            else -> generateModernTheme(treemap, paletteType)
        }
    }

    private fun generateModernTheme(treemap: Treemap, paletteType: ColorPaletteFactory.PaletteType): String {
        val theme = ThemeFactory.getTheme(useDark)
        val fontFamily = treemap.display.fontFamily.ifBlank { "'Bricolage Grotesque', -apple-system, sans-serif" }

        val sb = StringBuilder()
        val rects = calculateTreemapLayout(treemap)

        sb.append("""
            <svg width="${treemap.display.width}" height="${treemap.display.height}" 
                 viewBox="0 0 ${treemap.display.width} ${treemap.display.height}" 
                 xmlns="http://www.w3.org/2000/svg" id="ID_${guid}">
                <defs>
                    <style type="text/css">
                        @import url('https://fonts.googleapis.com/css2?family=Bricolage+Grotesque:wght@400;700&amp;family=JetBrains+Mono:wght@500&amp;display=swap');
                        
                        #ID_${guid} .treemap-rect {
                            stroke: ${theme.canvas};
                            stroke-width: 3;
                            opacity: 0;
                            animation: slideIn 0.7s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
                        }
                        
                        #ID_${guid} .treemap-rect:hover {
                            filter: brightness(1.2);
                            cursor: pointer;
                        }
                        
                        #ID_${guid} .label-category {
                            font-family: $fontFamily;
                            font-weight: 700;
                            pointer-events: none;
                            text-transform: uppercase;
                            letter-spacing: -0.02em;
                        }
                        
                        #ID_${guid} .label-value {
                            font-family: 'JetBrains Mono', monospace;
                            font-weight: 500;
                            font-size: 13px;
                            pointer-events: none;
                        }
                        
                        #ID_${guid} .label-metric {
                            font-family: 'JetBrains Mono', monospace;
                            font-size: 32px;
                            font-weight: 700;
                            pointer-events: none;
                        }
                        
                        #ID_${guid} .title-main {
                            font-family: $fontFamily;
                            font-weight: 700;
                            font-size: 42px;
                            fill: ${theme.primaryText};
                            letter-spacing: -0.03em;
                        }
                        
                        #ID_${guid} .title-sub {
                            font-family: 'JetBrains Mono', monospace;
                            font-size: 14px;
                            fill: ${theme.secondaryText};
                            letter-spacing: 0.05em;
                        }
                        
                        @keyframes slideIn {
                            from { opacity: 0; transform: scale(0.95); }
                            to { opacity: 1; transform: scale(1); }
                        }
                    </style>
        """.trimIndent())

        val tileColors = treemap.items.mapIndexed { index, item ->
            item.color?.takeIf { it.isNotBlank() } ?: getColorCyclic(paletteType, index) ?: "#64748b"
        }

        // Generate gradients
        tileColors.forEachIndexed { index, color ->
            sb.append("""
                <linearGradient id="grad-${index}" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:${color};stop-opacity:1" />
                    <stop offset="100%" style="stop-color:${svgColor.darkenColor(color, 0.15)};stop-opacity:1" />
                </linearGradient>
            """.trimIndent())
        }

        // Texture pattern
        sb.append("""
            <pattern id="dots" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                <circle cx="2" cy="2" r="1" fill="rgba(255,255,255,0.08)"/>
            </pattern>
        """.trimIndent())

        sb.append("</defs>")

        // Background
        val bgColor = if (useDark) "#0a0e1a" else "#f8fafc"
        val glowColor1 = if (useDark) "#9775fa" else "#818cf8"
        val glowColor2 = if (useDark) "#4dabf7" else "#60a5fa"

        sb.append("""
            <rect width="100%" height="100%" fill="$bgColor"/>
            <circle cx="${treemap.display.width * 0.75}" cy="${treemap.display.height * 0.25}" 
                    r="400" fill="$glowColor1" opacity="0.08" filter="blur(80px)"/>
            <circle cx="${treemap.display.width * 0.25}" cy="${treemap.display.height * 0.75}" 
                    r="350" fill="$glowColor2" opacity="0.06" filter="blur(100px)"/>
        """.trimIndent())

        // Title section
        sb.append("""
            <g transform="translate(60, 70)">
                <text class="title-main">${escapeXml(treemap.title.uppercase())}</text>
                <text class="title-sub" y="30">${escapeXml(treemap.subtitle.uppercase())}</text>
                <line x1="0" y1="45" x2="180" y2="45" stroke="${getColorCyclic(paletteType, 0)}" 
                      stroke-width="3" stroke-linecap="round"/>
            </g>
        """.trimIndent())

        // Treemap rectangles
        val total = treemap.items.sumOf { it.value }
        rects.forEachIndexed { index, rect ->
            val item = treemap.items[index]
            val tileColor = tileColors[index]
            val textColor = determineTextColor(tileColor)
            val percentage = (item.value / total * 100)
            val delay = treemap.display.animationDelay * index
            val tooltip = buildTooltip(item, percentage)

            // Determine if we have enough space for text
            val hasSpaceForLabel = rect.width > 80 && rect.height > 30
            val hasSpaceForDescription = rect.width > 120 && rect.height > 50
            val hasSpaceForMetric = rect.width > 100 && rect.height > 120
            val hasSpaceForPercentage = rect.width > 100 && rect.height > 100

            sb.append("""
                <g>
                    <title>${escapeForTitle(tooltip)}</title>
                    <rect class="treemap-rect" 
                          x="${rect.x}" y="${rect.y}" width="${rect.width}" height="${rect.height}" 
                          rx="8" fill="url(#grad-${index})" style="animation-delay: ${delay}s;"/>
                    <rect x="${rect.x}" y="${rect.y}" width="${rect.width}" height="${rect.height}" 
                          rx="8" fill="url(#dots)" pointer-events="none"/>
            """.trimIndent())

            if (hasSpaceForLabel) {
                val minSide = minOf(rect.width, rect.height)
                val padding = when {
                    minSide > 260 -> 28.0
                    minSide > 200 -> 24.0
                    minSide > 140 -> 18.0
                    else -> 12.0
                }
                val labelFontSize = when {
                    rect.width > 300 -> 18
                    rect.width > 200 -> 16
                    rect.width > 120 -> 14
                    else -> 12
                }
                val availableWidth = rect.width - (padding * 2)
                val labelFittedSize = fitFontSize(item.label, availableWidth, labelFontSize, 10)
                val labelLineHeight = labelFittedSize + 2
                val labelLines = wrapText(item.label, availableWidth, labelFittedSize, 2)
                val labelY = padding + labelFittedSize
                val descriptionY = labelY + ((labelLines.size - 1).coerceAtLeast(0) * labelLineHeight) + 16

                sb.append("""
                    <g transform="translate(${rect.x}, ${rect.y})">
                        <text class="label-category" x="$padding" y="$labelY" font-size="$labelFittedSize" fill="$textColor">${buildWrappedText(labelLines, labelLineHeight, padding)}</text>
                """.trimIndent())

                if (hasSpaceForDescription && item.description.isNotBlank()) {
                    val descriptionBaseSize = 13
                    val descriptionFittedSize = fitFontSize(item.description, availableWidth, descriptionBaseSize, 9)
                    val descriptionLineHeight = descriptionFittedSize + 2
                    val descriptionLines = wrapText(item.description, availableWidth, descriptionFittedSize, 2)
                    sb.append("""
                        <text class="label-value" x="$padding" y="$descriptionY" font-size="$descriptionFittedSize" fill="${svgColor.adjustOpacity(textColor, 0.7)}">${buildWrappedText(descriptionLines, descriptionLineHeight, padding)}</text>
                    """.trimIndent())
                }

                if (hasSpaceForMetric) {
                    val metricText = item.metric.ifBlank { formatValue(item.value) }
                    val metricFontSize = when {
                        rect.width > 300 -> 32
                        rect.width > 200 -> 28
                        rect.width > 150 -> 24
                        else -> 20
                    }
                    val metricFittedSize = fitFontSize(metricText, availableWidth, metricFontSize, 12)
                    val percentFontSize = when {
                        rect.width > 200 -> 13
                        rect.width > 120 -> 11
                        else -> 10
                    }
                    val percentY = rect.height - padding
                    val metricY = if (hasSpaceForPercentage) percentY - (percentFontSize + 14) else rect.height - padding
                    sb.append("""
                        <text class="label-metric" x="$padding" y="$metricY" font-size="$metricFittedSize" fill="$textColor">${escapeXml(metricText)}</text>
                    """.trimIndent())
                }

                if (hasSpaceForPercentage) {
                    val percentFontSize = when {
                        rect.width > 200 -> 13
                        rect.width > 120 -> 11
                        else -> 10
                    }
                    val percentLabel = formatDecimal(percentage, 1)
                    val percentText = "$percentLabel% of total"
                    val percentFittedSize = fitFontSize(percentText, availableWidth, percentFontSize, 9)
                    sb.append("""
                        <text class="label-value" x="$padding" y="${rect.height - padding}" font-size="$percentFittedSize" fill="${svgColor.adjustOpacity(textColor, 0.7)}">$percentText</text>
                    """.trimIndent())
                }

                sb.append("</g>")
            }

            sb.append("</g>")
        }

        // Footer annotation
        sb.append("""
            <g transform="translate(60, ${treemap.display.height - 60})">
                <text font-family="JetBrains Mono, monospace" font-size="11" fill="${theme.secondaryText}">
                    Updated: ${getCurrentDate()} • Hover for details
                </text>
            </g>
        """.trimIndent())

        sb.append("</svg>")
        return sb.toString()
    }

    private fun generateBrutalistTheme(treemap: Treemap, paletteType: ColorPaletteFactory.PaletteType): String {
        val theme = ThemeFactory.getTheme(useDark)
        val sb = StringBuilder()
        val rects = calculateTreemapLayout(treemap)

        sb.append("""
            <svg width="${treemap.display.width}" height="${treemap.display.height}" 
                 viewBox="0 0 ${treemap.display.width} ${treemap.display.height}" 
                 xmlns="http://www.w3.org/2000/svg">
                <defs>
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@700&amp;family=Roboto+Mono:wght@700&amp;display=swap');
                        .brut-rect { stroke: ${theme.canvas}; stroke-width: 4; transition: all 0.2s; }
                        .brut-rect:hover { transform: scale(1.02); filter: drop-shadow(0 8px 16px rgba(0,0,0,0.3)); }
                        .brut-title { font-family: 'Space Grotesk', sans-serif; font-weight: 700; text-transform: uppercase; letter-spacing: -0.02em; }
                        .brut-label { font-family: 'Roboto Mono', monospace; font-weight: 700; }
                    </style>
                </defs>
                
                <rect width="100%" height="100%" fill="${theme.canvas}"/>
                
                <text class="brut-title" x="60" y="80" font-size="48" fill="${theme.primaryText}">${escapeXml(treemap.title)}</text>
                <rect x="60" y="90" width="200" height="6" fill="${theme.accentColor}"/>
        """.trimIndent())

        val total = treemap.items.sumOf { it.value }
        rects.forEachIndexed { index, rect ->
            val item = treemap.items[index]
            val color = getColorCyclic(paletteType, index)
            val percentage = (item.value / total * 100)
            val tooltip = buildTooltip(item, percentage)

            sb.append("""
                <g>
                    <title>${escapeForTitle(tooltip)}</title>
                    <rect class="brut-rect" x="${rect.x}" y="${rect.y}" width="${rect.width}" height="${rect.height}" 
                          fill="$color" rx="0"/>
                </g>
                <text class="brut-label" x="${rect.x + 20}" y="${rect.y + 40}" font-size="20" fill="${theme.canvas}">${escapeXml(item.label)}</text>
                <text class="brut-label" x="${rect.x + 20}" y="${rect.y + rect.height - 40}" font-size="36" fill="${theme.canvas}">${formatValue(item.value)}</text>
                <text class="brut-label" x="${rect.x + 20}" y="${rect.y + rect.height - 20}" font-size="14" fill="${theme.canvas}" opacity="0.8">${formatDecimal(percentage, 1)}%</text>
            """.trimIndent())
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun generateNeonTheme(treemap: Treemap, paletteType: ColorPaletteFactory.PaletteType): String {
        // Neon theme with glowing effects - abbreviated for brevity
        return generateModernTheme(treemap, paletteType) // Simplified, can be expanded
    }

    private fun generateGlassmorphicTheme(treemap: Treemap, paletteType: ColorPaletteFactory.PaletteType): String {
        // Glassmorphic theme with frosted glass effect - abbreviated for brevity
        return generateModernTheme(treemap, paletteType) // Simplified, can be expanded
    }

    /**
     * Calculate treemap layout using squarified algorithm
     */
    private fun calculateTreemapLayout(treemap: Treemap): List<Rectangle> {
        val contentX = 60.0
        val contentY = 140.0
        val contentWidth = treemap.display.width - 120.0
        val contentHeight = treemap.display.height - 220.0

        val total = treemap.items.sumOf { it.value }

        // Normalize values to area
        val normalizedItems = treemap.items.map { item ->
            Pair(item, (item.value / total) * (contentWidth * contentHeight))
        }.sortedByDescending { it.second }

        val rectangles = mutableListOf<Rectangle>()
        layoutTreemap(normalizedItems, contentX, contentY, contentWidth, contentHeight, rectangles)

        return applyGutter(rectangles, 8.0)
    }

    private fun layoutTreemap(
        items: List<Pair<TreemapItem, Double>>,
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        result: MutableList<Rectangle>
    ) {
        if (items.isEmpty()) return

        if (items.size == 1) {
            result.add(Rectangle(x, y, w, h))
            return
        }

        // Calculate total area
        val totalArea = items.sumOf { it.second }

        // Determine orientation based on aspect ratio
        val horizontal = w >= h

        // Split into two groups to minimize aspect ratio
        val splitPoint = findBestSplit(items, w, h, horizontal)

        val firstGroup = items.subList(0, splitPoint)
        val secondGroup = items.subList(splitPoint, items.size)

        val firstArea = firstGroup.sumOf { it.second }
        val firstRatio = firstArea / totalArea

        if (horizontal) {
            // Split horizontally
            val firstWidth = w * firstRatio
            layoutTreemap(firstGroup, x, y, firstWidth, h, result)
            layoutTreemap(secondGroup, x + firstWidth, y, w - firstWidth, h, result)
        } else {
            // Split vertically
            val firstHeight = h * firstRatio
            layoutTreemap(firstGroup, x, y, w, firstHeight, result)
            layoutTreemap(secondGroup, x, y + firstHeight, w, h - firstHeight, result)
        }
    }

    /**
     * Find best split point to minimize aspect ratio
     */
    private fun findBestSplit(items: List<Pair<TreemapItem, Double>>, w: Double, h: Double, horizontal: Boolean): Int {
        if (items.size <= 2) return 1

        val totalArea = items.sumOf { it.second }
        var bestSplit = 1
        var bestRatio = Double.MAX_VALUE

        for (i in 1 until items.size) {
            val firstArea = items.subList(0, i).sumOf { it.second }
            val ratio = firstArea / totalArea

            val aspectRatio = if (horizontal) {
                val firstW = w * ratio
                maxOf(firstW / h, h / firstW)
            } else {
                val firstH = h * ratio
                maxOf(w / firstH, firstH / w)
            }

            if (aspectRatio < bestRatio) {
                bestRatio = aspectRatio
                bestSplit = i
            }
        }

        return bestSplit
    }

    private fun applyGutter(rectangles: List<Rectangle>, gutter: Double): List<Rectangle> {
        if (gutter <= 0) return rectangles

        val inset = gutter / 2.0
        return rectangles.map { rect ->
            val newWidth = rect.width - gutter
            val newHeight = rect.height - gutter
            if (newWidth <= 0 || newHeight <= 0) {
                rect
            } else {
                Rectangle(rect.x + inset, rect.y + inset, newWidth, newHeight)
            }
        }
    }

    private fun buildTooltip(item: TreemapItem, percentage: Double): String {
        val lines = mutableListOf<String>()
        lines.add(item.label)
        if (item.description.isNotBlank()) {
            lines.add(item.description)
        }
        lines.add("Value: ${formatValue(item.value)}")
        lines.add("Share: ${formatDecimal(percentage, 1)}%")
        if (item.metric.isNotBlank()) {
            lines.add("Metric: ${item.metric}")
        }
        return lines.joinToString("\n")
    }

    private fun escapeForTitle(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
    private fun squarify(
        items: List<TreemapItem>,
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        total: Double,
        result: MutableList<Rectangle>
    ) {
        if (items.isEmpty()) return

        if (items.size == 1) {
            // Base case: single item
            result.add(Rectangle(x, y, w, h))
            return
        }

        // Determine if we should split horizontally or vertically
        val isHorizontal = w >= h

        // Calculate how many items to put in the first row
        val item = items.first()
        val itemArea = (item.value / total) * (w * h)

        if (isHorizontal) {
            // Split horizontally
            val itemWidth = itemArea / h
            result.add(Rectangle(x, y, itemWidth, h))

            // Recurse on remaining items
            if (items.size > 1) {
                squarify(
                    items.drop(1),
                    x + itemWidth,
                    y,
                    w - itemWidth,
                    h,
                    total,
                    result
                )
            }
        } else {
            // Split vertically
            val itemHeight = itemArea / w
            result.add(Rectangle(x, y, w, itemHeight))

            // Recurse on remaining items
            if (items.size > 1) {
                squarify(
                    items.drop(1),
                    x,
                    y + itemHeight,
                    w,
                    h - itemHeight,
                    total,
                    result
                )
            }
        }
    }

    private fun layoutRow(
        row: List<TreemapItem>,
        x: Double, y: Double, w: Double, h: Double,
        total: Double,
        result: MutableList<Rectangle>
    ) {
        val rowTotal = row.sumOf { it.value }
        val isHorizontal = w >= h
        var offset = 0.0

        row.forEach { item ->
            val size = item.value / total
            val rectW = if (isHorizontal) size * w / (rowTotal / total) else rowTotal / total * w
            val rectH = if (isHorizontal) rowTotal / total * h else size * h / (rowTotal / total)
            val rectX = if (isHorizontal) x + offset else x
            val rectY = if (isHorizontal) y else y + offset

            result.add(Rectangle(rectX, rectY, rectW, rectH))
            offset += if (isHorizontal) rectW else rectH
        }
    }

    private fun worst(row: List<TreemapItem>, w: Double, h: Double, total: Double): Double {
        if (row.isEmpty()) return Double.MAX_VALUE

        val rowTotal = row.sumOf { it.value }
        val rowMax = row.maxOf { it.value }
        val rowMin = row.minOf { it.value }

        val areaW = if (w >= h) rowTotal / total * w else w
        val areaH = if (w >= h) h else rowTotal / total * h

        return maxOf(
            areaW * areaW * rowMax / (rowTotal * rowTotal),
            rowTotal * rowTotal / (areaH * areaH * rowMin)
        )
    }

    private fun getPaletteType(display: TreemapDisplay): ColorPaletteFactory.PaletteType {
        return when {
            display.paletteType.isNotBlank() -> {
                try {
                    ColorPaletteFactory.PaletteType.valueOf(display.paletteType.uppercase())
                } catch (e: IllegalArgumentException) {
                    if (useDark) ColorPaletteFactory.PaletteType.URBAN_NIGHT
                    else ColorPaletteFactory.PaletteType.OCEAN_BREEZE
                }
            }
            else -> if (useDark) ColorPaletteFactory.PaletteType.URBAN_NIGHT
            else ColorPaletteFactory.PaletteType.OCEAN_BREEZE
        }
    }

    private fun formatValue(value: Double): String {
        return when {
            value >= 1_000_000 -> "$${formatDecimal(value / 1_000_000, 1)}M"
            value >= 1_000 -> "$${formatDecimal(value / 1_000, 0)}K"
            else -> "$${formatDecimal(value, 0)}"
        }
    }

    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString()
    }

    private fun formatDecimal(value: Double, decimals: Int): String {
        if (decimals <= 0) {
            return round(value).toLong().toString()
        }
        val scale = 10.0.pow(decimals)
        val rounded = round(value * scale).toLong()
        if (rounded == 0L) return "0.${"0".repeat(decimals)}"
        val sign = if (rounded < 0) "-" else ""
        val absRounded = abs(rounded)
        val scaleLong = scale.toLong()
        val intPart = absRounded / scaleLong
        val fracPart = (absRounded % scaleLong).toString().padStart(decimals, '0')
        return "$sign$intPart.$fracPart"
    }

    /**
     * Truncate text to fit within available width
     */
    private fun truncateText(text: String, availableWidth: Double, fontSize: Int): String {
        // Rough estimate based on font size; fine-tuned by fitFontSize at call sites.
        val estimatedCharWidth = fontSize * 0.6
        val maxChars = (availableWidth / estimatedCharWidth).toInt()

        return if (text.length > maxChars && maxChars > 3) {
            text.take(maxChars - 1) + "…"
        } else if (maxChars <= 3) {
            "" // Too small to show any text
        } else {
            text
        }
    }

    private fun wrapText(text: String, availableWidth: Double, fontSize: Int, maxLines: Int): List<String> {
        val estimatedCharWidth = fontSize * 0.6
        val maxChars = (availableWidth / estimatedCharWidth).toInt()
        if (maxChars <= 0 || text.isBlank() || maxLines <= 0) return listOf("")

        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return listOf("")

        val lines = mutableListOf<String>()
        var index = 0

        for (lineIndex in 0 until maxLines) {
            if (index >= words.size) break
            val line = StringBuilder()

            while (index < words.size) {
                val word = words[index]
                val candidate = if (line.isEmpty()) word else "$line $word"
                if (candidate.length <= maxChars || line.isEmpty()) {
                    line.setLength(0)
                    line.append(candidate)
                    index++
                } else {
                    break
                }
            }

            if (line.isEmpty()) {
                line.append(truncateWord(words[index], maxChars))
                index++
            }

            if (lineIndex == maxLines - 1 && index < words.size) {
                if (line.length >= maxChars) {
                    val cutoff = (maxChars - 1).coerceAtLeast(0)
                    line.setLength(cutoff)
                }
                line.append("…")
            }

            lines.add(line.toString())
        }

        return lines
    }

    private fun truncateWord(word: String, maxChars: Int): String {
        if (maxChars <= 0) return ""
        if (word.length <= maxChars) return word
        if (maxChars == 1) return "…"
        return word.take(maxChars - 1) + "…"
    }

    private fun buildWrappedText(lines: List<String>, lineHeight: Int, x: Double): String {
        return lines.mapIndexed { index, line ->
            val dy = if (index == 0) "0" else "$lineHeight"
            "<tspan x=\"$x\" dy=\"$dy\">${escapeXml(line)}</tspan>"
        }.joinToString("")
    }

    private fun fitFontSize(text: String, availableWidth: Double, baseSize: Int, minSize: Int): Int {
        if (text.isBlank() || availableWidth <= 0) return baseSize
        val estimatedWidth = text.length * baseSize * 0.6
        if (estimatedWidth <= availableWidth) return baseSize
        val scaled = (baseSize * (availableWidth / estimatedWidth)).toInt()
        return maxOf(minSize, scaled)
    }


    data class Rectangle(val x: Double, val y: Double, val width: Double, val height: Double)
}
