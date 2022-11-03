package io.docops.extension.server.web

import io.docops.asciidoc.buttons.theme.ButtonType
import io.docops.asciidoctorj.extension.adr.ADRParser
import io.docops.asciidoctorj.extension.adr.AdrMaker
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

fun Route.adminUI() {
    route("/api") {
        put("/colorgen") {
            try {
                var bold = false
                var underline = false
                var italics = false
                var newWin = false
                val params = call.receiveParameters()
                val pts = params["points"]!!
                val btnType = params["buttonType"]!!
                val columns = params["columns"]!!
                val groupBY = params["sortBy"]!!
                val orderBy = params["order"]!!
                val case = params["case"]!!
                val dropShadow = params["dropShadow"]!!
                val buttonKind = ButtonType.valueOf(btnType)
                val color = params["color"]!!
                val weight = params["bold"]
                if (weight != null) {
                    bold = true
                }
                val italic = params["italic"]
                val und = params["underline"]
                val newWinParam = params["newWin"]
                if ("on" == italic) {
                    italics = true
                }
                if ("on" == und) {
                    underline = true
                }
                if ("on" == newWinParam) {
                    newWin = true
                }
                val font = params["font"]!!
                val fpoint = params["fpoint"]!!
                val size = params["size"]!!
                val spacing = params["spacing"]!!
                val cd = ColorDivCreator(
                    num = pts.toInt(), buttonKind = buttonKind,
                    columns = columns, groupBY = groupBY,
                    orderBy = orderBy, dropShadow = dropShadow,
                    color = color, weight = bold, font = font,
                    italics = italics, decoration = underline, size = size + fpoint,
                    case = case, newWin = newWin, spacing = spacing
                )
                val panel = cd.genPanels()

                call.respondBytes(panel, ContentType.Text.Html, HttpStatusCode.Accepted)
            } catch (e: Exception) {
                e.printStackTrace()
                //language=html
                call.respondBytes(
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
    """.toByteArray(), ContentType.Text.Html, HttpStatusCode.BadRequest
                )
            }
        }
        put("/adr") {
            val params = call.receiveParameters()
            val title = params["title"]!!
            val date = params["date"]!!
            val status = params["status"]!!
            val decision = params["decision"]!!
            val consequences = params["consequences"]!!
            val participants = params["participants"]!!
            val context = params["context"]!!
            try {
                val adrText = """
   Title: $title
        Date: $date
        Status:$status
        Context:$context
        Decision:$decision
        Consequences:$consequences
        Participants:$participants 
        """.trimIndent()
                val adr = ADRParser().parse(adrText)
                var svg = (AdrMaker().makeAdrSvg(adr))
                adr.urlMap.forEach { (t, u) ->
                    svg = svg.replace("_${t}_", u)
                }
                val results = makeAdrSource(adrText, svg)
                call.respondBytes(results.toByteArray(), ContentType.Text.Html, HttpStatusCode.Accepted)
            } catch (e: Exception) {
                e.printStackTrace()
                //language=html
                call.respondBytes("""incomplete""".toByteArray(), ContentType.Text.Html, HttpStatusCode.BadRequest)
            }
        }
        put("/panelimage"){
            val params = call.receiveParameters()
            var fillColor = params["fillColor"]!!
            val imageType = params["imageType"]!!
            val fontColor = params["fontColor"]!!
            val line1 = params["line1"]!!
            val line2 = params["line2"]!!
            val line3 = params["line3"]!!
            val transparentParam = params["transparent"]
            if ("on" == transparentParam) {
                fillColor = "none"
            }
            //language=html
            val contents =  if("CIRCLE" == imageType) {
                makePanelRoundMiddleImage(fillColor, fontColor, line1, line2, line3)

            } else {
                makeLineImage(fillColor, fontColor, line1, line2, line3)
            }
            call.respondBytes(contents.toByteArray(), ContentType.Text.Html, HttpStatusCode.Accepted)
        }
    }
    get("/partials/*") {
        //val port= this@adminUI.environment?.config?.propertyOrNull("ktor.deployment.rootPath")?.getString()
        var path = call.request.uri
        path = path.replace("/extension/", "")
        val resource = Application::class.java.classLoader.getResourceAsStream(path)
        if (resource != null) {
            call.response.headers.append("HX-Trigger-After-Settle", "showFrame")
            call.respondBytes(
                resource.readBytes(),
                ContentType.Text.Html
            )
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

}

fun makeLineImage(fillColor: String, fontColor: String, line1: String, line2: String?, line3: String?) : String {
    val results = StringBuilder("""
            <div id='imageblock'>
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="1000" height="1000" viewBox="0 0 1000 1000" id="panelText" xmlns="http://www.w3.org/2000/svg"
                 xmlns:xlink="http://www.w3.org/1999/xlink">
                <path id="Color-Fill" fill="$fillColor" stroke="none" d="M 0 0 L 1000 0 L 1000 1000 L 0 1000 Z"/>
                <g id="Logo-Placeholder-Replace-With-Your-Logo-">
                    <text id="text1" text-anchor="middle" xml:space="preserve">""")
    if(line1.isNotEmpty()) {
        results.append("""
                        <tspan x="50%" y="451" font-family="Rubik Mono One" font-size="90" fill="$fontColor" letter-spacing="2.56" xml:space="preserve">${line1.uppercase()}</tspan>
                """.trimIndent())
    }
    line2?.let {
        results.append("""
   <tspan x="50%" y="551" font-family="Rubik Mono One" font-size="90" fill="$fontColor" letter-spacing="2.56"
                               xml:space="preserve">${line2.uppercase()}</tspan>""".trimIndent())
    }

    line3?.let {
        results.append("""
    <tspan x="50%" y="651" font-family="Rubik Mono One" font-size="90" fill="$fontColor" letter-spacing="2.56"
                               xml:space="preserve">${line3.uppercase()}</tspan>
                  """.trimIndent())
    }
    results.append("""</text>
                </g>
                <g id="Dividers">
                    <path id="Bottom-Divider-" fill="none" stroke="#3f4652" stroke-width="5" stroke-linecap="round"
                          stroke-linejoin="round" d="M 117.5 707.5 L 882.5 707.5"/>
                    <path id="Top-Divider-" fill="none" stroke="#3f4652" stroke-width="5" stroke-linecap="round"
                          stroke-linejoin="round" d="M 117.5 292.5 L 882.5 292.5"/>
                </g>
            </svg>
            </div>
        """.trimIndent())
    return results.toString()
}
fun makePanelRoundMiddleImage(fillColor: String, fontColor: String, line1: String, line2: String?, line3: String?) : String
{
    var start = 420
    val span1 = """<tspan text-anchor="middle" x="50%" y="$start" font-family="Helvetica Neue" font-size="72" font-stretch="condensed" font-weight="700" fill="$fontColor" xml:space="preserve">$line1</tspan>"""
    var span2 = ""
    var span3 = ""
    line2?.let {
        start += 72
        span2 = """<tspan text-anchor="middle" x="50%" y="$start" font-family="Helvetica Neue" font-size="72" font-stretch="condensed"
                           font-weight="700" fill="$fontColor" xml:space="preserve">$line2</tspan>"""
    }
    line3?.let {
        start+=72
        span3 = """<tspan text-anchor="middle" x="50%" y="$start" font-family="Helvetica Neue" font-size="72" font-stretch="condensed"
                           font-weight="700" fill="$fontColor" xml:space="preserve">$line3</tspan>"""
    }
    return """
        <div id='imageblock'>
        <?xml version="1.0" encoding="UTF-8"?>
        <svg id="panelText" width="1000" height="1000" viewBox="0 0 1000 1000" xmlns="http://www.w3.org/2000/svg"
             xmlns:xlink="http://www.w3.org/1999/xlink">
            <path id="Color-Fill" fill="$fillColor" stroke="none" d="M 0 0 L 1000 0 L 1000 1000 L 0 1000 Z"/>
            <g id="Logo-Placeholder-Replace-With-Your-Logo-">
                <path id="Logo-Shape" fill="#3f4652" fill-rule="evenodd" stroke="none"
                      d="M 832.5 500 C 832.5 316.365356 683.634644 167.5 500 167.5 C 316.365326 167.5 167.5 316.365356 167.5 500 C 167.5 683.634644 316.365326 832.5 500 832.5 C 683.634644 832.5 832.5 683.634644 832.5 500 Z"/>
                <text text-anchor="middle" id="Beyond-The-Idea" xml:space="preserve">
                    $span1
                    $span2
                    $span3
                </text>
            </g>
        </svg>
        </div>
    """.trimIndent()
}
fun makeAdrSource(txt: String, svg: String): String {
    return """
        <div id='imageblock'>
        $svg
        </div>
        <br/>
        <h3>Adr Source</h3>
        <div class='pure-u-1 pure-u-md-20-24'>
        <pre>
        <code>
        $txt
        </code>
        </pre>
        </div>
        <script>
        var adrSource = `[adr]\n----\n${txt}\n----`;
        document.querySelectorAll('pre code').forEach((el) => {
            hljs.highlightElement(el);
        });
        </script>
    """.trimIndent()
}

