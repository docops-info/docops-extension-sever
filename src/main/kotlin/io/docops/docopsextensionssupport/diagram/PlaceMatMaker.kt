package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.gradientFromColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9

import io.docops.docopsextensionssupport.svgsupport.textWidth
import io.docops.docopsextensionssupport.web.ShapeResponse
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

                // iOS-style card with no stroke border
                sb.append(
                    """
            <g transform="translate($x,$y)" >
                <rect x="10" y="10" class="ios-card" width="250" height="90" ry="16" rx="16" 
                      style="fill: ${grad}; stroke: none;"/>
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
                    <stop offset="0%" style="stop-color:${gradientColors.first}" stop-opacity="1.0"/>
                    <stop offset="100%" style="stop-color:${gradientColors.second}" stop-opacity="0.9"/>
                </linearGradient>
            """.trimIndent()
            grad.append(simplifiedGradient)
        }


        //language=svg
        return """
            <defs>
            $grad
        <!-- iOS-style shadow filter with two layers -->
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
            #diag_$id .ios-card {
                filter: url(#ios-shadow);
                transition: all 0.2s ease-in-out;
            }

            #diag_$id .ios-card:hover {
                filter: url(#ios-shadow) brightness(1.05);
                transform: translateY(-2px);
            }

            #diag_$id .boxText {
                font-size: 20px;
                font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                font-weight: 600;
                letter-spacing: -0.02em;
            }

            #diag_$id .title-text {
                font-size: 28px;
                font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                font-weight: 700;
                letter-spacing: -0.03em;
            }

            #diag_$id .legend-box {
                filter: url(#ios-shadow);
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
        <rect id="ios-card" class="ios-card" width="250" height="90" ry="16" rx="16" />
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

        // Add a subtle background for the entire legend
        sb.append("""<rect x="0" y="0" width="$legendGroupWidth" height="$legendHeight" rx="20" ry="20" 
                      fill="${if(placeMatRequest.useDark) "#2a3a4a" else "#f5f5f7"}" 
                      opacity="0.4" class="legend-background"/>""")

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

            // iOS-style legend items with 16px border radius
            sb.append("""
            <g transform="translate($start,70)" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif" font-size="100" font-weight="600">
                <rect x="0" y="0" width="$rectWidth" height="150" fill="$grad" rx="16" ry="16" 
                      class="ios-card"/>
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

fun main() {
    val pmm = PlaceMatMaker(PlaceMatRequest(mutableListOf(PlaceMat("SUI", legend = "Vendor"), PlaceMat("Contact View", legend = "Both"),
        PlaceMat("Contact Management", legend = "Company"),
        PlaceMat("CDE Wrapper", legend = "Vendor"),
        PlaceMat("Live Publish", legend = "Vendor"),
        PlaceMat("Policy Quote Search", legend = "Both"),
        PlaceMat("NXT3", legend = "Vendor")
    ),config= PlaceMatConfig(legend = mutableListOf(
        ColorLegendConfig("#c9d7e4","Company"),
        ColorLegendConfig("#F34F1C","Vendor"),
        ColorLegendConfig("#01A6F0", "Both"))
    ), title = "Impacted Applications - Internal", fill = true, useDark = false), "SVG")
    val svg = pmm.makePlacerMat()
    val f = File("gen/place.svg")
    f.writeText(svg.shapeSvg)
}
