package io.docops.docopsextensionssupport.web

import io.docops.asciidoc.buttons.theme.*
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody


/**
 * This class represents the main controller for the application.
 *
 * It handles various HTTP GET requests and returns the corresponding HTML views.
 * The controller is responsible for rendering views related to panels, charts, badges, icons, statistics, and more.
 *
 * The class also includes a ping endpoint that returns "OK" when called.
 *
 * @property applicationContext The application context used for dependency injection.
 */
@Controller
@Observed(name = "main.controller")
class MainController @Autowired constructor(private val applicationContext: ApplicationContext) {


    @GetMapping("/panels.html", produces = [MediaType.TEXT_HTML_VALUE])
    @Timed(value = "docops.panels.html", histogram = true, percentiles = [0.5, 0.95])
    fun getPanelsView(model: Model): String {
        return "panels/panels"
    }

    @GetMapping("/panelgenerator.html", produces = [MediaType.TEXT_HTML_VALUE])
    @Timed(value = "docops.panel.generator.html", histogram = true, percentiles = [0.5, 0.95])
    fun getPanelGenerator(model: Model): String {
        return "panels/panelgenerator"
    }

    @GetMapping("/panelimagebuilder.html")
    @Timed(value = "docops.panel.image.builder.html", histogram = true, percentiles = [0.5, 0.95])
    fun getPanelImageBuilder(): String {
        return "panels/panelimagebuilder"
    }

    @GetMapping("/slimpanel.html")
    @Timed(value = "docops.panel.slim.html", histogram = true, percentiles = [0.5, 0.95])
    fun getSlimPanelEditor(): String {
        return "panels/slimpanel"
    }
    @GetMapping("/twotoneimagebuilder.html")
    @Timed(value = "docops.twotoneimagebuilder.html", histogram = true, percentiles = [0.5, 0.95])
    fun getTwoTone(): String {
        return "panels/twotoneimagebuilder"
    }
    @GetMapping("/panelseditor.html")
    @Timed(value = "docops.panel.editor.html", histogram = true, percentiles = [0.5, 0.95])
    fun panelsEditor(): String {
        return "panels/panelseditor"
    }


    @GetMapping("/charts.html", produces = [MediaType.TEXT_HTML_VALUE])
    @Timed(value = "docops.charts.html", histogram = true, percentiles = [0.5, 0.95])
    fun getChartsView(model: Model): String {
        return "chart/charts"
    }

    @GetMapping("/badge.html")
    @Timed(value = "docops.badge.html", histogram = true, percentiles = [0.5, 0.95])
    fun getBadge(): String {
        return "badge/badge"
    }

    @GetMapping("/chart.html")
    @Timed(value = "docops.chart.html", histogram = true, percentiles = [0.5, 0.95])
    fun getChart(model: Model): String {
        return "chart/chart"

    }

    @GetMapping("/mychart.html")
    @Timed(value = "docops.mychart.html", histogram = true, percentiles = [0.5, 0.95])
    fun mychart(model: Model): String {
        return "chart/customchart"
    }



    @GetMapping("/treechart.html")
    @Timed(value = "docops.treechart.html", histogram = true, percentiles = [0.5, 0.95])
    fun treeChart(): String {
        return "chart/treechart"
    }

    @GetMapping("/stacked.html")
    @Timed(value = "docops.stacked.html", histogram = true, percentiles = [0.5, 0.95])
    fun stacked(): String {
        return "chart/stacked"
    }

    @GetMapping("/adrbuilder.html")
    @Timed(value = "docops.panel.image.builder.html", histogram = true, percentiles = [0.5, 0.95])
    fun getAdr(model: Model): String {
        return "adr/adrbuilder"
    }


    @GetMapping("/simpleicons.html")
    @Timed(value = "docops.simpleicons.html", histogram = true, percentiles = [0.5, 0.95])
    fun getSimpleIcons(): String {
        return "icons/simpleicons"
    }


    @GetMapping("/stats.html")
    @Timed(value = "docops.panel.stats.html", histogram = true, percentiles = [0.5, 0.95])
    fun getStats(): String {
        return "stats/stats"
    }

    @GetMapping("/api/ping")
    @ResponseBody
    @Timed(value = "docops.api.ping", histogram = true, percentiles = [0.5, 0.95])
    fun ping(servletResponse: HttpServletResponse) {
        servletResponse.contentType = "text/html"
        servletResponse.characterEncoding = "UTF-8"
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print("OK")
        writer.flush()
    }

    @GetMapping("panels/customslim.html")
    fun customizeView(model: Model, httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse): String {
        val params = httpServletRequest.parameterMap
        val theme = params["theme"]?.get(0)
        return if("----" != theme) {
            model.addAttribute("theme", gradientMap[theme])
            "panels/customslim"
        } else {
            servletResponse.contentType = "text/html"
            servletResponse.characterEncoding = "UTF-8"
            servletResponse.status = 500
            "panels/errors"

        }

    }

    @GetMapping("/strat.html")
    @Timed(value = "docops.release.strategy.html", histogram = true, percentiles = [0.5, 0.95])
    fun stratForm(model: Model): String {
        return "release/strat"
    }

    @GetMapping("/fromJson.html")
    @Timed(value = "docops.release.strategy.from.json.html", histogram = true, percentiles = [0.5, 0.95])
    fun stratFromJson(model: Model): String {
        return "release/fromjson"
    }
    @GetMapping("/builder.html")
    @Timed(value = "docops.release.strategy.builder.html", histogram = true, percentiles = [0.5, 0.95])
    fun stratBuilder(model: Model): String {
        return "release/releasebuilder"
    }
    @GetMapping("/timeline.html")
    @Timed(value = "docops.timeline.strategy.html", histogram = true, percentiles = [0.5, 0.95])
    fun timeline(model: Model): String {
        return "timeline/tm"
    }
    @GetMapping("/roadmap.html")
    @Timed(value = "docops.roadmap.plan.html", histogram = true, percentiles = [0.5, 0.95])
    fun roadmap(model: Model): String {
        return "roadmap/rm"
    }
    @GetMapping("/button/fromJson.html")
    @Timed(value = "docops.button.from.json.html", histogram = true, percentiles = [0.5, 0.95])
    fun buttonFromJson(model: Model, @RequestParam(name = "type", defaultValue = "REGULAR") type: String): String {
        val json = MainController::class.java.classLoader.getResourceAsStream("samples/$type.json")
       json?.let {
            model.addAttribute("json", String(json.readAllBytes()))
       }
        val themeList = applicationContext.getResources("classpath:static/buttondisplay/*.json")
        val themeFiles = mutableListOf<String>()
        themeList.forEach {
            themeFiles.add(it.filename!!)
        }
        model.addAttribute("themes", themeFiles)
        model.addAttribute("themeBox", "")
        model.addAttribute("contentBox", "")
        return "buttons/fromjson"
    }

    @GetMapping("/button/fromJsonToPng.html")
    @Timed(value = "docops.button.from.json.html", histogram = true, percentiles = [0.5, 0.95])
    fun buttonFromJsonToPng(model: Model): String {
        return "buttons/formjsontopng"
    }
    @GetMapping("/scorecard/index.html")
    @Timed(value = "docops.scorecard.index.html", histogram = true, percentiles = [0.5, 0.95])
    fun scorecard(model: Model, @RequestParam(name = "type", defaultValue = "score1") type: String): String {
        val json = MainController::class.java.classLoader.getResourceAsStream("samples/$type.json")
        json?.let {
            model.addAttribute("json", String(json.readAllBytes()))
        }
        return "scorecard/score"
    }

    private val gradientMap = mapOf<String, GradientStyle>(
        "BlueTheme" to BlueTheme,
        "RedTheme" to RedTheme,
        "GreenTheme" to GreenTheme,
        "PurpleTheme" to PurpleTheme,
        "LightPurpleTheme" to LightPurpleTheme,
        "MagentaTheme" to MagentaTheme,
        "DarkTheme" to DarkTheme,
        "DarkTheme2" to DarkTheme2,
        "LightGreyTheme" to LightGreyTheme,
        "OrangeTheme" to OrangeTheme
        )
}



