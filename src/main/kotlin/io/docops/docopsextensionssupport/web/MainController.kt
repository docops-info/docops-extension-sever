package io.docops.docopsextensionssupport.web

import io.docops.docopsextensionssupport.aop.LogExecution
import io.github.wimdeblauwe.hsbt.mvc.HtmxResponse
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody


@Controller
@LogExecution
class MainController(private val observationRegistry: ObservationRegistry) {


    @GetMapping("/panelgenerator.html", produces = [MediaType.TEXT_HTML_VALUE])
    fun getPanelGenerator(model: Model): Any {
        return Observation.createNotStarted("docops.panel.generator.html", observationRegistry)
            .observe {
                fun run(): HtmxResponse {
                    return HtmxResponse()
                        .addTemplate("panelgenerator")
                }
            }

    }

    @GetMapping("/panelimagebuilder.html")
    fun getPanelImageBuilder(): Any {
        return Observation.createNotStarted("docops.panel.image.builder.html", observationRegistry).observe {
            HtmxResponse()
                .addTemplate("panelimagebuilder")
        }
    }

    @GetMapping("/badge.html")
    fun getBadge(): Any {
        return Observation.createNotStarted("docops.badge.html", observationRegistry).observe {
            HtmxResponse()
                .addTemplate("badge")
        }
    }
    @GetMapping("/chart.html")
    fun getChart(): Any {
        return Observation.createNotStarted("docops.chart.html", observationRegistry).observe {
            HtmxResponse()
                .addTemplate("chart")
        }
    }

    @GetMapping("/panelseditor.html")
    fun panelsEditor(): Any {
        return Observation.createNotStarted("docops.panel.editor.html", observationRegistry).observe {
            HtmxResponse()
                .addTemplate("panelseditor")
        }
    }

    @GetMapping("/treechart.html")
    fun treeChart(): Any {
        return Observation.createNotStarted("docops.treechart.html", observationRegistry).observe {
            HtmxResponse()
                .addTemplate("treechart")
        }
    }

    @GetMapping("/stacked.html")
    fun stacked(): Any {
        return Observation.createNotStarted("docops.stacked.html", observationRegistry).observe {
            HtmxResponse()
                .addTemplate("stacked")
        }
    }

    @GetMapping("/mychart.html")
    fun mychart(): Any {
        return Observation.createNotStarted("docops.mychart.html", observationRegistry).observe {
            HtmxResponse()
                .addTemplate("mychart")
        }
    }

    @GetMapping("/adrbuilder.html")
    fun getAdr(model: Model): Any {
        return Observation.createNotStarted("docops.panel.image.builder.html", observationRegistry).observe {
            HtmxResponse()
                .addTemplate("adrbuilder")
        }
    }
    @GetMapping("/api/ping")
    @ResponseBody
    fun ping(servletResponse: HttpServletResponse) {
        return Observation.createNotStarted("docops.api.ping", observationRegistry).observe {
            fun run() {
                servletResponse.contentType = "text/html";
                servletResponse.characterEncoding = "UTF-8";
                servletResponse.status = 200
                val writer = servletResponse.writer
                writer.print("OK")
                writer.flush()
            }
        }
    }

}
