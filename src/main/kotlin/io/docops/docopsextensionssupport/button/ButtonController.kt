package io.docops.docopsextensionssupport.button

import io.micrometer.observation.annotation.Observed
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.nio.charset.StandardCharsets

@Controller
@RequestMapping("/api")
@Observed(name = "buttons.controller")
class ButtonController {

    @PutMapping("/buttons/form")
    @ResponseBody
    fun fromJsonToButtonForm(@RequestParam(name = "payload") payload: String): ResponseEntity<ByteArray> {
        try {
            val buttons = Json.decodeFromString<Buttons>(payload)
            return createResponse(buttons)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun createResponse(buttons: Buttons): ResponseEntity<ByteArray> {
            val imgSrc = buttons.createSVGShape()
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
            return ResponseEntity(imgSrc.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)

    }

    @PutMapping("/buttons")
    @ResponseBody
    fun fromJsonToButton(@RequestBody buttons: Buttons): ResponseEntity<ByteArray> {
        try {
            return createResponse(buttons)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}