package io.docops.docopsextensionssupport.chart.pie

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class DonutMakerImproved {
    private var height = 660.0
    private var width = 760.0
    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)

    fun makeDonut(pieSlices: PieSlices): String = makeDonut(pieSlices, isPdf = false)

    fun makeDonut(pieSlices: PieSlices, isPdf: Boolean): String {
        theme = if (pieSlices.display.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(pieSlices.display.theme, pieSlices.display.useDark)
        } else {
            ThemeFactory.getTheme(pieSlices.display)
        }

        val donuts = pieSlices.toDonutSlices()
        val legendRows = ceil(donuts.size / 2.0).toInt().coerceAtLeast(1)

        width = 760.0
        height = max(660.0, 548.0 + legendRows * 34.0)

        val scaledWidth = width * pieSlices.display.scale
        val scaledHeight = height * pieSlices.display.scale

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="${scaledWidth / DISPLAY_RATIO_16_9}"
                 height="${scaledHeight / DISPLAY_RATIO_16_9}"
                 viewBox="0 0 $width $height"
                 id="id_${pieSlices.display.id}"
                 role="img"
                 aria-labelledby="title_${pieSlices.display.id} desc_${pieSlices.display.id}">
                <title id="title_${pieSlices.display.id}">${pieSlices.title.escapeXml()}</title>
                <desc id="desc_${pieSlices.display.id}">Donut chart with centered chart area and bottom legend.</desc>
                ${createDefs(pieSlices, donuts, isPdf)}
                ${createBackground(pieSlices, isPdf)}
                ${createTitle(pieSlices, isPdf)}
                ${createTotalBadge(pieSlices, donuts, isPdf)}
                ${createDonutCommands(donuts, pieSlices, isPdf)}
                ${addLegend(donuts, pieSlices, isPdf)}
            </svg>
        """.trimIndent()
    }

    private fun createDefs(pieSlices: PieSlices, donuts: List<DonutSlice>, isPdf: Boolean): String {
        val id = pieSlices.display.id
        val dark = pieSlices.display.useDark

        val bgStart = if (dark) "#08111d" else "#f8fafc"
        val bgMid = if (dark) "#102033" else "#eef4f8"
        val bgEnd = if (dark) "#11180f" else "#f9f4e8"
        val gridStroke = if (dark) "#334155" else "#cbd5e1"
        val haloA = if (dark) "#4ade80" else "#ffffff"
        val haloB = if (dark) "#14b8a6" else "#d9f99d"
        val shadowSlope = if (dark) "0.48" else "0.20"

        val gradients = buildString {
            donuts.forEachIndexed { index, donut ->
                val color = colorForSlice(index, donut)
                val start = lightenHex(color, if (dark) 0.18 else 0.10)
                val end = darkenHex(color, if (dark) 0.18 else 0.10)

                append(
                    """
                    <linearGradient id="orbit_seg_${id}_$index" x1="80" y1="80" x2="280" y2="280" gradientUnits="userSpaceOnUse">
                        <stop offset="0%" stop-color="$start"/>
                        <stop offset="100%" stop-color="$end"/>
                    </linearGradient>
                    """.trimIndent()
                )
            }
        }

        val animationCss = if (isPdf) {
            """
            #id_$id .reveal {
                opacity: 1;
            }
            """.trimIndent()
        } else {
            """
            @keyframes orbitReveal_$id {
                from {
                    opacity: 0;
                    transform: scale(.92) rotate(-5deg);
                }
                to {
                    opacity: 1;
                    transform: scale(1) rotate(0deg);
                }
            }

            #id_$id .reveal {
                opacity: 0;
                transform-box: fill-box;
                transform-origin: center;
                animation: orbitReveal_$id 720ms cubic-bezier(.18,.9,.24,1.12) forwards;
            }

            #id_$id .slice-shell {
                transform-box: fill-box;
                transform-origin: center;
                transition: transform 260ms cubic-bezier(.2,.9,.2,1), filter 260ms ease;
                cursor: pointer;
            }

            #id_$id .slice-shell:hover {
                transform: scale(1.035);
                filter: url(#donut_lift_$id);
            }

            #id_$id .legend-item {
                transition: transform 200ms ease, opacity 200ms ease;
                cursor: pointer;
            }

            #id_$id .legend-item:hover {
                transform: translateY(-1px);
                opacity: 0.94;
            }
            """.trimIndent()
        }

        return """
            <defs>
                <linearGradient id="donut_bg_$id" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="$bgStart"/>
                    <stop offset="48%" stop-color="$bgMid"/>
                    <stop offset="100%" stop-color="$bgEnd"/>
                </linearGradient>

                <radialGradient id="donut_halo_$id" cx="54%" cy="24%" r="68%">
                    <stop offset="0%" stop-color="$haloA" stop-opacity="${if (dark) "0.18" else "0.72"}"/>
                    <stop offset="48%" stop-color="$haloB" stop-opacity="${if (dark) "0.10" else "0.18"}"/>
                    <stop offset="100%" stop-color="$bgStart" stop-opacity="0"/>
                </radialGradient>

                <pattern id="donut_ticks_$id" width="26" height="26" patternUnits="userSpaceOnUse">
                    <path d="M 26 0 L 0 0 0 26"
                          fill="none"
                          stroke="$gridStroke"
                          stroke-width="0.8"
                          opacity="${if (dark) "0.38" else "0.32"}"/>
                </pattern>

                $gradients

                <linearGradient id="legend_surface_$id" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="${if (dark) "#132032" else "#ffffff"}" stop-opacity="${if (dark) "0.94" else "0.92"}"/>
                    <stop offset="100%" stop-color="${if (dark) "#0d1726" else "#f8fafc"}" stop-opacity="${if (dark) "0.94" else "0.92"}"/>
                </linearGradient>

                <linearGradient id="legend_stroke_$id" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="${if (dark) "#4ade80" else "#94a3b8"}" stop-opacity="0.30"/>
                    <stop offset="100%" stop-color="${if (dark) "#14b8a6" else "#cbd5e1"}" stop-opacity="0.16"/>
                </linearGradient>

                <filter id="donut_lift_$id" x="-30%" y="-30%" width="160%" height="160%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="${if (dark) "6" else "5"}" result="blur"/>
                    <feOffset in="blur" dx="0" dy="${if (dark) "12" else "10"}" result="offset"/>
                    <feComponentTransfer in="offset">
                        <feFuncA type="linear" slope="$shadowSlope"/>
                    </feComponentTransfer>
                    <feMerge>
                        <feMergeNode/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>

                <style>
                    #id_$id {
                        font-family: ${safeFontFamily(theme.fontFamily)};
                    }

                    #id_$id text {
                        font-family: ${safeFontFamily(theme.fontFamily)};
                    }

                    $animationCss
                </style>
            </defs>
        """.trimIndent()
    }

    private fun createBackground(pieSlices: PieSlices, isPdf: Boolean): String {
        val id = pieSlices.display.id
        val dark = pieSlices.display.useDark

        if (isPdf) {
            val pdfBg = if (dark) "#0b1220" else "#f8fafc"
            val pdfPanel = if (dark) "#101827" else "#ffffff"
            val pdfStroke = if (dark) "#263244" else "#d8e2ee"

            return """
                <rect width="$width" height="$height" rx="28" fill="$pdfBg"/>
                <rect x="22" y="22" width="${width - 44}" height="${height - 44}" rx="24" fill="$pdfPanel" stroke="$pdfStroke" stroke-width="1.2"/>
            """.trimIndent()
        }

        return """
            <rect width="$width" height="$height" rx="28" fill="url(#donut_bg_$id)"/>
            <rect width="$width" height="$height" rx="28" fill="url(#donut_ticks_$id)" opacity="${if (pieSlices.display.useDark) "0.42" else "0.50"}"/>
            <rect width="$width" height="$height" rx="28" fill="url(#donut_halo_$id)"/>
        """.trimIndent()
    }

    private fun createTitle(pieSlices: PieSlices, isPdf: Boolean): String {
        val dark = pieSlices.display.useDark
        val titleColor = if (dark) "#f8fafc" else "#111827"
        val labelColor = if (dark) "#5eead4" else "#0f766e"
        val muted = if (dark) "#cbd5e1" else "#475569"
        val accent = if (isPdf && dark) "#38bdf8" else theme.accentColor

        return """
            <text x="56"
                  y="50"
                  fill="$labelColor"
                  style="fill: $labelColor !important;"
                  font-size="10"
                  font-weight="900"
                  letter-spacing="2.2">
                PIE CHART
            </text>
            <text x="56"
                  y="74"
                  fill="$titleColor"
                  style="fill: $titleColor !important;"
                  font-size="28"
                  font-weight="900"
                  letter-spacing="-0.6">
                ${pieSlices.title.escapeXml()}
            </text>
            <rect x="56" y="88" width="64" height="4" rx="2" fill="$accent"/>
            <rect x="128" y="88" width="18" height="4" rx="2" fill="${if (dark) "#a3e635" else "#84cc16"}"/>
            <text x="56"
                  y="116"
                  fill="$muted"
                  style="fill: $muted !important;"
                  font-size="12"
                  font-weight="700">
                Rounded donut · bottom legend · ${pieSlices.slices.size} segments
            </text>
        """.trimIndent()
    }

    private fun createTotalBadge(pieSlices: PieSlices, slices: List<DonutSlice>, isPdf: Boolean): String {
        val dark = pieSlices.display.useDark
        val total = slices.sumOf { it.amount }
        val badgeFill = if (dark) "#1d3550" else "#eaf2ff"
        val badgeStroke = if (dark) "#3b5f83" else "#c7d9f4"
        val labelColor = if (dark) "#bae6fd" else "#0f766e"
        val valueColor = if (dark) "#ffffff" else "#111827"

        return """
            <g transform="translate(604 48)">
                <rect width="104" height="46" rx="12" fill="$badgeFill" stroke="$badgeStroke" stroke-width="${if (isPdf) "1.4" else "1"}"/>
                <text x="18"
                      y="17"
                      fill="$labelColor"
                      style="fill: $labelColor !important;"
                      font-size="9"
                      font-weight="900"
                      letter-spacing="1.8">
                    TOTAL
                </text>
                <text x="18"
                      y="35"
                      fill="$valueColor"
                      style="fill: $valueColor !important;"
                      font-size="17"
                      font-weight="900">
                    ${if (slices.isNotEmpty()) slices[0].valueFmt(total) else "0"}
                </text>
            </g>
        """.trimIndent()
    }

    private fun createDonutCommands(
        slices: List<DonutSlice>,
        pieSlices: PieSlices,
        isPdf: Boolean
    ): String {
        val id = pieSlices.display.id
        val dark = pieSlices.display.useDark

        val chartCenterX = 380.0
        val chartCenterY = 278.0
        val radius = 128.0
        val strokeWidth = 52.0
        val innerRadius = radius - strokeWidth
        val totalValue = slices.sumOf { it.amount }
        val safeTotal = if (totalValue <= 0.0) 1.0 else totalValue

        val chartSurface = if (dark) "#111827" else "#ffffff"
        val chartInner = if (dark) "#0b1220" else "#ffffff"
        val trackStroke = if (dark) "#334155" else "#d8e2ee"
        val ringStroke = if (dark) "#475569" else "#d8e2ee"
        val titleColor = if (dark) "#ffffff" else "#111827"
        val muted = if (dark) "#cbd5e1" else "#64748b"
        val filter = if (isPdf) "" else """filter="url(#donut_lift_$id)""""
        val chartSurfaceOpacity = if (isPdf) "1" else if (dark) "0.18" else "0.42"

        var startAngle = -90.0
        val segments = buildString {
            slices.forEachIndexed { index, slice ->
                val percent = (slice.amount / safeTotal) * 100.0
                val sweep = percent * 3.6
                val gap = if (slices.size > 1) minOf(3.2, sweep * 0.25) else 0.0
                val segmentStart = startAngle + gap / 2.0
                val segmentEnd = startAngle + sweep - gap / 2.0
                val delay = if (isPdf) "" else """style="animation-delay: ${index * 90}ms""""

                if (isPdf) {
                    val pdfPath = donutSegmentPath(
                        centerX = 0.0,
                        centerY = 0.0,
                        outerRadius = radius,
                        innerRadius = innerRadius,
                        startAngle = segmentStart,
                        endAngle = segmentEnd
                    )
                    val fill = colorForSlice(index, slice)

                    append(
                        """
                        <g>
                            <path d="$pdfPath"
                                  fill="$fill"
                                  stroke="${if (dark) "#101827" else "#ffffff"}"
                                  stroke-width="2">
                                <title>${slice.label.escapeXml()}: ${slice.valueFmt(slice.amount)} (${slice.valueFmt(percent)}%)</title>
                            </path>
                        </g>
                        """.trimIndent()
                    )
                } else {
                    val webPath = arcPath(0.0, 0.0, radius, segmentStart, segmentEnd)

                    append(
                        """
                        <g class="reveal" $delay>
                            <g class="slice-shell">
                                <path d="$webPath"
                                      fill="none"
                                      stroke="url(#orbit_seg_${id}_$index)"
                                      stroke-width="$strokeWidth"
                                      stroke-linecap="round">
                                    <title>${slice.label.escapeXml()}: ${slice.valueFmt(slice.amount)} (${slice.valueFmt(percent)}%)</title>
                                </path>
                            </g>
                        </g>
                        """.trimIndent()
                    )
                }

                startAngle += sweep
            }
        }

        return """
            <g transform="translate(0 0)">
                <circle cx="$chartCenterX" cy="$chartCenterY" r="168" fill="$chartSurface" opacity="$chartSurfaceOpacity" $filter/>
                <circle cx="$chartCenterX" cy="$chartCenterY" r="154" fill="none" stroke="$trackStroke" stroke-width="1.1" stroke-dasharray="2 8"/>
                <circle cx="$chartCenterX" cy="$chartCenterY" r="106" fill="$chartInner" stroke="$ringStroke" stroke-width="1"/>

                <g transform="translate($chartCenterX $chartCenterY)">
                    $segments
                </g>

                <circle cx="$chartCenterX" cy="$chartCenterY" r="74" fill="$chartInner" stroke="$ringStroke" stroke-width="1.4"/>
                <circle cx="$chartCenterX" cy="$chartCenterY" r="58" fill="none" stroke="$ringStroke" stroke-width="0.8" opacity="0.72"/>

                <text x="$chartCenterX"
                      y="${chartCenterY - 16}"
                      text-anchor="middle"
                      fill="$muted"
                      style="fill: $muted !important;"
                      font-size="11"
                      font-weight="900"
                      letter-spacing="1.4">
                    TOTAL
                </text>
                <text x="$chartCenterX"
                      y="${chartCenterY + 14}"
                      text-anchor="middle"
                      fill="$titleColor"
                      style="fill: $titleColor !important;"
                      font-size="34"
                      font-weight="900"
                      letter-spacing="-1.2">
                    ${if (slices.isNotEmpty()) slices[0].valueFmt(totalValue) else "0"}
                </text>
                <text x="$chartCenterX"
                      y="${chartCenterY + 36}"
                      text-anchor="middle"
                      fill="$muted"
                      style="fill: $muted !important;"
                      font-size="12"
                      font-weight="700">
                    ${slices.size} segments
                </text>
            </g>
        """.trimIndent()
    }

    private fun addLegend(
        donuts: MutableList<DonutSlice>,
        pieSlices: PieSlices,
        isPdf: Boolean
    ): String {
        val id = pieSlices.display.id
        val dark = pieSlices.display.useDark

        val itemsPerRow = 2
        val rows = ceil(donuts.size / itemsPerRow.toDouble()).toInt().coerceAtLeast(1)
        val rowHeight = 30
        val legendHeight = 56 + rows * rowHeight + 18
        val legendX = 80.0
        val legendY = max(500.0, height - legendHeight - 34.0)

        val primary = if (dark) "#ffffff" else "#111827"
        val secondary = if (dark) "#d1d5db" else "#475569"
        val header = if (dark) "#cbd5e1" else "#475569"
        val accent = if (dark) "#67e8f9" else "#0f766e"
        val divider = if (dark) "#475569" else "#cbd5e1"
        val filter = if (isPdf) "" else """filter="url(#donut_lift_$id)""""
        val legendFill = if (isPdf) {
            if (dark) "#111827" else "#ffffff"
        } else {
            "url(#legend_surface_$id)"
        }
        val legendStroke = if (isPdf) {
            if (dark) "#475569" else "#d8e2ee"
        } else {
            "url(#legend_stroke_$id)"
        }

        val total = donuts.sumOf { it.amount }.let { if (it <= 0.0) 1.0 else it }

        val items = buildString {
            donuts.forEachIndexed { index, donut ->
                val col = index % itemsPerRow
                val row = index / itemsPerRow
                val x = 32 + col * 300
                val y = 58 + row * rowHeight
                val value = donut.amount
                val share = (value / total) * 100.0
                val label = wrapLegendLabel(donut.label, 24)
                val swatchFill = if (isPdf) colorForSlice(index, donut) else "url(#orbit_seg_${id}_$index)"

                append(
                    """
                    <g class="legend-item" transform="translate($x, $y)">
                        <rect width="14" height="14" rx="4" fill="$swatchFill"/>
                        <text x="24"
                              y="11"
                              fill="$primary"
                              style="fill: $primary !important;"
                              font-size="13"
                              font-weight="850">${label.escapeXml()}</text>
                        <text x="260"
                              y="11"
                              text-anchor="end"
                              fill="$secondary"
                              style="fill: $secondary !important;"
                              font-size="12"
                              font-weight="800">${donut.valueFmt(value)} · ${donut.valueFmt(share)}%</text>
                    </g>
                    """.trimIndent()
                )
            }
        }

        return """
            <g transform="translate($legendX, $legendY)" $filter>
                <rect x="0"
                      y="0"
                      width="600"
                      height="$legendHeight"
                      rx="18"
                      fill="$legendFill"
                      stroke="$legendStroke"
                      stroke-width="1.2"/>

                <text x="32"
                      y="28"
                      fill="$header"
                      style="fill: $header !important;"
                      font-size="10"
                      font-weight="900"
                      letter-spacing="1.5">
                    LEGEND
                </text>

                <text x="568"
                      y="28"
                      text-anchor="end"
                      fill="$accent"
                      style="fill: $accent !important;"
                      font-size="10"
                      font-weight="900"
                      letter-spacing="0.9">
                    VALUES / SHARE
                </text>

                <line x1="32"
                      y1="40"
                      x2="568"
                      y2="40"
                      stroke="$divider"
                      stroke-opacity="${if (dark) "0.60" else "0.70"}"/>

                $items
            </g>
        """.trimIndent()
    }

    private fun arcPath(
        centerX: Double,
        centerY: Double,
        radius: Double,
        startAngle: Double,
        endAngle: Double
    ): String {
        val start = polarToCartesian(centerX, centerY, radius, startAngle)
        val end = polarToCartesian(centerX, centerY, radius, endAngle)
        val largeArcFlag = if (endAngle - startAngle > 180.0) 1 else 0

        return "M ${format(start.x)} ${format(start.y)} A ${format(radius)} ${format(radius)} 0 $largeArcFlag 1 ${format(end.x)} ${format(end.y)}"
    }

    private fun donutSegmentPath(
        centerX: Double,
        centerY: Double,
        outerRadius: Double,
        innerRadius: Double,
        startAngle: Double,
        endAngle: Double
    ): String {
        val outerStart = polarToCartesian(centerX, centerY, outerRadius, startAngle)
        val outerEnd = polarToCartesian(centerX, centerY, outerRadius, endAngle)
        val innerEnd = polarToCartesian(centerX, centerY, innerRadius, endAngle)
        val innerStart = polarToCartesian(centerX, centerY, innerRadius, startAngle)
        val largeArcFlag = if (endAngle - startAngle > 180.0) 1 else 0

        return """
            M ${format(outerStart.x)} ${format(outerStart.y)}
            A ${format(outerRadius)} ${format(outerRadius)} 0 $largeArcFlag 1 ${format(outerEnd.x)} ${format(outerEnd.y)}
            L ${format(innerEnd.x)} ${format(innerEnd.y)}
            A ${format(innerRadius)} ${format(innerRadius)} 0 $largeArcFlag 0 ${format(innerStart.x)} ${format(innerStart.y)}
            Z
        """.trimIndent().replace("\n", " ")
    }

    private fun polarToCartesian(
        centerX: Double,
        centerY: Double,
        radius: Double,
        angleDegrees: Double
    ): Point {
        val radians = angleDegrees * PI / 180.0

        return Point(
            x = centerX + radius * cos(radians),
            y = centerY + radius * sin(radians)
        )
    }

    private fun colorForSlice(index: Int, slice: DonutSlice): String {
        val explicit = slice.color.trim()
        if (explicit.isUsableHexColor()) {
            return normalizeHex(explicit)
        }

        val themeColor = theme.chartPalette.getOrNull(index % theme.chartPalette.size)?.color
        if (!themeColor.isNullOrBlank() && themeColor.isUsableHexColor()) {
            val normalized = normalizeHex(themeColor)
            if (contrastRatio(normalized, theme.canvas) >= 1.65) {
                return normalized
            }
        }

        return accessiblePalette[index % accessiblePalette.size]
    }

    private fun wrapLegendLabel(label: String, maxLength: Int): String {
        val clean = label.trim()
        if (clean.length <= maxLength) {
            return clean
        }

        return clean.take(maxLength - 1).trimEnd() + "…"
    }

    private fun safeFontFamily(fontFamily: String): String {
        return fontFamily.ifBlank { "Arial, Helvetica, sans-serif" }
    }

    private fun format(value: Double): String {
        return String.format(java.util.Locale.US, "%.2f", value)
    }

    private fun normalizeHex(hex: String): String {
        val clean = hex.trim().removePrefix("#")
        if (clean.length == 3) {
            return "#" + clean.map { "$it$it" }.joinToString("").lowercase()
        }

        return "#${clean.take(6).lowercase()}"
    }

    private fun String.isUsableHexColor(): Boolean {
        val clean = trim().removePrefix("#")
        return clean.length == 3 || clean.length == 6 && clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    private fun lightenHex(hex: String, amount: Double): String {
        return mixHex(hex, "#ffffff", amount)
    }

    private fun darkenHex(hex: String, amount: Double): String {
        return mixHex(hex, "#000000", amount)
    }

    private fun mixHex(hex: String, targetHex: String, amount: Double): String {
        val source = parseHex(hex) ?: return hex
        val target = parseHex(targetHex) ?: return hex
        val ratio = amount.coerceIn(0.0, 1.0)

        val r = (source.r + (target.r - source.r) * ratio).toInt().coerceIn(0, 255)
        val g = (source.g + (target.g - source.g) * ratio).toInt().coerceIn(0, 255)
        val b = (source.b + (target.b - source.b) * ratio).toInt().coerceIn(0, 255)

        return "#%02x%02x%02x".format(r, g, b)
    }

    private fun parseHex(hex: String): Rgb? {
        val clean = normalizeHex(hex).removePrefix("#")
        if (clean.length != 6) {
            return null
        }

        return runCatching {
            Rgb(
                r = clean.substring(0, 2).toInt(16),
                g = clean.substring(2, 4).toInt(16),
                b = clean.substring(4, 6).toInt(16)
            )
        }.getOrNull()
    }

    private fun relativeLuminance(hex: String): Double {
        val rgb = parseHex(hex) ?: return 0.0

        fun channel(value: Int): Double {
            val normalized = value / 255.0
            return if (normalized <= 0.03928) {
                normalized / 12.92
            } else {
                Math.pow((normalized + 0.055) / 1.055, 2.4)
            }
        }

        return 0.2126 * channel(rgb.r) + 0.7152 * channel(rgb.g) + 0.0722 * channel(rgb.b)
    }

    private fun contrastRatio(a: String, b: String): Double {
        val l1 = relativeLuminance(a)
        val l2 = relativeLuminance(b)
        val lighter = max(l1, l2)
        val darker = minOf(l1, l2)

        return (lighter + 0.05) / (darker + 0.05)
    }

    private data class Point(val x: Double, val y: Double)

    private data class Rgb(val r: Int, val g: Int, val b: Int)

    companion object {
        private val accessiblePalette = listOf(
            "#6ee16f",
            "#18c3b5",
            "#38bdf8",
            "#e56aa6",
            "#f59e0b",
            "#a78bfa",
            "#fb7185",
            "#22c55e",
            "#06b6d4",
            "#f97316"
        )
    }
}

data class DonutSliceWithCommands(
    val id: String,
    val percent: Double,
    val amount: Double,
    val color: String,
    val label: String,
    val commands: String,
    val offset: Double
)