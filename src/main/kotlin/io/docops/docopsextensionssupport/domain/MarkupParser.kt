package io.docops.docopsextensionssupport.domain

import io.docops.docopsextensionssupport.domain.model.DomainElement
import io.docops.docopsextensionssupport.web.CsvResponse
import org.springframework.stereotype.Service

@Service
class MarkupParser {

    private val colorPalette = mapOf(
        "blue" to "#3498db",
        "green" to "#2ecc71",
        "red" to "#e74c3c",
        "yellow" to "#f1c40f",
        "purple" to "#9b59b6",
        "orange" to "#e67e22",
        "pink" to "#ff6b9d",
        "gray" to "#95a5a6",
        "teal" to "#1abc9c",
        "indigo" to "#6c5ce7"
    )

    fun parseMarkup(markup: String): List<DomainElement> {
        val lines = markup.split('\n')
        val structure = mutableListOf<DomainElement>()
        var currentDomain: DomainElement.Domain? = null
        var currentSubdomain: DomainElement.Subdomain? = null

        for (line in lines) {
            val trimmedLine = line.trim()

            // Skip empty lines and comments
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("//")) continue

            // Horizontal separator
            if (trimmedLine == "---") {
                structure.add(DomainElement.Separator)
                continue
            }

            // Parse domain (# Title [color])
            val domainMatch = Regex("^# (.+?)(?:\\s*\\[([^\\]]+)\\])?$").find(trimmedLine)
            if (domainMatch != null) {
                val (title, color) = domainMatch.destructured
                currentDomain = DomainElement.Domain(
                    title = title.trim(),
                    color = parseColor(color)
                )
                structure.add(currentDomain)
                currentSubdomain = null
                continue
            }

            // Parse subdomain (## Title [color])
            val subdomainMatch = Regex("^## (.+?)(?:\\s*\\[([^\\]]+)\\])?$").find(trimmedLine)
            if (subdomainMatch != null) {
                val (title, color) = subdomainMatch.destructured
                currentSubdomain = DomainElement.Subdomain(
                    title = title.trim(),
                    color = parseColor(color)
                )
                currentDomain?.subdomains?.add(currentSubdomain)
                continue
            }

            // Parse sub-subdomain (### Title [color])
            val itemMatch = Regex("^### (.+?)(?:\\s*\\[([^\\]]+)\\])?$").find(trimmedLine)
            if (itemMatch != null) {
                val (title, color) = itemMatch.destructured
                val item = DomainElement.Item(
                    title = title.trim(),
                    color = parseColor(color)
                )
                currentSubdomain?.items?.add(item)
                continue
            }
        }

        return structure
    }

    private fun parseColor(colorStr: String): String {
        if (colorStr.isEmpty()) return "#3498db" // default blue

        val color = colorStr.trim().lowercase()

        // Check if it's a hex color
        if (color.startsWith("#")) {
            return color
        }

        // Check if it's a named color
        return colorPalette[color] ?: "#3498db"
    }
}

