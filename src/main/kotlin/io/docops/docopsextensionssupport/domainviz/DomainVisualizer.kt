package io.docops.docopsextensionssupport.domainviz

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.textWidth
import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DomainVisualizer

data class DiagramNode(
    val title: String,
    val emoji: String? = null,
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
        val headers = listOf("Type", "Emoji", "Row", "Nodes")
        val rows = mutableListOf<List<String>>()

        // Add main node row
        rows.add(listOf("MAIN", mainNode.emoji ?: "", "0", mainNode.title))

        // Add common rows
        commonRows.forEachIndexed { rowIndex, nodeList ->
            val nodeNames = nodeList.joinToString(",") { it.title }
            rows.add(listOf("COMMON", "", rowIndex.toString(), "\"$nodeNames\""))
        }

        // Add specialized groups
        specializedGroups.forEach { group ->
            group.rows.forEachIndexed { rowIndex, nodeList ->
                val nodeNames = nodeList.joinToString(",") { it.title }
                rows.add(listOf(group.title, group.emoji, rowIndex.toString(), "\"$nodeNames\""))
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



class SVGDiagramGenerator @OptIn(ExperimentalUuidApi::class)
    constructor(val useDark: Boolean = false,
    val id: String = Uuid.random().toHexDashString()) {
    companion object {
        private const val MAIN_NODE_Y = 20.0
        private const val ROW_HEIGHT = 60.0
        private const val NODE_SPACING = 20.0
        private const val COLUMN_WIDTH = 140.0
        private const val START_X = 180.0
        private const val START_Y = 100.0
    }

    @OptIn(ExperimentalUuidApi::class)
    fun generateSVG(data: DiagramData): String {
        // Calculate positions first
        calculatePositions(data)

        val svg = StringBuilder()
        // Compute dynamic SVG dimensions based on positioned nodes
        val (totalWidth, totalHeight) = computeCanvasSize(data)
        val backgroundColor = if (useDark) """
            <linearGradient id="backgroundGradient_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" style="stop-color:#1a1a2e;stop-opacity:1" />
                      <stop offset="100%" style="stop-color:#16213e;stop-opacity:1" />
            </linearGradient>""" else """
            <linearGradient id="backgroundGradient_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="#ffffff"/>
                <stop class="stop2" offset="50%" stop-color="#F8FAFC"/>
                <stop class="stop3" offset="100%" stop-color="#e2e8f0"/>
            </linearGradient>"""
        val glassColors = if (useDark) {
            """
                <!-- Glass Border (Dark Mode) -->
                <linearGradient id="glassBorder_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                    <stop offset="50%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
                </linearGradient>
                <!-- Apple Glass Effect Gradients (Dark Mode) -->
                <linearGradient id="glassOverlay_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.25);stop-opacity:1" />
                    <stop offset="30%" style="stop-color:rgba(255,255,255,0.15);stop-opacity:1" />
                    <stop offset="70%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0.02);stop-opacity:1" />
                </linearGradient>
            """
        } else {
            """
                <!-- Glass Border (Light Mode) -->
                <linearGradient id="glassBorder_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(0,122,255,0.4);stop-opacity:1" />
                    <stop offset="50%" style="stop-color:rgba(0,122,255,0.2);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(0,122,255,0.1);stop-opacity:1" />
                </linearGradient>
                <!-- Apple Glass Effect Gradients (Light Mode) -->
                <linearGradient id="glassOverlay_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.8);stop-opacity:1" />
                    <stop offset="30%" style="stop-color:rgba(255,255,255,0.6);stop-opacity:1" />
                    <stop offset="70%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                </linearGradient>
            """
        }

        val nodeStyles = if (useDark) {
            """
                #id_$id .node-rect { fill: #1f2937; stroke: #111827; stroke-width: 1; rx: 12; ry: 12; filter: url(#dropShadow); }
                #id_$id .main-node, .common-node, .specialized-node, .specialized-title { fill: #1f2937; stroke: #111827; stroke-width: 1; rx: 12; ry: 12; filter: url(#dropShadow); }
                #id_$id .node-text { fill: #e5e7eb; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; font-weight: 600; text-anchor: middle; }
                #id_$id .connection-line { stroke: #6b7280; stroke-width: 2; }
                #id_$id .dashed-line { stroke: #9ca3af; stroke-width: 2; stroke-dasharray: 6,6; }
                #id_$id .plus-symbol { fill: #6b7280; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; font-size: 28px; font-weight: 700; text-anchor: middle; filter: url(#dropShadow);}
            """
        } else {
            """
                #id_$id .node-rect { fill: rgba(255,255,255,0.9); stroke: rgba(0,122,255,0.3); stroke-width: 1.5; rx: 12; ry: 12; filter: url(#dropShadow); }
                #id_$id .main-node, .common-node, .specialized-node, .specialized-title { fill: rgba(255,255,255,0.9); stroke: rgba(0,122,255,0.3); stroke-width: 1.5; rx: 12; ry: 12; filter: url(#dropShadow); }
                #id_$id .node-text { fill: #1a1a1a; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; font-weight: 600; text-anchor: middle; }
                #id_$id .connection-line { stroke: #4a5568; stroke-width: 2; }
                #id_$id .dashed-line { stroke: #718096; stroke-width: 2; stroke-dasharray: 6,6; }
                #id_$id .plus-symbol { fill: #4a5568; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; font-size: 28px; font-weight: 700; text-anchor: middle; filter: url(#dropShadow);}
            """
        }

        svg.append("""
            <svg width="${totalWidth +20}" height="$totalHeight" id="id_$id" xmlns="http://www.w3.org/2000/svg">
                <defs>
                <!-- Enhanced filters for glass effect -->
                <filter id="glassDropShadow_$id" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="8" result="blur"/>
                    <feOffset in="blur" dx="0" dy="8" result="offsetBlur"/>
                    <feFlood flood-color="rgba(0,0,0,0.15)" result="shadowColor"/>
                    <feComposite in="shadowColor" in2="offsetBlur" operator="in" result="shadow"/>
                    <feMerge>
                        <feMergeNode in="shadow"/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>
        
                $glassColors
                    <style>
                        $nodeStyles
                        #id_$id .main-text { font-size: 16px; }
                        #id_$id .common-text { font-size: 12px; }
                        #id_$id .specialized-text { font-size: 12px; }
                        #id_$id .neon-green { fill: #39FF14; }
                        #id_$id .neon-pink { fill: #FF1493; }
                        #id_$id .neon-cyan { fill: #00FFFF; }
                        #id_$id .neon-yellow { fill: #FFFF00; }
                        #id_$id .neon-orange { fill: #FF6600; }
                        #id_$id .neon-purple { fill: #9D00FF; }
                        #id_$id .neon-blue { fill: #0080FF; }
                        #id_$id .neon-lime { fill: #CCFF00; }
                        #id_$id .neon-magenta { fill: #FF00FF; }
                        #id_$id .neon-red { fill: #FF073A; }
                        #id_$id .glass-card { transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);}
                    </style>
                    <filter id="dropShadow" x="-50%" y="-50%" width="200%" height="200%">
                        <feDropShadow dx="0" dy="2.5" stdDeviation="2.5" flood-color="#000000" flood-opacity="0.35"/>
                    </filter>
                    
                    $backgroundColor
                </defs>
                <rect width="100%" height="100%" fill="url(#backgroundGradient_$id)"/>
        """.trimIndent())


        // Draw connections first (so they appear behind nodes)
        drawConnections(svg, data)

        // Draw main node
        drawNode(svg, data.mainNode, "main-node", "main-text")

        // Draw common nodes
        data.commonRows.forEach { row ->
            row.forEach { node ->
                drawNode(svg, node, "common-node", "common-text")
            }
        }

        // Draw specialized groups
        data.specializedGroups.forEachIndexed{ groupIndex, group ->
            drawSpecializedGroup(svg, group, groupIndex)
        }

        svg.append("</svg>")
        return svg.toString()
    }

    private fun calculatePositions(data: DiagramData) {
        // Position main node
        data.mainNode.x = 80.0
        data.mainNode.y = MAIN_NODE_Y

        var currentY = START_Y

        // Position common rows
        data.commonRows.forEach { row ->
            var currentX = START_X
            row.forEach { node ->
                node.x = currentX
                node.y = currentY
                currentX += COLUMN_WIDTH
            }
            currentY += ROW_HEIGHT
        }

        // Position specialized groups
        data.specializedGroups.forEach { group ->
            group.rows.forEachIndexed { rowIndex, row ->
                var currentX = if (rowIndex == 0) START_X else START_X + COLUMN_WIDTH // Second+ rows start at column 1
                row.forEach { node ->
                    node.x = currentX
                    node.y = currentY
                    currentX += COLUMN_WIDTH
                }
                currentY += ROW_HEIGHT
            }
        }
    }

    private fun computeCanvasSize(data: DiagramData): Pair<Int, Int> {
        // Determine overall bounds from all nodes (including main node)
        val padding = 40.0 // outer padding for shadows and breathing room
        var maxRight = data.mainNode.x + data.mainNode.width
        var maxBottom = data.mainNode.y + data.mainNode.height
        var minLeft = data.mainNode.x
        var minTop = 0.0

        fun consider(node: DiagramNode) {
            if (node.x < minLeft) minLeft = node.x
            if (node.y < minTop) minTop = node.y
            val right = node.x + node.width
            val bottom = node.y + node.height
            if (right > maxRight) maxRight = right
            if (bottom > maxBottom) maxBottom = bottom
        }

        data.commonRows.forEach { row -> row.forEach { consider(it) } }
        data.specializedGroups.forEach { g -> g.rows.forEach { row -> row.forEach { consider(it) } } }

        // Account for plus symbols and vertical buses rendered 20px to the left of first node in a group
        if (data.specializedGroups.any()) {
            val firstNodes = data.specializedGroups.mapNotNull { it.rows.firstOrNull()?.firstOrNull() }
            if (firstNodes.isNotEmpty()) {
                val leftMostPlus = firstNodes.minOf { it.x - 20 }
                if (leftMostPlus < minLeft) minLeft = leftMostPlus
            }
        }

        val width = ((maxRight - minLeft) + padding * 2).coerceAtLeast(300.0)
        val height = ((maxBottom - minTop) + padding * 2).coerceAtLeast(200.0)
        // Return ints for SVG width/height attributes
        return width.toInt() to height.toInt()
    }

    private fun drawNode(svg: StringBuilder, node: DiagramNode, rectClass: String, textClass: String) {
        // Draw rect first
        //fill="rgba(0,122,255,0.1)
        val fill = if(useDark) "rgba(0,122,255,0.1)" else "rgba(255,255,255,0.8)"
        svg.append("""
            <g class="glass-card">
            <rect x="${node.x}" y="${node.y}" width="${node.width}" height="${node.height}" rx="12" ry="12"
            fill="$fill"
              stroke="url(#glassBorder_$id)"
              stroke-width="1.5"
              filter="url(#glassDropShadow_$id)"
          />
          <rect x="${node.x}" y="${node.y}" width="${node.width}" height="${node.height}" rx="12" ry="12"
            fill="url(#glassOverlay_$id)"
            opacity="0.7"/>
        """.trimIndent())


        // Prepare text content with optional emoji prefix
        val fullText = buildString {
            if (node.emoji != null) {
                append(node.emoji)
                append(' ')
            }
            append(node.title)
        }

        // Simple word wrapping using approximate text width metrics
        // Assumptions based on CSS in SVG: font-size ~12-14; we choose 12
        val fontSize = 12
        val fontFamily = "sans-serif"
        val sidePadding = 8.0
        val maxTextWidth = (node.width - sidePadding * 2).toInt().coerceAtLeast(20)

        fun measure(text: String): Int {
            return text.textWidth(fontFamily, fontSize)
        }

        // Split by spaces, build lines within maxTextWidth
        val words = fullText.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (w in words) {
            if (current.isEmpty()) {
                current.append(w)
            } else {
                val candidate = current.toString() + " " + w
                if (measure(candidate) <= maxTextWidth) {
                    current.clear(); current.append(candidate)
                } else {
                    lines.add(current.toString())
                    current.clear(); current.append(w)
                }
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())

        // Fallback if no spaces: hard cut by characters based on average width
        if (lines.isEmpty() && fullText.isNotEmpty()) {
            val avgCharWidth = (fontSize * 0.6).toInt().coerceAtLeast(1)
            val charsPerLine = (maxTextWidth / avgCharWidth).coerceAtLeast(1)
            var i = 0
            while (i < fullText.length) {
                val end = (i + charsPerLine).coerceAtMost(fullText.length)
                lines.add(fullText.substring(i, end))
                i = end
            }
        }

        // Ensure we don't exceed vertical space: compute line height and cap number of lines
        val lineHeight = (fontSize + 4)
        val maxLines = kotlin.math.max(1, (node.height / lineHeight).toInt())
        val finalLines = if (lines.size <= maxLines) {
            lines
        } else {
            val trimmed = lines.take(maxLines).toMutableList()
            // Ellipsize the last line to fit width
            var last = trimmed.last()
            val ellipsis = "…"
            while (last.isNotEmpty() && measure(last + ellipsis) > maxTextWidth) {
                last = last.dropLast(1)
            }
            trimmed[trimmed.lastIndex] = if (last.isEmpty()) ellipsis else last + ellipsis
            trimmed
        }
        val totalTextHeight = finalLines.size * lineHeight
        val startY = (node.y + (node.height - totalTextHeight) / 2 + fontSize).toInt()
        val centerX = (node.x + node.width / 2).toInt()

        // Output text with tspans, centered via text-anchor middle
        svg.append("""
            <text x="$centerX" y="$startY" class="node-text $textClass" text-anchor="middle">
        """.trimIndent())
        finalLines.forEachIndexed { idx, line ->
            if (idx == 0) {
                svg.append("""
                <tspan x="$centerX" dy="0">${line.escapeXml()}</tspan>
                """.trimIndent())
            } else {
                svg.append("""
                <tspan x="$centerX" dy="$lineHeight">${line.escapeXml()}</tspan>
                """.trimIndent())
            }
        }
        svg.append("""
            </text>
            </g>
        """.trimIndent())
    }

    private fun drawSpecializedGroup(svg: StringBuilder, group: SpecializedGroup, groupIndex: Int) {
        // Define list of neon colors to cycle through
        val neonColors = listOf(
            "neon-green",      // #39FF14 (Electric Green)
            "neon-pink",       // #FF1493 (Hot Pink)
            "neon-cyan",       // #00FFFF (Electric Cyan)
            "neon-yellow",     // #FFFF00 (Electric Yellow)
            "neon-orange",     // #FF6600 (Electric Orange)
            "neon-purple",     // #9D00FF (Electric Purple)
            "neon-blue",       // #0080FF (Electric Blue)
            "neon-magenta",    // #FF00FF (Electric Magenta)
            "neon-red",         // #FF073A (Electric Red)
            "neon-lime",       // #CCFF00 (Electric Lime)

        )

        val neonClass = neonColors[groupIndex % neonColors.size]

        // Draw plus symbol
        val firstNode = group.rows.first().first()
        val plusX = firstNode.x - 20
        val plusY = firstNode.y + 28
        svg.append("""
            <text x="$plusX" y="$plusY" class="plus-symbol">+</text>
        """.trimIndent())

        group.rows.forEach { row ->
            row.forEachIndexed { index, node ->
                val nodeClass = if (index == 0 && row == group.rows.first()) "specialized-title" else "specialized-node"
                val textClass = "specialized-text $neonClass"
                drawNode(svg, node, nodeClass, textClass)
            }
        }
    }

    private fun drawConnections(svg: StringBuilder, data: DiagramData) {
        val mainCenterX = data.mainNode.x + data.mainNode.width / 2
        val mainBottomY = data.mainNode.y + data.mainNode.height

        // Calculate where the main vertical line should end
        val mainVerticalEndY = if (data.specializedGroups.isNotEmpty()) {
            // Stop at the center Y of the last specialized group's first row (index 0)
            val lastSpecializedGroup = data.specializedGroups.last()
            lastSpecializedGroup.rows.first().first().y + lastSpecializedGroup.rows.first().first().height / 2
        } else if (data.commonRows.isNotEmpty()) {
            // If no specialized groups, stop at the last common row
            val lastCommonRow = data.commonRows.last()
            lastCommonRow.first().y + lastCommonRow.first().height / 2
        } else {
            // If no rows at all, just extend down a bit
            mainBottomY + 100
        }

        // Draw main vertical line down from main node
        svg.append("""
            <line x1="$mainCenterX" y1="$mainBottomY" x2="$mainCenterX" y2="$mainVerticalEndY" class="dashed-line"/>
        """.trimIndent())

        // Draw connections for common rows
        data.commonRows.forEach { row ->
            val rowCenterY = row.first().y + row.first().height / 2
            drawRowConnections(svg, row, mainCenterX, rowCenterY)
        }

        // Draw connections for specialized groups
        data.specializedGroups.forEach { group ->
            if (group.rows.isNotEmpty()) {
                val firstRow = group.rows.first()
                val otherRows = group.rows.drop(1)

                // Connect first row to main vertical line
                val firstRowCenterY = firstRow.first().y + firstRow.first().height / 2
                drawRowConnections(svg, firstRow, mainCenterX, firstRowCenterY)

                // If there are multiple rows, draw vertical bus from plus and connect other rows to bus
                if (otherRows.isNotEmpty()) {
                    val busX = firstRow.first().x - 20
                    val lastRow = group.rows.last()
                    val lastRowCenterY = lastRow.first().y + lastRow.first().height / 2

                    // Draw vertical bus from the plus symbol down
                    svg.append("""
                        <line x1="$busX" y1="$firstRowCenterY" x2="$busX" y2="$lastRowCenterY" class="dashed-line"/>
                    """.trimIndent())

                    // Connect other rows to the bus
                    otherRows.forEach { row ->
                        val rowCenterY = row.first().y + row.first().height / 2
                        drawRowConnections(svg, row, busX, rowCenterY)
                    }
                }
            }
        }
    }
    private fun drawRowConnections(svg: StringBuilder, nodes: List<DiagramNode>, startX: Double, verticalLineY: Double) {
        if (nodes.isEmpty()) return

        val firstNodeLeftEdge = nodes.first().x

        // Horizontal from start point to left edge of first node
        svg.append("""
            <line x1="$startX" y1="$verticalLineY" x2="$firstNodeLeftEdge" y2="$verticalLineY" class="dashed-line"/>
        """.trimIndent())

        // Draw segments between consecutive nodes
        for (i in 0 until nodes.size - 1) {
            val currentNode = nodes[i]
            val nextNode = nodes[i + 1]
            val gapStart = currentNode.x + currentNode.width
            val gapEnd = nextNode.x
            svg.append("""
                <line x1="$gapStart" y1="$verticalLineY" x2="$gapEnd" y2="$verticalLineY" class="dashed-line"/>
            """.trimIndent())
        }
    }
}


class DiagramParser {

    fun parseJson(json: String): DiagramData {
        val input = Json.decodeFromString<DiagramJsonInput>(json)
        return convertToDiagramData(input)
    }

    fun parseCSV(csv: String): DiagramData {
        val lines = csv.lines().filter { it.isNotBlank() }
        if (lines.size < 2) throw IllegalArgumentException("CSV must have at least 2 lines")

        // Handle the case where first line is "main,QUOTE" format or has header
        var mainNode = "QUOTE" // default
        var dataStartIndex = 1 // skip header by default

        // Check if first line is the old format "main,QUOTE"
        if (lines[0].lowercase().startsWith("main,")) {
            val parts = lines[0].split(",")
            if (parts.size >= 2) {
                mainNode = parts[1].trim()
            }
            dataStartIndex = 2 // skip both main line and header
        } else {
            // Look for MAIN row in the data
            val mainNodeLine = lines.drop(1).find { it.startsWith("MAIN,") }
            if (mainNodeLine != null) {
                val mainNodeParts = parseCSVRow(mainNodeLine)
                if (mainNodeParts.size >= 4) {
                    mainNode = mainNodeParts[3] // The node name is in the 4th column
                }
            }
        }

        val commonRows = mutableListOf<List<String>>()
        val specializedGroupsMap = mutableMapOf<String, MutableMap<Int, Pair<String?, List<String>>>>()

        lines.drop(dataStartIndex).forEach { line -> // Skip header and/or main line
            if (line.startsWith("MAIN,") || line.lowercase().startsWith("type,")) return@forEach // Skip main node line and header

            val parts = parseCSVRow(line)
            if (parts.size < 4) return@forEach // Skip invalid lines

            val type = parts[0]
            val emoji = parts[1].takeIf { it.isNotBlank() }
            val rowIndex = parts[2].toIntOrNull() ?: return@forEach
            val nodesString = parts[3].trim('"')
            val nodes = nodesString.split(",").map { it.trim() }

            if (type == "COMMON") {
                while (commonRows.size <= rowIndex) {
                    commonRows.add(emptyList())
                }
                commonRows[rowIndex] = nodes
            } else {
                specializedGroupsMap.getOrPut(type) { mutableMapOf() }[rowIndex] = emoji to nodes
            }
        }

        val specializedGroups = specializedGroupsMap.map { (title, rowsMap) ->
            val emoji = rowsMap.values.firstOrNull()?.first ?: ""
            val rows = rowsMap.entries.sortedBy { it.key }.map { it.value.second }
            SpecializedGroupInput(title, emoji, rows)
        }

        return convertToDiagramData(DiagramJsonInput(mainNode, commonRows, specializedGroups))
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

    private fun convertToDiagramData(input: DiagramJsonInput): DiagramData {
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