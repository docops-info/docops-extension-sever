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