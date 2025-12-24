package io.docops.docopsextensionssupport.domainviz

import kotlinx.serialization.json.Json
import kotlin.collections.get

class DiagramParser {

    fun parseJson(json: String): DiagramData {
        val input = Json.decodeFromString<DiagramJsonInput>(json)
        return convertToDiagramDataFromOriginal(input)
    }


    fun parseCSV(csv: String): DiagramData {
        val lines = csv.lines().filter { it.isNotBlank() }
        if (lines.size < 2) throw IllegalArgumentException("CSV must have at least 2 lines")

        // Handle the case where first line is "main,QUOTE" format or has header
        var mainNode = "QUOTE" // default
        var mainLinks = emptyList<WikiLink>()
        var dataStartIndex = 1 // skip header by default

        var useNeural = false

        // Check if first line is the old format "main,QUOTE"
        if (lines[0].lowercase().startsWith("main,")) {
            val parts = lines[0].split(",")
            // Check for useNeural=true in any part of the main line
            useNeural = parts.any { it.trim().lowercase() == "useneural=true" }

            if (parts.size >= 2) {
                mainNode = parts[1].trim()
                // Check for links in main node definition
                if (parts.size >= 3) {
                    mainLinks = parseWikiLinks(parts[2])
                }
            }
            dataStartIndex = 2 // skip both main line and header
        } else {
            // Look for MAIN row in the data
            val mainNodeLine = lines.drop(1).find { it.startsWith("MAIN,") }
            if (mainNodeLine != null) {
                val mainNodeParts = parseCSVRow(mainNodeLine)
                if (mainNodeParts.size >= 4) {
                    mainNode = mainNodeParts[3] // The node name is in the 4th column
                    if (mainNodeParts.size >= 5) {
                        mainLinks = parseWikiLinks(mainNodeParts[4])
                    }
                }
            }
        }

        val commonRows = mutableListOf<List<Pair<String, List<WikiLink>>>>()
        val specializedGroupsMap = mutableMapOf<String, MutableMap<Int, Triple<String?, List<String>, List<List<WikiLink>>>>>()

        lines.drop(dataStartIndex).forEach { line -> // Skip header and/or main line
            if (line.startsWith("MAIN,") || line.lowercase().startsWith("type,")) return@forEach // Skip main node line and header

            val parts = parseCSVRow(line)
            if (parts.size < 4) return@forEach // Skip invalid lines

            val type = parts[0]
            val emoji = parts[1].takeIf { it.isNotBlank() }
            val rowIndex = parts[2].toIntOrNull() ?: return@forEach
            val nodesString = parts[3].trim('"')

            // Parse nodes and extract embedded links
            val (nodes, nodeLinks) = parseNodesWithEmbeddedLinks(nodesString)

            // Parse links from separate links column if present (5th column)
            val separateLinksString = if (parts.size >= 5) parts[4].trim('"') else ""
            val separateNodeLinks = if (separateLinksString.isNotEmpty()) {
                separateLinksString.split(",").map { parseWikiLinks(it.trim()) }
            } else {
                // If no separate links column, use empty lists for nodes without embedded links
                nodes.indices.map { emptyList<WikiLink>() }
            }

            // Combine embedded links with separate links (embedded links take precedence)
            val finalNodeLinks = nodes.indices.map { index ->
                if (nodeLinks[index].isNotEmpty()) {
                    nodeLinks[index]
                } else if (index < separateNodeLinks.size) {
                    separateNodeLinks[index]
                } else {
                    emptyList<WikiLink>()
                }
            }

            if (type == "COMMON") {
                while (commonRows.size <= rowIndex) {
                    commonRows.add(emptyList())
                }
                commonRows[rowIndex] = nodes.zip(finalNodeLinks)
            } else {
                specializedGroupsMap.getOrPut(type) { mutableMapOf() }[rowIndex] = Triple(emoji, nodes, finalNodeLinks)
            }
        }

        val specializedGroups = specializedGroupsMap.map { (title, rowsMap) ->
            val emoji = rowsMap.values.firstOrNull()?.first ?: ""
            val rows = rowsMap.entries.sortedBy { it.key }.map {
                it.value.second.zip(it.value.third)
            }
            SpecializedGroupInputWithLinks(title, emoji, rows)
        }

        val data =  convertToDiagramDataWithLinks(DiagramJsonInputWithLinks(mainNode, mainLinks, commonRows, specializedGroups))
        data.useNeural = useNeural
        return data
    }

    /**
     * Parse nodes string and extract embedded wiki links
     * Returns a Pair of (nodes, links) where links[i] are the links for nodes[i]
     */
    private fun parseNodesWithEmbeddedLinks(nodesString: String): Pair<List<String>, List<List<WikiLink>>> {
        // Split by semicolons first, then commas for backward compatibility
        val nodeParts = if (nodesString.contains(";")) {
            nodesString.split(";").map { it.trim() }
        } else {
            nodesString.split(",").map { it.trim() }
        }

        val nodes = mutableListOf<String>()
        val nodeLinks = mutableListOf<List<WikiLink>>()

        for (nodePart in nodeParts) {
            if (nodePart.isEmpty()) continue

            // Check if this node part contains wiki links
            val links = parseWikiLinks(nodePart)

            if (links.isNotEmpty()) {
                // Extract the node name by removing the wiki link syntax and using the label
                val cleanNodeName = removeWikiLinksFromText(nodePart, links)
                nodes.add(cleanNodeName.trim())
                nodeLinks.add(links)
            } else {
                // Regular node without links
                nodes.add(nodePart)
                nodeLinks.add(emptyList())
            }
        }

        return Pair(nodes, nodeLinks)
    }

    /**
     * Remove wiki link syntax from text and replace with labels
     */
    private fun removeWikiLinksFromText(text: String, links: List<WikiLink>): String {
        var result = text
        val linkPattern = "\\[\\[([^\\s]+)\\s+(.*?)\\]\\]".toRegex()

        // Replace each wiki link with its label
        linkPattern.findAll(text).forEach { matchResult ->
            val fullMatch = matchResult.value
            val label = matchResult.groupValues[2]
            result = result.replace(fullMatch, label)
        }

        return result
    }
    /**
     * Extract wiki links from a string.
     * Format: [[url label]]
     */
    private fun parseWikiLinks(text: String): List<WikiLink> {
        val links = mutableListOf<WikiLink>()
        val linkPattern = "\\[\\[([^\\s]+)\\s+(.*?)\\]\\]".toRegex()
        linkPattern.findAll(text).forEach { matchResult ->
            val (url, label) = matchResult.destructured
            links.add(WikiLink(url, label.trim()))
        }
        return links
    }

    private fun parseCSVRow(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' && (i == 0 || line[i-1] != '\\') -> {
                    inQuotes = !inQuotes
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }

        result.add(current.toString().trim())
        return result
    }

    // Convert original format without links
    private fun convertToDiagramDataFromOriginal(input: DiagramJsonInput): DiagramData {
        val mainNode = DiagramNode(input.mainNode, isMainNode = true)

        val commonRows = input.commonRows.map { row ->
            row.map { title -> DiagramNode(title) }
        }

        val specializedGroups = input.specializedGroups.map { group ->
            val rows = group.rows.map { row ->
                row.map { title -> DiagramNode(title, emoji = group.emoji) }
            }
            SpecializedGroup(group.title, group.emoji, rows)
        }

        return DiagramData(mainNode, commonRows, specializedGroups)
    }

    // Convert format with links
    private fun convertToDiagramDataWithLinks(input: DiagramJsonInputWithLinks): DiagramData {
        val mainNode = DiagramNode(input.mainNode, links = input.mainLinks, isMainNode = true)

        val commonRows = input.commonRows.map { row ->
            row.map { (title, links) -> DiagramNode(title, links = links) }
        }

        val specializedGroups = input.specializedGroups.map { group ->
            val rows = group.rows.map { row ->
                row.map { (title, links) -> DiagramNode(title, emoji = group.emoji, links = links) }
            }
            SpecializedGroup(group.title, group.emoji, rows)
        }

        return DiagramData(mainNode, commonRows, specializedGroups)
    }
}

data class DiagramJsonInput(
    val mainNode: String,
    val commonRows: List<List<String>>,
    val specializedGroups: List<SpecializedGroupInput>
)

data class SpecializedGroupInput(
    val title: String,
    val emoji: String,
    val rows: List<List<String>>
)