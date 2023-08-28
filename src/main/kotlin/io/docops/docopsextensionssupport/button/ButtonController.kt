package io.docops.docopsextensionssupport.button

import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.observation.annotation.Observed
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Controller
@RequestMapping("/api")
@Observed(name = "buttons.controller")
class ButtonController {
    private val log = LoggerFactory.getLogger(ButtonController::class.java)
    @PutMapping("/buttons/form")
    @ResponseBody
    fun fromJsonToButtonForm(@RequestParam(name = "payload") payload: String): ResponseEntity<ByteArray> {
        return fromRequestParameter(payload)
    }

    private fun fromRequestParameter(payload: String): ResponseEntity<ByteArray> {
        try {
            val buttons = Json.decodeFromString<Buttons>(payload)
            return createResponse(buttons, true, "SVG")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun createResponse(buttons: Buttons, useDark: Boolean, type: String): ResponseEntity<ByteArray> {
        buttons.useDark = useDark
            val imgSrc = buttons.createSVGShape(type)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
            return ResponseEntity(imgSrc.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)

    }

    @PutMapping("/buttons")
    @ResponseBody
    fun fromJsonToButton(@RequestBody buttons: Buttons): ResponseEntity<ByteArray> {
        try {
            return createResponse(buttons, true, "SVG")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @GetMapping("/buttons")
    @ResponseBody
    fun getButtons(@RequestParam(name = "payload") payload: String,
                   @RequestParam(name="useDark", defaultValue = "false") useDark: Boolean,
                   @RequestParam(name="type", defaultValue = "SVG") type: String): ResponseEntity<ByteArray> {
        var data = ""
        try {
            data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val content = Json.decodeFromString<Buttons>(data)
            return createResponse(content, useDark, type)
        } catch (e: Exception) {
            log.info("Data received after uncompressed: -> $data")
            e.printStackTrace()
            throw e
        }
    }
}