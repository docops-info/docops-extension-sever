package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.PieSliceMaker
import io.docops.docopsextensionssupport.web.panel.uncompressString
import kotlinx.serialization.json.Json
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class PieSliceHandler {

    fun handleSVG(payload: String) : ResponseEntity<ByteArray>{
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val maker = PieSliceMaker()
        val slices = Json.decodeFromString<PieSlices>(data)
        val svg = maker.makePie(slices)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}