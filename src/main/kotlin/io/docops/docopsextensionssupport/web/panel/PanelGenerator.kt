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

package io.docops.docopsextensionssupport.web.panel

import io.docops.asciidoc.buttons.dsl.Panels
import io.docops.asciidoc.buttons.service.PanelService
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.ButtonType
import io.docops.docopsextensionssupport.support.*
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import org.w3c.dom.Document
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.GZIPInputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.system.measureTimeMillis

@Controller
@RequestMapping("/api")
@Observed(name = "panel.controller")
class PanelGenerator(private val observationRegistry: ObservationRegistry) {
    private val scriptLoader = ScriptLoader()
    private val log = LoggerFactory.getLogger(PanelGenerator::class.java)

    @GetMapping("/panel")
    @ResponseBody
    @Timed(value = "docops.panel", histogram = true, percentiles = [0.5, 0.95])
    fun getPanel(
        @RequestParam("data") data: String,
        @RequestParam("type") type: String,
        @RequestParam("width", required = false, defaultValue = "") width: String,
        @RequestParam("height", required = false, defaultValue = "") height: String,
        servletResponse: HttpServletResponse
    ): ResponseEntity<ByteArray> {

        val isPDF = "PDF" == type
        val contents = uncompressString(data)
        var imgSrc = contentsToImageStr(contents, scriptLoader, isPDF)
        val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(imgSrc.toByteArray()))
        if (!isPDF && (width.isNotEmpty() || height.isNotEmpty())) {
            imgSrc = manipulateSVG(xml, width, height)
        }

        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        return ResponseEntity(imgSrc.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)

    }


    @GetMapping("/panel/lines")
    @ResponseBody
    @Timed(value = "docops.panel.lines", histogram = true, percentiles = [0.5, 0.95])
    fun toLines(@RequestParam("data") data: String, @RequestParam("server") server: String): ResponseEntity<String> {
        val contents = uncompressString(data)
        val panelService = PanelService()
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.TEXT_PLAIN
        val lines = panelService.toLines("Panel Links", sourceToPanel(contents, scriptLoader), server = server)
        return ResponseEntity(lines.joinToString("\n"), headers, HttpStatus.OK)
    }

    @PutMapping("/colorgen")
    @ResponseBody
    @Timed(value = "docops.panel.generator.color", histogram = true, percentiles = [0.5, 0.95])
    fun putColorGen(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        val timings = measureTimeMillis {
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
                servletResponse.contentType = "text/html"
                servletResponse.characterEncoding = "UTF-8"
                servletResponse.status = 200
                val writer = servletResponse.writer
                writer.print(panel)
                writer.flush()
            } catch (e: Exception) {
                e.printStackTrace()
                servletResponse.contentType = "text/html"
                servletResponse.characterEncoding = "UTF-8"
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
        log.info("putColorGen Total Time : $timings ms")
    }


    @PutMapping("/panelimage")
    @ResponseBody
    @Timed(value = "docops.panel.image", histogram = true, percentiles = [0.5, 0.95])
    fun panelImage(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
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
        servletResponse.contentType = "text/html"
        servletResponse.characterEncoding = "UTF-8"
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print(contents)
        writer.flush()
    }

    @PostMapping("/panel/plain")
    @ResponseBody
    @Timed(value = "docops.panel.plain", histogram = true, percentiles = [0.5, 0.95])
    fun panelsPlain(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {

        val contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
        val imgSrc = contentsToImageStr(contents, scriptLoader, false)
        servletResponse.contentType = "text/html"
        servletResponse.characterEncoding = "UTF-8"
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print(
            """<div>
                $imgSrc
                </div>"""
        )
        writer.flush()
    }

    @GetMapping("/panel/pancolor")
    @ResponseBody
    @Timed(value = "docops.panel.pancolor", histogram = true, percentiles = [0.5, 0.95])
    fun panColor(
        @RequestParam("color") color: String,
        @RequestParam("label") label: String,
        servletResponse: HttpServletResponse
    ) {
        //language=svg
        val imgSrc = """
                <svg xmlns="http://www.w3.org/2000/svg" width="300" height="20">
                    <rect x="0" y="0" width="300" height="20" fill="$color"  rx="5" ry="5"/>
                    <a href="https://www.apple.com">
                        <text x="150" y="15" text-anchor="middle">$label</text>
                    </a>
                 </svg>
            """.trimIndent()
        servletResponse.contentType = "image/svg+xml"
        servletResponse.characterEncoding = "UTF-8"
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print(imgSrc)
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

    fun manipulateSVG(document: Document, width: String?, height: String?): String {
        val elem = document.documentElement
        width?.let {
            elem.setAttribute("width", width)
        }
        height?.let {
            elem.setAttribute("height", height)
        }
        val transformer: Transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.METHOD, "xml")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5")
        }
        val source = DOMSource(document)
        val writer = StringWriter()
        val result = StreamResult(writer)
        transformer.transform(source, result)
        return writer.toString()
    }

}

fun uncompressString(zippedBase64Str: String): String {
    try {
        val decoder = Base64.getUrlDecoder()
        val bytes: ByteArray = decoder.decode(zippedBase64Str)
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
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}