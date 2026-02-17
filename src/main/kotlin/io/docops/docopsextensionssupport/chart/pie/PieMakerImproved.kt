package io.docops.docopsextensionssupport.chart.pie

import io.docops.docopsextensionssupport.chart.ChartColors
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml


class PieMakerImproved {

    private val MODERN_COLORS = ChartColors.CYBER_PALETTE

    fun makePies(pies: Pies): String {
        val theme = if (pies.pieDisplay.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(pies.pieDisplay.theme, pies.pieDisplay.useDark)
        } else {
            ThemeFactory.getTheme(pies.pieDisplay)
        }
        val pieCount = pies.pies.size
        val pieWidth = 80 // Increased for Syne font and spacing
        val totalPieWidth = pieCount * pieWidth
        val width = totalPieWidth + 64 // Using 8-point grid
        
        val leftMargin = (width - totalPieWidth) / 2

        val sb = StringBuilder()
        sb.append(makeHead(width, pies, theme))
        sb.append("<defs>")
        sb.append(filters(pies, theme))
        
        sb.append(makePieGradients(pies))
        sb.append("</defs>")

        // Background with 8-point grid spacing and theme corners
        sb.append("<rect width='100%' height='100%' fill='${theme.canvas}' rx='${theme.cornerRadius}' pointer-events='none'/>")

        pies.pies.forEachIndexed { index, pie ->
            val x = leftMargin + (index * pieWidth)
            val delay = index * 0.1
            // SVG Animation Rule: Wrap in nested <g> for transforms
            sb.append("""<g transform="translate($x, 15)">""")
            sb.append("""<g class="pie-reveal" style="animation: revealScale 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) ${delay}s both;">""")
            sb.append(makePieSvg(pie, index, theme, pies.pieDisplay.id))
            sb.append(makeLabel(pie, theme))
            sb.append("</g></g>")
        }
        sb.append("</svg></svg>")
        return sb.toString()
    }

    private fun makeHead(width: Int, pies: Pies, theme: DocOpsTheme): String {
        val height = pies.maxRows() * 12 + 70
        val shadowPadding = 24
        val paddedWidth = width + shadowPadding * 2
        val paddedHeight = height + shadowPadding * 2

        val outerHeight = (1 + pies.pieDisplay.scale) * paddedHeight
        val outerWidth = (1 + pies.pieDisplay.scale) * paddedWidth


        return """<svg xmlns="http://www.w3.org/2000/svg" height="${outerHeight/DISPLAY_RATIO_16_9}" width="${outerWidth/DISPLAY_RATIO_16_9}" viewBox="-$shadowPadding -$shadowPadding $paddedWidth $paddedHeight" id="id_${pies.pieDisplay.id}">
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="${height+16}" viewBox="0 0 $width ${height+16}">
        """
    }

    private fun makePieSvg(pie: Pie, index: Int, theme: DocOpsTheme, id: String): String {
        val gradId = "grad_${id}_$index"

        return """
            <svg class="pie-unit" width="48" height="48" x="16" y="5" viewBox="0 0 36 36">
                <!-- Inner Depth -->
                <circle cx="18" cy="18" r="15.5" fill="${theme.canvas}" stroke="${theme.primaryText}" stroke-opacity="0.05" stroke-width="0.5"/>
                
                <!-- Glass Foundation -->
                <circle cx="18" cy="18" r="14" fill="${theme.glassEffect}" filter="url(#glassBlur)"/>
                
                <!-- Progress Track -->
                <path d="M18 4 a 14 14 0 0 1 0 28 a 14 14 0 0 1 0 -28" fill="none" stroke="${theme.primaryText}" stroke-opacity="0.1" stroke-width="3"/>
                
                <!-- High-Impact Stroke -->
                <path d="M18 4 a 14 14 0 0 1 0 28 a 14 14 0 0 1 0 -28" fill="none" 
                      stroke="url(#$gradId)" stroke-width="3.5" stroke-linecap="round"
                      stroke-dasharray="${pie.percent}, 100" filter="url(#glow)">
                    <animate attributeName="stroke-dashoffset" from="100" to="0" dur="1.2s" cubic-bezier(0.4, 0, 0.2, 1) fill="freeze"/>
                </path>

                <text x="18" y="19" dy="0.3em" text-anchor="middle" style="font-family: 'JetBrains Mono', monospace; font-size: 7px; font-weight: 800; fill: ${theme.primaryText}; letter-spacing: -0.5px;">
                    ${pie.percent}%
                </text>
            </svg>
        """.trimIndent()
    }

    private fun makeLabel(pie: Pie, theme: DocOpsTheme): String {
        val fontSize = 7 / theme.fontWidthMultiplier
        val sb = StringBuilder("""<text x="40" y="60" text-anchor="middle" style="font-family: ${theme.fontFamily}; font-size: ${fontSize}px; font-weight: 800; text-transform: uppercase; letter-spacing: 1px;">""")
        pie.label.split(" ").forEachIndexed { i, line ->
            sb.append("""<tspan x="40" dy="${if(i==0) 0 else 8}" fill="${theme.secondaryText}">${line.escapeXml()}</tspan>""")
        }
        sb.append("</text>")
        return sb.toString()
    }

    private fun filters(pies: Pies, theme: DocOpsTheme) = """
        <style>
            ${theme.fontImport}
            @keyframes revealScale {
                from { transform: scale(0.9); opacity: 0; }
                to { transform: scale(1); opacity: 1; }
            }
            #id_${pies.pieDisplay.id} .pie-unit { transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); cursor: pointer; }
            #id_${pies.pieDisplay.id} .pie-unit:hover { transform: scale(1.1); }
        </style>
        <filter id="glassBlur" x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur in="SourceGraphic" stdDeviation="1.5" />
        </filter>
        <filter id="glow">
            <feGaussianBlur stdDeviation="2" result="blur"/>
            <feComposite in="SourceGraphic" in2="blur" operator="over"/>
        </filter>
    """.trimIndent()

    private fun makePieGradients(pies: Pies): String {
        val sb = StringBuilder()
        pies.pies.forEachIndexed { i, _ ->
            val color = MODERN_COLORS[i % MODERN_COLORS.size].color
            sb.append("""
                <linearGradient id="grad_${pies.pieDisplay.id}_$i" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stop-color="$color" stop-opacity="0.8"/>
                    <stop offset="100%" stop-color="$color"/>
                </linearGradient>
            """.trimIndent())
        }
        return sb.toString()
    }
}
