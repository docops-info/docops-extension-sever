package io.docops.docopsextensionssupport.svgtable

import io.docops.docopsextensionssupport.scorecard.ComparisonChart
import io.docops.docopsextensionssupport.scorecard.ComparisonChartController
import io.docops.docopsextensionssupport.scorecard.ComparisonTableMaker
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
@RequestMapping("/api/table")
class TableController {

    private val log = LogFactory.getLog(TableController::class.java)

    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Counted(value = "docops.table.put", description = "Creating table in svg")
    @Timed(value = "docops.table.put", description = "Creating a table in svg", percentiles = [0.5, 0.9])
    fun createTable(httpServletRequest: HttpServletRequest) : ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            val contents = httpServletRequest.getParameter("content")
            val table = Json.decodeFromString<Table>(contents)
            val svg = table.toSvg()

            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
                <div id='imageblock'>
                $svg
                </div>
            </div>
        """.trimIndent()
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info("compChart executed in ${timings.duration.inWholeMilliseconds}ms ")
        return timings.value
    }
}