package io.docops.docopsextensionssupport.badge

import kotlin.math.max

/**
 * Generator for creating iOS-style SVG shields/badges
 */
class ShieldSvgGenerator(val useDark: Boolean) {

    companion object {
        // iOS style constants
        private const val DEFAULT_FONT_SIZE = 15
        private const val SECONDARY_FONT_SIZE = 12
        private const val DEFAULT_FONT_FAMILY = "-apple-system,BlinkMacSystemFont,'SF Pro Display',sans-serif"
        private const val SECONDARY_FONT_FAMILY = "-apple-system,BlinkMacSystemFont,'SF Pro Text',sans-serif"
        private const val PADDING_X = 16
        private const val PADDING_Y = 8
        private const val ICON_SIZE = 28
        private const val ICON_PADDING = 8
        private const val DEFAULT_HEIGHT = 56
        private const val BORDER_RADIUS = 28

        // Material design constants
        private const val MATERIAL_FONT_FAMILY = "'Roboto','Helvetica Neue',Arial,sans-serif"
        private const val MATERIAL_BORDER_RADIUS = 4
        private const val MATERIAL_HEIGHT = 40
        private const val MATERIAL_SHADOW = "0 2px 4px rgba(0,0,0,0.24)"
        private const val MATERIAL_BACKGROUND = "#FAFAFA"
    }

    private val iconRegistry = IconRegistry()

    /**
     * Generate SVG for a single shield based on theme
     */
    fun generateShieldSvg(shield: ShieldData, theme: String = "ios"): String {
        val isMaterial = theme == "material"

        // Use appropriate constants based on theme
        val borderRadius = if (isMaterial) MATERIAL_BORDER_RADIUS else BORDER_RADIUS
        val height = if (isMaterial) MATERIAL_HEIGHT else (shield.height.takeIf { it > 40 } ?: DEFAULT_HEIGHT)
        val fontFamily = if (isMaterial) MATERIAL_FONT_FAMILY else DEFAULT_FONT_FAMILY
        val secondaryFontFamily = if (isMaterial) MATERIAL_FONT_FAMILY else SECONDARY_FONT_FAMILY

        // Keep original label width calculation - don't shorten it
        val labelWidth = calculateTextWidth(shield.label, DEFAULT_FONT_SIZE) + PADDING_X * 2
        val messageWidth = calculateTextWidth(shield.message, SECONDARY_FONT_SIZE) + PADDING_X * 2
        val iconWidth = if (shield.icon != null) ICON_SIZE + ICON_PADDING * 2 else 0

        val totalWidth = max(220, labelWidth + messageWidth + iconWidth)

        val iconX = if (shield.icon != null) (if (isMaterial) ICON_PADDING else BORDER_RADIUS) else 0

        // Calculate available space for text sections
        val availableTextWidth = totalWidth - iconWidth - (PADDING_X * 2)
        val labelSectionWidth = availableTextWidth * 0.4 // 40% for label
        val messageSectionWidth = availableTextWidth * 0.6 // 60% for message

        // Calculate text positions (centers of each section)
        val labelX = iconX + iconWidth + (labelSectionWidth / 2)
        val messageX = iconX + iconWidth + labelSectionWidth + (messageSectionWidth / 2)

        // Calculate maximum text lengths to prevent overflow - more generous estimate
        val maxLabelChars = estimateMaxChars(labelSectionWidth, DEFAULT_FONT_SIZE.toDouble())
        val maxMessageChars = estimateMaxChars(messageSectionWidth, SECONDARY_FONT_SIZE.toDouble())

        // Truncate text if necessary
        val truncatedLabel = truncateText(shield.label, maxLabelChars)
        val truncatedMessage = truncateText(shield.message, maxMessageChars)

        return buildString {
            appendLine("""<svg width="$totalWidth" height="$height" viewBox="0 0 $totalWidth $height" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">""")
            appendLine("""<defs>""")
            appendLine(generateIOSGradients(shield, theme))
            appendLine(generateIOSFilters(shield, theme))

            // Add clipping paths to ensure text doesn't overflow
            val labelClipId = "label-clip-${shield.hashCode()}"
            val messageClipId = "message-clip-${shield.hashCode()}"

            appendLine("""
            <clipPath id="$labelClipId">
                <rect x="${iconX + iconWidth}" y="0" width="$labelSectionWidth" height="$height"/>
            </clipPath>
            <clipPath id="$messageClipId">
                <rect x="${iconX + iconWidth + labelSectionWidth}" y="0" width="$messageSectionWidth" height="$height"/>
            </clipPath>
        """.trimIndent())

            appendLine("""</defs>""")

            // Wrap content in a link if present
            if (shield.link != null) {
                appendLine("""<a xlink:href="${escapeXml(shield.link)}" target="_blank">""")
                appendLine("""<title>${escapeXml(shield.link)}</title>""")
            }

            // Main background with gradient and shadow
            appendLine("""<rect x="0" y="0" width="$totalWidth" height="$height" rx="$borderRadius" ry="$borderRadius" fill="url(#bg-gradient-${shield.hashCode()})" filter="url(#shadow-${shield.hashCode()})"/>""")

            // Highlight overlay for glassmorphism effect (iOS only)
            if (!isMaterial) {
                appendLine("""<rect x="1" y="1" width="${totalWidth-2}" height="${height-2}" rx="${borderRadius-1}" ry="${borderRadius-1}" fill="url(#highlight-${shield.hashCode()})" opacity="0.6"/>""")
            }

            // Icon handling
            shield.icon?.let {
                val iconCenterX = if (isMaterial) ICON_SIZE/2 + ICON_PADDING else BORDER_RADIUS
                val iconCenterY = height / 2

                // Icon background (circle for iOS, no background for Material)
                if (!isMaterial) {
                    appendLine("""<circle cx="$iconCenterX" cy="$iconCenterY" r="14" fill="rgba(255,255,255,0.2)"/>""")
                }

                // Icon
                val iconSvg = iconRegistry.getIOSIcon(it, shield.iconColor)
                appendLine("""<g transform="translate(${iconCenterX - ICON_SIZE/2}, ${iconCenterY - ICON_SIZE/2})">""")
                appendLine(iconSvg)
                appendLine("""</g>""")

                // Vertical separator line (iOS only)
                if (!isMaterial) {
                    val separatorX = iconCenterX + 24
                    appendLine("""<line x1="$separatorX" y1="14" x2="$separatorX" y2="${height-14}" stroke="rgba(255,255,255,0.3)" stroke-width="1"/>""")
                }
            }

            // Primary text (label) with clipping to prevent overflow - NO textLength stretching
            val primaryTextY = height / 2 - (if (isMaterial) 5 else 8)
            val labelCase = if (isMaterial) truncatedLabel else truncatedLabel.uppercase()
            val labelWeight = if (isMaterial) "500" else "700"

            appendLine("""<g clip-path="url(#$labelClipId)">""")
            appendLine("""<text x="$labelX" y="$primaryTextY" font-family="$fontFamily" font-size="$DEFAULT_FONT_SIZE" font-weight="$labelWeight" fill="${shield.labelColor}" text-anchor="middle" letter-spacing="0.5px">${escapeXml(labelCase)}</text>""")
            appendLine("""</g>""")

            // Secondary text (message) with clipping to prevent overflow - NO textLength stretching
            val secondaryTextY = height / 2 + (if (isMaterial) 8 else 10)

            appendLine("""<g clip-path="url(#$messageClipId)">""")
            appendLine("""<text x="$messageX" y="$secondaryTextY" font-family="$secondaryFontFamily" font-size="$SECONDARY_FONT_SIZE" font-weight="400" fill="${shield.messageColor}" text-anchor="middle" letter-spacing="0.2px">${escapeXml(truncatedMessage)}</text>""")
            appendLine("""</g>""")

            // Optional branch/additional info in top right (iOS only)
            if (!isMaterial) {
                shield.link?.let { link ->
                    if (link.contains("branch=") || link.contains("/")) {
                        val branchName = extractBranchName(link)
                        val maxBranchChars = estimateMaxChars(totalWidth * 0.3, 10.0)
                        val truncatedBranch = truncateText(branchName, maxBranchChars)
                        appendLine("""<text x="${totalWidth-10}" y="18" font-family="$secondaryFontFamily" font-size="10" font-weight="600" fill="rgba(255,255,255,0.7)" text-anchor="end">${truncatedBranch}</text>""")
                    }
                }
            }

            // Close link if present
            if (shield.link != null) {
                appendLine("""</a>""")
            }

            appendLine("""</svg>""")
        }
    }

    /**
     * Estimates maximum characters that can fit in given width (more generous)
     */
    private fun estimateMaxChars(width: Double, fontSize: Double): Int {
        // More generous estimate to avoid over-truncation
        val avgCharWidth = fontSize * 0.6
        return maxOf(1, (width / avgCharWidth).toInt())
    }

    /**
     * Truncates text to fit within character limit with ellipsis
     */
    private fun truncateText(text: String, maxChars: Int): String {
        return if (text.length <= maxChars) {
            text
        } else if (maxChars <= 3) {
            text.take(maxChars)
        } else {
            text.take(maxChars - 1) + "…"
        }
    }

    private fun calculateTextWidth(text: String, fontSize: Int): Int {
        // Keep the original text width calculation - don't shorten it
        val baseWidth = when (fontSize) {
            15 -> 8.5  // Original value preserved
            12 -> 6.8  // Original value preserved
            else -> 7.0  // Original value preserved
        }
        return (text.length * baseWidth).toInt()
    }

    /**
     * Generate gradients based on theme
     */
    private fun generateIOSGradients(shield: ShieldData, theme: String = "ios"): String {
        val id = shield.hashCode()

        return if (theme == "material") {
            // Material design uses flat colors, no gradients
            """
            <linearGradient id="bg-gradient-$id" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" style="stop-color:${shield.leftColor};stop-opacity:1" />
                <stop offset="100%" style="stop-color:${shield.rightColor};stop-opacity:1" />
            </linearGradient>
            """.trimIndent()
        } else {
            // iOS style with vertical gradient and highlight
            """
            <linearGradient id="bg-gradient-$id" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:${shield.leftColor};stop-opacity:1" />
                <stop offset="100%" style="stop-color:${darkenColor(shield.leftColor)};stop-opacity:1" />
            </linearGradient>

            <linearGradient id="highlight-$id" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
            </linearGradient>
            """.trimIndent()
        }
    }

    /**
     * Generate filters based on theme
     */
    private fun generateIOSFilters(shield: ShieldData, theme: String = "ios"): String {
        val id = shield.hashCode()

        return if (theme == "material") {
            // Material design shadow
            """
            <filter id="shadow-$id" x="-20%" y="-20%" width="140%" height="140%">
                <feDropShadow dx="0" dy="2" stdDeviation="2" flood-color="rgba(0,0,0,0.24)"/>
            </filter>
            """.trimIndent()
        } else {
            // iOS style shadow and inner shadow
            """
            <filter id="shadow-$id" x="-20%" y="-20%" width="140%" height="140%">
                <feDropShadow dx="0" dy="3" stdDeviation="4" flood-color="rgba(0,0,0,0.15)"/>
            </filter>

            <filter id="inner-shadow-$id" x="-20%" y="-20%" width="140%" height="140%">
                <feOffset dx="0" dy="1"/>
                <feGaussianBlur stdDeviation="1" result="offset-blur"/>
                <feFlood flood-color="rgba(0,0,0,0.1)"/>
                <feComposite in2="offset-blur" operator="in"/>
            </filter>
            """.trimIndent()
        }
    }

    /**
     * Generate SVG for multiple shields with spacing and arrangement based on theme
     */
    fun generateShieldsTable(shields: List<ShieldData>, config: ShieldTableConfig): String {
        // All arrangement methods now support both iOS and Material themes
        return when (config.arrangement) {
            ShieldArrangement.HORIZONTAL -> generateHorizontalShields(shields, config)
            ShieldArrangement.VERTICAL -> generateIOSVerticalShields(shields, config)
            ShieldArrangement.GRID -> generateIOSGridShields(shields, config)
        }
    }

    private fun generateHorizontalShields(shields: List<ShieldData>, config: ShieldTableConfig): String {
        val isMaterial = config.theme == "material"
        val shieldSvgs = shields.map { generateShieldSvg(it, config.theme) }
        val spacing = config.spacing.takeIf { it > 0 } ?: 15

        // Use appropriate height based on theme
        val defaultHeight = if (isMaterial) MATERIAL_HEIGHT else DEFAULT_HEIGHT
        val shieldHeight = shields.firstOrNull()?.height?.takeIf { it > 40 } ?: defaultHeight

        // Calculate total width based on shields and spacing
        var totalWidth = 0
        shields.forEachIndexed { index, shield ->
            val labelWidth = calculateTextWidth(shield.label, DEFAULT_FONT_SIZE) + PADDING_X * 2
            val messageWidth = calculateTextWidth(shield.message, SECONDARY_FONT_SIZE) + PADDING_X * 2
            val iconWidth = if (shield.icon != null) ICON_SIZE + ICON_PADDING * 2 else 0
            val shieldWidth = max(220, labelWidth + messageWidth + iconWidth)

            totalWidth += shieldWidth
            if (index < shields.size - 1) {
                totalWidth += spacing
            }
        }

        // Add padding to total width and height
        totalWidth += 40  // 20px padding on each side
        val totalHeight = shieldHeight + 40  // 20px padding on top and bottom

        return buildString {
            appendLine("""<svg width="$totalWidth" height="$totalHeight" viewBox="0 0 $totalWidth $totalHeight" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">""")

            // Background rectangle with styling based on theme
            val containerBg = if (isMaterial) MATERIAL_BACKGROUND else "#f5f5f7"
            val cornerRadius = if (isMaterial) "2" else "12"

            appendLine("""<rect x="0" y="0" width="$totalWidth" height="$totalHeight" rx="$cornerRadius" ry="$cornerRadius" fill="$containerBg" filter="url(#container-shadow)"/>""")

            // Define shadow filter for container
            val shadowColor = if (isMaterial) "rgba(0,0,0,0.24)" else "rgba(0,0,0,0.1)"
            val shadowDeviation = if (isMaterial) "2" else "3"

            appendLine("""<defs>
                <filter id="container-shadow" x="-10%" y="-10%" width="120%" height="120%">
                    <feDropShadow dx="0" dy="2" stdDeviation="$shadowDeviation" flood-color="$shadowColor"/>
                </filter>
            </defs>""")

            // Position each shield
            var xOffset = 20  // Start with left padding
            shieldSvgs.forEach { svg ->
                // Extract width from the SVG
                val widthMatch = Regex("""width="(\d+)"""").find(svg)
                val width = widthMatch?.groupValues?.get(1)?.toIntOrNull() ?: 220

                // Create a group for this shield and position it
                appendLine("""<g transform="translate($xOffset, 20)">""")
                appendLine(svg)
                appendLine("""</g>""")

                xOffset += width + spacing
            }

            appendLine("""</svg>""")
        }
    }

    private fun generateIOSVerticalShields(shields: List<ShieldData>, config: ShieldTableConfig): String {
        val isMaterial = config.theme == "material"
        val shieldSvgs = shields.map { generateShieldSvg(it, config.theme) }
        val spacing = config.spacing.takeIf { it > 0 } ?: 15

        // Find the widest shield
        var maxWidth = 300 // Default max width
        shields.forEach { shield ->
            val labelWidth = calculateTextWidth(shield.label, DEFAULT_FONT_SIZE) + PADDING_X * 2
            val messageWidth = calculateTextWidth(shield.message, SECONDARY_FONT_SIZE) + PADDING_X * 2
            val iconWidth = if (shield.icon != null) ICON_SIZE + ICON_PADDING * 2 else 0
            val shieldWidth = max(220, labelWidth + messageWidth + iconWidth)
            maxWidth = max(maxWidth, shieldWidth)
        }

        // Calculate total height
        val defaultHeight = if (isMaterial) MATERIAL_HEIGHT else DEFAULT_HEIGHT
        val shieldHeight = shields.firstOrNull()?.height?.takeIf { it > 40 } ?: defaultHeight
        var totalHeight = 0
        shields.forEachIndexed { index, _ ->
            totalHeight += shieldHeight
            if (index < shields.size - 1) {
                totalHeight += spacing
            }
        }

        // Add padding
        val totalWidth = maxWidth + 40 // 20px padding on each side
        totalHeight += 40 // 20px padding on top and bottom

        return buildString {
            appendLine("""<svg width="$totalWidth" height="$totalHeight" viewBox="0 0 $totalWidth $totalHeight" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">""")

            // Background rectangle with styling based on theme
            val containerBg = if (isMaterial) MATERIAL_BACKGROUND else "#f5f5f7"
            val cornerRadius = if (isMaterial) "2" else "12"

            appendLine("""<rect x="0" y="0" width="$totalWidth" height="$totalHeight" rx="$cornerRadius" ry="$cornerRadius" fill="$containerBg" filter="url(#container-shadow)"/>""")

            // Define shadow filter for container
            val shadowColor = if (isMaterial) "rgba(0,0,0,0.24)" else "rgba(0,0,0,0.1)"
            val shadowDeviation = if (isMaterial) "2" else "3"

            appendLine("""<defs>
                <filter id="container-shadow" x="-10%" y="-10%" width="120%" height="120%">
                    <feDropShadow dx="0" dy="2" stdDeviation="$shadowDeviation" flood-color="$shadowColor"/>
                </filter>
            </defs>""")

            // Position each shield
            var yOffset = 20 // Start with top padding
            shieldSvgs.forEach { svg ->
                // Extract height from the SVG
                val heightMatch = Regex("""height="(\d+)"""").find(svg)
                val height = heightMatch?.groupValues?.get(1)?.toIntOrNull() ?: defaultHeight

                // Create a group for this shield and position it
                appendLine("""<g transform="translate(20, $yOffset)">""")
                appendLine(svg)
                appendLine("""</g>""")

                yOffset += height + spacing
            }

            appendLine("""</svg>""")
        }
    }

    private fun generateIOSGridShields(shields: List<ShieldData>, config: ShieldTableConfig, columns: Int = 2): String {
        val isMaterial = config.theme == "material"
        val shieldSvgs = shields.map { generateShieldSvg(it, config.theme) }
        val spacing = config.spacing.takeIf { it > 0 } ?: 15

        // Calculate shield dimensions
        val defaultHeight = if (isMaterial) MATERIAL_HEIGHT else DEFAULT_HEIGHT
        val shieldHeight = shields.firstOrNull()?.height?.takeIf { it > 40 } ?: defaultHeight

        // Find the widest shield
        var maxShieldWidth = 220 // Default shield width
        shields.forEach { shield ->
            val labelWidth = calculateTextWidth(shield.label, DEFAULT_FONT_SIZE) + PADDING_X * 2
            val messageWidth = calculateTextWidth(shield.message, SECONDARY_FONT_SIZE) + PADDING_X * 2
            val iconWidth = if (shield.icon != null) ICON_SIZE + ICON_PADDING * 2 else 0
            val shieldWidth = max(220, labelWidth + messageWidth + iconWidth)
            maxShieldWidth = max(maxShieldWidth, shieldWidth)
        }

        // Calculate grid dimensions
        val rows = (shields.size + columns - 1) / columns // Ceiling division
        val cellWidth = maxShieldWidth
        val cellHeight = shieldHeight

        // Calculate total dimensions with padding
        val totalWidth = columns * cellWidth + (columns - 1) * spacing + 40 // Add padding
        val totalHeight = rows * cellHeight + (rows - 1) * spacing + 40 // Add padding

        return buildString {
            appendLine("""<svg width="$totalWidth" height="$totalHeight" viewBox="0 0 $totalWidth $totalHeight" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">""")

            // Background rectangle with styling based on theme
            val containerBg = if (isMaterial) MATERIAL_BACKGROUND else "#f5f5f7"
            val cornerRadius = if (isMaterial) "2" else "12"

            appendLine("""<rect x="0" y="0" width="$totalWidth" height="$totalHeight" rx="$cornerRadius" ry="$cornerRadius" fill="$containerBg" filter="url(#container-shadow)"/>""")

            // Define shadow filter for container
            val shadowColor = if (isMaterial) "rgba(0,0,0,0.24)" else "rgba(0,0,0,0.1)"
            val shadowDeviation = if (isMaterial) "2" else "3"

            appendLine("""<defs>
                <filter id="container-shadow" x="-10%" y="-10%" width="120%" height="120%">
                    <feDropShadow dx="0" dy="2" stdDeviation="$shadowDeviation" flood-color="$shadowColor"/>
                </filter>
            </defs>""")

            // Position each shield in a grid layout
            shields.forEachIndexed { index, _ ->
                val row = index / columns
                val col = index % columns

                val xOffset = 20 + col * (cellWidth + spacing)
                val yOffset = 20 + row * (cellHeight + spacing)

                // Create a group for this shield and position it
                appendLine("""<g transform="translate($xOffset, $yOffset)">""")
                appendLine(shieldSvgs[index])
                appendLine("""</g>""")
            }

            appendLine("""</svg>""")
        }
    }

    private fun darkenColor(color: String): String {
        return when {
            color.startsWith("#") && color.length == 7 -> {
                val r = color.substring(1, 3).toInt(16)
                val g = color.substring(3, 5).toInt(16)
                val b = color.substring(5, 7).toInt(16)

                val darkerR = (r * 0.8).toInt().coerceIn(0, 255)
                val darkerG = (g * 0.8).toInt().coerceIn(0, 255)
                val darkerB = (b * 0.8).toInt().coerceIn(0, 255)

                "#%02x%02x%02x".format(darkerR, darkerG, darkerB)
            }
            else -> color
        }
    }

    private fun extractBranchName(link: String): String {
        return when {
            link.contains("branch=") -> {
                link.substringAfter("branch=").substringBefore("&").take(10)
            }
            link.contains("/") -> {
                link.substringAfterLast("/").take(10)
            }
            else -> ""
        }
    }



    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}

/**
 * Registry for iOS-style SVG icons used in shields
 */
class IconRegistry {

    private val iosIcons = mapOf(
        "check" to """<path d="M10 14l3 3 6-6" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" fill="none"/>""",
        "x" to """<path d="M10 10l8 8m0-8l-8 8" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>""",
        "spinner" to """<circle cx="14" cy="14" r="6" stroke="white" stroke-width="2" fill="none" stroke-linecap="round" stroke-dasharray="37.7" stroke-dashoffset="37.7">
                      <animateTransform attributeName="transform" type="rotate" values="0 14 14;360 14 14" dur="1s" repeatCount="indefinite"/>
                      </circle>""",
        "clock" to """<circle cx="14" cy="14" r="6" stroke="white" stroke-width="2" fill="none"/>
                    <path d="M14 8v6l4 2" stroke="white" stroke-width="2" stroke-linecap="round"/>""",
        "stop" to """<rect x="10" y="10" width="8" height="8" rx="1" fill="white"/>""",
        "star" to """<path d="M14 2l3.09 6.26L24 9.27l-5 4.87 1.18 6.88L14 17.77l-6.18 3.25L9 14.14 4 9.27l6.91-1.01L14 2z" fill="white"/>""",
        "heart" to """<path d="M14 9c-2-2-5.5-1-5.5 1.5 0 3 5.5 6.5 5.5 6.5s5.5-3.5 5.5-6.5c0-2.5-3.5-3.5-5.5-1.5z" fill="white"/>""",
        "github" to """<path d="M14 0C6.27 0 0 6.43 0 14.36c0 6.34 4.01 11.72 9.57 13.62.7.13.96-.31.96-.69 0-.34-.01-1.24-.02-2.44-3.89.87-4.71-1.92-4.71-1.92-.64-1.66-1.55-2.1-1.55-2.1-1.27-.89.1-.87.1-.87 1.4.1 2.14 1.48 2.14 1.48 1.25 2.19 3.28 1.56 4.08 1.19.13-.93.49-1.56.89-1.92-3.11-.36-6.38-1.6-6.38-7.09 0-1.57.55-2.85 1.44-3.85-.14-.36-.62-1.82.14-3.8 0 0 1.18-.39 3.85 1.47a12.8 12.8 0 0 1 7 0c2.67-1.86 3.85-1.47 3.85-1.47.76 1.98.28 3.44.14 3.8.9 1 1.44 2.28 1.44 3.85 0 5.51-3.27 6.73-6.39 7.08.5.44.95 1.32.95 2.66 0 1.92-.02 3.47-.02 3.94 0 .38.25.83.96.69C23.99 26.07 28 20.7 28 14.36 28 6.43 21.73 0 14 0z" fill="white"/>""",
        "book" to """<path d="M4 2h16c1.1 0 2 .9 2 2v16c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2zm0 2v16h16V4H4zm2 2h12v2H6V6zm0 4h12v2H6v-2zm0 4h8v2H6v-2z" fill="white"/>""",
        "code" to """<path d="M9 7l-5 7 5 7 1.5-1.5L6 14l4.5-5.5L9 7zm10 0l-1.5 1.5L22 14l-4.5 5.5L19 21l5-7-5-7z" fill="white"/>""",
        "api" to """<path d="M2 6h24v4H2V6zm0 8h24v4H2v-4zm0 8h24v4H2v-4z" fill="white"/>""",
        "award" to """<circle cx="14" cy="10" r="8" fill="none" stroke="white" stroke-width="2"/>
                   <path d="M14 18l-4 8h8l-4-8" fill="white"/>
                   <circle cx="14" cy="10" r="3" fill="white"/>""",
        "building" to """<path d="M4 2h16v20H4V2zm4 4v12h8V6H8zm2 2h2v2h-2V8zm4 0h2v2h-2V8zm-4 4h2v2h-2v-2zm4 0h2v2h-2v-2zm-4 4h2v2h-2v-2zm4 0h2v2h-2v-2z" fill="white"/>""",
        "twitter" to """<path d="M28 5.1c-1 .5-2.1.8-3.2 1 1.1-.7 2-1.8 2.4-3.2-1.1.6-2.3 1.1-3.6 1.3-1-1.1-2.4-1.7-4-1.7-3 0-5.4 2.5-5.4 5.5 0 .4 0 .8.1 1.2C9.1 8.9 5.1 6.9 2.5 3.8c-.5.8-.7 1.8-.7 2.9 0 1.9.9 3.6 2.4 4.6-.9 0-1.7-.3-2.4-.7v.1c0 2.7 1.9 4.9 4.4 5.4-.5.1-1 .2-1.5.2-.4 0-.7 0-1.1-.1.7 2.3 2.9 3.9 5.4 4C6.7 21.4 4.4 22.2 2 22.2c-.4 0-.9 0-1.3-.1 2.4 1.6 5.2 2.5 8.2 2.5 9.8 0 15.2-8.4 15.2-15.7v-.7c1.1-.8 2-1.8 2.7-2.9z" fill="white"/>""",
        "linkedin" to """<path d="M5 4h4v16H5V4zM7 0c1.6 0 3 1.4 3 3S8.6 6 7 6 4 4.6 4 3s1.4-3 3-3zM12 8h4v2.2c.6-1.2 2.4-2.4 4.8-2.4 5.2 0 6.2 3.4 6.2 7.8V20h-4v-4.6c0-1.4 0-3.2-2-3.2s-2.2 1.6-2.2 3.2V20h-4V8z" fill="white"/>""",
        "download" to """<path d="M14 2v12m-6-6l6 6 6-6" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                       <path d="M4 20h20" stroke="white" stroke-width="2" stroke-linecap="round"/>"""
    )

    fun getIOSIcon(name: String, color: String = "white"): String {
        return iosIcons[name]?.replace("white", color)
            ?: """<circle cx="14" cy="14" r="6" fill="$color"/>"""
    }
}
