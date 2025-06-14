/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.releasestrategy

import io.docops.docopsextensionssupport.diagram.PlaceMatMaker
import io.docops.docopsextensionssupport.diagram.PlaceMatRequest
import io.docops.docopsextensionssupport.svgsupport.compressString
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
import java.net.URLDecoder
import kotlin.time.measureTimedValue


/**
 * The ReleaseController class handles requests related to releases.
 *
 * @property freeMarkerConfigurer The FreeMarkerConfigurer used for generating HTML templates.
 */
@Controller
@RequestMapping("/api/release")
class ReleaseController @Autowired constructor(val freeMarkerConfigurer: FreeMarkerConfigurer) {

    private val log = KotlinLogging.logger {  }

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(@RequestParam(required = false, defaultValue = "TLS") style: String): ResponseEntity<String> {
        //language=json
        val defaultReleaseJson = """
        {
  "title": "Product Release Strategy",
  "style": "$style", "scale": 0.5,
  "releases": [
    {
      "type": "M1",
      "date": "2023-01-15",
      "goal": "Initial Planning",
      "lines": [
        "Define release scope and objectives",
        "Identify key features and enhancements",
        "Create detailed project timeline",
        "Allocate resources and responsibilities"
      ]
    },
    {
      "type": "M2",
      "date": "2023-02-15",
      "goal": "Development Phase",
      "lines": [
        "Code development and unit testing",
        "Integration of components",
        "Documentation updates",
        "Internal code reviews"
      ]
    },
    {
      "type": "RC1",
      "date": "2023-03-15",
      "goal": "Testing Phase",
      "lines": [
        "Functional testing",
        "Performance testing",
        "Security testing",
        "User acceptance testing"
      ]
    },
    {
      "type": "GA",
      "date": "2023-04-15",
      "goal": "Deployment Phase",
      "lines": [
        "Final approval and sign-off",
        "Production deployment",
        "Post-deployment verification",
        "Monitoring and support"
      ]
    }
  ]
}
        """.trimIndent()

        val editModeHtml = """
            <div id="releaseContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/release/strategy" hx-target="#releasePreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit Release Strategy JSON:</label>
                        <textarea id="content" name="content" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultReleaseJson}</textarea>
                    </div>
                    <div class="mb-4">
                        <label class="block text-sm font-medium text-gray-700 mb-1">Style:</label>
                        <div class="flex space-x-4">
                            <label class="inline-flex items-center">
                                <input type="radio" name="styleToggle" value="TLS" class="form-radio h-4 w-4 text-blue-600" ${if (style == "TLS") "checked" else ""} 
                                       hx-get="api/release/edit-mode?style=TLS" 
                                       hx-target="#releaseContainer" 
                                       hx-swap="outerHTML">
                                <span class="ml-2 text-sm text-gray-700">TLS</span>
                            </label>
                            <label class="inline-flex items-center">
                                <input type="radio" name="styleToggle" value="R" class="form-radio h-4 w-4 text-blue-600" ${if (style == "R") "checked" else ""} 
                                       hx-get="api/release/edit-mode?style=R" 
                                       hx-target="#releaseContainer" 
                                       hx-swap="outerHTML">
                                <span class="ml-2 text-sm text-gray-700">R</span>
                            </label>
                        </div>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Strategy
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/release/view-mode"
                                hx-target="#releaseContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="releasePreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Strategy" to see the preview
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
            <div id="releaseContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/release.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/release.svg" alt="Release Strategy" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    @PutMapping("/strategy")
    @ResponseBody
    fun createReleaseStrategy(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val contents = httpServletRequest.getParameter("content")
            var release = Json.decodeFromString<ReleaseStrategy>(contents)
            var output = ""
            val isPdf = false
            when (release.style) {
                "TL" -> {
                    output = createTimelineSvg(release, isPdf)
                }

                "TLS" -> {
                    output = createTimelineSummarySvg(release, isPdf)
                }

                "R" -> {
                    output = createRoadMap(release, isPdf, "OFF")
                }

                "TLG" -> {
                    output = createTimelineGrouped(release, isPdf)
                }
            }
            val svg = output

            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val compressedPayload = compressString(contents)
            val imageUrl = "https://roach.gy/extension/api/docops/svg?kind=release&payload=${compressedPayload}&type=SVG&useDark=false&title=Title&numChars=24&backend=html5&filename=release.svg"

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
                var adrSource = `[docops,release]\n----\n${contents}\n----`;

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

        log.info{"createReleaseStrategy executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }

    /**
     * Retrieves the release in the specified format.
     *
     * @param payload the payload string
     * @param type the type of release (PDF, XLS)
     * @param animate whether to animate the release (ON, OFF)
     * @param useDark whether to use dark mode (true, false)
     * @return a ResponseEntity containing the release in the requested format
     */
//support for pdf png file type
    @Traceable
    @GetMapping("/", produces = [MediaType.IMAGE_PNG_VALUE, "image/svg+xml"])
    @Counted(value="docops.release.get", description="Creating a release diagram using http get")
    @Timed(value = "docops.release.get", description="Creating a release diagram using http get", percentiles=[0.5, 0.9])
    fun getRelease(@RequestParam(name = "payload") payload: String,
                   @RequestParam("type", required = false, defaultValue = "PDF") type: String,
                   @RequestParam("animate", required = false, defaultValue = "ON") animate: String,
                   @RequestParam(name="useDark", defaultValue = "false") useDark: Boolean) : ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val release = Json.decodeFromString<ReleaseStrategy>(data)
            release.useDark = useDark
            val isPdf = "PDF" == type
            var output = ""
            when (release.style) {
                "TL" -> {
                    output = createTimelineSvg(release, isPdf)
                }

                "TLS" -> {
                    output = createTimelineSummarySvg(release, isPdf)
                }

                "R" -> {
                    output = createRoadMap(release, isPdf, animate)
                }

                "TLG" -> {
                    output = createTimelineGrouped(release, isPdf)
                }
            }
            if ("XLS".equals(type, true)) {
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType =
                    MediaType.parseMediaType("text/plain")
                ResponseEntity(release.asciidocTable().toByteArray(), headers, HttpStatus.OK)
            } else {
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType.parseMediaType("image/svg+xml")
                ResponseEntity(output.toByteArray(), headers, HttpStatus.OK)
            }
        }
        log.info{"getRelease executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }


    /**
     * Retrieves prefill data and generates a filled view based on the release strategy.
     *
     * @param model the ModelMap object containing the prefill data for the view
     * @param payload the payload string containing the prefill data
     * @param type the type of the filled view (optional, default value is "PDF")
     * @return the generated filled view as a string
     */
    @Traceable
    @GetMapping("/prefill", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Counted(value="docops.release.get.prefill", description="Creating a release diagram from prefilled data using http get")
    @Timed(value = "docops.release.get.prefill", description="Creating a release diagram from prefilled data using http get", percentiles=[0.5, 0.9])
    fun prefill(@ModelAttribute model: ModelMap, @RequestParam(name = "payload") payload: String, @RequestParam("type", required = false, defaultValue = "PDF") type: String): String {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val release = Json.decodeFromString<ReleaseStrategy>(data)
            makeFilledView(model = model, releaseStrategy = release)
        }
        log.info{"prefill executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }

    /**
     * Fills the given ModelMap with data from a JSON payload and returns the result as a String.
     *
     * @param model The ModelMap to fill with data.
     * @param payload The JSON payload containing the data to fill the ModelMap with.
     * @param type The type of the payload. Default value is "PDF".
     * @return The result of filling the ModelMap with the data from the JSON payload as a String.
     */
    @Traceable
    @PutMapping("prefill", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Counted(value = "docops.release.put.json", description="Creating a release diagram from prefilled data using http put")
    @Timed(value = "docops.release.put.json", description="Creating a release diagram from prefilled data using http put", percentiles=[0.5, 0.9])
    fun prefillFromJson(@ModelAttribute model: ModelMap, @RequestParam(name = "payload") payload: String, @RequestParam("type", required = false, defaultValue = "PDF") type: String): String {
        val timing = measureTimedValue {
            val release = Json.decodeFromString<ReleaseStrategy>(payload)
            makeFilledView(model = model, releaseStrategy = release)
        }
        log.info{"prefillFromJson executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }

    /**
     * Updates the strategy for releasing a resource.
     *
     * @param releaseStrategy The release strategy to be updated.
     * @return The SVG representation of the updated release strategy.
     */
    @Traceable
    @PutMapping("/", produces = ["image/svg+xml"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Counted("docops.release.put", description="Creating a release diagram using http put")
    @Timed(value = "docops.release.put", description="Creating a release diagram using http put", percentiles=[0.5, 0.9])
    fun putStrategy(@RequestBody(required = false) jsonReleaseStrategy: ReleaseStrategy?,
                    @ModelAttribute model: ModelMap,
                    servletRequest: HttpServletRequest): String {
        val timing = measureTimedValue {
            val releaseStrategy = jsonReleaseStrategy ?: throw ErrorResponseException(
                HttpStatus.BAD_REQUEST,
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "JSON body is required"),
                null
            )

            processReleaseStrategy(releaseStrategy)
        }
        log.info{"putStrategy executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }

    /**
     * Updates the strategy for releasing a resource using form-urlencoded data.
     *
     * @return The SVG representation of the updated release strategy.
     */
    @Traceable
    @PutMapping("/", produces = ["image/svg+xml"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @ResponseBody
    @Counted("docops.release.put.form", description="Creating a release diagram using http put with form data")
    @Timed(value = "docops.release.put.form", description="Creating a release diagram using http put with form data", percentiles=[0.5, 0.9])
    fun putStrategyForm(@ModelAttribute model: ModelMap, servletRequest: HttpServletRequest): String {
        val timing = measureTimedValue {
            // Construct from form parameters
            val title = servletRequest.getParameter("title") ?: ""
            val style = servletRequest.getParameter("style") ?: "TL"
            val scale = servletRequest.getParameter("scale")?.toFloatOrNull() ?: 1.0f

            // Get releases from form parameters
            val releases = getReleaseTypesFromForm(servletRequest)

            // Create display config
            val fontColor = servletRequest.getParameter("displayConfig.fontColor") ?: "#fcfcfc"
            val milestoneColor = servletRequest.getParameter("displayConfig.milestoneColor") ?: "#fcfcfc"
            val notesVisible = servletRequest.getParameter("displayConfig.notesVisible") != null

            // Get colors arrays
            val colors = listOf(
                servletRequest.getParameter("displayConfig.colors[0]") ?: "#5f57ff",
                servletRequest.getParameter("displayConfig.colors[1]") ?: "#2563eb",
                servletRequest.getParameter("displayConfig.colors[2]") ?: "#7149c6"
            )

            val circleColors = listOf(
                servletRequest.getParameter("displayConfig.circleColors[0]") ?: "#fc86be",
                servletRequest.getParameter("displayConfig.circleColors[1]") ?: "#dc93f6",
                servletRequest.getParameter("displayConfig.circleColors[2]") ?: "#aeb1ed"
            )

            val carColors = listOf(
                servletRequest.getParameter("displayConfig.carColors[0]") ?: "#fcfcfc",
                servletRequest.getParameter("displayConfig.carColors[1]") ?: "#000000",
                servletRequest.getParameter("displayConfig.carColors[2]") ?: "#ff0000"
            )

            val displayConfig = DisplayConfig(
                fontColor = fontColor,
                milestoneColor = milestoneColor,
                colors = colors,
                circleColors = circleColors,
                carColors = carColors,
                notesVisible = notesVisible
            )

            val releaseStrategy = ReleaseStrategy(
                title = title,
                releases = releases,
                style = style,
                scale = scale,
                displayConfig = displayConfig
            )

            processReleaseStrategy(releaseStrategy)
        }
        log.info{"putStrategyForm executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }

    /**
     * Process a release strategy and generate the appropriate SVG based on the style.
     */
    private fun processReleaseStrategy(releaseStrategy: ReleaseStrategy): String {
        return when (releaseStrategy.style) {
            "TL" -> {
                createTimelineSvg(releaseStrategy)
            }

            "TLS" -> {
                createTimelineSummarySvg(releaseStrategy)
            }

            "R" -> {
                 createRoadMap(releaseStrategy, animate = "ON")
            }

            "TLG" -> {
                 createTimelineGrouped(releaseStrategy)
            }

            else -> {
                val pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Unknown Release Strategy style ${releaseStrategy.style}")
                throw ErrorResponseException(HttpStatus.BAD_REQUEST,pb, null)
            }
        }
    }

    private fun getReleaseTypesFromForm(servletRequest: HttpServletRequest): MutableList<Release> {
        val releases = mutableListOf<Release>()
        val paramNames = servletRequest.parameterNames.toList()

        // Find all release type parameters to determine how many releases we have
        val typeParams = paramNames.filter { it.matches(Regex("releases\\[\\d+\\]\\.type")) }

        typeParams.forEach { typeParam ->
            // Extract the index from the parameter name
            val indexMatch = Regex("releases\\[(\\d+)\\]\\.type").find(typeParam)
            val index = indexMatch?.groupValues?.get(1) ?: return@forEach

            // Get the release type
            val typeValue = servletRequest.getParameter(typeParam) ?: return@forEach
            val type = try {
                ReleaseEnum.valueOf(typeValue)
            } catch (e: IllegalArgumentException) {
                return@forEach
            }

            // Get date and goal
            val date = servletRequest.getParameter("releases[$index].date") ?: "TBD"
            val goal = servletRequest.getParameter("releases[$index].goal") ?: ""

            // Get selected and completed status
            val selected = servletRequest.getParameter("releases[$index].selected") != null
            val completed = servletRequest.getParameter("releases[$index].completed") != null

            // Get lines
            val lines = mutableListOf<String>()
            var lineIndex = 0
            while (true) {
                val lineParam = "releases[$index].lines[$lineIndex]"
                val line = servletRequest.getParameter(lineParam) ?: break
                lines.add(line)
                lineIndex++
            }

            // Add the release to our list
            releases.add(Release(type, lines, date, selected, goal, completed))
        }

        return releases
    }


    /**
     * Creates a strategy based on the provided title, style, and servlet request.
     *
     * @param model The model map used for rendering the view.
     * @param title The title of the strategy.
     * @param style The style of the strategy.
     * @param servletRequest The current HTTP servlet request.
     * @return The rendered view as a string.
     */
    @Traceable
    @PutMapping("/build", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Counted("docops.release.put.build", description="Creating a release strategy builder diagram using http put")
    @Timed(value = "docops.release.put.build", description="Creating a release strategy buildeer diagram using http put", percentiles=[0.5, 0.9])
    fun createStrategy(@ModelAttribute model: ModelMap,
                       @RequestParam("title") title: String,
                       @RequestParam("style") style: String,
                       servletRequest: HttpServletRequest): String {
        val timing = measureTimedValue {
            getReleaseTypes(servletRequest)
            val releases = getReleaseTypes(servletRequest)

            val releaseStrategy = ReleaseStrategy(title=title, releases= releases, style= style)
            makeFilledView(model = model, releaseStrategy = releaseStrategy)
        }
        log.info{"createStrategy executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }

    private fun makeFilledView(model: ModelMap, releaseStrategy: ReleaseStrategy): String {
        model["releaseStrategy"] = releaseStrategy

        var svg = ""
        when (releaseStrategy.style) {
            "TL" -> {
                svg = createTimelineSvg(releaseStrategy)
            }
            "TLS" -> {
                svg = createTimelineSummarySvg(releaseStrategy)
            }
            "R" -> {
                svg = createRoadMap(releaseStrategy, animate = "ON")
            }
            "TLG" -> {
                svg = createTimelineGrouped(releaseStrategy = releaseStrategy)
            }
        }
        val div = """
                <div id='imageblock'>
                $svg
                </div>

        """.lines().joinToString(transform = String::trim, separator = "\n")

        return div
    }

    fun createTimelineSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String = ReleaseTimelineMaker().make(releaseStrategy, isPdf)

    private fun createTimelineSummarySvg(release: ReleaseStrategy, pdf: Boolean = false) : String =ReleaseTimelineSummaryMaker().make(release, isPdf = pdf)

    fun createTimelineGrouped(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String = ReleaseTimelineGroupedMaker().make(releaseStrategy, isPdf)

    fun createRoadMap(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false, animate: String = "ON"): String = ReleaseRoadMapMaker().make(releaseStrategy, isPdf, animate)
    private fun getReleaseTypes(servletRequest: HttpServletRequest): MutableList<Release> {
        val addLine = servletRequest.getParameter("addLine")
        val addType = servletRequest.getParameter("addType")
        val releases = mutableListOf<Release>()
        val types = servletRequest.parameterNames.toList().filter { it.startsWith("type_") }
        types.forEach {
            val type = ReleaseEnum.valueOf(servletRequest.getParameter(it))
            val splitType = it.split("type_")
            val dateEntry = servletRequest.getParameter("date_${splitType[1]}")
            val goal = servletRequest.getParameter("goal_${splitType[1]}")
            val lineArr = servletRequest.getParameterValues("line_${splitType[1]}").toMutableList()
            if (addLine != null && addLine.isNotEmpty() && addLine == "line_${splitType[1]}") {
                lineArr.add("")
            }
            releases.add(Release(type, lineArr, date = dateEntry, true, goal))
        }
        if (addType != null && addType == "increase") {
            releases.add(Release(ReleaseEnum.M1, mutableListOf(""), "TBD", goal = "Our goal is ..."))
        }
        return releases
    }

    @PostMapping("")
    fun editFormSubmit(@RequestParam("payload") payload: String): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val release = Json.decodeFromString<ReleaseStrategy>(data)
            val contents = processReleaseStrategy(release)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.TEXT_PLAIN
            ResponseEntity(contents.toByteArray(), headers, HttpStatus.OK)
        }
        return timing.value
    }
}
