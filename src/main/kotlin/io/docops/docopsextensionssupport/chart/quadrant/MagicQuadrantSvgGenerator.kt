package io.docops.docopsextensionssupport.chart.quadrant

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MagicQuadrantSvgGenerator {

    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)

    private data class Layout(
        val baseWidth: Int = 700,
        val baseHeight: Int = 700,
        val boardX: Int = 40,
        val boardY: Int = 28,
        val boardWidth: Int = 620,
        val boardHeight: Int = 644,
        val margin: Int = 60,
        val chartStartY: Int = 110,
        val chartBottomReserve: Int = 100
    ) {
        val chartWidth: Int = baseWidth - 2 * margin
        val chartHeight: Int = baseHeight - 2 * margin - chartBottomReserve
        val quadrantWidth: Int = chartWidth / 2
        val quadrantHeight: Int = chartHeight / 2
        val chartCenterX: Int = margin + quadrantWidth
        val chartCenterY: Int = chartStartY + quadrantHeight
        val xAxisY: Int = baseHeight - 28
        val yAxisX: Int = 24
    }

    private data class DefIds(val svgId: String) {
        val bg = "bg_$svgId"
        val washA = "washA_$svgId"
        val washB = "washB_$svgId"
        val grid = "grid_$svgId"
        val plotVignette = "plot_vignette_$svgId"
        val axisArrow = "axis_arrow_$svgId"

        val qLeaders = "q_leaders_$svgId"
        val qChallengers = "q_challengers_$svgId"
        val qVisionaries = "q_visionaries_$svgId"
        val qNiche = "q_niche_$svgId"

        val bLeaders = "b_leaders_$svgId"
        val bChallengers = "b_challengers_$svgId"
        val bVisionaries = "b_visionaries_$svgId"
        val bNiche = "b_niche_$svgId"

        val boardShadow = "boardShadow_$svgId"
        val nodeGlow = "nodeGlow_$svgId"
        val labelShadow = "labelShadow_$svgId"
    }

    private data class QuadrantPalette(
        val leaders: String = "#12B886",
        val challengers: String = "#F59F00",
        val visionaries: String = "#3B82F6",
        val niche: String = "#F43F5E"
    )

    private data class CompanyPoint(
        val company: QuadrantCompany,
        val normalizedX: Double,
        val normalizedY: Double,
        val screenX: Int,
        val screenY: Int,
        val radius: Int,
        val quadrant: QuadrantType,
        val isFocus: Boolean,
        val showLabel: Boolean
    )

    private data class LabelMetrics(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val textAnchor: String,
        val textX: Int,
        val textY: Int,
        val connectorX: Int,
        val connectorY: Int
    )

    private enum class QuadrantType {
        LEADERS,
        CHALLENGERS,
        VISIONARIES,
        NICHE
    }

    @OptIn(ExperimentalUuidApi::class)
    fun generateMagicQuadrant(
        config: MagicQuadrantConfig,
        scale: String = "1.0",
        isPdf: Boolean = false
    ): String {
        val svgId = generateSvgId(config.title)
        val ids = DefIds(svgId)
        val layout = Layout()
        val palette = QuadrantPalette()

        theme = ThemeFactory.getTheme(config)

        val scaleFactor = scale.toDoubleOrNull()
            ?.takeIf { it.isFinite() && it > 0.0 }
            ?: 1.0

        val width = (layout.baseWidth * scaleFactor).roundToInt().coerceAtLeast(1)
        val height = (layout.baseHeight * scaleFactor).roundToInt().coerceAtLeast(1)

        val focusCompany = config.companies.maxByOrNull { it.size }
        val points = createCompanyPoints(config, layout, focusCompany)

        val titleId = "${svgId}_title"
        val descId = "${svgId}_desc"

        return buildString {
            append("""<?xml version="1.0" encoding="UTF-8"?>""")
            append(
                """<svg width="$width" height="$height" viewBox="0 0 ${layout.baseWidth} ${layout.baseHeight}" xmlns="http://www.w3.org/2000/svg" id="$svgId" role="img" aria-labelledby="$titleId $descId">"""
            )
            append("""<title id="$titleId">${escapeXml(config.title)}</title>""")
            append(
                """<desc id="$descId">${escapeXml(buildDescription(config, points))}</desc>"""
            )

            append(generateStyles(svgId, config.useDark, isPdf))
            append(generateDefs(ids, config.useDark, isPdf))

            appendBackground(this, ids, isPdf, config.useDark, layout)
            appendBoard(this, layout)
            appendHeader(this, config, layout)
            appendQuadrantChart(this, config, layout, ids, palette, isPdf)
            appendAxisLabels(this, config, layout, ids, isPdf)
            appendCompanies(this, points, layout, ids, palette, isPdf)

            append("</svg>")
        }
    }

    private fun createCompanyPoints(
        config: MagicQuadrantConfig,
        layout: Layout,
        focusCompany: QuadrantCompany?
    ): List<CompanyPoint> {
        val showAllLabels = config.companies.size <= 16
        val showProminentLabels = config.companies.size <= 30
        val prominentThreshold = config.companies
            .map { it.size }
            .sortedDescending()
            .getOrNull(min(7, config.companies.lastIndex))
            ?: Int.MIN_VALUE

        return config.companies.map { company ->
            val normalizedX = company.x.coerceIn(0.0, 100.0)
            val normalizedY = company.y.coerceIn(0.0, 100.0)
            val radius = company.size.coerceIn(8, 25)
            val quadrant = getQuadrant(normalizedX, normalizedY)
            val isFocus = focusCompany == company

            CompanyPoint(
                company = company,
                normalizedX = normalizedX,
                normalizedY = normalizedY,
                screenX = layout.margin + (normalizedX / 100.0 * layout.chartWidth).roundToInt(),
                screenY = layout.chartStartY + layout.chartHeight - (normalizedY / 100.0 * layout.chartHeight).roundToInt(),
                radius = radius,
                quadrant = quadrant,
                isFocus = isFocus,
                showLabel = showAllLabels || isFocus || (showProminentLabels && company.size >= prominentThreshold)
            )
        }
    }

    private fun appendBackground(sb: StringBuilder, ids: DefIds, isPdf: Boolean, useDark: Boolean, layout: Layout) {
        if (isPdf) {
            val bgColor = if (useDark) "#071126" else "#F7FAFF"
            sb.append("""<rect width="${layout.baseWidth}" height="${layout.baseHeight}" fill="$bgColor"/>""")
        } else {
            sb.append("""<rect width="100%" height="100%" fill="url(#${ids.bg})"/>""")
        }
        sb.append("""<rect width="100%" height="100%" fill="url(#${ids.grid})"/>""")
        if (!isPdf) {
            sb.append("""<rect width="100%" height="100%" fill="url(#${ids.washA})"/>""")
            sb.append("""<rect width="100%" height="100%" fill="url(#${ids.washB})"/>""")
        }
    }

    private fun appendBoard(sb: StringBuilder, layout: Layout) {
        sb.append("""<g transform="translate(${layout.boardX},${layout.boardY})">""")
        sb.append("""<g class="reveal d1">""")
        sb.append(
            """<rect x="0" y="0" width="${layout.boardWidth}" height="${layout.boardHeight}" rx="22" class="board"/>"""
        )
        sb.append("""<path d="M24 84 H596" class="board-rule"/>""")
        sb.append("</g>")
        sb.append("</g>")
    }

    private fun appendHeader(sb: StringBuilder, config: MagicQuadrantConfig, layout: Layout) {
        sb.append("""<g transform="translate(${layout.boardX},${layout.boardY})">""")
        sb.append("""<g class="reveal d1">""")
        sb.append(
            """<text x="${layout.boardWidth / 2}" y="39" text-anchor="middle" class="title-text">${escapeXml(config.title)}</text>"""
        )
        sb.append(
            """<text x="${layout.boardWidth / 2}" y="61" text-anchor="middle" class="sub-text">Analyst positioning map • ${config.companies.size} ${if (config.companies.size == 1) "company" else "companies"}</text>"""
        )
        sb.append("</g>")
        sb.append("</g>")
    }

    private fun appendQuadrantChart(
        sb: StringBuilder,
        config: MagicQuadrantConfig,
        layout: Layout,
        ids: DefIds,
        palette: QuadrantPalette,
        isPdf: Boolean
    ) {
        val leadersLabel = config.leadersLabel.uppercase()
        val challengersLabel = config.challengersLabel.uppercase()
        val visionariesLabel = config.visionariesLabel.uppercase()
        val nicheLabel = config.nichePlayersLabel.uppercase()

        sb.append("""<g transform="translate(${layout.margin},${layout.chartStartY})">""")
        sb.append("""<g class="reveal d2">""")

        if (isPdf) {
            val qOpacity = if (config.useDark) "0.18" else "0.12"
            sb.append("""<rect x="0" y="0" width="${layout.quadrantWidth}" height="${layout.quadrantHeight}" fill="${palette.challengers}" fill-opacity="$qOpacity"/>""")
            sb.append("""<rect x="${layout.quadrantWidth}" y="0" width="${layout.quadrantWidth}" height="${layout.quadrantHeight}" fill="${palette.leaders}" fill-opacity="$qOpacity"/>""")
            sb.append("""<rect x="0" y="${layout.quadrantHeight}" width="${layout.quadrantWidth}" height="${layout.quadrantHeight}" fill="${palette.niche}" fill-opacity="$qOpacity"/>""")
            sb.append("""<rect x="${layout.quadrantWidth}" y="${layout.quadrantHeight}" width="${layout.quadrantWidth}" height="${layout.quadrantHeight}" fill="${palette.visionaries}" fill-opacity="$qOpacity"/>""")
        } else {
            sb.append(
                """<rect x="0" y="0" width="${layout.quadrantWidth}" height="${layout.quadrantHeight}" fill="url(#${ids.qChallengers})"/>"""
            )
            sb.append(
                """<rect x="${layout.quadrantWidth}" y="0" width="${layout.quadrantWidth}" height="${layout.quadrantHeight}" fill="url(#${ids.qLeaders})"/>"""
            )
            sb.append(
                """<rect x="0" y="${layout.quadrantHeight}" width="${layout.quadrantWidth}" height="${layout.quadrantHeight}" fill="url(#${ids.qNiche})"/>"""
            )
            sb.append(
                """<rect x="${layout.quadrantWidth}" y="${layout.quadrantHeight}" width="${layout.quadrantWidth}" height="${layout.quadrantHeight}" fill="url(#${ids.qVisionaries})"/>"""
            )

            sb.append(
                """<rect x="0" y="0" width="${layout.chartWidth}" height="${layout.chartHeight}" rx="14" fill="url(#${ids.plotVignette})" class="plot-vignette"/>"""
            )
        }

        appendChartMicroGrid(sb, layout)

        sb.append(
            """<line x1="${layout.quadrantWidth}" y1="0" x2="${layout.quadrantWidth}" y2="${layout.chartHeight}" class="split-line" stroke-dasharray="7 7"/>"""
        )
        sb.append(
            """<line x1="0" y1="${layout.quadrantHeight}" x2="${layout.chartWidth}" y2="${layout.quadrantHeight}" class="split-line" stroke-dasharray="7 7"/>"""
        )
        sb.append(
            """<circle cx="${layout.quadrantWidth}" cy="${layout.quadrantHeight}" r="4" class="center-pin"/>"""
        )
        sb.append(
            """<rect x="0" y="0" width="${layout.chartWidth}" height="${layout.chartHeight}" rx="14" fill="none" class="chart-border"/>"""
        )
        sb.append("</g>")

        appendQuadrantChip(sb, challengersLabel, 12, 12, palette.challengers)
        appendQuadrantChip(
            sb,
            leadersLabel,
            layout.chartWidth - estimateChipWidth(leadersLabel) - 12,
            12,
            palette.leaders
        )
        appendQuadrantChip(sb, nicheLabel, 12, layout.chartHeight - 40, palette.niche)
        appendQuadrantChip(
            sb,
            visionariesLabel,
            layout.chartWidth - estimateChipWidth(visionariesLabel) - 12,
            layout.chartHeight - 40,
            palette.visionaries
        )

        sb.append("</g>")
    }

    private fun appendChartMicroGrid(sb: StringBuilder, layout: Layout) {
        val xStep = layout.chartWidth / 4
        val yStep = layout.chartHeight / 4

        for (i in 1..3) {
            val x = i * xStep
            val y = i * yStep
            sb.append("""<line x1="$x" y1="0" x2="$x" y2="${layout.chartHeight}" class="micro-grid-line"/>""")
            sb.append("""<line x1="0" y1="$y" x2="${layout.chartWidth}" y2="$y" class="micro-grid-line"/>""")
        }
    }

    private fun appendAxisLabels(
        sb: StringBuilder,
        config: MagicQuadrantConfig,
        layout: Layout,
        ids: DefIds,
        isPdf: Boolean
    ) {
        val leftXAxis = config.xAxisLabel.ifBlank { "Ability to Execute" }.uppercase()
        val rightXAxis = config.xAxisLabelEnd.uppercase()
        val bottomYAxis = config.yAxisLabel.ifBlank { "Completeness of Vision" }.uppercase()
        val topYAxis = config.yAxisLabelEnd.uppercase()

        sb.append("""<g class="reveal d3">""")

        sb.append(
            """<line x1="${layout.margin}" y1="${layout.xAxisY - 10}" x2="${layout.margin + layout.chartWidth}" y2="${layout.xAxisY - 10}" class="axis-guide" marker-end="url(#${ids.axisArrow})"/>"""
        )
        sb.append(
            """<line x1="${layout.yAxisX + 10}" y1="${layout.chartStartY + layout.chartHeight}" x2="${layout.yAxisX + 10}" y2="${layout.chartStartY}" class="axis-guide" marker-end="url(#${ids.axisArrow})"/>"""
        )

        sb.append(
            """<text x="${layout.margin + layout.quadrantWidth / 2}" y="${layout.xAxisY}" text-anchor="middle" class="axis-label">${escapeXml(leftXAxis)}</text>"""
        )

        if (rightXAxis.isNotBlank()) {
            sb.append(
                """<text x="${layout.margin + layout.quadrantWidth + layout.quadrantWidth / 2}" y="${layout.xAxisY}" text-anchor="middle" class="axis-label">${escapeXml(rightXAxis)}</text>"""
            )
        } else {
            sb.append(
                """<text x="${layout.margin + layout.chartWidth - 4}" y="${layout.xAxisY - 18}" text-anchor="end" class="axis-end-label">HIGHER</text>"""
            )
        }

        val yBottomHalfCenter = layout.chartStartY + layout.quadrantHeight + layout.quadrantHeight / 2
        val yTopHalfCenter = layout.chartStartY + layout.quadrantHeight / 2

        sb.append(
            """<text x="${layout.yAxisX}" y="$yBottomHalfCenter" text-anchor="middle" class="axis-label" transform="rotate(-90 ${layout.yAxisX} $yBottomHalfCenter)">${escapeXml(bottomYAxis)}</text>"""
        )

        if (topYAxis.isNotBlank()) {
            sb.append(
                """<text x="${layout.yAxisX}" y="$yTopHalfCenter" text-anchor="middle" class="axis-label" transform="rotate(-90 ${layout.yAxisX} $yTopHalfCenter)">${escapeXml(topYAxis)}</text>"""
            )
        } else {
            sb.append(
                """<text x="${layout.yAxisX + 24}" y="${layout.chartStartY + 12}" text-anchor="start" class="axis-end-label">HIGHER</text>"""
            )
        }

        sb.append("</g>")
    }

    private fun appendCompanies(
        sb: StringBuilder,
        points: List<CompanyPoint>,
        layout: Layout,
        ids: DefIds,
        palette: QuadrantPalette,
        isPdf: Boolean
    ) {
        points.forEachIndexed { index, point ->
            val delayClass = "d${(index % 4) + 2}"
            val bubbleId = getBubbleId(ids, point.quadrant)
            val labelMetrics = computeLabelMetrics(point, layout)

            sb.append("""<g transform="translate(${point.screenX},${point.screenY})">""")
            sb.append("""<g class="reveal $delayClass">""")

            val nodeHasLink = point.company.url.isNotBlank()
            if (nodeHasLink) {
                sb.append(
                    """<a href="${escapeXml(point.company.url.trim())}" target="_blank" rel="noopener noreferrer">"""
                )
            }

            sb.append(
                """<g class="company-node" role="listitem" aria-label="${escapeXml(point.company.name)}">"""
            )

            val titleText = if (point.company.description.isNotBlank()) {
                "${point.company.name}: ${point.company.description}"
            } else {
                "${point.company.name} — x ${formatScore(point.normalizedX)}, y ${formatScore(point.normalizedY)}"
            }
            sb.append("""<title>${escapeXml(titleText)}</title>""")

            if (point.isFocus) {
                sb.append(
                    """<circle cx="0" cy="0" r="${point.radius + 8}" class="${if (isPdf) "focus-ring" else "focus-pulse"}" fill="none" stroke="var(--focus)" stroke-width="2.4"/>"""
                )
                sb.append(
                    """<circle cx="0" cy="0" r="${point.radius + 3}" class="focus-inner-ring" fill="none" stroke="var(--focus)" stroke-width="1.2"/>"""
                )
            }

            sb.append(
                """<circle cx="0" cy="0" r="${point.radius}" fill="${if (isPdf) colorFor(point.quadrant, palette) else "url(#$bubbleId)"}" class="node-core"/>"""
            )
            sb.append(
                """<circle cx="${-(point.radius * 0.25).roundToInt()}" cy="${-(point.radius * 0.30).roundToInt()}" r="${max(2, (point.radius * 0.22).roundToInt())}" class="node-specular"/>"""
            )

            if (point.showLabel) {
                sb.append(
                    """<line x1="0" y1="0" x2="${labelMetrics.connectorX}" y2="${labelMetrics.connectorY}" class="label-connector"/>"""
                )
                sb.append(
                    """<rect x="${labelMetrics.x}" y="${labelMetrics.y}" width="${labelMetrics.width}" height="${labelMetrics.height}" rx="8" class="company-label-bg" ${if (isPdf) "" else "filter=\"url(#${ids.labelShadow})\""}/>"""
                )
                sb.append(
                    """<text x="${labelMetrics.textX}" y="${labelMetrics.textY}" text-anchor="${labelMetrics.textAnchor}" class="company-name">${escapeXml(ellipsize(point.company.name, 24))}</text>"""
                )
            }

            sb.append("</g>")

            if (nodeHasLink) {
                sb.append("</a>")
            }

            sb.append("</g>")
            sb.append("</g>")
        }
    }

    private fun appendQuadrantChip(sb: StringBuilder, text: String, x: Int, y: Int, color: String) {
        val width = estimateChipWidth(text)
        sb.append("""<g class="reveal d3">""")
        sb.append(
            """<rect x="$x" y="$y" width="$width" height="28" rx="14" class="chip-bg" stroke="$color" stroke-opacity="0.55"/>"""
        )
        sb.append("""<circle cx="${x + 15}" cy="${y + 14}" r="4" fill="$color" opacity="0.9"/>""")
        sb.append(
            """<text x="${x + 28}" y="${y + 18}" text-anchor="start" class="quad-chip-text" fill="$color">${escapeXml(text)}</text>"""
        )
        sb.append("</g>")
    }

    private fun computeLabelMetrics(point: CompanyPoint, layout: Layout): LabelMetrics {
        val text = ellipsize(point.company.name, 24)
        val width = estimateLabelWidth(text)
        val height = 24

        val prefersRight = point.normalizedX < 72.0
        val prefersBelow = point.normalizedY > 62.0

        val horizontalGap = point.radius + 10
        val verticalGap = point.radius + 14

        val rawX = if (prefersRight) horizontalGap else -horizontalGap - width
        val rawY = if (prefersBelow) -verticalGap - height else verticalGap

        val absoluteMinX = layout.margin
        val absoluteMaxX = layout.margin + layout.chartWidth
        val absoluteMinY = layout.chartStartY
        val absoluteMaxY = layout.chartStartY + layout.chartHeight

        val absoluteLabelX = (point.screenX + rawX).coerceIn(
            absoluteMinX + 6,
            absoluteMaxX - width - 6
        )
        val absoluteLabelY = (point.screenY + rawY).coerceIn(
            absoluteMinY + 6,
            absoluteMaxY - height - 6
        )

        val localX = absoluteLabelX - point.screenX
        val localY = absoluteLabelY - point.screenY
        val anchor = if (point.screenX < absoluteLabelX + width / 2) "start" else "end"
        val textX = if (anchor == "start") localX + 10 else localX + width - 10
        val textY = localY + 16

        val connectorX = when {
            point.screenX < absoluteLabelX -> localX
            point.screenX > absoluteLabelX + width -> localX + width
            else -> localX + width / 2
        }
        val connectorY = when {
            point.screenY < absoluteLabelY -> localY
            point.screenY > absoluteLabelY + height -> localY + height
            else -> localY + height / 2
        }

        return LabelMetrics(
            x = localX,
            y = localY,
            width = width,
            height = height,
            textAnchor = anchor,
            textX = textX,
            textY = textY,
            connectorX = connectorX,
            connectorY = connectorY
        )
    }

    private fun estimateChipWidth(text: String): Int {
        return max(108, (text.length * 7.4).roundToInt() + 42)
    }

    private fun estimateLabelWidth(text: String): Int {
        return max(76, (text.length * 6.7).roundToInt() + 22)
    }

    private fun getQuadrant(x: Double, y: Double): QuadrantType {
        return when {
            x >= 50.0 && y >= 50.0 -> QuadrantType.LEADERS
            x < 50.0 && y >= 50.0 -> QuadrantType.CHALLENGERS
            x >= 50.0 && y < 50.0 -> QuadrantType.VISIONARIES
            else -> QuadrantType.NICHE
        }
    }

    private fun colorFor(quadrant: QuadrantType, palette: QuadrantPalette): String {
        return when (quadrant) {
            QuadrantType.LEADERS -> palette.leaders
            QuadrantType.CHALLENGERS -> palette.challengers
            QuadrantType.VISIONARIES -> palette.visionaries
            QuadrantType.NICHE -> palette.niche
        }
    }

    private fun getBubbleId(ids: DefIds, quadrant: QuadrantType): String {
        return when (quadrant) {
            QuadrantType.LEADERS -> ids.bLeaders
            QuadrantType.CHALLENGERS -> ids.bChallengers
            QuadrantType.VISIONARIES -> ids.bVisionaries
            QuadrantType.NICHE -> ids.bNiche
        }
    }

    private fun generateDefs(ids: DefIds, useDark: Boolean, isPdf: Boolean = false): String {
        val bg0 = if (useDark) "#071126" else "#F7FAFF"
        val bg1 = if (useDark) "#101A3A" else "#EAF2FF"
        val bg2 = if (useDark) "#0B1430" else "#E1EBFA"

        val washAColor = if (useDark) "#36D1DC" else "#1E88E5"
        val washBColor = if (useDark) "#F7B733" else "#F59F00"
        val washAOpacity = if (useDark) "0.18" else "0.12"
        val washBOpacity = if (useDark) "0.14" else "0.10"

        val gridColor = if (useDark) "#BFD0FF" else "#2A487A"
        val gridOpacity = if (useDark) "0.18" else "0.14"
        val qOpacity = if (useDark) "0.24" else "0.17"

        val vignetteCenter = if (useDark) "rgba(255,255,255,0.06)" else "rgba(255,255,255,0.34)"
        val vignetteEdge = if (useDark) "rgba(0,0,0,0.18)" else "rgba(20,43,88,0.06)"

        return """
            <defs>
                <linearGradient id="${ids.bg}" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="$bg0"/>
                    <stop offset="52%" stop-color="$bg1"/>
                    <stop offset="100%" stop-color="$bg2"/>
                </linearGradient>

                ${if (isPdf) "" else """
                <radialGradient id="${ids.washA}" cx="16%" cy="18%" r="56%">
                    <stop offset="0%" stop-color="$washAColor" stop-opacity="$washAOpacity"/>
                    <stop offset="100%" stop-color="$washAColor" stop-opacity="0"/>
                </radialGradient>

                <radialGradient id="${ids.washB}" cx="90%" cy="14%" r="48%">
                    <stop offset="0%" stop-color="$washBColor" stop-opacity="$washBOpacity"/>
                    <stop offset="100%" stop-color="$washBColor" stop-opacity="0"/>
                </radialGradient>
                
                <radialGradient id="${ids.plotVignette}" cx="50%" cy="48%" r="78%">
                    <stop offset="0%" stop-color="$vignetteCenter"/>
                    <stop offset="100%" stop-color="$vignetteEdge"/>
                </radialGradient>
                """}

                <pattern id="${ids.grid}" width="32" height="32" patternUnits="userSpaceOnUse">
                    <path d="M32 0H0V32" fill="none" stroke="$gridColor" stroke-opacity="$gridOpacity" stroke-width="1"/>
                    <path d="M0 0L32 32" fill="none" stroke="$gridColor" stroke-opacity="${(gridOpacity.toDouble() * 0.38)}" stroke-width="0.35"/>
                </pattern>

                <marker id="${ids.axisArrow}" markerWidth="7" markerHeight="7" refX="5" refY="3.5" orient="auto" markerUnits="strokeWidth">
                    <path d="M0,0 L6,3.5 L0,7 Z" class="axis-arrow"/>
                </marker>

                <radialGradient id="${ids.qLeaders}" cx="82%" cy="18%" r="92%">
                    <stop offset="0%" stop-color="#12B886" stop-opacity="$qOpacity"/>
                    <stop offset="100%" stop-color="#12B886" stop-opacity="0"/>
                </radialGradient>
                <radialGradient id="${ids.qChallengers}" cx="18%" cy="18%" r="92%">
                    <stop offset="0%" stop-color="#F59F00" stop-opacity="$qOpacity"/>
                    <stop offset="100%" stop-color="#F59F00" stop-opacity="0"/>
                </radialGradient>
                <radialGradient id="${ids.qVisionaries}" cx="82%" cy="82%" r="92%">
                    <stop offset="0%" stop-color="#3B82F6" stop-opacity="$qOpacity"/>
                    <stop offset="100%" stop-color="#3B82F6" stop-opacity="0"/>
                </radialGradient>
                <radialGradient id="${ids.qNiche}" cx="18%" cy="82%" r="92%">
                    <stop offset="0%" stop-color="#F43F5E" stop-opacity="$qOpacity"/>
                    <stop offset="100%" stop-color="#F43F5E" stop-opacity="0"/>
                </radialGradient>

                <radialGradient id="${ids.bLeaders}" cx="34%" cy="28%" r="76%">
                    <stop offset="0%" stop-color="#A7F3D0"/>
                    <stop offset="48%" stop-color="#35D0A0"/>
                    <stop offset="100%" stop-color="#07895F"/>
                </radialGradient>
                <radialGradient id="${ids.bChallengers}" cx="34%" cy="28%" r="76%">
                    <stop offset="0%" stop-color="#FFE8A3"/>
                    <stop offset="48%" stop-color="#FDBA32"/>
                    <stop offset="100%" stop-color="#C77700"/>
                </radialGradient>
                <radialGradient id="${ids.bVisionaries}" cx="34%" cy="28%" r="76%">
                    <stop offset="0%" stop-color="#BFDBFE"/>
                    <stop offset="48%" stop-color="#60A5FA"/>
                    <stop offset="100%" stop-color="#1D4ED8"/>
                </radialGradient>
                <radialGradient id="${ids.bNiche}" cx="34%" cy="28%" r="76%">
                    <stop offset="0%" stop-color="#FDA4AF"/>
                    <stop offset="48%" stop-color="#FB7185"/>
                    <stop offset="100%" stop-color="#BE123C"/>
                </radialGradient>

                <filter id="${ids.boardShadow}" x="-40%" y="-40%" width="180%" height="180%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="${if (useDark) 5 else 4}"/>
                    <feOffset dx="0" dy="${if (useDark) 5 else 4}"/>
                    <feComponentTransfer>
                        <feFuncA type="linear" slope="${if (useDark) 0.34 else 0.16}"/>
                    </feComponentTransfer>
                    <feMerge>
                        <feMergeNode/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>

                <filter id="${ids.nodeGlow}" x="-70%" y="-70%" width="240%" height="240%">
                    <feGaussianBlur stdDeviation="${if (useDark) 4 else 3}" result="blur"/>
                    <feColorMatrix in="blur" type="matrix" values="
                        1 0 0 0 0
                        0 1 0 0 0
                        0 0 1 0 0
                        0 0 0 ${if (useDark) 0.58 else 0.36} 0
                    " result="glow"/>
                    <feMerge>
                        <feMergeNode in="glow"/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>

                <filter id="${ids.labelShadow}" x="-30%" y="-80%" width="160%" height="260%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="2.2"/>
                    <feOffset dx="0" dy="1.6"/>
                    <feComponentTransfer>
                        <feFuncA type="linear" slope="${if (useDark) 0.32 else 0.14}"/>
                    </feComponentTransfer>
                    <feMerge>
                        <feMergeNode/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>
            </defs>
        """.trimIndent()
    }

    private fun generateStyles(svgId: String, useDark: Boolean, isPdf: Boolean): String {
        val animRise = "mqRise_$svgId"
        val animPulse = "mqPulse_$svgId"

        val text = if (useDark) "#EAF1FF" else "#10213D"
        val muted = if (useDark) "#A8B8DA" else "#4E6791"

        val axisColor = if (useDark) "#CBD8FF" else "#2A487A"
        val axisOpacity = if (useDark) "0.50" else "0.48"

        val microAxisColor = if (useDark) "#CBD8FF" else "#2A487A"
        val microAxisOpacity = if (useDark) "0.15" else "0.13"

        val glassColor = "#FFFFFF"
        val glassOpacity = if (useDark) "0.105" else "0.62"

        val glassStrokeColor = if (useDark) "#DBE6FF" else "#8EACDD"
        val glassStrokeOpacity = if (useDark) "0.34" else "0.62"

        val chipBgColor = if (useDark) "#081126" else "#FFFFFF"
        val chipBgOpacity = if (useDark) "0.64" else "0.82"

        val labelBgColor = if (useDark) "#071126" else "#FFFFFF"
        val labelBgOpacity = if (useDark) "0.78" else "0.88"

        val focus = if (useDark) "#36D1DC" else "#0077B6"
        val specularOpacity = if (useDark) 0.52 else 0.66
        val revealCss = if (isPdf) {
            """
            #$svgId .reveal {
                opacity: 1;
            }
            """.trimIndent()
        } else {
            """
            #$svgId .reveal {
                opacity: 0;
                animation: $animRise 460ms cubic-bezier(.2,.8,.2,1) forwards;
            }

            #$svgId .d1 { animation-delay: 50ms; }
            #$svgId .d2 { animation-delay: 115ms; }
            #$svgId .d3 { animation-delay: 180ms; }
            #$svgId .d4 { animation-delay: 245ms; }
            #$svgId .d5 { animation-delay: 310ms; }

            @keyframes $animRise {
                from {
                    opacity: 0;
                    transform: translateY(8px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }

            #$svgId .focus-pulse {
                animation: $animPulse 2.35s ease-in-out infinite;
                transform-box: fill-box;
                transform-origin: center;
            }

            @keyframes $animPulse {
                0%, 100% {
                    opacity: ${if (useDark) ".46" else ".38"};
                    transform: scale(1);
                }
                50% {
                    opacity: ${if (useDark) ".16" else ".12"};
                    transform: scale(1.34);
                }
            }
            """.trimIndent()
        }

        return """
            <style>
                ${theme.fontImport}

                #$svgId {
                    --text: $text;
                    --muted: $muted;
                    --axis: $axisColor;
                    --axis-opacity: $axisOpacity;
                    --micro-axis: $microAxisColor;
                    --micro-axis-opacity: $microAxisOpacity;
                    --glass: $glassColor;
                    --glass-opacity: $glassOpacity;
                    --glass-stroke: $glassStrokeColor;
                    --glass-stroke-opacity: $glassStrokeOpacity;
                    --chip-bg: $chipBgColor;
                    --chip-bg-opacity: $chipBgOpacity;
                    --label-bg: $labelBgColor;
                    --label-bg-opacity: $labelBgOpacity;
                    --focus: $focus;
                }

                #$svgId .board {
                    fill: ${if (isPdf) glassColor else "var(--glass)"};
                    fill-opacity: ${if (isPdf) glassOpacity else "var(--glass-opacity)"};
                    stroke: ${if (isPdf) glassStrokeColor else "var(--glass-stroke)"};
                    stroke-opacity: ${if (isPdf) glassStrokeOpacity else "var(--glass-stroke-opacity)"};
                    stroke-width: 1.1;
                    ${if (isPdf) "" else "filter: url(#boardShadow_$svgId);"}
                }

                #$svgId .board-rule {
                    stroke: ${if (isPdf) glassStrokeColor else "var(--glass-stroke)"};
                    stroke-opacity: ${if (isPdf) glassStrokeOpacity else "var(--glass-stroke-opacity)"};
                    stroke-width: 1;
                    opacity: 0.46;
                }

                #$svgId .title-text {
                    font-family: ${theme.fontFamily};
                    font-size: ${38 / theme.fontWidthMultiplier}px;
                    font-weight: 800;
                    letter-spacing: -0.01em;
                    fill: ${if (isPdf) text else "var(--text)"};
                }

                #$svgId .sub-text {
                    font-family: ${theme.fontFamily};
                    font-size: 12px;
                    font-weight: 600;
                    letter-spacing: 0.02em;
                    fill: ${if (isPdf) muted else "var(--muted)"};
                }

                #$svgId .axis-label {
                    font-family: ${theme.fontFamily};
                    font-size: 10.5px;
                    font-weight: 800;
                    letter-spacing: 0.105em;
                    text-transform: uppercase;
                    fill: ${if (isPdf) muted else "var(--muted)"};
                }

                #$svgId .axis-end-label {
                    font-family: ${theme.fontFamily};
                    font-size: 8.5px;
                    font-weight: 800;
                    letter-spacing: 0.12em;
                    fill: ${if (isPdf) muted else "var(--muted)"};
                    opacity: 0.72;
                }

                #$svgId .axis-guide {
                    stroke: ${if (isPdf) axisColor else "var(--axis)"};
                    stroke-opacity: ${if (isPdf) axisOpacity else "var(--axis-opacity)"};
                    stroke-width: 1.3;
                    opacity: 0.52;
                }

                #$svgId .axis-arrow {
                    fill: ${if (isPdf) axisColor else "var(--axis)"};
                }

                #$svgId .split-line {
                    stroke: ${if (isPdf) axisColor else "var(--axis)"};
                    stroke-opacity: ${if (isPdf) axisOpacity else "var(--axis-opacity)"};
                    stroke-width: 2;
                    stroke-linecap: round;
                }

                #$svgId .micro-grid-line {
                    stroke: ${if (isPdf) microAxisColor else "var(--micro-axis)"};
                    stroke-opacity: ${if (isPdf) microAxisOpacity else "var(--micro-axis-opacity)"};
                    stroke-width: 1;
                }

                ${if (isPdf) "" else """
                #$svgId .plot-vignette {
                    pointer-events: none;
                }
                """}

                #$svgId .chart-border {
                    stroke: ${if (isPdf) axisColor else "var(--axis)"};
                    stroke-opacity: ${if (isPdf) axisOpacity else "var(--axis-opacity)"};
                    stroke-width: 1.25;
                }

                #$svgId .center-pin {
                    fill: ${if (isPdf) axisColor else "var(--axis)"};
                    fill-opacity: ${if (isPdf) axisOpacity else "var(--axis-opacity)"};
                    opacity: 0.7;
                }

                #$svgId .chip-bg {
                    fill: ${if (isPdf) chipBgColor else "var(--chip-bg)"};
                    fill-opacity: ${if (isPdf) chipBgOpacity else "var(--chip-bg-opacity)"};
                    ${if (isPdf) "" else "backdrop-filter: blur(8px);"}
                }

                #$svgId .quad-chip-text {
                    font-family: ${theme.fontFamily};
                    font-size: 11.5px;
                    font-weight: 800;
                    letter-spacing: 0.075em;
                }

                #$svgId .company-node {
                    cursor: default;
                }

                #$svgId a .company-node {
                    cursor: pointer;
                }

                #$svgId .company-name {
                    font-family: ${theme.fontFamily};
                    font-size: 11.5px;
                    font-weight: 750;
                    fill: ${if (isPdf) text else "var(--text)"};
                }

                #$svgId .company-label-bg {
                    fill: ${if (isPdf) labelBgColor else "var(--label-bg)"};
                    fill-opacity: ${if (isPdf) labelBgOpacity else "var(--label-bg-opacity)"};
                    stroke: ${if (isPdf) glassStrokeColor else "var(--glass-stroke)"};
                    stroke-opacity: ${if (isPdf) glassStrokeOpacity else "var(--glass-stroke-opacity)"};
                    stroke-width: 0.75;
                }

                #$svgId .label-connector {
                    stroke: ${if (isPdf) axisColor else "var(--axis)"};
                    stroke-opacity: ${if (isPdf) axisOpacity else "var(--axis-opacity)"};
                    stroke-width: 0.9;
                    opacity: 0.52;
                }

                #$svgId .node-core {
                    ${if (isPdf) "" else "filter: url(#nodeGlow_$svgId);"}
                    stroke: #FFFFFF;
                    stroke-opacity: 0.46;
                    stroke-width: 0.9;
                }

                #$svgId .node-specular {
                    fill: #ffffff;
                    opacity: $specularOpacity;
                    pointer-events: none;
                }

                #$svgId .focus-ring {
                    opacity: 0.72;
                }

                #$svgId .focus-inner-ring {
                    opacity: ${if (useDark) ".64" else ".52"};
                }

                $revealCss
            </style>
        """.trimIndent()
    }

    private fun buildDescription(config: MagicQuadrantConfig, points: List<CompanyPoint>): String {
        if (points.isEmpty()) {
            return "Magic quadrant chart titled ${config.title} with no companies."
        }

        val counts = points.groupingBy { it.quadrant }.eachCount()
        val leaderCount = counts[QuadrantType.LEADERS] ?: 0
        val challengerCount = counts[QuadrantType.CHALLENGERS] ?: 0
        val visionaryCount = counts[QuadrantType.VISIONARIES] ?: 0
        val nicheCount = counts[QuadrantType.NICHE] ?: 0

        return "Magic quadrant chart titled ${config.title}. " +
                "Companies are positioned by ${config.xAxisLabel.ifBlank { "Ability to Execute" }} and " +
                "${config.yAxisLabel.ifBlank { "Completeness of Vision" }}. " +
                "Distribution: $leaderCount leaders, $challengerCount challengers, " +
                "$visionaryCount visionaries, and $nicheCount niche players."
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateSvgId(title: String): String {
        val slug = title.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), "_")
            .trim('_')
            .ifBlank { "magic_quadrant" }
            .take(32)

        val suffix = Uuid.random().toHexString().take(10)
        return "mq_${slug}_$suffix"
    }

    private fun ellipsize(text: String, maxLength: Int): String {
        val clean = text.trim()
        return if (clean.length <= maxLength) clean else clean.take(maxLength - 1).trimEnd() + "…"
    }

    private fun formatScore(value: Double): String {
        val rounded = (value * 10.0).roundToInt() / 10.0
        return if (abs(rounded - rounded.roundToInt()) < 0.0001) {
            rounded.roundToInt().toString()
        } else {
            rounded.toString()
        }
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}