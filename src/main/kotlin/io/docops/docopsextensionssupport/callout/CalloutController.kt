package io.docops.docopsextensionssupport.callout

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
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/callout")
class CalloutController  {
    private val log = KotlinLogging.logger {  }

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultCalloutContent = """
        title: Software Development Process
        type=systematic
        ---
        Phase | Action | Result | Improvement
        Requirements | Gather user needs and system requirements | Detailed requirements document | Involve end-users earlier in the process
        Design | Create system architecture and UI/UX designs | Technical specifications and wireframes | Use more design thinking workshops
        Development | Implement features according to specifications | Working code with unit tests | Increase pair programming sessions
        Testing | Perform QA and user acceptance testing | Bug reports and validation results | Automate more test cases
        Deployment | Release to production environment | Live application | Implement more robust CI/CD pipeline
        """.trimIndent()

        val editModeHtml = """
            <div id="calloutContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/callout/render" hx-target="#calloutPreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit Callout Content:</label>
                        <textarea id="content" name="content" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultCalloutContent}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Callout
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/callout/view-mode"
                                hx-target="#calloutContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="calloutPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Callout" to see the preview
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
            <div id="calloutContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/callout.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/callout.svg" alt="Scorecard" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    @PutMapping("/render")
    @ResponseBody
    fun renderCallout(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            val content = httpServletRequest.getParameter("content")
            val calloutHandler = CalloutHandler()
            val svg = calloutHandler.makeSvgPlainText(content, 600,400)

            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
                <div id='imageblock'>
                $svg
                </div>
                <script>
                var calloutSource = `[docops,callout]\n----\n${content}\n----`;
                </script>
            """.trimIndent()
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info{"callout render executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }
}