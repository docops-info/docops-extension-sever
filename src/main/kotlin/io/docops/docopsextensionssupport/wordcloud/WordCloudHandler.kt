package io.docops.docopsextensionssupport.wordcloud

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import org.springframework.http.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class WordCloudHandler {

    fun handleSVG(payload: String): ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val wordCloudImproved = WordCloudImproved()
        val svg = wordCloudImproved.makeWordCloudSvg(data)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}