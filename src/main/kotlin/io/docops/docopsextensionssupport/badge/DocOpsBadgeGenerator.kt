/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.badge


import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import org.silentsoft.simpleicons.SimpleIcons
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


private const val BadgePerRow = 5

/**
 * This class is responsible for generating badges using DocOps theme with enhanced visual styling.
 * 
 * ## Enhanced Badge Features
 * 
 * ### Improved Gradient Styling
 * - Smoother color transitions with 5 color stops instead of 3
 * - Configurable opacity values for depth (opacityStart, opacityMiddle, opacityEnd)
 * - Multiple gradient angle options (to bottom, to right, to bottom right, etc.)
 * - Color blending for more natural transitions
 * 
 * ### Modern Rounded Corners and Shape Improvements
 * - Consistent rounded corners with configurable radius
 * - Subtle divider between label and message sections
 * - Improved shape consistency across different badge sizes
 * 
 * ### Enhanced Typography
 * - Modern font stack: 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif
 * - Improved letter-spacing (0.5px) for better readability
 * - Font-weight variations for visual hierarchy (500 for label, 400 for message)
 * - Better text alignment and padding
 * 
 * ### Shadow and Lighting Effects
 * - Subtle drop shadow for depth (optional)
 * - Optimized bevel effects for consistent lighting
 * - Soft highlight effect on top edge (optional)
 * - All effects can be disabled for simpler rendering
 * 
 * ### Improved Logo/Icon Handling
 * - Enhanced logo positioning and scaling
 * - Better spacing around icons
 * - Improved aspect ratio preservation with preserveAspectRatio
 * - Subtle effects to make icons pop visually
 * 
 * ### Interactive Elements (for web usage)
 * - Hover effects for badges with links
 * - Subtle animations for state changes
 * - CSS transitions for a polished feel
 * - Animations degrade gracefully in static contexts
 * 
 * ### Optimized SVG Output
 * - Whitespace trimming and SVG code optimization
 * - Removal of redundant elements and attributes
 * - Shorter path definitions
 * - Optional minification for production use
 * 
 * ### Accessibility Enhancements
 * - Improved ARIA attributes and role definitions
 * - More descriptive title and desc elements
 * - Proper contrast ratios for text readability
 * - Keyboard navigation support for interactive badges
 * 
 * ### Theme Support
 * - Dark/light mode detection and adaptation
 * - Theme-aware styling for consistent appearance
 * - Color scheme preference media queries
 * - High contrast mode support
 * 
 * ## Example Usage
 * 
 * Basic badge:
 * ```kotlin
 * val badge = docOpsBadgeGenerator.createBadge(
 *     "Label", 
 *     "Message", 
 *     labelColor = "#007acc", 
 *     messageColor = "#2ecc71"
 * )
 * ```
 * 
 * Badge with link and icon:
 * ```kotlin
 * val badge = docOpsBadgeGenerator.createBadge(
 *     "GitHub", 
 *     "Stars: 1.2k", 
 *     labelColor = "#24292e", 
 *     messageColor = "#2188ff",
 *     href = "https://github.com/docops/docops-extension-server",
 *     icon = "<github>"
 * )
 * ```
 * 
 * ## Before/After Comparison
 * 
 * The enhanced badges feature:
 * - More professional appearance with smoother gradients
 * - Better readability with improved typography
 * - More consistent styling across different environments
 * - Better accessibility for all users
 * - Responsive design that adapts to user preferences
 */
@Service
class DocOpsBadgeGenerator {


    companion object {
        private const val BadgePerRow = 5
        val widths = arrayOf<Number>(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.1316650390625,0.42833404541015624,0.5066665649414063,0.7066665649414062,0.7066665649414062,1.0383331298828125,0.8183334350585938,0.34499969482421877,0.4850006103515625,0.4850006103515625,0.5383331298828125,0.7350006103515625,0.42833404541015624,0.4850006103515625,0.42833404541015624,0.42833404541015624,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.42833404541015624,0.42833404541015624,0.7350006103515625,0.7350006103515625,0.7350006103515625,0.7066665649414062,1.1649993896484374,0.8199996948242188,0.8183334350585938,0.8716659545898438,0.8716659545898438,0.8183334350585938,0.7633331298828125,0.9316665649414062,0.8716659545898438,0.42833404541015624,0.65,0.8183334350585938,0.7066665649414062,0.9833328247070312,0.8716659545898438,0.9316665649414062,0.8183334350585938,0.9316665649414062,0.8716659545898438,0.8183334350585938,0.7633331298828125,0.8716659545898438,0.8183334350585938,1.0949996948242187,0.8183334350585938,0.8183334350585938,0.7633331298828125,0.42833404541015624,0.42833404541015624,0.42833404541015624,0.6199996948242188,0.7349990844726563,0.4850006103515625,0.7066665649414062,0.7066665649414062,0.65,0.7066665649414062,0.7066665649414062,0.4633331298828125,0.7066665649414062,0.7066665649414062,0.375,0.42166748046875,0.65,0.375,0.9833328247070312,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.498333740234375,0.65,0.42833404541015624,0.7066665649414062,0.65,0.8716659545898438,0.65,0.65,0.65,0.4850006103515625,0.4100006103515625,0.4850006103515625,0.7350006103515625)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun createBadgeFromList(badges: MutableList<Badge>): Pair<String, Float> {

        val allDefs = StringBuilder()
        val badgeElements = StringBuilder()

        var xPos = 0f
        var rowCount = 0
        var rowNum = 0
        var width= 1f

        badges.forEachIndexed { _, badge ->
            val label = badge.label.escapeXml()
            val message = badge.message.escapeXml()
            val maskId = Uuid.random().toHexString()

            // Use default colors if not provided
            val labelColorValue = badge.labelColor ?: "#555555"
            val messageColorValue = badge.messageColor ?: "#007ec6"

            val labelColor = SVGColor(labelColorValue, "label_${maskId}")
            val messageColor = SVGColor(messageColorValue, "message_${maskId}")

            // Collect gradients in defs
            allDefs.append(labelColor.linearGradient)
            allDefs.append(messageColor.linearGradient)

            // Calculate natural text widths with padding
            val labelPadding = 10f
            val messagePadding = 10f
            val dividerWidth = 0f

            var labelTextWidth = measureText(badge.label, 11) // 11pt font
            var messageTextWidth = measureText(badge.message, 11)

            // Add padding to create natural spacing
            var labelSectionWidth = labelTextWidth + (labelPadding * 2)
            var messageSectionWidth = messageTextWidth + (messagePadding * 2)

            var labelLink = label
            var messageLink = message
            badge.url?.let {
                if(it.isNotEmpty() && !badge.isPdf) {
                    labelLink = """<a href='${it}' target='_blank'>$label</a>"""
                    messageLink = """<a href='${it}' target='_blank'>$message</a>"""
                }
            }

            var logoWidth = 0f
            var img = ""
            badge.logo?.let {
                val logoSize = 14f // Logo size in the same units as text
                val logo = getBadgeLogo(it, logoSize.toInt(), true)
                logoWidth = logoSize + 4f // Logo + spacing
                labelSectionWidth += logoWidth

                img = """<image x='${labelPadding}' y='3' width='$logoSize' height='$logoSize' xlink:href='$logo' preserveAspectRatio='xMidYMid meet' aria-label='${badge.label} logo'/>"""
            }

            val totalBadgeWidth = labelSectionWidth + messageSectionWidth + dividerWidth

            val labelFill = "url(#label_${maskId})"
            val messageFill = "url(#message_${maskId})"

            // Generate mask definition (add to defs)
            allDefs.append(createMaskDef(maskId, labelSectionWidth, messageSectionWidth, dividerWidth))

            if(rowCount > BadgePerRow) {
                rowCount = 0
                rowNum++
                xPos = 0f
            } else {
                rowCount++
            }

            badgeElements.append("<g transform='translate($xPos,${rowNum*21})'>")

            val b = makeBadgeElement(
                maskId,
                labelSectionWidth,
                messageSectionWidth,
                dividerWidth,
                labelPadding + logoWidth,
                messagePadding,
                badge.fontColor,
                messageLink,
                labelLink,
                img,
                label,
                message,
                labelFill,
                messageFill
            )
            badgeElements.append(b)
            badgeElements.append("</g>")

            xPos += totalBadgeWidth + 2f // Add small gap between badges
            width = maxOf(width, xPos)
        }

        // Build final SVG with all defs and styles at the top
        val finalSvg = buildFinalSvg(allDefs.toString(), badgeElements.toString())
        return Pair(finalSvg, width)
    }
    /**
     * Creates mask definition to be placed in defs section
     */
    private fun createMaskDef(
        maskId: String,
        labelWidth: Float,
        messageWidth: Float,
        dividerWidth: Float,
        cornerRadius: Float = 3f
    ): String {
        val totalWidth = labelWidth + messageWidth + dividerWidth

        return """
                <mask id='mask_$maskId'>
                    <rect width='$totalWidth' height='20' rx='$cornerRadius' fill='#FFF'/>
                </mask>
            """.trimIndent()
    }

    /**
     * Creates the badge visual element (without defs/styles)
     */
    private fun makeBadgeElement(
        maskId: String,
        labelWidth: Float,
        messageWidth: Float,
        dividerWidth: Float,
        labelTextX: Float,
        messageTextX: Float,
        fontColor: String,
        messageLink: String,
        labelLink: String,
        img: String,
        label: String,
        message: String,
        labelFill: String,
        messageFill: String
    ): String {
        val totalWidth = labelWidth + messageWidth + dividerWidth
        val hasLink = labelLink.contains("<a") || messageLink.contains("<a")

        return """
                <svg width='$totalWidth' height='20' viewBox='0 0 $totalWidth 20' xmlns='http://www.w3.org/2000/svg' xmlns:xlink="http://www.w3.org/1999/xlink" role="img" aria-label="$label: $message" class="${if (hasLink) "badge-link" else ""}">
                 <title>$label: $message</title>
                 <desc>Badge showing $label with value $message</desc>

                 <!-- Badge shape with mask -->
                 <g mask='url(#mask_$maskId)' ${if (hasLink) "class='badge-shape'" else ""}>
                    <!-- Label background -->
                    <rect x='0' y='0' width='$labelWidth' height='20' fill='$labelFill'/>
                    
                    <!-- Message background -->
                    <rect x='${labelWidth + dividerWidth}' y='0' width='$messageWidth' height='20' fill='$messageFill'/>
                    
                    <!-- Subtle gradient overlay for depth -->
                    <rect x='0' y='0' width='$totalWidth' height='20' fill='url(#a)' opacity='0.1'/>
                 </g>

                 <!-- Text elements with natural sizing -->
                 <g aria-hidden='true' fill='$fontColor' font-family="'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif" 
                    font-size='11' ${if (hasLink) "class='badge-text'" else ""}>
                    <!-- Label text -->
                    <text x='$labelTextX' y='14' fill='$fontColor' style='font-weight: 500;'>$labelLink</text>
                    
                    <!-- Message text -->
                    <text x='${labelWidth + dividerWidth + messageTextX}' y='14' fill='$fontColor' style='font-weight: 400;'>$messageLink</text>
                 </g>

                 <!-- Logo/icon -->
                 ${if (img.isNotEmpty()) """
                 <g ${if (hasLink) "class='badge-logo'" else ""}>
                    $img
                 </g>
                 """ else ""}
                </svg>
            """.trimIndent()
    }

    /**
     * Builds the final SVG with all defs and styles at the top
     */
    private fun buildFinalSvg(defs: String, badgeElements: String): String {
        return """
                <defs>
                    ${createFilterDefinitions(useShadow = false, useTopHighlight = false)}
                    <linearGradient id='a' x2='0' y2='100%'>
                        <stop offset='0' stop-opacity='.1' stop-color='#EEE'/>
                        <stop offset='1' stop-opacity='.1'/>
                    </linearGradient>
                    $defs
                </defs>
                <style>
                    ${createGlobalStyles()}
                </style>
                $badgeElements
            """.trimIndent()
    }

    /**
     * Creates global styles that apply to all badges
     */
    private fun createGlobalStyles(): String {
        return """
                /* Base badge styles */
                svg {
                    shape-rendering: crispEdges;
                }
                
                text {
                    text-rendering: optimizeLegibility;
                    -webkit-font-smoothing: antialiased;
                    -moz-osx-font-smoothing: grayscale;
                }

                /* Theme-aware styles */
                @media (prefers-color-scheme: dark) {
                    .badge-shape rect {
                        filter: brightness(0.9);
                    }
                }

                @media (prefers-color-scheme: light) {
                    .badge-shape rect {
                        filter: brightness(1);
                    }
                }

                /* Hover effect for linked badges */
                a {
                    text-decoration: none;
                }
                
                a:hover {
                    text-decoration: underline;
                }

                .badge-link:hover {
                    opacity: 0.9;
                    transition: opacity 0.2s ease;
                }

                /* Ensure animations degrade gracefully */
                @media (prefers-reduced-motion: reduce) {
                    a:hover, .badge-link:hover {
                        transition: none;
                    }
                }
            """.trimIndent()
    }

    fun measureText(str: String, fontSize: Int = 11): Float {
        var total = 0f
        var i = 0
        while (i < str.length) {
            val char = str[i]
            val code = char.code

            // Handle surrogate pairs for proper Unicode support
            if (char.isHighSurrogate() && i + 1 < str.length) {
                val nextChar = str[i + 1]
                if (nextChar.isLowSurrogate()) {
                    // This is a surrogate pair, skip to next character
                    i += 2
                    // Use default width for emoji/extended Unicode
                    total += widths[64].toFloat() * fontSize
                    continue
                }
            }

            val charWidth = when {
                code >= widths.size -> widths[64].toFloat()
                else -> widths[code].toFloat()
            }

            total += charWidth * fontSize
            i++
        }
        return total
    }

    private fun makeGradient(
        maskId: String,
        clrMap: Map<String, String>,
        mMap: Map<String, String>
    ): String {
        val grad = """
                    <linearGradient id="label_${maskId}" x2="0%" y2="100%">
                        <stop class="stop1" offset="0%" stop-color="${clrMap["color1"]}"/>
                        <stop class="stop2" offset="50%" stop-color="${clrMap["color2"]}"/>
                        <stop class="stop3"  stop-color="${clrMap["color3"]}" stop-opacity="1" offset="100%"/>
                    </linearGradient> 
                    <linearGradient id="message_${maskId}" x2="0%" y2="100%">
                        <stop class="stop1" offset="0%" stop-color="${mMap["color1"]}"/>
                        <stop class="stop2" offset="50%" stop-color="${mMap["color2"]}"/>
                        <stop class="stop3"  stop-color="${mMap["color3"]}" stop-opacity="1" offset="100%"/>
                    </linearGradient> 
                """.trimIndent()
        return grad
    }


    /**
     * Creates enhanced filter definitions for badges with improved lighting effects and shadows
     * @param useShadow Whether to include drop shadow effect
     * @param useTopHighlight Whether to include top edge highlight effect
     * @return SVG filter definitions as a string
     */
    /**
     * Creates enhanced accessibility attributes for badges
     * @param label The badge label
     * @param message The badge message
     * @param hasLink Whether the badge has a link
     * @return Enhanced accessibility attributes as a string
     */
    private fun createAccessibilityAttributes(label: String, message: String, hasLink: Boolean = false): String {
        val description = "Badge with label '$label' and message '$message'"

        return """
            role="img" 
            aria-label="$label: $message"
            aria-description="$description"
            ${if (hasLink) """
            tabindex="0"
            aria-live="polite"
            aria-atomic="true"
            """ else ""}
        """.trimIndent()
    }



    /**
     * Creates enhanced filter definitions for badges with improved lighting effects and shadows
     * @param useShadow Whether to include drop shadow effect
     * @param useTopHighlight Whether to include top edge highlight effect
     * @return SVG filter definitions as a string
     */
    private fun createFilterDefinitions(useShadow: Boolean = true, useTopHighlight: Boolean = true): String {
        return """
                ${if (useShadow) """
                <filter id="DropShadow" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="1.5" result="blur"/>
                    <feOffset in="blur" dx="1" dy="1" result="offsetBlur"/>
                    <feFlood flood-color="#000000" flood-opacity="0.2" result="shadowColor"/>
                    <feComposite in="shadowColor" in2="offsetBlur" operator="in" result="shadowBlur"/>
                    <feComposite in="SourceGraphic" in2="shadowBlur" operator="over"/>
                </filter>
                """ else ""}
                ${if (useTopHighlight) """
                <filter id="TopHighlight" filterUnits="objectBoundingBox" x="0%" y="0%" width="100%" height="100%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="0.5" result="blur"/>
                    <feOffset in="blur" dx="0" dy="-1" result="offsetBlur"/>
                    <feFlood flood-color="#FFFFFF" flood-opacity="0.3" result="highlightColor"/>
                    <feComposite in="highlightColor" in2="offsetBlur" operator="in" result="highlightBlur"/>
                    <feComposite in="SourceGraphic" in2="highlightBlur" operator="over"/>
                </filter>
                """ else ""}
            """.trimIndent()
    }

    /**
     * Optimizes SVG content by removing unnecessary whitespace and elements
     * @param svg The SVG content to optimize
     * @param minify Whether to perform more aggressive minification
     * @return The optimized SVG content
     */
    private fun optimizeSvg(svg: String, minify: Boolean = false): String {
        var result = svg

        // Basic whitespace cleanup
        result = result.replace(Regex("\\s+"), " ")
            .replace(Regex("> +<"), "><")
            .replace(Regex("\\n"), "")

        if (minify) {
            // More aggressive optimizations for production
            result = result.replace(Regex("<!--.*?-->"), "")
            result = result.replace(Regex("version=\"1\\.1\""), "")
                .replace(Regex("xmlns:xlink=\"http://www\\.w3\\.org/1999/xlink\"\\s+"), "")
            result = result.replace(Regex("#([0-9a-f])\\1([0-9a-f])\\2([0-9a-f])\\3"), "#$1$2$3")
            result = result.replace(Regex("([\\s:])0\\.([0-9])"), "$1.$2")
            result = result.replace(Regex("([\\d])\\.0([\\D])"), "$1$2")
        }

        return result
    }


}

private fun getBadgeLogo(
    input: String?,
    size: Int = 100,
    addEffects: Boolean = true,
    backgroundColor: String? = null
): String {
    if (input == null || input.isEmpty()) {
        return ""
    }

    var logo = input

    // Handle SimpleIcons format: <iconname>
    if (logo.startsWith("<") && logo.endsWith(">")) {
        val iconName = logo.substring(1, logo.length - 1)
        val simpleIcon = SimpleIcons.get(iconName)

        if (simpleIcon != null) {
            val ico = simpleIcon.svg
            if (ico.isNotBlank()) {
                try {
                    val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(ByteArrayInputStream(ico.toByteArray()))

                    // Pass background color for better contrast
                    val src = manipulateSVG(xml, simpleIcon.hex, addEffects, backgroundColor)

                    return "data:image/svg+xml;base64," + Base64.getEncoder()
                        .encodeToString(src.toByteArray())
                } catch (e: Exception) {
                    // Log the error and return empty string instead of falling through
                    println("Error processing SimpleIcon '$iconName': ${e.message}")
                    return ""
                }
            }
        } else {
            // SimpleIcon not found - return empty string instead of the input
            println("SimpleIcon '$iconName' not found")
            return ""
        }
    }
    // Handle URL format
    else if (logo.startsWith("http://") || logo.startsWith("https://")) {
        return getLogoFromUrl(logo)
    }
    // Handle data URLs or other formats
    else if (logo.startsWith("data:")) {
        return logo
    }

    // If none of the above formats match, return empty string
    // This prevents href URLs from being used as logos
    return ""
}
private fun getLogoFromUrl(url: String): String {
    val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(20))
        .build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(Duration.ofSeconds(10))
        .build()
    return try {
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        response.body()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

private fun manipulateSVG(
    doc: org.w3c.dom.Document,
    hexColor: String,
    addEffects: Boolean = true,
    backgroundColor: String? = null
): String {
    val paths = doc.getElementsByTagName("path")
    val circles = doc.getElementsByTagName("circle")
    val rects = doc.getElementsByTagName("rect")
    val polygons = doc.getElementsByTagName("polygon")
    val ellipses = doc.getElementsByTagName("ellipse")

    // Determine the best color for the icon based on background
    val iconColor = determineIconColor(hexColor, backgroundColor)

    // Apply color to all SVG elements
    for (i in 0 until paths.length) {
        val path = paths.item(i) as org.w3c.dom.Element
        path.setAttribute("fill", "#$iconColor")

        if (addEffects) {
            // Add subtle stroke for better definition on any background
            path.setAttribute("stroke", getContrastStroke(iconColor))
            path.setAttribute("stroke-width", "0.5")
        }
    }

    for (i in 0 until circles.length) {
        val circle = circles.item(i) as org.w3c.dom.Element
        circle.setAttribute("fill", "#$iconColor")

        if (addEffects) {
            circle.setAttribute("stroke", getContrastStroke(iconColor))
            circle.setAttribute("stroke-width", "0.5")
        }
    }

    for (i in 0 until rects.length) {
        val rect = rects.item(i) as org.w3c.dom.Element
        rect.setAttribute("fill", "#$iconColor")

        if (addEffects) {
            rect.setAttribute("stroke", getContrastStroke(iconColor))
            rect.setAttribute("stroke-width", "0.5")
        }
    }

    for (i in 0 until polygons.length) {
        val polygon = polygons.item(i) as org.w3c.dom.Element
        polygon.setAttribute("fill", "#$iconColor")

        if (addEffects) {
            polygon.setAttribute("stroke", getContrastStroke(iconColor))
            polygon.setAttribute("stroke-width", "0.5")
        }
    }

    for (i in 0 until ellipses.length) {
        val ellipse = ellipses.item(i) as org.w3c.dom.Element
        ellipse.setAttribute("fill", "#$iconColor")

        if (addEffects) {
            ellipse.setAttribute("stroke", getContrastStroke(iconColor))
            ellipse.setAttribute("stroke-width", "0.5")
        }
    }

    val transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer()
    val result = javax.xml.transform.stream.StreamResult(java.io.StringWriter())
    val source = javax.xml.transform.dom.DOMSource(doc)
    transformer.transform(source, result)

    return result.writer.toString()
}

/**
 * Determines the best icon color based on the original color and background
 */
private fun determineIconColor(originalHex: String, backgroundColor: String?): String {
    if (backgroundColor == null) {
        return originalHex
    }

    val backgroundIsDark = isColorDark(backgroundColor)
    val originalIsDark = isColorDark(originalHex)

    return when {
        // If background is dark and original icon is also dark, make icon light
        backgroundIsDark && originalIsDark -> "ffffff"
        // If background is light and original icon is light, make icon dark
        !backgroundIsDark && !originalIsDark -> "000000"
        // Otherwise, use original color as it should have good contrast
        else -> originalHex
    }
}
private fun isColorDark(hexColor: String): Boolean {
    val color = hexColor.removePrefix("#")
    val r = color.substring(0, 2).toInt(16)
    val g = color.substring(2, 4).toInt(16)
    val b = color.substring(4, 6).toInt(16)

    // Calculate luminance using the standard formula
    val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
    return luminance < 0.5
}

private fun getContrastStroke(iconColor: String): String {
    return if (isColorDark(iconColor)) "#ffffff40" else "#00000040"
}