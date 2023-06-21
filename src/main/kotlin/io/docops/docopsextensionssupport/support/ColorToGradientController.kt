package io.docops.docopsextensionssupport.support

import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.accepted
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.awt.Color

@RestController
@RequestMapping("/api")
class ColorToGradientController  {

    @GetMapping("/grad/{color}")
    fun colors(@PathVariable("color") color: String): ResponseEntity<Map<String, String>> {
        return accepted().body(gradientFromColor(color))
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

    fun randomColor() {
        val color = (Math.random() * 16777215).toInt() or (0xFF shl 24)
    }
}