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

import io.docops.docopsextensionssupport.web.panel.uncompressString
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
 */
@Controller
@RequestMapping("/api/connector")
class BoxyController {

    private val log = LogFactory.getLog(BoxyController::class.java)

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
            val scale = httpServletRequest.getParameter("scale")
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
        log.info("makeDiag executed in ${timings.duration.inWholeMilliseconds}ms ")
        return timings.value
    }

    fun fromRequestToConnector(contents: String, scale: Float, useDark: Boolean): String {
        val connectors = Json.decodeFromString<Connectors>(contents)
        val maker = ConnectorMaker(connectors = connectors.connectors, useDark = useDark, "SVG")
        val svg = maker.makeConnectorImage(scale = scale)
        return svg.shapeSvg
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
        log.info("getConnector executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }

}