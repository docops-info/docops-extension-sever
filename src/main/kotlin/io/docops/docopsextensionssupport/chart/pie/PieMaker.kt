package io.docops.docopsextensionssupport.chart.pie

import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.formatHex
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import java.io.File

class PieMaker {

    // Array of modern, visually appealing colors for the pie charts
    private val MODERN_COLORS = arrayOf(
        "#4361ee", "#3a0ca3", "#7209b7", "#f72585", "#4cc9f0",
        "#4895ef", "#560bad", "#f15bb5", "#00bbf9", "#00f5d4",
        "#ff9e00", "#ff0054", "#390099", "#9e0059", "#ffbd00"
    )

    private var theme = ThemeFactory.getTheme(false)
    fun makePies(pies: Pies) : String {
        theme = ThemeFactory.getTheme(pies.pieDisplay)
        val pieCount = pies.pies.size
        val pieWidth = 60 // Increased from 36 to prevent horizontal overlap
        val totalPieWidth = pieCount * pieWidth
        val width = totalPieWidth + 40 // More padding


        // Calculate left margin to center the pies in the container
        val leftMargin = (width - totalPieWidth) / 2

        val sb = StringBuilder()
        sb.append(makeHead(width, pies))
        sb.append("<defs>")
        sb.append(filters(pies))
        sb.append(gradients(pies))

        sb.append("</defs>")
        // Apply Background Pattern overlay
        sb.append("<rect width='100%' height='100%' fill='${theme.canvas}' rx='${theme.cornerRadius}' pointer-events='none'/>")

        pies.pies.forEachIndexed { index, pie ->
            val x = leftMargin + (index * pieWidth)
            val delay = index * 0.12
            // Outer group handles position, inner group handles reveal animation
            sb.append("""<g transform="translate($x,5)">""")
            sb.append("""<g class="pie-reveal-container" style="animation: revealScale 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) ${delay}s forwards; opacity: 0;">""")
            sb.append(makePieSvg(pie, pies.pieDisplay, index))
            sb.append(makeLabel(pie, pies.pieDisplay))
            sb.append("</g></g>")
        }
        sb.append(tail())

        return sb.toString()
    }

    private fun makeHead(width: Int, pies: Pies) : String {
        val height = pies.maxRows() * 10 + 50  // Increased from 40 to 50 to accommodate multi-line labels
        // Add padding for the shadow effect (20% on each side)
        val shadowPadding = 20
        val paddedWidth = width + shadowPadding * 2
        val paddedHeight = height + shadowPadding * 2

        val outerHeight = (1+pies.pieDisplay.scale) * paddedHeight
        val outerWidth = (1+pies.pieDisplay.scale) * paddedWidth

        //val bg =  BackgroundHelper.getBackGroundPath(pies.pieDisplay.useDark, pies.pieDisplay.id, width = width.toFloat(), height = height.toFloat())

        return """<svg xmlns="http://www.w3.org/2000/svg" height="${outerHeight/ DISPLAY_RATIO_16_9}" width="${outerWidth/DISPLAY_RATIO_16_9}" viewBox="-$shadowPadding -$shadowPadding $paddedWidth $paddedHeight" id="id_${pies.pieDisplay.id}">
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="${height+10}" viewBox="0 0 $width ${height+10}" >
            """
    }


    private fun tail() = """</svg></svg>"""

    private fun makePieSvg(pie: Pie, display: PieDisplay, index: Int) : String {
        val fill = theme.glassEffect
        val gradientId = "pieGradient_$index"


        //language=svg
        return """
            <svg class="pie" width="36" height="36" x="12" y="5" viewBox="0 0 36 36">
                <!-- Background circle -->
                <circle cx="18" cy="18" r="16" fill="${fill}"/>

                <!-- Glass overlay -->
                <circle cx="18" cy="18" r="16" fill="url(#glassOverlay)" class="glass-overlay"/>

                <!-- Base border -->
                <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" 
                      style="fill: none; stroke: url(#glassBorder); stroke-width: 0.5;"/>

                <!-- High-Impact Progress stroke -->
                <path stroke-dasharray="${pie.percent}, 100" 
                      d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" 
                      stroke="url(#${gradientId})" 
                      style="fill: none; stroke-width: 3.5; stroke-linecap: round; filter: url(#glow);">
                    <animate attributeName="stroke-dashoffset" values="${pie.percent};0" dur="1.2s" repeatCount="1" cubic-bezier="0.4, 0, 0.2, 1"/>
                </path>

                <!-- Distinctive Percentage Text -->
                <text x="18" y="18" dy="0.35em" style="font-family: ${theme.fontFamily}; font-size: 6px; font-weight: 800; text-anchor: middle; fill: ${theme.primaryText}; opacity: 0;">
                    ${pie.percent}%
                    <animate attributeName="opacity" values="0;1" dur="1s" delay="0.5s" fill="freeze"/>
                </text>
            </svg>
            """.trimIndent()
    }
    private fun makeLabel(pie: Pie, display: PieDisplay): String {
        val sb = StringBuilder()
        // Adjusted x to 30 (center of 60px pieWidth)
        sb.append("""<text x="30" y="48" style="font-family: ${theme.fontFamily}; font-size: 5px; font-weight: 700; text-anchor: middle; text-transform: uppercase; letter-spacing: 0.5px;">""")
        val labels = pie.label.split(" ")
        labels.forEachIndexed { idx, s ->
            var dy = 6
            if(0 == idx) {
                dy = 0
            }
            sb.append("""
                <tspan x="30" dy="$dy" style="fill: ${theme.secondaryText};">${s.escapeXml()}</tspan>
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
                    filter: url(#glassDropShadow);
                }
                #id_${pies.pieDisplay.id} .pie:hover {
                    transform: scale(1.05);
                    filter: url(#glassDropShadow) brightness(1.1);
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
                #id_${pies.pieDisplay.id} .glass-highlight {
                    pointer-events: none;
                    opacity: 0.5;
                    transition: opacity 0.2s ease;
                }
                #id_${pies.pieDisplay.id} .pie:hover + .glass-highlight {
                    opacity: 0.8;
                }
             </style>

             <!-- Glass effect gradients -->
             <linearGradient id="glassOverlay" x1="0%" y1="0%" x2="0%" y2="100%">
                 <stop offset="0%" style="stop-color:rgba(255,255,255,0.4);stop-opacity:1" />
                 <stop offset="30%" style="stop-color:rgba(255,255,255,0.2);stop-opacity:1" />
                 <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                 <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
             </linearGradient>

             <!-- Highlight gradient -->
             <linearGradient id="glassHighlight" x1="0%" y1="0%" x2="0%" y2="100%">
                 <stop offset="0%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                 <stop offset="60%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                 <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
             </linearGradient>

             <!-- Radial gradient for realistic light reflections -->
             <radialGradient id="glassRadial" cx="30%" cy="30%" r="70%">
                 <stop offset="0%" style="stop-color:rgba(255,255,255,0.5);stop-opacity:1" />
                 <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                 <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
             </radialGradient>

             <!-- Enhanced drop shadow filter for glass elements -->
             <filter id="glassDropShadow" x="-30%" y="-30%" width="160%" height="160%">
                 <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="rgba(0,0,0,0.2)"/>
             </filter>

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

             <!-- Legacy filters for backward compatibility -->
             <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
                 <feGaussianBlur in="SourceAlpha" stdDeviation="1" result="blur"/>
                 <feOffset in="blur" dx="1" dy="1" result="offsetBlur"/>
                 <feComponentTransfer in="offsetBlur" result="shadow">
                     <feFuncA type="linear" slope="0.3"/>
                 </feComponentTransfer>
                 <feMerge>
                     <feMergeNode in="shadow"/>
                     <feMergeNode in="SourceGraphic"/>
                 </feMerge>
             </filter>

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

    private fun gradients(pies: Pies): String {
        val sb = StringBuilder()

        pies.pies.forEachIndexed { index, _ ->
            val color = getColorForIndex(index, pies.pieDisplay)

            // Create brighter and darker versions of the color for enhanced gradients
            val brighterColor = brightenColor(color, 0.3)

            // Create enhanced gradient with glass-like appearance
            sb.append("""
                <linearGradient id="pieGradient_$index" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="$brighterColor" stop-opacity="0.9"/>
                    <stop offset="40%" stop-color="$color" stop-opacity="0.95"/>
                    <stop offset="100%" stop-color="$color" stop-opacity="0.8"/>
                </linearGradient>

                <!-- Radial gradient for this specific pie segment -->
                <radialGradient id="pieRadial_$index" cx="30%" cy="30%" r="70%">
                    <stop offset="0%" stop-color="$brighterColor" stop-opacity="0.7"/>
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
        return MODERN_COLORS[index % MODERN_COLORS.size]
    }

    /**
     * Helper function to brighten a color by a given factor.
     *
     * @param hexColor The hex color to brighten (e.g., "#4361ee")
     * @param factor The factor to brighten by (0.0 to 1.0)
     * @return The brightened hex color
     */
    private fun brightenColor(hexColor: String, factor: Double): String {
        val hex = hexColor.replace("#", "")
        val r = hex.substring(0, 2).toInt(16)
        val g = hex.substring(2, 4).toInt(16)
        val b = hex.substring(4, 6).toInt(16)


        val newR = (r + (255 - r) * factor).toInt().coerceIn(0, 255)
        val newG = (g + (255 - g) * factor).toInt().coerceIn(0, 255)
        val newB = (b + (255 - b) * factor).toInt().coerceIn(0, 255)

        return formatHex( newR, newG, newB)
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
