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

package io.docops.docopsextensionssupport.web


import io.docops.docopsextensionssupport.adr.AdrParser
import io.docops.docopsextensionssupport.adr.AdrSvgGenerator
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * This class is responsible for handling HTTP requests related to ADR (Architectural Decision Records).
 * It provides methods to create and retrieve ADRs.
 *
 * @property svg The SVG content of the ADR.
 */
@Controller
@RequestMapping("/api")
class AdrController() {

    @GetMapping("/adr/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultAdrContent = """title: Use Elasticsearch for Search Functionality
status: Accepted
date: 2024-05-15
context:
- Our application needs robust search capabilities across multiple data types
- We need to support full-text search with relevance ranking
- The search functionality must scale with growing data volumes
- We need to support faceted search and filtering
decision:
- We will use Elasticsearch as our search engine
- We will integrate it with our existing PostgreSQL database
- We will implement a synchronization mechanism to keep data in sync
consequences:
- Improved search performance and capabilities
- Additional infrastructure to maintain
- Need for expertise in Elasticsearch configuration and optimization
- Potential complexity in keeping data synchronized
participants:
Jane Smith (Architect), John Doe (Developer), Alice Johnson (Product Manager)"""

        val editModeHtml = """
            <div id="adrContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/adr" hx-target="#adrPreview" class="space-y-4">
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label for="title" class="block text-sm font-medium text-gray-700 mb-1">Title:</label>
                            <input type="text" id="title" name="title" value="Use Elasticsearch for Search Functionality" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">
                        </div>
                        <div>
                            <label for="status" class="block text-sm font-medium text-gray-700 mb-1">Status:</label>
                            <select id="status" name="status" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">
                                <option value="Proposed">Proposed</option>
                                <option value="Accepted" selected>Accepted</option>
                                <option value="Superseded">Superseded</option>
                                <option value="Deprecated">Deprecated</option>
                                <option value="Rejected">Rejected</option>
                            </select>
                        </div>
                    </div>
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label for="date" class="block text-sm font-medium text-gray-700 mb-1">Date:</label>
                            <input type="text" id="date" name="date" value="2024-05-15" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">
                        </div>
                        <div>
                            <label for="participants" class="block text-sm font-medium text-gray-700 mb-1">Participants:</label>
                            <input type="text" id="participants" name="participants" value="Jane Smith (Architect), John Doe (Developer), Alice Johnson (Product Manager)" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">
                        </div>
                    </div>
                    <div>
                        <label for="context" class="block text-sm font-medium text-gray-700 mb-1">Context:</label>
                        <textarea id="context" name="context" rows="4" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">- Our application needs robust search capabilities across multiple data types
- We need to support full-text search with relevance ranking
- The search functionality must scale with growing data volumes
- We need to support faceted search and filtering</textarea>
                    </div>
                    <div>
                        <label for="decision" class="block text-sm font-medium text-gray-700 mb-1">Decision:</label>
                        <textarea id="decision" name="decision" rows="3" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">- We will use Elasticsearch as our search engine
- We will integrate it with our existing PostgreSQL database
- We will implement a synchronization mechanism to keep data in sync</textarea>
                    </div>
                    <div>
                        <label for="consequences" class="block text-sm font-medium text-gray-700 mb-1">Consequences:</label>
                        <textarea id="consequences" name="consequences" rows="4" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">- Improved search performance and capabilities
- Additional infrastructure to maintain
- Need for expertise in Elasticsearch configuration and optimization
- Potential complexity in keeping data synchronized</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update ADR
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/adr/view-mode"
                                hx-target="#adrContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="adrPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update ADR" to see the preview
                        </div>
                    </div>
                </form>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(editModeHtml, headers, HttpStatus.OK)
    }

    @GetMapping("/adr/view-mode")
    @ResponseBody
    fun getViewMode(): ResponseEntity<String> {
        val viewModeHtml = """
            <div id="adrContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/adr.svg" type="image/svg+xml" class="max-h-full max-w-full">
                <img src="images/adr.svg" alt="Architecture Decision Record" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }


    /**
     * Handles the HTTP PUT request for creating an Architecture Decision Record (ADR).
     *
     * @param title        The title of the ADR.
     * @param date         The date of the ADR.
     * @param status       The status of the ADR.
     * @param decision     The decision made in the ADR.
     * @param consequences The consequences of the decision in the ADR.
     * @param participants The participants involved in the ADR.
     * @param context      The*/
    @Traceable(description = "Handles the HTTP PUT request for creating an Architecture Decision Record (ADR).")
    @PutMapping("/adr", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Timed(value = "docops.adr.put", description = "Creating adr from web form", percentiles=[0.5, 0.9])
    @Counted(value = "docops.adr.put", description = "Creating adr from web form")
    fun adr(@RequestParam("title") title: String,
            @RequestParam("date") date: String,
            @RequestParam("status") status: String,
            @RequestParam("decision") decision: String,
            @RequestParam("consequences") consequences: String,
            @RequestParam("participants") participants: String,
            @RequestParam("context") context: String,
        servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {

            try {
                val adrText = adrFromTemplate(title, date, status, context, decision, consequences, participants)
                val generator = AdrSvgGenerator()
                val adr = AdrParser().parseContent(adrText)
                val svg = generator.generate(adr)

                val results = makeAdrSource(adrText, svg).lines().joinToString(transform = String::trim, separator = "\n")
                servletResponse.contentType = "text/html"
                servletResponse.characterEncoding = "UTF-8"
                //servletResponse.addHeader("HX-Push-Url", "adrbuilder.html")
                servletResponse.status = 200
                val writer = servletResponse.writer
                writer.print(results)
                writer.flush()
            } catch (e: Exception) {
                e.printStackTrace()
                throw IllegalArgumentException(e.message, e)
            }

    }

    private fun adrFromTemplate(
        title: String,
        date: String,
        status: String,
        context: String,
        decision: String,
        consequences: String,
        participants: String
    ): String {
        val adrText = """
    Title: $title
    Date: $date
    Status: $status
    Context: $context
    Decision: $decision
    Consequences: $consequences
    Participants: $participants 
            """.trimIndent()
        return adrText
    }

    fun makeAdrSource(txt: String, svg: String): String {
        //language=html
        return """
            <div id='imageblock'>
                $svg
             </div>
            <div class="">
                <h3>Adr Source</h3>
                <div>
                <pre class='scrollbar-none overflow-x-auto p-6 text-sm leading-snug text-white bg-black bg-opacity-75 kotlin hljs language-kotlin'>
                <code>
                [docops,adr]
                ----
                ${txt.lines().joinToString(transform = String::trim, separator = "\n")}
                ----
                </code>
                </pre>
                </div>
                <script>
                var adrSource = `[docops,adr]\n----\n${txt}\n----`;
                document.querySelectorAll('pre code').forEach((el) => {
                hljs.highlightElement(el);
                });
                </script>
            </div>


    """.trimMargin()
    }

    /**
     * Retrieves the ADR (Adverse Drug Reaction) data in SVG format.
     *
     * @param data The compressed ADR data.
     * @param type The type of ADR data.
     * @param width The increase in width of the ADR.
     *      Default value: "0"
     * @param scale The scale of the ADR.
     *      Default value: "1.0"
     * @param servletResponse The servlet response object.
     * @return The ADR data in SVG format as a byte array.
     */
    @Traceable
    @GetMapping("/adr")
    @ResponseBody
    @Timed(value = "docops.adr.get", description="docops adr from asciidoctor plugin", percentiles=[0.5, 0.9])
    @Counted(value = "docops.adr.get", description="docops adr from asciidoctor plugin")
    fun getAdr(
        @RequestParam("data") data: String,
        @RequestParam("type") type: String,
        @RequestParam("increaseWidth", required = false, defaultValue = "0") width: String,
        @RequestParam("lineSize", required = false, defaultValue = "80") lineSize: String,
        @RequestParam("scale", required = false, defaultValue = "1.0") scale: String,
        servletResponse: HttpServletResponse
    ): ResponseEntity<ByteArray>{
        val contents = uncompressString(data)
        val generator = AdrSvgGenerator()
        val adr = AdrParser().parseContent(contents)
        val svg = generator.generate(adr)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }

    @Traceable
    @GetMapping("/adr/summary/table")
    @ResponseBody
    fun getAdrRow(@RequestParam("payload") payload: String): ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val generator = AdrSvgGenerator()
        val adr = AdrParser().parseContent(data)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.TEXT_PLAIN
        return ResponseEntity("${adr.title}~${adr.participants}~${adr.date}".toByteArray(), headers, HttpStatus.OK)
    }
}
