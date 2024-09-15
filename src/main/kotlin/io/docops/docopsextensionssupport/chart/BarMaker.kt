package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.support.gradientFromColor
import org.apache.catalina.manager.JspHelper.escapeXml
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BarMaker {

    fun makeBar(bar: Bar) : String {

        val sb = StringBuilder()
        sb.append(makeHead(bar))
        sb.append(makeDefs(bar, itemGradients(bar)))
        if(bar.display.showGrid) {
            sb.append(addGrid(bar = bar))
        }
        sb.append(addGroupStart(bar))
        var startX: Int = 1
        var startY: Int = bar.calcLeftPadding()
        var incY = 42
        var minY = max(0, startY)
        if(bar.display.type == "C") {
            incY = 44
        }
        bar.ticks()
        bar.series.forEachIndexed { index, barData ->
            sb.append(makeBarItem(index, barData, startX, startY, bar.seriesTotal(), bar))
            startY += incY
            minY = min(minY, startY)
        }
        sb.append(endGroup(bar))
        sb.append(addTitle(bar))
        sb.append(addTicks(bar))
        sb.append(end(bar))
        return sb.toString()
    }

    private fun end(bar: Bar) = "</svg>"
    private fun makeHead(bar: Bar): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="${bar.calcWidth()}" height="540" viewBox="0 0 ${bar.calcWidth()} 540" xmlns="http://www.w3.org/2000/svg">
        """.trimIndent()
    }
    private fun addTicks( bar: Bar): String {
        val sb = StringBuilder()

        val nice =bar.ticks()
        val minV = nice.getNiceMin()
        val maxV = nice.getNiceMax()
        val tickSpacing = nice.getTickSpacing()
        var i = minV
        while(i < maxV ) {
            val y = 500 - bar.scaleUp(i)
            sb.append("""
     <line x1="24" x2="28" y1="$y" y2="$y" stroke="gold" stroke-width="3"/>
    <text x="20" y="${y+3}" text-anchor="end" style="font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:10px; text-anchor:end">${bar.valueFmt(i)}</text>
            """.trimIndent())

            i+=tickSpacing
        }
        return sb.toString()
    }
    private fun makeBarItem(index: Int, barData: Series, startX: Int, startY: Int, total: Double, bar: Bar): String {
       // val per = bar.weightedPercentage(barData, 512)
        val per = bar.scaleUp(barData.value)
        var displayGradId =  bar.display.id
        var fontColor = bar.display.barFontColor
        if(barData.itemDisplay != null) {
            displayGradId = barData.itemDisplay.id
            fontColor = barData.itemDisplay.barFontColor
        }
        var labelY = 0
        var shape = ""
        when (bar.display.type) {
            "C" -> {
                labelY = 18
                shape = """<path class="bar" d="M 0,6 a 20,6 0,0,0 40 0 a 20,6 0,0,0 -40 0 l 0,$per a 20,6 0,0,0 40 0 l 0,-$per" fill="url(#linearGradient_${displayGradId})" transform="translate(0,35) rotate(-90)" style="background: conic-gradient(#655 40%, yellowgreen 0);"/>"""
            }
            "R" -> {
                labelY = 19
                shape = """<rect class="bar" x="0" y="0" height="$per" width="38" fill="url(#linearGradient_${displayGradId})" transform="translate(0,35) rotate(-90)"/>"""
            }
        }

        return """
            <g transform="translate($startX,$startY)">
                $shape
               
                <text x="${per-4}" y="$labelY" style="font-family: Arial,Helvetica, sans-serif; fill: ${fontColor}; font-size:9px;" text-anchor="end" >${barData.value.toInt()}</text>
                <text x="10" y="19"  style="font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:10px; text-anchor: start;" >${escapeXml(barData.label)}</text>
            </g>
        """.trimIndent()
    }

    private fun addGroupStart(bar: Bar) : String {
        var x = 0
        if(bar.calcWidth() > 512)
        {
            x = 56
        }
         return """<g transform="translate($x,0)">
            <g transform="translate(${bar.innerX()},500) rotate(-90) ">
        """.trimMargin()
    }
    private fun endGroup(bar: Bar) : String {
        val center = bar.centerWidth()
        val barY = bar.calcLeftPadding() - 15
        return """
            </g>
        </g>
        <text x="250" y="-10" style="font-family: Arial,Helvetica, sans-serif; font-size:10px; fill: #fcfcfc;text-anchor: middle;" transform="rotate(90)">${bar.yLabel}</text>
        <text x="$center" y="520" style="font-family: Arial,Helvetica, sans-serif; font-size:10px; fill: #fcfcfc;text-anchor: middle;">${bar.xLabel}</text>
        """
    }
    private fun addTitle(bar: Bar): String {
        val center = bar.centerWidth()
        return """<text x="$center" y="24" style="font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc;text-anchor: middle; font-size: 24px;">${bar.title}</text>"""
    }
    private fun addGrid(bar: Bar) : String
    {
        val maxHeight = 540
        val maxWidth = bar.calcWidth()
        val maxData = bar.series.maxOf { it.value } + 100
        val oneUnit = maxHeight / maxData
        val xGap = maxWidth / (bar.series.size + 1)
        val yGap = maxHeight / (bar.series.size + 1)
        var num = xGap
        var num2 = yGap
        val elements = StringBuilder()
        elements.append("""<rect width='100%' height='100%' fill='url(#backGrad3)' stroke="#aaaaaa" stroke-width="1"/>""")

       /* bar.series.forEach {
            elements.append("""
                 <line x1="20" x2="30" y1="${it.value}" y2="${it.value}" stroke-width="3" stroke="gold"/>
            """)
            //elements.append("""<text x="20" y="${it-5}" style="font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:10px; text-anchor: middle;">${it.toInt()}</text>""")
        }*/
        bar.series.forEach {
            val per = bar.scaleUp(it.value)
            //ticks
           /* elements.append("""
                 <line x1="20" x2="30" y1="${500-per}" y2="${500-per}" stroke-width="3" stroke="gold"/>
                <text x="20" y="${500-per-5}" style="font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:10px; text-anchor: middle;">${it.valueFmt()}</text>
            """.trimIndent())*/
            elements.append("""<polyline points="$num,0 $num,$maxHeight" style="stroke: #aaaaaa"/>""")
            elements.append("""<polyline points="0,$num2 $maxWidth,$num2" style="stroke: #aaaaaa"/>""")
            num += xGap
            num2 += yGap
        }
        elements.append("""
            <line x1="30" x2="${bar.calcWidth()}" y1="500" y2="500" stroke="gold" stroke-width="3"/>
            <line x1="30" x2="30" y1="12" y2="501" stroke="gold" stroke-width="3"/>
        """.trimIndent())
        return elements.toString()
    }
    private fun makeDefs(bar: Bar, gradients: String) : String =
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


    private fun itemGradients(bar: Bar): String {
        val sb = StringBuilder()
        bar.series.forEach {
            if(it.itemDisplay != null) {
                sb.append(makeGradient(it.itemDisplay))
            } else {
                sb.append(makeGradient(bar.display))
            }
        }
        return sb.toString()
    }
    private fun makeGradient(barDisplay: BarDisplay): String {
        val gradient1 = gradientFromColor(barDisplay.baseColor)
        return """
        <linearGradient id="linearGradient_${barDisplay.id}" x2="0%" y2="100%">
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
        series = mutableListOf(Series("Jan", 120.0), Series("Feb", 334.0), Series("Mar", 455.0), Series("Apr", 244.0),
            Series("May", 256.0), Series("Jun", 223.0), Series("Jul", 345.0), Series("Aug", 356.0), Series("Sep", 467.0),
            Series("Oct", 345.0), Series("Nov", 356.0), Series("Dec", 467.0)),
        display = BarDisplay(showGrid = true, baseColor = "#492E87", barFontColor = "#FFFFFF"))
    val svg = BarMaker().makeBar(bar)
    val outfile2 = File("gen/bars.svg")
    outfile2.writeBytes(svg.toByteArray())
}