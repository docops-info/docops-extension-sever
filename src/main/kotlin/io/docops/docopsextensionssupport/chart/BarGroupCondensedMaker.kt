package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.support.generateRectanglePathData
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.textWidth
import java.util.UUID


class BarGroupCondensedMaker {
    private val BAR_HEIGHT = 10f
    private val width = 600f
    private val BOTTOM_BUFFER = 55f
    private var additional = 0f
    private var height = 400f
    private var TOTAL_BAR_HEIGHT = 400f

    private var fontColor = "#fcfcfc"
    var theme = BarTheme()
    fun makeBar(barGroup: BarGroup): String {
        theme = getTheme(barGroup.display.useDark)
        fontColor = determineTextColor(barGroup.display.baseColor)
        val sb = StringBuilder()
        var h = determineHeight(barGroup)
        var w = determineWidth(barGroup)
        sb.append(makeHead(h, w, barGroup, scale= barGroup.display.scale))

        height = h
        sb.append(makeDefs(barGroup))
        sb.append("<g transform='scale(${barGroup.display.scale})'>")
        sb.append("""<rect width="100%" height="100%" fill="${theme.background}"/>""")
        sb.append("""<text x="${width/2}" text-anchor="middle" y="16" style="font-size: 16px; fill: ${theme.titleColor}; font-family: Arial, Helvetica, sans-serif;">${barGroup.title}</text>""")
        sb.append(makeColumnHeader(barGroup))
        sb.append(addTickBars(h, w, barGroup))
        sb.append("""<g transform="translate(100,20)">""")
        var startY= 0
        barGroup.groups.forEach { group ->
            sb.append("""<g transform="translate(0,$startY)">""")
            sb.append(addGroup(barGroup, group))
            sb.append("</g>")
            startY += group.series.size * 10 + 5

        }
        sb.append("</g>")
        sb.append(addLegend((TOTAL_BAR_HEIGHT + 35) + 10f, barGroup))
        sb.append("</g>")
        sb.append(end())
        return joinXmlLines(sb.toString())
    }
    private fun addGroup(barGroup: BarGroup, added: Group): String {
        val sb = StringBuilder()
        var startY = 10
        val bars = (added.series.size * 10) /2 + 14
        sb.append("""
            <text x="0" y="$bars" text-anchor="end" style="fill: ${theme.textColor}; font-family: Arial; Helvetica; sans-serif; font-size:8px;">${added.label}</text>
        """.trimIndent())
        added.series.forEachIndexed { index, series ->
            val per = barGroup.scaleUp(series.value)
            val color = "url(#${series.label?.replace(" ", "")})"
            //println("${series.value} -> $per -> ${500-per}")
            val path = generateRectanglePathData(per.toFloat(), 10f,0.0f, 6.0f, 5f, 0.0f).replace("\n", "")
            sb.append("""
           <g transform="translate(10, $startY)">
                <path d="$path" fill="$color" class="bar shadowed"/>
                <text x="${per+5}" y="8" style="font-size: 8px; fill: ${theme.titleColor}; font-family: Arial, Helvetica, sans-serif;">${barGroup.valueFmt(series.value)}</text>
            </g>
            """.trimIndent())
            startY+=10

        }
        return sb.toString()

    }

    private fun makeHead(h: Float, w: Float, barGroup: BarGroup, scale: Double): String {
        return  """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="${(w * scale)/ DISPLAY_RATIO_16_9}" height="${(h * scale )/DISPLAY_RATIO_16_9}" viewBox="0 0 ${w*scale} ${h*scale}" xmlns="http://www.w3.org/2000/svg" id="${barGroup.id}" preserveAspectRatio="xMidYMid meet">
        """.trimIndent()
    }

    private fun end() = "</svg>"
    private fun makeDefs(barGroup: BarGroup): String {
        val defs = StringBuilder()
        val clrs = chartColorAsSVGColor()
        defs.append("<defs>")
        defs.append("""
            <linearGradient id="condensedLite" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop class="stop1" offset="0%" stop-color="#ffffff"/>
            <stop class="stop2" offset="50%" stop-color="#F8FAFC"/>
            <stop class="stop3" offset="100%" stop-color="#c6c8c9"/>
        </linearGradient>
            <linearGradient id="condensedDark" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop class="stop1" offset="0%" stop-color="#37084b"/>
            <stop class="stop2" offset="50%" stop-color="#2E073F"/>
            <stop class="stop3" offset="100%" stop-color="#240532"/>
        </linearGradient>""")
        val labels = barGroup.uniqueLabels()
        val sz = labels.size
        for(i in 0 until sz) {
            defs.append(SVGColor(STUNNINGPIE[i], labels[i].replace(" ", "")).linearGradient)
        }

        defs.append("<style type=\"text/css\">")
        defs.append("""
            .shadowed {
                -webkit-filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
                filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
            }
            .bar:hover {
                filter: grayscale(100%) sepia(100%);
            }
        """.trimIndent())
        defs.append("</style>")
        defs.append("</defs>")
        return defs.toString()
    }
    private fun addTickBars(h: Float, w: Float, barGroup: BarGroup): String {
        val bottom = TOTAL_BAR_HEIGHT + 35
        val tickBar = barGroup.maxValue() / 3
        val sb = StringBuilder()
        sb.append("""<line x1="109" x2="109" y1="30" y2="$bottom" class="light-shadow" stroke-width="2" stroke="${theme.lineColor}" />""")
        for(i in 1..9 step 1) {
            val spot = barGroup.scaleUp(tickBar * i) + 108
            sb.append("""
                <line x1="$spot" x2="$spot" y1="30" y2="$bottom" class="light-shadow" stroke-width="1" stroke="${theme.lineColor}" />
                <text x="$spot" y="${bottom+8}" text-anchor="middle" style="font-size: 8px; fill: ${theme.titleColor}; font-family: Arial, Helvetica, sans-serif;">${barGroup.valueFmt(tickBar*i)}</text>
            """.trimIndent())
        }
        sb.append("""<line x1="0" x2="$width" y1="$bottom" y2="$bottom" class="light-shadow" stroke-width="1" stroke="${theme.lineColor}" />""")
        return sb.toString()

    }
    private fun determineHeight(barGroup: BarGroup): Float {
        var count = 0
        barGroup.groups.forEachIndexed { index, g ->
            count += g.series.size
        }
        val labels = legendRow(barGroup)

        if(labels > 2) {
            additional += 12 * (labels - 2) + 14
        }
        TOTAL_BAR_HEIGHT = count * BAR_HEIGHT + barGroup.groups.size * 5 + BAR_HEIGHT
        return count * BAR_HEIGHT + barGroup.groups.size * 5 + BAR_HEIGHT + BOTTOM_BUFFER + 20 + additional
    }
    private fun determineWidth(barGroup: BarGroup): Float {
        return width
    }
    private fun addLegend(d: Float, group: BarGroup): String {
        var back = ""
        var fColor = theme.titleColor
        val sb = StringBuilder()
        val distinct = group.uniqueLabels()
        sb.append("<g transform='translate(0, $d)'>")
        sb.append("""<text x="${width/2}" y="14" text-anchor="middle" style="font-family: Arial,Helvetica, sans-serif; fill: $fColor; font-size:12px;">Legend</text> """)
        var y = 18
        var startX = 0.2 * width
        val endX =  width - (0.2 * width)
        distinct.forEachIndexed { index, item ->
            if(startX > endX) {
                y+= 10
                startX = 0.2 * width
            }
            val color = "url(#${item.replace(" ", "")})"
            sb.append("""<rect x="$startX" y="$y" width="8" height="8" fill="$color"/>""")
            sb.append("""<text x="${startX+ 10}" y="${y+8}" style="font-family: Arial,Helvetica, sans-serif; fill: $fColor; font-size:8px;">""")
            sb.append("""<tspan x="${startX+ 10}" dy="0">$item</tspan>""")
            sb.append("</text>")
            startX += 8 + item.textWidth("Helvetica", 10) + 8
        }
        sb.append("</g>")
        return sb.toString()
    }
    private fun legendRow(group: BarGroup): Int {
        var y = 18
        var startX = 0.2 * width
        val endX =  width - (0.2 * width)
        val distinct = group.uniqueLabels()
        var rows = 1
        distinct.forEachIndexed { index, item ->
            if(startX > endX) {
                y+= 10
                rows++
                startX = 0.2 * width
            }
            startX += 8 + item.textWidth("Helvetica", 10) + 8
        }
        return rows
    }
    fun getTheme(useDark: Boolean = false) : BarTheme {
        return if(useDark) {
            BarThemeDark()
        } else {
            BarThemeLite()
        }
    }
    private fun makeColumnHeader(barGroup: BarGroup) : String {

        return """
     <g>
        <text x="107" y="25" text-anchor="end" style="fill: ${theme.textColor}; font-family: Arial; Helvetica; sans-serif; font-size:10px; font-weight: bold;">${barGroup.xLabel?.escapeXml()}</text>
        <text x="112" y="25" text-anchor="start" style="fill: ${theme.textColor}; font-family: Arial; Helvetica; sans-serif; font-size:10px; font-weight: bold;">${barGroup.yLabel?.escapeXml()}</text>
    </g>
        """.trimIndent()
    }
}

open class BarTheme(val background: String = "#F7F7F7", val lineColor: String = "#111111", val textColor: String = "#111111", val titleColor: String = "#000000", val id: String = UUID.randomUUID().toString())

class BarThemeLite(background: String = "url(#condensedLite)", lineColor: String="#E4D0D0", textColor: String="#000000", titleColor: String="#000000"): BarTheme(background, lineColor, textColor, titleColor)
class BarThemeDark(background: String = "url(#condensedDark)",  lineColor: String = "#fcfcfc",  textColor: String = "#ABB2BF", titleColor: String = "#FCFCFC"): BarTheme(background, lineColor, textColor, titleColor)


