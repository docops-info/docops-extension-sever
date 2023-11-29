package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.support.getRandomColorHex
import io.docops.docopsextensionssupport.support.gradientFromColor
import java.io.File

class PlaceMatMaker(val placeMats: MutableList<PlaceMat>, val useDark: Boolean = false, val config: PlaceMatConfig= PlaceMatConfig()) {

    private var bgColor = "#fcfcfc"
    private val colors = mutableListOf<String>()
    fun makePlacerMat(scale: Float = 1.0f): String {
        if(useDark) {
            bgColor = "#111111"
        }
        val width: Float = (placeMats.chunked(5)[0].size * 250).toFloat() + 60
        val height = placeMats.chunked(5).size * 110.0f
        val sb = StringBuilder()
        sb.append(head(height, width = width, scale))
        sb.append(defs())
        sb.append("<rect width=\"100%\" height=\"100%\" fill=\"$bgColor\"/>")
        sb.append("<g transform=\"translate(0,0)\">")
        sb.append(makeBody())
        sb.append("</g>")
        sb.append(tail())
        return sb.toString()
    }

    private fun makeBody(): String {
        val sb = StringBuilder()
        var x = 0
        var y = 0
        placeMats.forEachIndexed {
                i, conn ->
            val lines= conn.textToLines()
            val str = StringBuilder("""<text x="135" y="${lines.second}" text-anchor="middle" class="filtered glass boxText">""")
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

            if(i == placeMats.lastIndex) {
                //language=svg
                sb.append("""
             <g transform="translate($x,$y)" >
                <use href="#bbox" x="10" y="10" fill="url(#grad${conn.colorIndex})"/>
                $str
            </g>
                """.trimIndent())
            }
            else {
                if((i + 1) % 5 == 0) {
                    newLine = true
                }
                //language=svg
                sb.append(
                    """
            <g transform="translate($x,$y)" >
                <use href="#bbox" x="10" y="10" fill="url(#grad${conn.colorIndex})"/>
                $str
            """.trimIndent()
                )
                if(newLine) {
                    x = 0
                    y += 110
                }
                sb.append("</g>")
            }
            if(!newLine)
                x += 260
        }
        return sb.toString()
    }
    private fun head(height: Float, width: Float, scale: Float = 1.0f)  = """
        <svg xmlns="http://www.w3.org/2000/svg" width="${width*scale}" height="${height*scale}"
     viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="diag">
    """.trimIndent()

    private fun tail() = "</svg>"

    private fun defs() : String {

        val grad= StringBuilder()

        for (i in 0..placeMats.size) {
            val choiceColor: String =  if(i>config.baseColors.size-1) {
                getRandomColorHex()
            } else {
                config.baseColors[i]
            }
            colors.add(choiceColor)

            val gradient = gradientFromColor(choiceColor)
            grad.append(
                """
           <linearGradient id="grad${i}" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/>
            </linearGradient> 
            """.trimIndent()
            )
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
}

fun main() {
    val pmm = PlaceMatMaker(mutableListOf(PlaceMat("SUI", colorIndex = 1), PlaceMat("Contact View", colorIndex = 2),
        PlaceMat("Contact Management", colorIndex = 2),
        PlaceMat("CDE Wrapper", colorIndex = 1),
        PlaceMat("Live Publish", colorIndex = 1),
        PlaceMat("Policy Quote Search", colorIndex = 3),
        PlaceMat("NXT3", colorIndex = 1)
    ),config= PlaceMatConfig(baseColors = mutableListOf(
        "#CDF5FD",
        "#9AD0C2",
        "#FFEBD8",
        "#9EB8D9"))
    )
    val svg = pmm.makePlacerMat(1.0f)
    val f = File("gen/place.svg")
    f.writeText(svg)
}