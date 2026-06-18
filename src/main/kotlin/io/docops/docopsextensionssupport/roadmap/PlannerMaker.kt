package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import java.util.UUID

val DOCOPS_BRANDING_COLORS = listOf(
    "#5BC0FF", // now
    "#8ED2FF", // next
    "#B8A5FF", // later
    "#62E5BA", // done
    "#0AA0FF",
    "#4D8BFF",
    "#7A6BFF",
    "#1DBA86"
)

class PlannerMaker {
    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)

    private val fontFamily = "'Inter', 'Segoe UI', ui-sans-serif, system-ui, sans-serif"
    private val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Inter:wght@450;500;600;700;800&amp;display=swap');"

    private val leftPadding = 48
    private val topPadding = 140
    private val laneWidth = 352
    private val laneGap = 24

    fun makePlannerImage(planItems: PlanItems, title: String, scale: String, useDark: Boolean = false): String {
        theme = ThemeFactory.getTheme(useDark)
        val cols = planItems.toColumns()
        val width = determineWidth(cols.size)
        val height = determineHeight(cols)

        val svgId = "planner_${UUID.randomUUID().toString().replace("-", "")}"
        val ids = DefIds(svgId)

        val sb = StringBuilder()
        sb.append(makeHead(width, height, useDark, svgId, ids))
        sb.append("""<rect width="100%" height="100%" fill="url(#${ids.bgSurface})"/>""")
        sb.append("""<rect width="100%" height="100%" fill="url(#${ids.gridPattern})"/>""")
        sb.append("""<rect width="100%" height="100%" fill="url(#${ids.auroraA})"/>""")
        sb.append("""<rect width="100%" height="100%" fill="url(#${ids.auroraB})"/>""")

        sb.append(
            """
            <g transform="translate(48,44)">
                <g class="reveal d1">
                    <text class="title" x="0" y="34">${title.escapeXml()}</text>
                    <text class="sub" x="0" y="54">${if (useDark) "Glass Night" else "Glass Day"} • compact ${cols.size}-lane board</text>
                    <line x1="0" y1="68" x2="${width - 96}" y2="68" stroke="var(--line)" stroke-width="1"/>
                </g>
            </g>
            """.trimIndent()
        )

        sb.append("""<g transform="translate($leftPadding,$topPadding)">""")
        cols.entries.forEachIndexed { index, (key, value) ->
            val laneX = index * (laneWidth + laneGap)
            val color = value.firstOrNull()?.color ?: DOCOPS_BRANDING_COLORS[index % DOCOPS_BRANDING_COLORS.size]
            sb.append(makeColumn(key, value, laneX, color, index, useDark, ids))
        }
        sb.append("</g>")
        sb.append("</svg>")
        return sb.toString()
    }

    private fun makeColumn(
        key: String,
        items: List<PlanItem>,
        laneX: Int,
        laneColor: String,
        index: Int,
        useDark: Boolean,
        ids: DefIds
    ): String {
        val sb = StringBuilder()
        val revealClass = "d${(index % 4) + 2}"

        sb.append("""<g transform="translate($laneX,0)">""")
        sb.append(
            """
            <g class="reveal $revealClass">
                <text class="col" x="0" y="0">${"%02d".format(index + 1)} // ${key.escapeXml()}</text>
                <rect x="${laneWidth - 60}" y="-14" width="60" height="18" rx="9" fill="$laneColor" fill-opacity="${if (useDark) "0.20" else "0.16"}" stroke="$laneColor" stroke-opacity="${if (useDark) "0.55" else "0.50"}"/>
            </g>
            """.trimIndent()
        )

        var currentY = 16
        sb.append("""<g transform="translate(0,16)">""")
        items.forEachIndexed { itemIndex, item ->
            val cardHeight = calculateCardHeight(item)
            val cardTint = cardTintFor(laneColor, itemIndex, useDark)
            val cardRevealClass = "d${(itemIndex % 3) + 3}"

            sb.append("""<g transform="translate(0,$currentY)">""")
            sb.append(
                """
                <g class="reveal $cardRevealClass">
                    <rect x="8" y="8" width="$laneWidth" height="$cardHeight" rx="18" fill="$cardTint" fill-opacity="${if (useDark) "0.10" else "0.24"}" filter="url(#${ids.glassBlur})"/>
                    <rect x="0" y="0" width="$laneWidth" height="$cardHeight" rx="18" fill="var(--glass)" stroke="var(--stroke)" filter="url(#${ids.softShadow})"/>
                    <path d="M14 12 H${laneWidth - 14} C${laneWidth - 22} 28 ${laneWidth - 46} 38 ${laneWidth - 84} 41 H14 Z" fill="${if (useDark) "rgba(255,255,255,0.16)" else "rgba(255,255,255,0.54)"}"/>
                    <rect x="0" y="50" width="5" height="${minOf(62, cardHeight - 64)}" rx="2.5" fill="$laneColor"/>
                    <text class="meta" x="16" y="30">${key.uppercase().escapeXml()} • item ${itemIndex + 1}</text>
                """.trimIndent()
            )

            if (!item.title.isNullOrBlank()) {
                sb.append("""<text class="cardTitle" x="16" y="58">${item.title.escapeXml()}</text>""")
            }

            val contentY = if (!item.title.isNullOrBlank()) 84f else 58f
            if (!item.content.isNullOrBlank()) {
                sb.append(renderTextWithBullets(item.content!!, 16f, contentY, item.urlMap, useDark, ids))
            }

            sb.append("</g>")
            sb.append("</g>")
            currentY += cardHeight + 12
        }
        sb.append("</g>")
        sb.append("</g>")
        return sb.toString()
    }

    private fun renderTextWithBullets(
        text: String,
        x: Float,
        y: Float,
        urlMap: MutableMap<String, String>,
        useDark: Boolean,
        ids: DefIds,
        lineHeight: Float = 18f
    ): String {
        val lines = text.split("\n").filter { it.isNotBlank() }
        val result = StringBuilder()
        var currentY = y
        val bodyColor = if (useDark) "#DDE7FF" else "#1B2F56"

        lines.forEach { line ->
            val bulletType = when {
                line.startsWith("[BULLET_DOT]") -> ids.bulletDot
                line.startsWith("[BULLET_CHEVRON]") -> ids.bulletChevron
                line.startsWith("[BULLET_PLUS]") -> ids.bulletPlus
                line.startsWith("[BULLET_DASH]") -> ids.bulletDash
                else -> null
            }

            val cleanLine = when (bulletType) {
                ids.bulletDot -> line.substring(12)
                ids.bulletChevron -> line.substring(16)
                ids.bulletPlus, ids.bulletDash -> line.substring(13)
                else -> line
            }

            val wrapped = itemTextWidth(cleanLine, 320F, 11, "Inter")
            val processed = linesToUrlIfExist(wrapped, urlMap)

            processed.forEachIndexed { idx, wrappedLine ->
                if (idx == 0 && bulletType != null) {
                    result.append("""<use href="#$bulletType" x="$x" y="${currentY - 8}" width="8" height="8" style="color: ${if (useDark) "#A8B8DA" else "#4B638C"}"/>""")
                }
                val tx = if (bulletType != null) x + 12 else x
                if (wrappedLine.contains("<a xlink:href=")) {
                    result.append("""<text x="$tx" y="$currentY" class="body" fill="$bodyColor">$wrappedLine</text>""")
                } else {
                    result.append("""<text x="$tx" y="$currentY" class="body" fill="$bodyColor">${wrappedLine.escapeXml()}</text>""")
                }
                currentY += lineHeight
            }
        }

        return result.toString()
    }

    private fun calculateCardHeight(item: PlanItem): Int {
        val base = if (!item.title.isNullOrBlank()) 96 else 70
        var lineCount = 0

        item.content?.split("\n")
            ?.filter { it.isNotBlank() }
            ?.forEach { line ->
                val clean = line
                    .replace("[BULLET_DOT]", "")
                    .replace("[BULLET_CHEVRON]", "")
                    .replace("[BULLET_PLUS]", "")
                    .replace("[BULLET_DASH]", "")
                lineCount += itemTextWidth(clean, 320F, 11, "Inter").size
            }

        return base + (lineCount * 18) + 12
    }

    private fun determineWidth(columnCount: Int): Int {
        val contentWidth = leftPadding + (columnCount * laneWidth) + ((columnCount - 1).coerceAtLeast(0) * laneGap) + 72
        return maxOf(1600, contentWidth)
    }

    private fun determineHeight(cols: Map<String, List<PlanItem>>): Int {
        val maxColHeight = cols.values.maxOfOrNull { list ->
            list.sumOf { calculateCardHeight(it) + 12 }
        } ?: 0
        val contentHeight = topPadding + 16 + maxColHeight + 24
        return maxOf(430, contentHeight)
    }

    private fun cardTintFor(color: String, index: Int, useDark: Boolean): String {
        if (index == 0) return color
        return if (useDark) "#BFD0FF" else "#9BD8FF"
    }

    private fun createBulletSymbols(ids: DefIds): String {
        return """
            <symbol id="${ids.bulletDot}" viewBox="0 0 10 10">
                <circle cx="5" cy="5" r="2" fill="currentColor"/>
            </symbol>
            <symbol id="${ids.bulletChevron}" viewBox="0 0 10 10">
                <path d="M3 2 L7 5 L3 8" stroke="currentColor" stroke-width="1.5" fill="none"/>
            </symbol>
            <symbol id="${ids.bulletPlus}" viewBox="0 0 10 10">
                <path d="M5 2 L5 8 M2 5 L8 5" stroke="currentColor" stroke-width="1.5"/>
            </symbol>
            <symbol id="${ids.bulletDash}" viewBox="0 0 10 10">
                <path d="M2 5 L8 5" stroke="currentColor" stroke-width="1.5"/>
            </symbol>
        """.trimIndent()
    }

    private fun makeHead(width: Int, height: Int, useDark: Boolean, svgId: String, ids: DefIds): String {
        val bgStart = if (useDark) "#0A1026" else "#F7FBFF"
        val bgMid = if (useDark) "#0F1B3A" else "#EEF5FF"
        val bgEnd = if (useDark) "#0A1430" else "#EAF2FF"

        val auroraA = if (useDark) "#53B8FF" else "#4CB4FF"
        val auroraB = if (useDark) "#9D86FF" else "#8E7BFF"
        val auroraAOpacity = if (useDark) "0.26" else "0.16"
        val auroraBOpacity = if (useDark) "0.20" else "0.12"

        val gridStroke = if (useDark) "#BFD0FF" else "#2C4A7A"
        val text = if (useDark) "#EAF1FF" else "#0E1A33"
        val muted = if (useDark) "#A8B8DA" else "#4B638C"
        val line = if (useDark) "rgba(197,212,255,0.28)" else "rgba(42,73,124,0.22)"
        val glass = if (useDark) "rgba(255,255,255,0.10)" else "rgba(255,255,255,0.58)"
        val stroke = if (useDark) "rgba(219,230,255,0.34)" else "rgba(141,171,218,0.62)"

        val shadowBlur = if (useDark) 8 else 6
        val shadowOffset = if (useDark) 6 else 4
        val shadowSlope = if (useDark) 0.35 else 0.18
        val glassBlur = if (useDark) 5 else 4
        val bodyDefault = if (useDark) "#DDE7FF" else "#1B2F56"

        return """
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height" id="$svgId" xmlns:xlink="http://www.w3.org/1999/xlink">
                <defs>
                    <linearGradient id="${ids.bgSurface}" x1="0" y1="0" x2="1" y2="1">
                        <stop offset="0%" stop-color="$bgStart"/>
                        <stop offset="55%" stop-color="$bgMid"/>
                        <stop offset="100%" stop-color="$bgEnd"/>
                    </linearGradient>

                    <radialGradient id="${ids.auroraA}" cx="20%" cy="20%" r="48%">
                        <stop offset="0%" stop-color="$auroraA" stop-opacity="$auroraAOpacity"/>
                        <stop offset="100%" stop-color="$auroraA" stop-opacity="0"/>
                    </radialGradient>

                    <radialGradient id="${ids.auroraB}" cx="86%" cy="18%" r="40%">
                        <stop offset="0%" stop-color="$auroraB" stop-opacity="$auroraBOpacity"/>
                        <stop offset="100%" stop-color="$auroraB" stop-opacity="0"/>
                    </radialGradient>

                    <filter id="${ids.glassBlur}" x="-35%" y="-35%" width="170%" height="170%">
                        <feGaussianBlur stdDeviation="$glassBlur"/>
                    </filter>

                    <filter id="${ids.softShadow}" x="-50%" y="-50%" width="200%" height="200%">
                        <feGaussianBlur in="SourceAlpha" stdDeviation="$shadowBlur"/>
                        <feOffset dx="0" dy="$shadowOffset"/>
                        <feComponentTransfer>
                            <feFuncA type="linear" slope="$shadowSlope"/>
                        </feComponentTransfer>
                        <feMerge>
                            <feMergeNode/>
                            <feMergeNode in="SourceGraphic"/>
                        </feMerge>
                    </filter>

                    <pattern id="${ids.gridPattern}" width="28" height="28" patternUnits="userSpaceOnUse">
                        <path d="M28 0H0V28" fill="none" stroke="$gridStroke" stroke-opacity="0.08" stroke-width="1"/>
                    </pattern>

                    ${createBulletSymbols(ids)}

                    <style>
                        $fontImport
                        #$svgId{
                            --text:$text;
                            --muted:$muted;
                            --line:$line;
                            --glass:$glass;
                            --stroke:$stroke;
                        }

                        #$svgId .title{
                            font-family:$fontFamily;
                            font-size:38px;
                            font-weight:800;
                            letter-spacing:-0.01em;
                            fill:var(--text);
                        }

                        #$svgId .sub{
                            font-family:$fontFamily;
                            font-size:12px;
                            font-weight:500;
                            fill:var(--muted);
                        }

                        #$svgId .col{
                            font-family:$fontFamily;
                            font-size:10px;
                            font-weight:700;
                            letter-spacing:0.14em;
                            text-transform:uppercase;
                            fill:var(--text);
                        }

                        #$svgId .meta{
                            font-family:$fontFamily;
                            font-size:9px;
                            font-weight:600;
                            letter-spacing:0.08em;
                            text-transform:uppercase;
                            fill:var(--muted);
                        }

                        #$svgId .cardTitle{
                            font-family:$fontFamily;
                            font-size:14px;
                            font-weight:700;
                            fill:var(--text);
                        }

                        #$svgId .body{
                            font-family:$fontFamily;
                            font-size:11px;
                            font-weight:450;
                            fill:$bodyDefault;
                        }

                        #$svgId .reveal{
                            opacity:0;
                            animation:planner-rise-${ids.animSuffix} ${if (useDark) ".48s" else ".45s"} cubic-bezier(.2,.8,.2,1) forwards;
                        }

                        #$svgId .d1{animation-delay:.07s;}
                        #$svgId .d2{animation-delay:.15s;}
                        #$svgId .d3{animation-delay:.23s;}
                        #$svgId .d4{animation-delay:.31s;}
                        #$svgId .d5{animation-delay:.39s;}

                        @keyframes planner-rise-${ids.animSuffix}{
                            from{opacity:0; transform:translateY(8px);}
                            to{opacity:1; transform:translateY(0);}
                        }
                    </style>
                </defs>
        """.trimIndent()
    }

    private data class DefIds(val svgId: String) {
        val bgSurface = "${svgId}_bgSurface"
        val auroraA = "${svgId}_auroraA"
        val auroraB = "${svgId}_auroraB"
        val glassBlur = "${svgId}_glassBlur"
        val softShadow = "${svgId}_softShadow"
        val gridPattern = "${svgId}_gridPattern"

        val bulletDot = "${svgId}_bullet_dot"
        val bulletChevron = "${svgId}_bullet_chevron"
        val bulletPlus = "${svgId}_bullet_plus"
        val bulletDash = "${svgId}_bullet_dash"

        val animSuffix = svgId
    }
}