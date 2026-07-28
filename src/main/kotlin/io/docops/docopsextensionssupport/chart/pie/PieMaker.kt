package io.docops.docopsextensionssupport.chart.pie

import io.docops.docopsextensionssupport.chart.ChartColors
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.formatHex
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import java.io.File

class PieMaker {

    // Array of modern, visually appealing colors for the pie charts
   /* private val MODERN_COLORS = arrayOf(
        "#4361ee", "#3a0ca3", "#7209b7", "#f72585", "#4cc9f0",
        "#4895ef", "#560bad", "#f15bb5", "#00bbf9", "#00f5d4",
        "#ff9e00", "#ff0054", "#390099", "#9e0059", "#ffbd00"
    )*/
    private val MODERN_COLORS = ChartColors.CYBER_PALETTE

    private var theme = ThemeFactory.getTheme(false)
    fun makePies(pies: Pies) : String {
        theme = if (pies.pieDisplay.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(pies.pieDisplay.theme, pies.pieDisplay.useDark)
        } else {
            ThemeFactory.getTheme(pies.pieDisplay)
        }
        val pieCount = pies.pies.size
        val pieWidth = 80 // Increased for Syne font and spacing
        val totalPieWidth = pieCount * pieWidth
        val width = totalPieWidth + 64 // Using 8-point grid


        // Calculate left margin to center the pies in the container
        val leftMargin = (width - totalPieWidth) / 2

        val sb = StringBuilder()
        sb.append(makeHead(width, pies))
        sb.append("<defs>")
        sb.append(filters(pies))
        sb.append(gradients(pies, pies.pieDisplay.id))

        sb.append("</defs>")
        // Apply Background Pattern overlay
        sb.append("<rect width='100%' height='100%' fill='${theme.canvas}' rx='${theme.cornerRadius}' pointer-events='none'/>")

        pies.pies.forEachIndexed { index, pie ->
            val x = leftMargin + (index * pieWidth)
            val delay = index * 0.12
            // Outer group handles position, inner group handles reveal animation
            sb.append("""<g transform="translate($x,16)">""")
            sb.append("""<g class="pie-reveal-container" style="animation: revealScale 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) ${delay}s forwards; opacity: 0;">""")
            sb.append(makePieSvg(pie, pies.pieDisplay, index))
            sb.append(makeLabel(pie, pies.pieDisplay))
            sb.append("</g></g>")
        }
        sb.append(tail())

        return sb.toString()
    }

    private fun makeHead(width: Int, pies: Pies) : String {
        val height = pies.maxRows() * 12 + 70  // Increased to accommodate multi-line labels and 8-point grid
        // Add padding for the shadow effect (20% on each side)
        val shadowPadding = 24
        val paddedWidth = width + shadowPadding * 2
        val paddedHeight = height + shadowPadding * 2

        val outerHeight = (1+pies.pieDisplay.scale) * paddedHeight
        val outerWidth = (1+pies.pieDisplay.scale) * paddedWidth

        //val bg =  BackgroundHelper.getBackGroundPath(pies.pieDisplay.useDark, pies.pieDisplay.id, width = width.toFloat(), height = height.toFloat())

        return """<svg xmlns="http://www.w3.org/2000/svg" height="${outerHeight/ DISPLAY_RATIO_16_9}" width="${outerWidth/DISPLAY_RATIO_16_9}" viewBox="-$shadowPadding -$shadowPadding $paddedWidth $paddedHeight" id="id_${pies.pieDisplay.id}">
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="${height+16}" viewBox="0 0 $width ${height+16}" >
            """
    }


    private fun tail() = """</svg></svg>"""

    private fun makePieSvg(pie: Pie, display: PieDisplay, index: Int) : String {
        val fill = theme.glassEffect
        val gradientId = "id_${display.id}_pieGradient_$index"


        //language=svg
        return """
            <svg class="pie" width="36" height="36" x="16" y="8" viewBox="0 0 36 36">
                <!-- Background circle -->
                <circle cx="18" cy="18" r="14" fill="${fill}"/>

                <!-- Glass overlay -->
                <circle cx="18" cy="18" r="14" fill="url(#glassOverlay)" class="glass-overlay"/>

                <!-- Base border -->
                <path d="M18 4 a 14 14 0 0 1 0 28 a 14 14 0 0 1 0 -28" 
                      style="fill: none; stroke: url(#glassBorder); stroke-width: 0.5;"/>

                <!-- High-Impact Progress stroke -->
                <path stroke-dasharray="${pie.percent}, 100" 
                      d="M18 4 a 14 14 0 0 1 0 28 a 14 14 0 0 1 0 -28" 
                      stroke="url(#${gradientId})" 
                      style="fill: none; stroke-width: 3.5; stroke-linecap: round; filter: url(#glow);">
                    <animate attributeName="stroke-dashoffset" values="${pie.percent};0" dur="1.2s" repeatCount="1" cubic-bezier="0.4, 0, 0.2, 1"/>
                </path>

                <!-- Distinctive Percentage Text -->
                <text x="18" y="18" dy="0.35em" style="fill: ${theme.primaryText} !important; font-family: ${theme.fontFamily}; font-size: 9px; font-weight: 800; text-anchor: middle; opacity: 0;">
                    ${pie.percent}%
                    <animate attributeName="opacity" values="0;1" dur="1s" delay="0.5s" fill="freeze"/>
                </text>
            </svg>
            """.trimIndent()
    }
    private fun makeLabel(pie: Pie, display: PieDisplay): String {
        val fontSize = 9 / theme.fontWidthMultiplier
        val sb = StringBuilder()
        // Adjusted x to 40 (center of 80px pieWidth)
        sb.append("""<text x="40" y="60" style="font-family: ${theme.fontFamily}; font-size: ${fontSize}px; font-weight: 700; text-anchor: middle; text-transform: uppercase; letter-spacing: 0.5px;">""")
        val labels = pie.label.split(" ")
        labels.forEachIndexed { idx, s ->
            var dy = 8
            if(0 == idx) {
                dy = 0
            }
            sb.append("""
                <tspan x="40" dy="$dy" style="fill: ${theme.secondaryText} !important;">${s.escapeXml()}</tspan>
                """.trimIndent())
        }
        sb.append("</text>")
        return sb.toString()
    }

    private fun filters(pies: Pies) =
        """
             <style>
                ${theme.fontImport}
                @keyframes revealScale {
                    from { transform: scale(0.8); opacity: 0; }
                    to { transform: scale(1); opacity: 1; }
                }
                /* Set transform origin to center of individual pie groups */
                .pie-reveal-container {
                    transform-origin: center;
                }
               #id_${pies.pieDisplay.id} .pie {
                    transition: transform 0.3s ease;
                    cursor: pointer;
                }
                #id_${pies.pieDisplay.id} .pie:hover {
                    transform: scale(1.05);
                    filter: brightness(1.1);
                }
                #id_${pies.pieDisplay.id} .pie-container {
                    transition: all 0.3s ease;
                }
                #id_${pies.pieDisplay.id} .pie-container:hover text {
                    font-weight: bold;
                }
                #id_${pies.pieDisplay.id} .glass-overlay {
                    pointer-events: none;
                    opacity: 0.7;
                }
             </style>

             <!-- Glass effect gradients -->
             <linearGradient id="glassOverlay" x1="0%" y1="0%" x2="0%" y2="100%">
                 <stop offset="0%" style="stop-color:rgba(255,255,255,0.4);stop-opacity:1" />
                 <stop offset="30%" style="stop-color:rgba(255,255,255,0.2);stop-opacity:1" />
                 <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                 <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
             </linearGradient>

             <!-- Radial gradient for realistic light reflections -->
             <radialGradient id="glassRadial" cx="30%" cy="30%" r="70%">
                 <stop offset="0%" style="stop-color:rgba(255,255,255,0.5);stop-opacity:1" />
                 <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                 <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
             </radialGradient>

             <!-- Frosted glass blur filter -->
             <filter id="glassBlur" x="-10%" y="-10%" width="120%" height="120%">
                 <feGaussianBlur in="SourceGraphic" stdDeviation="1.5" />
             </filter>

             <!-- Glass border gradient -->
             <linearGradient id="glassBorder" x1="0%" y1="0%" x2="0%" y2="100%">
                 <stop offset="0%" style="stop-color:rgba(255,255,255,0.6);stop-opacity:1" />
                 <stop offset="50%" style="stop-color:rgba(255,255,255,0.2);stop-opacity:1" />
                 <stop offset="100%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
             </linearGradient>

             <!-- Glow filter for hover effect -->
             <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                 <feGaussianBlur in="SourceGraphic" stdDeviation="2" result="blur"/>
                 <feColorMatrix in="blur" type="matrix" values="
                     1 0 0 0 0
                     0 1 0 0 0
                     0 0 1 0 0
                     0 0 0 18 -7
                 " result="glow"/>
                 <feMerge>
                     <feMergeNode in="glow"/>
                     <feMergeNode in="SourceGraphic"/>
                 </feMerge>
             </filter>
         """.trimIndent()

    private fun gradients(pies: Pies, id: String): String {
        val sb = StringBuilder()

        pies.pies.forEachIndexed { index, _ ->
            val color = getColorForIndex(index, pies.pieDisplay)

            // Create enhanced gradient with glass-like appearance
            sb.append("""
                <linearGradient id="id_${id}_pieGradient_$index" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="$color" stop-opacity="0.9"/>
                    <stop offset="40%" stop-color="$color" stop-opacity="0.95"/>
                    <stop offset="100%" stop-color="$color" stop-opacity="0.8"/>
                </linearGradient>

                <!-- Radial gradient for this specific pie segment -->
                <radialGradient id="pieRadial_$index" cx="30%" cy="30%" r="70%">
                    <stop offset="0%" stop-color="$color" stop-opacity="0.7"/>
                    <stop offset="70%" stop-color="$color" stop-opacity="0.3"/>
                    <stop offset="100%" stop-color="$color" stop-opacity="0.1"/>
                </radialGradient>
            """.trimIndent())
        }

        return sb.toString()
    }

    private fun getColorForIndex(index: Int, display: PieDisplay): String {
        // If we have a custom outline color, use it for the first item
        if (index == 0 && display.outlineColor != "#050C9C") {
            return display.outlineColor
        }

        // Otherwise use our modern color palette
        return theme.chartPalette[index % theme.chartPalette.size].color
    }
}

fun main() {
    val pieMaker = PieMaker()
    val pies = mutableListOf(Pie(percent = 40f, label = "Mathematics"), Pie(percent = 20f, label = "English"), Pie(
        percent = 30f,
        label = "French"
    ), Pie(percent = 10f, label = "Science"))
    val svg = pieMaker.makePies(Pies(
        pies = pies,
        pieDisplay = PieDisplay(baseColor = "#B9B4C7", outlineColor = "#DA0C81", scale = 2f, useDark = true)
    ))
    val outfile2 = File("gen/pies.svg")
    outfile2.writeBytes(svg.toByteArray())
}
