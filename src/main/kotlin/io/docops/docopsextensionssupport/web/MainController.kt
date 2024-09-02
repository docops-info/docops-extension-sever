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

package io.docops.docopsextensionssupport.web

import io.docops.asciidoc.buttons.theme.*
import io.micrometer.core.annotation.Counted
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
    @Counted
    @Timed(value = "docops.panels.html")
    fun getPanelsView(model: Model): String {
        return "panels/panels"
    }

    @GetMapping("/panelgenerator.html", produces = [MediaType.TEXT_HTML_VALUE])
    @Counted
    @Timed(value = "docops.panel.generator.html")
    fun getPanelGenerator(model: Model): String {
        return "panels/panelgenerator"
    }

    @GetMapping("/panelimagebuilder.html")
    @Counted
    @Timed(value = "docops.panel.image.builder.html")
    fun getPanelImageBuilder(): String {
        return "panels/panelimagebuilder"
    }

    @GetMapping("/slimpanel.html")
    @Counted
    @Timed(value = "docops.panel.slim.html")
    fun getSlimPanelEditor(): String {
        return "panels/slimpanel"
    }
    @GetMapping("/twotoneimagebuilder.html")
    @Counted
    @Timed(value = "docops.twotoneimagebuilder.html")
    fun getTwoTone(): String {
        return "panels/twotoneimagebuilder"
    }
    @GetMapping("/panelseditor.html")
    @Counted
    @Timed(value = "docops.panel.editor.html")
    fun panelsEditor(): String {
        return "panels/panelseditor"
    }


    @GetMapping("/charts.html", produces = [MediaType.TEXT_HTML_VALUE])
    @Counted
    @Timed(value = "docops.charts.html")
    fun getChartsView(model: Model): String {
        return "chart/charts"
    }

    @GetMapping("/badge.html")
    @Counted
    @Timed(value = "docops.badge.html")
    fun getBadge(): String {
        return "badge/badge"
    }

    @GetMapping("/chart.html")
    @Counted
    @Timed(value = "docops.chart.html")
    fun getChart(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "bar"}}""")
        return "chart/chart"

    }

    @GetMapping("/mychart.html")
    @Counted
    @Timed(value = "docops.mychart.html")
    fun mychart(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "mychart"}}""")
        return "chart/customchart"
    }



    @GetMapping("/treechart.html")
    @Counted
    @Timed(value = "docops.treechart.html")
    fun treeChart(response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "tree"}}""")
        return "chart/treechart"
    }

    @GetMapping("/stacked.html")
    @Counted
    @Timed(value = "docops.stacked.html")
    fun stacked(response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "stacked"}}""")
        return "chart/stacked"
    }

    @GetMapping("/adrbuilder.html")
    @Counted
    @Timed(value = "docops.panel.image.builder.html")
    fun getAdr(model: Model): String {
        return "adr/adrbuilder"
    }


    @GetMapping("/simpleicons.html")
    @Counted
    @Timed(value = "docops.simpleicons.html")
    fun getSimpleIcons(): String {
        return "icons/simpleicons"
    }


    @GetMapping("/stats.html")
    @Counted
    @Timed(value = "docops.panel.stats.html")
    fun getStats(): String {
        return "stats/stats"
    }

    @GetMapping("/api/ping")
    @ResponseBody
    @Counted
    @Timed(value = "docops.api.ping")
    fun ping(servletResponse: HttpServletResponse) {
        servletResponse.contentType = "text/html"
        servletResponse.characterEncoding = "UTF-8"
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print("OK")
        writer.flush()
    }

    @GetMapping("panels/customslim.html")
    @Counted
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
    @Counted
    @Timed(value = "docops.release.strategy.html")
    fun stratForm(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "strat"}}""")
        return "release/strat"
    }

    @GetMapping("/fromJson.html")
    @Counted
    @Timed(value = "docops.release.strategy.from.json.html")
    fun stratFromJson(model: Model,  response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "fromJson"}}""")
        return "release/fromjson"
    }
    @GetMapping("/builder.html")
    @Counted
    @Timed(value = "docops.release.strategy.builder.html")
    fun stratBuilder(model: Model): String {
        return "release/releasebuilder"
    }
    @GetMapping("/timeline.html")
    @Counted
    @Timed(value = "docops.timeline.strategy.html")
    fun timeline(model: Model): String {
        return "timeline/tm"
    }

    @GetMapping("/boxy.html")
    @Counted
    @Timed(value = "docops.boxy.html")
    fun boxy(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "boxy"}}""")
        return "boxy/boxy"
    }

    @GetMapping("/pm.html")
    @Counted
    @Timed(value = "docops.pm.html")
    fun pm(model: Model,  response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "pm"}}""")
        return "boxy/pm"
    }

    @GetMapping("/roadmap.html")
    @Counted
    @Timed(value = "docops.roadmap.plan.html")
    fun roadmap(model: Model): String {
        return "roadmap/rm"
    }
    @GetMapping("/button/fromJson.html")
    @Counted
    @Timed(value = "docops.button.from.json.html")
    fun buttonFromJson(model: Model, @RequestParam(name = "type", defaultValue = "REGULAR") type: String,  response: HttpServletResponse): String {
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
        response.addHeader("HX-Trigger", """{"button-click": {"element": "$type"}}""")
        return "buttons/fromjson"
    }

    @GetMapping("/button/fromJsonToPng.html")
    @Counted
    @Timed(value = "docops.button.from.json.html")
    fun buttonFromJsonToPng(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "PNG"}}""")
        return "buttons/formjsontopng"
    }
    @GetMapping("/scorecard/index.html")
    @Counted
    @Timed(value = "docops.scorecard.index.html")
    fun scorecard(model: Model, @RequestParam(name = "type", defaultValue = "score1", ) type: String, response: HttpServletResponse): String {
        val json = MainController::class.java.classLoader.getResourceAsStream("samples/$type.json")
        json?.let {
            model.addAttribute("json", String(json.readAllBytes()))
        }
        response.addHeader("HX-Trigger", """{"button-click": {"element": "$type"}}""")
        return "scorecard/score"
    }

    @GetMapping("/color/grad.html")
    @Counted
    @Timed(value = "docops.button.from.color.grad.html")
    fun grad(model: Model): String {
        return "color/gradhelp"
    }

    @GetMapping("/button/convert.html")
    @Counted
    @Timed(value = "docops.button.convert.html")
    fun convertPanels(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "panel"}}""")
        return "buttons/convert"
    }
    @GetMapping("/search/index.html")
    @Counted
    @Timed(value = "docops.search.html")
    fun convertPanels(): String {
        return "search/search"
    }

    @GetMapping("/buttons.html")
    @Counted
    @Timed(value = "docops.buttons.html")
    fun btns(model: Model): String {
        return "buttons/buttons"
    }
    @GetMapping("/draw.html")
    @Counted
    @Timed(value = "docops.draw")
    fun draw(model: Model): String {
        return "boxy/draw"
    }
    @GetMapping("/pie.html")
    @Counted
    @Timed(value = "pie.draw")
    fun pie(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "pie"}}""")
        return "boxy/pie"
    }
    @GetMapping("/pieslice.html")
    @Counted
    @Timed(value = "pieslice.draw")
    fun pieslice(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "pieslice"}}""")
        return "boxy/pieslice"
    }

    @GetMapping("/bar.html")
    @Counted
    @Timed(value = "bar.draw")
    fun bar(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "bar"}}""")
        return "boxy/bar"
    }

    @GetMapping("/line.html")
    @Counted
    @Timed(value = "line.draw")
    fun line(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "line"}}""")
        return "boxy/line"
    }

    @GetMapping("/cal.html")
    @Counted
    @Timed(value = "cal.draw")
    fun cal(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "cal"}}""")
        return "boxy/cal"
    }
    @GetMapping("/bargroup.html")
    @Counted
    @Timed(value = "cal.draw")
    fun barGroup(model: Model, response: HttpServletResponse): String {
        response.addHeader("HX-Trigger", """{"button-click": {"element": "bargroup"}}""")
        return "boxy/bargroup"
    }

    @GetMapping("/release.html")
    @Counted
    @Timed(value = "docops.release")
    fun release(model: Model): String {
        return "release/release"
    }
    @GetMapping("/scorecard.html")
    @Counted
    @Timed(value = "docops.scorecard")
    fun score(model: Model): String {
        return "scorecard/menu"
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



