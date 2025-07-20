package io.docops.docopsextensionssupport.domain

import io.docops.docopsextensionssupport.domain.model.DomainElement
import org.springframework.stereotype.Service

@Service
class SvgGenerator {

    fun generateSvg(structure: List<DomainElement>): String {
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
        svg.append("""
            <defs>
                <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="2" dy="2" stdDeviation="3" flood-color="rgba(0,0,0,0.3)"/>
                </filter>
                <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#667eea;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#764ba2;stop-opacity:1" />
                </linearGradient>
            </defs>
        """.trimIndent())

        // Background
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"url(#grad1)\" opacity=\"0.1\"/>")

        // Draw elements
        x = padding
        var y = padding

        for (element in structure) {
            when (element) {
                is DomainElement.Separator -> {
                    svg.append("<line x1=\"$padding\" y1=\"$y\" x2=\"${totalWidth - padding}\" y2=\"$y\" stroke=\"#bdc3c7\" stroke-width=\"2\" stroke-dasharray=\"5,5\"/>")
                    y += 20
                }
                is DomainElement.Domain -> {
                    // Draw domain
                    svg.append("<rect x=\"$x\" y=\"$y\" width=\"$domainWidth\" height=\"$domainHeight\" fill=\"${element.color}\" stroke=\"white\" stroke-width=\"2\" rx=\"8\" filter=\"url(#shadow)\"/>")
                    svg.append("<text x=\"${x + domainWidth/2}\" y=\"${y + domainHeight/2 + 5}\" text-anchor=\"middle\" fill=\"white\" font-family=\"Arial, sans-serif\" font-size=\"16\" font-weight=\"bold\">${escapeHtml(element.title)}</text>")

                    var subY = y + domainHeight + spacing

                    for (subdomain in element.subdomains) {
                        // Draw subdomain
                        svg.append("<rect x=\"$x\" y=\"$subY\" width=\"$domainWidth\" height=\"$subdomainHeight\" fill=\"${subdomain.color}\" stroke=\"white\" stroke-width=\"1\" rx=\"5\" filter=\"url(#shadow)\"/>")
                        svg.append("<text x=\"${x + domainWidth/2}\" y=\"${subY + subdomainHeight/2 + 4}\" text-anchor=\"middle\" fill=\"white\" font-family=\"Arial, sans-serif\" font-size=\"14\" font-weight=\"600\">${escapeHtml(subdomain.title)}</text>")

                        subY += subdomainHeight + spacing

                        for (item in subdomain.items) {
                            svg.append("<rect x=\"${x + 20}\" y=\"$subY\" width=\"${domainWidth - 40}\" height=\"$itemHeight\" fill=\"${item.color}\" stroke=\"white\" stroke-width=\"1\" rx=\"3\" filter=\"url(#shadow)\"/>")
                            svg.append("<text x=\"${x + domainWidth/2}\" y=\"${subY + itemHeight/2 + 4}\" text-anchor=\"middle\" fill=\"white\" font-family=\"Arial, sans-serif\" font-size=\"12\">${escapeHtml(item.title)}</text>")
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

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
