package io.docops.docopsextensionssupport.diagram.connector

import io.docops.docopsextensionssupport.diagram.gradientMapToHsl
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
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
class ConnectorMaker(val connectors: MutableList<Connector>, val useDark: Boolean = false, val type: String, var useGlassEffect: Boolean = true, val isPdf : Boolean = false, val themeName: String = "classic") {
    private val alphabets = ('A'..'Z') + ('a'..'z') + ('0'..'9').toMutableList()
    private val colors = mutableListOf<String>()
    private var useGrad = true

    private var bgColor = "#F8F9FA"
    private var fill = ""
    private val baseColors = mutableListOf("#E14D2A", "#82CD47", "#687EFF", "#C02739", "#FEC260", "#e9d3ff", "#7fc0b7")

    private var theme: DocOpsTheme = ThemeFactory.getThemeByName(themeName, useDark)
    private val isPremium = theme.name.contains("Premium", ignoreCase = true)
    private val horizontalStep = if (isPremium) 352 else 340
    private val verticalStep = if (isPremium) 160 else 150


    fun makeConnectorImage(scale: Float = 1.0f): ShapeResponse {
        bgColor = theme.canvas
        if(useDark) {
            bgColor = "#111827"
        }
        if(isPdf) {
            useGlassEffect = false
        }
        val sb = StringBuilder()

        // Brutalist specific dimensions: 340 horizontal step, 150 vertical step
        // Premium dimensions: 352 horizontal step, 160 vertical step
        val columns = 5
        val maxCols = if (connectors.size < columns) connectors.size else columns
        val width: Float = (maxCols * horizontalStep).toFloat() + 200
        val bodyHeight = (Math.ceil(connectors.size / columns.toDouble()).toInt() * verticalStep).toFloat()
        val descriptionHeight = (connectors.size * 32) + 100

        val totalHeight = bodyHeight + descriptionHeight
        val id = UUID.randomUUID().toString()

        sb.append(head(totalHeight, width = width, scale, id))
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
        if (!isPdf) {
            sb.append("<rect width=\"100%\" height=\"100%\" fill=\"url(#dotPattern)\" />")
        }
        sb.append("<g transform=\"translate(100,40)\">")
        sb.append(makeBody())
        sb.append("</g>")

        // Start descriptions after the body height
        sb.append(descriptions(bodyHeight + 60))
        sb.append(tail())
        return ShapeResponse(shapeSvg = sb.toString(), height = totalHeight, width = width)
    }


    private fun descriptions(start: Float): String {
        val sb = StringBuilder("<g transform='translate(100,${start})'>")
        var y = 0
        val textColor = theme.primaryText


        connectors.forEachIndexed { i, item ->
            // Skip if the description is empty (e.g., the last element 'L' in your example)
            if (item.description.trim().isEmpty()) return@forEachIndexed

            val boxColor = colors[i]
            val animationDelay = (connectors.size + i) * 0.05

            sb.append("""<g transform="translate(0, $y)">""")
            sb.append("""
                    <g style="animation: slideIn 0.4s ease-out ${animationDelay}s both;">
                        <rect x="0" y="0" width="24" height="24" fill="#000000" ${if (isPremium) "rx=\"16\"" else ""} />
                        <rect x="2" y="2" width="20" height="20" fill="$boxColor" ${if (isPremium) "rx=\"16\"" else ""} />
                        <text x="12" y="17" fill="white" text-anchor="middle" style="font-family: ${theme.fontFamily}; font-size: 12px; font-weight: ${if (isPremium) "600" else "800"};">${alphabets[i]}</text>
                        <text x="36" y="17" fill="$textColor" style="font-family: ${theme.fontFamily}; font-size: 14px; font-weight: 600; ${if (!isPremium) "text-transform: uppercase;" else ""} letter-spacing: 0.03em;">${item.description}</text>
                        <line x1="0" y1="28" x2="400" y2="28" stroke="$textColor" stroke-width="1" stroke-opacity="0.1" />
                    </g>
                """.trimIndent())
            sb.append("</g>")
            y += if (isPremium) 40 else 36
        }
        sb.append("</g>")
        return sb.toString()
    }

    private fun head(height: Float, width: Float, scale: Float = 1.0f, id: String)  = """
        <svg xmlns="http://www.w3.org/2000/svg" width="${(width*scale) / DISPLAY_RATIO_16_9}" height="${(height*scale) /DISPLAY_RATIO_16_9}" viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="diag_$id" preserveAspectRatio="xMidYMid meet">
    """.trimIndent()

    private fun tail() = "</svg>"

    private fun initColors() {
        connectors.forEachIndexed { i, connector ->
            var choiceColor = ""
            if (connector.baseColor != null && connector.baseColor.startsWith("#")) {
                choiceColor = connector.baseColor
            } else {
                choiceColor = if (i > baseColors.size - 1) {
                    getRandomColorHex()
                } else {
                    baseColors[i]
                }
            }
            colors.add(choiceColor)
        }
    }
    private fun defs(id: String) : String {
        val grad= StringBuilder()
        val connectorColor = theme.accentColor
        val connectorOpacity = if(useDark) "0.6" else "1.0"


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

        val shadowFilter = if (isPremium) {
            """
                <filter id="premiumShadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="12"/>
                    <feOffset dx="0" dy="4" result="offsetblur"/>
                    <feComponentTransfer>
                        <feFuncA type="linear" slope="0.1"/>
                    </feComponentTransfer>
                    <feMerge>
                        <feMergeNode/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>
            """
        } else {
            """
                <filter id="brutalistShadow" x="-20%" y="-20%" width="150%" height="150%">
                    <feOffset dx="6" dy="6" in="SourceAlpha" result="offset" />
                    <feFlood flood-color="#000000" flood-opacity="0.8" result="color" />
                    <feComposite in="color" in2="offset" operator="in" result="shadow" />
                    <feMerge>
                        <feMergeNode in="shadow" />
                        <feMergeNode in="SourceGraphic" />
                    </feMerge>
                </filter>
            """
        }

        val styles = """
                <style>
                    ${theme.fontImport}
                
                    #diag_$id .module-card {
                        transition: transform 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                        cursor: pointer;
                    }

                    #diag_$id .module-card:hover {
                        transform: translate(-2px, -2px);
                    }
                    
                    #diag_$id .accent-bar {
                        transition: width 0.3s ease;
                    }

                    @keyframes slideIn {
                        from { transform: translateX(-30px); opacity: 0; }
                        to { transform: translateX(0); opacity: 1; }
                    }
                </style>
            """

        return """
                <defs>
                <pattern id="dotPattern" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                    <circle cx="2" cy="2" r="1" fill="${if (isPremium) "#64748b" else if(useDark) "#374151" else "#E5E7EB"}" fill-opacity="${if (isPremium) "0.1" else "1.0"}"/>
                </pattern>
                <linearGradient id="glassGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="${theme.canvas}" stop-opacity="0.9"/>
                    <stop offset="100%" stop-color="${theme.canvas}" stop-opacity="0.7"/>
                </linearGradient>
                $grad
                $shadowFilter
                $styles
                <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="2" />
                </defs>
            """.trimIndent()
    }

    private fun makeBody(): String {
        val sb = StringBuilder()
        var x = 0
        var y = 0
        val connectorColor = theme.accentColor
        val textColor = theme.primaryText
        val shadowId = if (isPremium) "premiumShadow" else "brutalistShadow"
        val strokeWidth = if (isPremium) 1 else 3
        val cardStroke = if (isPremium) theme.accentColor else "#000000"
        val cardStrokeOpacity = if (isPremium) "0.1" else "1.0"

        connectors.forEachIndexed { i, conn ->
            val boxColor = colors[i]
            val animationDelay = i * 0.08

            val lines = conn.textToLines()
            val textContent = StringBuilder("""<text x="25" y="48" fill="$textColor" style="font-family: ${theme.fontFamily}; font-size: 16px; font-weight: ${if (isPremium) "700" else "800"}; ${if (!isPremium) "text-transform: uppercase;" else ""} letter-spacing: 0.02em;">""")
            lines.forEachIndexed { j, content ->
                val dy = if (j > 0) "20" else "0"
                textContent.append("""<tspan x="25" dy="$dy">$content</tspan>""")
            }
            textContent.append("</text>")

            // Positional group (handles grid layout)
            sb.append("""<g transform="translate($x,$y)">""")

            // Animation group (handles entry effect separately to avoid coordinate overlap)
            sb.append("""
                <g class="module-card" style="animation: slideIn 0.5s ease-out ${animationDelay}s both;">
                    <!-- The "Module" Base -->
                    <rect x="0" y="0" width="260" height="100" rx="${theme.cornerRadius}" fill="${if (isPremium) "url(#glassGradient)" else theme.canvas}" stroke="$cardStroke" stroke-width="$strokeWidth" stroke-opacity="$cardStrokeOpacity" filter="url(#$shadowId)" />
        
                    <!-- Color Header/Accent Area -->
                    <rect x="0" y="0" width="260" height="12" rx="${theme.cornerRadius}" fill="$boxColor" stroke="$cardStroke" stroke-width="$strokeWidth" stroke-opacity="$cardStrokeOpacity" />
                    ${if (isPremium) """<rect x="0" y="6" width="260" height="6" fill="$boxColor" />""" else ""}
        
                    <!-- Decorative corner notch -->
                    ${if (!isPremium) """<path d="M 240 100 L 260 80 L 260 100 Z" fill="#000000" />""" else ""}
        
                    <!-- Identifier Tab / Badge -->
                    <rect x="${if (isPremium) 230 else 220}" y="${if (isPremium) -12 else 12}" width="40" height="${if (isPremium) 24 else 25}" rx="${if (isPremium) 12 else 0}" fill="${if (isPremium) theme.accentColor else "#000000"}" />
                    <text x="${if (isPremium) 250 else 240}" y="${if (isPremium) 5 else 30}" fill="white" text-anchor="middle" style="font-family: ${theme.fontFamily}; font-size: 14px; font-weight: 800;">${alphabets[i]}</text>
   
                    $textContent
            
                    <!-- Interactive Hover Accent Bar -->
                    <rect x="0" y="94" width="0" height="6" fill="#000000" class="accent-bar" />
                """.trimIndent())

            // Connection logic
            if (i < connectors.lastIndex) {
                if ((i + 1) % 5 == 0) {
                    // Row-wrapping connector
                    val wrapGap = horizontalStep - 260
                    val wrapDown = verticalStep / 2.0
                    val arrowHeadX = -(4 * horizontalStep + 260)
                    val wrapBack = arrowHeadX - 40
                    val arrowX = arrowHeadX - 5
                    
                    sb.append("""
                             <g transform="translate(260,50)">
                                    <path d="M0,0 L${wrapGap/2.0},0" stroke-width="$strokeWidth" stroke="$connectorColor" fill="none"/>
                                    <line x1="${wrapGap/2.0}" x2="${wrapGap/2.0}" y1="0" y2="$wrapDown" stroke-width="$strokeWidth" stroke="$connectorColor" stroke-linecap="square"/>
                                    <line x1="${wrapGap/2.0}" x2="$wrapBack" y1="$wrapDown" y2="$wrapDown" stroke-width="$strokeWidth" stroke="$connectorColor" stroke-linecap="square"/>
                                    <line x1="$wrapBack" x2="$wrapBack" y1="$verticalStep" y2="$wrapDown" stroke-width="$strokeWidth" stroke="$connectorColor" stroke-linecap="square"/>
                                    <line x1="$wrapBack" x2="$arrowHeadX" y1="$verticalStep" y2="$verticalStep" stroke-width="$strokeWidth" stroke="$connectorColor" stroke-linecap="square"/>
                                    <g transform="translate($arrowX,${verticalStep - 2.5})">
                                        <use xlink:href="#ppoint" fill="$connectorColor" stroke="$connectorColor"/>
                                    </g>
                                </g>
                        """.trimIndent())
                } else {
                    // Normal horizontal connector
                    val lineEnd = horizontalStep - 260 - 3
                    val arrowX = lineEnd - 5
                    sb.append("""
                            <line x1="260" y1="50" x2="${260 + lineEnd}" y2="50" stroke="$connectorColor" stroke-width="$strokeWidth" />
                            <g transform="translate(${260 + arrowX},47.5)">
                                <use xlink:href="#ppoint" fill="$connectorColor" stroke="$connectorColor"/>
                            </g>
                        """.trimIndent())
                }
            }
            // Close both groups
            sb.append("</g></g>")

            // Updated grid spacing
            if ((i + 1) % 5 == 0) {
                x = 0
                y += verticalStep
            } else {
                x += horizontalStep
            }
        }
        return sb.toString()
    }

}

fun main() {
    val connectors = mutableListOf<Connector>()
    connectors.add(Connector("Developer", description = "Writes unit tests"))
    connectors.add(Connector("Unit Tests", description = "Unit tests produces excel"))
    connectors.add(Connector("Microsoft Excel", description = "Excel is stored in test engine"))
    connectors.add(Connector("Test Engine", description = "Test Engine write documentation"))
    connectors.add(Connector("API Documentation Output", description = "Documentation is committed"))
    connectors.add(Connector("GitHub", description = "Triggers a webhook"))
    connectors.add(Connector("Developer", description = "Developer consumes git content"))
    connectors.add(Connector("Unit Tests", description = "Unit tests produces excel"))
    connectors.add(Connector("Microsoft Excel", description = "Excel is stored in test engine"))
    connectors.add(Connector("Test Engine", description = "Test Engine write documentation"))
    connectors.add(Connector("API Documentation Output", description = "Documentation is committed"))
    connectors.add(Connector("GitHub", ""))
    val maker = ConnectorMaker(connectors, true, "SVG")
    val svg = maker.makeConnectorImage()
    File("gen/connector.svg").writeText(svg.shapeSvg)
}
