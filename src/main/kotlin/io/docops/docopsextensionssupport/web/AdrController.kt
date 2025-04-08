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

import io.docops.docopsextensionssupport.adr.ADRParser
import io.docops.docopsextensionssupport.adr.AdrMaker
import io.docops.docopsextensionssupport.adr.AdrParserConfig
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
                val config = AdrParserConfig(newWin = true, isPdf = false, lineSize = 75, increaseWidthBy = 10)
                val adr = ADRParser().parse(adrText, config)
                var svg = AdrMaker().makeAdrSvg(adr, config = config, useDark = false)

                adr.urlMap.forEach { (t, u) ->
                    svg = svg.replace("_${t}_", u)
                }
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
        val config = AdrParserConfig(newWin = true, isPdf = false, lineSize = lineSize.toInt(), increaseWidthBy = width.toInt(), scale = scale.toFloat())
        val adr = ADRParser().parse(contents, config)
        var svg = AdrMaker().makeAdrSvg(adr, config = config, useDark = true)
        adr.urlMap.forEach { (t, u) ->
            svg = svg.replace("_${t}_", u)
        }
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }

    @Traceable
    @GetMapping("/adr/summary/table")
    @ResponseBody
    fun getAdrRow(@RequestParam("payload") payload: String): ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val config = AdrParserConfig(newWin = true, isPdf = false, lineSize = 80, increaseWidthBy = 0, scale = 1.0f)
        val adr = ADRParser().parse(data, config)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.TEXT_PLAIN
        return ResponseEntity("${adr.title}~${adr.participantAsStr()}~${adr.date}".toByteArray(), headers, HttpStatus.OK)
    }
}
