package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import kotlinx.serialization.json.Json
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class BarGroupHandler {
    fun handleSVG(payload: String): ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val maker = BarGroupMaker()
        val bar = Json.decodeFromString<BarGroup>(data)
        val svg = if(bar.display.vBar) {
            maker.makeVGroupBar(bar)
        }else if (bar.display.condensed) {
            maker.makeCondensed(bar)
        }
        else {
             maker.makeBar(bar)
        }
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}