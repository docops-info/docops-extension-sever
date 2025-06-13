package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.joinXmlLines
import java.io.File

class PieMaker {

    // Array of modern, visually appealing colors for the pie charts
    private val MODERN_COLORS = arrayOf(
        "#4361ee", "#3a0ca3", "#7209b7", "#f72585", "#4cc9f0",
        "#4895ef", "#560bad", "#f15bb5", "#00bbf9", "#00f5d4",
        "#ff9e00", "#ff0054", "#390099", "#9e0059", "#ffbd00"
    )

    fun makePies(pies: Pies) : String {
        val pieCount = pies.pies.size
        val pieWidth = 36 // Width of each pie
        val totalPieWidth = pieCount * pieWidth
        val width = totalPieWidth + 20 // Add some padding

        // Calculate left margin to center the pies in the container
        val leftMargin = (width - totalPieWidth) / 2

        val sb = StringBuilder()
        sb.append(makeHead(width, pies))
        sb.append("<defs>")
            sb.append(filters(pies))
            sb.append(gradients(pies))
        sb.append("</defs>")
        pies.pies.forEachIndexed { index, pie ->
            val x = leftMargin + (index * pieWidth)
            sb.append("""<g transform="translate($x,5)" class="pie-container">""")
            sb.append(makePieSvg(pie, pies.pieDisplay, index))
            sb.append(makeLabel(pie, pies.pieDisplay, index))
            sb.append("</g>")
        }
        sb.append(tail())

        return joinXmlLines(sb.toString())
    }

    private fun makeHead(width: Int, pies: Pies) : String {
        val height = pies.maxRows() * 10 + 40
        // Add padding for the shadow effect (20% on each side)
        val shadowPadding = 20
        val paddedWidth = width + shadowPadding * 2
        val paddedHeight = height + shadowPadding * 2

        val outerHeight = (1+pies.pieDisplay.scale) * paddedHeight
        val outerWidth = (1+pies.pieDisplay.scale) * paddedWidth
        var backgroundColor = ""
        if(pies.pieDisplay.useDark) {
            backgroundColor = """<rect width="100%" height="100%" rx="10" ry="10" fill="#374151"/>"""
        } else {
            backgroundColor = """<rect width="100%" height="100%" rx="10" ry="10" fill="#ffffff" filter="url(#dropShadow)"/>"""
        }
        return """<svg xmlns="http://www.w3.org/2000/svg" height="${outerHeight/ DISPLAY_RATIO_16_9}" width="${outerWidth/DISPLAY_RATIO_16_9}" viewBox="-$shadowPadding -$shadowPadding $paddedWidth $paddedHeight" id="id_${pies.pieDisplay.id}">
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="${height+10}" viewBox="0 0 $width ${height+10}" >
            $backgroundColor
            """
    }

    private fun tail() = """</svg></svg>"""

    private fun makePieSvg(pie: Pie, display: PieDisplay, index: Int) : String {
        val fill = if(display.useDark) "#2d3748" else "#f8f9fa"
        val gradientId = "pieGradient_$index"
        // Use a dark color for text on light background and vice versa
        val textColor = if(display.useDark) "#FCFCFC" else "#333333"

        //language=svg
        return """
        <svg class="pie" width="36" height="36" style="display: block;margin: 10px auto; max-width: 80%; max-height: 250px;">
            <!-- Background circle -->
            <circle cx="18" cy="18" r="16" fill="${fill}" filter="url(#dropShadow)"/>

            <!-- Base circle -->
            <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" 
                  style="fill: ${fill}; stroke: ${display.baseColor}; stroke-width: 1; stroke-opacity: 0.5;"/>

            <!-- Progress circle with gradient fill -->
            <path stroke-dasharray="${pie.percent}, 100" 
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" 
                  stroke="url(#${gradientId})" 
                  style="fill: none; stroke-width: 3; stroke-linecap: round;">
                <animate attributeName="stroke-dashoffset" values="${pie.percent};0" dur="1.5s" repeatCount="1"/>
            </path>

            <!-- Percentage text with animation -->
            <text x="18" y="18" dy="0.35em" style="font-family: Arial, Helvetica, sans-serif; font-size: 8px; font-weight: bold; text-anchor: middle; fill: ${textColor}; opacity: 0;">
                ${pie.percent}%
                <animate attributeName="opacity" values="0;1" dur="2s" fill="freeze"/>
            </text>
        </svg>
        """.trimIndent()
    }

    private fun makeLabel(pie: Pie, display: PieDisplay, index: Int): String {
        val textColor = if(display.useDark) "#f9fafb" else "#333333"
        val sb = StringBuilder()
        sb.append("""<text x="18" y="48" style="font-family: Arial, Helvetica, sans-serif; font-size: 6px; font-weight: 500; text-anchor: middle;">""")
        val labels = pie.label.split(" ")
        labels.forEachIndexed { idx, s ->
            var dy = 6
            if(0 == idx) {
                dy = 0
            }
            sb.append("""
            <tspan x="18" dy="$dy" style="font-family: Arial, Helvetica, sans-serif; fill: ${textColor};">${s.escapeXml()}</tspan>
            """.trimIndent())
        }
        sb.append("</text>")
        return sb.toString()
    }

    private fun filters(pies: Pies) =
         """
             <style>
               #id_${pies.pieDisplay.id} .pie {
                    transition: transform 0.3s ease;
                    cursor: pointer;
                    filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, 0.2));
                }
                #id_${pies.pieDisplay.id} .pie:hover {
                    transform: scale(1.05);
                    filter: url(#glow);
                }
                #id_${pies.pieDisplay.id} .pie-container {
                    transition: all 0.3s ease;
                }
                #id_${pies.pieDisplay.id} .pie-container:hover text {
                    font-weight: bold;
                }
             </style>

             <!-- Drop shadow filter -->
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
            sb.append("""
                <linearGradient id="pieGradient_$index" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="${color}" stop-opacity="1"/>
                    <stop offset="100%" stop-color="${color}" stop-opacity="0.7"/>
                </linearGradient>
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
}

fun main() {
    val pieMaker = PieMaker()
    val pies = mutableListOf(Pie(percent = 40f, label = "Mathematics"), Pie(percent = 20f, label = "English"), Pie(
        percent = 30f,
        label = "French"
    ), Pie(percent = 10f, label = "Science"))
    val svg = pieMaker.makePies(Pies(
        pies = pies,
        pieDisplay = PieDisplay(baseColor = "#B9B4C7", outlineColor = "#DA0C81", scale = 2f, useDark = false)
    ))
    val outfile2 = File("gen/pies.svg")
    outfile2.writeBytes(svg.toByteArray())
}
