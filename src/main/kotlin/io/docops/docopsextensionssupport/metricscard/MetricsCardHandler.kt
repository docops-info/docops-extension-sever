package io.docops.docopsextensionssupport.metricscard

import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType
import org.springframework.http.HttpHeaders
import org.springframework.http.CacheControl
import java.util.concurrent.TimeUnit

/**
 * Handler for metrics card visualizations
 */
class MetricsCardHandler {

    /**
     * Handles the creation of metrics card SVGs
     */
    fun handleSVG(
        payload: String,
        type: String = "SVG",
        scale: String = "1.0",
        useDark: Boolean = false,
        width: Int = 800,
        height: Int = 400
    ): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")

        val maker = MetricsCardMaker()
        val svg = maker.createMetricsCardSvg(payload, width, height)

        return ResponseEntity.ok()
            .headers(headers)
            .body(svg.toByteArray())
    }
}