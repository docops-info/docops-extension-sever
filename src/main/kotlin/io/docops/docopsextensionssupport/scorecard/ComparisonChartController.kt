package io.docops.docopsextensionssupport.scorecard

import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.json.Json
import org.apache.commons.logging.LogFactory
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/comp")
class ComparisonChartController {

    private val log = LogFactory.getLog(ComparisonChartController::class.java)

    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Counted(value = "docops.comp.put", description = "Creating comparison chart http put")
    @Timed(value = "docops.comp.put", description = "Creating a comparison chart http put", percentiles = [0.5, 0.9])
    fun makePieChart(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            val maker = ComparisonChartMaker()
            val comp = Json.decodeFromString<ComparisonChart>(contents)
            val svg = maker.make(comp)

            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
                <div id='imageblock'>
                $svg
                </div>
                <script>
                var adrSource = `[diag,scale="0.7",role="center"]\n----\n${contents}\n----`;
                </script>
            </div>
        """.trimIndent()
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info("compChart executed in ${timings.duration.inWholeMilliseconds}ms ")
        return timings.value
    }

}