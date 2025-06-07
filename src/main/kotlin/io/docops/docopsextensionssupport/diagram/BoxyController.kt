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

package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.json.Json
import org.apache.commons.logging.LogFactory
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.time.measureTimedValue

/**
 * The BoxyController class handles HTTP requests related to boxy connectors and images.
 * Supports both JSON and table format for connectors.
 */
@Controller
@RequestMapping("/api/connector")
class BoxyController {

    private val log = KotlinLogging.logger {  }

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultConnectorJson = """
        {
          "connectors": [
            {"text": "Engineer","description": "Creates tests"},
            {"text": "Unit Tests","description": "Run Unit Tests"},
            {"text": "GitHub","description": "Upload to Github"},
            {"text": "Test Engine","description": "GitHub webhook plugged into engine"},
            {"text": "GitHub","description": "Results stored in Github"},
            {"text": "API Documentation","description": "API documentation ready for consumption"}
          ]
        }
        """.trimIndent()

        val defaultTableFormat = """
        ---
        Text | Description | Color
        Engineer | Creates tests | #E14D2A
        Unit Tests | Run Unit Tests | #3E6D9C
        GitHub | Upload to Github | #7286D3
        Test Engine | GitHub webhook plugged into engine | #8EA7E9
        GitHub | Results stored in Github | #7286D3
        API Documentation | API documentation ready for consumption | #FFA41B
        """.trimIndent()

        val editModeHtml = """
            <div id="connectorContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/connector/" hx-target="#connectorPreview" class="space-y-4">
                    <div class="mb-4">
                        <div class="flex justify-between items-center mb-2">
                            <label for="formatSelector" class="block text-sm font-medium text-gray-700">Format:</label>
                            <div class="flex space-x-2">
                                <button type="button" id="jsonFormatBtn" class="text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-xs px-3 py-1 text-center active">
                                    JSON
                                </button>
                                <button type="button" id="tableFormatBtn" class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-xs px-3 py-1 text-center">
                                    Table
                                </button>
                            </div>
                        </div>
                        <textarea id="content" name="content" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultConnectorJson}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Connector
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/connector/view-mode"
                                hx-target="#connectorContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="connectorPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Connector" to see the preview
                        </div>
                    </div>
                    <script>
                        document.getElementById('jsonFormatBtn').addEventListener('click', function() {
                            document.getElementById('content').value = `${defaultConnectorJson.replace("`", "\\`")}`;
                            document.getElementById('jsonFormatBtn').classList.add('text-white', 'bg-blue-600', 'hover:bg-blue-700');
                            document.getElementById('jsonFormatBtn').classList.remove('text-gray-700', 'bg-gray-200', 'hover:bg-gray-300');
                            document.getElementById('tableFormatBtn').classList.add('text-gray-700', 'bg-gray-200', 'hover:bg-gray-300');
                            document.getElementById('tableFormatBtn').classList.remove('text-white', 'bg-blue-600', 'hover:bg-blue-700');
                        });

                        document.getElementById('tableFormatBtn').addEventListener('click', function() {
                            document.getElementById('content').value = `${defaultTableFormat.replace("`", "\\`")}`;
                            document.getElementById('tableFormatBtn').classList.add('text-white', 'bg-blue-600', 'hover:bg-blue-700');
                            document.getElementById('tableFormatBtn').classList.remove('text-gray-700', 'bg-gray-200', 'hover:bg-gray-300');
                            document.getElementById('jsonFormatBtn').classList.add('text-gray-700', 'bg-gray-200', 'hover:bg-gray-300');
                            document.getElementById('jsonFormatBtn').classList.remove('text-white', 'bg-blue-600', 'hover:bg-blue-700');
                        });
                    </script>
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
            <div id="connectorContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/connector.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/connector.svg" alt="Flow Connectors" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    /**
     * Generates a diagnostic image and returns it as a response entity.
     *
     * @param httpServletRequest The HttpServletRequest containing the request parameters.
     * @return A ResponseEntity containing the diagnostic image as a byte array.
     */
    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Counted(value="docops.boxy.put", description="Creating a Button using http put")
    @Timed(value = "docops.boxy.put", description="Creating a Button using http put", percentiles=[0.5, 0.9])
    fun makeDiag(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            var title = "title"
            if (contents.isNullOrEmpty()) {
                contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
                title = httpServletRequest.getParameter("title")
            }
            var scale = httpServletRequest.getParameter("scale")
            if(scale.isNullOrEmpty()) {
                scale = "1.0"
            }
            val useDarkInput = httpServletRequest.getParameter("useDark")
            val svg = fromRequestToConnector(contents = contents, scale =  scale.toFloat(), useDark = "on" == useDarkInput)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
                <div id='imageblock'>
                $svg
                </div>
            <div class="collapse-content">
                <h3>Adr Source</h3>
                <div>
                <pre>
                <code class="json">
                 $contents
                </code>
                </pre>
                </div>
                <script>
                var adrSource = `[diag,scale="0.7",role="center"]\n----\n${contents}\n----`;
                document.querySelectorAll('pre code').forEach((el) => {
                    hljs.highlightElement(el);
                });
                </script>
            </div>
        """.lines().joinToString(transform = String::trim, separator = "\n")
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info{"makeDiag executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }

    fun fromRequestToConnector(contents: String, scale: Float, useDark: Boolean): String {
        val handler = ConnectorHandler()
        val response = handler.handleSVG(contents, "SVG", scale.toString(), useDark)
        return String(response.body ?: ByteArray(0))
    }
    /**
     * Retrieves the connector based on the provided payload, scale, type, useDark, and outlineColor parameters.
     *
     * @param payload The encoded payload string.
     * @param scale The scale value used to determine the size of the connector image.
     * @param type The type of the connector image. Default value is "SVG".
     * @param useDark A flag indicating whether to use a dark theme for the connector image. Default value is false.
     * @param outlineColor The color of the connector outline. Default value is "#37cdbe".
     * @return A ResponseEntity containing the byte array representation of the connector image.
     */
    @Traceable
    @GetMapping("/")
    @ResponseBody
    @Counted(value = "docops.connector.get", description="Creating a connector diagram using http get")
    @Timed(value = "docops.connector.get", description="Creating a connector diagram using http get", percentiles=[0.5, 0.9])
    fun getConnector(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "scale", defaultValue = "1.0") scale: String,
        @RequestParam("type", required = false, defaultValue = "SVG") type: String,
        @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
        @RequestParam(name = "outlineColor", defaultValue = "#37cdbe") outlineColor: String
    ): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val svg = fromRequestToConnector(data, scale = scale.toFloat(), useDark = useDark)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)

        }
        log.info{"getConnector executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }

}
