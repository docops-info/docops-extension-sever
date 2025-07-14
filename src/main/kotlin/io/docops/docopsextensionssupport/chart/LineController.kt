package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.svgsupport.compressString
import io.docops.docopsextensionssupport.util.UrlUtil
import io.docops.docopsextensionssupport.web.DefaultCsvResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.nio.charset.StandardCharsets
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/linechart", "/api/line")
class LineController {
    private val log = KotlinLogging.logger {  }

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultLineChartJson = """
        title=Department Performance
        width=650
        smooth=true
        darkMode=false
        ---
        Sales | Jan | 40
        Sales | Feb | 70
        Sales | Mar | 90
        Sales | Apr | 70
        Sales | May | 40
        Sales | Jun | 30
        Sales | Jul | 60
        Sales | Aug | 90
        Sales | Sept | 70
        Marketing | Jan | 22
        Marketing | Feb | 33
        Marketing | Mar | 44
        Marketing | Apr | 55
        Marketing | May | 66
        Marketing | Jun | 77
        Marketing | Jul | 88
        Marketing | Aug | 109
        Marketing | Sept | 110
        Development | Jan | 56
        Development | Feb | 65
        Development | Mar | 78
        Development | Apr | 72
        Development | May | 56
        Development | Jun | 94
        Development | Jul | 86
        Development | Aug | 73
        Development | Sept | 70
        """.trimIndent()

        val editModeHtml = """
            <div id="lineChartContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/linechart/" hx-target="#lineChartPreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit Line Chart JSON:</label>
                        <textarea id="content" name="content" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultLineChartJson}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Chart
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/linechart/view-mode"
                                hx-target="#lineChartContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="lineChartPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Chart" to see the preview
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
            <div id="lineChartContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/line.svg" type="image/svg+xml" height="99%" width="99%">
                <img src="images/line.svg" alt="Line Chart" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }
    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Counted(value="docops.boxy.put", description="Creating a Line using http put")
    @Timed(value = "docops.boxy.put", description="Creating a Line using http put", percentiles=[0.5, 0.9])
    fun makeLineChart(httpServletRequest: HttpServletRequest) : ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            val contents = httpServletRequest.getParameter("content")
            val lineChartImproved = LineChartImproved()
            val svg = lineChartImproved.makeLineSvg(contents, DefaultCsvResponse)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val compressedPayload = compressString(contents)
            val imageUrl = UrlUtil.getImageUrl(
                request = httpServletRequest,
                kind = "line",
                payload = compressedPayload,
                type = "SVG",
                useDark = false,
                title = "Title",
                numChars = "24",
                filename = "line.svg"
            )

            val div = """
                <div id='imageblock'>
                $svg
                </div>
                <div class="mb-4">
                    <h3>Image Request</h3>
                    <div class="flex items-center">
                        <input id="imageUrlInput" type="text" value="$imageUrl" readonly class="w-full p-2 border border-gray-300 rounded-l-md text-sm bg-gray-50">
                        <button onclick="copyToClipboard('imageUrlInput')" class="bg-blue-600 text-white px-4 py-2 rounded-r-md hover:bg-blue-700 transition-colors">
                            Copy URL
                        </button>
                    </div>
                </div>
                <script>
                var adrSource = `[docops,line]\n----\n${contents}\n----`;

                function copyToClipboard(elementId) {
                    const element = document.getElementById(elementId);
                    element.select();
                    document.execCommand('copy');

                    // Show a temporary "Copied!" message
                    const button = element.nextElementSibling;
                    const originalText = button.textContent;
                    button.textContent = "Copied!";
                    setTimeout(() => {
                        button.textContent = originalText;
                    }, 2000);
                }
                </script>
        """.trimIndent()
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info{"linechart executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }

    @PostMapping("", produces = ["image/svg+xml"])
    fun lineFromContent(@RequestParam("payload") payload: String): ResponseEntity<ByteArray> {
        val lineChartImproved = LineChartImproved()
        val svg = lineChartImproved.makeLineSvg(payload, DefaultCsvResponse)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}
