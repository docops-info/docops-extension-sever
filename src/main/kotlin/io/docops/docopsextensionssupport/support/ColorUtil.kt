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
    val tinted1 = tint(decoded, 0.35)
    val tinted2 = tint(decoded, 0.10)
    return mapOf("color1" to tinted1, "color2" to tinted2, "color3" to color)
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