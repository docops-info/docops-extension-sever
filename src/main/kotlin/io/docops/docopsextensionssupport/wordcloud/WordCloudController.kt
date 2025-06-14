package io.docops.docopsextensionssupport.wordcloud

import io.github.oshai.kotlinlogging.KotlinLogging
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
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/wordcloud")
class WordCloudController {
    private val log = KotlinLogging.logger {  }
    private val wordCloudHandler = WordCloudHandler()

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultWordCloudContent = """
        title= Technology Trends 2024
        width= 600
        height= 400
        minFontSize= 12
        maxFontSize= 60
        ---
        Cloud Computing | 85 | #3498db
        Artificial Intelligence | 92 | #9b59b6
        Machine Learning | 78 | #2ecc71
        Blockchain | 65 | #e74c3c
        IoT | 70 | #f39c12
        Cybersecurity | 88 | #1abc9c
        Big Data | 75 | #34495e
        DevOps | 68 | #e67e22
        Quantum Computing | 55 | #27ae60
        Edge Computing | 60 | #d35400
        5G | 72 | #3498db
        AR/VR | 58 | #9b59b6
        """.trimIndent()

        val editModeHtml = """
            <div id="wordCloudContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/wordcloud/render" hx-target="#wordCloudPreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit Word Cloud Content:</label>
                        <textarea id="content" name="content" rows="15" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultWordCloudContent}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Word Cloud
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/wordcloud/view-mode"
                                hx-target="#wordCloudContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="wordCloudPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Word Cloud" to see the preview
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
            <div id="wordCloudContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                 <object data="images/wordcloud.svg" type="image/svg+xml" height="100%" width="100%">
                    <img src="images/wordcloud.svg" alt="Scorecard" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    @PutMapping("/render")
    @ResponseBody
    fun renderWordCloud(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            val content = httpServletRequest.getParameter("content")
            val wordCloudImproved = WordCloudImproved()
            val svg = wordCloudImproved.makeWordCloudSvg(content)

            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
                <div id='imageblock'>
                $svg
                </div>
                <script>
                var wordCloudSource = `[docops,wordcloud]\n----\n${content}\n----`;
                </script>
            """.trimIndent()
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info{"word cloud render executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }

    @PostMapping("")
    fun editFormSubmission(@RequestParam("payload") payload: String) : ResponseEntity<ByteArray> {
        val wordCloudImproved = WordCloudImproved()
        val svg = wordCloudImproved.makeWordCloudSvg(payload)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("text/html")
        return ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)
    }
}