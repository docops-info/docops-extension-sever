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

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
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

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultScorecardJson = """
        {
  "title": "Technology Comparison",
  "initiativeTitle": "Current Solution",
  "outcomeTitle": "Proposed Solution",
  "initiativeItems": [
    {"displayText":"Limited scalability"},
    {"displayText":"High maintenance costs"},
    {"displayText":"Manual deployment process"},
    {"displayText":"Minimal monitoring capabilities"},
    {"displayText":"Difficult to extend"}
  ],
  "outcomeItems": [
    {"displayText":"Highly scalable architecture"},
    {"displayText":"Reduced operational costs"},
    {"displayText":"Automated CI/CD pipeline"},
    {"displayText":"Comprehensive monitoring"},
    {"displayText":"Modular and extensible design"}
  ]
}
        """.trimIndent()

        val editModeHtml = """
            <div id="scorecardContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/scorecard/form" hx-target="#scorecardPreview" class="space-y-4">
                    <div>
                        <label for="payload" class="block text-sm font-medium text-gray-700 mb-1">Edit Scorecard JSON:</label>
                        <textarea id="payload" name="payload" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultScorecardJson}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Scorecard
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/scorecard/view-mode"
                                hx-target="#scorecardContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="scorecardPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Scorecard" to see the preview
                        </div>
                    </div>
                </form>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(editModeHtml, headers, HttpStatus.OK)
    }

    @GetMapping("/view-mode")
    @ResponseBody
    fun getViewMode(): ResponseEntity<String> {
        val viewModeHtml = """
            <div id="scorecardContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/compare.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/compare.svg" alt="Scorecard" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }
    /**
     * Retrieves a scorecard as an SVG image file based on the provided payload.
     *
     * @param payload The encoded payload containing the data to generate the score card.
     * @param useDark Determines whether to use dark mode for the score card. Defaults to false.
     * @return A ResponseEntity object containing the generated score card as an SVG image file.
     * @throws Exception If an error occurs during the retrieval or generation of the score card.
     */
    @Traceable
    @GetMapping("/")
    @ResponseBody
    @Timed(value="docops.getScoreCard", description="docops asciidoctorj plugin", percentiles=[0.5, 0.9])
    @Counted(value="docops.getScoreCard", description="docops asciidoctorj plugin")
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
    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Timed(value="docops.putScorecard", description="creating a scorecard from a web form", percentiles=[0.5, 0.9])
    @Counted(value="docops.putScorecard", description="creating a scorecard from a web form")
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
    @Traceable
    @PutMapping("/form")
    @ResponseBody
    @Timed(value="docops.putScorecardForm", description="creating a scorecard from a web form with json", percentiles=[0.5, 0.9])
    @Counted(value="docops.putScorecardForm", description="creating a scorecard from a web form with json")
    fun fromJsonToScorecard(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
           val  payload: String= httpServletRequest.getParameter("payload") ?: ""
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
