package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import io.docops.docopsextensionssupport.releasestrategy.gradientColorFromColor
import io.docops.docopsextensionssupport.support.determineTextColor

class VBarMaker {
    private var fontColor = ""
    private var height = 600
    fun makeVerticalBar(bar: Bar): String {
        bar.sorted()
        fontColor = determineTextColor(bar.display.baseColor)
        val sb = StringBuilder()
        sb.append(head(bar))
        sb.append(addDefs(bar))
        sb.append(makeBackground(bar))
        sb.append(makeTitle(bar))
        sb.append(makeLineSeparator(bar))
        sb.append(makeColumnHeader(bar))
        sb.append(addBars(bar))
        sb.append(tail())
        return joinXmlLines(sb.toString())
    }

    private fun addBars(bar: Bar) : String {
        var sb = StringBuilder()
        var startY = 80
        var anchor = "start"
        var fill = "#FFF6F6"
        var fontDisplayColor = "#111111"
        if(bar.display.useDark) {
            fontDisplayColor = "#fcfcfc"
            fill = "url(#backGrad_${bar.display.id})"
        }
        bar.series.forEach {
            val per = bar.scaleUp(it.value)
            if(per < 41) {
                anchor = "start"
            }
            sb.append("""
       <g transform="translate(245, $startY)">
        <rect class="glass bar shadowed" x="0" height="40" width="$per" stroke="url(#backGrad_${bar.display.id})" stroke-width="3" fill="$fill"/>
        <text x="-10" y="24" text-anchor="end" style="fill: $fontDisplayColor; font-family: Arial; Helvetica; sans-serif; font-size:12px;">${it.label?.escapeXml()}
        </text>
        <text x="${per + 10}" y="24" text-anchor="$anchor" style="fill: $fontDisplayColor; font-family: Arial; Helvetica; sans-serif; font-size:12px;">${bar.valueFmt(it.value)}
        </text>
    </g>
            """.trimIndent())
            startY += 45

        }
        return sb.toString()
    }

    private fun makeColumnHeader(bar: Bar) : String {
        var fontDisplayColor = "#111111"
        if(bar.display.useDark) {
            fontDisplayColor = "#fcfcfc"
        }
        return """
     <g>
        <text x="235" y="75" text-anchor="end" style="fill: $fontDisplayColor; font-family: Arial; Helvetica; sans-serif; font-size:12px; font-weight: bold;">${bar.xLabel?.escapeXml()}</text>
        <text x="245" y="75" text-anchor="start" style="fill: $fontDisplayColor; font-family: Arial; Helvetica; sans-serif; font-size:12px; font-weight: bold;">${bar.yLabel?.escapeXml()}</text>
    </g>
        """.trimIndent()
    }

    private fun makeLineSeparator(bar: Bar) : String{
        return "<line x1=\"240\" x2=\"240\" y1=\"60\" y2=\"$height\" stroke=\"${bar.display.baseColor}\"/>"
    }

    private fun makeBackground(bar: Bar): String {
        var backGround = "#f5f5f5"
        if(bar.display.useDark) {
            backGround = "#1f2937"
        }
        return """
            <rect width="100%" height="100%" fill="$backGround" stroke="url(#backGrad_${bar.display.id})"/>
        """.trimIndent()
    }


    private fun head(bar: Bar): String {
        val numOfBars = bar.series.size
        val heightAdjustment = (numOfBars * 40) + (numOfBars * 5) + 160
        height = heightAdjustment
        val finalHeight = height * bar.display.scale
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="800" height="$finalHeight" viewBox="0 0 800 $heightAdjustment" xmlns="http://www.w3.org/2000/svg">
        """.trimIndent()
    }
    private fun tail(): String {
        return "</svg>"
    }

    private fun makeTitle(bar: Bar): String {
        return """
        <g>
        <rect height="60" x="0" y="0" width="100%" fill="url(#backGrad_${bar.display.id})"/>
        <text x="400" y="40" text-anchor="middle" style="fill: $fontColor; font-family: Arial; Helvetica; sans-serif; font-size:20px;">${bar.title.escapeXml()}
        </text>
    </g>
        """.trimIndent()
    }
    private fun addDefs(bar: Bar) : String {
        val backColor = gradientColorFromColor(bar.display.baseColor, "backGrad_${bar.display.id}")
        return """
            <defs>
            $backColor
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
            </defs>
        """.trimIndent()
    }
}