package io.docops.docopsextensionssupport.badge.type

import io.docops.docopsextensionssupport.badge.Badge
import io.docops.docopsextensionssupport.badge.BadgeConfig
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.escapeXml


class Classic(val useDark: Boolean = false) {

    val theme = ThemeFactory.getTheme(useDark = useDark, isBrutalist = false)
    fun generateClassicBadge(badges: MutableList<Badge>, config: BadgeConfig): String {
        val sb = StringBuilder()
        val spacing = config.spacing.toDouble()
        val isVertical = config.direction.lowercase() == "vertical"

        // Calculate dimensions
        val badgeWidths = badges.map { calculateWidth(it) }
        val totalWidth = if (isVertical) {
            (badgeWidths.maxOrNull() ?: 0.0) + 20.0
        } else {
            badgeWidths.sum() + (badges.size - 1) * spacing + 20.0
        }
        val totalHeight = if (isVertical) {
            badges.size * (20.0 + spacing) + 20.0
        } else {
            44.0
        }

        sb.append("<svg width='$totalWidth' height='$totalHeight' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' role='img' aria-label='Badge Collection'>")

        // Background based on Theme
        sb.append("<rect width='100%' height='100%' fill='none'/>")

        // Definitions
        sb.append("<defs>")
        sb.append("<linearGradient id='a' x2='0' y2='100%'><stop offset='0' stop-opacity='.1' stop-color='#EEE'/><stop offset='1' stop-opacity='.1'/></linearGradient>")

        badges.forEachIndexed { index, badge ->
            val id = "b_$index"
            // Adding a touch of transparency to the bottom stop for a smoother blend
            sb.append("<linearGradient id='label_$id' x1='0%' y1='0%' x2='0%' y2='100%'><stop offset='0%' stop-color='${badge.labelColor}'/><stop offset='100%' stop-color='${badge.labelColor}' stop-opacity='0.8'/></linearGradient>")
            sb.append("<linearGradient id='message_$id' x1='0%' y1='0%' x2='0%' y2='100%'><stop offset='0%' stop-color='${badge.messageColor}'/><stop offset='100%' stop-color='${badge.messageColor}' stop-opacity='0.8'/></linearGradient>")
            sb.append("<mask id='mask_$id'><rect width='${badgeWidths[index]}' height='20' rx='${theme.cornerRadius}' fill='#FFF'/></mask>")
        }
        sb.append("</defs>")

        // Styles using theme font
        val finalFont = config.fontFamily.ifEmpty { theme.fontFamily }
        sb.append("<style>svg { shape-rendering: crispEdges; } text { text-rendering: optimizeLegibility; -webkit-font-smoothing: antialiased; font-family: $finalFont; }</style>")

        // Render Badges
        var currentX = 10.0
        var currentY = 10.0

        badges.forEachIndexed { index, badge ->
            val width = badgeWidths[index]
            val labelW = estimateWidth(badge.label) + 20.0
            val messageW = width - labelW
            val id = "b_$index"

            val transform = if (isVertical) "translate(10, $currentY)" else "translate($currentX, 12)"

            sb.append("<g transform='$transform'>")
            sb.append("<svg width='$width' height='20' viewBox='0 0 $width 20'>")
            sb.append("<g mask='url(#mask_$id)'>")
            sb.append("<rect x='0' y='0' width='$labelW' height='20' fill='url(#label_$id)'/>")
            sb.append("<rect x='$labelW' y='0' width='$messageW' height='20' fill='url(#message_$id)'/>")
            sb.append("<rect x='0' y='0' width='$width' height='20' fill='url(#a)' opacity='0.1'/>")
            sb.append("</g>")
            sb.append("<g fill='${badge.fontColor}' font-size='11'>")
            sb.append("<text x='10' y='14' style='font-weight: 500;'>${badge.label.escapeXml()}</text>")
            sb.append("<text x='${labelW + 10}' y='14' style='font-weight: 400;'>${badge.message.escapeXml()}</text>")
            sb.append("</g></svg></g>")

            if (isVertical) currentY += 20.0 + spacing else currentX += width + spacing
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun calculateWidth(badge: Badge): Double {
        val labelWidth = estimateWidth(badge.label) + 20.0
        val messageWidth = estimateWidth(badge.message) + 20.0
        return labelWidth + messageWidth
    }

    private fun estimateWidth(text: String): Double = text.length * 6.5
}