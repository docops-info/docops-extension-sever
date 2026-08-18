/**
 * A modern, high-contrast domain visualizer following the 'Neural Blueprint' aesthetic.
 * Focuses on atmospheric depth, technical typography, and clear hierarchy for nested data.
 */
package io.docops.docopsextensionssupport.domainviz

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A modern, high-contrast domain visualizer following the 'Neural Blueprint' aesthetic.
 * Focuses on atmospheric depth, technical typography, and clear hierarchy for nested data.
 */
class NeuralDomainVisualizer @OptIn(ExperimentalUuidApi::class) constructor(
    private val useDark: Boolean = false,
    private val id: String = Uuid.random().toHexString()
) {
    private lateinit var theme: DocOpsTheme

    @OptIn(ExperimentalUuidApi::class)
    fun generateSVG(data: DiagramData): String {
        theme = ThemeFactory.getTheme(useDark)
        val rowHeight = 72.0
        val columnWidth = 240.0
        val maxPerRow = 4

        // Width is now stable since we wrap at 4
        val totalWidth = (144 + (maxPerRow * columnWidth) + 40).toInt().coerceAtMost(1100)
        val totalHeight = computeNeededHeight(data, rowHeight, maxPerRow)
        val fontSize = 14 / theme.fontWidthMultiplier

        return buildString {
            append("""
                <svg width="$totalWidth" height="$totalHeight" id="neural_$id" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $totalWidth $totalHeight">
                    <defs>
                        <style>
                            ${theme.fontImport}
                            .main-title_$id { font-family: ${theme.fontFamily}; font-weight: 800; font-size: ${30 / theme.fontWidthMultiplier}px; fill: ${theme.primaryText}; letter-spacing: -1px; }
                            .group-rail-text_$id { font-family: ${theme.fontFamily}; font-weight: 800; font-size: ${12 / theme.fontWidthMultiplier}px; fill: ${theme.accentColor}; text-transform: uppercase; letter-spacing: 2px; }
                            .node-text_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: ${fontSize}px; fill: ${theme.primaryText}; }
                            .link-text_$id { font-family: ${theme.fontFamily}; font-weight: 800; font-size: ${fontSize}px; fill: ${theme.accentColor}; }
                            .row-index_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: ${12 / theme.fontWidthMultiplier}px; fill: ${theme.secondaryText}; opacity: 0.7; }
                            .version-tag_$id { font-family: ${theme.fontFamily}; font-size: ${12 / theme.fontWidthMultiplier}px; fill: ${theme.secondaryText}; opacity: 0.5; font-weight: 800; }
                        </style>
                        <linearGradient id="bgGrad_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stop-color="${theme.canvas}" />
                            <stop offset="100%" stop-color="${theme.canvas}" stop-opacity="0.8" />
                        </linearGradient>
                    </defs>

                    <rect width="100%" height="100%" fill="url(#bgGrad_$id)" rx="${theme.cornerRadius}"/>
                
                    <path d="M80 144 V${totalHeight - 64}" stroke="${theme.accentColor}" stroke-width="1.5" stroke-dasharray="4 4" opacity="0.2"/>

                    <g transform="translate(64, 72)">
                        <text x="0" y="0" class="main-title_$id">${data.mainNode.title.uppercase()}</text>
                        <rect x="0" y="16" width="64" height="4" fill="${theme.accentColor}" rx="2"/>
                    </g>
                    
                    <text x="${totalWidth - 24}" y="${totalHeight - 24}" text-anchor="end" class="version-tag_$id">NEURAL_DOMAIN_VIZ V2.2 | THEME: ${theme.name.uppercase()}</text>

                    <g transform="translate(144, 160)">
            """.trimIndent())

            var currentY = 0.0

            // Common Rows with wrapping
            data.commonRows.forEach { originalRow ->
                val wrappedRows = originalRow.chunked(maxPerRow)
                wrappedRows.forEach { row ->
                    append("""<g transform="translate(0, $currentY)">""")
                    row.forEachIndexed { colIndex, node ->
                        drawNeuralNode(this, node, colIndex * columnWidth)
                    }
                    append("</g>")
                    currentY += rowHeight
                }
            }

            // Specialized Groups with wrapping
            data.specializedGroups.forEach { group ->
                currentY += 30.0

                // Pre-calculate wrapped structure to determine total rail height
                val allGroupRows = group.rows.flatMap { it.chunked(maxPerRow) }
                val wrappedTitle = wrapByWords(group.title.uppercase(), 22)

                // Ensure the rail is at least as tall as the wrapped title
                val titleHeightPx = wrappedTitle.size * 14.0 + 20.0
                val contentHeightPx = allGroupRows.size * rowHeight
                val groupHeight = maxOf(contentHeightPx, titleHeightPx)

                append("""
                            <g transform="translate(0, $currentY)">
                                <rect x="-64" y="0" width="4" height="${groupHeight - 24}" fill="${theme.accentColor}" rx="2" opacity="0.25"/>
                                <g transform="translate(-48, ${(groupHeight - 24) / 2}) rotate(-90)">
                                    <text x="0" y="0" class="group-rail-text_$id" text-anchor="middle" dominant-baseline="middle" style="letter-spacing: 3px;">
                        """.trimIndent())

                val lineHeight = 14
                val startOffset = -(wrappedTitle.size - 1) * (lineHeight / 2.0)
                wrappedTitle.forEachIndexed { index, line ->
                    append("""<tspan x="0" dy="${if (index == 0) startOffset else lineHeight.toDouble()}">${line.escapeXml()}</tspan>""")
                }

                append("""
                                    </text>
                                </g>
                        """.trimIndent())

                var internalY = 0.0
                group.rows.forEachIndexed { rowIndex, originalRow ->
                val wrappedSubRows = originalRow.chunked(maxPerRow)

                    wrappedSubRows.forEachIndexed { subIdx, row ->
                        append("""<g transform="translate(0, $internalY)">""")
                        // Only show index/emoji on the first sub-row of an original data row
                        if (subIdx == 0 && (group.rows.size > 1 || originalRow.size > maxPerRow)) {
                            append("""<text x="0" y="-8" class="row-index_$id">${group.emoji} LEVEL_0${rowIndex + 1}</text>""")
                        }

                        row.forEachIndexed { colIndex, node ->
                            drawNeuralNode(this, node, colIndex * columnWidth)
                        }
                        append("</g>")
                        internalY += rowHeight
                    }
                }
                append("</g>")
                currentY += groupHeight + 40.0
            }
            append("</g></svg>")
        }
    }

    private fun computeNeededHeight(data: DiagramData, rowHeight: Double, maxPerRow: Int): Int {
        val commonRowWrappedCount = data.commonRows.sumOf { it.chunked(maxPerRow).size }
        val groupRowWrappedCount = data.specializedGroups.sumOf { g ->
            val rowsCount = g.rows.sumOf { it.chunked(maxPerRow).size }
            val titleLines = wrapByWords(g.title.uppercase(), 22).size
            // Factor in both row count and title height
            maxOf(rowsCount.toDouble(), (titleLines * 16.0 + 24.0) / rowHeight).toInt()
        }
        val gaps = data.specializedGroups.size * 80.0 // Increased gap buffer
        return (240 + (commonRowWrappedCount + groupRowWrappedCount) * rowHeight + gaps).toInt()
    }

    private fun wrapByWords(text: String, maxChars: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 <= maxChars) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }
    private fun calculateMaxNodes(data: DiagramData): Int {
        val commonMax = data.commonRows.maxOfOrNull { it.size } ?: 0
        val specializedMax = data.specializedGroups.maxOfOrNull { group ->
            group.rows.maxOfOrNull { it.size } ?: 0
        } ?: 0
        return maxOf(commonMax, specializedMax)
    }


    private fun drawNeuralNode(sb: StringBuilder, node: DiagramNode, x: Double) {
        val isLink = node.links.isNotEmpty()
        val textClass = if (isLink) "link-text_$id" else "node-text_$id"
        val strokeColor = if (isLink) theme.accentColor else theme.primaryText
        val opacity = if (isLink) "0.9" else "0.15"

        val pointerStyle = if (isLink) "style=\"cursor: pointer;\"" else ""
        val clickHandler = if (isLink) {
            val primaryLink = node.links.first()
            "onclick=\"window.open('${primaryLink.url.escapeXml()}', '_blank')\""
        } else ""

        sb.append("""
                <g transform="translate($x, 0)" $pointerStyle $clickHandler>
                    <rect width="216" height="40" rx="20" 
                          fill="${theme.glassEffect}" 
                          stroke="$strokeColor" 
                      stroke-opacity="$opacity" 
                      stroke-width="${if (isLink) 1.5 else 1}"/>
                <text x="24" y="20" class="$textClass" dominant-baseline="central">
                    ${if (node.emoji != null) "${node.emoji} " else ""}${node.title.escapeXml()}
                </text>
            </g>
        """.trimIndent())
    }

}