package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.releasestrategy.gradientColorFromColor
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.support.gradientFromColor
import io.docops.docopsextensionssupport.svgsupport.textWidth

class VGroupBar {
    private var height  = 600
    private var fontDisplayColor = "#111111"
    private val width = 800
    fun makeVerticalBar(barGroup: BarGroup): String {
        if(barGroup.display.useDark) {
            fontDisplayColor = "#fcfcfc"
        }
        val sb = StringBuilder()
        sb.append(head(barGroup))
        sb.append(makeDefs(makeGradient(barGroup.display), barGroup))
        sb.append(makeBackground(barGroup))
        sb.append(makeTitle(barGroup))
        sb.append(makeLineSeparator(barGroup))
        sb.append(makeColumnHeader(barGroup))
        var startY = 80
        barGroup.groups.forEach { t ->
            startY = makeGroup(startY, t, barGroup, sb)
        }
        val lastBar = startY
        sb.append(addLegend(lastBar.toDouble(), barGroup))
        sb.append(tail())
        return sb.toString()
    }

    private fun makeGroup(startY: Int, group: Group, barGroup: BarGroup, builder: StringBuilder): Int {
        val sb = StringBuilder()
        sb.append("""<g aria-label="${group.label}" transform="translate(203,$startY)">""")
        var currentY = 0
        val bars = (group.series.size * 24) /2 + 6
        sb.append("""
            <text x="-10" y="$bars" text-anchor="end"
                  style="fill: $fontDisplayColor; font-family: Arial; Helvetica; sans-serif; font-size:12px;">${group.label}
            </text>
        """.trimIndent())
        group.series.forEachIndexed { idx, it ->
            val per = barGroup.scaleUp(it.value)
            sb.append("""
            <rect class="glass bar shadowed" y="$currentY" x="0.0" height="24" width="$per" fill="url(#defColor_$idx)" style="stroke: #111111;" />
            <text x="${per+5}" y="${currentY + 14}" style="font-family: Arial,Helvetica, sans-serif; font-size:9px;; fill: $fontDisplayColor;">
                ${barGroup.valueFmt(it.value)}
            </text>
            """.trimIndent())
            currentY += 26
        }
        sb.append("</g>")
        builder.append(sb.toString())
        return startY+currentY+24
    }

    private fun head(barGroup: BarGroup): String {
        height = barGroup.calcHeight()
        val numOfBars = barGroup.groups.sumOf{it.series.size}
        val heightAdjustment = (numOfBars * 24) + (numOfBars * 5)  + (barGroup.groups.size*24) + 80
        height = heightAdjustment
        val finalHeight = height * barGroup.display.scale
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="$width" height="$finalHeight" viewBox="0 0 $width $heightAdjustment" xmlns="http://www.w3.org/2000/svg">
        """.trimIndent()
    }
    private fun tail(): String {
        return "</svg>"
    }
    private fun makeBackground(barGroup: BarGroup): String {
        var backGround = "#f5f5f5"
        if(barGroup.display.useDark) {
            backGround = "#1f2937"
        }
        return """
            <rect width="100%" height="100%" fill="$backGround" stroke="url(#backGrad_${barGroup.id})"/>
        """.trimIndent()
    }
    private fun makeTitle(barGroup: BarGroup): String {
        var back = ""
        if(barGroup.display.useDark) {
            back= "dark_"
        }
        return """
        <g>
        <rect height="60" x="0" y="0" width="100%" fill="url(#backGrad_$back${barGroup.id})"/>
        <text x="400" y="40" text-anchor="middle" style="fill: $fontDisplayColor; font-family: Arial; Helvetica; sans-serif; font-size:20px;">${barGroup.title.escapeXml()}
        </text>
    </g>
        """.trimIndent()
    }
    private fun makeLineSeparator(barGroup: BarGroup) : String{
        var strokeColor = "#000000"
        if(barGroup.display.useDark) {
            strokeColor = "#fcfcfc"
        }
        return "<line x1=\"200\" x2=\"200\" y1=\"80\" y2=\"$height\" stroke=\"$strokeColor\" stroke-width=\"3\"/>"
    }

    private fun makeColumnHeader(barGroup: BarGroup) : String {

        return """
     <g>
        <text x="193" y="75" text-anchor="end" style="fill: $fontDisplayColor; font-family: Arial; Helvetica; sans-serif; font-size:12px; font-weight: bold;">${barGroup.xLabel?.escapeXml()}</text>
        <text x="205" y="75" text-anchor="start" style="fill: $fontDisplayColor; font-family: Arial; Helvetica; sans-serif; font-size:12px; font-weight: bold;">${barGroup.yLabel?.escapeXml()}</text>
    </g>
        """.trimIndent()
    }
    private fun addLegend(d: Double, group: BarGroup): String {
        var back = ""
        var fColor = "#111111"
        if(group.display.useDark) {
            back= "dark_"
            fColor = "#fcfcfc"
        }
        val sb = StringBuilder()
        val distinct = group.legendLabel().distinct()
        sb.append("<g transform='translate(0, $d)'>")

        sb.append("""<rect x="0" y="0" width="100%" height="${height-d}"  fill="url(#backGrad_$back${group.id})"/> """)
        sb.append("""<text x="${width/2}" y="14" text-anchor="middle" style="font-family: Arial,Helvetica, sans-serif; fill: $fColor; font-size:12px;">Legend</text> """)
        var y = 18
        var startX = 0.2 * width
        val endX =  width - (0.2 * width)
        distinct.forEachIndexed { index, item ->
            if(startX > endX) {
                y+= 10
                startX = 0.2 * width
            }
            val color = "url(#defColor_$index)"
            sb.append("""<rect x="$startX" y="$y" width="8" height="8" fill="$color"/>""")
            sb.append("""<text x="${startX+ 10}" y="${y+8}" style="font-family: Arial,Helvetica, sans-serif; fill: $fColor; font-size:10px;">""")
            sb.append("""<tspan x="${startX+ 10}" dy="0">$item</tspan>""")
            sb.append("</text>")
            startX += 8 + item.textWidth("Helvetica", 10) + 8
        }
        sb.append("</g>")
        return sb.toString()
    }
    private fun makeDefs(gradients: String, barGroup: BarGroup): String {
        val defGrad = StringBuilder()
        STUNNINGPIE.forEachIndexed { idx, item->
            val gradient = gradientFromColor(item)
            defGrad.append("""
                <linearGradient id="defColor_$idx" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/>
        </linearGradient>
            """.trimIndent())
        }

        val backColor = gradientColorFromColor(barGroup.display.baseColor, "backGrad_${barGroup.id}")
        val darkBackColor = gradientColorFromColor("#1f2937", "backGrad_dark_${barGroup.id}")
        return """<defs>
                $defGrad
                $backColor
                $darkBackColor
                $gradients                   
                    <style>
                    .bar:hover {
                filter: grayscale(100%) sepia(100%);
            }
            .bar:hover {
                filter: grayscale(100%) sepia(100%);
            }
            .glass:after, .glass:before {
                content: "";
                display: block;
                position: absolute
            }

            .glass {
                overflow: hidden;
                color: #fff;
                text-shadow: 0 1px 2px rgba(0, 0, 0, .7);
                background-image: radial-gradient(circle at center, rgba(0, 167, 225, .25), rgba(0, 110, 149, .5));
                box-shadow: 0 5px 10px rgba(0, 0, 0, .75), inset 0 0 0 2px rgba(0, 0, 0, .3), inset 0 -6px 6px -3px rgba(0, 129, 174, .2);
                position: relative
            }

            .glass:after {
                background: rgba(0, 167, 225, .2);
                z-index: 0;
                height: 100%;
                width: 100%;
                top: 0;
                left: 0;
                backdrop-filter: blur(3px) saturate(400%);
                -webkit-backdrop-filter: blur(3px) saturate(400%)
            }

            .glass:before {
                width: calc(100% - 4px);
                height: 35px;
                background-image: linear-gradient(rgba(255, 255, 255, .7), rgba(255, 255, 255, 0));
                top: 2px;
                left: 2px;
                border-radius: 30px 30px 200px 200px;
                opacity: .7
            }

            .glass:hover {
                text-shadow: 0 1px 2px rgba(0, 0, 0, .9)
            }

            .glass:hover:before {
                opacity: 1
            }

            .glass:active {
                text-shadow: 0 0 2px rgba(0, 0, 0, .9);
                box-shadow: 0 3px 8px rgba(0, 0, 0, .75), inset 0 0 0 2px rgba(0, 0, 0, .3), inset 0 -6px 6px -3px rgba(0, 129, 174, .2)
            }

            .glass:active:before {
                height: 25px
            }
            .shadowed {
                -webkit-filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
                filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
            }
            </style>
           </defs>"""
    }
    private fun makeGradient(barDisplay: BarGroupDisplay): String {
        val gradient1 = gradientFromColor(barDisplay.baseColor)
        return """
        <linearGradient id="linearGradient_${barDisplay.id}" x2="100%" y2="0%">
            <stop class="stop1" offset="0%" stop-color="${gradient1["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient1["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient1["color3"]}"/>
        </linearGradient>
        """.trimIndent()
    }
    class GroupResult(val endY: Int, val bars: String)
}