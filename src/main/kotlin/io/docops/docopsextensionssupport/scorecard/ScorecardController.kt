/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Timed
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.commons.logging.LogFactory
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.time.measureTimedValue

/**
 * Controller class responsible for handling scorecard-related API requests.
 *
 * This class provides methods to retrieve and update scorecards as SVG image files.
 */
@Controller
@RequestMapping("/api/scorecard")
class ScorecardController {
    private val log = LogFactory.getLog(ScorecardController::class.java)
    /**
     * Retrieves a scorecard as an SVG image file based on the provided payload.
     *
     * @param payload The encoded payload containing the data to generate the score card.
     * @param useDark Determines whether to use dark mode for the score card. Defaults to false.
     * @return A ResponseEntity object containing the generated score card as an SVG image file.
     * @throws Exception If an error occurs during the retrieval or generation of the score card.
     */
    @GetMapping("/")
    @ResponseBody
    @Timed(value="docops.getScoreCard")
    fun getScoreCard(@RequestParam(name = "payload") payload: String, @RequestParam(name="useDark", defaultValue = "false") useDark: Boolean): ResponseEntity<ByteArray> {

        try {
            val timing = measureTimedValue {
                val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
                val content = Json.decodeFromString<ScoreCard>(data)
                val sm = ScoreCardMaker()
                val svg = sm.make(scoreCard = content)
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

    /**
     * Updates the scorecard by generating an SVG image based on the given scorecard data.
     *
     * @param scoreCard The scorecard object containing the data for generating the SVG image.
     * @return The response entity containing the generated SVG image as a byte array.
     * @throws Exception if an error occurs while generating the SVG image.
     */
    @PutMapping("/")
    @ResponseBody
    @Timed(value="docops.putScorecard")
    fun putScorecard(@RequestBody scoreCard: ScoreCard): ResponseEntity<ByteArray> {
        try {
            val timing = measureTimedValue {
                val sm = ScoreCardMaker()
                val svg = sm.make(scoreCard = scoreCard)
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
                ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
            }
            log.info("putScorecard executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    /**
     * Converts the JSON payload from Request Parameter to a Scorecard object.
     *
     * @param payload The JSON payload as a String.
     * @return A ResponseEntity containing the Scorecard object as ByteArray.
     */
    @PutMapping("/form")
    @ResponseBody
    fun fromJsonToScorecard(@RequestParam(name = "payload") payload: String): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            fromRequestParameter(payload)
        }
        log.info("fromJsonToScorecard executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
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