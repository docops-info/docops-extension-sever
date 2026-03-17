package io.docops.docopsextensionssupport.badge.type

import io.docops.docopsextensionssupport.badge.Badge
import io.docops.docopsextensionssupport.badge.BadgeConfig
import io.docops.docopsextensionssupport.badge.BadgeStyle


abstract class ThemedBadge(darkMode: Boolean)
{
    fun generate(badges: MutableList<Badge>, config: BadgeConfig, style: BadgeStyle): String {

        val spacing = config.spacing
        val direction = config.direction.lowercase()
        val perRow = config.perRow

        // Badge dimensions based on style
        val (badgeWidth, badgeHeight) = when (style) {
            BadgeStyle.GLASSMORPHIC -> 180 to 32
            BadgeStyle.NEON -> 160 to 36
            BadgeStyle.BRUTALIST -> 200 to 40
            BadgeStyle.GRADIENT -> 170 to 34
            BadgeStyle.MINIMAL -> 190 to 28
            BadgeStyle.NEUMORPHIC -> 180 to 38
        }

        // Calculate layout
        val (totalWidth, totalHeight) = if (direction == "horizontal") {
            val rows = (badges.size + perRow - 1) / perRow
            val cols = minOf(badges.size, perRow)

            // Calculate actual width for horizontal layout
            val maxWidthPerRow = 0
            val rowWidths = mutableListOf<Int>()
            for (r in 0 until rows) {
                var currentRowWidth = 0
                for (c in 0 until perRow) {
                    val index = r * perRow + c
                    if (index < badges.size) {
                        val w = calculateBadgeWidth(badges[index], style)
                        currentRowWidth += w + spacing
                    }
                }
                rowWidths.add(currentRowWidth - spacing)
            }
            (rowWidths.maxOrNull() ?: 0) to ((badgeHeight + spacing) * rows - spacing)
        } else {
            val maxWidth = badges.maxOf { calculateBadgeWidth(it, style) }
            maxWidth to ((badgeHeight + spacing) * badges.size - spacing)
        }

        val svgContent = buildString {
            appendLine("""<svg width="$totalWidth" height="$totalHeight" xmlns="http://www.w3.org/2000/svg">""")
            appendLine("""  <defs>""")
            appendLine(BadgeStyleGenerator.generateStyles(style, config.theme, config.fontFamily))
            appendLine("""  </defs>""")

            badges.forEachIndexed { index, badge ->
                val row = index / perRow
                val col = index % perRow

                val currentBadgeWidth = calculateBadgeWidth(badge, style)

                val x = if (direction == "horizontal") {
                    // For horizontal, we need to sum previous widths in the same row
                    var offset = 0
                    for(i in (row * perRow) until index) {
                        offset += calculateBadgeWidth(badges[i], style) + spacing
                    }
                    offset
                } else {
                    0
                }

                val y = if (direction == "horizontal") {
                    row * (badgeHeight + spacing)
                } else {
                    index * (badgeHeight + spacing)
                }

                appendLine(BadgeStyleGenerator.generateBadge(badge, x, y, currentBadgeWidth, badgeHeight, style, config))
            }

            appendLine("""</svg>""")
        }

        return svgContent
    }

    private fun calculateBadgeWidth(badge: Badge, style: BadgeStyle): Int {
        return when (style) {
            BadgeStyle.NEON -> ((badge.label.length + badge.message.length) * 8) + 60
            BadgeStyle.BRUTALIST -> ((badge.label.length + badge.message.length) * 10) + 40
            BadgeStyle.GRADIENT -> ((badge.label.length + badge.message.length) * 8.5).toInt() + 80
            BadgeStyle.MINIMAL -> ((badge.label.length + badge.message.length) * 8) + 40
            BadgeStyle.NEUMORPHIC -> ((badge.label.length + badge.message.length) * 8.5).toInt() + 60
            BadgeStyle.GLASSMORPHIC -> ((badge.label.length + badge.message.length) * 8) + 40
        }
    }

    abstract fun generate(badges: MutableList<Badge>, config: BadgeConfig): String
}