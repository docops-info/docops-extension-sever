package io.docops.docopsextensionssupport.diagram.placemat

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.ShapeResponse
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

class PlaceMatMaker(val placeMatRequest: PlaceMatRequest, val type: String= "SVG", val isPdf: Boolean = false) {

    private var theme: DocOpsTheme = ThemeFactory.getTheme(placeMatRequest.useDark)
    private val colors = mutableListOf<String>()

    private var useGrad = true

    fun makePlacerMat(): ShapeResponse {
        theme = ThemeFactory.getTheme(placeMatRequest)

        val cardWidth = 340
        val verticalSpacing = 130
        val columns = 2

        // Dynamic width and height based on items
        val width = (columns * cardWidth + 120).toFloat()
        val numRows = Math.ceil(placeMatRequest.placeMats.size / 2.0).toInt()
        val bodyHeight = numRows * verticalSpacing
        val totalHeight = bodyHeight + 250f // Padding for title and legend

        val sb = StringBuilder()
        val id = UUID.randomUUID().toString()
        sb.append(head(totalHeight, width = width, placeMatRequest.scale, id))
        initColors()

        sb.append(defs(id))

        sb.append("""<rect width="100%" height="100%" fill="${theme.canvas}"/>""")
        if (!isPdf) {
            sb.append("""<rect width="100%" height="100%" fill="url(#blueprint_$id)"/>""")
        }

        sb.append("<g class=\"placemat-root\">")

        // Title Section using theme
        sb.append("""
            <g transform="translate(40,60)">
                <text font-family="${if(isPdf) "Helvetica" else theme.fontFamily}" font-weight="700" font-size="36" fill="${theme.primaryText}" style="letter-spacing: -0.05em;">${placeMatRequest.title.uppercase()}</text>
                <rect x="0" y="12" width="120" height="4" fill="${theme.accentColor}" rx="2" />
            </g>
        """.trimIndent())

        // Body Section
        sb.append("<g transform=\"translate(0,120)\">")
        sb.append(makeBody(id))
        sb.append("</g>")

        // Legend Section - Positioned below the body
        val legendY = 120 + bodyHeight + 40
        sb.append(makeLegend(legendY.toFloat(), id, width))

        sb.append("</g>")
        sb.append(tail())
        return ShapeResponse(sb.toString(), height = totalHeight, width = width)
    }

    private fun makeBody(id: String): String {
        val sb = StringBuilder()
        val cardWidth = 340
        val verticalSpacing = 130 // Increased to ensure no vertical overlap

        placeMatRequest.placeMats.forEachIndexed { i, conn ->
            val column = i % 2
            val row = i / 2

            // Stagger logic:
            // column 0 is at x=40
            // column 1 is at x=380
            // Every second row (row 1, 3, 5...) gets an extra 40px x-offset
            val x = 40 + (column * cardWidth) + (if (row % 2 == 1) 40 else 0)
            val y = row * verticalSpacing

            val accentColor = placeMatRequest.config.colorFromLegendName(conn.legend).color

            val cardFill = theme.canvas
            val secondaryText = theme.secondaryText
            val fontMain = if (isPdf) "Helvetica" else theme.fontFamily

            sb.append("""
                    <g transform="translate($x,$y)">
                        <g class="tech-card" style="animation-delay: ${0.1 * i}s;">
                            <rect width="300" height="110" fill="$cardFill" fill-opacity="0.8" stroke="${if(isPdf) accentColor else theme.accentColor}" stroke-width="${if(isPdf) 2 else 1}" stroke-opacity="0.3" />
                            <rect x="0" y="0" width="100" height="20" fill="$accentColor" />
                            <text x="8" y="14" font-family="$fontMain" font-weight="700" font-size="10" fill="#FFFFFF" style="text-transform: uppercase; letter-spacing: 0.1em;">${conn.legend}</text>
                
                            <text x="20" y="55" font-family="$fontMain" font-weight="700" font-size="18" fill="${theme.primaryText}">${conn.name}</text>
                            <text x="20" y="80" font-family="$fontMain" font-size="13" fill="$secondaryText" opacity="0.8">System Component</text>
                
                            <path d="M 270 95 L 290 95 L 290 75" fill="none" stroke="$accentColor" stroke-width="1.5" opacity="0.5" />
                        </g>
                    </g>
                    """.trimIndent())
        }
        return sb.toString()
    }


    private fun head(height: Float, width: Float, scale: Float = 1.0f, id: String)  = """
        <svg xmlns="http://www.w3.org/2000/svg" width="${width * scale}" height="${height * scale}"
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
        val gridColor = theme.accentColor

        return """
                    <defs>
                        ${theme.fontImport}
                        <pattern id="blueprint_$id" width="60" height="60" patternUnits="userSpaceOnUse">
                            <rect width="60" height="60" fill="none" stroke="$gridColor" stroke-width="0.5" stroke-opacity="0.1" />
                            <circle cx="0" cy="0" r="1" fill="$gridColor" fill-opacity="0.2" />
                        </pattern>
                        <style>
                            #diag_$id .placemat-root { font-family: ${theme.fontFamily}; }
                        
                            /* Animation applied ONLY to the inner tech-card group */
                            #diag_$id .tech-card { 
                                animation: reveal_$id 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
                                transition: transform 0.2s ease-out;
                                transform-box: fill-box;
                                transform-origin: center;
                            }
                        
                            @keyframes reveal_$id {
                                from { opacity: 0; transform: translateY(10px); }
                                to { opacity: 1; transform: translateY(0); }
                            }
                        
                            #diag_$id .tech-card:hover {
                                transform: translate(4px, -4px);
                            }
                        </style>
                    </defs>
                """.trimIndent()
    }


    private fun makeLegend(y: Float, id: String, svgWidth: Float): String {
        val legendItems = placeMatRequest.config.legend
        if (legendItems.isEmpty()) return ""

        val fontMain = if (isPdf) "Helvetica" else theme.fontFamily
        val itemWidth = 140
        val itemHeight = 30
        val spacing = 20

        val totalWidth = (legendItems.size * itemWidth) + ((legendItems.size - 1) * spacing)
        var startX = (svgWidth - totalWidth) / 2

        val sb = StringBuilder()
        sb.append("""<g transform="translate(0, $y)" class="placemat-root">""")

        // Section Label
        sb.append("""
            <text x="${svgWidth / 2}" y="-15" text-anchor="middle" font-family="$fontMain" font-weight="700" font-size="12" fill="${theme.primaryText}" style="text-transform: uppercase; letter-spacing: 0.2em; opacity: 0.5;">Legend</text>
            <line x1="${(svgWidth / 2) - 40}" y1="-5" x2="${(svgWidth / 2) + 40}" y2="-5" stroke="${theme.accentColor}" stroke-width="1" opacity="0.2" />
        """.trimIndent())

        legendItems.forEach { item ->
            val color = item.color

            sb.append("""
                <g transform="translate($startX, 10)">
                    <rect width="$itemWidth" height="$itemHeight" fill="${theme.canvas}" stroke="${theme.accentColor}" stroke-width="1" stroke-opacity="0.2" />
                    <rect width="6" height="$itemHeight" fill="$color" />
                    <text x="15" y="20" font-family="$fontMain" font-weight="700" font-size="11" fill="${theme.primaryText}" style="letter-spacing: 0.05em;">${item.legend.uppercase()}</text>
                </g>
            """.trimIndent())

            startX += itemWidth + spacing
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
