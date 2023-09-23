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

import io.docops.asciidoc.buttons.dsl.Panels
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.ButtonType
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.ButtonDisplay
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.support.sourceToPanel
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.encodeToString
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.nio.charset.Charset

@Controller
@RequestMapping("/api/panel/convert")
class PanelToController {
    private val scriptLoader = ScriptLoader()
    @PutMapping("/")
    @Timed(value = "docops.PanelToController.put", histogram = true, percentiles = [0.5, 0.95])
    fun convert(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse){
        val contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
        val imgSrc = contentsToButtons(contents, scriptLoader, false)
        servletResponse.contentType = "text/html"
        servletResponse.characterEncoding = "UTF-8"
        servletResponse.status = 200
        val writer = servletResponse.writer
        val contentsButtons = contentsToButtons(contents, scriptLoader, false)
        val sb = StringBuilder()
        contentsButtons.buttons.forEach {
            sb.append(buttonToJson(it) +",")
        }
        val str = buttons(removeDelimiter(sb.toString())!!, contentsButtons.buttonType.toString())
        writer.print(
            """
              $str
                """
        )
        writer.flush()
    }
    private fun contentsToButtons(
        contents: String,
        scriptLoader: ScriptLoader, isPDf: Boolean
    ): Buttons {

        try {
            val panels: Panels = sourceToPanel(contents, scriptLoader)

            val p = makeButtons(panels)
            return Buttons(
                buttons = p.second, buttonType = p.first, useDark = false, theme = ButtonDisplay()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw java.lang.RuntimeException(e)
        }
    }

    fun buttons(str: String, type: String) : String
    {
        return  """
            {
                "buttons": [$str],
                "buttonType": "$type",
                "theme": {
  "colors": [
    "#45618E",
    "#A43B3B",
    "#FFD373",
    "#F7E67A",
    "#01FF90",
    "#FF6F36",
    "#EAA213",
    "#FFAF10",
    "#FF7F00",
    "#6D4F98"
  ],
  "scale": 1.0,
  "columns": 3,
  "buttonStyle": {
    "labelStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 14px; fill: #303030; letter-spacing: normal;font-weight: bold;"
  }
}
             }
        """.trimIndent()
    }
    fun buttonToJson(button: Button): String {
        //language=json
        return """
      {
        "label": "${button.label}",
        "link": "${button.link}",
        "description": "${button.description}",
        "type": "${button.type}",
        "date": "${button.date}",
        "author": [${button.author?.let { toAuthor(it) }}]
      }
        """.trimIndent()
    }
    fun removeDelimiter(s: String?): String? {
        return if (s == null || s.isEmpty()) {
            s
        } else s.replaceFirst(".$".toRegex(), "")
    }
    fun toAuthor(authors: MutableList<String>): String? {
        val sb = StringBuilder()
        authors.forEach {
            sb.append("\"$it\",")
        }
        return removeDelimiter(sb.toString())
    }
    private fun makeButtons(panels: Panels): Pair<io.docops.docopsextensionssupport.button.ButtonType, MutableList<Button>> {
        val btns = mutableListOf<Button>()
        val btnType: io.docops.docopsextensionssupport.button.ButtonType
        when(panels.buttonType) {
            ButtonType.BUTTON  -> {
                btnType = io.docops.docopsextensionssupport.button.ButtonType.REGULAR
            panels.panelButtons.forEach {
                val button = Button(label = it.label, link = it.link, description = it.description, author = it.authors, date = it.date)
                btns.add(button)
            }
        }

            ButtonType.ROUND -> TODO()
            ButtonType.LARGE_CARD -> TODO()
            ButtonType.SLIM_CARD -> {
                btnType = io.docops.docopsextensionssupport.button.ButtonType.SLIM
                panels.slimButtons.forEach {
                    val button = Button(label = it.label, link = it.link, description = it.description, author = it.authors, date = it.date)
                    btns.add(button)
                }
            }
            ButtonType.RECTANGLE -> TODO()
            ButtonType.PILL -> TODO()
        }
        return Pair(btnType,btns)
    }
}