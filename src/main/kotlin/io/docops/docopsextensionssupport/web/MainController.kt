package io.docops.docopsextensionssupport.web

import io.github.wimdeblauwe.hsbt.mvc.HtmxResponse
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody


@Controller
@Observed(name = "main.controller")
class MainController() {


    @GetMapping("/panels.html", produces = [MediaType.TEXT_HTML_VALUE])
    @Timed(value = "docops.panels.html", histogram = true, percentiles = [0.5, 0.95])
    fun getPanelsView(model: Model): String {
        return "panels"
    }

    @GetMapping("/charts.html", produces = [MediaType.TEXT_HTML_VALUE])
    @Timed(value = "docops.charts.html", histogram = true, percentiles = [0.5, 0.95])
    fun getChartssView(model: Model): String {
        return "charts"
    }

    @GetMapping("/panelgenerator.html", produces = [MediaType.TEXT_HTML_VALUE])
    @Timed(value = "docops.panel.generator.html", histogram = true, percentiles = [0.5, 0.95])
    fun getPanelGenerator(model: Model): Any {
        return HtmxResponse()
            .addTemplate("panelgenerator")
    }

    @GetMapping("/panelimagebuilder.html")
    @Timed(value = "docops.panel.image.builder.html", histogram = true, percentiles = [0.5, 0.95])
    fun getPanelImageBuilder() {
        HtmxResponse()
            .addTemplate("panelimagebuilder")
    }

    @GetMapping("/badge.html")
    @Timed(value = "docops.badge.html", histogram = true, percentiles = [0.5, 0.95])
    fun getBadge() {
        HtmxResponse()
            .addTemplate("badge")
    }

    @GetMapping("/chart.html")
    @Timed(value = "docops.chart.html", histogram = true, percentiles = [0.5, 0.95])
    fun getChart() {
        HtmxResponse()
            .addTemplate("chart")
    }

    @GetMapping("/panelseditor.html")
    @Timed(value = "docops.panel.editor.html", histogram = true, percentiles = [0.5, 0.95])
    fun panelsEditor() {
        HtmxResponse()
            .addTemplate("panelseditor")
    }

    @GetMapping("/treechart.html")
    @Timed(value = "docops.treechart.html", histogram = true, percentiles = [0.5, 0.95])
    fun treeChart() {
        HtmxResponse()
            .addTemplate("treechart")
    }

    @GetMapping("/stacked.html")
    @Timed(value = "docops.stacked.html", histogram = true, percentiles = [0.5, 0.95])
    fun stacked() {
        HtmxResponse()
            .addTemplate("stacked")
    }

    @GetMapping("/mychart.html")
    @Timed(value = "docops.mychart.html", histogram = true, percentiles = [0.5, 0.95])
    fun mychart() {
        HtmxResponse()
            .addTemplate("mychart")
    }

    @GetMapping("/adrbuilder.html")
    @Timed(value = "docops.panel.image.builder.html", histogram = true, percentiles = [0.5, 0.95])
    fun getAdr(model: Model) {
        HtmxResponse()
            .addTemplate("adrbuilder")
    }

    @GetMapping("/stats.html")
    @Timed(value = "docops.panel.stats.html", histogram = true, percentiles = [0.5, 0.95])
    fun getStats() {
        HtmxResponse()
            .addTemplate("stats")
    }
    @GetMapping("/simpleicons.html")
    @Timed(value = "docops.simpleicons.html", histogram = true, percentiles = [0.5, 0.95])
    fun getSimpleIcons() {
        HtmxResponse()
            .addTemplate("simpleicons")
    }
    @GetMapping("/twotoneimagebuilder.html")
    @Timed(value = "docops.twotoneimagebuilder.html", histogram = true, percentiles = [0.5, 0.95])
    fun getTwoTone(): String {
        return "twotoneimagebuilder"
    }


    @GetMapping("/api/ping")
    @ResponseBody
    @Timed(value = "docops.api.ping", histogram = true, percentiles = [0.5, 0.95])
    fun ping(servletResponse: HttpServletResponse) {
        servletResponse.contentType = "text/html";
        servletResponse.characterEncoding = "UTF-8";
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print("OK")
        writer.flush()
    }

}



