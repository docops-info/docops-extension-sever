package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.badge.findHeightWidth
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import kotlinx.serialization.json.Json
import org.apache.commons.logging.LogFactory
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.time.measureTimedValue

class ScorecardHandler {
    private val log = LogFactory.getLog(ScorecardController::class.java)
    fun handleSVG(
        payload: String,
        backend: String
    ): ResponseEntity<ByteArray> {
        try {
            val timing = measureTimedValue {
                var isPdf = backend == "pdf"
                val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
                val content = Json.decodeFromString<ScoreCard>(data)
                val sm = ScoreCardMaker()
                val svg = sm.make(scoreCard = content, isPdf = isPdf)
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
                ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
            }
            log.info("getScoreCard executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun handlePNG(
        payload: String
    ): ResponseEntity<ByteArray> {
        try {
            val timing = measureTimedValue {
                val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
                val content = Json.decodeFromString<ScoreCard>(data)
                val sm = ScoreCardMaker()
                val svg = sm.make(scoreCard = content, isPdf = true)
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType.IMAGE_PNG
                val res = findHeightWidth(svg)
                val baos = SvgToPng().toPngFromSvg(svg, res)
                ResponseEntity(baos, headers, HttpStatus.OK)
            }
            log.info("getScoreCard executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}