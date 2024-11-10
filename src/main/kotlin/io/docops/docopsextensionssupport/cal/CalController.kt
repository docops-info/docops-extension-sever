package io.docops.docopsextensionssupport.cal

import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.json.Json
import org.apache.commons.logging.LogFactory
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/cal")
class CalController {

    private val log = LogFactory.getLog(CalController::class.java)
    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Counted(value="docops.boxy.put", description="Creating a Button using http put")
    @Timed(value = "docops.boxy.put", description="Creating a Button using http put", percentiles=[0.5, 0.9])
    fun makePieChart(httpServletRequest: HttpServletRequest) : ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            val calMaker = CalMaker()
            var svg = ""
            if(contents.trim().isEmpty()) {
                svg = calMaker.makeCalendar(null)
            } else {
                val calEntry = Json.decodeFromString<CalEntry>(contents)
                svg = calMaker.makeCalendar(calEntry)
            }
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
                 <div id='imageblock'>
                $svg
                </div>
            <div class="collapse-content">
                <h3>Calendar Source</h3>
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
}