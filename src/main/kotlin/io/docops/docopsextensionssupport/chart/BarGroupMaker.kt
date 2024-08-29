package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.support.gradientFromColor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class BarGroupMaker {

    fun makeBar(barGroup: BarGroup): String {
        val sb = StringBuilder()
        sb.append(makeHead(barGroup))
        sb.append(makeDefs(makeGradient(barDisplay = barGroup.display)))
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
        sb.append("<g transform='translate(${(470 - startX) / 2},0)'>")
        sb.append(elements.toString())
        sb.append("</g>")
        sb.append(end())
        return sb.toString()
    }

    private fun addGroup(barGroup: BarGroup, added: Group, startX: Double): String {
        var displayGradId = barGroup.display.id
        val sb = StringBuilder()
        var counter = startX
        added.series.forEachIndexed { index, series ->
            val per = barGroup.scaleUp(series.value)
            sb.append("""<rect class="bar" x="$counter" y="${500 - per}" height="$per" width="24" fill="url(#linearGradient_${displayGradId})" style="stroke: #fcfcfc;"/>""")
            if(series.value > 0) {
                sb.append(
                    """<text x="${counter + 4}" y="${500 - per - 2}" style="${barGroup.display.barFontValueStyle}">${
                        barGroup.valueFmt(
                            series.value
                        )
                    }</text>"""
                )
            }
            sb.append("""<text x="-490" y="${counter + 15}" transform="rotate(270)" style="${barGroup.display.barSeriesLabelFontStyle}">${series.label}</text>""")
            counter += 26.0
        }
        val textX = startX + (added.series.size / 2 * 26.0)
        sb.append("""<text x="$textX" y="512" style="${barGroup.display.barSeriesFontStyle}">${added.label}</text>""")

        return sb.toString()

    }

    private fun makeTitle(barGroup: BarGroup): String {
        return """<text x="${barGroup.calcWidth()/2}" y="20" style="${barGroup.display.titleStyle}">${barGroup.title}</text>"""
    }
    private fun makeXLabel(barGroup: BarGroup): String {
        return """<text x="${barGroup.calcWidth()/2}" y="526" style="${barGroup.display.xLabelStyle}">${barGroup.xLabel}</text>"""
    }
    private fun makeYLabel(barGroup: BarGroup): String {
        return """<text x="-270" y="18" style="${barGroup.display.yLabelStyle}" transform="rotate(270)">${barGroup.yLabel}</text>"""
    }
    private fun end() = "</svg>"
    private fun makeHead(barGroup: BarGroup): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="${barGroup.calcWidth()}" height="540" viewBox="0 0 ${barGroup.calcWidth()} 540" xmlns="http://www.w3.org/2000/svg">
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

    private fun makeDefs(gradients: String): String =
        """<defs>
             <linearGradient id="backGrad3" x2="0%" y2="100%">
                 <stop class="stop1" offset="0%" stop-color="#9ea1a8"/>
                <stop class="stop2" offset="50%" stop-color="#6d727c"/>
                <stop class="stop3" offset="100%" stop-color="#3d4451"/>
            </linearGradient>
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
        elements.append("""<rect width='100%' height='100%' fill='url(#backGrad3)' stroke="#aaaaaa" stroke-width="1"/>""")

        barGroup.maxGroup().series.forEach {
            elements.append("""<polyline points="$num,0 $num,$maxHeight" style="stroke: #aaaaaa"/>""")
            elements.append("""<polyline points="0,$num2 $maxWidth,$num2" style="stroke: #aaaaaa"/>""")
            num += xGap
            num2 += yGap
        }

        return elements.toString()
    }

    private fun makeXLine(barGroup: BarGroup): String {
        return """<line x1="30" x2="${barGroup.calcWidth() - 10}" y1="500" y2="500" stroke="${barGroup.display.lineColor}" stroke-width="3"/>"""
    }

    private fun makeYLine(barGroup: BarGroup): String {
        return """<line x1="30" x2="30" y1="12" y2="500" stroke="${barGroup.display.lineColor}" stroke-width="3"/>"""
    }
}


fun createBarGroupTestData(): BarGroup {
    val seriesA1 = Series(label = "Product A Q1", value = 5000.0)
    val seriesA2 = Series(label = "Product A Q2", value = 7000.0)
    val seriesA3 = Series(label = "Product A Q3", value = 8000.0)
    val seriesA4 = Series(label = "Product A Q4", value = 6000.0)

    val seriesB1 = Series(label = "Product B Q1", value = 6000.0)
    val seriesB2 = Series(label = "Product B Q2", value = 8000.0)
    val seriesB3 = Series(label = "Product B Q3", value = 7000.0)
    val seriesB4 = Series(label = "Product B Q4", value = 9000.0)

    val seriesC1 = Series(label = "Product B Q1", value = 6000.0)
    val seriesC2 = Series(label = "Product B Q2", value = 8000.0)
    val seriesC3 = Series(label = "Product B Q3", value = 7000.0)
    val seriesC4 = Series(label = "Product B Q4", value = 9000.0)

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
    val barGroup = BarGroup(
        title = "Annual Product Sales Report",
        yLabel = "Sales (USD)",
        xLabel = "Quarters",
        groups = mutableListOf(groupA, groupB, groupC),
        display = BarGroupDisplay(lineColor = "#FFBB5C", baseColor = "#e60049", barFontValueStyle = "font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:9px;")
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