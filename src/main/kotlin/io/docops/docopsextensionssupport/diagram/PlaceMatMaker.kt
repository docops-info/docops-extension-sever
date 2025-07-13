package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.gradientFromColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9

import io.docops.docopsextensionssupport.svgsupport.textWidth
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.ShapeResponse
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

class PlaceMatMaker(val placeMatRequest: PlaceMatRequest, val type: String= "SVG", val isPdf: Boolean = false) {

    private var bgColor = "#f2f2f7" // iOS light gray background
    private var fgColor = "#1d1d1f" // iOS dark text color
    private val colors = mutableListOf<String>()

    private var useGrad = true

    fun makePlacerMat(): ShapeResponse {
        if(placeMatRequest.useDark) {
            bgColor = "#17242b" // Keep dark mode background
            fgColor = "#fcfcfc" // Keep dark mode text color
        }

        val width: Float = (placeMatRequest.placeMats.chunked(5)[0].size * 250).toFloat() + 60
        val height = placeMatRequest.placeMats.chunked(5).size * 110.0f + 50
        val sb = StringBuilder()
        val id = UUID.randomUUID().toString()
        sb.append(head(height+60, width = width, placeMatRequest.scale, id))
        initColors()
        if(!isPdf) {
            sb.append(defs(id))
        } else {
            useGrad = false
            sb.append("""
                <defs>
                <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="7" />
        <rect id="bbox" class="shadowed"  width="250" height="90" ry="18" rx="18"  />
        <path id="hconnector" d="M260,50.0 h34" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
        <path id="vconnector" d="M135,100 v34" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                </defs>
            """.trimIndent())
        }
        // Enhanced background with subtle gradient
        sb.append("""<rect width="100%" height="100%" fill="$bgColor" stroke="none"/>""")

        // Add subtle grid pattern for background texture
        if (!isPdf) {
            sb.append("""
            <pattern id="subtle-grid" width="40" height="40" patternUnits="userSpaceOnUse" patternTransform="rotate(45)">
                <rect width="40" height="40" fill="$bgColor"/>
                <path d="M 40 0 L 0 0 0 40" fill="none" stroke="${if(placeMatRequest.useDark) "#333333" else "#dddddd"}" stroke-width="0.5"/>
            </pattern>
            <rect width="100%" height="100%" fill="url(#subtle-grid)" opacity="0.4"/>
            """)
        }

        sb.append("<g transform=\"translate(0,0)\">")

        // Enhanced title with better styling
        sb.append("""<g transform="translate(0,0)">
            <text x="20" y="30" text-anchor="start" font-size="28" class="title-text" fill="$fgColor">${placeMatRequest.title}</text>
            <line x1="20" y1="38" x2="${Math.min(placeMatRequest.title.length * 16, 400)}" y2="38" stroke="$fgColor" stroke-width="2" opacity="0.6" />
        </g>
        <g transform="translate(0,50)">""")
        sb.append(makeBody(id))
        sb.append(makeLegend(height + 20 - 50, id, width))
        sb.append("</g>")
        sb.append("</g>")
        sb.append(tail())
        return ShapeResponse(sb.toString(), height = height + 60, width = width)
    }

    private fun makeBody(id: String): String {
        val sb = StringBuilder()
        var x = 0
        var y = 0
        placeMatRequest.placeMats.forEachIndexed {
                i, conn ->
            val svgColor = SVGColor(placeMatRequest.config.colorFromLegendName(conn.legend).color, UUID.randomUUID().toString())
            val textColor = svgColor.foreGroundColor

            var grad = "url(#grad_${conn.legendAsStyle()}_$id)"
            var strokeWidth = 1
            if(!placeMatRequest.fill) {
                strokeWidth = 3
            }
            if(placeMatRequest.useDark && !placeMatRequest.fill ) {
                grad = "#fcfcfc"
            }
            if(isPdf) {
                grad = placeMatRequest.config.colorFromLegendName(conn.legend).color
            }

            // Improved text layout
            val lines = conn.textToLines()
            val textY = lines.second

            // Enhanced text styling with iOS-style class
            val str = StringBuilder("""<text x="135" y="$textY" text-anchor="middle" class="boxText" style="fill:$textColor;">""")
            var newLine = false

            lines.first.forEachIndexed {
                    j, content ->
                var dy=""
                if(j>0) {
                    // Reduce line spacing from 24px to 18-20px for better text density
                    dy = if (lines.first.size > 2) "dy=\"18\"" else "dy=\"20\""
                }
                str.append("""<tspan x="135" $dy>$content</tspan>""")
            }
            str.append("</text>")
                if((i + 1) % 5 == 0) {
                    newLine = true
                }

                // Enhanced glass-style card with layered structure
                sb.append(
                    """
            <g transform="translate($x,$y)" class="glass-card">
                <rect x="10" y="10" width="250" height="90" ry="16" rx="16"
                      style="fill: ${grad}; stroke: url(#glassBorder);"
                      class="ios-card glass-border" filter="url(#glassDropShadow)"></rect>
                <rect x="10" y="10" width="250" height="90" ry="16" rx="16"
                      fill="url(#${conn.legendAsStyle()}GlassOverlay_$id)" class="glass-overlay"></rect>
                <rect x="10" y="10" width="250" height="90" ry="16" rx="16"
                      fill="url(#glassBackdrop)" class="glass-overlay"></rect>
                <rect x="18" y="18" width="234" height="35" rx="12" ry="12"
                      fill="url(#glassHighlight)" class="glass-highlight"></rect>
                <ellipse cx="40" cy="35" rx="15" ry="12" fill="rgba(255,255,255,0.3)" class="glass-highlight"></ellipse>
                $str
            """.trimIndent()
                )
                if(newLine) {
                    x = 0
                    y += 110
                }
                sb.append("</g>")

            if(!newLine)
                x += 260
        }
        return sb.toString()
    }
    private fun head(height: Float, width: Float, scale: Float = 1.0f, id: String)  = """
        <svg xmlns="http://www.w3.org/2000/svg" width="${(width*scale)/ DISPLAY_RATIO_16_9}" height="${(350*scale)/DISPLAY_RATIO_16_9}"
     viewBox="0 0 $width ${Math.max(height, 600.0f)}" xmlns:xlink="http://www.w3.org/1999/xlink" id="diag_$id">
    """.trimIndent()

    private fun tail() = "</svg>"

    fun initColors() {
        placeMatRequest.config.legend.forEach {
                item ->
            val choiceColor = item.color
            colors.add(choiceColor)
        }
    }
    private fun defs(id: String) : String {

        val grad= StringBuilder()
        val glassOverlays = StringBuilder()

        placeMatRequest.config.legend.forEach {
            item ->
            // Create iOS-style gradients with updated colors
            val gradientColors = when(item.legendAsStyle()) {
                "Business_Capability" -> Pair("#ff6b6b", "#ff5252") // Updated iOS-style gradient
                "Engineering" -> Pair("#4fc3f7", "#29b6f6") // Updated iOS-style gradient
                "Both" -> Pair("#ba68c8", "#ab47bc") // Updated iOS-style gradient
                else -> {
                    val colorMap = gradientFromColor(item.color)
                    Pair(colorMap["color1"] ?: "#000000", colorMap["color3"] ?: "#000000")
                }
            }

            val simplifiedGradient = """
                <linearGradient id="grad_${item.legendAsStyle()}_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:${gradientColors.first}" stop-opacity="0.9"/>
                    <stop offset="100%" style="stop-color:${gradientColors.second}" stop-opacity="0.8"/>
                </linearGradient>
            """.trimIndent()
            grad.append(simplifiedGradient)

            // Add category-specific glass overlay
            val color1 = gradientColors.first.replace("#", "")
            val color2 = gradientColors.second.replace("#", "")
            val glassOverlay = """
                <radialGradient id="${item.legendAsStyle()}GlassOverlay_$id" cx="30%" cy="30%" r="70%">
                    <stop offset="0%" style="stop-color:rgba(${Integer.parseInt(color1.substring(0, 2), 16)},${Integer.parseInt(color1.substring(2, 4), 16)},${Integer.parseInt(color1.substring(4, 6), 16)},0.3);stop-opacity:1" />
                    <stop offset="70%" style="stop-color:rgba(${Integer.parseInt(color2.substring(0, 2), 16)},${Integer.parseInt(color2.substring(2, 4), 16)},${Integer.parseInt(color2.substring(4, 6), 16)},0.15);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(${Integer.parseInt(color2.substring(0, 2), 16)},${Integer.parseInt(color2.substring(2, 4), 16)},${Integer.parseInt(color2.substring(4, 6), 16)},0.08);stop-opacity:1" />
                </radialGradient>
            """.trimIndent()
            glassOverlays.append(glassOverlay)
        }


        //language=svg
        return """
            <defs>
            $grad

            <!-- Enhanced Glass Effect Gradients -->
            <radialGradient id="glassBackdrop" cx="30%" cy="30%" r="70%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.4);stop-opacity:1" />
                <stop offset="50%" style="stop-color:rgba(255,255,255,0.2);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
            </radialGradient>

            <linearGradient id="glassOverlayMain" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.5);stop-opacity:1" />
                <stop offset="30%" style="stop-color:rgba(255,255,255,0.25);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0.08);stop-opacity:1" />
            </linearGradient>

            <linearGradient id="glassHighlight" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.8);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
            </linearGradient>

            <!-- Glass Border Gradients -->
            <linearGradient id="glassBorder" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.6);stop-opacity:1" />
                <stop offset="50%" style="stop-color:rgba(255,255,255,0.2);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
            </linearGradient>

            <!-- Category-Specific Glass Overlays -->
            $glassOverlays

            <!-- Enhanced Glass Filters -->
            <filter id="glassDropShadow" x="-30%" y="-30%" width="160%" height="160%">
                <feDropShadow dx="0" dy="8" stdDeviation="12" flood-color="rgba(0,0,0,0.25)"/>
                <feDropShadow dx="0" dy="2" stdDeviation="4" flood-color="rgba(0,0,0,0.15)"/>
            </filter>

            <filter id="glassInnerGlow" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur stdDeviation="2" result="coloredBlur"/>
                <feMerge>
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>

            <!-- Legacy iOS-style shadow filter -->
            <filter id="ios-shadow" x="-50%" y="-50%" width="200%" height="200%">
                <feDropShadow dx="0" dy="2" stdDeviation="8" flood-color="rgba(0,0,0,0.15)" flood-opacity="1"/>
                <feDropShadow dx="0" dy="4" stdDeviation="16" flood-color="rgba(0,0,0,0.1)" flood-opacity="1"/>
            </filter>

            <!-- Subtle inner highlight for depth -->
            <filter id="inner-highlight">
                <feFlood flood-color="rgba(255,255,255,0.2)"/>
                <feComposite operator="in" in2="SourceGraphic"/>
            </filter>

            <style>
                /* Enhanced Glass Card Effects */
                #diag_$id .glass-card {
                    transition: filter 0.2s ease;
                    transform-origin: center;
                }

                #diag_$id .glass-card:hover {
                    filter: brightness(1.1);
                    cursor: pointer;
                }

                #diag_$id .glass-overlay {
                    pointer-events: none;
                }

                #diag_$id .glass-highlight {
                    pointer-events: none;
                    opacity: 0.6;
                    transition: opacity 0.2s ease;
                }

                #diag_$id .glass-card:hover .glass-highlight {
                    opacity: 0.9;
                }

                #diag_$id .glass-border {
                    stroke-width: 1;
                    transition: stroke-width 0.2s ease;
                }

                #diag_$id .glass-card:hover .glass-border {
                    stroke-width: 2;
                }

                #diag_$id .glass-content {
                    backdrop-filter: blur(8px) saturate(150%);
                    -webkit-backdrop-filter: blur(8px) saturate(150%);
                }

                /* Legacy iOS card styles updated for glass */
                #diag_$id .ios-card {
                    filter: url(#glassDropShadow);
                    transition: all 0.2s ease-in-out;
                }

                #diag_$id .ios-card:hover {
                    filter: url(#glassDropShadow) brightness(1.1);
                    transform: translateY(-1px);
                }

                #diag_$id .boxText {
                    font-size: 20px;
                    font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    font-weight: 600;
                    letter-spacing: -0.02em;
                    text-shadow: 0 1px 2px rgba(0,0,0,0.1);
                }

                #diag_$id .title-text {
                    font-size: 28px;
                    font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    font-weight: 700;
                    letter-spacing: -0.03em;
                }

                #diag_$id .legend-box {
                    filter: url(#glassDropShadow);
                    opacity: 1;
                }

                #diag_$id .legend-text {
                    font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    font-weight: 600;
                    letter-spacing: -0.02em;
                }

                #diag_$id .legend-title {
                    font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    font-weight: 700;
                    letter-spacing: -0.03em;
                }
            </style>
            <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="7" />
            <rect id="ios-card" class="ios-card glass-card" width="250" height="90" ry="16" rx="16" />
            <path id="hconnector" d="M260,50.0 h34" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
            <path id="vconnector" d="M135,100 v34" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
            </defs>
        """.trimIndent()
    }
    private fun makeLegend(y: Float, id: String, svgWidth: Float): String {
        // Calculate total width needed for legend items
        var totalWidth = 0
        val legendItems = mutableListOf<Triple<String, Int, String>>() // grad, width, textColor

        // Get the longest legend text to ensure proper sizing
        var maxTextLen = 0

        placeMatRequest.config.legend.forEach { item ->
            var grad = "url(#grad_${item.legendAsStyle()}_$id)"
            if(!useGrad) {
                grad = item.color
            }
            val textColor = SVGColor(item.color, UUID.randomUUID().toString()).foreGroundColor
            // Add 20% to text width to ensure text fits properly
            val textLen = (item.legend.textWidth("Helvetica", 110) * 1.2).toInt()
            maxTextLen = maxOf(maxTextLen, textLen)

            // Store item details for later use
            legendItems.add(Triple(grad, textLen, textColor))

            // Add width plus increased spacing (100 instead of 80)
            totalWidth += (textLen + 100)
        }

        // Add padding to total width for better spacing
        val horizontalPadding = 120 // 60px on each side
        val legendGroupWidth = totalWidth + horizontalPadding

        // Calculate legend height based on content
        val titleHeight = 60 // Space for title and separator
        val itemHeight = 150 // Height of legend items with padding (increased from 130)
        val legendHeight = titleHeight + itemHeight + 90 // Additional bottom padding (increased to match 300px total)

        // Reduce legend scale to 0.12 as per iOS design requirements
        // Adjust based on number of items for better proportions
        val scaleFactor = when {
            placeMatRequest.config.legend.size > 5 -> 0.10
            placeMatRequest.config.legend.size <= 3 -> 0.14
            else -> 0.12
        }

        // Center the legend group horizontally
        // Calculate the starting x position to center the legend
        val centerX = svgWidth / 2 - (legendGroupWidth * scaleFactor) / 2 // Adjust for the scale factor

        // Modern legend with centered positioning and increased spacing
        val sb = StringBuilder("""<g transform="translate($centerX,$y),scale($scaleFactor)">""")

        // Enhanced glass-style background for the entire legend
        sb.append("""<rect x="0" y="0" width="$legendGroupWidth" height="$legendHeight" rx="20" ry="20" 
                      fill="${if(placeMatRequest.useDark) "#2a3a4a" else "rgba(245,245,247,0.85)"}" 
                      class="legend-background glass-content" filter="url(#glassDropShadow)"></rect>""")
        sb.append("""<rect x="0" y="0" width="$legendGroupWidth" height="$legendHeight" rx="20" ry="20"
                      fill="url(#glassOverlayMain)" class="glass-overlay"></rect>""")
        sb.append("""<rect x="10" y="10" width="${legendGroupWidth-20}" height="100" rx="15" ry="15"
                      fill="url(#glassHighlight)" class="glass-highlight" opacity="0.4"></rect>""")

        // iOS-style legend title
        sb.append("""<text x="${legendGroupWidth/2}" y="45" text-anchor="middle" font-size="120" 
                      class="legend-title" fill="$fgColor">Legend</text>""")

        // Add a subtle separator line
        sb.append("""<line x1="${legendGroupWidth*0.1}" y1="65" x2="${legendGroupWidth*0.9}" y2="65" 
                      stroke="$fgColor" stroke-width="2" opacity="0.3" />""")

        var start = (legendGroupWidth - totalWidth) / 2 // Center the items within the legend box

        // Add each legend item with more spacing
        legendItems.forEachIndexed { index, (grad, textLen, textColor) ->
            val item = placeMatRequest.config.legend[index]

            // Calculate proper width for the legend item rectangle
            val rectWidth = textLen + 40 // Add more padding around text

            // Enhanced glass-style legend items
            sb.append("""
            <g transform="translate($start,70)" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif" font-size="100" font-weight="600" class="glass-card">
                <rect x="0" y="0" width="$rectWidth" height="150" fill="$grad" rx="16" ry="16" 
                      class="ios-card" filter="url(#glassDropShadow)"></rect>
                <rect x="0" y="0" width="$rectWidth" height="150" fill="url(#${item.legendAsStyle()}GlassOverlay_$id)" rx="16" ry="16" 
                      class="glass-overlay"></rect>
                <rect x="8" y="8" width="${rectWidth-16}" height="50" fill="url(#glassHighlight)" rx="12" ry="12" 
                      class="glass-highlight"></rect>
                <text text-anchor="middle" style="fill: $textColor; letter-spacing: -0.02em;" 
                      x="${rectWidth/2}" y="90">${item.legend}</text>
            </g>
            """.trimIndent())

            // Increased spacing between items
            start += (textLen + 100)
        }

        sb.append("</g>")
        return sb.toString()
    }
}

/**
 * Convert PlaceMatRequest to basic CsvResponse
 */
fun PlaceMatRequest.toCsv(): CsvResponse {
    val headers = listOf("Name", "Legend", "Style")

    val rows = this.placeMats.map { placeMat ->
        listOf(
            placeMat.name,
            placeMat.legend,
            placeMat.style
        )
    }

    return CsvResponse(headers, rows)
}


fun main() {
    val pmRequest = Json.decodeFromString<PlaceMatRequest>("""
{
  "title": "Enterprise Architecture Domains",
  "placeMats": [
    {"name": "Business Strategy", "legend": "Business"},
    {"name": "Business Processes", "legend": "Business"},
    {"name": "Organization Structure", "legend": "Business"},
    {"name": "Application Portfolio", "legend": "Application"},
    {"name": "Application Integration", "legend": "Application"},
    {"name": "User Experience", "legend": "Application"},
    {"name": "Data Models", "legend": "Data"},
    {"name": "Data Governance", "legend": "Data"},
    {"name": "Data Quality", "legend": "Data"},
    {"name": "Infrastructure", "legend": "Technology"},
    {"name": "Security", "legend": "Technology"},
    {"name": "Cloud Strategy", "legend": "Technology"}
  ],
  "config": {
    "legend": [
      {"legend": "Business", "color": "#ff9e00"},
      {"legend": "Application", "color": "#ff5500"},
      {"legend": "Data", "color": "#e10600"},
      {"legend": "Technology", "color": "#8900f2"}
    ]
  }
}        
    """.trimIndent())
    val pmm = PlaceMatMaker(placeMatRequest = pmRequest, isPdf = false)
    val svg = pmm.makePlacerMat()
    val f = File("gen/place.svg")
    f.writeText(svg.shapeSvg)
}
