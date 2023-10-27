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
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/api/panel/convert")
class PanelToController {
    private val scriptLoader = ScriptLoader()
    @PutMapping("/")
    @Timed(value = "docops.PanelToController.put", histogram = true, percentiles = [0.5, 0.95])
    @ResponseBody
    fun convert(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse){
        val contents = httpServletRequest.getParameter("payload")
        servletResponse.contentType = "text/html"
        servletResponse.characterEncoding = "UTF-8"
        servletResponse.status = 200
        val writer = servletResponse.writer
        val buttonContent=  getPanels(contents, scriptLoader)
        val resp = """
            <div>
            <pre>
                <code class="json">
                   ${panelToButtons(buttonContent)}
                </code>
            </pre>
            </div>
            <script>
            document.querySelectorAll('pre code').forEach((el) => {
                hljs.highlightElement(el);
            });
            </script>
        """.trimIndent()
        writer.println(resp)
        writer.flush()
    }
    private fun getPanels(
        contents: String,
        scriptLoader: ScriptLoader
    ): Panels {
        return sourceToPanel(contents, scriptLoader)
    }
    fun panelToButtons(pans: Panels): String {
        var dark = "#000000"
        val str = StringBuilder("{")
        if(pans.buttonType == ButtonType.BUTTON) {
            val btns = pans.panelButtons
            str.append(""" "buttons": [""")
            btns.forEach {
                    pb ->
                //language=json
                str.append("""
                        {
                            "link": "${pb.link}",
                            "label": "${pb.label}",
                            "description": "${pb.description}",
                            "type": "${pb.type}"
                        },
                    """.trimIndent())

            }
            val idx = str.lastIndexOf(",")
            if(-1 != idx) {
                str.deleteCharAt(idx)
            }
            str.append("],")
            str.append(""" "buttonType": "REGULAR", """)
            dark = "#fcfcfc"
        }
        else if (pans.buttonType == ButtonType.SLIM_CARD) {
            dark = "#fcfcfc"
            val btns = pans.slimButtons
            str.append(""" "buttons": [""")
            btns.forEach {
                    pb ->
                val authors = StringBuilder("[")
                if(pb.authors.isNotEmpty())
                {
                    pb.authors.forEach {
                        authors.append(""" "$it",""")
                    }
                }
                val aidx = authors.lastIndexOf(",")
                if(-1 != aidx) {
                    authors.deleteCharAt(aidx)
                }
                authors.append("]")
                //language=json
                str.append("""
                        {
                            "link": "${pb.link}",
                            "label": "${pb.label}",
                            "description": "${pb.description}",
                            "type": "${pb.type}",
                            "author": $authors
                        },
                    """.trimIndent())

            }
            val idx = str.lastIndexOf(",")
            if(-1 != idx) {
                str.deleteCharAt(idx)
            }
            str.append("],")
            str.append(""" "buttonType": "SLIM", """)
        }
        else if(pans.buttonType == ButtonType.LARGE_CARD) {
            val btns = pans.largeButtons
            str.append(""" "buttons": [""")
            btns.forEach {
                    pb ->
                val authors = StringBuilder("[")
                if(pb.authors.isNotEmpty())
                {
                    pb.authors.forEach {
                        authors.append(""" "$it",""")
                    }
                }
                val aidx = authors.lastIndexOf(",")
                if(-1 != aidx) {
                    authors.deleteCharAt(aidx)
                }
                authors.append("]")
                val line1 = StringBuilder("")
                pb.line1?.let {
                    line1.append("""
                    "cardLine1": {
                        "line": "${it.line}",
                        "size": "${it.size}"
                      },
                """.trimIndent())
                }
                val line2 =   StringBuilder()
                pb.line2?.let {
                    line2.append("""
                    "cardLine2": {
                        "line": "${it.line}",
                        "size": "${it.size}"
                      },
                """.trimIndent())
                }
                //language=json
                str.append("""
                        {
                            "link": "${pb.link}",
                            "label": "${pb.label}",
                            "description": "${pb.description}",
                            "type": "${pb.type}",
                            $line1
                            $line2
                            "author": $authors
                        },
                    """.trimIndent())

            }
            val idx = str.lastIndexOf(",")
            if(-1 != idx) {
                str.deleteCharAt(idx)
            }
            str.append("],")
            str.append(""" "buttonType": "LARGE", """)
        }
        else if(pans.buttonType == ButtonType.RECTANGLE) {
            val btns = pans.rectangleButtons
            str.append(""" "buttons": [""")
            btns.forEach {
                    pb ->
                val links = StringBuilder("[")
                if(pb.links.isNotEmpty())
                {
                    pb.links.forEach {
                        links.append(""" {"label": "${it.label}", "href": "${it.href}"},""")
                    }
                }
                val aidx = links.lastIndexOf(",")
                if(-1 != aidx) {
                    links.deleteCharAt(aidx)
                }
                links.append("]")
                //language=json
                str.append("""
                        {
                            "link": "${pb.link}",
                            "label": "${pb.label}",
                            "description": "${pb.description}",
                            "type": "${pb.type}",
                            "links": $links
                        },
                    """.trimIndent())

            }
            val idx = str.lastIndexOf(",")
            if(-1 != idx) {
                str.deleteCharAt(idx)
            }
            str.append("],")
            str.append(""" "buttonType": "RECTANGLE", """)
        }
        else if(pans.buttonType == ButtonType.PILL) {
            val btns = pans.panelButtons
            str.append(""" "buttons": [""")
            btns.forEach {
                    pb ->
                //language=json
                str.append("""
                        {
                            "link": "${pb.link}",
                            "label": "${pb.label}",
                            "description": "${pb.description}",
                            "type": "${pb.type}"
                        },
                    """.trimIndent())

            }
            val idx = str.lastIndexOf(",")
            if(-1 != idx) {
                str.deleteCharAt(idx)
            }
            str.append("],")
            str.append(""" "buttonType": "PILL", """)
            dark = "#fcfcfc"
        }
        else if(pans.buttonType == ButtonType.ROUND) {
            val btns = pans.roundButtons
            str.append(""" "buttons": [""")
            btns.forEach {
                    pb ->
                //language=json
                str.append("""
                        {
                            "link": "${pb.link}",
                            "label": "${pb.label}",
                            "type": "${pb.type}"
                        },
                    """.trimIndent())

            }
            val idx = str.lastIndexOf(",")
            if(-1 != idx) {
                str.deleteCharAt(idx)
            }
            str.append("],")
            str.append(""" "buttonType": "ROUND", """)
            dark = "#fcfcfc"
        }
        str.append("""
  "theme": {
    "colors": [
      "#FF9898", "#CF455C", "#971549", "#470031", "#F70776", "#C3195D", "#680747", "#872341"
    ],
    "columns": 3,
    "sortBy": {"sort": "LABEL"},
    "buttonStyle": {
      "labelStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: $dark; letter-spacing: normal;font-weight: bold;",
      "dateStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #000000; letter-spacing: normal;font-weight: normal;",
      "descriptionStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 10px; fill: $dark; letter-spacing: normal;font-weight: normal;",
      "typeStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; letter-spacing: normal;font-weight: bold; font-style: italic;",
      "authorStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px;  fill: $dark; letter-spacing: normal;font-weight: normal; font-style: italic;"
    },
    "scale": 1.0
  }
    """.trimIndent())

        str.append("}")
        return str.toString()
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