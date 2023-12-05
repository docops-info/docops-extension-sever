package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.badge.findHeightWidth
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import org.apache.commons.logging.LogFactory
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.net.URLDecoder
import kotlin.time.measureTimedValue

class RoadmapHandler {
    val log = LogFactory.getLog(RoadmapHandler::class.java)
    fun handleSVG(payload: String, useDark: Boolean, type: String, scale: String, numChars: String, title: String): ResponseEntity<ByteArray>  {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val rmm = RoadMapMaker(useDark)

            val svg = rmm.makeRoadMapImage(data, scale, title, numChars)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)
        }
        log.info("getRoadMap executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }

    fun handlePNG(payload: String, useDark: Boolean, type: String, scale: String, numChars: String, title: String): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val rmm = RoadMapMaker(useDark)
            val svg = rmm.makeRoadMapImage(data, scale, title, numChars)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.IMAGE_PNG
            val res = findHeightWidth(svg)
            val baos = SvgToPng().toPngFromSvg(svg, res)
            ResponseEntity(baos, headers, HttpStatus.OK)
        }
        log.info("getRoadMap executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }
}
