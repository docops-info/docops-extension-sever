package io.docops.docopsextensionssupport.button

import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import kotlinx.serialization.json.Json
import org.apache.commons.logging.LogFactory
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.time.measureTimedValue

class ButtonHandler {

    val log = LogFactory.getLog(ButtonHandler::class.java)
    fun handleSVG(payload: String, useDark: Boolean, type: String): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val content = Json.decodeFromString<Buttons>(data)
            createResponse(content, useDark, type)
        }
        log.info("getButton executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }
    fun handlePNG(payload: String): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val buttons = Json.decodeFromString<Buttons>(data)
            val buttonShape = buttons.createSVGShape()
            buttons.useDark = true
            val imgSrc = buttonShape.drawShape("PDF")
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType("image", "png", StandardCharsets.UTF_8)
            val png = SvgToPng().toPngFromSvg(
                imgSrc,
                Pair(buttonShape.height().toString(), buttonShape.width().toString())
            )
            ResponseEntity(png, headers, HttpStatus.OK)
        }
        log.info("getButton executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }

    private fun createResponse(buttons: Buttons, useDark: Boolean, type: String): ResponseEntity<ByteArray> {
        buttons.useDark = useDark
        val buttonShape = buttons.createSVGShape()
        val imgSrc = buttonShape.drawShape(type)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
        return ResponseEntity(imgSrc.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)

    }
}