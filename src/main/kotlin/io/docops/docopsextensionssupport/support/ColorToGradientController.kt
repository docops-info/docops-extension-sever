package io.docops.docopsextensionssupport.support

import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.accepted
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


@RestController
@RequestMapping("/api")
class ColorToGradientController  {

    @GetMapping("/grad/{color}")
    fun colors(@PathVariable("color") color: String): ResponseEntity<Map<String, String>> {
        return accepted().body(gradientFromColor(color))
    }


    @GetMapping("/grad/svg/{color}")
    fun svgLinearGradient(@PathVariable("color") color: String): ResponseEntity<String> {
        val gradient = gradientFromColor(color)
        return accepted().body("""
        <linearGradient id="headerGreen" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/>
        </linearGradient>
        """.trimIndent())
        //#e56516
    }

}

fun colorLuminance(hexValue: String, lumValue: Double): String {
    // validate hex string
    var hex = hexValue.replace("[^0-9a-f]".toRegex(), "")
    if (hex.length < 6) {
        hex = hex[0].toString() + hex[0] + hex[1] + hex[1] + hex[2] + hex[2]
    }
    val lum = lumValue
    // convert to decimal and change luminosity
    var rgb = "#"
    for (i in 0 until 3) {
        var c = hex.substring(i * 2, i * 2 + 2).toInt(16)
        c = (min(max(0, c + (c * lum).toInt()), 255)).toString(16).toInt()
        rgb += ("00" + c.toString(16)).substring(c.toString(16).length)
    }
    return rgb
}
fun RandomColorDark(offset: Int): Color {
    val maxValue = 256 - offset
    val ran = Random(maxValue)
    return Color(ran.nextInt(maxValue), ran.nextInt(maxValue), ran.nextInt(maxValue))
}