package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.time.measureTimedValue

/**
 * Controller for handling quadrant chart requests.
 */
@Controller
@RequestMapping("/api/quadrant")
class QuadrantController {
    private val log = KotlinLogging.logger {}

    /**
     * Returns the edit mode HTML for the quadrant chart.
     */
    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val editModeHtml = """
            <div id="quadrantContainer" class="bg-gray-50 rounded-lg p-4">
                <div class="mb-4">
                    <div class="flex flex-wrap -mx-2">
                        <div class="w-full px-2 mb-4">
                            <textarea id="quadrantInput" rows="12" class="w-full px-3 py-2 text-gray-700 border rounded-lg focus:outline-none focus:border-blue-500" placeholder="Enter quadrant data...">title: Strategic Priority Matrix
subtitle: Impact vs. Effort Analysis
---
Label | X | Y | Size | Color | Description
Feature A | 75 | 80 | 8 | #10b981 | High impact, low effort
Feature B | 30 | 85 | 10 | #3b82f6 | High impact, high effort
Feature C | 20 | 30 | 7 | #f59e0b | Low impact, low effort
Feature D | 85 | 25 | 9 | #ef4444 | Low impact, high effort</textarea>
                        </div>
                        <div class="w-full md:w-1/2 px-2 mb-4">
                            <label class="block text-gray-700 text-sm font-bold mb-2" for="scaleInput">
                                Scale
                            </label>
                            <input id="scaleInput" type="number" min="0.1" max="2" step="0.1" value="1.0" class="w-full px-3 py-2 text-gray-700 border rounded-lg focus:outline-none focus:border-blue-500">
                        </div>
                        <div class="w-full md:w-1/2 px-2 mb-4">
                            <label class="block text-gray-700 text-sm font-bold mb-2" for="darkModeCheckbox">
                                Dark Mode
                            </label>
                            <div class="flex items-center">
                                <input id="darkModeCheckbox" type="checkbox" class="form-checkbox h-5 w-5 text-blue-600">
                                <span class="ml-2 text-gray-700">Enable Dark Mode</span>
                            </div>
                        </div>
                    </div>
                    <div class="flex justify-end">
                        <button id="generateQuadrantButton" class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline">
                            Generate Quadrant Chart
                        </button>
                    </div>
                </div>
                <div id="quadrantResult" class="mt-4 border rounded-lg p-4 min-h-64 flex items-center justify-center">
                    <p class="text-gray-500">Quadrant chart will appear here...</p>
                </div>
                <div class="mt-4">
                    <h3 class="text-lg font-semibold mb-2">How to Use</h3>
                    <p class="text-gray-700 mb-2">Enter your quadrant data in the text area above. You can use the following format:</p>
                    <pre class="bg-gray-100 p-2 rounded text-sm mb-2">
title: Your Chart Title
subtitle: Your Chart Subtitle
q1Label: Custom Q1 Label
q2Label: Custom Q2 Label
q3Label: Custom Q3 Label
q4Label: Custom Q4 Label
q1Description: Custom Q1 Description
q2Description: Custom Q2 Description
q3Description: Custom Q3 Description
q4Description: Custom Q4 Description
---
Label | X | Y | Size | Color | Description
Item 1 | 75 | 80 | 8 | #10b981 | Description for Item 1
Item 2 | 30 | 85 | 10 | #3b82f6 | Description for Item 2
                    </pre>
                    <p class="text-gray-700">
                        <strong>X and Y values:</strong> Range from 0 to 100, where X represents effort (0 = low, 100 = high) and Y represents impact (0 = low, 100 = high).<br>
                        <strong>Size:</strong> Optional. Size of the data point (default is 8).<br>
                        <strong>Color:</strong> Optional. Hex color code for the data point.<br>
                        <strong>Description:</strong> Optional. Additional description for the data point.
                    </p>
                </div>
            </div>
            <script>
                document.getElementById('generateQuadrantButton').addEventListener('click', function() {
                    const quadrantInput = document.getElementById('quadrantInput').value;
                    const scale = document.getElementById('scaleInput').value;
                    const useDark = document.getElementById('darkModeCheckbox').checked;
                    const quadrantResult = document.getElementById('quadrantResult');

                    quadrantResult.innerHTML = '<p class="text-gray-500">Generating quadrant chart...</p>';

                    // Compress and encode the input
                    const compressedInput = btoa(quadrantInput);

                    // Make the API request
                    fetch('api/docops/svg?kind=quadrant&payload=' + encodeURIComponent(compressedInput) + '&scale=' + scale + '&useDark=' + useDark)
                        .then(response => {
                            if (!response.ok) {
                                throw new Error('Network response was not ok');
                            }
                            return response.text();
                        })
                        .then(data => {
                            quadrantResult.innerHTML = data;
                        })
                        .catch(error => {
                            quadrantResult.innerHTML = '<p class="text-red-500">Error: ' + error.message + '</p>';
                        });
                });
            </script>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(editModeHtml, headers, HttpStatus.OK)
    }

    /**
     * Returns the view mode HTML for the quadrant chart.
     */
    @GetMapping("/view-mode")
    @ResponseBody
    fun getViewMode(): ResponseEntity<String> {
        val viewModeHtml = """
            <div id="quadrantContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/quad1.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/quad1.svg" alt="Quadrant Chart" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    /**
     * Generates a quadrant chart from the request and returns it as a response entity.
     */
    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Counted(value="docops.quadrant.put", description="Creating a Quadrant Chart using http put")
    @Timed(value = "docops.quadrant.put", description="Creating a Quadrant Chart using http put", percentiles=[0.5, 0.9])
    fun makeQuadrant(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            var title = "title"
            if (contents.isNullOrEmpty()) {
                contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
                title = httpServletRequest.getParameter("title") ?: ""
            }
            var scale = httpServletRequest.getParameter("scale")
            if(scale.isNullOrEmpty()) {
                scale = "1.0"
            }
            val useDarkInput = httpServletRequest.getParameter("useDark")
            val svg = fromRequestToQuadrant(contents = contents, scale = scale.toFloat(), useDark = "on" == useDarkInput, title = title)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
                <div id='imageblock'>
                $svg
                </div>
                <div class="collapse-content">
                    <h3>Quadrant Source</h3>
                    <div>
                    <pre>
                    <code class="json">
                     $contents
                    </code>
                    </pre>
                    </div>
                    <script>
                    var quadrantSource = `[quadrant,scale="0.7",role="center"]\n----\n${contents}\n----`;
                    document.querySelectorAll('pre code').forEach((el) => {
                        hljs.highlightElement(el);
                    });
                    </script>
                </div>
            """.lines().joinToString(transform = String::trim, separator = "\n")
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info{"makeQuadrant executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }

    /**
     * Converts the request payload to a quadrant chart and generates the SVG.
     */
    fun fromRequestToQuadrant(contents: String, scale: Float, useDark: Boolean, title: String = ""): String {
        val handler = QuadrantHandler()
        val response = handler.fromRequestToQuadrant(contents, scale = scale, useDark = useDark, title = title)
        return response.shapeSvg
    }

    /**
     * Retrieves the quadrant chart based on the provided parameters.
     */
    @Traceable
    @GetMapping("/")
    @ResponseBody
    @Counted(value = "docops.quadrant.get", description="Creating a quadrant chart using http get")
    @Timed(value = "docops.quadrant.get", description="Creating a quadrant chart using http get", percentiles=[0.5, 0.9])
    fun getQuadrant(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "scale", defaultValue = "1.0") scale: String,
        @RequestParam("type", required = false, defaultValue = "SVG") type: String,
        @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
        @RequestParam(name = "title", defaultValue = "") title: String,
        @RequestParam(name = "numChars", defaultValue = "24") numChars: String,
        @RequestParam(name = "backend", defaultValue = "html") backend: String
    ): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val svg = fromRequestToQuadrant(data, scale = scale.toFloat(), useDark = useDark, title = title)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)
        }
        log.info{"getQuadrant executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }
}
