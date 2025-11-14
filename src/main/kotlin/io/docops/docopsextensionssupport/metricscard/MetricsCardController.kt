package io.docops.docopsextensionssupport.metricscard

import io.docops.docopsextensionssupport.web.DefaultCsvResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.CacheControl
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/metricscard")
class MetricsCardController {
    private val log = KotlinLogging.logger {  }
    private val metricsCardHandler = MetricsCardHandler(DefaultCsvResponse)

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultMetricsCardContent = """
        title= Q2 2024 Business Metrics
        ---
        Metric | Value | Sublabel
        Revenue | $4.2M | 18% YoY Growth
        New Customers | 156 | 42 Enterprise
        Customer Retention | 94% | 2% Improvement
        NPS Score | 72 | Industry Leading
        """.trimIndent()

        val editModeHtml = """
            <div id="metricsCardContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/metricscard/render" hx-target="#metricsCardPreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit Metrics Card Content:</label>
                        <textarea id="content" name="content" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultMetricsCardContent}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Metrics Card
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/metricscard/view-mode"
                                hx-target="#metricsCardContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="metricsCardPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Metrics Card" to see the preview
                        </div>
                    </div>
                </form>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(editModeHtml, headers, HttpStatus.OK)
    }

    @GetMapping("/view-mode")
    @ResponseBody
    fun getViewMode(): ResponseEntity<String> {
        val viewModeHtml = """
            <div id="metricsCardContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/metrics.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/metrics.svg" alt="Scorecard" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    @PutMapping("/render")
    @ResponseBody
    fun renderMetricsCard(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            val content = httpServletRequest.getParameter("content")
            val maker = MetricsCardMaker(DefaultCsvResponse, false)
            val svg = maker.createCards(content, 650, 300, false)


            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
                <div id='imageblock'>
                $svg
                </div>
                <script>
                var metricsCardSource = `[docops,metricscard]\n----\n${content}\n----`;
                </script>
            """.trimIndent()
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info{"metrics card render executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }

    @PostMapping("")
    fun editFormSubmission(@RequestParam("payload") payload: String) : ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            val maker = MetricsCardMaker(DefaultCsvResponse, false)
            val svg = maker.createCards(payload, 650, 300, false)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            ResponseEntity(svg.first.toByteArray(), headers, HttpStatus.OK)
        }
        return timings.value
    }
}
