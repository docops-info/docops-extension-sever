package io.docops.docopsextensionssupport.domainviz

import io.docops.docopsextensionssupport.support.*
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.textWidth
import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.json.Json
import kotlin.compareTo
import kotlin.div
import kotlin.text.compareTo
import kotlin.text.toInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DomainVisualizer @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toHexString(),
    val useDark: Boolean = false) {
    
    private val theme: DocOpsTheme = ThemeFactory.getThemeByName("premium", useDark)
    
    companion object {
        private const val MAIN_NODE_Y = 24.0
        private const val ROW_HEIGHT = 72.0
        private const val NODE_SPACING = 24.0
        private const val COLUMN_WIDTH = 160.0
        private const val START_X = 200.0
        private const val START_Y = 120.0
    }

    @OptIn(ExperimentalUuidApi::class)
    fun generateSVG(data: DiagramData): String {
        // Calculate positions first
        calculatePositions(data)

        val svg = StringBuilder()
        // Compute dynamic SVG dimensions based on positioned nodes
        val (totalWidth, totalHeight) = computeCanvasSize(data)
        
        val backgroundGradient = """
            <linearGradient id="backgroundGradient_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:${theme.canvas};stop-opacity:1" />
                <stop offset="100%" style="stop-color:${theme.surfaceLow};stop-opacity:1" />
            </linearGradient>
        """.trimIndent()

        val glassColors = """
                <!-- Glass Border -->
                <linearGradient id="glassBorder_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:${theme.primaryText};stop-opacity:0.2" />
                    <stop offset="50%" style="stop-color:${theme.primaryText};stop-opacity:0.1" />
                    <stop offset="100%" style="stop-color:${theme.primaryText};stop-opacity:0.05" />
                </linearGradient>
                <!-- Link Border -->
                <linearGradient id="linkBorder_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:${theme.accentColor};stop-opacity:0.6" />
                    <stop offset="50%" style="stop-color:${theme.accentColor};stop-opacity:0.4" />
                    <stop offset="100%" style="stop-color:${theme.accentColor};stop-opacity:0.2" />
                </linearGradient>
                <!-- Glass Effect Gradients -->
                <linearGradient id="glassOverlay_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:${theme.primaryText};stop-opacity:0.05" />
                    <stop offset="100%" style="stop-color:${theme.primaryText};stop-opacity:0.02" />
                </linearGradient>
            """.trimIndent()

        val nodeStyles = """
                #id_$id .node-rect { fill: ${theme.glassEffect}; stroke: ${theme.secondaryText}; stroke-width: 1; rx: ${theme.cornerRadius}; ry: ${theme.cornerRadius}; filter: url(#dropShadow); }
                #id_$id .main-node, #id_$id .common-node, #id_$id .specialized-node, #id_$id .specialized-title { fill: ${theme.glassEffect}; stroke: ${theme.secondaryText}; stroke-width: 1; rx: ${theme.cornerRadius}; ry: ${theme.cornerRadius}; filter: url(#dropShadow); }
                #id_$id .node-text { fill: ${theme.primaryText}; font-family: ${theme.fontFamily}; font-weight: 500; text-anchor: middle; }
                #id_$id .connection-line { stroke: ${theme.secondaryText}; stroke-width: 2; }
                #id_$id .dashed-line { stroke: ${theme.secondaryText}; stroke-width: 1.5; stroke-dasharray: 4,4; opacity: 0.6; }
                #id_$id .plus-symbol { fill: ${theme.accentColor}; font-family: ${theme.fontFamily}; font-size: 24px; font-weight: 700; text-anchor: middle; dominant-baseline: central; }
            """.trimIndent()

        // Generate dynamic color classes for specialized groups from theme palette
        val groupColorStyles = theme.chartPalette.mapIndexed { index, svgColor ->
            "#id_$id .group-color-$index { fill: ${svgColor.color}; }"
        }.joinToString("\n")

        svg.append("""
            <svg width="${totalWidth + 20}" height="$totalHeight" id="id_$id" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${totalWidth + 20} $totalHeight" preserveAspectRatio="xMidYMid meet">
                <defs>
                ${theme.fontImport}
                <!-- Enhanced filters for glass effect -->
                <filter id="glassDropShadow_$id" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="4" result="blur"/>
                    <feOffset in="blur" dx="0" dy="4" result="offsetBlur"/>
                    <feFlood flood-color="rgba(0,0,0,0.1)" result="shadowColor"/>
                    <feComposite in="shadowColor" in2="offsetBlur" operator="in" result="shadow"/>
                    <feMerge>
                        <feMergeNode in="shadow"/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>
        
                $glassColors
                    <style>
                        $nodeStyles
                        $groupColorStyles
                        #id_$id .main-text { font-size: 16px; font-weight: 700; }
                        #id_$id .common-text { font-size: 12px; }
                        #id_$id .specialized-text { font-size: 12px; }
                        #id_$id .glass-card { transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);}
                        #id_$id .clickable-node:hover { filter: brightness(1.1); }
                    </style>
                    <filter id="dropShadow" x="-50%" y="-50%" width="200%" height="200%">
                        <feDropShadow dx="0" dy="2" stdDeviation="2" flood-color="#000000" flood-opacity="0.15"/>
                    </filter>
                    
                    $backgroundGradient
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
                // First row of a group starts at START_X, subsequent rows offset by COLUMN_WIDTH
                var currentX = if (rowIndex == 0) START_X else START_X + COLUMN_WIDTH 
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
        // Check if node has links to make it clickable
        val hasLinks = node.links.isNotEmpty()

        if (hasLinks) {
            // If node has links, wrap in a clickable group with the primary link
            val primaryLink = node.links.first()
            svg.append("""
                <g class="glass-card clickable-node" style="cursor: pointer;" onclick="window.open('${primaryLink.url.escapeXml()}', '_blank')">
            """.trimIndent())
        } else {
            svg.append("""
                <g class="glass-card">
            """.trimIndent())
        }

        // Draw rect first
        val strokeColor = if (hasLinks) {
            "url(#linkBorder_$id)"
        } else {
            "url(#glassBorder_$id)"
        }

        svg.append("""
            <rect x="${node.x}" y="${node.y}" width="${node.width}" height="${node.height}" rx="${theme.cornerRadius}" ry="${theme.cornerRadius}"
              fill="${theme.glassEffect}"
              stroke="$strokeColor"
              stroke-width="1.5"
              filter="url(#glassDropShadow_$id)"
          />
          <rect x="${node.x}" y="${node.y}" width="${node.width}" height="${node.height}" rx="${theme.cornerRadius}" ry="${theme.cornerRadius}"
            fill="url(#glassOverlay_$id)"
            opacity="0.4"/>
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
        val fontSize = if (rectClass == "main-node") 14 else 12
        val sidePadding = 12.0
        val maxTextWidth = (node.width - sidePadding * 2).toInt().coerceAtLeast(20)

        fun measure(text: String): Int {
            return text.textWidth(theme.fontFamily, fontSize)
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
                    current.append(" ").append(w)
                } else {
                    lines.add(current.toString())
                    current.clear(); current.append(w)
                }
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())

        // Fallback if no spaces: hard cut by characters
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

        // Ensure we don't exceed vertical space
        val lineHeight = (fontSize * 1.2).toInt()
        val maxLines = kotlin.math.max(1, (node.height / lineHeight).toInt())
        val finalLines = if (lines.size <= maxLines) {
            lines
        } else {
            val trimmed = lines.take(maxLines).toMutableList()
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

        val textFill = if (hasLinks) {
            theme.accentColor
        } else {
            theme.primaryText
        }

        svg.append("""
            <text x="$centerX" y="$startY" class="node-text $textClass" text-anchor="middle" fill="$textFill">
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
        """.trimIndent())

        // Add link indicator icon if node has links
        if (hasLinks) {
            val iconX = node.x + node.width - 12
            val iconY = node.y + 12
            svg.append("""
                <text x="$iconX" y="$iconY" class="link-icon" text-anchor="middle" fill="$textFill" font-size="8" opacity="0.8">🔗</text>
            """.trimIndent())
        }

        svg.append("</g>")
    }


    private fun drawSpecializedGroup(svg: StringBuilder, group: SpecializedGroup, groupIndex: Int) {
        val colorClass = "group-color-${groupIndex % theme.chartPalette.size}"

        // Draw plus symbol
        val firstNode = group.rows.first().first()
        val plusX = firstNode.x - 20
        val plusY = firstNode.y + firstNode.height / 2 - 2.0
        svg.append("""
            <text x="$plusX" y="$plusY" class="plus-symbol">+</text>
        """.trimIndent())

        group.rows.forEach { row ->
            row.forEachIndexed { index, node ->
                val nodeClass = if (index == 0 && row == group.rows.first()) "specialized-title" else "specialized-node"
                val textClass = "specialized-text $colorClass"
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


