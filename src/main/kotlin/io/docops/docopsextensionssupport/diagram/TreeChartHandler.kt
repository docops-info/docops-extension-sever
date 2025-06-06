package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.chart.BarChartImproved
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class TreeChartHandler {

    fun handleSVG(payload: String, isPdf: Boolean = false) : ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val treeMaker = TreeMaker()
        val svg = treeMaker.makeTree(data, isPdf)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}