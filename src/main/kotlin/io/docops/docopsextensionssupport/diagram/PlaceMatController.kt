package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/placemat")
class PlaceMatController {
    private val log = KotlinLogging.logger {  }

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        //language=json
        val defaultPlacematJson = """
        {
          "title": "System Architecture Overview",
          "placeMats": [
            {"name": "Frontend","legend": "UI"},
            {"name": "Backend","legend": "API"},
            {"name": "Database","legend": "DATA"}
          ],
          "config": {
          "legend": [
            {"legend": "UI","color": "#4361ee"},
            {"legend": "API","color": "#3a0ca3"},
            {"legend": "DATA","color": "#7209b7"}
          ]}
        }
        """.trimIndent()
        //language=html
        val editModeHtml = """
            <div id="placematContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/placemat/" hx-target="#placematPreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit Placemat JSON:</label>
                        <textarea id="content" name="content" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultPlacematJson}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Placemat
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/placemat/view-mode"
                                hx-target="#placematContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="placematPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Placemat" to see the preview
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
        //language=html
        val viewModeHtml = """
            <div id="placematContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/placemat.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/placemat.svg" alt="Placemat" class="max-h-full max-w-full" />
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
    @Counted(value="docops.placemat.put", description="Creating a Placemat using http put")
    @Timed(value = "docops.placemat.put", description="Creating a Placemat using http put", percentiles=[0.5, 0.9])
    fun makeDiag(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            var title = "title"
            if (contents.isNullOrEmpty()) {
                contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
                title = httpServletRequest.getParameter("title")
            }
            val useDarkInput = httpServletRequest.getParameter("useDark")
            val svg = fromRequestToPlaceMat(contents = contents, useDark = "on" == useDarkInput)
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
        """.lines().joinToString(transform = String::trim, separator = "\n")
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }

        return timings.value
    }

    fun fromRequestToPlaceMat(contents: String, useDark: Boolean): String {
        val pms = Json.decodeFromString<PlaceMatRequest>(contents)
        pms.useDark = useDark
        val maker = PlaceMatMaker(placeMatRequest = pms)
        return maker.makePlacerMat().shapeSvg
    }

    @Traceable
    @GetMapping("/")
    @ResponseBody
    @Counted(value = "docops.placemat.get", description="Creating a placemat diagram using http get")
    @Timed(value = "docops.placemat.get", description="Creating a placemat diagram using http get", percentiles=[0.5, 0.9])
    fun getConnector(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "scale", defaultValue = "1.0") scale: String,
        @RequestParam("type", required = false, defaultValue = "SVG") type: String,
        @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
        @RequestParam(name = "outlineColor", defaultValue = "#37cdbe") outlineColor: String
    ): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val svg = fromRequestToPlaceMat(data, useDark = useDark)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)

        }
        log.info{"getConnector executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }
}
