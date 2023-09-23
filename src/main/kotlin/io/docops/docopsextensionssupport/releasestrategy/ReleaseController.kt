package io.docops.docopsextensionssupport.releasestrategy

import io.docops.asciidoctorj.extension.adr.compressString
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
import java.io.StringWriter
import java.net.URLDecoder
import java.util.*
import kotlin.time.measureTimedValue


/**
 * The ReleaseController class handles requests related to releases.
 *
 * @property freeMarkerConfigurer The FreeMarkerConfigurer used for generating HTML templates.
 */
@Controller
@RequestMapping("/api/release")
@Observed(name = "release.controller")
class ReleaseController @Autowired constructor(val freeMarkerConfigurer: FreeMarkerConfigurer) {

    private val log = LogFactory.getLog(ReleaseController::class.java)

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
    @GetMapping("/", produces = [MediaType.IMAGE_PNG_VALUE, "image/svg+xml"])
    @Timed(value = "docops.release.get.html", histogram = true, percentiles = [0.5, 0.95])
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
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                ResponseEntity(release.excel(output), headers, HttpStatus.OK)
            } else {
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType.parseMediaType("image/svg+xml")
                ResponseEntity(output.toByteArray(), headers, HttpStatus.OK)
            }
        }
        log.info("getRelease executed in ${timing.duration.inWholeMilliseconds}ms ")
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
    @GetMapping("/prefill", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Timed(value = "docops.release.get.prefill.html", histogram = true, percentiles = [0.5, 0.95])
    fun prefill(@ModelAttribute model: ModelMap, @RequestParam(name = "payload") payload: String, @RequestParam("type", required = false, defaultValue = "PDF") type: String): String {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val release = Json.decodeFromString<ReleaseStrategy>(data)
            makeFilledView(model = model, releaseStrategy = release)
        }
        log.info("prefill executed in ${timing.duration.inWholeMilliseconds}ms ")
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
    @PutMapping("prefill", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Timed(value = "docops.release.put.json.html", histogram = true, percentiles = [0.5, 0.95])
    fun prefillFromJson(@ModelAttribute model: ModelMap, @RequestParam(name = "payload") payload: String, @RequestParam("type", required = false, defaultValue = "PDF") type: String): String {
        val timing = measureTimedValue {
            val release = Json.decodeFromString<ReleaseStrategy>(payload)
            makeFilledView(model = model, releaseStrategy = release)
        }
        log.info("prefillFromJson executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }

    /**
     * Updates the strategy for releasing a resource.
     *
     * @param releaseStrategy The release strategy to be updated.
     * @return The SVG representation of the updated release strategy.
     */
    @PutMapping("/", produces = ["image/svg+xml"])
    @ResponseBody
    @Timed(value = "docops.release.put.html", histogram = true, percentiles = [0.5, 0.95])
    fun putStrategy(@RequestBody releaseStrategy: ReleaseStrategy): String {
        val timing = measureTimedValue {
            when (releaseStrategy.style) {
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
        log.info("putStrategy executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
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
    @PutMapping("/build", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Timed(value = "docops.release.put.build.html", histogram = true, percentiles = [0.5, 0.95])
    fun createStrategy(@ModelAttribute model: ModelMap,
                       @RequestParam("title") title: String,
                       @RequestParam("style") style: String,
                       servletRequest: HttpServletRequest): String {
        val timing = measureTimedValue {
            getReleaseTypes(servletRequest)
            val releases = getReleaseTypes(servletRequest)

            val releaseStrategy = ReleaseStrategy(title, releases, style)
            makeFilledView(model = model, releaseStrategy = releaseStrategy)
        }
        log.info("createStrategy executed in ${timing.duration.inWholeMilliseconds}ms ")
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
        model["svg"] = svg
        model["bsvg"] = Base64.getEncoder().encodeToString(svg.toByteArray())
        val selectedStrategy = mutableListOf<SelectedStrategy>()
        ReleaseEnum.entries.forEach {
            selectedStrategy.add(SelectedStrategy(it.name, false))
        }
        val format = Json { prettyPrint = true }
        model["releaseTypes"] = ReleaseEnum.entries
        model["sourceJson"] = format.encodeToString(releaseStrategy)
        model["styles"] = releaseStrategy.styles()
        val json = Json.encodeToString(releaseStrategy)
        model["getUrl"] = "api/release/?payload=${compressString(json)}&type=svg"
        model["prefill"] = "api/release/prefill?payload=${compressString(json)}&type=svg"
        val writer = StringWriter()
        val tpl = freeMarkerConfigurer.configuration.getTemplate("release/filled.ftlh")
        tpl.process(model, writer)
        return writer.toString()
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