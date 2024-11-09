package io.docops.docopsextensionssupport

import io.docops.docopsextensionssupport.chart.STUNNINGPIE
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