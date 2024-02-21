package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.ShapeResponse
import io.docops.docopsextensionssupport.support.getRandomColorHex
import io.docops.docopsextensionssupport.support.gradientFromColor
import io.docops.docopsextensionssupport.support.hexToHsl
import java.io.File


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

    private var bgColor = "#fcfcfc"
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
        sb.append(head(height + descriptionHeight, width = width, scale))
        initColors()
        if("PDF" != type) {
            sb.append(defs())
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
            sb.append("<rect width=\"100%\" height=\"100%\" fill=\"$bgColor\"/>")

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
        str.lines().forEach {
            sb.append(it.trim())
        }
        return sb.toString()
    }

    private fun descriptions(start: Float): String {
        val sb = StringBuilder("<g transform='translate(100,$start)'>")
        var y = 0


        connectors.forEachIndexed {
            i, item ->
            var fill = "fill=\"${colors[i]}\""
            /*if("PDF" != type) {
                fill = "fill=\"url(#grad$i)\""
            }*/
            sb.append("""
                <g transform="translate(0,$y)">
                    <rect x="0" y="13" height="24" width="24" $fill rx="5" ry="5"/>
                    <text x="12" y="29" fill="#111111" text-anchor="middle" class="filtered-small glass">${alphabets[i]}</text>
                    <text x="42" y="29" text-anchor="start" style="font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif;" class="desc_txt">
                        ${item.description}
                    </text>
                </g>
            """.trimIndent())
            y += 26
        }
        sb.append("</g>")
        return sb.toString()
    }
    private fun head(height: Float, width: Float, scale: Float = 1.0f)  = """
        <svg xmlns="http://www.w3.org/2000/svg" width="${width*scale}" height="${height*scale}" viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="diag">
    """.trimIndent()

    private fun tail() = "</svg>"

    private fun initColors() {
        for (i in 0..connectors.size) {
            var choiceColor = ""
            if(i > gradients.size) {
                choiceColor = getRandomColorHex()
            } else {
                choiceColor = gradients.keys.elementAt(i)
            }
            colors.add(choiceColor)
        }
    }
    private fun defs() : String {

        val grad= StringBuilder()

        colors.forEachIndexed {
            i, choiceColor ->
           val res = gradientMapToHsl()[choiceColor]
            if(null == res) {
                val gradient = gradientFromColor(choiceColor)
                val hsl = hexToHsl(choiceColor)
                grad.append(
                    """
           <linearGradient id="grad${i}" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/>
            <stop class="stop2" offset="100%" stop-color="$hsl"/>
            </linearGradient> 
            """.trimIndent()
                )
            } else {
                grad.append(res)
            }
        }



        //language=svg
        return """
            <defs>
            $grad
        <filter id="filter">
            <feMorphology in="SourceAlpha" operator="dilate" radius="2" result="OUTLINE"/>
            <feComposite operator="out" in="OUTLINE" in2="SourceAlpha"/>
        </filter>
        <filter id="poly" x="0" y="0" width="200%" height="200%">
            <feOffset result="offOut" in="SourceGraphic" dx="10" dy="15" />
            <feGaussianBlur result="blurOut" in="offOut" stdDeviation="5" />
            <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />
        </filter>
        <filter id="shadow2">
            <feDropShadow dx="-0.8" dy="-0.8" stdDeviation="0" flood-color="pink" flood-opacity="0.5" />
        </filter>
        <style>
            .shadowed {
                -webkit-filter: drop-shadow( 3px 3px 2px rgba(0, 0, 0, .3));
                filter: drop-shadow( 3px 3px 2px rgba(0, 0, 0, .3));
            }
            .filtered {
                filter: url(#filter);
                fill: black;
                font-family: 'Ultra', serif;
                font-size: 100px;

            }
            .filtered-small {
                filter: url(#filter);
                fill: black;
                font-family: 'Ultra', serif;
                font-size: 14px;

            }

            .glass:after,.glass:before{content:"";display:block;position:absolute}.glass{overflow:hidden;color:#fff;text-shadow:0 1px 2px rgba(0,0,0,.7);background-image:radial-gradient(circle at center,rgba(0,167,225,.25),rgba(0,110,149,.5));box-shadow:0 5px 10px rgba(0,0,0,.75),inset 0 0 0 2px rgba(0,0,0,.3),inset 0 -6px 6px -3px rgba(0,129,174,.2);position:relative}.glass:after{background:rgba(0,167,225,.2);z-index:0;height:100%;width:100%;top:0;left:0;backdrop-filter:blur(3px) saturate(400%);-webkit-backdrop-filter:blur(3px) saturate(400%)}.glass:before{width:calc(100% - 4px);height:35px;background-image:linear-gradient(rgba(255,255,255,.7),rgba(255,255,255,0));top:2px;left:2px;border-radius:30px 30px 200px 200px;opacity:.7}.glass:hover{text-shadow:0 1px 2px rgba(0,0,0,.9)}.glass:hover:before{opacity:1}.glass:active{text-shadow:0 0 2px rgba(0,0,0,.9);box-shadow:0 3px 8px rgba(0,0,0,.75),inset 0 0 0 2px rgba(0,0,0,.3),inset 0 -6px 6px -3px rgba(0,129,174,.2)}.glass:active:before{height:25px}

            .boxText {
                font-size:24px;
                font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif;
                font-variant: small-caps;
                font-weight: bold;
            }
            
        </style>
        <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="7" />
        <rect id="bbox" class="shadowed"  width="250" height="90" ry="18" rx="18"  />
        <path id="hconnector" d="M260,50.0 h34" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
        <path id="vconnector" d="M135,100 v34" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
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
            var strokeWidth = 3
            fill = "none"
            var style = ""
            if(!useGrad) {
                fill = "none"
                strokeWidth = 5
                grad = colors[i]
                style = """style="font-size: 24px; font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif; font-variant: small-caps; font-weight: bold;""""
            }
            val lines= conn.textToLines()
            val str = StringBuilder("""<text x="135" y="${conn.start}" text-anchor="middle" class="filtered glass boxText" $style> """)
            var newLine = false

            lines.forEachIndexed {
                j, content ->
                var dy=""
                if(j>0) {
                    dy = "dy=\"24\""
                }
                str.append("""<tspan x="135" $dy>$content</tspan>""")
            }
            str.append("</text>")

            if(i == connectors.lastIndex) {
                //language=svg
                sb.append("""
             <g transform="translate($x,$y)" >
                <use xlink:href="#bbox" x="10" y="10" fill="$fill" stroke="${colors[i]}" stroke-width='$strokeWidth'/>
                $str
            </g>
                """.trimIndent())
            }
            else {
                if((i + 1) % 5 == 0) {
                    newLine = true
                }
                var arrow = """<g transform="translate(297,47)"><use xlink:href="#ppoint" fill="$grad" stroke-width="7" stroke="$grad"/></g>"""
                if(newLine) {
                    arrow=""
                }
                //language=svg
                sb.append(
                    """
            <g transform="translate($x,$y)" >
                <use xlink:href="#bbox" x="10" y="10" fill="$fill" stroke="${colors[i]}" stroke-width='$strokeWidth'/>
                $str
                <use xlink:href="#hconnector" stroke="${colors[i]}" fill="$grad"/>
                $arrow
                <rect x="270" y="13" height="24" width="24" fill="$grad" rx="5" ry="5"/>
                <text x="282" y="29" fill="#111111" text-anchor="middle" class="filtered-small glass">${alphabets[i]}</text>
            """.trimIndent()
                )
                if(newLine) {
                    sb.append("""
                <g transform="translate(260,50)">
                    <path d="M0,0 L60,0" fill="#111111" stroke-width="3" stroke="${colors[i]}"/>
                    <line x1="60" x2="60" y1="0" y2="60" stroke-width="3" stroke="${colors[i]}" stroke-linecap="round" stroke-linejoin="round"/>
                    <line x1="60" x2="-1480" y1="60" y2="60" stroke-width="3" stroke="${colors[i]}" stroke-linecap="round" stroke-linejoin="round"/>
                    <line x1="-1480" x2="-1480" y1="110" y2="60" stroke-width="3" stroke="${colors[i]}" stroke-linecap="round" stroke-linejoin="round"/>
                    <line x1="-1480" x2="-1460" y1="110" y2="110" stroke-width="3" stroke="${colors[i]}" stroke-linecap="round" stroke-linejoin="round"/>
                    <g transform="translate(-1460,107)"><use xlink:href="#ppoint" fill="$grad" stroke-width="7" stroke="$grad"/></g>
                </g>
                """.trimIndent())
                    x = 0
                    y += 110
                }
                sb.append("</g>")
            }
            if(!newLine)
            x += 300
        }
        return sb.toString()
    }
}

fun main() {
    val collectors = mutableListOf<Connector>(
        Connector("Developer", description = "Writes unit tests"),
        Connector("Unit Tests", description = "Unit tests produces excel"),
        Connector("Microsoft Excel", description = "Excel is stored in test engine"),
        Connector("Test Engine", description = "Test Engine write documentation"),
        Connector("API Documentation Output", description = "Documentation is committed"),
        Connector("GitHub", description ="Triggers a webhook"),
        Connector("Developer", description ="Developer consumes git content"),
        Connector("Unit Tests", description = "Unit tests produces excel"),
        Connector("Microsoft Excel", description = "Excel is stored in test engine"),
        Connector("Test Engine", description = "Test Engine write documentation"),
        Connector("API Documentation Output", description = "Documentation is committed"),
        Connector("GitHub", description ="Triggers a webhook")
    )
    val conn= ConnectorMaker(collectors, true, "SVG")
    val svg = conn.makeConnectorImage(0.8f)
    val f = File("gen/connector.svg")
    f.writeText(svg.shapeSvg)
    val conn2 = ConnectorMaker(collectors, false, "PDF")
    val svg2 = conn2.makeConnectorImage(0.8f)
    val f2 = File("gen/connector2.svg")
    f2.writeText(svg2.shapeSvg)

}