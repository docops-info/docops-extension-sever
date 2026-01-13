package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.pie.DonutMaker
import io.docops.docopsextensionssupport.chart.pie.PieSliceMaker
import io.docops.docopsextensionssupport.chart.pie.PieSlices
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
@RequestMapping("/api/pieslice")
class PieSliceController {
    private val log = LogFactory.getLog(PieSliceController::class.java)

    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Counted(value = "docops.boxy.put", description = "Creating a Button using http put")
    @Timed(value = "docops.boxy.put", description = "Creating a Button using http put", percentiles = [0.5, 0.9])
    fun makePieChart(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            val contents = httpServletRequest.getParameter("content")
            val slices = Json.decodeFromString<PieSlices>(contents)
            var svg = ""
            if(!slices.display.donut) {
                val maker = PieSliceMaker()
                svg = maker.makePie(slices)
            } else {
                val maker = DonutMaker()
                svg = maker.makeDonut(slices)
            }
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
        log.info("make pie slices executed in ${timings.duration.inWholeMilliseconds}ms ")
        return timings.value
    }
}