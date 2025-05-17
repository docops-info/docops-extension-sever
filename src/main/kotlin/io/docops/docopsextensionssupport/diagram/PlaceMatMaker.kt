package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9

import io.docops.docopsextensionssupport.svgsupport.textWidth
import io.docops.docopsextensionssupport.web.ShapeResponse
import java.io.File
import java.util.*

class PlaceMatMaker(val placeMatRequest: PlaceMatRequest, val type: String= "SVG", val isPdf: Boolean = false) {

    private var bgColor = "#fcfcfc"
    private var fgColor = "#111111"
    private val colors = mutableListOf<String>()

    private var useGrad = true

    fun makePlacerMat(): ShapeResponse {
        if(placeMatRequest.useDark) {
            bgColor = "#17242b"
            fgColor = "#fcfcfc"
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

            // Enhanced text styling with better class
            val str = StringBuilder("""<text x="135" y="$textY" text-anchor="middle" class="placemat-text" style="fill:$textColor; font-size:22px;">""")
            var newLine = false

            lines.first.forEachIndexed {
                    j, content ->
                var dy=""
                if(j>0) {
                    dy = "dy=\"24\""
                }
                str.append("""<tspan x="135" $dy>$content</tspan>""")
            }
            str.append("</text>")
                if((i + 1) % 5 == 0) {
                    newLine = true
                }

                // Enhanced rectangle with better styling
                sb.append(
                    """
            <g transform="translate($x,$y)" >
                <rect x="10" y="10" class="placemat-box" width="250" height="90" ry="20" rx="20" 
                      style="fill: ${grad}; stroke: ${placeMatRequest.config.colorFromLegendName(conn.legend).color};
                      stroke-width: $strokeWidth; stroke-opacity: 0.8;"/>
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
        <svg xmlns="http://www.w3.org/2000/svg" width="${(width*scale)/ DISPLAY_RATIO_16_9}" height="${(height*scale)/DISPLAY_RATIO_16_9}"
     viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="diag_$id">
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
            val gradient = SVGColor(item.color, "grad_${item.legendAsStyle()}_$id")
            grad.append(gradient.linearGradient)
        }


        //language=svg
        return """
            <defs>
            $grad
        <filter id="filter">
            <feMorphology in="SourceAlpha" operator="dilate" radius="2" result="OUTLINE"/>
            <feComposite operator="out" in="OUTLINE" in2="SourceAlpha"/>
        </filter>
        <filter id="enhanced-shadow" x="-20%" y="-20%" width="140%" height="140%">
            <feDropShadow dx="2" dy="2" stdDeviation="4" flood-opacity="0.3" />
        </filter>
        <filter id="inner-glow" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur" />
            <feOffset in="blur" dx="0" dy="0" result="offsetBlur" />
            <feFlood flood-color="#ffffff" flood-opacity="0.5" result="glowColor" />
            <feComposite in="glowColor" in2="offsetBlur" operator="in" result="innerGlow" />
            <feComposite in="SourceGraphic" in2="innerGlow" operator="over" />
        </filter>
        <filter id="soft-highlight" x="0" y="0" width="100%" height="100%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="2" result="blur" />
            <feOffset in="blur" dx="0" dy="-2" result="offsetBlur" />
            <feFlood flood-color="#ffffff" flood-opacity="0.3" result="highlightColor" />
            <feComposite in="highlightColor" in2="offsetBlur" operator="in" result="highlight" />
            <feComposite in="SourceGraphic" in2="highlight" operator="over" />
        </filter>
        <style>
            #diag_$id .shadowed {
                filter: url(#enhanced-shadow);
                transition: all 0.3s ease;
            }
            #diag_$id .placemat-box {
                filter: url(#enhanced-shadow);
                transition: transform 0.2s ease;
            }
            #diag_$id .placemat-text {
                font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif;
                font-weight: 600;
                font-variant: small-caps;
                text-rendering: optimizeLegibility;
                fill-opacity: 0.95;
                filter: url(#soft-highlight);
            }
            #diag_$id .title-text {
                font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif;
                font-weight: 700;
                font-variant: small-caps;
                letter-spacing: 0.5px;
                filter: url(#soft-highlight);
            }
            #diag_$id .legend-box {
                filter: url(#enhanced-shadow);
                opacity: 0.95;
            }
            #diag_$id .legend-text {
                font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif;
                font-weight: 600;
                text-rendering: optimizeLegibility;
            }
            #diag_$id .legend-title {
                font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif;
                font-weight: 700;
                letter-spacing: 0.5px;
            }
        </style>
        <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="7" />
        <rect id="bbox" class="placemat-box" width="250" height="90" ry="20" rx="20" />
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
        val itemHeight = 130 // Height of legend items with padding
        val legendHeight = titleHeight + itemHeight + 30 // Additional bottom padding

        // Calculate the scale factor based on the number of legend items
        // Use a smaller scale for many items, larger for few
        val scaleFactor = when {
            placeMatRequest.config.legend.size > 5 -> 0.16
            placeMatRequest.config.legend.size <= 3 -> 0.20
            else -> 0.18
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

        // Center the legend title
        sb.append("""<text x="${legendGroupWidth/2}" y="35" text-anchor="middle" font-size="110" 
                      class="legend-title" fill="$fgColor">Legend</text>""")

        // Add a subtle separator line
        sb.append("""<line x1="${legendGroupWidth*0.1}" y1="50" x2="${legendGroupWidth*0.9}" y2="50" 
                      stroke="$fgColor" stroke-width="2" opacity="0.3" />""")

        var start = (legendGroupWidth - totalWidth) / 2 // Center the items within the legend box

        // Add each legend item with more spacing
        legendItems.forEachIndexed { index, (grad, textLen, textColor) ->
            val item = placeMatRequest.config.legend[index]

            // Calculate proper width for the legend item rectangle
            val rectWidth = textLen + 40 // Add more padding around text

            // Enhanced legend items with modern styling
            sb.append("""
            <g transform="translate($start,70)" font-size="96">
                <rect x="0" y="0" width="$rectWidth" height="110" fill="$grad" rx="15" ry="15" 
                      class="legend-box" filter="url(#enhanced-shadow)"/>
                <text text-anchor="middle" class="legend-text" style="fill: $textColor;" 
                      x="${rectWidth/2}" y="70">${item.legend}</text>
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
    ), title = "Impacted Applications - Internal", fill = true, useDark = true), "SVG")
    val svg = pmm.makePlacerMat()
    val f = File("gen/place.svg")
    f.writeText(svg.shapeSvg)
}
