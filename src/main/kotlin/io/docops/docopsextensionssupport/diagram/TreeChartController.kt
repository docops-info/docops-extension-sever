package io.docops.docopsextensionssupport.diagram

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.nio.charset.Charset
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/treechart")
class TreeChartController {

    private val log = KotlinLogging.logger {  }

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultTreeChartData = """
        title=Organization Chart
        width=800
        height=600
        orientation=vertical
        collapsible=true
        expanded=true
        darkMode=false
        ---
        CEO
            CTO | #3498db
                Engineering Manager
                    Senior Developer
                    Developer
                    Junior Developer
                QA Manager
                    QA Engineer
                    QA Analyst
            CFO | #2ecc71
                Finance Manager
                    Accountant
                    Financial Analyst
            CMO | #e74c3c
                Marketing Manager
                    Marketing Specialist
                    Content Creator
        """.trimIndent()

        val editModeHtml = """
            <div id="treeChartContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/treechart/" hx-target="#treeChartPreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit Tree Chart Data:</label>
                        <textarea id="content" name="content" rows="20" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultTreeChartData}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Tree Chart
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/treechart/view-mode"
                                hx-target="#treeChartContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="treeChartPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[300px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Tree Chart" to see the preview
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
            <div id="treeChartContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/treechart.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/treechart.svg" alt="Tree Chart" class="max-h-full max-w-full" />
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
    @Counted(value="docops.treechart.put", description="Creating a Tree Chart using http put")
    @Timed(value = "docops.treechart.put", description="Creating a Tree Chart using http put", percentiles=[0.5, 0.9])
    fun makeTreeChart(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            if (contents.isNullOrEmpty()) {
                contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
            }

            val treeMaker = TreeMaker()
            val svg = treeMaker.makeTree(contents, false)

            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
                <div id='imageblock'>
                $svg
                </div>
                
            """.lines().joinToString(transform = String::trim, separator = "\n")
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info{"makeTreeChart executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }

    @Traceable
    @GetMapping("/")
    @ResponseBody
    @Counted(value = "docops.treechart.get", description="Creating a tree chart using http get")
    @Timed(value = "docops.treechart.get", description="Creating a tree chart using http get", percentiles=[0.5, 0.9])
    fun getTreeChart(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "backend", defaultValue = "html") backend: String
    ): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val handler = TreeChartHandler()
            handler.handleSVG(payload, "pdf".equals(backend, ignoreCase = true))
        }
        log.info{"getTreeChart executed in ${timing.duration.inWholeMilliseconds}ms "}
        return ResponseEntity(timing.value.toByteArray(), HttpStatus.OK)
    }
}