package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.svgsupport.ToolTip
import io.docops.docopsextensionssupport.svgsupport.ToolTipConfig
import io.docops.docopsextensionssupport.svgsupport.textWidth
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin

class DonutMaker : PieSliceMaker(){

    fun makeDonut(pieSlices: PieSlices) : String {
        val sb= StringBuffer()
        sb.append(startSvg(pieSlices))
        sb.append(makeDefs(pieSlices))
        val donuts = pieSlices.toDonutSlices()
        sb.append(createDonutCommands(donuts))
        sb.append(addLegend(donuts))
        sb.append(endSvg())
        return sb.toString()
    }



    private fun createDonutCommands(slices: List<DonutSlice>): StringBuilder {
        val viewBox = 300.0
        val commands = getSlicesWithCommandsAndOffsets(slices, 120.0, viewBox, 50.0)
        val sb = StringBuilder()
        sb.append("""<g transform="translate(10, 10)">""")
        sb.append("""<svg viewBox="0 0 $viewBox $viewBox">""")
        val toolTipGen = ToolTip()

        commands.forEachIndexed { index, it ->
            val cMap = SVGColor(STUNNINGPIE[index])
            val tipWidth = it.label.textWidth("Helvetica", 12) + 24
            sb.append("""
                <path d="${it.commands}" fill="${it.color}" transform="rotate(${it.offset})" class="pie" style="transform-origin: center; cursor: pointer;" onmouseover="showText('text_${it.id}')" onmouseout="hideText('text_${it.id}')"/>
                <g transform="translate(150,175)" visibility="hidden" id="text_${it.id}">
                <path d="${toolTipGen.getTopToolTip(ToolTipConfig(width = tipWidth, height = 50))}" fill="url(#defColor_$index)" stroke="${cMap.darker()}" stroke-width="3" opacity="0.8" /> 
                <text x="0" y="-50" text-anchor="middle" style="fill:#111111; font-size:12px;font-family: Arial, Helvetica, sans-serif;">${it.label}</text>
                <text x="0" y="-35" text-anchor="middle" style="fill:#111111; font-size:12px;font-family: Arial, Helvetica, sans-serif;">${it.amount}</text>
                <text x="0" y="-20" text-anchor="middle" style="fill:#111111; font-size:12px;font-family: Arial, Helvetica, sans-serif;">${slices[index].valueFmt(it.percent)} %</text>
                </g>
            """.trimIndent())
        }
        sb.append("</svg>")
        sb.append("</g>")
        return sb
    }

    private fun addLegend(donuts: MutableList<DonutSlice>): StringBuilder {
        val sb = StringBuilder()
        sb.append("<g transform='translate(210,460)'>")
        sb.append("""<text text-anchor="middle" x="0" y="20" style="font-size: 10px; font-family: Arial, Helvetica, sans-serif; cursor: pointer;">""")
        donuts.forEachIndexed { index, donutSlice ->

            sb.append("""<tspan x="0" dy="14" fill="url(#defColor_$index)" onmouseover="showText('text_${donutSlice.id}')" onmouseout="hideText('text_${donutSlice.id}')">${donutSlice.label}(${donutSlice.valueFmt(donutSlice.amount)}) | ${donutSlice.valueFmt(donutSlice.percent)}%</tspan>""")
        }
        sb.append("</text>")
        sb.append("</g>")
        return sb
    }
    fun getSlicesWithCommandsAndOffsets(
        donutSlices: List<DonutSlice>,
        radius: Double,
        svgSize: Double,
        borderSize: Double
    ): List<DonutSliceWithCommands> {
        var previousPercent = 0.0
        return donutSlices.map { slice ->
            val sliceWithCommands = DonutSliceWithCommands(
                id = slice.id,
                percent = slice.percent,
                amount = slice.amount,
                color = slice.color,
                label = slice.label,
                commands = getSliceCommands(slice, radius, svgSize, borderSize),
                offset = previousPercent * 3.6 * -1
            )
            previousPercent += slice.percent
            sliceWithCommands
        }
    }

    fun getSliceCommands(
        donutSlice: DonutSlice,
        radius: Double,
        svgSize: Double,
        borderSize: Double
    ): String {
        val degrees = percentToDegrees(donutSlice.percent)
        val longPathFlag = if (degrees > 180) 1 else 0
        val innerRadius = radius - borderSize

        val commands = mutableListOf<String>()
        commands.add("M ${svgSize / 2 + radius} ${svgSize / 2}")
        commands.add(
            "A $radius $radius 0 $longPathFlag 0 ${getCoordFromDegrees(degrees, radius, svgSize)}"
        )
        commands.add(
            "L ${getCoordFromDegrees(degrees, innerRadius, svgSize)}"
        )
        commands.add(
            "A $innerRadius $innerRadius 0 $longPathFlag 1 ${svgSize / 2 + innerRadius} ${svgSize / 2}"
        )
        return commands.joinToString(" ")
    }

    fun getCoordFromDegrees(angle: Double, radius: Double, svgSize: Double): String {
        val x = cos(toRadians(angle))
        val y = sin(toRadians(angle))
        val coordX = x * radius + svgSize / 2
        val coordY = y * -radius + svgSize / 2
        return "$coordX $coordY"
    }

    fun percentToDegrees(percent: Double): Double {
        return percent * 3.6
    }
}


data class DonutSliceWithCommands(
    val id: String,
    val percent: Double,
    val amount: Double,
    val color: String,
    val label: String,
    val offset: Double,
    val commands: String
)




