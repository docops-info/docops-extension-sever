package io.docops.docopsextensionssupport.button

import io.docops.docopsextensionssupport.web.panel.PanelGenerator
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

    @GetMapping("/buttons")
    @ResponseBody
    fun getButtons(@RequestParam(name = "payload") payload: String): ResponseEntity<ByteArray> {
        try {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            log.info("Data received after uncompressed: -> $data")
            val content = Json.decodeFromString<Buttons>(data)
            return createResponse(content)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}