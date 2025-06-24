package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.web.DocOpsContext
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
@RequestMapping("/api/featurecard")
class FeatureCardController {
    private val log = KotlinLogging.logger {}
    private val featureCardHandler = FeatureCardHandler()

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultFeatureCardContent = """
        @theme: light
        @layout: grid

        Title | Description | Emoji | ColorScheme
        Feature Cards | Interactive card components | 🎴 | PURPLE
        >> Click to expand details
        >> Smooth animations
        >> Responsive design
        Easy Integration | Simple setup process | ⚡ | GREEN
        >> Copy and paste
        >> No configuration needed
        >> Works everywhere
        """.trimIndent()

        val editModeHtml = """
            <div id="featurecardContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/featurecard/render" hx-target="#featurecardPreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit Feature Card Content:</label>
                        <textarea id="content" name="content" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultFeatureCardContent}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Feature Card
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/featurecard/view-mode"
                                hx-target="#featurecardContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="featurecardPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Feature Card" to see the preview
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
            <div id="featurecardContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/featurecard.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/featurecard.svg" alt="Feature Card" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    @PutMapping("/render")
    @ResponseBody
    fun renderFeatureCard(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            val content = httpServletRequest.getParameter("content")
            val svg = featureCardHandler.handleSVG(content, DocOpsContext())

            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
                <div id='imageblock'>
                $svg
                </div>
                <script>
                var featurecardSource = `[docops,featurecard]\n----\n${content}\n----`;
                </script>
            """.trimIndent()
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info{"featurecard render executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }

    @PostMapping("")
    @ResponseBody
    fun handleFeatureCard(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            val payload = httpServletRequest.reader.readText()
            val svg = featureCardHandler.handleSVG(payload, DocOpsContext())
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)
        }
        log.info{"featurecard POST executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }
}
