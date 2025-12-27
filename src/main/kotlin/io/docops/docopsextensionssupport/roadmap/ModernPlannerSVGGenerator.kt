package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ModernPlannerMaker {

    fun makePlannerImage(planItems: PlanItems, title: String, scale: String, useDark: Boolean = false): String {
        val cols = planItems.toColumns()
        val width = determineWidth(planItems)
        val height = determineHeight(planItems)

        val sb = StringBuilder()
        sb.append(makeHead(width, height, useDark))

        // Background & Grid
        val bgColor = if (useDark) "#09090b" else "#f8fafc"
        val dotColor = if (useDark) "#27272a" else "#e2e8f0"

        sb.append(
            """
            <rect width="100%" height="100%" fill="$bgColor"/>
            <pattern id="dotGrid" width="30" height="30" patternUnits="userSpaceOnUse">
                <circle cx="2" cy="2" r="1" fill="$dotColor" opacity="0.5"/>
            </pattern>
            <rect width="100%" height="100%" fill="url(#dotGrid)"/>
        """
        )

        // Header
        val titleColor = if (useDark) "#f4f4f5" else "#09090b"
        val accentColor = "#ef4444"
        sb.append(
            """
            <g transform="translate(80, 80)">
                <text class="planner-title" fill="$titleColor">${title.escapeXml()}</text>
                <rect y="15" width="60" height="5" fill="$accentColor" rx="2"/>
            </g>
        """
        )

        // Columns
        var columnIndex = 0
        cols.forEach { (key, value) ->
            val color = value[0].color ?: DOCOPS_BRANDING_COLORS[columnIndex % DOCOPS_BRANDING_COLORS.size]
            val x = 80 + (columnIndex * 360)
            sb.append(makeColumn(key, value, x, color, useDark, columnIndex))
            columnIndex++
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun makeColumn(
        key: String,
        items: List<PlanItem>,
        x: Int,
        color: String,
        useDark: Boolean,
        index: Int
    ): String {
        val sb = StringBuilder()
        val delay = index * 0.1

        sb.append(
            """
            <g transform="translate($x, 180)">
                <text class="column-title" fill="$color">0${index + 1} // ${key.escapeXml()}</text>
                <g transform="translate(0, 30)">
        """
        )

        var currentY = 0
        items.forEach { item ->
            val cardHeight = calculateCardHeight(item)
            sb.append(makeCard(item, currentY, cardHeight, color, useDark))
            currentY += cardHeight + 20
        }

        sb.append("</g></g>")
        return sb.toString()
    }

    private fun makeCard(item: PlanItem, y: Int, height: Int, accentColor: String, useDark: Boolean): String {
        val cardBg = if (useDark) "#18181b" else "#ffffff"
        val cardStroke = if (useDark) "#27272a" else "#e2e8f0"
        val titleColor = if (useDark) "#f4f4f5" else "#18181b"

        val sb = StringBuilder()
        sb.append(
            """
            <g transform="translate(0, $y)">
                <rect width="320" height="$height" rx="20" fill="$cardBg" stroke="$cardStroke"/>
                <rect width="4" height="40" x="0" y="30" fill="$accentColor" rx="2"/>
                <g transform="translate(25, 40)">
        """
        )

        if (item.title != null) {
            sb.append("""<text class="card-title" fill="$titleColor">${item.title.escapeXml()}</text>""")
        }

        val contentY = if (item.title != null) 30f else 10f
        if (!item.content.isNullOrEmpty()) {
            sb.append(renderTextWithBullets(item.content!!, 0f, contentY, item.urlMap, useDark))
        }

        sb.append("</g></g>")
        return sb.toString()
    }

    private fun renderTextWithBullets(text: String, x: Float, y: Float, urlMap: MutableMap<String, String>, useDark: Boolean = false, lineHeight: Float = 20f): String {
        val lines = text.split("\n")
        val result = StringBuilder()
        var currentY = y

        val textColor = if (useDark) "#71717a" else "#4b5563"
        val bulletColor = if (useDark) "#52525b" else "#94a3b8"
        val linkColor = if (useDark) "#60a5fa" else "#2563eb"

        lines.forEach { line ->
            val isBullet = line.startsWith("[BULLET_")
            val cleanLine = if (isBullet) line.substringAfter("]") else line
            val bulletType = if (isBullet) line.substringBetween("[", "]").lowercase() else ""

            val wrappedLines = itemTextWidth(cleanLine, 270F, 13, "Outfit")

            wrappedLines.forEachIndexed { index, wrappedLine ->
                val textX = if (isBullet) x + 15 else x

                if (index == 0 && isBullet) {
                    val symbolId = bulletType.replace("bullet_", "bullet-")
                    result.append("""<use href="#$symbolId" x="${x - 1}" y="${currentY - 10}" width="10" height="10" style="color: $bulletColor"/>""")
                }

                // Process wiki links within the wrapped line
                var processedLine = wrappedLine.escapeXml()
                val wikiRegex = "\\[\\[(.*?)\\]\\]".toRegex()

                val matches = wikiRegex.findAll(processedLine)
                matches.forEach { match ->
                    val label = match.groupValues[1]
                    val fullMatch = match.value
                    val url = urlMap["[[$label]]"]
                    if (url != null) {
                        val anchor = """<a xlink:href="${url.escapeXml()}" target="_blank"><tspan fill="$linkColor" style="text-decoration: underline;">$label</tspan></a>"""
                        processedLine = processedLine.replace(fullMatch, anchor)
                    }
                }

                result.append("""<text x="$textX" y="$currentY" class="card-body" fill="$textColor">$processedLine</text>""")
                currentY += lineHeight
            }
        }
        return result.toString()
    }

    private fun String.substringBetween(start: String, end: String): String =
        substringAfter(start, "").substringBefore(end, "")

    private fun calculateCardHeight(item: PlanItem): Int {
        var linesCount = 0
        item.content?.split("\n")?.forEach { line ->
            val cleanLine = line.replace(Regex("\\[BULLET_.*?\\]"), "• ")
            linesCount += itemTextWidth(cleanLine, 270F, 13, "Outfit").size
        }
        val contentHeight = linesCount * 20
        val base = if (item.title != null) 80 else 50
        return base + contentHeight
    }

    private fun determineWidth(planItems: PlanItems) = (planItems.toColumns().size * 360) + 160
    private fun determineHeight(planItems: PlanItems): Int {
        val maxColHeight = planItems.toColumns().values.maxOf { col ->
            col.sumOf { calculateCardHeight(it) + 20 }
        }
        return maxColHeight + 250
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun makeHead(width: Int, height: Int, useDark: Boolean): String {
        val id = Uuid.random().toHexString()
        return """
        <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height" id="id_$id">
            <defs>
                <style>
                    @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@700;800&amp;family=Outfit:wght@400;600&amp;display=swap');
                    #id_$id .planner-title { font-family: 'Plus Jakarta Sans', sans-serif; font-size: 42px; font-weight: 800; letter-spacing: -0.05em; }
                    #id_$id .column-title { font-family: 'Outfit', sans-serif; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.2em; }
                    #id_$id .card-title { font-family: 'Plus Jakarta Sans', sans-serif; font-size: 16px; font-weight: 700; }
                    #id_$id .card-body { font-family: 'Outfit', sans-serif; font-size: 13px; }
                </style>
                <symbol id="bullet-dot" viewBox="0 0 10 10">
                    <circle cx="5" cy="5" r="2" fill="currentColor"/>
                </symbol>
                <symbol id="bullet-chevron" viewBox="0 0 10 10">
                    <path d="M3 2 L7 5 L3 8" stroke="currentColor" stroke-width="1.5" fill="none"/>
                </symbol>
                <symbol id="bullet-plus" viewBox="0 0 10 10">
                    <path d="M5 2 L5 8 M2 5 L8 5" stroke="currentColor" stroke-width="1.5"/>
                </symbol>
                <symbol id="bullet-dash" viewBox="0 0 10 10">
                    <path d="M2 5 L8 5" stroke="currentColor" stroke-width="1.5"/>
                </symbol>
            </defs>
        """.trimIndent()
    }
}