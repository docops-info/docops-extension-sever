package io.docops.extension.server.web

import io.docops.asciidoc.buttons.service.PanelService
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.ButtonType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor


class ColorDivCreator(val num: Int, val buttonKind: ButtonType, val columns: String, val groupBY: String, val orderBy: String, val dropShadow: String) {
    val scriptLoader = ScriptLoader()
    fun genPanels(): ByteArray {
        var cols = 3
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
            <div class='contentBox'>
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
            <span id="pointsValue" data-hx-swap-oob="true">$num</span>
            <span id="colsValue" data-hx-swap-oob="true">$cols</span>
            <span id="shadowValue" data-hx-swap-oob="true">$dropShadow</span>
        """.trimIndent()
        return results.toByteArray()
    }

    private fun getRandomColor(): String {
        val letters = "0123456789ABCDEF"
        var color = "#"
        for (i in 0..5) {
            color += letters[floor(Math.random() * 16).toInt()]
        }
        return color
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
        if(buttonKind == ButtonType.ROUND) {
            type = "round"
        }
        else if(buttonKind == ButtonType.SLIM_CARD) {
            type="slim"
        }
        else if(buttonKind == ButtonType.LARGE_CARD) {
            type="large"
        }
        val panels = StringBuilder()
        val str = StringBuilder("\tcolorMap {\n")
        for (x in 0 until num) {
            val count = x % 5
            val color = getRandomColor()
            str.append("\t\t\t\tcolor(\"$color\")\n")
            panels.append("\n\t$type{\n")
            panels.append("\t\tlink = \"https://www.apple.com\"\n")
            panels.append("\t\tlabel = \"$color\"\n")
            if(buttonKind == ButtonType.SLIM_CARD || buttonKind == ButtonType.LARGE_CARD) {
                panels.append("\t\ttype = \"Advertising $count\"\n")
                panels.append("\t\tdescription = \"Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit...\"\n")
                panels.append("\t\tauthor(\"author1\")\n")
                panels.append("\t\tauthor(\"author2\")\n")
                val formatter = SimpleDateFormat("MM/dd/yyyy")
                val today = Date()
                val next = Date(today.time - ((1000 * 60 * 60 * 24)* x))
                panels.append("\t\tdate =\"${formatter.format(next)}\"\n")
            }
            panels.append("\t}")
        }
        str.append("\t\t\t}")
        return Pair(panels, str)
    }
}
