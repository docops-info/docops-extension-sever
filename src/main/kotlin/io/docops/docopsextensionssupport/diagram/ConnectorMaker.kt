package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.determineTextColor
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
class ConnectorMaker(val connectors: MutableList<Connector>, val useDark: Boolean = false, val type: String, var useGlassEffect: Boolean = true, val isPdf : Boolean = false) {
    private val alphabets = ('A'..'Z') + ('a'..'z') + ('0'..'9').toMutableList()
    private val colors = mutableListOf<String>()
    private var useGrad = true

    private var bgColor = "#F8F9FA"
    private var fill = ""
    private val baseColors = mutableListOf("#E14D2A", "#82CD47", "#687EFF", "#C02739", "#FEC260", "#e9d3ff", "#7fc0b7")
    fun makeConnectorImage(scale: Float = 1.0f): ShapeResponse {
        if(useDark) {
            bgColor = "#111827"
        }
        if(isPdf) {
            useGlassEffect = false
        }
        val sb = StringBuilder()
        val width: Float = (connectors.chunked(5)[0].size * 250).toFloat() + (connectors.chunked(5)[0].size * 46).toFloat() + 200
        val height = connectors.chunked(5).size * 120.0f
        val descriptionHeight = (connectors.size * 36) + 160
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
        if (!isPdf) {
            sb.append("<rect width=\"100%\" height=\"100%\" fill=\"url(#dotPattern)\" />")
        }
        sb.append("<g transform=\"translate(100,20)\">")
        sb.append(makeBody())
        sb.append("</g>")
        sb.append(descriptions(height + 20))
        sb.append(tail())
        return ShapeResponse(shapeSvg = sb.toString(), height = height, width = width)
    }


    private fun descriptions(start: Float): String {
        val sb = StringBuilder("<g transform='translate(100,${start + 20})'>")
        var y = 0
        var textColor = "#374151"
        if(useDark){
            textColor = "#F3F4F6"
        }

        connectors.forEachIndexed {
            i, item ->
            var fill = "fill=\"url(#grad$i)\""
            if("PDF" == type) {
                fill = "fill=\"${colors[i]}\""
            }
            val animationDelay = (connectors.size + i) * 0.05
            val labelColor = determineTextColor(colors[i])
            sb.append("""
                <g transform="translate(0, $y)">
                    <g class="glass-card" style="animation-delay: ${animationDelay}s">
                        <rect x="0" y="0" width="20" height="20" $fill rx="4" filter="url(#cardShadow)"/>
                        <text x="10" y="14" fill="$labelColor" text-anchor="middle" style="font-family: 'Outfit', sans-serif; font-size: 10px; font-weight: 700;">${alphabets[i]}</text>
                        <text x="35" y="15" fill="$textColor" style="font-family: 'Outfit', sans-serif; font-size: 14px; font-weight: 500;">${item.description}</text>
                    </g>
                </g>
            """.trimIndent())
            y += 36
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

        val glassEffectDefs = if (useGlassEffect) {
            """
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

            <!-- Enhanced drop shadow filter for glass boxes -->
            <filter id="glassDropShadow" x="-30%" y="-30%" width="160%" height="160%">
                <feDropShadow dx="0" dy="8" stdDeviation="12" flood-color="rgba(0,0,0,0.25)"/>
            </filter>

            <!-- Frosted glass blur filter -->
            <filter id="glassBlur" x="-10%" y="-10%" width="120%" height="120%">
                <feGaussianBlur in="SourceGraphic" stdDeviation="2" />
            </filter>

            <!-- Glass border gradient -->
            <linearGradient id="glassBorder" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.8);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0.2);stop-opacity:1" />
            </linearGradient>
            """
        } else {
            ""
        }

        val shadowFilter = if (useGlassEffect) {
            """
            <filter id="cardShadow" x="-50%" y="-50%" width="200%" height="200%">
                <feDropShadow dx="0" dy="4" stdDeviation="12" flood-color="rgba(0,0,0,0.15)"/>
            </filter>
            """
        } else {
            """
            <filter id="cardShadow" x="-50%" y="-50%" width="200%" height="200%">
                <feDropShadow dx="0" dy="4" stdDeviation="10" flood-color="rgba(0,0,0,0.1)"/>
            </filter>
            """
        }

        val styles = if (useGlassEffect) {
            """
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@400;600;700&amp;display=swap');
                
                #diag_$id .shadowed {
                    filter: url(#glassDropShadow);
                }

                #diag_$id .glass-card {
                    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
                    transform-origin: center;
                    animation: fadeInSlideUp 0.6s ease-out forwards;
                    opacity: 0;
                }

                @keyframes fadeInSlideUp {
                    from {
                        opacity: 0;
                        transform: translateY(20px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }

                #diag_$id .glass-card:hover {
                    transform: scale(1.02) translateY(-5px);
                    filter: brightness(1.05);
                    cursor: pointer;
                }

                #diag_$id .glass-overlay {
                    pointer-events: none;
                }

                #diag_$id .glass-highlight {
                    pointer-events: none;
                    opacity: 0.4;
                    transition: opacity 0.3s ease;
                }

                #diag_$id .glass-card:hover .glass-highlight {
                    opacity: 0.8;
                }

                #diag_$id .glass-border {
                    stroke-width: 1.5;
                    transition: all 0.3s ease;
                }

                #diag_$id .glass-card:hover .glass-border {
                    stroke-width: 2.5;
                    stroke: rgba(255, 255, 255, 0.6);
                }
                
                #diag_$id .card-text {
                    font-family: 'Outfit', sans-serif;
                }
                
                #diag_$id .dark .card-text {
                }
            </style>
            """
        } else {
            """
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@400;600;700&amp;display=swap');
                #diag_$id .shadowed {
                    filter: url(#cardShadow);
                }
                #diag_$id .card-text {
                    font-family: 'Outfit', sans-serif;
                }
            </style>
            """
        }

        return """
            <defs>
            <pattern id="dotPattern" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                <circle cx="2" cy="2" r="1" fill="${if(useDark) "#374151" else "#E5E7EB"}" />
            </pattern>
            $grad
            $glassEffectDefs
            $shadowFilter
            $styles
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
        connectors.forEachIndexed { i, conn ->
            val grad = "url(#grad$i)"
            var strokeWidth = 2
            fill = "white"
            var style = ""
            val boxColor = colors[i]
            val textFill = determineTextColor(boxColor)
            if (!useGrad) {
                fill = "none"
                strokeWidth = 5
                style = "font-size: 24px; font-family: 'Outfit', sans-serif; font-variant: small-caps; font-weight: bold;"
            }
            val lines = conn.textToLines()
            val str = StringBuilder("""<text x="135" y="${conn.start}" text-anchor="middle" fill="$textFill" class="card-text" style="font-size: 18px; font-weight: 600; letter-spacing: -0.01em;$style" > """)

            lines.forEachIndexed { j, content ->
                val dy = if (j > 0) "dy=\"22\"" else ""
                str.append("""<tspan x="135" $dy>$content</tspan>""")
            }
            str.append("</text>")

            val animationDelay = i * 0.05
            
            sb.append("<g transform=\"translate($x,$y)\">")
            if (useGlassEffect) {
                sb.append("""
                    <g class="glass-card" style="animation-delay: ${animationDelay}s">
                        <!-- Base rectangle with gradient -->
                        <rect x="10" y="10" width="250" height="90" ry="16" rx="16" fill="$grad" filter="url(#glassDropShadow)" stroke="url(#glassBorder)" stroke-width="1.5" class="glass-border" />
                        <!-- Glass overlay with transparency -->
                        <rect x="10" y="10" width="250" height="90" ry="16" rx="16" fill="url(#glassOverlay)" filter="url(#glassBlur)" class="glass-overlay" />
                        <!-- Top highlight for shine -->
                        <rect x="18" y="18" width="234" height="35" rx="12" ry="12" fill="url(#glassHighlight)" class="glass-highlight" />
                        <!-- Radial highlight for realistic light effect -->
                        <ellipse cx="40" cy="35" rx="15" ry="12" fill="url(#glassRadial)" class="glass-highlight" opacity="0.5" />
                        $str
                        <rect x="270" y="13" height="20" width="20" fill="$grad" rx="6" ry="6" filter="url(#cardShadow)"/>
                        <text x="280" y="27" fill="$textFill" text-anchor="middle" style="font-family: 'Outfit', sans-serif; font-size: 12px; font-weight: 700; letter-spacing: 0.02em;">${alphabets[i]}</text>
                """.trimIndent())
            } else {
                sb.append("""
                    <g>
                        <use xlink:href="#bbox" x="10" y="10" fill="$fill" stroke="rgba(0,0,0,0.08)" stroke-width='1'/>
                        $str
                        <rect x="270" y="13" height="20" width="20" fill="$grad" rx="6" ry="6"/>
                        <text x="280" y="27" fill="$textFill" text-anchor="middle" style="font-family: 'Outfit', sans-serif; font-size: 12px; font-weight: 700; letter-spacing: 0.02em;">${alphabets[i]}</text>
                """.trimIndent())
            }

            // Connection logic
            if (i < connectors.lastIndex) {
                if ((i + 1) % 5 == 0) {
                    // Row-wrapping connector
                    sb.append("""
                        <!-- Row-wrapping connector -->
                        <g transform="translate(260,50)">
                            <path d="M0,0 L60,0" stroke-width="2" stroke="rgba(107,114,128,0.4)" fill="none"/>
                            <line x1="60" x2="60" y1="0" y2="70" stroke-width="2" stroke="rgba(107,114,128,0.4)" stroke-linecap="round" stroke-linejoin="round"/>
                            <line x1="60" x2="-1480" y1="70" y2="70" stroke-width="2" stroke="rgba(107,114,128,0.4)" stroke-linecap="round" stroke-linejoin="round"/>
                            <line x1="-1480" x2="-1480" y1="120" y2="70" stroke-width="2" stroke="rgba(107,114,128,0.4)" stroke-linecap="round" stroke-linejoin="round"/>
                            <line x1="-1480" x2="-1460" y1="120" y2="120" stroke-width="2" stroke="rgba(107,114,128,0.4)" stroke-linecap="round" stroke-linejoin="round"/>
                            <g transform="translate(-1460,117)">
                                <use xlink:href="#ppoint" fill="url(#grad${i + 1})" stroke="url(#grad${i + 1})"/>
                            </g>
                        </g>
                    """.trimIndent())
                } else {
                    // Normal horizontal connector
                    sb.append("""
                        <use xlink:href="#hconnector" stroke="rgba(107,114,128,0.4)" fill="none"/>
                        <g transform="translate(297,47)">
                            <use xlink:href="#ppoint" fill="$grad" stroke="$grad"/>
                        </g>
                    """.trimIndent())
                }
            }
            sb.append("</g></g>")

            // Update coordinates
            if ((i + 1) % 5 == 0) {
                x = 0
                y += 120
            } else {
                x += 300
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
