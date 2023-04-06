package io.docops.docopsextensionssupport.web

import io.docops.asciidoctorj.extension.adr.ADRParser
import io.docops.asciidoctorj.extension.adr.AdrMaker
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

@Controller
@RequestMapping("/api")
@Observed(name="adr.controller")
class AdrController() {


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

    @GetMapping("/adr")
    @ResponseBody
    @Timed(value = "docops.adr.get", histogram = true, percentiles = [0.5, 0.95])
    fun getAdr(
        @RequestParam("data") data: String,
        @RequestParam("type") type: String,
        @RequestParam("increaseWidth", required = false, defaultValue = "0") width: String,
        servletResponse: HttpServletResponse
    ): ResponseEntity<ByteArray>{
        val contents = uncompressString(data)
        val config = AdrParserConfig(newWin = true, isPdf = false, lineSize = 75, increaseWidthBy = width.toInt())
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
