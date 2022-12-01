package io.docops.docopsextensionssupport.web.panel

import io.docops.asciidoc.buttons.dsl.Panels
import io.docops.asciidoc.buttons.service.PanelService
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.ButtonType
import io.docops.docopsextensionssupport.support.*
import io.github.wimdeblauwe.hsbt.mvc.HtmxResponse
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.awt.Label
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*
import java.util.zip.GZIPInputStream

@Controller
class PanelGenerator(private val observationRegistry: ObservationRegistry) {
    private val scriptLoader = ScriptLoader()

    @GetMapping("/api/panel")
    fun getPanel(
        @RequestParam("data") data: String,
        @RequestParam("type") type: String,
        servletResponse: HttpServletResponse
    ) {
        return Observation.createNotStarted("docops.panel", observationRegistry).observe {
            val isPDF = "PDF" == type
            val contents = uncompressString(data)
            val imgSrc = contentsToImageStr(contents, scriptLoader, isPDF)
            servletResponse.contentType = "image/svg+xml";
            servletResponse.characterEncoding = "UTF-8";
            servletResponse.status = 200
            val writer = servletResponse.writer
            writer.print(imgSrc)
            writer.flush()
        }
    }

    @PutMapping("/api/colorgen")
    fun putColorGen(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        return Observation.createNotStarted("docops.panel.generator.color", observationRegistry).observe {
            try {
                var bold = false
                var underline = false
                var italics = false
                var newWin = false
                val params = httpServletRequest.parameterMap
                val pts = params["points"]?.get(0) as String
                val btnType = params["buttonType"]?.get(0)!!
                val columns = params["columns"]?.get(0)!!
                val groupBY = params["sortBy"]?.get(0)!!
                val orderBy = params["order"]?.get(0)!!
                val case = params["case"]?.get(0)!!
                val dropShadow = params["dropShadow"]?.get(0)!!
                val buttonKind = ButtonType.valueOf(btnType)
                val color = params["color"]?.get(0)!!
                val weight = params["bold"]
                if (weight != null) {
                    bold = true
                }
                val italic = params["italic"]?.get(0)
                val und = params["underline"]?.get(0)
                val newWinParam = params["newWin"]?.get(0)
                if ("on" == italic) {
                    italics = true
                }
                if ("on" == und) {
                    underline = true
                }
                if ("on" == newWinParam) {
                    newWin = true
                }
                val font = params["font"]?.get(0)!!
                val fpoint = params["fpoint"]?.get(0)!!
                val size = params["size"]?.get(0)!!
                val spacing = params["spacing"]?.get(0)!!
                val cd = ColorDivCreator(
                    num = pts.toInt(), buttonKind = buttonKind,
                    columns = columns, groupBY = groupBY,
                    orderBy = orderBy, dropShadow = dropShadow,
                    color = color, weight = bold, font = font,
                    italics = italics, decoration = underline, size = size + fpoint,
                    case = case, newWin = newWin, spacing = spacing
                )
                val panel = cd.genPanels()
                servletResponse.contentType = "text/html";
                servletResponse.characterEncoding = "UTF-8";
                servletResponse.status = 200
                val writer = servletResponse.writer
                writer.print(panel)
                writer.flush()
            } catch (e: Exception) {
                e.printStackTrace()
                servletResponse.contentType = "text/html";
                servletResponse.characterEncoding = "UTF-8";
                servletResponse.status = 400
                val writer = servletResponse.writer
                //language=html
                writer.print(
                    """
        <input class="checker" type="checkbox" id="o" checked hidden>
        <div class="modal">
          <div class="modal-body">
            <div class="modal-content">Invalid selection(s) for panel!</div>
            <div class="modal-footer">
              <label for="o" class='mlabel'>close</label>
            </div>
          </div>
        </div>
    """
                )
                writer.flush()
            }
        }

    }


    @PutMapping("/api/panelimage")
    fun panelImage(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        return Observation.createNotStarted("docops.panel.image", observationRegistry).observe {
            val params = httpServletRequest.parameterMap
            var fillColor = params["fillColor"]?.get(0)!!
            val imageType = params["imageType"]?.get(0)!!
            val fontColor = params["fontColor"]?.get(0)!!
            val line1 = params["line1"]?.get(0)!!
            val line2 = params["line2"]?.get(0)!!
            val line3 = params["line3"]?.get(0)!!
            val transparentParam = params["transparent"]?.get(0)
            if ("on" == transparentParam) {
                fillColor = "none"
            }
            //language=html
            val contents = when (imageType) {
                "CIRCLE" -> {
                    makePanelRoundMiddleImage(fillColor, fontColor, line1, line2, line3)

                }

                "TAGLINE" -> {
                    makeTagLine(fillColor, fontColor, line1, line2, line3)
                }

                else -> {
                    makeLineImage(fillColor, fontColor, line1, line2, line3)
                }
            }
            servletResponse.contentType = "text/html";
            servletResponse.characterEncoding = "UTF-8";
            servletResponse.status = 200
            val writer = servletResponse.writer
            writer.print(contents)
            writer.flush()
        }
    }

    @PostMapping("/api/panel/plain")
    fun panelsPlain(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {

        return Observation.createNotStarted("docops.panel.plain", observationRegistry).observe {
            val contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
            val imgSrc = contentsToImageStr(contents, scriptLoader, false)
            servletResponse.contentType = "text/html";
            servletResponse.characterEncoding = "UTF-8";
            servletResponse.status = 200
            val writer = servletResponse.writer
            writer.print(
                """<div>
                $imgSrc
                </div>"""
            )
            writer.flush()
        }
    }

    @GetMapping("/api/panel/pancolor")
    fun panColor(@RequestParam("color") color: String, @RequestParam("label") label: String, servletResponse: HttpServletResponse) {
        return Observation.createNotStarted("docops.panel.pancolor", observationRegistry).observe {
            //language=svg
            val imgSrc = """
                <svg xmlns="http://www.w3.org/2000/svg" width="300" height="20">
                    <rect x="0" y="0" width="300" height="20" fill="$color"  rx="5" ry="5"/>
                    <a href="https://www.apple.com">
                        <text x="150" y="15" text-anchor="middle">$label</text>
                    </a>
                 </svg>
            """.trimIndent()
            servletResponse.contentType = "image/svg+xml";
            servletResponse.characterEncoding = "UTF-8";
            servletResponse.status = 200
            val writer = servletResponse.writer
            writer.print(imgSrc)
        }
    }

    private fun contentsToImageStr(
        contents: String,
        scriptLoader: ScriptLoader, isPDf: Boolean
    ): String {

        try {
            val panels: Panels = sourceToPanel(contents, scriptLoader)
            val panelService = PanelService()
            panels.isPdf = isPDf
            return panelService.fromPanelToSvg(panels)
        } catch (e: Exception) {
            e.printStackTrace()
            throw java.lang.RuntimeException(e)
        }
    }



}
fun uncompressString(zippedBase64Str: String): String {
    val bytes: ByteArray = Base64.getUrlDecoder().decode(zippedBase64Str)
    var zi: GZIPInputStream? = null
    zi = GZIPInputStream(ByteArrayInputStream(bytes))
    val reader = InputStreamReader(zi, Charset.defaultCharset())
    val input = BufferedReader(reader)

    val content = StringBuilder()
    try {
        var line = input.readLine()
        while (line != null) {
            content.append(line + "\n")
            line = input.readLine()
        }
    } finally {
        reader.close()
    }
    return content.toString()
}