package io.docops.docopsextensionssupport.domainviz

import io.docops.docopsextensionssupport.web.CsvResponse


class DomainModels {
}

/**
 * Data class representing a wiki-style link.
 */
data class WikiLink(
    val url: String,
    val label: String
)

data class DiagramNode(
    val title: String,
    val emoji: String? = null,
    val links: List<WikiLink> = emptyList(), // Added wiki links support
    val isMainNode: Boolean = false,
    val isSpecializedNode: Boolean = false,
    // Remove x, y - these will be calculated automatically
    val width: Double = 120.0,
    val height: Double = 40.0,
    // Add calculated position properties
    var x: Double = 0.0,
    var y: Double = 0.0
)

data class DiagramConnection(
    val fromNode: DiagramNode,
    val toNode: DiagramNode,
    val connectionType: ConnectionType = ConnectionType.SOLID
)

enum class ConnectionType {
    SOLID,
    DASHED
}

data class DiagramData(
    val mainNode: DiagramNode,
    val commonRows: List<List<DiagramNode>>, // Changed to rows instead of flat list
    val specializedGroups: List<SpecializedGroup>
) {
    fun toCsv(): CsvResponse {
        val headers = listOf("Type", "Emoji", "Row", "Nodes", "Links")
        val rows = mutableListOf<List<String>>()

        // Helper function to format links
        fun formatLinks(node: DiagramNode): String {
            return if (node.links.isEmpty()) "" else {
                node.links.joinToString(";") { "[[${it.url} ${it.label}]]" }
            }
        }

        // Add main node row
        rows.add(listOf("MAIN", mainNode.emoji ?: "", "0", mainNode.title, formatLinks(mainNode)))

        // Add common rows
        commonRows.forEachIndexed { rowIndex, nodeList ->
            val nodeNames = nodeList.joinToString(",") { it.title }
            val nodeLinks = nodeList.joinToString(",") { formatLinks(it) }
            rows.add(listOf("COMMON", "", rowIndex.toString(), "\"$nodeNames\"", "\"$nodeLinks\""))
        }

        // Add specialized groups
        specializedGroups.forEach { group ->
            group.rows.forEachIndexed { rowIndex, nodeList ->
                val nodeNames = nodeList.joinToString(",") { it.title }
                val nodeLinks = nodeList.joinToString(",") { formatLinks(it) }
                rows.add(listOf(group.title, group.emoji, rowIndex.toString(), "\"$nodeNames\"", "\"$nodeLinks\""))
            }
        }

        return CsvResponse(headers, rows)
    }

}
data class SpecializedGroup(
    val title: String,
    val emoji: String,
    val rows: List<List<DiagramNode>> // Changed to rows instead of flat list
)

data class DiagramJsonInputWithLinks(
    val mainNode: String,
    val mainLinks: List<WikiLink>,
    val commonRows: List<List<Pair<String, List<WikiLink>>>>,
    val specializedGroups: List<SpecializedGroupInputWithLinks>
)

data class SpecializedGroupInputWithLinks(
    val title: String,
    val emoji: String,
    val rows: List<List<Pair<String, List<WikiLink>>>>
)