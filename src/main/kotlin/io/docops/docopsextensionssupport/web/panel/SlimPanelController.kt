package io.docops.docopsextensionssupport.web.panel
import io.docops.asciidoc.buttons.dsl.font
import io.docops.asciidoc.buttons.dsl.panels
import io.docops.asciidoc.buttons.service.PanelService
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.*
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import io.docops.docopsextensionssupport.support.sourceToPanel
import org.springframework.web.bind.annotation.GetMapping
import java.util.UUID


@Controller
@RequestMapping("/api")
@Observed(name = "slimpanel.controller")
class SlimPanelController {
    private val scriptLoader = ScriptLoader()
    @PutMapping("/slimpanel")
    @ResponseBody
    fun slimPanel(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        val params = httpServletRequest.parameterMap
        val themeInput = params["theme"]?.get(0)!!

        mageImage(themeInput, servletResponse)
    }
    @PutMapping("/slimpanelcustom")
    @ResponseBody
    fun slimPanelCustom(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        val params = httpServletRequest.parameterMap
        val gid = System.currentTimeMillis()
        val gradientStyle = """GradientStyle(gradientId= "grad_$gid", color1 = "${params["color1"]?.get(0)!!}", color2 = "${params["color2"]?.get(0)!!}", color3 = "${params["color2"]?.get(0)!!}", fontColor = "${params["fontColor"]?.get(0)!!}", titleColor = "${params["titleColor"]?.get(0)!!}", panelStroke = PanelStroke(color = "${params["strokeColor"]?.get(0)!!}", width = ${params["strokeWidth"]?.get(0)!!}))"""
        mageImage(gradientStyle, servletResponse)
    }

    private fun mageImage(gradientStyle: String, servletResponse: HttpServletResponse) {
        val panelService = PanelService()
        val panelSource = getPanelSource(gradientStyle)

        val panels = sourceToPanel(panelSource, scriptLoader)
        val svg = panelService.fromPanelToSvg(panels)

        servletResponse.contentType = "text/html"
        servletResponse.characterEncoding = "UTF-8"
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print(makeResults(svg, panelSource))
        writer.flush()
    }

    companion object Template{

        fun makeResults(svg: String, panelSource: String): String {
           return   """
            <div id='imageblock'>
            $svg      
            </div>
            <br/>
            <h3>Panel Source</h3>
            <div class='pure-u-1'>
            <pre>
            <code>
            ${panelSource}
            </code>
            </pre>
            </div>
            <script>
            var txt = `${panelSource}`;
            var panelSource = `[panels]\n----\n${panelSource}\n----`;
            document.querySelectorAll('pre code').forEach((el) => {
                hljs.highlightElement(el);
            });
            </script>
        """.trimIndent()
        }
        fun getPanelSource(themeInput: String): String {
            return """panels {
            theme {
                layout {
                    columns = 3
                    groupBy = Grouping.AUTHOR
                    groupOrder = GroupingOrder.ASCENDING
                }
                font = font {
                    color = "#000000"
                    bold = false
                    italic = false
                }
                dropShadow = 3
                legendOn = false
                gradientStyle = ${themeInput};
            }
            slim {
                link = "https://www.apple.com"
                label = "Apple"
                type = "Personal Devices"
                description =
                    "Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. "
                author("Steve Jobs")
            }
            slim {
                link = "https://www.apple.com"
                label = "Apple"
                type = "Personal Devices"
                description =
                    "Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. "
                author("Steve Wozniak")
            }
            slim {
                link = "https://www.apple.com"
                label = "Apple"
                type = "Personal Devices"
                description =
                    "Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. "
                author("Steve Wozniak")
            }
            slim {
                link = "https://www.apple.com"
                label = "Apple"
                type = "Personal Devices"
                description =
                    "Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. "
                author("Steve Wozniak")
            }
            slim {
                link = "https://www.apple.com"
                label = "Apple"
                type = "Personal Devices"
                description =
                    "Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. "
                author("Steve Wozniak")
            }
            slim {
                link = "https://www.apple.com"
                label = "Apple"
                type = "Personal Devices"
                description =
                    "Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. "
                author("Steve Wozniak")
            }
        }"""
        }
    }
}

