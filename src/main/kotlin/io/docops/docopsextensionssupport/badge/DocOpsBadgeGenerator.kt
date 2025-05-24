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
import io.docops.docopsextensionssupport.support.gradientFromColor
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.github.sercasti.tracing.Traceable
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


    @Traceable
    fun createBadgeFromList(badges: MutableList<Badge>): Pair<String, Float> {

        val sb= StringBuilder()
        var x = 0f
        var xPos = 0f
        var rowCount = 0
        var rowNum = 0
        var width= 1f
        badges.forEachIndexed { i, badge ->
            val label = badge.label.escapeXml()
            val message = badge.message.escapeXml()
            val maskId = UUID.randomUUID().toString()

            val labelColor = SVGColor(badge.labelColor!!, "label_${maskId}")
            val messageColor = SVGColor(badge.messageColor!!, "message_${maskId}")
            //val clrMap = gradientFromColor(badge.labelColor!!)
            //val mMap = gradientFromColor(badge.messageColor!!)
            val grad = labelColor.linearGradient + messageColor.linearGradient
            var labelWidth = measureText(badge.label) * 100.0F
            val messageWidth = measureText(badge.message) * 100.0F
            var labelLink = label
            var messageLink = message
            badge.url?.let {
                if(it.isNotEmpty() && !badge.isPdf) {
                    labelLink = """<a href='${badge.url}' target='_blank'>$label</a>"""
                    messageLink = """<a href='${badge.url}' target='_blank'>$message</a>"""
                }
            }


            var startX = 50
            var textWidth = 0
            var img = ""
            badge.logo?.let {
                // Use enhanced logo handling with effects
                val logoSize = 100
                val logo = getBadgeLogo(it, logoSize, true)

                // Adjust positioning and spacing
                startX += 127
                labelWidth += 100
                textWidth = 49

                // Create enhanced image element with better positioning and aria-label
                img = """<image x='30' y='49' width='$logoSize' height='$logoSize' 
                       xlink:href='$logo' preserveAspectRatio='xMidYMid meet'
                       aria-label='${badge.label} logo'/>"""
            }
            val labelFill = "url(#label_${maskId})"
            val messageFill = "url(#message_${maskId})"
            val filterText = ""
            val maskText = "url(#a)"
            val mask = createMask(maskId, labelWidth, messageWidth, labelFill, filterText, messageFill, maskText)
            if(rowCount > BadgePerRow) {
                rowCount = 0
                rowNum++
                xPos = 0f
            } else {
                rowCount++
            }
            sb.append("<g transform='translate($xPos,${rowNum*21})'>")
            if(badge.isPdf) {
                val b = makeSVGForPDF(labelColor, messageColor, labelWidth, messageWidth, textWidth, startX, badge.fontColor, badge.message, messageLink, labelLink, img, label)
                sb.append(b)
            }
            else {
                val b = makeSvg(
                    labelColor, 
                    messageColor, 
                    labelWidth, 
                    messageWidth, 
                    textWidth, 
                    startX, 
                    badge.fontColor, 
                    badge.message, 
                    messageLink, 
                    labelLink, 
                    grad, 
                    mask, 
                    filterText, 
                    img, 
                    label,
                    useShadow = true,
                    useTopHighlight = true
                )
                sb.append(b)
            }
            sb.append("</g>")
            val position = (labelWidth + messageWidth + 200) / 10 + 1f
            x += position
            xPos += position
            width = maxOf(width, xPos)
        }
        return Pair(sb.toString(), width)
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

    private fun makeSVGForPDF(
        clrMap: SVGColor,
        mMap: SVGColor,
        labelWidth: Float,
        messageWidth: Float,
        textWidth: Int,
        startX: Int,
        fontColor: String,
        message: String,
        messageLink: String,
        labelLink: String,
        img: String,
        label : String
    ): String {
        val labelFill = clrMap.darker()
        val messageFill = mMap.darker()
        val filterText = ""
        val maskText = ""
        val mask = """
            <rect fill='$labelFill' width='${labelWidth + 100}' height='200' filter='$filterText'/>
            <rect fill='$messageFill' x='${labelWidth + 100}' width='${messageWidth + 100}' height='200' filter='$filterText'/>
            """
        //language=svg
        return """<svg xmlns='http://www.w3.org/2000/svg' role='img' xmlns:xlink="http://www.w3.org/1999/xlink" aria-label='$label: $message' width='${(labelWidth + messageWidth + 200) / 10}' height='20' viewBox='0 0 ${labelWidth + messageWidth + 200} 200'  >
             <title>$label: $message</title>
            $mask
            <g text-anchor='start' font-family="'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif" font-size='110'>
                <text x='$startX' y='138' textLength='${(labelWidth - 60) - textWidth}' fill="$fontColor" 
                     style='font-variant: small-caps; letter-spacing: 0.5px; font-weight: 500;'>$labelLink</text>
                <text x='${labelWidth + 155}' y='138' textLength='${messageWidth}' fill="$fontColor" 
                     style='font-variant: small-caps; letter-spacing: 0.5px; font-weight: 400;'>$messageLink</text>
            </g>
               $img
             </svg>
        """.trimIndent()
    }
    private fun makeSvg(
        clrMap: SVGColor,
        mMap: SVGColor,
        labelWidth: Float,
        messageWidth: Float,
        textWidth: Int,
        startX: Int,
        fontColor: String,
        message: String,
        messageLink: String,
        labelLink: String,
        grad: String,
        mask: String,
        filterText: String,
        img: String,
        label: String,
        useShadow: Boolean = true,
        useTopHighlight: Boolean = true
    ): String {
        // Create combined filter for badge elements
        val combinedFilter = if (useShadow) "filter='url(#DropShadow) $filterText'" else "filter='$filterText'"
        val highlightFilter = if (useTopHighlight) "filter='url(#TopHighlight)'" else ""

        // Check if badge has a link (for interactive elements)
        val hasLink = labelLink.contains("<a") || messageLink.contains("<a")

        //language=SVG
        // Generate enhanced accessibility attributes
        val accessibilityAttrs = createAccessibilityAttributes(label, message, hasLink)

        val svgContent = """
            <svg width='${(labelWidth + messageWidth + 200) / 10}' height='20' viewBox='0 0 ${labelWidth + messageWidth + 200} 200'  
                 xmlns='http://www.w3.org/2000/svg' xmlns:xlink="http://www.w3.org/1999/xlink" 
                 $accessibilityAttrs
                 class="${if (hasLink) "badge-link" else ""}">
             <title>$label: $message</title>
             <desc>Badge showing $label with value $message</desc>
             <defs>
                ${createFilterDefinitions(useShadow, useTopHighlight)}
                <linearGradient id='a' x2='0' y2='100%'>
                    <stop offset='0' stop-opacity='.1' stop-color='#EEE'/>
                    <stop offset='1' stop-opacity='.1'/>
                </linearGradient>
                $grad
             </defs>

             <!-- Theme and interactive styles -->
             ${createThemeStyles(clrMap.original(), mMap.original(), fontColor)}
             ${createInteractiveStyles(hasLink)}

            <!-- Badge shape with shadow effect -->
            <g ${if (hasLink) "class='badge-shape'" else ""} ${combinedFilter.replace("'", "\"")}>
                $mask
            </g>

            <!-- Text elements -->
            <g aria-hidden='true' text-anchor='start' font-family="'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif" 
               font-size='110' filter='url(#Bevel2)' ${if (hasLink) "class='badge-text'" else ""}>
                <text x='$startX' y='138' textLength='${(labelWidth - 60) - textWidth}' fill="$fontColor" 
                     style='font-variant: small-caps; letter-spacing: 0.5px; font-weight: 500;'>$labelLink</text>
                <text x='${labelWidth + 155}' y='138' textLength='${messageWidth}' fill="$fontColor" 
                     style='font-variant: small-caps; letter-spacing: 0.5px; font-weight: 400;'>$messageLink</text>
            </g>

            <!-- Logo/icon with highlight effect -->
            <g ${highlightFilter.replace("'", "\"")} ${if (hasLink) "class='badge-logo'" else ""}>
                $img
            </g>
             </svg>
        """.trimIndent()

        // Apply SVG optimization (basic cleanup for all SVGs, more aggressive minification for production)
        // For development, use minimal optimization to keep readability
        return optimizeSvg(svgContent, minify = false)
    }
    /**
     * Creates an SVG badge with the specified label and message.
     *
     * @param iLabel The label text for the badge.
     * @param iMessage The message text for the badge.
     * @param labelColor The color of the label background. Default is #999999.
     * @param messageColor The color of the message background. Default is #ececec.
     * @param href The URL to link the label and message to. Default is empty string.
     * @param icon The icon image URL to display on the badge. Default is empty string.
     * @param fontColor The color of the label and message text. Default is #000000.
     *
     * @return The SVG representation of the badge.
     */
    @Traceable
    fun createBadge(
        iLabel: String,
        iMessage: String,
        labelColor: String = "#999999",
        messageColor: String = "#ececec",
        href: String = "",
        icon: String = "",
        fontColor: String = "#000000",
        backend: String = ""
    ): String {
        val isPdf = backend == "pdf"

        val label = iLabel.escapeXml()
        val message = iMessage.escapeXml()
        val clrMap = gradientFromColor(labelColor)
        val mMap = gradientFromColor(messageColor)
        val maskId = UUID.randomUUID().toString()
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
        var labelWidth = measureText(iLabel) * 100.0F
        val messageWidth = measureText(iMessage) * 100.0F
        var labelLink = label
        var messageLink = message
        if (href.isNotEmpty() && !isPdf) {
            labelLink = """<a href='$href' target='_blank'>$label</a>"""
            messageLink = """<a href='$href' target='_blank'>$message</a>"""
        }
        var startX = 50
        var textWidth = 0
        var img = ""
        if (icon.isNotEmpty()) {
            // Use enhanced logo handling with effects
            val logoSize = 100
            val logo = getBadgeLogo(icon, logoSize, true)

            // Adjust positioning and spacing
            startX += 127
            labelWidth += 100
            textWidth = 49

            // Create enhanced image element with better positioning and aria-label
            img = """<image x='30' y='49' width='$logoSize' height='$logoSize' 
                   xlink:href='$logo' preserveAspectRatio='xMidYMid meet'
                   aria-label='$label logo'/>"""
        }
        var labelFill = "url(#label_${maskId})"
        var messageFill = "url(#message_${maskId})"
        var filterText = "url(#Bevel2)"
        var maskText = "url(#a)"
        var mask = createMask(maskId, labelWidth, messageWidth, labelFill, filterText, messageFill, maskText)
        if(isPdf) {
            labelFill = clrMap["color3"]!!
            messageFill = mMap["color3"]!!
            filterText = ""
            maskText = ""
            mask = """
            <rect fill='$labelFill' width='${labelWidth + 100}' height='200' filter='$filterText'/>
            <rect fill='$messageFill' x='${labelWidth + 100}' width='${messageWidth + 100}' height='200' filter='$filterText'/>
            """
            //language=svg
            return """
            <svg width='${(labelWidth + messageWidth + 200) / 10}' height='20' viewBox='0 0 ${labelWidth + messageWidth + 200} 200' 
            xmlns='http://www.w3.org/2000/svg' role='img' xmlns:xlink="http://www.w3.org/1999/xlink" aria-label='$label: $message'>
             <title>$label: $message</title>
            $mask
            <g text-anchor='start' font-family="'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif" font-size='110'>
                <text x='$startX' y='138' textLength='${(labelWidth - 60) - textWidth}' fill="$fontColor" 
                     style='font-variant: small-caps; letter-spacing: 0.5px; font-weight: 500;'>$labelLink</text>
                <text x='${labelWidth + 155}' y='138' textLength='${messageWidth}' fill="$fontColor" 
                     style='font-variant: small-caps; letter-spacing: 0.5px; font-weight: 400;'>$messageLink</text>
            </g>
             </svg>
        """.trimIndent()
        }

        // Use the makeSvg method to create the badge
        return makeSvg(
            clrMap = SVGColor(labelColor),
            mMap = SVGColor(messageColor),
            labelWidth = labelWidth,
            messageWidth = messageWidth,
            textWidth = textWidth,
            startX = startX,
            fontColor = fontColor,
            message = message,
            messageLink = messageLink,
            labelLink = labelLink,
            grad = grad,
            mask = mask,
            filterText = filterText,
            img = img,
            label = label,
            useShadow = true,
            useTopHighlight = true
        )
    }

    private fun createMask(
        maskId: String,
        labelWidth: Float,
        messageWidth: Float,
        labelFill: String,
        filterText: String,
        messageFill: String,
        maskText: String,
        cornerRadius: Int = 30,
        addDivider: Boolean = true,
        dividerColor: String = "#ffffff20" // Semi-transparent white
    ): String {
        // Calculate total width
        val totalWidth = labelWidth + messageWidth + 200

        // Create mask with consistent rounded corners
        var mask = """
                <mask id='$maskId'>
                    <rect width='$totalWidth' height='200' rx='$cornerRadius' fill='#FFF'/>
                </mask>
                <g mask='url(#$maskId)'>
                    <!-- Label section with rounded left corners -->
                    <path class="badge-label" d="M0,0 h${labelWidth + 100} v200 h-${labelWidth + 100} v-200 z" 
                          fill='$labelFill' filter='$filterText'/>

                    <!-- Message section with rounded right corners -->
                    <path class="badge-message" d="M${labelWidth + 100},0 h${messageWidth + 100} v200 h-${messageWidth + 100} v-200 z" 
                          fill='$messageFill' filter='$filterText'/>

                    <!-- Overlay for consistent lighting -->
                    <rect width='$totalWidth' height='200' fill='$maskText' filter='$filterText'/>

                    ${if (addDivider) """
                    <!-- Subtle divider between sections -->
                    <line x1='${labelWidth + 100}' y1='40' x2='${labelWidth + 100}' y2='160' 
                          stroke='$dividerColor' stroke-width='2' stroke-opacity='0.7'/>
                    """ else ""}
                </g>
            """.trimIndent()
        return mask
    }

    fun measureText(str: String, fontSize : Int = 10): Float {
        var total = 0f
        str.codePoints().forEach {
                code ->
            total += when {
                code >= widths.size -> {
                    widths[64].toFloat()
                }
                else -> {
                    widths[code].toFloat()
                }
            }
        }
        return total
    }

    /**
     * Enhanced logo handling for badges
     * @param input The logo input string
     * @param size The desired size of the logo (default: 100)
     * @param addEffects Whether to add visual effects to the logo
     * @return The processed logo as a data URL or URL string
     */
    private fun getBadgeLogo(input: String?, size: Int = 100, addEffects: Boolean = true): String {
        // Default transparent 1x1 pixel as fallback
        var logo = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="

        input?.let {
            if (input.startsWith("<") && input.endsWith(">")) {
                // Handle SimpleIcons format (e.g. <github>)
                val iconName = input.replace("<", "").replace(">", "")
                val simpleIcon = SimpleIcons.get(iconName)

                if (simpleIcon != null) {
                    val ico = simpleIcon.svg
                    try {
                        val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                            .parse(ByteArrayInputStream(ico?.toByteArray()))
                        var src = ""
                        xml?.let {
                            // Apply enhanced SVG manipulation with original color
                            src = manipulateSVG(xml, simpleIcon.hex, addEffects)
                        }
                        logo = "data:image/svg+xml;base64," + Base64.getEncoder()
                            .encodeToString(src.toByteArray())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else if (input.startsWith("http")) {
                // External URL - use as is
                logo = input
            } else {
                // Assume it's a data URL or direct path
                logo = input
            }
        }

        return logo
    }

    /**
     * Enhanced SVG manipulation for better icon appearance
     * @param doc The XML document containing the SVG
     * @param hexColor The hex color to use for the icon
     * @param addEffects Whether to add visual effects to the icon
     * @return The processed SVG as a string
     */
    private fun manipulateSVG(doc: org.w3c.dom.Document, hexColor: String, addEffects: Boolean = true): String {
        val svg = doc.documentElement

        // Ensure viewBox is set for proper scaling
        if (!svg.hasAttribute("viewBox") && svg.hasAttribute("width") && svg.hasAttribute("height")) {
            val width = svg.getAttribute("width").replace("px", "").toFloatOrNull() ?: 24f
            val height = svg.getAttribute("height").replace("px", "").toFloatOrNull() ?: 24f
            svg.setAttribute("viewBox", "0 0 $width $height")
        }

        // Set fill color for all paths
        val paths = svg.getElementsByTagName("path")
        for (i in 0 until paths.length) {
            val path = paths.item(i)
            path.attributes.getNamedItem("fill")?.nodeValue = "#$hexColor"
        }

        // Add effects if requested
        if (addEffects) {
            // Add filter element for subtle shadow/glow using DOM API
            val defs = doc.createElement("defs")

            val filter = doc.createElement("filter")
            filter.setAttribute("id", "iconEffect")
            filter.setAttribute("x", "-10%")
            filter.setAttribute("y", "-10%")
            filter.setAttribute("width", "120%")
            filter.setAttribute("height", "120%")

            val blur = doc.createElement("feGaussianBlur")
            blur.setAttribute("stdDeviation", "0.5")
            blur.setAttribute("result", "blur")

            val colorMatrix = doc.createElement("feColorMatrix")
            colorMatrix.setAttribute("in", "blur")
            colorMatrix.setAttribute("type", "matrix")
            colorMatrix.setAttribute("values", "1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 18 -7")
            colorMatrix.setAttribute("result", "glow")

            val blend = doc.createElement("feBlend")
            blend.setAttribute("in", "SourceGraphic")
            blend.setAttribute("in2", "glow")
            blend.setAttribute("mode", "normal")

            filter.appendChild(blur)
            filter.appendChild(colorMatrix)
            filter.appendChild(blend)
            defs.appendChild(filter)

            svg.appendChild(defs)

            // Apply filter to the SVG
            svg.setAttribute("filter", "url(#iconEffect)")
        }

        // Ensure proper dimensions
        svg.setAttribute("width", "100%")
        svg.setAttribute("height", "100%")

        // Convert back to string
        val transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer()
        val source = javax.xml.transform.dom.DOMSource(doc)
        val result = javax.xml.transform.stream.StreamResult(java.io.StringWriter())
        transformer.transform(source, result)

        return (result.writer as java.io.StringWriter).toString()
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
     * Creates theme-aware styles that adapt to dark/light mode
     * @param labelColor The label color
     * @param messageColor The message color
     * @param fontColor The font color
     * @return Theme-aware styles as a string
     */
    private fun createThemeStyles(labelColor: String, messageColor: String, fontColor: String): String {
        return """
            <style>
                /* Theme-aware styles */
                @media (prefers-color-scheme: dark) {
                    .badge-shape .badge-label {
                        filter: brightness(0.85);
                    }
                    .badge-shape .badge-message {
                        filter: brightness(0.85);
                    }
                    .badge-text {
                        filter: contrast(1.1);
                    }
                }

                @media (prefers-color-scheme: light) {
                    .badge-shape .badge-label {
                        filter: brightness(1.05);
                    }
                    .badge-shape .badge-message {
                        filter: brightness(1.05);
                    }
                }

                /* High contrast mode support */
                @media (forced-colors: active) {
                    .badge-shape .badge-label, 
                    .badge-shape .badge-message {
                        forced-color-adjust: none;
                    }
                    .badge-text {
                        forced-color-adjust: auto;
                    }
                }
            </style>
        """.trimIndent()
    }

    /**
     * Creates CSS styles for interactive elements like hover effects and animations
     * @param hasLink Whether the badge has a link
     * @return CSS styles as a string
     */
    private fun createInteractiveStyles(hasLink: Boolean = false): String {
        if (!hasLink) return "" // Only add styles if there's a link

        return """
            <style>
                /* Hover effect for linked badges */
                a:hover {
                    opacity: 0.9;
                    transition: opacity 0.3s ease;
                }

                /* Scale effect on hover */
                .badge-link:hover {
                    transform: scale(1.02);
                    transition: transform 0.2s ease-in-out;
                }

                /* Text highlight effect */
                .badge-text:hover {
                    filter: brightness(1.1);
                    transition: filter 0.3s ease;
                }

                /* Ensure animations degrade gracefully */
                @media (prefers-reduced-motion: reduce) {
                    a:hover, .badge-link:hover, .badge-text:hover {
                        transition: none;
                        transform: none;
                        filter: none;
                    }
                }
            </style>
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
            <!-- Improved bevel effect for subtle 3D appearance -->
            <filter id="Bevel" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="2" result="blur"/>
                <feSpecularLighting in="blur" surfaceScale="4" specularConstant="0.6" specularExponent="12" result="specOut" lighting-color="white">
                    <fePointLight x="-5000" y="-10000" z="15000"/>
                </feSpecularLighting>
                <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
            </filter>

            <!-- Subtle bevel effect for text and small elements -->
            <filter id="Bevel2" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="0.4" result="blur"/>
                <feSpecularLighting in="blur" surfaceScale="4" specularConstant="0.6" specularExponent="12" result="specOut" lighting-color="white">
                    <fePointLight x="-5000" y="-10000" z="10000"/>
                </feSpecularLighting>
                <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
            </filter>

            <!-- Enhanced bevel with stronger highlight -->
            <filter id="Bevel3" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="0.3" result="blur"/>
                <feSpecularLighting in="blur" surfaceScale="8" specularConstant="2.5" specularExponent="12" result="specOut" lighting-color="#ffffff">
                  <fePointLight x="-5000" y="-10000" z="8000"/>
                </feSpecularLighting>
                <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
            </filter>
            ${if (useShadow) """
            <!-- Subtle drop shadow for depth -->
            <filter id="DropShadow" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="1.5" result="blur"/>
                <feOffset in="blur" dx="1" dy="1" result="offsetBlur"/>
                <feFlood flood-color="#000000" flood-opacity="0.2" result="shadowColor"/>
                <feComposite in="shadowColor" in2="offsetBlur" operator="in" result="shadowBlur"/>
                <feComposite in="SourceGraphic" in2="shadowBlur" operator="over"/>
            </filter>
            """ else ""}
            ${if (useTopHighlight) """
            <!-- Top edge highlight for a polished look -->
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

            // Remove comments
            result = result.replace(Regex("<!--.*?-->"), "")

            // Remove unnecessary attributes
            result = result.replace(Regex("version=\"1\\.1\""), "")
                           .replace(Regex("xmlns:xlink=\"http://www\\.w3\\.org/1999/xlink\"\\s+"), "")

            // Shorten color values where possible
            result = result.replace(Regex("#([0-9a-f])\\1([0-9a-f])\\2([0-9a-f])\\3"), "#$1$2$3")

            // Remove leading zeros in values
            result = result.replace(Regex("([\\s:])0\\.([0-9])"), "$1.$2")

            // Optimize path data
            result = result.replace(Regex("([\\d])\\.0([\\D])"), "$1$2")
        }

        return result
    }

    companion object {
        val widths = arrayOf<Number>(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.1316650390625,0.42833404541015624,0.5066665649414063,0.7066665649414062,0.7066665649414062,1.0383331298828125,0.8183334350585938,0.34499969482421877,0.4850006103515625,0.4850006103515625,0.5383331298828125,0.7350006103515625,0.42833404541015624,0.4850006103515625,0.42833404541015624,0.42833404541015624,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.42833404541015624,0.42833404541015624,0.7350006103515625,0.7350006103515625,0.7350006103515625,0.7066665649414062,1.1649993896484374,0.8199996948242188,0.8183334350585938,0.8716659545898438,0.8716659545898438,0.8183334350585938,0.7633331298828125,0.9316665649414062,0.8716659545898438,0.42833404541015624,0.65,0.8183334350585938,0.7066665649414062,0.9833328247070312,0.8716659545898438,0.9316665649414062,0.8183334350585938,0.9316665649414062,0.8716659545898438,0.8183334350585938,0.7633331298828125,0.8716659545898438,0.8183334350585938,1.0949996948242187,0.8183334350585938,0.8183334350585938,0.7633331298828125,0.42833404541015624,0.42833404541015624,0.42833404541015624,0.6199996948242188,0.7349990844726563,0.4850006103515625,0.7066665649414062,0.7066665649414062,0.65,0.7066665649414062,0.7066665649414062,0.4633331298828125,0.7066665649414062,0.7066665649414062,0.375,0.42166748046875,0.65,0.375,0.9833328247070312,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.498333740234375,0.65,0.42833404541015624,0.7066665649414062,0.65,0.8716659545898438,0.65,0.65,0.65,0.4850006103515625,0.4100006103515625,0.4850006103515625,0.7350006103515625)
    }
}
