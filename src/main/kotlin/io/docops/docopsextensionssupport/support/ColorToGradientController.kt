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


}