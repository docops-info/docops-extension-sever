package io.docops.docopsextensionssupport.web

import io.docops.asciidoctorj.extension.adr.ADRParser
import io.docops.asciidoctorj.extension.adr.AdrMaker
import io.docops.docopsextensionssupport.aop.LogExecution
import io.github.wimdeblauwe.hsbt.mvc.HtmxResponse
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.lang.Exception

@Controller
@RequestMapping("/api")
@LogExecution
class AdrController(private val observationRegistry: ObservationRegistry) {


    @PutMapping("/adr", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody

    fun adr(@RequestParam("title") title: String,
            @RequestParam("date") date: String,
            @RequestParam("status") status: String,
            @RequestParam("decision") decision: String,
            @RequestParam("consequences") consequences: String,
            @RequestParam("participants") participants: String,
            @RequestParam("context") context: String,
        servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        return Observation.createNotStarted("docops.adr", observationRegistry).observe {
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
                val adr = ADRParser().parse(adrText)
                var svg = (AdrMaker().makeAdrSvg(adr))
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
}
