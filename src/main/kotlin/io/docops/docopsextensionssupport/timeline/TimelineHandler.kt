package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import org.apache.commons.logging.LogFactory
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import kotlin.time.measureTimedValue

class TimelineHandler {
    private val log = KotlinLogging.logger {  }
    fun handleSVG(
        payload: String,
        type: String,
        title: String,
        useDark: Boolean,
        outlineColor: String,
        scale: String,
        numChars: String,
        backend: String
    ): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val isPdf = backend == "pdf"
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val tm = TimelineMaker(useDark = useDark, outlineColor = outlineColor)
            val svg = tm.makeTimelineSvg(data, title, scale, isPdf = isPdf, numChars)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            withLoggingContext("isPdf" to "$isPdf", "title" to title, "scale" to scale, "numChars" to numChars, "type" to type, "payload" to data) {
                log.info{"getTimeLineTable executed"}
            }
            ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)
        }
        log.info{"getTimeLineTable executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }

}