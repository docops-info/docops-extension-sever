package io.docops.docopsextensionssupport.domain

import io.docops.docopsextensionssupport.domain.model.DomainElement
import org.springframework.stereotype.Service
import kotlin.math.min

@Service
class SvgGenerator {

    fun generateSvg(structure: List<DomainElement>, useGradients: Boolean = false, useGlass: Boolean = false): String {
        val padding = 20
        val domainWidth = 300
        val domainHeight = 50
        val subdomainHeight = 40
        val itemHeight = 30
        val spacing = 10

        // Calculate dimensions
        var totalWidth = 0
        var x = padding
        var maxDomainHeight = 0
        var separatorHeight = 0

        for (element in structure) {
            when (element) {
                is DomainElement.Separator -> {
                    separatorHeight += 20
                }
                is DomainElement.Domain -> {
                    var domainTotalHeight = domainHeight + spacing

                    if (element.subdomains.isNotEmpty()) {
                        var totalSubdomainHeight = 0
                        for (subdomain in element.subdomains) {
                            var subdomainTotalHeight = subdomainHeight + spacing
                            if (subdomain.items.isNotEmpty()) {
                                subdomainTotalHeight += subdomain.items.size * (itemHeight + spacing)
                            }
                            totalSubdomainHeight += subdomainTotalHeight
                        }
                        domainTotalHeight += totalSubdomainHeight
                    }

                    // Track the maximum height needed by any domain
                    maxDomainHeight = maxOf(maxDomainHeight, domainTotalHeight)
                    x += domainWidth + spacing
                }

                is DomainElement.Item -> TODO()
                is DomainElement.Subdomain -> TODO()
            }
        }

        totalWidth = x
        // Calculate total height: starting y position (padding + separators) + maximum domain height + bottom padding
        val totalHeight = padding + separatorHeight + maxDomainHeight + padding

        // Generate SVG
        val svg = StringBuilder()
        svg.append("<svg width=\"$totalWidth\" height=\"$totalHeight\" xmlns=\"http://www.w3.org/2000/svg\">")

        // Add definitions
        svg.append("<defs>")
        // Add glass effect filter
        if (useGlass) {
            svg.append("""
            <filter id="glass" x="-10%" y="-10%" width="120%" height="120%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="2" result="blur"/>
                <feOffset in="blur" dx="0" dy="0" result="offsetBlur"/>
                <feComponentTransfer in="offsetBlur" result="compTransfer">
                    <feFuncA type="linear" slope="0.5"/>
                </feComponentTransfer>
                <feComposite in="SourceGraphic" in2="compTransfer" operator="over" result="composite"/>
                
                <!-- Light reflection on top -->
                <feSpecularLighting in="composite" surfaceScale="3" specularConstant=".75" 
                                   specularExponent="20" lighting-color="#white" result="specOut">
                    <fePointLight x="-5000" y="-10000" z="20000"/>
                </feSpecularLighting>
                <feComposite in="specOut" in2="composite" operator="in" result="specOut2"/>
                <feComposite in="composite" in2="specOut2" operator="arithmetic" 
                            k1="0" k2="1" k3="1" k4="0" result="final"/>
            </filter>
        """.trimIndent())
        }

        svg.append("""
        <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
            <feDropShadow dx="2" dy="2" stdDeviation="3" flood-color="rgba(0,0,0,0.3)"/>
        </filter>
        <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#667eea;stop-opacity:1" />
            <stop offset="100%" style="stop-color:#764ba2;stop-opacity:1" />
        </linearGradient>
    """.trimIndent())

        // Add gradient definitions for each unique color
        val uniqueColors = mutableSetOf<String>()
        structure.forEach { element ->
            when (element) {
                is DomainElement.Domain -> {
                    uniqueColors.add(element.color)
                    element.subdomains.forEach { subdomain ->
                        uniqueColors.add(subdomain.color)
                        subdomain.items.forEach { item ->
                            uniqueColors.add(item.color)
                        }
                    }
                }
                else -> {}
            }
        }

        // Create a gradient for each color
        uniqueColors.forEachIndexed { index, color ->
            // Create a slightly lighter variant of the color for gradient end
            val lighterColor = createLighterColor(color)
            svg.append("""
            <linearGradient id="gradient-$index" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" style="stop-color:$color;stop-opacity:1" />
                <stop offset="100%" style="stop-color:$lighterColor;stop-opacity:1" />
            </linearGradient>
        """.trimIndent())
        }

        svg.append("</defs>")

        // Background
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"url(#grad1)\" opacity=\"0.1\"/>")

        // Draw elements
        x = padding
        var y = padding

        for (element in structure) {
            when (element) {
                is DomainElement.Separator -> {
                    // Separator code...
                }
                is DomainElement.Domain -> {
                    // Determine fill
                    val domainFill = if (useGradients) {
                        "url(#gradient-${uniqueColors.indexOf(element.color)})"
                    } else {
                        element.color
                    }

                    // Determine filter
                    val domainFilter = if (useGlass) "filter=\"url(#glass)\"" else "filter=\"url(#shadow)\""

                    // Glass effect typically needs higher opacity to see through
                    val opacity = if (useGlass) "0.85" else "1.0"

                    svg.append("<rect x=\"$x\" y=\"$y\" width=\"$domainWidth\" height=\"$domainHeight\" " +
                            "fill=\"$domainFill\" stroke=\"white\" stroke-width=\"2\" rx=\"8\" " +
                            "$domainFilter opacity=\"$opacity\"/>")

                    // Make text more visible on glass
                    val textColor = if (useGlass) "black" else "white"
                    svg.append("<text x=\"${x + domainWidth/2}\" y=\"${y + domainHeight/2 + 5}\" " +
                            "text-anchor=\"middle\" fill=\"$textColor\" font-family=\"Arial, sans-serif\" " +
                            "font-size=\"16\" font-weight=\"bold\">${escapeHtml(element.title)}</text>")

                    var subY = y + domainHeight + spacing

                    for (subdomain in element.subdomains) {
                        // Similar modifications for subdomains
                        val subdomainFill = if (useGradients) {
                            "url(#gradient-${uniqueColors.indexOf(subdomain.color)})"
                        } else {
                            subdomain.color
                        }

                        svg.append("<rect x=\"$x\" y=\"$subY\" width=\"$domainWidth\" height=\"$subdomainHeight\" " +
                                "fill=\"$subdomainFill\" stroke=\"white\" stroke-width=\"1\" rx=\"5\" " +
                                "$domainFilter opacity=\"$opacity\"/>")

                        svg.append("<text x=\"${x + domainWidth/2}\" y=\"${subY + subdomainHeight/2 + 4}\" " +
                                "text-anchor=\"middle\" fill=\"$textColor\" font-family=\"Arial, sans-serif\" " +
                                "font-size=\"14\" font-weight=\"600\">${escapeHtml(subdomain.title)}</text>")

                        subY += subdomainHeight + spacing

                        for (item in subdomain.items) {
                            // Similar modifications for items
                            val itemFill = if (useGradients) {
                                "url(#gradient-${uniqueColors.indexOf(item.color)})"
                            } else {
                                item.color
                            }

                            svg.append("<rect x=\"${x + 20}\" y=\"$subY\" width=\"${domainWidth - 40}\" height=\"$itemHeight\" " +
                                    "fill=\"$itemFill\" stroke=\"white\" stroke-width=\"1\" rx=\"3\" " +
                                    "$domainFilter opacity=\"$opacity\"/>")

                            svg.append("<text x=\"${x + domainWidth/2}\" y=\"${subY + itemHeight/2 + 4}\" " +
                                    "text-anchor=\"middle\" fill=\"$textColor\" font-family=\"Arial, sans-serif\" " +
                                    "font-size=\"12\">${escapeHtml(item.title)}</text>")

                            subY += itemHeight + spacing
                        }
                    }

                    x += domainWidth + spacing
                }

                is DomainElement.Item -> TODO()
                is DomainElement.Subdomain -> TODO()
            }
        }

        svg.append("</svg>")
        return svg.toString()

    }

    // Helper function to create a lighter version of a color for the gradient
    private fun createLighterColor(hexColor: String): String {
        // Parse the hex color
        val colorStr = hexColor.removePrefix("#")
        val r = Integer.parseInt(colorStr.substring(0, 2), 16)
        val g = Integer.parseInt(colorStr.substring(2, 4), 16)
        val b = Integer.parseInt(colorStr.substring(4, 6), 16)

        // Make it lighter (70% original color + 30% white)
        val lighterR = min(255.0, r + (255 - r) * 0.3).toInt()
        val lighterG = min(255.0, g + (255 - g) * 0.3).toInt()
        val lighterB = min(255.0, b + (255 - b) * 0.3).toInt()

        // Convert back to hex
        return String.format("#%02x%02x%02x", lighterR, lighterG, lighterB)
    }


    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
