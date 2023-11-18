package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.support.getRandomColorHex
import io.docops.docopsextensionssupport.support.gradientFromColor
import java.io.File


class ConnectorMaker(val connectors: MutableList<Connector>) {
    private val alphabets = CharRange('A','Z').toMutableList()
    private val colors = mutableListOf<String>()

    fun makeConnectorImage(scale: Float = 1.0f): String {

        val sb = StringBuilder()
        val width: Float = (connectors.size * 250).toFloat() + (connectors.size * 46).toFloat()
        sb.append(head(110.0f, width = width, scale))
        sb.append(defs())
        sb.append(makeBody())
        sb.append(tail())
        return sb.toString()
    }

    private fun head(height: Float, width: Float, scale: Float = 1.0f)  = """
        <svg xmlns="http://www.w3.org/2000/svg" width="${width*scale}" height="${height*scale}"
     viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="diag">
    """.trimIndent()

    private fun tail() = "</svg>"

    private fun defs() : String {

        val grad= StringBuilder()
        for (i in 0 .. connectors.size) {
            val randomColor = getRandomColorHex()
            colors.add(randomColor)
            val gradient = gradientFromColor(randomColor)
            grad.append("""
           <linearGradient id="grad${i}" x2="1" y2="1">
            <stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/>
            </linearGradient> 
            """.trimIndent())
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
            <feDropShadow
                    dx="-0.8"
                    dy="-0.8"
                    stdDeviation="0"
                    flood-color="pink"
                    flood-opacity="0.5" />
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

            .glass:after,.glass:before{content:"";display:block;position:absolute}.glass{overflow:hidden;color:#fff;text-shadow:0
            1px 2px rgba(0,0,0,.7);background-image:radial-gradient(circle at
            center,rgba(0,167,225,.25),rgba(0,110,149,.5));box-shadow:0 5px 10px rgba(0,0,0,.75),inset 0 0 0 2px
            rgba(0,0,0,.3),inset 0 -6px 6px -3px
            rgba(0,129,174,.2);position:relative}.glass:after{background:rgba(0,167,225,.2);z-index:0;height:100%;width:100%;top:0;left:0;backdrop-filter:blur(3px)
            saturate(400%);-webkit-backdrop-filter:blur(3px) saturate(400%)}.glass:before{width:calc(100% -
            4px);height:35px;background-image:linear-gradient(rgba(255,255,255,.7),rgba(255,255,255,0));top:2px;left:2px;border-radius:30px
            30px 200px 200px;opacity:.7}.glass:hover{text-shadow:0 1px 2px
            rgba(0,0,0,.9)}.glass:hover:before{opacity:1}.glass:active{text-shadow:0 0 2px rgba(0,0,0,.9);box-shadow:0
            3px 8px rgba(0,0,0,.75),inset 0 0 0 2px rgba(0,0,0,.3),inset 0 -6px 6px -3px
            rgba(0,129,174,.2)}.glass:active:before{height:25px}

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
        <rect />
        </defs>
        """.trimIndent()
    }
    private fun makeBody(): String {
        val sb = StringBuilder()
        var x = 0
        connectors.forEachIndexed {
            i, conn ->
            val lines= conn.textToLines()
            val str = StringBuilder("""<text x="135" y="${conn.start}" text-anchor="middle" class="filtered glass boxText">""")
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
             <g transform="translate($x,0)" >
                <use href="#bbox" x="10" y="10" fill="url(#grad$i)"/>
                $str
            </g>
                """.trimIndent())
            }
            else {
                //language=svg
                sb.append(
                    """
            <g transform="translate($x,0)" >
                <use href="#bbox" x="10" y="10" fill="url(#grad$i)"/>
                $str
                <use href="#hconnector" stroke="${colors[i]}" fill="url(#grad$i)"/>
                <g transform="translate(297,47)"><use href="#ppoint" fill="url(#grad$i)" stroke-width="7" stroke="url(#grad$i)"/></g>
                <rect x="270" y="13" height="24" width="24" fill="url(#grad$i)" rx="5" ry="5"/>
                <text x="282" y="29" fill="#111111" text-anchor="middle" class="filtered-small glass">${alphabets[i]}</text>
            </g>

            """.trimIndent()
                )
            }
            x += 300
        }
        return sb.toString()
    }
}

fun main() {
    val collectors = mutableListOf<Connector>(Connector("Developer"), Connector("Unit Tests"), Connector("Microsoft Excel"),
        Connector("Test Engine"), Connector("API Documentation Output")
    )
    val conn= ConnectorMaker(collectors)
    val svg = conn.makeConnectorImage(1.5f)
    val f = File("gen/connector.svg")
    f.writeText(svg)

}