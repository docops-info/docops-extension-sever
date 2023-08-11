package io.docops.docopsextensionssupport.releasestrategy

import io.docops.asciidoctorj.extension.adr.compressString
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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


@Controller
@RequestMapping("/api/release")
@Observed(name = "release.controller")
class ReleaseController @Autowired constructor(val freeMarkerConfigurer: FreeMarkerConfigurer) {

    //support for pdf png file type
    @GetMapping("/", produces = [MediaType.IMAGE_PNG_VALUE, "image/svg+xml"])
    @Timed(value = "docops.release.get.html", histogram = true, percentiles = [0.5, 0.95])
    fun getRelease(@RequestParam(name = "payload") payload: String,
                   @RequestParam("type", required = false, defaultValue = "PDF") type: String,
                   @RequestParam("animate", required = false, defaultValue = "ON") animate: String) : ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload,"UTF-8"))
        val release = Json.decodeFromString<ReleaseStrategy>(data)
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
        return if ("XLS".equals(type,true)) {
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            ResponseEntity(release.excel(output),headers, HttpStatus.OK)
         } else {
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(output.toByteArray(),headers,HttpStatus.OK)
        }
    }


    @GetMapping("/prefill", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Timed(value = "docops.release.get.prefill.html", histogram = true, percentiles = [0.5, 0.95])
    fun prefill(@ModelAttribute model: ModelMap, @RequestParam(name = "payload") payload: String, @RequestParam("type", required = false, defaultValue = "PDF") type: String): String {
        val data = uncompressString(URLDecoder.decode(payload,"UTF-8"))
        val release = Json.decodeFromString<ReleaseStrategy>(data)
        return makeFilledView(model = model, releaseStrategy = release)
    }

    @PutMapping("prefill", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Timed(value = "docops.release.put.json.html", histogram = true, percentiles = [0.5, 0.95])
    fun prefillFromJson(@ModelAttribute model: ModelMap, @RequestParam(name = "payload") payload: String, @RequestParam("type", required = false, defaultValue = "PDF") type: String): String {
        val release = Json.decodeFromString<ReleaseStrategy>(payload)
        return makeFilledView(model = model, releaseStrategy = release)
    }

    @PutMapping("/", produces = ["image/svg+xml"])
    @ResponseBody
    @Timed(value = "docops.release.put.html", histogram = true, percentiles = [0.5, 0.95])
    fun putStrategy(@RequestBody releaseStrategy: ReleaseStrategy): String {
        when (releaseStrategy.style) {
            "TL" -> {
                return createTimelineSvg(releaseStrategy)
            }
            "TLS" -> {
                return createTimelineSummarySvg(releaseStrategy)
            }
            "R" -> {
                return  createRoadMap(releaseStrategy, animate = "ON")
            }
            "TLG" -> {
                return  createTimelineGrouped(releaseStrategy)
            }
        }
        val pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Unknown Release Strategy style ${releaseStrategy.style}")
        throw ErrorResponseException(HttpStatus.BAD_REQUEST,pb, null)
    }


    @PutMapping("/build", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Timed(value = "docops.release.put.build.html", histogram = true, percentiles = [0.5, 0.95])
    fun createStrategy(@ModelAttribute model: ModelMap,
                       @RequestParam("title") title: String,
                       @RequestParam("style") style: String,
                       servletRequest: HttpServletRequest): String {
        getReleaseTypes(servletRequest)
        val releases = getReleaseTypes(servletRequest)

        val releaseStrategy = ReleaseStrategy(title, releases, style)
        return makeFilledView(model=model, releaseStrategy =  releaseStrategy)

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