package io.docops.docopsextensionssupport.cal


import io.docops.docopsextensionssupport.web.panel.uncompressString
import kotlinx.serialization.json.Json
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class CalHandler {

    fun handleSVG(payload: String) : ResponseEntity<ByteArray>{
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val calMaker = CalMaker()
        var svg = ""
        if(data.trim().isEmpty()) {
            svg = calMaker.makeCalendar(null)
        } else {
            val calEntry = Json.decodeFromString<CalEntry>(data)
            svg = calMaker.makeCalendar(calEntry)
        }
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}