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

import io.docops.docopsextensionssupport.svgsupport.compressString
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.extension.wasm.timeline.TimelineMaker
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
 * The TimelineController class handles HTTP requests for timeline-related operations.
 *
 * @constructor Creates a new instance of TimelineController.
 */
@Controller
@RequestMapping("/api/timeline")
class TimelineController {
    private val log = LogFactory.getLog(TimelineController::class.java)

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultTimelineContent = """
        -
        date: 1891
        text: Mailbox, invented by Phillip Downing
        -
        date: 1923
        text: The Three-Light Traffic Signal, invented by Garrett Morgan
        -
        date: 1932
        text: Automatic Gear Shift, invented by Richard Spikes
        date: July 23rd, 2023
        text: DocOps extension Server releases a new feature, Timeline Maker
        for [[https://github.com/asciidoctor/asciidoctorj asciidoctorj]]. With a simple text markup block you can
        create very powerful timeline images. Enjoy!
        -
        date: August 15th, 2023
        text: DocOps.io revamping website with updated documentation. All
        our work will be updated with latest documentation for Panels,
        for extension server are the various plug-ing for asciidoctorj.
        """.trimIndent()

        val editModeHtml = """
            <div id="timelineContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/timeline/" hx-target="#timelinePreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit Timeline Content:</label>
                        <textarea id="content" name="content" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultTimelineContent}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Timeline
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/timeline/view-mode"
                                hx-target="#timelineContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="timelinePreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Timeline" to see the preview
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
            <div id="timelineContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/timeline.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/timeline.svg" alt="Timeline" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    /**
     * Updates the timeline based on the provided request data.
     *
     * @param httpServletRequest The HttpServletRequest object containing the request data.
     * @return The ResponseEntity object containing the updated timeline in the response body.
     */
    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Timed(
        value = "docops.timeline.put",
        description = "docops create timeline from web form",
        percentiles = [0.5, 0.9]
    )
    @Counted(value = "docops.timeline.put", description = "docops create timeline from web form")
    fun putTimeline(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            var title = "title"
            var contents = httpServletRequest.getParameter("content")
            if (contents.isNullOrEmpty()) {
                contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
                title = httpServletRequest.getParameter("title")
            }
            val scale = httpServletRequest.getParameter("scale")?: "0.5"

            val outlineColor = httpServletRequest.getParameter("outline")
            val useDarkInput = httpServletRequest.getParameter("useDark")
            val tm = TimelineMaker()
            val entries = TimelineParser().parse(contents)
            val svg = tm.makeSvg(entries, "on" == useDarkInput, scale)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val compressedPayload = compressString(contents)
            val imageUrl = "https://roach.gy/extension/api/docops/svg?kind=timeline&payload=${compressedPayload}&type=SVG&useDark=false&title=$title&numChars=34&scale=$scale&filename=timeline.svg"

            val div = """
                <div id='imageblock'>
                $svg
                </div>
                <div class="mb-4">
                    <h3>Image Request</h3>
                    <div class="flex items-center">
                        <input id="imageUrlInput" type="text" value="$imageUrl" readonly class="w-full p-2 border border-gray-300 rounded-l-md text-sm bg-gray-50">
                        <button onclick="copyToClipboard('imageUrlInput')" class="bg-blue-600 text-white px-4 py-2 rounded-r-md hover:bg-blue-700 transition-colors">
                            Copy URL
                        </button>
                    </div>
                </div>
                <script>
                var adrSource = `[timeline,title="Demo timeline Builder by docops.io",scale="0.7",role="center"]\n----\n${contents}\n----`;

                function copyToClipboard(elementId) {
                    const element = document.getElementById(elementId);
                    element.select();
                    document.execCommand('copy');

                    // Show a temporary "Copied!" message
                    const button = element.nextElementSibling;
                    const originalText = button.textContent;
                    button.textContent = "Copied!";
                    setTimeout(() => {
                        button.textContent = originalText;
                    }, 2000);
                }
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
    @Traceable
    @GetMapping("/")
    @ResponseBody
    @Timed(
        value = "docops.roadmap.get",
        description = "docops create timeline from asciidoctorj plugin",
        percentiles = [0.5, 0.9]
    )
    @Counted(value = "docops.roadmap.get", description = "docops create timeline from asciidoctorj plugin")
    fun getTimeLine(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "title") title: String,
        @RequestParam(name = "scale") scale: String,
        @RequestParam("type", required = false, defaultValue = "SVG") type: String,
        @RequestParam("numChars", required = false, defaultValue = "35") numChars: String,
        @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
        @RequestParam(name = "outlineColor", defaultValue = "#37cdbe") outlineColor: String,
        @RequestParam(name = "useGlass", defaultValue = "true") useGlass: Boolean

    ): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val tm = TimelineMaker()
            val isPdf = "PDF" == type
            val entries = TimelineParser().parse(data)
            val svg = tm.makeSvg(entries, false, scale)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)
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
    @Traceable
    @GetMapping("/table")
    @ResponseBody
    @Timed(value = "docops.roadmap.table.data.html")
    fun getTimeLineTable(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "title") title: String
    ): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val tm = TimelineParser()
            val entries = tm.parse(data)
            val sb = StringBuilder(".$title\n")
            sb.append("[%header,cols=\"1,2\",stripes=even]\n")
            sb.append("!===\n")
            sb.append("|Date |Event\n")
            entries.events.forEach {
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

    @PostMapping("")
    fun editFormSubmission(@RequestParam("payload") payload: String) : ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val tm = TimelineMaker()
            val entries = TimelineParser().parse(data)
            val svg = tm.makeSvg(entries, false, "0.6")
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.TEXT_PLAIN
            ResponseEntity(svg.toString().toByteArray(), headers, HttpStatus.OK)
        }
        return timing.value
    }
}
