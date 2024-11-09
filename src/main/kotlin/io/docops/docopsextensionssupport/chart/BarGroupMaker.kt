package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.releasestrategy.gradientColorFromColor
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.support.gradientFromColor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class BarGroupMaker {

    private var fontColor = "#fcfcfc"
    fun makeBar(barGroup: BarGroup): String {
        fontColor = determineTextColor(barGroup.display.baseColor)
        val sb = StringBuilder()
        sb.append(makeHead(barGroup))
        sb.append(makeDefs(makeGradient(barDisplay = barGroup.display), barGroup=barGroup))
        sb.append(addGrid(barGroup))
        sb.append(makeTitle(barGroup))
        sb.append(makeXLabel(barGroup))
        sb.append(makeYLabel(barGroup))
        sb.append(makeXLine(barGroup))
        sb.append(makeYLine(barGroup))
        var startX = 40.0
        val elements = StringBuilder()
        barGroup.groups.forEach { group ->
            val added = addGroup(barGroup, group, startX)
            startX += group.series.size * 26.0 + 15
            elements.append(added)
        }

        sb.append("<g transform='translate(${(barGroup.calcWidth() - startX) / 2},0)'>")
        sb.append(elements.toString())
        sb.append("</g>")
        sb.append(addTicks(barGroup))
        sb.append(addLegend(startX + ((barGroup.calcWidth() - startX)/2), barGroup))
        sb.append(end())
        return sb.toString()
    }

    private fun addLegend(d: Double, group: BarGroup): String {
        val sb = StringBuilder()
        val distinct = group.legendLabel().distinct()
        val h = distinct.size * 12
        val w = group.calcWidth() - d - 10
        sb.append("""<rect x="${d-10}" y="45" width="$w" height="$h"  fill="#cccccc"/> """)
        var y = 52
        distinct.forEachIndexed { index, item ->
            val color = "url(#defColor_$index)"
            sb.append("""<rect x="${d-5}" y="$y" width="8" height="8" fill="$color"/>""")
            y+=10
        }
        sb.append("""<text x="$d" y="49" style="font-family: Arial,Helvetica, sans-serif; fill: #111111; font-size:10px;">""")
        distinct.forEachIndexed{ i, item ->
            sb.append("""<tspan x="${d+5}" dy="10">$item</tspan>""")
        }
        sb.append("</text>")
        return sb.toString()
    }

    private fun addGroup(barGroup: BarGroup, added: Group, startX: Double): String {
        val sb = StringBuilder()
        var counter = startX
        added.series.forEachIndexed { index, series ->
            val per = barGroup.scaleUp(series.value)
            val color = "url(#defColor_$index)"
           //println("${series.value} -> $per -> ${500-per}")
            sb.append("""<rect class="bar" x="$counter" y="${498 - per}" height="$per" width="24" fill="$color" style="stroke: #fcfcfc;"/>""")
            if(series.value > 0) {
                sb.append("""<text x="${counter + 12}" y="${500 - per - 4}" style="${barGroup.display.barFontValueStyle}; fill: $fontColor; text-anchor: middle;">${barGroup.valueFmt(series.value)}</text>""")
            }
            //sb.append("""<text x="-490" y="${counter + 15}" transform="rotate(270)" style="${barGroup.display.barSeriesLabelFontStyle}">${series.label}</text>""")
            counter += 26.0
        }
        val textX = startX + (added.series.size / 2 * 26.0)
        sb.append(makeSeriesLabel(textX, 500.0, added.label, barGroup))
        //sb.append("""<text x="$textX" y="512" style="${barGroup.display.barSeriesFontStyle}">${added.label}</text>""")
        return sb.toString()

    }

    private fun makeSeriesLabel(x: Double, y: Double, label: String, barGroup: BarGroup): String {
        val sb = StringBuilder()
        sb.append("""<text x="$x" y="$y" style="${barGroup.display.barSeriesFontStyle}; fill: $fontColor;" >""")
        val str = label.split(" ")
        str.forEachIndexed { index, s ->
            sb.append("<tspan x='$x' dy='10' style=\"${barGroup.display.barSeriesFontStyle}; fill: $fontColor;\">$s</tspan>")
        }
        sb.append("</text>")
        return sb.toString()
    }

    private fun addTicks( barGroup: BarGroup): String {
        val sb = StringBuilder()

        val nice =barGroup.ticks()
        val minV = nice.getNiceMin()
        val maxV = nice.getNiceMax()
        val tickSpacing = nice.getTickSpacing()
        var i = minV
        while(i < maxV ) {
            val y = 500 - barGroup.scaleUp(i)
            sb.append("""
     <line x1="40" x2="48" y1="$y" y2="$y" stroke="${barGroup.display.lineColor}" stroke-width="3"/>
    <text x="35" y="${y+3}" text-anchor="end" style="font-family: Arial,Helvetica, sans-serif; fill: $fontColor; font-size:10px; text-anchor:end">${barGroup.valueFmt(i)}</text>
            """.trimIndent())

            i+=tickSpacing
        }
        return sb.toString()
    }
    private fun makeTitle(barGroup: BarGroup): String {
        return """<text x="${barGroup.calcWidth()/2}" y="20" style="${barGroup.display.titleStyle}; fill: $fontColor;">${barGroup.title}</text>"""
    }
    private fun makeXLabel(barGroup: BarGroup): String {
        return """<text x="${barGroup.calcWidth()/2}" y="536" style="${barGroup.display.xLabelStyle}; fill: $fontColor;" >${barGroup.xLabel}</text>"""
    }
    private fun makeYLabel(barGroup: BarGroup): String {
        return """<text x="-270" y="18" style="${barGroup.display.yLabelStyle}; fill: $fontColor;" transform="rotate(270)">${barGroup.yLabel}</text>"""
    }
    private fun end() = "</svg>"
    private fun makeHead(barGroup: BarGroup): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="${barGroup.calcWidth() * barGroup.display.scale}" height="${540 * barGroup.display.scale}" viewBox="0 0 ${barGroup.calcWidth()} 540" xmlns="http://www.w3.org/2000/svg">
        """.trimIndent()
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

    private fun makeDefs(gradients: String, barGroup: BarGroup): String {
        val defGrad = StringBuilder()
        STUNNINGPIE.forEachIndexed { idx, item->
            val gradient = gradientFromColor(item)
            defGrad.append("""
                <linearGradient id="defColor_$idx" x2="100%" y2="0%">
            <stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/>
        </linearGradient>
            """.trimIndent())
        }

        val backColor = gradientColorFromColor(barGroup.display.baseColor, "backGrad_${barGroup.id}")

        return """<defs>
            $defGrad
             $backColor
                 <linearGradient id="grad1" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="#f6f6f5"/>
                <stop class="stop2" offset="50%" stop-color="#f2f1f0"/>
                <stop class="stop3" offset="100%" stop-color="#EEEDEB"/>
                </linearGradient>
                   $gradients
                    <pattern id="tenthGrid" width="10" height="10" patternUnits="userSpaceOnUse">
                        <path d="M 10 0 L 0 0 0 10" fill="none" stroke="silver" stroke-width="0.5"/>
                    </pattern>
                    <pattern id="grid" width="100" height="100" patternUnits="userSpaceOnUse">
                        <rect width="100" height="100" fill="url(#tenthGrid)"/>
                        <path d="M 100 0 L 0 0 0 100" fill="none" stroke="gray" stroke-width="1"/>
                    </pattern>
                    <filter id="Bevel" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                        <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                        <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10"
                                            result="specOut" lighting-color="white">
                            <fePointLight x="-5000" y="-10000" z="20000"/>
                        </feSpecularLighting>
                        <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                        <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                                     result="litPaint"/>
                    </filter>
                    <filter id="Bevel3" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                        <feGaussianBlur in="SourceAlpha" stdDeviation="0.2" result="blur"/>
                        <feSpecularLighting in="blur" surfaceScale="10" specularConstant="3.5" specularExponent="10"
                                            result="specOut" lighting-color="#ffffff">
                            <fePointLight x="-5000" y="-10000" z="0000"/>
                        </feSpecularLighting>
                        <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                        <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                                     result="litPaint"/>
                    </filter>
                    <style>
                    .bar:hover {
                        filter: grayscale(100%) sepia(100%);
                    }
                     </style>
                </defs>"""
    }

    private fun addGrid(barGroup: BarGroup): String {
        val maxHeight = 540
        val maxWidth = barGroup.calcWidth()
        val maxData = barGroup.maxData() + 100
        val oneUnit = maxHeight / maxData
        val xGap = maxWidth / (barGroup.maxGroup().series.size + 1)
        val yGap = maxHeight / (barGroup.maxGroup().series.size + 1)
        var num = xGap
        var num2 = yGap
        val elements = StringBuilder()
        elements.append("""<rect width='100%' height='100%' fill='url(#backGrad_${barGroup.id})' stroke="#aaaaaa" stroke-width="1"/>""")

        barGroup.maxGroup().series.forEach {
            elements.append("""<polyline points="$num,0 $num,$maxHeight" style="stroke: #aaaaaa"/>""")
            elements.append("""<polyline points="0,$num2 $maxWidth,$num2" style="stroke: #aaaaaa"/>""")
            num += xGap
            num2 += yGap
        }

        return elements.toString()
    }

    private fun makeXLine(barGroup: BarGroup): String {
        return """<line x1="50" x2="${barGroup.calcWidth() - 10}" y1="500" y2="500" stroke="${barGroup.display.lineColor}" stroke-width="3"/>
            <g transform="translate(${barGroup.calcWidth() - 10},497.5)">
            <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="3" stroke="${barGroup.display.lineColor}"/>
            </g>
        """.trimMargin()
    }

    private fun makeYLine(barGroup: BarGroup): String {
        return """<line x1="50" x2="50" y1="12" y2="500" stroke="${barGroup.display.lineColor}" stroke-width="3"/>
            <g transform="translate(47.5,16), rotate(-90)">
            <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="3" stroke="${barGroup.display.lineColor}"/>
            </g>
        """.trimMargin()
    }
}


fun createBarGroupTestData(): BarGroup {
    val seriesA1 = Series(label = "Q1", value = 5000.0)
    val seriesA2 = Series(label = "Q2", value = 7000.0)
    val seriesA3 = Series(label = "Q3", value = 8000.0)
    val seriesA4 = Series(label = "Q4", value = 6000.0)

    val seriesB1 = Series(label = "Q1", value = 6000.0)
    val seriesB2 = Series(label = "Q2", value = 8000.0)
    val seriesB3 = Series(label = "Q3", value = 7000.0)
    val seriesB4 = Series(label = "Q4", value = 9000.0)

    val seriesC1 = Series(label = "Q1", value = 6000.0)
    val seriesC2 = Series(label = "Q2", value = 8000.0)
    val seriesC3 = Series(label = "Q3", value = 7000.0)
    val seriesC4 = Series(label = "Q4", value = 9000.0)

    val seriesD1 = Series(label = "Q1", value = 6000.0)
    val seriesD2 = Series(label = "Q2", value = 8000.0)
    val seriesD3 = Series(label = "Q3", value = 7000.0)
    val seriesD4 = Series(label = "Q4", value = 9000.0)

    val seriesE1 = Series(label = "Q1", value = 6000.0)
    val seriesE2 = Series(label = "Q2", value = 8000.0)
    val seriesE3 = Series(label = "Q3", value = 7000.0)
    val seriesE4 = Series(label = "Q4", value = 9000.0)


    val groupA = Group(
        label = "Product A",
        series = mutableListOf(seriesA1, seriesA2, seriesA3, seriesA4)
    )

    val groupB = Group(
        label = "Product B",
        series = mutableListOf(seriesB1, seriesB2, seriesB3, seriesB4)
    )

    val groupC = Group(
        label = "Product C",
        series = mutableListOf(seriesC1, seriesC2, seriesC3, seriesC4)
    )

    val groupD = Group(label = "Product D", series = mutableListOf(seriesD1, seriesD2, seriesD3, seriesD4))

    val groupE = Group(label = "Product E", series = mutableListOf(seriesE1, seriesE2, seriesE3, seriesE4))
    val barGroup = BarGroup(
        title = "Annual Product Sales Report",
        yLabel = "Sales (USD)",
        xLabel = "Quarters",
        groups = mutableListOf(groupA, groupB, groupC, groupD, groupE),
        display = BarGroupDisplay(lineColor = "#921A40", baseColor = "#F3EDED", barFontValueStyle = "font-family: Arial,Helvetica, sans-serif; font-size:9px;", scale = 1.0)
    )

    return barGroup
}

fun main() {
    val barGroupTestData = createBarGroupTestData()


    val str = Json.encodeToString(barGroupTestData)
    println(str)
    val svg = BarGroupMaker().makeBar(barGroupTestData)
    val outfile2 = File("gen/groupbar.svg")
    outfile2.writeBytes(svg.toByteArray())
}