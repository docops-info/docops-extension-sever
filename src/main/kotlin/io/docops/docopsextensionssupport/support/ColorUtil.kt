/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.support

import kotlinx.serialization.Serializable
import java.awt.Color
import java.util.*
import kotlin.math.floor

fun getRandomColorHex(): String {
    val random = Random()
    val rgb = 0xff + 1
    val colors = IntArray(2)
    val a = 256
    val r1 = floor(Math.random() * rgb).toInt()
    val r2 = floor(Math.random() * rgb).toInt()
    val r3 = floor(Math.random() * rgb).toInt()
    return String.format("#%02x%02x%02x", r1, r2, r3)
}

fun gradientFromColor(color: String): Map<String, String> {
    val m = generateGradient(color)
    return mapOf("color1" to m["lighter"]!!, "color2" to m["original"]!!, "color3" to m["darker"]!!)
}
fun svgGradient(color: String, id: String): String {
    val map = gradientFromColor(color)
    return """
        <linearGradient id="$id" x2="0%" y2="100%">
            <stop offset="0%" style="stop-color:${map["color1"]}"/>
            <stop offset="50%" style="stop-color:${map["color2"]}"/>
            <stop offset="100%" style="stop-color:${map["color3"]}"/>
        </linearGradient>
    """.trimIndent()
}
private fun shade(color: Color): String {
    val rs: Double = color.red * 0.50
    val gs = color.green * 0.50
    val bs = color.blue * 0.50
    return  "#${rs.toInt().toString(16)}${gs.toInt().toString(16)}${bs.toInt().toString(16)}"
}
private fun tint(color: Color, factor: Double): String {
    val rs = color.red + (factor * (255 - color.red))
    val gs = color.green + (factor * (255 - color.green))
    val bs = color.blue + (factor * (255 - color.blue))
    return  "#${rs.toInt().toString(16)}${gs.toInt().toString(16)}${bs.toInt().toString(16)}"
}

fun randomColor(): Int {
    return (Math.random() * 16777215).toInt() or (0xFF shl 24)
}

fun hexToRgb(hex: String): Map<String, Int> {
    try {
        val r = hex.substring(1, 3).toInt(16)
        val g = hex.substring(3, 5).toInt(16)
        val b = hex.substring(5, 7).toInt(16)
        return mapOf("r" to r, "g" to g, "b" to b)
    } catch (e: Exception) {
        println(hex)
        throw e
    }

}

fun rgbToHex(r: Int, g: Int, b: Int): String {
    return "#" + ((1 shl 24) + (r shl 16) + (g shl 8) + b).toString(16).substring(1)
}
fun adjustColor(color: Int, percentage: Double): Int {
    return (color * (1 + percentage)).toInt().coerceIn(0, 255)
}

fun generateGradient(hexColor: String): Map<String, String> {
    val rgbColor = hexToRgb(hexColor)
    val lighterColor = mapOf(
        "r" to adjustColor(rgbColor["r"]!!, 0.2),
        "g" to adjustColor(rgbColor["g"]!!, 0.2),
        "b" to adjustColor(rgbColor["b"]!!, 0.2)
    )
    val darkerColor = mapOf(
        "r" to adjustColor(rgbColor["r"]!!, -0.2),
        "g" to adjustColor(rgbColor["g"]!!, -0.2),
        "b" to adjustColor(rgbColor["b"]!!, -0.2)
    )
    return mapOf(
        "original" to hexColor,
        "lighter" to rgbToHex(lighterColor["r"]!!, lighterColor["g"]!!, lighterColor["b"]!!),
        "darker" to rgbToHex(darkerColor["r"]!!, darkerColor["g"]!!, darkerColor["b"]!!)
    )
}


fun calculateLuminance(rgb: Triple<Int, Int, Int>): Double {
    val (r, g, b) = rgb
    val rNorm = r / 255.0
    val gNorm = g / 255.0
    val bNorm = b / 255.0
    return 0.2126 * rNorm + 0.7152 * gNorm + 0.0722 * bNorm
}

fun determineTextColor(hexColor: String): String {
    val rgb = hexToRgb(hexColor)
    val luminance = calculateLuminance(Triple(rgb["r"]!!, rgb["g"]!!, rgb["b"]!!))
    return if (luminance < 0.5) "#FCFCFC" else "#000000"
}

@Serializable
class SVGColor(
    val color: String, 
    val id: String = UUID.randomUUID().toString(),
    val gradientAngle: String = "to bottom", // Options: "to bottom", "to right", "to bottom right", etc.
    val opacityStart: Double = 1.0,
    val opacityMiddle: Double = 0.95,
    val opacityEnd: Double = 0.9
) {
    val foreGroundColor: String = determineTextColor(color)
    val colorMap = gradientFromColor(color)

    // Convert CSS gradient angle to SVG x1,y1,x2,y2 coordinates
    private val gradientCoordinates: String = when(gradientAngle) {
        "to right" -> "x1='0%' y1='0%' x2='100%' y2='0%'"
        "to bottom right" -> "x1='0%' y1='0%' x2='100%' y2='100%'"
        "to bottom left" -> "x1='100%' y1='0%' x2='0%' y2='100%'"
        "to top" -> "x1='0%' y1='100%' x2='0%' y2='0%'"
        "to top right" -> "x1='0%' y1='100%' x2='100%' y2='0%'"
        "to top left" -> "x1='100%' y1='100%' x2='0%' y2='0%'"
        "to left" -> "x1='100%' y1='0%' x2='0%' y2='0%'"
        else -> "x1='0%' y1='0%' x2='0%' y2='100%'" // Default: to bottom
    }

    // Enhanced gradient with more stops and opacity control
   /* val linearGradient = """
        <linearGradient id="$id" $gradientCoordinates>
            <stop offset="0%" style="stop-color:${colorMap["color1"]}" stop-opacity="$opacityStart"/>
            <stop offset="25%" style="stop-color:${blendColors(colorMap["color1"]!!, colorMap["color2"]!!, 0.75)}" stop-opacity="${opacityStart + (opacityMiddle - opacityStart) * 0.5}"/>
            <stop offset="50%" style="stop-color:${colorMap["color2"]}" stop-opacity="$opacityMiddle"/>
            <stop offset="75%" style="stop-color:${blendColors(colorMap["color2"]!!, colorMap["color3"]!!, 0.75)}" stop-opacity="${opacityMiddle + (opacityEnd - opacityMiddle) * 0.5}"/>
            <stop offset="100%" style="stop-color:${colorMap["color3"]}" stop-opacity="$opacityEnd"/>
        </linearGradient>
    """.trimIndent()*/

    /*val linearGradient = """
    <linearGradient id="$id" x1="0%" y1="0%" x2="0%" y2="100%">
        <stop offset="0%" style="stop-color:$color"/>
        <stop offset="100%" style="stop-color:${darkenColor(color, 0.3)}"/>
    </linearGradient>
""".trimIndent()*/

    val linearGradient = createSimpleGradient(color, id)
    fun lighter() = colorMap["color1"]
    fun darker() = colorMap["color3"]
    fun original() = color

    fun createSimpleGradient(baseColor: String, id: String): String {
        val darkerColor = darkenColor(baseColor, 0.3)
        return """
        <linearGradient id="$id" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stop-color="$baseColor"/>
            <stop offset="100%" stop-color="$darkerColor"/>
        </linearGradient>
    """.trimIndent()
    }





    // Helper function to blend two colors
    private fun blendColors(color1: String, color2: String, ratio: Double): String {
        val rgb1 = hexToRgb(color1)
        val rgb2 = hexToRgb(color2)

        val r = (rgb1["r"]!! * (1 - ratio) + rgb2["r"]!! * ratio).toInt()
        val g = (rgb1["g"]!! * (1 - ratio) + rgb2["g"]!! * ratio).toInt()
        val b = (rgb1["b"]!! * (1 - ratio) + rgb2["b"]!! * ratio).toInt()

        return rgbToHex(r, g, b)
    }
    private fun darkenColor(hexColor: String, factor: Double): String {
        return adjustColor(hexColor, factor, false)
    }

    private fun brightenColor(hexColor: String, factor: Double): String {
        return adjustColor(hexColor, factor, true)
    }

    private fun adjustColor(hexColor: String, factor: Double, brighten: Boolean): String {
        val color = Color.decode(hexColor)
        val r = if (brighten) {
            (color.red + (255 - color.red) * factor).toInt().coerceIn(0, 255)
        } else {
            (color.red * (1 - factor)).toInt().coerceIn(0, 255)
        }
        val g = if (brighten) {
            (color.green + (255 - color.green) * factor).toInt().coerceIn(0, 255)
        } else {
            (color.green * (1 - factor)).toInt().coerceIn(0, 255)
        }
        val b = if (brighten) {
            (color.blue + (255 - color.blue) * factor).toInt().coerceIn(0, 255)
        } else {
            (color.blue * (1 - factor)).toInt().coerceIn(0, 255)
        }
        return String.format("#%02x%02x%02x", r, g, b)
    }

}
