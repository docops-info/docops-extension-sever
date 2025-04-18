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


}
