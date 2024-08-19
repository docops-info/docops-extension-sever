package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.support.gradientFromColor
import org.apache.catalina.manager.JspHelper.escapeXml
import java.io.File

class BarMaker {

    fun makeBar(bar: Bar) : String {

        val sb = StringBuilder()
        sb.append(makeHead(bar))
        sb.append(makeDefs(bar))
        if(bar.display.showGrid) {
            sb.append(addGrid(bar = bar))
        }
        sb.append(addGroupStart())
        var startX: Int = 20
        var startY: Int = bar.calcLeftPadding()
        var incY = 60
        bar.series.forEachIndexed { index, barData ->
            sb.append(makeBarItem(index, barData, startX, startY, bar.seriesTotal(), bar))
            startY += incY
        }
        sb.append(endGroup(bar))
        sb.append(addTitle(bar))
        sb.append(end(bar))
        return sb.toString()
    }

    fun end(bar: Bar) = "</svg>"
    fun makeHead(bar: Bar): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="${bar.calcWidth()}" height="540" viewBox="0 0 ${bar.calcWidth()} 540" xmlns="http://www.w3.org/2000/svg">
        """.trimIndent()
    }

    fun makeBarItem(index: Int, barData: Series, startX: Int, startY: Int, total: Float, bar: Bar): String {
        val per = bar.weightedPercentage(barData, 512)
        return """
            <g transform="translate($startX,$startY)">
                <path class="bar" d="M 0,6 a 20,6 0,0,0 40 0 a 20,6 0,0,0 -40 0 l 0,$per a 20,6 0,0,0 40 0 l 0,-$per" fill="url(#linearGradient6)" transform="translate(0,35) rotate(-90)" style="background: conic-gradient(#655 40%, yellowgreen 0);"/>
                <text x="$per" y="22" style="font-family: Arial,Helvetica, sans-serif; fill: ${bar.display.barFontColor}; font-size:9px;" text-anchor="end" >${barData.value}</text>
                <text x="15" y="12" transform="rotate(90)" style="font-family: Arial,Helvetica, sans-serif; fill: #111111; font-size:10px; text-anchor: middle;" >${escapeXml(barData.label)}</text>
            </g>
        """.trimIndent()
    }

    fun addGroupStart() = """<g transform="translate(56,0)">
        <g transform="translate(60,500) rotate(-90) ">
    """.trimMargin()
    fun endGroup(bar: Bar) : String {
        val barY = bar.calcLeftPadding() - 15
        return """
            </g>
            <text x="-300" y="10" style="font-family: Arial,Helvetica, sans-serif; fill: #111111;text-anchor: middle;" transform="translate($barY, 10) rotate(-90)">${bar.yLabel}</text>
            <text x="220" y="520" style="font-family: Arial,Helvetica, sans-serif; fill: #111111;text-anchor: middle;">${bar.xLabel}</text>
        </g>"""
    }
    fun addTitle(bar: Bar) = """<text x="256" y="24" style="font-family: Arial,Helvetica, sans-serif; fill: #111111;text-anchor: middle; font-size: 24px;">${bar.title}</text>"""
    fun addGrid(bar: Bar) = """<rect width="${bar.calcWidth()}" height="540" fill="url(#grid)"/>"""
    fun makeDefs(bar: Bar) : String =
         """<defs>
                   ${makeGradient(bar.display)}
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


    private fun makeGradient(barDisplay: BarDisplay): String {
        val gradient1 = gradientFromColor(barDisplay.baseColor)
        return """
        <linearGradient id="linearGradient6" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${gradient1["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient1["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient1["color3"]}"/>
        </linearGradient>
        """.trimIndent()
    }
}

fun main() {
    val bar = Bar(title = "Berry Picking by Month 2024",
        yLabel = "Number of Sales",
        xLabel = "Month",
        series = mutableListOf(Series("Jan", 120.0f), Series("Feb", 334.0f)), display = BarDisplay(showGrid = true, baseColor = "#492E87", barFontColor = "#FFFFFF"))
    val svg = BarMaker().makeBar(bar)
    val outfile2 = File("gen/bars.svg")
    outfile2.writeBytes(svg.toByteArray())
}