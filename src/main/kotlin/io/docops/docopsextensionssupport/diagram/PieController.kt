package io.docops.docopsextensionssupport.diagram

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
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/piechart")
class PieController {
    private val log = LogFactory.getLog(PieController::class.java)

    @PutMapping("/")
    @ResponseBody
    @Counted(value="docops.boxy.put", description="Creating a Button using http put")
    @Timed(value = "docops.boxy.put", description="Creating a Button using http put", percentiles=[0.5, 0.9])
    fun makePieChart(httpServletRequest: HttpServletRequest) : ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            val calMaker = PieMaker()
            val pies = Json.decodeFromString<Pies>(contents)
            val svg = calMaker.makePies(pies)
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

        """.trimIndent()
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info("makeDiag executed in ${timings.duration.inWholeMilliseconds}ms ")
        return timings.value
    }
}