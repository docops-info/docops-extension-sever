package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.cal.CalEntry
import io.docops.docopsextensionssupport.diagram.PieMaker
import io.docops.docopsextensionssupport.diagram.Pies
import io.docops.docopsextensionssupport.web.panel.uncompressString
import kotlinx.serialization.json.Json
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class BarHandler {

    fun handleSVG(payload: String) : ResponseEntity<ByteArray>{
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val barMaker = BarMaker()
        val bar = Json.decodeFromString<Bar>(data)
        var svg = ""
        if(bar.display.vBar) {
             svg = barMaker.makeVerticalBar(bar)

        } else {
             svg = barMaker.makeHorizontalBar(bar)
        }
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}