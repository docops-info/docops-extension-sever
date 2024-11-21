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

package io.docops.docopsextensionssupport.support

import io.docops.asciidoc.buttons.dsl.Panels
import io.docops.asciidoc.buttons.service.PanelService
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.ButtonType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor


class ColorDivCreator(
    val num: Int, val buttonKind: ButtonType, val columns: String,
    val groupBY: String, val orderBy: String,
    val dropShadow: String,
    val color: String,
    val weight: Boolean,
    val italics: Boolean,
    val font: String,
    val decoration: Boolean,
    val size: String,
    val case: String,
    val newWin: Boolean,
    val spacing: String
) {
    private val scriptLoader = ScriptLoader()
    fun genPanels(): String {
        var cols: Int
        columns.let {
            cols = it.toInt()
        }
        val panelStr = genPanelStr(cols)
        val p = sourceToPanel(panelStr.first, scriptLoader)
        val svc = PanelService()
        val svg = svc.fromPanelToSvg(p)

        //language=html
        val results = """
            <div id='imageblock'>
            $svg      
            </div>
            <br/>
            <h3>Panel Source</h3>
            <div class='pure-u-1 pure-u-md-1-2'>
            <pre>
            <code>
            ${panelStr.first}
            </code>
            </pre>
            </div>
            <script>
            var txt = `${panelStr.second}`;
            var panelSource = `[panels]\n----\n${panelStr.first}\n----`;
            document.querySelectorAll('pre code').forEach((el) => {
                hljs.highlightElement(el);
            });
            </script>
            <span id="pointsValue" class='badge-green' data-hx-swap-oob="true">$num</span>
            <span id="colsValue" class='badge-green' data-hx-swap-oob="true">$cols</span>
            <span id="shadowValue" class='badge-green' data-hx-swap-oob="true">$dropShadow</span>
            <span id="fSize" class='badge-green' data-hx-swap-oob="true">$size</span>
        """.trimIndent()
        return results
    }

    private fun getRandomColor(): String {
        val letters = "0123456789ABCDEF"
        var color = "#"
        for (i in 0..5) {
            color += letters[floor(Math.random() * 16).toInt()]
        }
        return color
    }


    private fun getBase16(input: Int) : String {
        println(input)
        if(input < 10) {
            return "0$input"
        }
        return input.toString(16)
    }
    fun genPanelStr(cols: Int): Pair<String, StringBuilder> {
        val str = StringBuilder()
        str.append("panels{\n")

        val panelMap = getColorMapAndPanels()
        str.append(
            """
    theme {
        ${panelMap.second}
        legendOn = false
        layout {
            columns = $cols
            groupBy = $groupBY
            groupOrder = $orderBy
        }
        font = font {
             family = "$font"
             size = "$size"
             color = "$color"
             spacing = "$spacing"
             bold = $weight
             italic = $italics
             underline = $decoration
             vertical = false
             case = $case
        
        }
        newWin = $newWin
        dropShadow = $dropShadow
        }
    """.trimIndent()
        )
        str.append(panelMap.first)
        str.append("\n}")
        return Pair(str.toString(), panelMap.second)

    }

    private fun getColorMapAndPanels(): Pair<StringBuilder, StringBuilder> {
        var type = "panel"
        when (buttonKind) {
            ButtonType.ROUND -> {
                type = "round"
            }
            ButtonType.SLIM_CARD -> {
                type="slim"
            }
            ButtonType.LARGE_CARD -> {
                type="large"
            }
            ButtonType.RECTANGLE -> {
                type = "rectangle"
            }

            ButtonType.BUTTON -> {
                type = "panel"
            }
            ButtonType.PILL -> {
                type = "pill"
            }
        }
        val panels = StringBuilder()
        val str = StringBuilder("\tcolorMap {\n")
        for (x in 0 until num) {
            val count = x % 5
            val color = getRandomColorHex()
            str.append("\t\t\t\tcolor(\"$color\")\n")
            panels.append("\n\t$type{\n")
            panels.append("\t\tlink = \"https://www.apple.com\"\n")
            panels.append("\t\tlabel = \"$color\"\n")
            val formatter = SimpleDateFormat("MM/dd/yyyy")
            val today = Date()
            val next = Date(today.time - ((1000 * 60 * 60 * 24)* x))
            panels.append("\t\tdate = \"${formatter.format(next)}\"\n")
            panels.append("\t\ttype = \"Advertising $count\"\n")
            if(buttonKind == ButtonType.SLIM_CARD || buttonKind == ButtonType.LARGE_CARD) {
                panels.append("\t\tdescription = \"Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit...\"\n")
                panels.append("\t\tauthor(\"author1\")\n")
                panels.append("\t\tauthor(\"author2\")\n")
            }
            if(buttonKind == ButtonType.RECTANGLE) {
                panels.append("link {\n")
                panels.append("\thref = \"https://www.apple.com\"\n")
                panels.append("\tlabel = \"Devices\"\n")
                panels.append("}\n")
            }
            if(buttonKind == ButtonType.LARGE_CARD) {
                panels.append("line1=Line(\"Line1\")\n")
                panels.append("line2=Line(\"Line2\")\n")
            }
            panels.append("\t}")
        }
        str.append("\t\t\t}")
        return Pair(panels, str)
    }


}

fun sourceToPanel(contents: String, scriptLoader: ScriptLoader): Panels {
    //language=kotlin
    val source = """
            import io.docops.asciidoc.buttons.dsl.*
            import io.docops.asciidoc.buttons.models.*
            import io.docops.asciidoc.buttons.theme.*
            import io.docops.asciidoc.buttons.*
            import io.docops.asciidoc.buttons.models.ButtonImage
            import io.docops.asciidoc.buttons.theme.GradientStyle
            
            $contents
        """.trimIndent()
    try {
        return scriptLoader.parseKotlinScript(source = source)
    } catch (e: Exception) {
        println(source)
        throw e
    }

}
