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
        sb.append("<rect width=\"100%\" height=\"100%\" fill=\"$bgColor\" stroke=\"#111111\" stroke-width=\"3\"/>")
        sb.append("<g transform=\"translate(0,0)\">")
        sb.append("""<g transform="translate(0,0)">
            <text x="20" y="24" text-anchor="start" font-size="24" font-family="Arial,DejaVu Sans,sans-serif" font-variant="small-caps" fill="$fgColor">${placeMatRequest.title}</text>
        </g>
        <g transform="translate(0,50)">""")
        sb.append(makeBody(id))
        sb.append(makeLegend(height + 20 - 50, id))
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
            /*if(!useGrad) {
                grad = placeMatRequest.config.colorFromLegendName(conn.legend).color
            }*/
            var strokeWidth = 1
            if(!placeMatRequest.fill) {
                //grad = "none"
                strokeWidth = 5
            }
            if(placeMatRequest.useDark && !placeMatRequest.fill ) {
                grad = "#fcfcfc"
            }
            if(isPdf) {
                grad = placeMatRequest.config.colorFromLegendName(conn.legend).color
            }
            val lines= conn.textToLines()
            val str = StringBuilder("""<text x="135" y="${lines.second}" text-anchor="middle" class="glass" style="fill:$textColor; font-family: 'Ultra', serif;font-size:24px;font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif;font-variant: small-caps;font-weight: bold;">""")
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
                //language=svg
                sb.append(
                    """
            <g transform="translate($x,$y)" >
                <rect x="10" y="10" class="shadowed"  width="250" height="90" ry="18" rx="18"  style="fill: ${grad}; stroke: ${placeMatRequest.config.colorFromLegendName(conn.legend).color};stroke-width: $strokeWidth;"/>
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
            #diag_$id .shadowed {
                -webkit-filter: drop-shadow( 3px 3px 2px rgba(0, 0, 0, .3));
                filter: drop-shadow( 3px 3px 2px rgba(0, 0, 0, .3));
            }
            #diag_$id  .filtered {
                filter: url(#filter);
                fill: black;
                font-family: 'Ultra', serif;
                font-size: 100px;

            }
            #diag_$id  .filtered-small {
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

            #diag_$id  .boxText {
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
    private fun makeLegend(y: Float, id: String): String {
        val sb = StringBuilder("""<g transform="translate(20,$y),scale(0.15)">""")
        sb.append("""<text x="10" font-size="110" font-family="Arial,DejaVu Sans,sans-serif" font-variant="small-caps" fill="$fgColor">Legend</text>""")
        var start = 10.0
        placeMatRequest.config.legend.forEach{
            item ->
            var grad = "url(#grad_${item.legendAsStyle()}_$id)"
            if(!useGrad) {
                grad = item.color
            }
            val textColor = SVGColor(item.color, UUID.randomUUID().toString()).foreGroundColor
                val textLen = item.legend.textWidth("Helvetica", 110)
            sb.append("""
            <g transform="translate($start,40)" font-family="Helvetica,Arial,sans-serif" font-size="96">
                <rect x="0" y="0" width="${textLen+20}" height="110" fill="$grad" class="shadowed"/>
                <text text-anchor="middle" style="fill: $textColor;" x="${(textLen+20)/2}" y="90" textLength="${textLen - 10}">${item.legend}</text>
            </g>
            """.trimIndent())
            start += (textLen + 60)
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