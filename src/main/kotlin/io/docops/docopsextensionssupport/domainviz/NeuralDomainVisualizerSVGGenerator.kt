/**
 * A modern, high-contrast domain visualizer following the 'Neural Blueprint' aesthetic.
 * Focuses on atmospheric depth, technical typography, and clear hierarchy for nested data.
 */
package io.docops.docopsextensionssupport.domainviz

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
    private val theme: DomainTheme = if (useDark) DomainDarkTheme() else DomainLightTheme()

    @OptIn(ExperimentalUuidApi::class)
    fun generateSVG(data: DiagramData): String {
        val rowHeight = 70.0
        val columnWidth = 240.0
        val maxPerRow = 4

        // Width is now stable since we wrap at 4
        val totalWidth = (140 + (maxPerRow * columnWidth) + 40).toInt().coerceAtMost(1100)
        val totalHeight = computeNeededHeight(data, rowHeight, maxPerRow)


        return buildString {
            append("""
                <svg width="$totalWidth" height="$totalHeight" id="neural_$id" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $totalWidth $totalHeight">
                    <defs>
                        <style>
                            @import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@300;600&amp;display=swap');
                            .main-title_$id { font-family: ${theme.font}; font-weight: 600; font-size: 28px; fill: ${theme.textPrimary}; letter-spacing: -1px; }
                            .group-rail-text_$id { font-family: ${theme.font}; font-weight: 600; font-size: 11px; fill: ${theme.accentPrimary}; text-transform: uppercase; letter-spacing: 2px; }
                            .node-text_$id { font-family: ${theme.font}; font-weight: 600; font-size: 13px; fill: ${theme.textPrimary}; }
                            .link-text_$id { font-family: ${theme.font}; font-weight: 600; font-size: 13px; fill: ${theme.accentSuccess}; }
                            .row-index_$id { font-family: ${theme.font}; font-weight: 600; font-size: 9px; fill: ${theme.textSecondary}; opacity: 0.7; }
                        </style>
                        <linearGradient id="bgGrad_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stop-color="${theme.bgStart}" />
                            <stop offset="100%" stop-color="${theme.bgEnd}" />
                        </linearGradient>
                    </defs>

                    <rect width="100%" height="100%" fill="url(#bgGrad_$id)" rx="32"/>
                    
                    <path d="M80 140 V${totalHeight - 60}" stroke="${theme.accentPrimary}" stroke-width="1.5" stroke-dasharray="4 4" opacity="0.2"/>

                    <g transform="translate(60, 70)">
                        <text x="0" y="0" class="main-title_$id">${data.mainNode.title.uppercase()}</text>
                        <rect x="0" y="15" width="60" height="4" fill="${theme.accentPrimary}" rx="2"/>
                    </g>

                    <g transform="translate(140, 160)">
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
                val groupHeight = allGroupRows.size * rowHeight

                append("""
                    <g transform="translate(0, $currentY)">
                        <rect x="-40" y="0" width="4" height="${groupHeight - 20}" fill="${theme.accentPrimary}" rx="2" opacity="0.6"/>
                        <text x="-30" y="5" class="group-rail-text_$id" transform="rotate(90 -30 5)">${group.title.uppercase()}</text>
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
                currentY += groupHeight + 20.0
            }
            append("</g></svg>")
        }
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
        val strokeColor = if (isLink) theme.accentSuccess else theme.textPrimary
        val opacity = if (isLink) "0.7" else theme.strokeOpacity

        sb.append("""
            <g transform="translate($x, 0)">
                <rect width="215" height="42" rx="21" 
                      fill="${theme.cardBg}" 
                      stroke="$strokeColor" 
                      stroke-opacity="$opacity" 
                      stroke-width="${if (isLink) 1.5 else 1}"/>
                <text x="24" y="26" class="$textClass">
                    ${if (node.emoji != null) "${node.emoji} " else ""}${node.title.escapeXml()}
                </text>
            </g>
        """.trimIndent())
    }

    private fun computeNeededHeight(data: DiagramData, rowHeight: Double, maxPerRow: Int): Int {
        val commonRowWrappedCount = data.commonRows.sumOf { it.chunked(maxPerRow).size }
        val groupRowWrappedCount = data.specializedGroups.sumOf { g -> g.rows.sumOf { it.chunked(maxPerRow).size } }
        val gaps = data.specializedGroups.size * 50.0
        return (220 + (commonRowWrappedCount + groupRowWrappedCount) * rowHeight + gaps).toInt()
    }
}