package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.web.panel.uncompressString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Controller
@RequestMapping("/api/scorecard")
class ScorecardController {

    @GetMapping("/")
    @ResponseBody
    fun getMapping(@RequestParam(name = "payload") payload: String, @RequestParam(name="useDark", defaultValue = "false") useDark: Boolean): ResponseEntity<ByteArray> {
        try {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val content = Json.decodeFromString<ScoreCard>(data)
            val sm = ScoreCardMaker()
            val svg = sm.make(scoreCard = content)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
            return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @PutMapping("/")
    @ResponseBody
    fun putMapping(@RequestBody scoreCard: ScoreCard): ResponseEntity<ByteArray> {
        try {
            val sm = ScoreCardMaker()
            val svg = sm.make(scoreCard = scoreCard)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
            return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    @PutMapping("/form")
    @ResponseBody
    fun fromJsonToScorecard(@RequestParam(name = "payload") payload: String): ResponseEntity<ByteArray> {
        return fromRequestParameter(payload)
    }

    private fun fromRequestParameter(payload: String): ResponseEntity<ByteArray> {
        try {
            val scoreCard = Json.decodeFromString<ScoreCard>(payload)
            val sm = ScoreCardMaker()
            val svg = sm.make(scoreCard = scoreCard)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
            return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}