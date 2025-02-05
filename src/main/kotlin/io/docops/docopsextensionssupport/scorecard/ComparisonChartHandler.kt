package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.web.panel.uncompressString
import kotlinx.serialization.json.Json
import org.springframework.http.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class ComparisonChartHandler {

    fun handleSVG(payload: String) : ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val maker = ComparisonTableMaker()
        val comparisonChart = Json.decodeFromString<ComparisonChart>(data)
        val svg = maker.make(comparisonChart)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}