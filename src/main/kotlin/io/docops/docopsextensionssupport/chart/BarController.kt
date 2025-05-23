package io.docops.docopsextensionssupport.chart


import io.github.oshai.kotlinlogging.KotlinLogging
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/bar")
class BarController {
    private val log = KotlinLogging.logger {  }

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultBarChartJson = """
        {
          "title": "Berry Picking by Month 2024",
          "yLabel": "Number of Sales",
          "xLabel": "Month",
          "series": [
            {"label": "Jan","value": 120.0},
            {"label": "Feb","value": 334.0},
            {"label": "Mar","value": 455.0},
            {"label": "Apr","value": 244.0},
            {"label": "May","value": 256.0},
            {"label": "Jun","value": 223.0}
          ],
          "display": {"baseColor": "#4361ee","vBar": true}
        }
        """.trimIndent()

        val editModeHtml = """
            <div id="barChartContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/bar/barchart" hx-target="#barChartPreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit Bar Chart JSON:</label>
                        <textarea id="content" name="content" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultBarChartJson}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Chart
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/bar/view-mode"
                                hx-target="#barChartContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="barChartPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
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
            <div id="barChartContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/bar.svg" type="image/svg+xml" height="99%" width="99%">
                <img src="images/bar.svg" alt="Bar Chart" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    @Traceable
    @PutMapping("/barchart")
    @ResponseBody
    @Counted(value="docops.barchart.put", description="Creating a barchart using http put")
    @Timed(value = "docops.barchart.put", description="Creating a barchart using http put", percentiles=[0.5, 0.9])
    fun makeBarChart(httpServletRequest: HttpServletRequest) : ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            val barMaker = BarMaker()
            val bars = Json.decodeFromString<Bar>(contents)
            var svg = ""
            if(bars.display.vBar) {
                svg = barMaker.makeVerticalBar(bars)

            } else {
                svg = barMaker.makeHorizontalBar(bars)
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
        log.info{"makeDiag executed in ${timings.duration.inWholeMilliseconds}ms "}
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
            val svg = if(bars.display.vBar) {
                maker.makeVGroupBar(bars)
            } else if (bars.display.condensed) {
                maker.makeCondensed(bars)
            }
            else {
                maker.makeBar(bars)
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
        log.info{"makeDiag executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }
}
