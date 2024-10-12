package io.docops.docopsextensionssupport.chart


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
@RequestMapping("/api/bar")
class BarController {
    private val log = LogFactory.getLog(BarController::class.java)
    @PutMapping("/barchart")
    @ResponseBody
    @Counted(value="docops.barchart.put", description="Creating a barchart using http put")
    @Timed(value = "docops.barchart.put", description="Creating a barchart using http put", percentiles=[0.5, 0.9])
    fun makeBarChart(httpServletRequest: HttpServletRequest) : ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            val barMaker = BarMaker()
            val bars = Json.decodeFromString<Bar>(contents)
            val svg = barMaker.makeBar(bars)
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

    @PutMapping("/groupbarchart")
    @ResponseBody
    @Counted(value="docops.groupbarchart.put", description="Creating a groupbarchart using http put")
    @Timed(value = "docops.groupbarchart.put", description="Creating a groupbarchart using http put", percentiles=[0.5, 0.9])
    fun makeBarGroupChart(httpServletRequest: HttpServletRequest) : ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            val maker = BarGroupMaker()
            val bars = Json.decodeFromString<BarGroup>(contents)
            val svg = maker.makeBar(bars)
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