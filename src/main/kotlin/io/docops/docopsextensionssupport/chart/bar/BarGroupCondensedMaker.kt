package io.docops.docopsextensionssupport.chart.bar


import io.docops.docopsextensionssupport.chart.ColorPaletteFactory
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.support.generateRectanglePathData
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.textWidth
import kotlin.collections.get
import kotlin.compareTo
import kotlin.div
import kotlin.text.toFloat
import kotlin.times
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class BarGroupCondensedMaker {
    private val BAR_HEIGHT = 10f
    private val width = 650f // Increased from 600f to provide more space for labels
    private val BOTTOM_BUFFER = 75f // Increased from 55f to provide more space at the bottom
    private var additional = 0f
    private var height = 400f
    private var TOTAL_BAR_HEIGHT = 400f

    private var fontColor = "#fcfcfc"
    var theme: DocOpsTheme = ThemeFactory.getTheme(false)
    fun makeBar(barGroup: BarGroup): String {
        theme = if (barGroup.display.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(barGroup.display.theme, barGroup.display.useDark)
        } else {
            ThemeFactory.getTheme(barGroup.display)
        }
        val svgColor = SVGColor(barGroup.display.baseColor)
        fontColor = determineTextColor(barGroup.display.baseColor)

        // Get the palette type for this bar group
        val paletteType = getPaletteType(barGroup.display)


        val sb = StringBuilder()
        val h = determineHeight(barGroup)
        val w = determineWidth(barGroup)
        sb.append(makeHead(h, w, barGroup, scale= barGroup.display.scale))

        height = h
        sb.append(makeDefs(barGroup, paletteType))
        sb.append("<g transform='scale(${barGroup.display.scale})'>")

        // Use theme canvas instead of pure colors - already correct!
        sb.append("""<rect width="100%" height="100%" fill="${theme.canvas}"/>""")

        // Move title up and give it more space (y=24 instead of 28)
        sb.append("""<text x="${width/2}" text-anchor="middle" y="24" style="font-size: 24px; fill: ${theme.primaryText}; font-family: ${theme.fontFamily}; font-weight: 700; letter-spacing: -0.01em;">${barGroup.title}</text>""")

        sb.append(makeColumnHeader(barGroup))
        sb.append(addTickBars(h, w, barGroup))

        // Push bars down more (from 30 to 40) to give room for right-side labels
        sb.append("""<g transform="translate(100,40)">""")
        var startY= 0
        barGroup.groups.forEach { group ->
            sb.append("""<g transform="translate(0,$startY)">""")
            sb.append(addGroup(barGroup, group, paletteType))
            sb.append("</g>")
            startY += group.series.size * 10 + 5

        }
        sb.append("</g>")

        // Push legend down much more (from 55 to 70) to create proper separation
        val legendY = TOTAL_BAR_HEIGHT + 70
        sb.append(addLegend(legendY, barGroup))

        sb.append("</g>")
        sb.append(end())
        return sb.toString()
    }


    private fun addGroup(barGroup: BarGroup, added: Group, paletteType: ColorPaletteFactory.PaletteType): String {
        val sb = StringBuilder()
        var startY = 10
        val bars = (added.series.size * 10) /2 + 14
        sb.append("""
        <text x="0" y="$bars" text-anchor="end" style="fill: ${theme.primaryText}; font-family: ${theme.fontFamily}; font-size:11px; font-weight: 600;">${added.label}</text>
    """.trimIndent())
        added.series.forEachIndexed { index, series ->
            val per = barGroup.scaleUp(series.value)
            // Use ColorPaletteFactory for colors
            val baseColor = ColorPaletteFactory.getColorCyclic(paletteType, index) ?: "#4361ee"
            val color = baseColor

            // Improved rounded corners for a more modern look
            val path = generateRectanglePathData(per.toFloat(), 10f, 0.0f, 5.0f, 5.0f, 0.0f).replace("\n", "")
            sb.append("""
       <g transform="translate(10, $startY)">
            <path d="$path" fill="$color" class="bar shadowed"/>
            <!-- Move label slightly further right (per+8 instead of per+5) to ensure visibility -->
            <text x="${per+8}" y="8" class="bar-label" style="font-size: 11px; fill: ${theme.primaryText}; font-family: ${theme.fontFamily}; font-weight: 600;">${barGroup.valueFmt(series.value)}</text>
        </g>
        """.trimIndent())
            startY+=10

        }
        return sb.toString()

    }

    private fun makeHead(h: Float, w: Float, barGroup: BarGroup, scale: Double): String {
        return  """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="${(w * scale)/ DISPLAY_RATIO_16_9}" height="${(h * scale )/DISPLAY_RATIO_16_9}" viewBox="0 0 ${w*scale} ${h*scale}" xmlns="http://www.w3.org/2000/svg" id="id_${barGroup.id}" preserveAspectRatio="xMidYMid meet">
        """.trimIndent()
    }

    private fun end() = "</svg>"
    // Helper methods for palette type determination
    private fun getPaletteType(display: BarGroupDisplay): ColorPaletteFactory.PaletteType {
        return when {
            display.paletteType.isNotBlank() -> {
                try {
                    ColorPaletteFactory.PaletteType.valueOf(display.paletteType.uppercase())
                } catch (e: IllegalArgumentException) {
                    getDefaultPaletteType(display.useDark)
                }
            }
            else -> getDefaultPaletteType(display.useDark)
        }
    }

    private fun getDefaultPaletteType(useDark: Boolean): ColorPaletteFactory.PaletteType {
        return if (useDark) {
            ColorPaletteFactory.PaletteType.URBAN_NIGHT
        } else {
            ColorPaletteFactory.PaletteType.TABLEAU
        }
    }

    private fun makeDefs(barGroup: BarGroup, paletteType: ColorPaletteFactory.PaletteType): String {
        val defs = StringBuilder()
        defs.append("<defs>")

        // Use ColorPaletteFactory for gradient definitions
        val labels = barGroup.uniqueLabels()
        val sz = labels.size
        for(i in 0 until sz) {
            val baseColor = ColorPaletteFactory.getColorCyclic(paletteType, i) ?: "#4361ee"
            val svgColor = SVGColor(baseColor)
            val lighterColor = svgColor.brightenColor(baseColor, 0.15)
            val darkerColor = svgColor.darkenColor(baseColor, 0.2)

            defs.append(svgColor.createSimpleGradient(lighterColor, labels[i].replace(" ", "")))
        }

        defs.append("<style type=\"text/css\">")
        defs.append("""
        @keyframes barSlideIn {
            from {
                transform: scaleX(0);
                opacity: 0;
            }
            to {
                transform: scaleX(1);
                opacity: 1;
            }
        }
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        #id_${barGroup.id} .shadowed {
            -webkit-filter: drop-shadow(1px 2px 2px rgba(0, 0, 0, .2));
            filter: drop-shadow(1px 2px 2px rgba(0, 0, 0, .2));
            transition: all 0.3s ease;
        }
        #id_${barGroup.id} .bar {
            transform-origin: left center;
            animation: barSlideIn 0.6s ease-out backwards;
        }
        #id_${barGroup.id} .bar:nth-child(1) { animation-delay: 0.1s; }
        #id_${barGroup.id} .bar:nth-child(2) { animation-delay: 0.2s; }
        #id_${barGroup.id} .bar:nth-child(3) { animation-delay: 0.3s; }
        #id_${barGroup.id} .bar:nth-child(4) { animation-delay: 0.4s; }
        #id_${barGroup.id} .bar:hover {
            filter: brightness(1.15) saturate(1.2);
            transform: translateY(-2px);
            -webkit-filter: drop-shadow(2px 4px 4px rgba(0, 0, 0, .3));
            filter: drop-shadow(2px 4px 4px rgba(0, 0, 0, .3));
        }
        #id_${barGroup.id} text {
            font-family: ${theme.fontFamily};
            transition: all 0.3s ease;
        }
        #id_${barGroup.id} .bar-label {
            opacity: 0.9;
            animation: fadeIn 0.4s ease-out backwards;
        }
        #id_${barGroup.id} .bar:hover + .bar-label {
            opacity: 1;
            font-weight: bold;
        }
    """.trimIndent())
        defs.append("</style>")
        defs.append("</defs>")
        return defs.toString()
    }


    private fun addTickBars(h: Float, w: Float, barGroup: BarGroup): String {
        val bottom = TOTAL_BAR_HEIGHT + 55  // Increased from 45 to 55 for more spacing
        val tickBar = barGroup.maxValue() / 4
        val sb = StringBuilder()

        // Main vertical axis - extend UP to y1="30" to separate left labels from chart area
        sb.append("""<line x1="109" x2="109" y1="30" y2="$bottom" class="light-shadow" stroke-width="2" stroke="${theme.accentColor}" stroke-opacity="0.8" />""")

        // Tick marks with alternating opacity for a more elegant grid
        for(i in 1..8 step 1) {
            val spot = barGroup.scaleUp(tickBar * i) + 108
            val opacity = if (i % 2 == 0) "0.4" else "0.6"
            sb.append("""
            <line x1="$spot" x2="$spot" y1="60" y2="$bottom" class="light-shadow" stroke-width="1" stroke="${theme.accentColor}" stroke-opacity="$opacity" stroke-dasharray="${if (i % 4 == 0) "none" else "1,1"}" />
            <text x="$spot" y="${bottom+15}" text-anchor="middle" style="font-size: 12px; fill: ${theme.primaryText}; font-family: ${theme.fontFamily};">${barGroup.valueFmt(tickBar*i)}</text>
        """.trimIndent())
        }

        // Bottom horizontal axis
        sb.append("""<line x1="0" x2="$width" y1="$bottom" y2="$bottom" class="light-shadow" stroke-width="1.5" stroke="${theme.accentColor}" stroke-opacity="0.8" />""")
        return sb.toString()
    }


    private fun determineHeight(barGroup: BarGroup): Float {
        var count = 0
        barGroup.groups.forEachIndexed { index, g ->
            count += g.series.size
        }
        val labels = legendRow(barGroup)

        // Calculate additional height needed for the legend
        // For each row beyond the first, add more space
        if(labels > 1) {
            // Base height for legend (title + padding) + extra height per additional row
            additional += 60 + (16 * (labels - 1))  // Increased from 50 to 60
        } else {
            // Minimum additional height for single row legend
            additional += 60  // Increased from 50 to 60
        }

        TOTAL_BAR_HEIGHT = count * BAR_HEIGHT + barGroup.groups.size * 5 + BAR_HEIGHT

        // Ensure there's enough space at the bottom for the legend
        return count * BAR_HEIGHT + barGroup.groups.size * 5 + BAR_HEIGHT + BOTTOM_BUFFER + 40 + additional  // Increased from 30 to 40
    }


    private fun determineWidth(barGroup: BarGroup): Float {
        return width
    }

    private fun addLegend(d: Float, group: BarGroup): String {
        val fColor = theme.primaryText
        val sb = StringBuilder()
        val distinct = group.uniqueLabels()

        // Calculate the legend height based on the number of rows and text height
        val rows = legendRow(group)
        val textHeight = 12 // Font size for legend text
        val rowSpacing = 16 // Increased spacing between rows
        val legendHeight = if (rows > 1) {
            // Title (16px) + padding (6px) + (rows * (text height + spacing))
            22 + (rows * rowSpacing)
        } else {
            // Title (16px) + padding (6px) + single row (text height)
            38
        }

        // Calculate the width needed for the legend rectangle based on the actual text content
        val (legendX, legendWidth) = calculateLegendWidth(group)

        // Add a background container for the legend
        sb.append("<g transform='translate(0, $d)'>")

        // Add a subtle background for the legend with adjusted width and height
        sb.append("""<rect x="$legendX" y="2" width="$legendWidth" height="$legendHeight" rx="8" ry="8" fill="${theme.accentColor}" fill-opacity="0.08" stroke="${theme.accentColor}" stroke-opacity="0.3" stroke-width="1" />""")

        // Legend title with improved styling - centered within the calculated width
        sb.append("""<text x="${width/2}" y="18" text-anchor="middle" style="font-family: ${theme.fontFamily}; fill: $fColor; font-size:12px; font-weight: 700;">Legend</text> """)

        var y = 22  // Starting position after title
        var currentRowWidth = 0f
        var startX = legendX + 20  // Start with padding from the left edge of the rectangle
        val maxAvailableWidth = legendWidth - 40  // Maximum width available for legend content (with padding on both sides)

        distinct.forEachIndexed { index, item ->
            // Width of this legend item: color indicator (10) + spacing (14) + text width + spacing between items (14)
            val itemWidth = 10f + 14f + item.textWidth("Helvetica", 10) + 14f

            // If this item would exceed the available width, start a new row
            if (currentRowWidth + itemWidth > maxAvailableWidth && currentRowWidth > 0) {
                y += rowSpacing  // Use consistent row spacing
                startX = legendX + 20  // Reset to left edge with padding
                currentRowWidth = 0f
            }

            val color = "url(#${item.replace(" ", "")})"

            // Create a group for each legend item for better organization
            sb.append("""<g class="legend-item">""")

            // Rounded rectangle for the color indicator
            sb.append("""<rect x="$startX" y="$y" width="10" height="10" rx="2" ry="2" fill="$color" class="shadowed"/>""")

            // Calculate vertical position to center text with the color indicator
            // For 10px height color indicator and 10px font size
            val textY = y + 8 // Position text to vertically align with the color indicator

            // Text with improved styling - increased font size for better readability
            sb.append("""<text x="${startX + 14}" y="$textY" style="font-family: ${theme.fontFamily}; fill: $fColor; font-size:11px; font-weight: 500; dominant-baseline: central;">""")
            sb.append("""<tspan x="${startX + 14}" dy="0">$item</tspan>""")
            sb.append("</text>")
            sb.append("</g>")

            currentRowWidth += itemWidth
            startX += itemWidth  // Move to the next position
        }

        sb.append("</g>")
        return sb.toString()
    }

    private fun legendRow(group: BarGroup): Int {
        val distinct = group.uniqueLabels()
        val padding = 20f // Padding on each side of the legend
        val maxAvailableWidth = width - padding * 2 // Maximum width available for legend content

        var currentRowWidth = 0f
        var rows = 1

        distinct.forEach { item ->
            // Width of this legend item: color indicator (10) + spacing (14) + text width + spacing between items (14)
            val itemWidth = 10f + 14f + item.textWidth("Helvetica", 10) + 14f

            // If this item would exceed the available width, start a new row
            if (currentRowWidth + itemWidth > maxAvailableWidth && currentRowWidth > 0) {
                rows++
                currentRowWidth = 0f
            }

            currentRowWidth += itemWidth
        }

        return rows
    }

    /**
     * Calculates the width needed for the legend rectangle based on the actual text content.
     * Returns a pair of (leftX, width) where leftX is the x-coordinate of the left edge of the rectangle
     * and width is the width of the rectangle.
     */
    private fun calculateLegendWidth(group: BarGroup): Pair<Float, Float> {
        val distinct = group.uniqueLabels()
        val rowWidths = mutableListOf<Float>()

        var currentRowWidth = 0f
        val padding = 20f // Padding on each side of the legend
        val maxAvailableWidth = width - padding * 2 // Maximum width available for legend content

        // Calculate width needed for each row
        distinct.forEach { item ->
            // Width of this legend item: color indicator (10) + spacing (14) + text width + spacing between items (14)
            val itemWidth = 10f + 14f + item.textWidth("Helvetica", 10) + 14f

            // If this item would exceed the available width, start a new row
            if (currentRowWidth + itemWidth > maxAvailableWidth && currentRowWidth > 0) {
                rowWidths.add(currentRowWidth)
                currentRowWidth = 0f
            }

            currentRowWidth += itemWidth
        }

        // Add the last row if it has content
        if (currentRowWidth > 0) {
            rowWidths.add(currentRowWidth)
        }

        // Find the maximum row width
        val maxRowWidth = rowWidths.maxOrNull() ?: 0f

        // Add padding on both sides
        val totalWidth = maxRowWidth + padding * 2

        // Ensure the width is at least a minimum value for aesthetics
        val finalWidth = maxOf(totalWidth, width * 0.4f)

        // Center the legend by calculating the left x-coordinate
        val leftX = (width - finalWidth) / 2

        return Pair(leftX, finalWidth)
    }
    fun getTheme(useDark: Boolean = false) : BarTheme {
        return if(useDark) {
            BarThemeDark()
        } else {
            BarThemeLite()
        }
    }
    private fun makeColumnHeader(barGroup: BarGroup) : String {
        return """
 <g>
    <!-- Move column headers down to y="40" from y="18" to give title more space -->
    <rect x="20" y="40" width="85" height="16" rx="3" ry="3" fill="${theme.accentColor}" fill-opacity="0.1" />
    <text x="107" y="50" text-anchor="end" style="fill: ${theme.primaryText}; font-family: ${theme.fontFamily}; font-size:12px; font-weight: bold;">${barGroup.xLabel?.escapeXml()}</text>

    <rect x="112" y="40" width="120" height="16" rx="3" ry="3" fill="${theme.accentColor}" fill-opacity="0.1" />
    <text x="115" y="50" text-anchor="start" style="fill: ${theme.primaryText}; font-family: ${theme.fontFamily}; font-size:12px; font-weight: bold;">${barGroup.yLabel?.escapeXml()}</text>
</g>
    """.trimIndent()
    }
}

open class BarTheme @OptIn(ExperimentalUuidApi::class) constructor(val background: String = "#F7F7F7", val lineColor: String = "#111111", val textColor: String = "#111111", val titleColor: String = "#000000", val id: String = Uuid.random().toHexString())

class BarThemeLite(background: String = "url(#condensedLite)", lineColor: String="#94a3b8", textColor: String="#334155", titleColor: String="#1e293b"): BarTheme(background, lineColor, textColor, titleColor)
class BarThemeDark(background: String = "url(#condensedDark)",  lineColor: String = "#94a3b8",  textColor: String = "#e2e8f0", titleColor: String = "#f8fafc"): BarTheme(background, lineColor, textColor, titleColor)
