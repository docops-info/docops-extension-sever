package io.docops.docopsextensionssupport.web

import io.docops.asciidoctorj.extension.adr.ADRParser
import io.docops.asciidoctorj.extension.adr.AdrMakerNext
import io.docops.asciidoctorj.extension.adr.AdrParserConfig
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import java.nio.charset.StandardCharsets

/**
 * This class is responsible for handling HTTP requests related to ADR (Architectural Decision Records).
 * It provides methods to create and retrieve ADRs.
 *
 * @property adrText The text content of the ADR.
 * @property svg The SVG content of the ADR.
 * @property config The configuration options for parsing and rendering the ADR.
 */
@Controller
@RequestMapping("/api")
@Observed(name="adr.controller")
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
    @PutMapping("/adr", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Timed(value = "docops.adr", histogram = true, percentiles = [0.5, 0.95])
    @Observed(name = "AdrController.adr",
        contextualName = "creating-adr",
        lowCardinalityKeyValues = ["decision", "status"])
    fun adr(@RequestParam("title") title: String,
            @RequestParam("date") date: String,
            @RequestParam("status") status: String,
            @RequestParam("decision") decision: String,
            @RequestParam("consequences") consequences: String,
            @RequestParam("participants") participants: String,
            @RequestParam("context") context: String,
        servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {

            try {
                val adrText = """
                
Title: $title
Date: $date
Status: $status
Context: $context
Decision: $decision
Consequences: $consequences
Participants: $participants 

        """.trimIndent()
                val config = AdrParserConfig(newWin = true, isPdf = false, lineSize = 75, increaseWidthBy = 10)
                val adr = ADRParser().parse(adrText, config)
                var svg = AdrMakerNext().makeAdrSvg(adr, dropShadow = true, config)

                adr.urlMap.forEach { (t, u) ->
                    svg = svg.replace("_${t}_", u)
                }
                val results = makeAdrSource(adrText, svg)
                servletResponse.contentType = "text/html";
                servletResponse.characterEncoding = "UTF-8";
                servletResponse.status = 200
                val writer = servletResponse.writer
                writer.print(results)
                writer.flush()
            } catch (e: Exception) {
                e.printStackTrace()
                throw IllegalArgumentException(e.message, e)
            }

    }
    fun makeAdrSource(txt: String, svg: String): String {
        return """
        <div id='imageblock'>
        $svg
        </div>
        <br/>
        <h3>Adr Source</h3>
        <div class='pure-u-1 pure-u-md-20-24'>
        <pre>
        <code class="kotlin">
        $txt
        </code>
        </pre>
        </div>
        <script>
        var adrSource = `[adr]\n----\n${txt}\n----`;
        document.querySelectorAll('pre code').forEach((el) => {
            hljs.highlightElement(el);
        });
        </script>
    """.trimIndent()
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
    @GetMapping("/adr")
    @ResponseBody
    @Timed(value = "docops.adr.get", histogram = true, percentiles = [0.5, 0.95])
    fun getAdr(
        @RequestParam("data") data: String,
        @RequestParam("type") type: String,
        @RequestParam("increaseWidth", required = false, defaultValue = "0") width: String,
        @RequestParam("scale", required = false, defaultValue = "1.0") scale: String,
        servletResponse: HttpServletResponse
    ): ResponseEntity<ByteArray>{
        val contents = uncompressString(data)
        val config = AdrParserConfig(newWin = true, isPdf = false, lineSize = 90, increaseWidthBy = width.toInt(), scale = scale.toFloat())
        val adr = ADRParser().parse(contents, config)
        var svg = AdrMakerNext().makeAdrSvg(adr, dropShadow = true, config)
        adr.urlMap.forEach { (t, u) ->
            svg = svg.replace("_${t}_", u)
        }
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}
