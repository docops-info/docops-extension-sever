package io.docops.docopsextensionssupport.chart.pie

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.formatDecimal
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class PieDashboardMaker(val useDark: Boolean) {

    private lateinit var theme: DocOpsTheme

    private val width = 600.0
    private val height = 588.0
    private val chartCenterX = 300.0
    private val chartCenterY = 242.0
    private val radius = 154.0

    fun makePies(pies: Pies, title: String = "Pie Chart"): String {
        theme = if (pies.pieDisplay.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(pies.pieDisplay.theme, useDark)
        } else {
            ThemeFactory.getTheme(useDark)
        }

        val id = pies.pieDisplay.id
        val total = pies.pies.sumOf { it.percent.toDouble() }.takeIf { it > 0.0 } ?: 1.0
        val scaledWidth = (width * pies.pieDisplay.scale) / DISPLAY_RATIO_16_9
        val scaledHeight = (height * pies.pieDisplay.scale) / DISPLAY_RATIO_16_9

        return buildString {
            append(createSvgStart(id, scaledWidth, scaledHeight, pies, title))
            append(createDefs(id, pies))
            append(createBackground(id))
            append(createHeader(pies, title))
            append(createTotalBadge(total))
            append(createRings(id))
            append(createPie(pies, total, id))
            append(createLegend(pies, total, id))
            append("</svg>")
        }
    }

    private fun createSvgStart(id: String, scaledWidth: Double, scaledHeight: Double, pies: Pies, title: String): String {
        val desc = buildString {
            append("${title} pie chart with ${pies.pies.size} segments. ")
            pies.pies.forEachIndexed { index, pie ->
                if (index > 0) append(", ")
                append("${pie.label} ${formatDecimal(pie.percent.toDouble(), 1)}%")
            }
            append(".")
        }

        return """
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="$scaledWidth"
                 height="$scaledHeight"
                 viewBox="0 0 $width $height"
                 id="id_$id"
                 role="img"
                 aria-labelledby="pie_title_$id pie_desc_$id">
              <title id="pie_title_$id">${title.escapeXml()}</title>
              <desc id="pie_desc_$id">${desc.escapeXml()}</desc>
        """.trimIndent()
    }

    private fun createDefs(id: String, pies: Pies): String {
        return """
        <defs>
          <style>
            ${theme.fontImport}

            #id_$id text {
              font-family: ${theme.fontFamily};
            }

            @keyframes titleReveal_$id {
              from { opacity: 0; transform: translateY(-8px); }
              to { opacity: 1; transform: translateY(0); }
            }

            @keyframes pieReveal_$id {
              from { opacity: 0; transform: scale(0.84) rotate(-4deg); }
              to { opacity: 1; transform: scale(1) rotate(0deg); }
            }

            @keyframes legendReveal_$id {
              from { opacity: 0; transform: translateY(14px); }
              to { opacity: 1; transform: translateY(0); }
            }

            @keyframes pulseRing_$id {
              0%, 100% { opacity: 0.16; stroke-width: 1; }
              50% { opacity: 0.36; stroke-width: 1.6; }
            }

            #id_$id .header-motion {
              animation: titleReveal_$id 520ms cubic-bezier(0.22, 1, 0.36, 1) both;
            }

            #id_$id .pie-segment {
              opacity: 1;
              transform-box: fill-box;
              transform-origin: center;
              animation: pieReveal_$id 680ms cubic-bezier(0.22, 1, 0.36, 1) both;
            }

            #id_$id .slice-motion {
              transform-box: fill-box;
              transform-origin: center;
              transition:
                transform 280ms cubic-bezier(0.22, 1, 0.36, 1),
                filter 280ms ease;
              cursor: pointer;
            }

            #id_$id .slice-motion:hover {
              transform: scale(1.045);
              filter: url(#sliceGlow_$id);
            }

            #id_$id .legend-motion {
              animation: legendReveal_$id 620ms cubic-bezier(0.22, 1, 0.36, 1) 780ms both;
            }

            #id_$id .legend-item {
              transition:
                transform 220ms cubic-bezier(0.22, 1, 0.36, 1),
                opacity 220ms ease;
              cursor: pointer;
            }

            #id_$id .legend-item:hover {
              transform: translateY(-2px);
            }

            #id_$id .pulse-ring {
              animation: pulseRing_$id 3.8s ease-in-out infinite;
            }

            @media (prefers-reduced-motion: reduce) {
              #id_$id .header-motion,
              #id_$id .pie-segment,
              #id_$id .legend-motion,
              #id_$id .pulse-ring {
                animation: none !important;
              }
            }
          </style>

          ${createBackgroundDefs(id)}
          ${createSliceGradients(id, pies)}
          <!-- ... existing code ... -->
        </defs>
    """.trimIndent()
    }

    private fun createBackgroundDefs(id: String): String {


        val bgStart = if (useDark) theme.canvas else SVGColor(theme.canvas).darker() ?: theme.canvas
        val bgMid = if (useDark) surfaceLift() else theme.canvas
        val bgEnd = if (useDark) "#041317" else SVGColor(theme.canvas).darker() ?: theme.canvas

        return """
            <linearGradient id="bgSurface_$id" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="$bgStart"/>
              <stop offset="46%" stop-color="$bgMid"/>
              <stop offset="100%" stop-color="$bgEnd"/>
            </linearGradient>

            <radialGradient id="bgGlowA_$id" cx="18%" cy="10%" r="70%">
              <stop offset="0%" stop-color="${theme.accentColor}" stop-opacity="${if (useDark) "0.26" else "0.16"}"/>
              <stop offset="58%" stop-color="${theme.accentColor}" stop-opacity="0.05"/>
              <stop offset="100%" stop-color="${theme.accentColor}" stop-opacity="0"/>
            </radialGradient>

            <radialGradient id="bgGlowB_$id" cx="84%" cy="22%" r="58%">
              <stop offset="0%" stop-color="${secondaryAccent()}" stop-opacity="${if (useDark) "0.17" else "0.12"}"/>
              <stop offset="65%" stop-color="${secondaryAccent()}" stop-opacity="0.04"/>
              <stop offset="100%" stop-color="${secondaryAccent()}" stop-opacity="0"/>
            </radialGradient>

            <radialGradient id="vignette_$id" cx="50%" cy="48%" r="78%">
              <stop offset="0%" stop-color="#000000" stop-opacity="0"/>
              <stop offset="100%" stop-color="#000000" stop-opacity="${if (useDark) "0.38" else "0.10"}"/>
            </radialGradient>

            <pattern id="sonarDots_$id" x="0" y="0" width="24" height="24" patternUnits="userSpaceOnUse">
              <circle cx="2" cy="2" r="1" fill="${theme.primaryText}" opacity="${if (useDark) "0.10" else "0.12"}"/>
            </pattern>

            <pattern id="fineGrid_$id" x="0" y="0" width="48" height="48" patternUnits="userSpaceOnUse">
              <path d="M48 0 H0 V48" fill="none" stroke="${theme.primaryText}" stroke-opacity="${if (useDark) "0.035" else "0.055"}" stroke-width="1"/>
            </pattern>
        """.trimIndent()
    }

    private fun createSliceGradients(id: String, pies: Pies): String {
        return buildString {
            pies.pies.forEachIndexed { index, _ ->
                val color = getColor(index)
                val bright = brightenColor(color, 0.28)
                val dark = darkenColor(color, 0.18)

                append("""
                    <linearGradient id="slice_${id}_$index" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" stop-color="$bright"/>
                      <stop offset="52%" stop-color="$color"/>
                      <stop offset="100%" stop-color="$dark"/>
                    </linearGradient>
                """.trimIndent())
            }
        }
    }

    private fun createBackground(id: String): String {
        val r = theme.cornerRadius.coerceAtLeast(18)
        return """
            <rect width="$width" height="$height" rx="$r" fill="url(#bgSurface_$id)"/>
            <rect width="$width" height="$height" rx="$r" fill="url(#bgGlowA_$id)"/>
            <rect width="$width" height="$height" rx="$r" fill="url(#bgGlowB_$id)"/>
            <rect width="$width" height="$height" rx="$r" fill="url(#fineGrid_$id)"/>
            <rect width="$width" height="$height" rx="$r" fill="url(#sonarDots_$id)"/>
            <rect width="$width" height="$height" rx="$r" fill="url(#vignette_$id)"/>
        """.trimIndent()
    }

    private fun createHeader(pies: Pies, title: String): String {
        return """
            <g transform="translate(40, 44)">
              <g class="header-motion">
                <text x="0" y="0"
                      fill="${theme.secondaryText}"
                      style="fill: ${theme.secondaryText} !important;"
                      font-size="10"
                      font-weight="800"
                      letter-spacing="2.4">PIE CHART</text>

                <text x="0" y="34"
                      fill="${theme.primaryText}"
                      style="fill: ${theme.primaryText} !important;"
                      font-size="28"
                      font-weight="900"
                      letter-spacing="0">${title.escapeXml()}</text>

                <rect x="0" y="47" width="74" height="5" rx="2.5" fill="${theme.accentColor}"/>
                <rect x="82" y="47" width="22" height="5" rx="2.5" fill="${secondaryAccent()}"/>
              </g>
            </g>
        """.trimIndent()
    }

    private fun createTotalBadge(total: Double): String {
        return """
            <g transform="translate(448, 36)">
              <rect x="0" y="0" width="112" height="46" rx="14"
                    fill="${surfaceLift()}"
                    stroke="${theme.accentColor}"
                    stroke-opacity="0.28"/>
              <text x="16" y="18"
                    fill="${theme.secondaryText}"
                    style="fill: ${theme.secondaryText} !important;"
                    font-size="9"
                    font-weight="900"
                    letter-spacing="1.5">TOTAL</text>
              <text x="16" y="36"
                    fill="${theme.primaryText}"
                    style="fill: ${theme.primaryText} !important;"
                    font-size="18"
                    font-weight="900">${formatNumber(total)}</text>
            </g>
        """.trimIndent()
    }

    private fun createRings(id: String): String {
        return """
            <g opacity="0.7">
              <circle class="pulse-ring" cx="$chartCenterX" cy="$chartCenterY" r="180" fill="none" stroke="${theme.accentColor}"/>
              <circle cx="$chartCenterX" cy="$chartCenterY" r="128" fill="none" stroke="${theme.primaryText}" stroke-opacity="0.045"/>
              <circle cx="$chartCenterX" cy="$chartCenterY" r="204" fill="none" stroke="${secondaryAccent()}" stroke-opacity="0.035"/>
            </g>
        """.trimIndent()
    }

    private fun createPie(pies: Pies, total: Double, id: String): String {
        var startAngle = -90.0

        return buildString {
            append("""<g filter="url(#sliceShadow_$id)">""")

            pies.pies.forEachIndexed { index, pie ->
                val value = pie.percent.toDouble()
                val sweep = (value / total) * 360.0
                val endAngle = startAngle + sweep
                val path = createPieSegmentPath(chartCenterX, chartCenterY, radius, startAngle, endAngle)
                val delay = 0.12 + index * 0.10

                append("""
                    <g class="pie-segment" style="animation-delay: ${formatDecimal(delay, 2)}s;">
                      <g class="slice-motion">
                        <path d="$path"
                              fill="url(#slice_${id}_$index)"
                              stroke="#FFFFFF"
                              stroke-opacity="0.18"
                              stroke-width="1.2">
                          <title>${pie.label.escapeXml()}: ${formatDecimal(value, 1)}</title>
                        </path>
                        <path d="$path"
                              fill="url(#sliceGlass_$id)"
                              opacity="0.54"
                              pointer-events="none"/>
                      </g>
                    </g>
                """.trimIndent())

                startAngle = endAngle
            }

            append("</g>")
            append(createCenterPin())
            append(createLabelChips(pies, total, id))
        }
    }

    private fun createLabelChips(pies: Pies, total: Double, id: String): String {
        var startAngle = -90.0

        return buildString {
            append("""<g pointer-events="none">""")

            pies.pies.forEach { pie ->
                val value = pie.percent.toDouble()
                val share = if (total == 0.0) 0.0 else (value / total) * 100.0
                val sweep = (value / total) * 360.0
                val midAngle = startAngle + sweep / 2.0
                val labelRadius = radius * 0.66
                val point = polarToCartesian(chartCenterX, chartCenterY, labelRadius, midAngle)
                val label = "${formatDecimal(share, 1)}%"
                val chipWidth = max(58.0, label.length * 8.5 + 20.0)
                val halfChip = chipWidth / 2.0

                append("""
                <g transform="translate(${point.first}, ${point.second})">
                  <rect x="-$halfChip" y="-14" width="$chipWidth" height="26" rx="13"
                        fill="${chipFill()}"
                        opacity="0.94"/>
                  <text x="0" y="-1"
                        text-anchor="middle"
                        dominant-baseline="middle"
                        font-family="${theme.fontFamily}"
                        font-size="14"
                        font-weight="900"
                        fill="${chipTextColor()}"
                        style="fill: ${chipTextColor()} !important;">$label</text>
                </g>
            """.trimIndent())

                startAngle += sweep
            }

            append("</g>")
        }
    }

    private fun createCenterPin(): String {
        return """
            <circle cx="$chartCenterX" cy="$chartCenterY" r="10" fill="${theme.canvas}" stroke="${theme.accentColor}" stroke-opacity="0.55" stroke-width="1.2"/>
            <circle cx="$chartCenterX" cy="$chartCenterY" r="4" fill="${secondaryAccent()}" opacity="0.95"/>
        """.trimIndent()
    }

    private fun createLegend(pies: Pies, total: Double, id: String): String {
        val itemsPerRow = 2
        val rows = ceil(pies.pies.size / itemsPerRow.toDouble()).toInt().coerceAtLeast(1)
        val rowHeight = 26
        val legendHeight = 48 + rows * rowHeight + 16
        val legendY = height - legendHeight - 24
        val legendX = 80.0

        return buildString {
            append("""
            <g transform="translate($legendX, $legendY)" filter="url(#cardShadow_$id)">
              <g class="legend-motion">
                <rect x="0" y="0" width="440" height="$legendHeight" rx="18"
                      fill="url(#legendSurface_$id)"
                      stroke="url(#legendStroke_$id)"
                      stroke-width="1.2"/>

                <text x="32" y="26"
                      fill="${legendSecondaryColor()}"
                      font-size="10"
                      font-weight="900"
                      letter-spacing="1.4">LEGEND</text>

                <text x="408" y="26"
                      text-anchor="end"
                      fill="${legendAccentColor()}"
                      font-size="10"
                      font-weight="900"
                      letter-spacing="0.8">VALUES / SHARE</text>

                <line x1="32" y1="38" x2="408" y2="38"
                      stroke="${legendDividerColor()}"
                      stroke-opacity="0.28"/>
        """.trimIndent())

            pies.pies.forEachIndexed { index, pie ->
                val col = index % itemsPerRow
                val row = index / itemsPerRow
                val x = 32 + col * 210
                val y = 56 + row * rowHeight
                val value = pie.percent.toDouble()
                val share = if (total == 0.0) 0.0 else (value / total) * 100.0

                append("""
                <g class="legend-item" transform="translate($x, $y)">
                  <rect width="14" height="14" rx="4" fill="url(#slice_${id}_$index)"/>
                  <text x="23" y="11"
                        fill="${legendPrimaryColor()}"
                        style="fill: ${legendPrimaryColor()} !important;"
                        font-size="13"
                        font-weight="800">${pie.label.escapeXml()}</text>
                  <text x="170" y="11"
                        text-anchor="end"
                        fill="${legendSecondaryColor()}"
                        style="fill: ${legendSecondaryColor()} !important;"
                        font-size="12"
                        font-weight="700">${formatNumber(value)} · ${formatDecimal(share, 1)}%</text>
                </g>
            """.trimIndent())
            }

            append("</g></g>")
        }
    }

    private fun createPieSegmentPath(
        cx: Double,
        cy: Double,
        r: Double,
        startAngle: Double,
        endAngle: Double
    ): String {
        val start = polarToCartesian(cx, cy, r, startAngle)
        val end = polarToCartesian(cx, cy, r, endAngle)
        val largeArcFlag = if (endAngle - startAngle > 180.0) 1 else 0

        return "M $cx $cy L ${start.first} ${start.second} A $r $r 0 $largeArcFlag 1 ${end.first} ${end.second} Z"
    }

    private fun polarToCartesian(cx: Double, cy: Double, r: Double, angleDegrees: Double): Pair<Double, Double> {
        val angleRadians = angleDegrees * PI / 180.0
        return Pair(
            cx + r * cos(angleRadians),
            cy + r * sin(angleRadians)
        )
    }

    private fun getColor(index: Int): String {
        val palette = theme.chartPalette
        return palette[index % palette.size].color
    }

    private fun secondaryAccent(): String {
        return theme.chartPalette.getOrNull(4)?.color ?: theme.secondaryText
    }

    private fun surfaceLift(): String {
        return if (useDark) {
            SVGColor(theme.canvas).lighter() ?: theme.glassEffect
        } else {
            theme.glassEffect
        }
    }

    private fun chipFill(): String {
        return if (useDark) "#06191E" else "#172033"
    }



    private fun formatNumber(value: Double): String {
        return if (value == value.toInt().toDouble()) {
            value.toInt().toString()
        } else {
            formatDecimal(value, 1)
        }
    }

    private fun brightenColor(hexColor: String, factor: Double): String {
        val hex = hexColor.removePrefix("#")
        if (hex.length != 6) return hexColor

        val r = hex.substring(0, 2).toInt(16)
        val g = hex.substring(2, 4).toInt(16)
        val b = hex.substring(4, 6).toInt(16)

        val nr = (r + (255 - r) * factor).toInt().coerceIn(0, 255)
        val ng = (g + (255 - g) * factor).toInt().coerceIn(0, 255)
        val nb = (b + (255 - b) * factor).toInt().coerceIn(0, 255)

        return "#%02X%02X%02X".format(nr, ng, nb)
    }

    private fun darkenColor(hexColor: String, factor: Double): String {
        val hex = hexColor.removePrefix("#")
        if (hex.length != 6) return hexColor

        val r = hex.substring(0, 2).toInt(16)
        val g = hex.substring(2, 4).toInt(16)
        val b = hex.substring(4, 6).toInt(16)

        val nr = (r * (1.0 - factor)).toInt().coerceIn(0, 255)
        val ng = (g * (1.0 - factor)).toInt().coerceIn(0, 255)
        val nb = (b * (1.0 - factor)).toInt().coerceIn(0, 255)

        return "#%02X%02X%02X".format(nr, ng, nb)
    }



    private fun chipTextColor(): String {
        return if (useDark) "#F7FBFF" else "#FFFFFF"
    }

    private fun legendPrimaryColor(): String {
        return if (useDark) "#EAF6FF" else theme.primaryText
    }

    private fun legendSecondaryColor(): String {
        return if (useDark) "#C4D7E6" else theme.secondaryText
    }

    private fun legendAccentColor(): String {
        return if (useDark) "#9FD8FF" else theme.accentColor
    }

    private fun legendDividerColor(): String {
        return if (useDark) "#8FB0C6" else theme.primaryText
    }

}
