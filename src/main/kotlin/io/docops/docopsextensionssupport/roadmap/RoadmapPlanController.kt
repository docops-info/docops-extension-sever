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

package io.docops.docopsextensionssupport.roadmap

import io.docops.asciidoctorj.extension.panels.compressString
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.logging.LogFactory
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.time.measureTimedValue

/**
 * Controller class for managing roadmap plans.
 */
@Controller
@RequestMapping("/api/roadmap")
class RoadmapPlanController {
    val log = LogFactory.getLog(RoadmapPlanController::class.java)

    /**
     * Generates a roadmap plan based on the provided parameters.
     *
     * @param httpServletRequest The HttpServletRequest object that contains the request parameters.
     * @return A ResponseEntity object with the binary representation of the generated roadmap plan and the appropriate headers.
     */
    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Counted(value = "docops.roadmap.put.html", description = "Creating a roadmap plan from webform")
    @Timed(
        value = "docops.roadmap.put.html",
        description = "Creating a roadmap plan from webform",
        percentiles = [0.5, 0.9]
    )
    fun putRoadmapPlan(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            var div = ""
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            var contents = httpServletRequest.getParameter("content")
            if (contents.isNullOrEmpty()) {
                contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
            }
            val scale = httpServletRequest.getParameter("scale")
            val title = httpServletRequest.getParameter("title")
            val numChars = httpServletRequest.getParameter("numChars")
            var chars = numChars
            if (numChars == null || numChars.isEmpty()) {
                chars = "32"
            }
            val useDarkInput = httpServletRequest.getParameter("useDark")
            val index = httpServletRequest.getParameter("index")
            val rmm = PlannerMaker()

            val svg = rmm.makePlannerImage(contents, title)

            div = """
            <div>$svg</div>
            <div class="divider"></div> 
            <div>
               <a class="btn btn-outline" href="api/roadmap/?payload=${compressString(contents)}&title=$title&numChars=$numChars&scale=$scale&type=svg" target="_blank">Open Url</a>
            </div>
        """.trimIndent()
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }

        log.info("putRoadmapPlan executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value

    }

    /**
     * Retrieves a road map image based on the provided parameters.
     *
     * @param payload The compressed payload data representing the road map. (required)
     * @param scale The scale of the road map image. (required)
     * @param type The type of the road map image. Defaults to "SVG" if not provided. (optional)
     * @param title The title of the road map image. (optional)
     * @param numChars The number of characters to display in the road map image. Defaults to 30 if not provided. (optional)
     * @param useDark Determines whether to use a dark theme for the road map image. Defaults to false. (optional)
     *
     * @return The road map image as a byte array wrapped in a ResponseEntity.
     */
    @Traceable
    @GetMapping("/")
    @ResponseBody
    @Counted("docops.roadmap.get.html", description = "Creating a roadmap plan from http get")
    @Timed(
        value = "docops.roadmap.get.html",
        description = "Creating a roadmap plan from http get",
        percentiles = [0.5, 0.9]
    )
    fun getRoadMap(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "scale") scale: String,
        @RequestParam("type", required = false, defaultValue = "SVG") type: String,
        @RequestParam("title", required = false) title: String,
        @RequestParam("numChars", required = false, defaultValue = "30") numChars: String,
        @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean
    )
            : ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val rmm = PlannerMaker()

            val svg = rmm.makePlannerImage(data, title)
            val headers = HttpHeaders()

            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)

        }
        log.info("getRoadMap executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }


}