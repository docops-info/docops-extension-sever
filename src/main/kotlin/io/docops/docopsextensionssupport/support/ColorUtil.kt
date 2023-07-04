package io.docops.docopsextensionssupport.support

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
    val decoded = Color.decode(color)
    val tinted1 = tint(decoded, 0.5)
    val tinted2 = tint(decoded, 0.25)
    return mapOf("color1" to tinted1, "color2" to tinted2, "color3" to color)
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