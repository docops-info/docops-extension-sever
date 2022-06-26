package io.docops.extension.server.web

import io.docops.asciidoc.buttons.service.PanelService
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.ButtonType
import kotlin.math.floor


class ColorDivCreator {
    val scriptLoader = ScriptLoader()
    fun genPanels(num: Int, buttonKind: ButtonType): ByteArray {
        val panelStr = genPanelStr(num, buttonKind)
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

    fun genPanelStr(num: Int, buttonKind: ButtonType): Pair<String, StringBuilder> {
        val str = StringBuilder()
        str.append("panels{\n")

        val panelMap = getColorMapAndPanels(num, buttonKind)
        str.append(
            """
    theme {
    ${panelMap.second}
        legendOn = false
        layout {
            columns = 4
        }
    }
    """.trimIndent()
        )
        str.append(panelMap.first)
        str.append("\n}")
        return Pair(str.toString(), panelMap.second)

    }

    private fun getColorMapAndPanels(num: Int, buttonKind: ButtonType): Pair<StringBuilder, StringBuilder> {
        var type = "panel"
        if(buttonKind == ButtonType.ROUND) {
            type = "round"
        }
        else if(buttonKind == ButtonType.SLIM_CARD) {
            type="slim"
        }
        val panels = StringBuilder()
        val str = StringBuilder("colorMap {\n")
        for (x in 0 until num) {
            val color = getRandomColor()
            str.append("\tcolor(\"$color\")\n")
            panels.append("\n\t$type{\n")
            panels.append("\t\tlink = \"https://www.apple.com\"\n")
            panels.append("\t\tlabel = \"$color\"\n")
            panels.append("\t}")
        }
        str.append("}")
        return Pair(panels, str)
    }
}
