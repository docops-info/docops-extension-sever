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
@RequestMapping("/api/linechart")
class LineController {
    private val log = KotlinLogging.logger {  }

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultLineChartJson = """
        {
          "title": "Point on graph",
          "points": [
            {
              "label": "Sales",
              "points": [
                {"label": "Jan","y": 40.0},
                {"label": "Feb","y": 70.0},
                {"label": "Mar","y": 90.0},
                {"label": "Apr","y": 70.0},
                {"label": "May","y": 40.0},
                {"label": "Jun","y": 30.0},
                {"label": "Jul","y": 60.0},
                {"label": "Aug","y": 90.0},
                {"label": "Sept","y": 70.0}
              ]
            },
            {
              "label": "Marketing",
              "points": [
                {"label": "Jan","y": 22.0},
                {"label": "Feb","y": 33.0},
                {"label": "Mar","y": 44.0},
                {"label": "Apr","y": 55.0},
                {"label": "May","y": 66.0},
                {"label": "Jun","y": 77.0},
                {"label": "Jul","y": 88.0},
                {"label": "Aug","y": 109.0},
                {"label": "Sept","y": 110.0}
              ]
            },
            {
              "label": "Divest",
              "points": [
                {"label": "Jan","y": 56.0},
                {"label": "Feb","y": 65.0},
                {"label": "Mar","y": 78.0},
                {"label": "Apr","y": 72.0},
                {"label": "May","y": 56.0},
                {"label": "Jun","y": 94.0},
                {"label": "Jul","y": 86.0},
                {"label": "Aug","y": 73.0},
                {"label": "Sept","y": 70.0}
              ]
            }
          ], "display": {"smoothLines": true}
        }
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
            val maker = LineChartMaker(false)
            val chart = Json.decodeFromString<LineChart>(contents)
            val svg = maker.makeLineChart(chart)
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
        log.info{"linechart executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }
}
