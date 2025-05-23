package io.docops.docopsextensionssupport.button

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import org.springframework.http.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.time.measureTimedValue

/**
 * Handles the processing and generation of SVG representations for buttons.
 * 
 * This class is responsible for decoding button data from URL-encoded payloads,
 * creating button objects, and generating SVG responses. It serves as a focused
 * utility for SVG generation, while [ButtonController] provides a more comprehensive
 * REST API with additional features like theme handling.
 * 
 * The handler measures and logs the execution time of operations for performance monitoring.
 */
class ButtonHandler {

    val log = KotlinLogging.logger {}

    /**
     * Processes a URL-encoded payload and generates an SVG representation of buttons.
     *
     * This method decodes the payload, creates a [Buttons] object, and generates an SVG response.
     * It also measures and logs the execution time for performance monitoring.
     *
     * @param payload The URL-encoded and compressed string containing button data
     * @param useDark Whether to use dark mode styling for the buttons
     * @param type The type of output format (e.g., "SVG")
     * @param backend The backend rendering type to use
     * @return A [ResponseEntity] containing the SVG representation as a byte array with appropriate HTTP headers
     */
    fun handleSVG(payload: String, useDark: Boolean, type: String , backend: String): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val content = Json.decodeFromString<Buttons>(data)
            createResponse(content, useDark, backend)
        }
        log.info{"getButton executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }

    /**
     * Creates an HTTP response containing the SVG representation of buttons.
     *
     * This method sets the dark mode flag on the buttons, creates an SVG shape,
     * draws the shape, and returns the result as an HTTP response with appropriate headers.
     *
     * @param buttons The [Buttons] object containing the button data
     * @param useDark Whether to use dark mode styling for the buttons
     * @param type The type of output format (e.g., "SVG")
     * @return A [ResponseEntity] containing the SVG representation as a byte array with appropriate HTTP headers
     */
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
