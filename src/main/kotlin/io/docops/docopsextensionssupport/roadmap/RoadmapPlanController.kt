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

import io.docops.docopsextensionssupport.svgsupport.compressString
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.logging.Log
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
    val log: Log = LogFactory.getLog(RoadmapPlanController::class.java)

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultRoadmapContent = """- now Authentication
* Implement user authentication system
* Set up CI/CD pipeline
* Create database schema
- next REST
* Develop REST API endpoints
* Build frontend components
* Implement search functionality
- later Analytics
* Add analytics dashboard
* Optimize performance
* Implement advanced features
- done Requirements
* Project requirements gathering
* Architecture design
* Technology stack selection"""

        val editModeHtml = """
            <div id="roadmapContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/roadmap/" hx-target="#roadmapPreview" class="space-y-4">
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label for="title" class="block text-sm font-medium text-gray-700 mb-1">Title:</label>
                            <input type="text" id="title" name="title" value="Q3 Development Roadmap" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">
                        </div>
                        <div>
                            <label for="scale" class="block text-sm font-medium text-gray-700 mb-1">Scale:</label>
                            <input type="text" id="scale" name="scale" value="2.2" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">
                        </div>
                    </div>
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Roadmap Content:</label>
                        <textarea id="content" name="content" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultRoadmapContent}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Roadmap
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/roadmap/view-mode"
                                hx-target="#roadmapContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="roadmapPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Roadmap" to see the preview
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
            <div id="roadmapContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/planner.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/planner.svg" alt="Project Roadmap" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

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

            val svg = rmm.makePlannerImage(contents, title, "1.0")
            val compressedPayload = compressString(contents)
            val imageUrl = "https://roach.gy/extension/api/docops/svg?kind=roadmap&payload=${compressedPayload}&type=SVG&useDark=false&title=$title&numChars=$numChars&scale=$scale&filename=planner.svg"

            div = """
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

            val svg = rmm.makePlannerImage(data, title, scale)
            val headers = HttpHeaders()

            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)

        }
        log.info("getRoadMap executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }


}
