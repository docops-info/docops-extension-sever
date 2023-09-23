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

package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.badge.findHeightWidth
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.logging.LogFactory
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.time.measureTimedValue

/**
 * The TimelineController class handles HTTP requests for timeline-related operations.
 *
 * @constructor Creates a new instance of TimelineController.
 */
@Controller
@RequestMapping("/api/timeline")
@Observed(name = "timeline.controller")
class TimelineController {
    private val log = LogFactory.getLog(TimelineController::class.java)
    /**
     * Updates the timeline based on the provided request data.
     *
     * @param httpServletRequest The HttpServletRequest object containing the request data.
     * @return The ResponseEntity object containing the updated timeline in the response body.
     */
    @PutMapping("/")
    @ResponseBody
    @Timed(value = "docops.timeline.put.html", histogram = true, percentiles = [0.5, 0.95])
    fun putTimeline(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            var title = "title"
            var contents = httpServletRequest.getParameter("content")
            if (contents.isNullOrEmpty()) {
                contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
                title = httpServletRequest.getParameter("title")
            }
            val scale = httpServletRequest.getParameter("scale")
            val numChars = httpServletRequest.getParameter("numChars")
            var chars = numChars
            if (numChars == null || numChars.isEmpty()) {
                chars = "24"
            }
            val useDarkInput = httpServletRequest.getParameter("useDark")
            val tm = TimelineMaker("on".equals(useDarkInput))
            val svg = tm.makeTimelineSvg(contents, title, scale, false, chars)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
        <div id='imageblock'>
        $svg
        </div>
        <br/>
        <h3>Timeline Source</h3>
        <div class='pure-u-1 pure-u-md-20-24'>
        <pre>
        <code class="kotlin">
        $contents
        </code>
        </pre>
        </div>
        <script>
        var adrSource = `[timeline,title="Demo timeline Builder by docops.io",scale="0.7",role="center"]\n----\n${contents}\n----`;
        document.querySelectorAll('pre code').forEach((el) => {
            hljs.highlightElement(el);
        });
        </script>
        """.trimIndent()
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info("putTimeline executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }

    /**
     * Retrieves a timeline based on the provided parameters.
     *
     * @param payload The timeline data as a compressed string.
     * @param title The title of the timeline.
     * @param scale The scale of the timeline.
     * @param type The type of the output, defaults to "SVG".
     * @param numChars The number of characters displayed on the timeline, defaults to 35.
     * @param useDark Indicates whether to use a dark theme for the timeline, defaults to false.
     * @return A ResponseEntity containing the timeline data, either as an SVG image or a PNG image if the type is "PDF".
     */
    @GetMapping("/")
    @ResponseBody
    @Timed(value = "docops.roadmap.get.html", histogram = true, percentiles = [0.5, 0.95])
    fun getTimeLine(@RequestParam(name = "payload") payload: String,
                    @RequestParam(name="title") title: String,
                    @RequestParam(name="scale") scale: String,
                    @RequestParam("type", required = false, defaultValue = "SVG") type: String,
                    @RequestParam("numChars", required = false, defaultValue = "35") numChars: String,
                    @RequestParam(name="useDark", defaultValue = "false") useDark: Boolean
                    ): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val tm = TimelineMaker(useDark = useDark)
            val isPdf = "PDF" == type
            val svg = tm.makeTimelineSvg(data, title, scale, isPdf, numChars)
            if (isPdf) {
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType.IMAGE_PNG
                val res = findHeightWidth(svg)
                val baos = SvgToPng().toPngFromSvg(svg, res)
                ResponseEntity(baos, headers, HttpStatus.OK)
            } else {
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType.parseMediaType("image/svg+xml")
                ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)
            }
        }
        log.info("getTimeLine executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }

    /**
     * Generate a timeline table based on the provided payload and title.
     *
     * @param payload The encoded and compressed string representing the timeline data.
     * @param title The title of the timeline table.
     * @return A ResponseEntity containing the generated timeline table as a byte array.
     */
    @GetMapping("/table")
    @ResponseBody
    @Timed(value = "docops.roadmap.table.data.html", histogram = true, percentiles = [0.5, 0.95])
    fun getTimeLineTable(@RequestParam(name = "payload") payload: String, @RequestParam(name="title") title: String): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val tm = TimelineParser()
            val entries = tm.parse(data)
            val sb = StringBuilder(".$title\n")
            sb.append("[%header,cols=\"1,2\",stripes=even]\n")
            sb.append("!===\n")
            sb.append("|Date |Event\n")
            entries.forEach {
                sb.append("a|${it.date} |${it.text}\n")
            }
            sb.append("!===")
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.TEXT_PLAIN
            ResponseEntity(sb.toString().toByteArray(), headers, HttpStatus.OK)
        }
        log.info("getTimeLineTable executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }
}

