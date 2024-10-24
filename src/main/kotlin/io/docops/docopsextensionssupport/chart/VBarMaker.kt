package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.releasestrategy.gradientColorFromColor
import io.docops.docopsextensionssupport.support.determineTextColor

class VBarMaker {
    private var fontColor = ""
    private var height = 600
    fun makeVerticalBar(bar: Bar): String {
        fontColor = determineTextColor(bar.display.baseColor)
        val sb = StringBuilder()
        sb.append(head(bar))
        sb.append(addDefs(bar))
        sb.append(makeBackground(bar))
        sb.append(makeTitle(bar))
        sb.append(makeLineSeparator())
        sb.append(makeColumnHeader(bar))
        sb.append(addBars(bar))
        sb.append(tail())
        return sb.toString()
    }

    private fun addBars(bar: Bar) : String {
        var sb = StringBuilder()
        var startY = 80
        bar.series.forEach {
            val per = bar.scaleUp(it.value)
            sb.append("""
       <g transform="translate(245, $startY)"><!--560 168/2-->
        <rect class="bar" x="0" height="40" width="$per" fill="url(#backGrad_${bar.display.id})"/>
        <text x="-10" y="20" text-anchor="end"
              style="fill: #111111; font-family: Arial; Helvetica; sans-serif; font-size:12px;">${it.label?.escapeXml()}
        </text>
        <text x="${per / 2}" y="24" text-anchor="middle"
              style="fill: #fcfcfc; font-family: Arial; Helvetica; sans-serif; font-size:12px;">${it.value}
        </text>
    </g>
            """.trimIndent())
            startY += 45

        }
        return sb.toString()
    }

    private fun makeColumnHeader(bar: Bar) : String{
        return """
     <g>
        <text x="235" y="75" text-anchor="end" style="fill: #111111; font-family: Arial; Helvetica; sans-serif; font-size:12px; font-weight: bold;">${bar.xLabel?.escapeXml()}</text>
        <text x="245" y="75" text-anchor="start" style="fill: #111111; font-family: Arial; Helvetica; sans-serif; font-size:12px; font-weight: bold;">${bar.yLabel?.escapeXml()}</text>
    </g>
        """.trimIndent()
    }

    private fun makeLineSeparator() : String{
        return "<line x1=\"240\" x2=\"240\" y1=\"60\" y2=\"${height - 60}\" stroke=\"#A64942\"/>"
    }

    private fun makeBackground(bar: Bar): String {
        return """
            <rect width="100%" height="100%" fill="#f5f5f5"/>
        """.trimIndent()
    }


    private fun head(bar: Bar): String {
        val numOfBars = bar.series.size
        val heightAdjustment = (numOfBars * 40) + (numOfBars * 5) + 160
        height = heightAdjustment
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="800" height="$heightAdjustment" viewBox="0 0 800 $heightAdjustment" xmlns="http://www.w3.org/2000/svg">
        """.trimIndent()
    }
    private fun tail(): String {
        return "</svg>"
    }

    private fun makeTitle(bar: Bar): String {
        return """
        <g>
        <rect height="60" x="0" y="0" width="100%" fill="url(#backGrad_${bar.display.id})"/>
        <rect height="60" x="0" y="${height - 60}" width="100%" fill="url(#backGrad_${bar.display.id})"/>
        <text x="400" y="40" text-anchor="middle"
              style="fill: $fontColor; font-family: Arial; Helvetica; sans-serif; font-size:20px;">${bar.title.escapeXml()}
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
            </style>
            </defs>
        """.trimIndent()
    }
}