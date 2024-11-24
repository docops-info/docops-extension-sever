package io.docops.docopsextensionssupport.chart

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

    fun handleSVG(payload: String, isPdf: Boolean) : ResponseEntity<ByteArray>{
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val slices = Json.decodeFromString<PieSlices>(data)
        var svg = ""
        if(!slices.display.donut || isPdf) {
            val maker = PieSliceMaker()
             svg = maker.makePie(slices)
        } else {
            val maker = DonutMaker()
             svg = maker.makeDonut(slices)
        }
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}