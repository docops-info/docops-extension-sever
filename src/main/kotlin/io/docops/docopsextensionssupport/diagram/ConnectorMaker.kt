package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.getRandomColorHex
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.web.ShapeResponse
import java.io.File
import java.util.*


/**
 * Represents a ConnectorMaker that creates connector images.
 *
 * @property connectors A list of connectors to be included in the image.
 * @property useDark Indicates whether to use a dark background.
 * @property alphabets A list of alphabets from A to Z.
 * @property colors A list of colors for the connectors.
 * @property bgColor The background color of the image.
 * @property baseColors A list of base colors for the connectors.
 */
class ConnectorMaker(val connectors: MutableList<Connector>, val useDark: Boolean = false, val type: String) {
    private val alphabets = ('A'..'Z') + ('a'..'z') + ('0'..'9').toMutableList()
    private val colors = mutableListOf<String>()
    private var useGrad = true

    private var bgColor = "#F8F9FA"
    private var fill = ""
    private val baseColors = mutableListOf("#E14D2A", "#82CD47", "#687EFF", "#C02739", "#FEC260", "#e9d3ff", "#7fc0b7")
    fun makeConnectorImage(scale: Float = 1.0f): ShapeResponse {
        if(useDark) {
            bgColor = "#17242b"
        }
        val sb = StringBuilder()
        val width: Float = (connectors.chunked(5)[0].size * 250).toFloat() + (connectors.chunked(5)[0].size * 46).toFloat() + 200
        val height = connectors.chunked(5).size * 110.0f
        val descriptionHeight = (connectors.size * 26) + 140
        val id = UUID.randomUUID().toString()
        sb.append(head(height + descriptionHeight, width = width, scale, id))
        initColors()
        if("PDF" != type) {
            sb.append(defs(id))
        } else {
            useGrad = false
            sb.append("""
       <defs>
        <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="2" />
        <rect id="bbox" class="shadowed"  width="250" height="90" ry="16" rx="16"  />
        <path id="hconnector" d="M260,50.0 h34" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <path id="vconnector" d="M135,100 v34" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
       </defs>
            """.trimIndent())
            sb.append("<rect width=\"100%\" height=\"100%\" fill=\"none\"/>")

        }
        sb.append("<rect width=\"100%\" height=\"100%\" fill=\"$bgColor\"/>")
        sb.append("<g transform=\"translate(100,0)\">")
        sb.append(makeBody())
        sb.append("</g>")
        sb.append(descriptions(height))
        sb.append(tail())
        return ShapeResponse(shapeSvg = joinXmlLines(sb.toString()), height = height, width = width)
    }
    private fun joinXmlLines(str: String): String {
        val sb = StringBuilder()
        var previousLine = ""

        str.lines().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) {
                return@forEach
            }

            // Add a space if the previous line ends with a quote and the current line starts with an attribute
            if (previousLine.endsWith("\"") && 
                (trimmedLine.startsWith("style=") || 
                 trimmedLine.matches(Regex("^[a-zA-Z-]+=.*")))) {
                sb.append(" ")
            }

            // If the previous line doesn't end with a tag closing character, quote, or space,
            // and the current line doesn't start with a tag opening character, quote, or space,
            // then add a space to prevent content from running together
            else if (previousLine.isNotEmpty() && 
                    !previousLine.endsWith(">") && 
                    !previousLine.endsWith("\"") && 
                    !previousLine.endsWith("'") && 
                    !previousLine.endsWith(" ") &&
                    trimmedLine.isNotEmpty() && 
                    !trimmedLine.startsWith("<") && 
                    !trimmedLine.startsWith("\"") && 
                    !trimmedLine.startsWith("'") && 
                    !trimmedLine.startsWith(" ")) {
                sb.append(" ")
            }

            sb.append(trimmedLine)
            previousLine = trimmedLine
        }

        // Fix any remaining attribute issues by ensuring there's a space between quotes and attributes
        return sb.toString().replace("\"style=", "\" style=")
            .replace("\"class=", "\" class=")
            .replace("\"id=", "\" id=")
            .replace("\"width=", "\" width=")
            .replace("\"height=", "\" height=")
            .replace("\"x=", "\" x=")
            .replace("\"y=", "\" y=")
            .replace("\"rx=", "\" rx=")
            .replace("\"ry=", "\" ry=")
            .replace("\"fill=", "\" fill=")
            .replace("\"stroke=", "\" stroke=")
            .replace("\"d=", "\" d=")
            .replace("\"transform=", "\" transform=")
            .replace("\"viewBox=", "\" viewBox=")
            .replace("\"xmlns=", "\" xmlns=")
    }

    private fun descriptions(start: Float): String {
        val sb = StringBuilder("<g transform='translate(100,$start)'>")
        var y = 0
        val textColor = "#374151"
        var stroke = "rgba(0,0,0,0.08)"
        if(useDark){
            stroke = "#fcfcfc"
        }

        connectors.forEachIndexed {
            i, item ->
            var fill = "fill=\"url(#grad$i)\""
            if("PDF" == type) {
                fill = "fill=\"${colors[i]}\""
            }
            sb.append("""
                <g transform="translate(0,$y)">
                    <rect x="0" y="0" height="20" width="20" $fill rx="6" ry="6"/>
                    <text x="10" y="13" fill="white" text-anchor="middle" style="font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 12px; font-weight: 700; letter-spacing: 0.02em;">${alphabets[i]}</text>
                    <text x="32" y="14" fill="$textColor" text-anchor="start" style="font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 14px; font-weight: 400; letter-spacing: -0.01em;">
                        ${item.description}
                    </text>
                </g>
            """.trimIndent())
            y += 32
        }
        sb.append("</g>")
        return sb.toString()
    }
    private fun head(height: Float, width: Float, scale: Float = 1.0f, id: String)  = """
        <svg xmlns="http://www.w3.org/2000/svg" width="${(width*scale) / DISPLAY_RATIO_16_9}" height="${(height*scale) /DISPLAY_RATIO_16_9}" viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="diag_$id" preserveAspectRatio="xMidYMid meet">
    """.trimIndent()

    private fun tail() = "</svg>"

    private fun initColors() {
        for (i in 0 until connectors.size) {
            var choiceColor = ""
            if(i > baseColors.size - 1) {
                choiceColor = getRandomColorHex()
            } else {
                choiceColor = baseColors[i]
            }
            colors.add(choiceColor)
        }
    }
    private fun defs(id: String) : String {
        val grad= StringBuilder()

        colors.forEachIndexed {
            i, choiceColor ->
           val res = gradientMapToHsl()[choiceColor]
            if(null == res) {
                val gradient = SVGColor(choiceColor, "grad${i}")
                grad.append(gradient.linearGradient)
            } else {
                grad.append(res)
            }
        }

        return """
            <defs>
            $grad
        <filter id="cardShadow" x="-50%" y="-50%" width="200%" height="200%">
            <feDropShadow dx="0" dy="2" stdDeviation="8" flood-color="rgba(0,0,0,0.1)"/>
            <feDropShadow dx="0" dy="4" stdDeviation="16" flood-color="rgba(0,0,0,0.06)"/>
        </filter>
        <style>
            #diag_$id .shadowed {
                filter: url(#cardShadow);
            }
        </style>
        <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="2" />
        <rect id="bbox" class="shadowed"  width="250" height="90" ry="16" rx="16"  />
        <path id="hconnector" d="M260,50.0 h34" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <path id="vconnector" d="M135,100 v34" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </defs>
        """.trimIndent()
    }
    private fun makeBody(): String {
        val sb = StringBuilder()
        var x = 0
        var y = 0
        connectors.forEachIndexed {
                i, conn ->
            var grad = "url(#grad$i)"
            var strokeWidth = 2
            fill = "white"
            var style = ""
            if(!useGrad) {
                fill = "none"
                strokeWidth = 5
                grad = colors[i]
                style = """style="font-size: 24px; font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif; font-variant: small-caps; font-weight: bold;""""
            }
            val lines= conn.textToLines()
            val str = StringBuilder("""<text x="135" y="${conn.start}" text-anchor="middle" fill="#1F2937" style="font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 18px; font-weight: 600; letter-spacing: -0.02em;" $style> """)
            var newLine = false

            lines.forEachIndexed {
                    j, content ->
                var dy=""
                if(j>0) {
                    dy = "dy=\"20\""
                }
                str.append("""<tspan x="135" $dy>$content</tspan>""")
            }
            str.append("</text>")

            if(i == connectors.lastIndex) {
                // Last connector - no connections
                //language=svg
                sb.append("""
         <g transform="translate($x,$y)" >
            <use xlink:href="#bbox" x="10" y="10" fill="$fill" stroke="rgba(0,0,0,0.08)" stroke-width='1'/>
            $str
            <rect x="270" y="13" height="20" width="20" fill="$grad" rx="6" ry="6"/>
            <text x="280" y="26" fill="white" text-anchor="middle" style="font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 12px; font-weight: 700; letter-spacing: 0.02em;">${alphabets[i]}</text>
        </g>
            """.trimIndent())
            }
            else {
                if((i + 1) % 5 == 0) {
                    // End of row - check if there's a next row
                    val hasNextRow = i < connectors.size - 1

                    //language=svg
                    sb.append("""
         <g transform="translate($x,$y)" >
            <use xlink:href="#bbox" x="10" y="10" fill="$fill" stroke="rgba(0,0,0,0.08)" stroke-width='1'/>
            $str
            <rect x="270" y="13" height="20" width="20" fill="$grad" rx="6" ry="6"/>
            <text x="280" y="26" fill="white" text-anchor="middle" style="font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 12px; font-weight: 700; letter-spacing: 0.02em;">${alphabets[i]}</text>
            ${if (hasNextRow) """
            <!-- Row-wrapping connector -->
            <g transform="translate(260,50)">
                <path d="M0,0 L60,0" stroke-width="2" stroke="rgba(107,114,128,0.4)"/>
                <line x1="60" x2="60" y1="0" y2="60" stroke-width="2" stroke="rgba(107,114,128,0.4)" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="60" x2="-1480" y1="60" y2="60" stroke-width="2" stroke="rgba(107,114,128,0.4)" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="-1480" x2="-1480" y1="110" y2="60" stroke-width="2" stroke="rgba(107,114,128,0.4)" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="-1480" x2="-1460" y1="110" y2="110" stroke-width="2" stroke="rgba(107,114,128,0.4)" stroke-linecap="round" stroke-linejoin="round"/>
                <g transform="translate(-1460,107)">
                    <use xlink:href="#ppoint" fill="url(#grad${i+1})" stroke="url(#grad${i+1})"/>
                </g>
            </g>
            """ else ""}
        </g>
                """.trimIndent())
                    x = 0
                    y += 110
                } else {
                    // Middle of row - normal horizontal connector
                    //language=svg
                    sb.append("""
         <g transform="translate($x,$y)" >
            <use xlink:href="#bbox" x="10" y="10" fill="$fill" stroke="rgba(0,0,0,0.08)" stroke-width='1'/>
            $str
            <use xlink:href="#hconnector" stroke="rgba(107,114,128,0.4)" fill="rgba(107,114,128,0.4)"/>
            <g transform="translate(297,47)">
                <use xlink:href="#ppoint" fill="$grad" stroke="$grad"/>
            </g>
            <rect x="270" y="13" height="20" width="20" fill="$grad" rx="6" ry="6"/>
            <text x="280" y="26" fill="white" text-anchor="middle" style="font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 12px; font-weight: 700; letter-spacing: 0.02em;">${alphabets[i]}</text>
        </g>
                """.trimIndent())
                    x += 300
                }
            }
        }
        return sb.toString()
    }

}

fun main() {
    val connectors = mutableListOf<Connector>()
    connectors.add(Connector("Developer", description = "Writes unit tests"))
    connectors.add(Connector("Unit Tests", description ="Unit tests produces excel"))
    connectors.add(Connector("Microsoft Excel", description ="Excel is stored in test engine"))
    connectors.add(Connector("Test Engine", description ="Test Engine write documentation"))
    connectors.add(Connector("API Documentation Output", description ="Documentation is committed"))
    connectors.add(Connector("GitHub", description ="Triggers a webhook"))
    connectors.add(Connector("Developer", description ="Developer consumes git content"))
    connectors.add(Connector("Unit Tests", description ="Unit tests produces excel"))
    connectors.add(Connector("Microsoft Excel", description ="Excel is stored in test engine"))
    connectors.add(Connector("Test Engine", description ="Test Engine write documentation"))
    connectors.add(Connector("API Documentation Output", description ="Documentation is committed"))
    connectors.add(Connector("GitHub", ""))
    val maker = ConnectorMaker(connectors, false, "SVG")
    val svg = maker.makeConnectorImage()
    File("gen/connector.svg").writeText(svg.shapeSvg)
}
