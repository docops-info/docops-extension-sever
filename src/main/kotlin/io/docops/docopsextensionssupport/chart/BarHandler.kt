package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import kotlinx.serialization.json.Json
import org.springframework.http.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class BarHandler {

    fun handleSVG(payload: String) : ResponseEntity<ByteArray>{
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val barChartImproved = BarChartImproved()
        val svg = barChartImproved.makeBarSvg(data)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}