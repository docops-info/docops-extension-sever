package io.docops.docopsextensionssupport.releasestrategy

import com.fasterxml.jackson.databind.ObjectMapper
import io.docops.docopsextensionssupport.badge.findHeightWidth
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
import java.io.StringWriter
import java.net.URLDecoder
import java.util.*


@Controller
@RequestMapping("/api/release")
class ReleaseController @Autowired constructor(val freeMarkerConfigurer: FreeMarkerConfigurer,
    val objectMapper: ObjectMapper) {

    @GetMapping("/", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getRelease(@RequestParam(name = "payload") payload: String) : ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload,"UTF-8"))
        val release = objectMapper.readValue<ReleaseStrategy>(data, ReleaseStrategy::class.java)
        var output = ""
        when (release.style) {
            "TL" -> {
                output = createTimelineSvg(release, true)
            }
            "R" -> {
                output = createRoadMap(release, true)
            }
            "TLG" -> {
                output = createTimelineGrouped(release, true)
            }
        }
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.IMAGE_PNG
        val res = findHeightWidth(output)
        val baos = SvgToPng().toPngFromSvg(output, res)
        return ResponseEntity(baos, headers, HttpStatus.OK)
    }
    @PutMapping("/", produces = ["image/svg+xml"])
    @ResponseBody
    fun putStrategy(@RequestBody releaseStrategy: ReleaseStrategy): String {
        when (releaseStrategy.style) {
            "TL" -> {
                return createTimelineSvg(releaseStrategy)
            }
            "R" -> {
                return  createRoadMap(releaseStrategy)
            }
            "TLG" -> {
                return  createTimelineGrouped(releaseStrategy)
            }
        }
        return createTimelineSvg(releaseStrategy)
    }


    @PutMapping("/build", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun createStrategy(@ModelAttribute model: ModelMap,
                       @RequestParam("title") title: String,
                       @RequestParam("style") style: String,
                       servletRequest: HttpServletRequest): String {
        getReleaseTypes(servletRequest)
        val releases = getReleaseTypes(servletRequest)

        val releaseStrategy = ReleaseStrategy(title, releases, style)

        model["releaseStrategy"] = releaseStrategy

        var svg = ""
        when (style) {
            "TL" -> {
                svg = createTimelineSvg(releaseStrategy)
            }
            "R" -> {
                svg = createRoadMap(releaseStrategy)
            }
            "TLG" -> {
                svg = createTimelineGrouped(releaseStrategy = releaseStrategy)
            }
        }
        model["svg"] = svg
        model["bsvg"] = Base64.getEncoder().encodeToString(svg.toByteArray())
        val selectedStrategy = mutableListOf<SelectedStrategy>()
        ReleaseEnum.values().forEach {
            selectedStrategy.add(SelectedStrategy(it.name, false))
        }
        model["releaseTypes"] = ReleaseEnum.values()
        model["sourceJson"] = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(releaseStrategy)
        model["styles"] = releaseStrategy.styles()
        val writer = StringWriter()
        val tpl = freeMarkerConfigurer.configuration.getTemplate("release/filled.ftlh")
        tpl.process(model, writer)
        return writer.toString()

    }

    fun createTimelineSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String = ReleaseTimelineMaker().make(releaseStrategy, isPdf)

    fun createTimelineGrouped(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String = ReleaseTimelineGroupedMaker().make(releaseStrategy, isPdf)

    fun createRoadMap(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String = ReleaseRoadMapMaker().make(releaseStrategy, isPdf)
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