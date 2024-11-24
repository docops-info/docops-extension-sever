package io.docops.docopsextensionssupport

import io.docops.docopsextensionssupport.chart.DonutSlice
import io.docops.docopsextensionssupport.chart.DonutSliceWithCommands
import io.docops.docopsextensionssupport.chart.PieSlice
import io.docops.docopsextensionssupport.chart.PieSlices
import io.docops.docopsextensionssupport.chart.STUNNINGPIE
import io.docops.docopsextensionssupport.chart.SliceDisplay
import io.docops.docopsextensionssupport.chart.toDonutSlices
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin


@SpringBootTest
class DocopsExtensionsSupportApplicationTests {

    @Test
    fun contextLoads() {
    }



    class CalculusHelper {
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
    val donutSlices = mutableListOf<PieSlice>(
        PieSlice("1", amount = 50.0, label = "Slice 1"),
        PieSlice("2", amount = 25.0, label = "Slice 2"),
        PieSlice("3", amount = 25.0, label = "Slice 3")

    )
    val slices = PieSlices(title = "Favorite Anime",mutableListOf(PieSlice(label= "Naruto", amount = 5.0),
        PieSlice(label = "Bleach", amount = 4.0),
        PieSlice(label = "One Piece", amount = 9.0),
        PieSlice(label = "One Punch Man", amount = 7.0),
        PieSlice(label = "My Hero Academia", amount = 6.0),
        PieSlice(label = "Demon Slayer", amount = 10.0),
    ), SliceDisplay(donut = true) )

    @Test
    fun makeDonut() {
        val helper = CalculusHelper()
        val commands = helper.getSlicesWithCommandsAndOffsets(slices.toDonutSlices(), 200.0, 400.0, 50.0)
        val sb = StringBuilder()
        sb.append("""<svg viewBox="0 0 400 400">""")
        commands.forEach {
            sb.append("""
                <path d="${it.commands}" fill="${it.color}" transform="rotate(${it.offset})" class="wedge" style="transform-origin: center;"/>
            """.trimIndent())
        }
        sb.append("</svg>")
        println(sb.toString())
    }

}

fun main() {
    val L = mutableListOf(
        """<?xml version="1.0" encoding="UTF-8" standalone="no"?>""",
        """<svg width="500" height="400" xmlns="http://www.w3.org/2000/svg" version="1.1"> """,
        """ <defs>
            <style>
            .pie:hover {
                filter: grayscale(100%) sepia(100%);
            }
            </style>
            </defs>"""
    )

    val newValues = mutableListOf<Float>(1.0f, 2.0f, 2.2f, 1.4f, 3.1f)

    val total = newValues.sum()
    val radius = 150
    val startx = 200
    val starty = 200
    var lastx = radius
    var lasty = 0
    var ykey = 40
    val colors = STUNNINGPIE
    val borderColor = "#fcfcfc"

    var seg = 0.0
    for ((i, n) in newValues.withIndex()) {
        val arc = if ((n / total * 360) > 180) "1" else "0"
        seg += (n / total * 360)
        val radSeg = toRadians(seg)
        val nextx = (cos(radSeg) * radius).toInt()
        val nexty = (sin(radSeg) * radius).toInt()

        L.add("""<path class="pie" d="M $startx,$starty l $lastx,${-lasty} a150,150 0 $arc,0 ${nextx - lastx},${-(nexty - lasty)} z" """)
        L.add("""fill="${colors[i]}" stroke="$borderColor" stroke-width="0.5" stroke-linejoin="round" />""")
        L.add("""<rect x="375" y="$ykey" width="40" height="30" fill="${colors[i]}" stroke="black" stroke-width="0.5"/>""")
        ykey += 35
        lastx = nextx
        lasty = nexty
    }
    L.add("</svg>")

    File("gen/pienew.svg").writeText(L.joinToString(""))


}
// Placeholder functions for Scribus functionalities