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
            var download= false
            val params = call.receiveParameters()
            val fillColor = params["fillColor"]!!
            val fontColor = params["fontColor"]!!
            val line1 = params["line1"]!!
            val line2 = params["line2"]!!
            val line3 = params["line3"]!!
            val downloadParam = params["download"]
            if("on" == downloadParam) {
                download = true
            }
            //language=html

            val results = StringBuilder("")
            if(!download) {
                results.append("<div id='imageblock'>")
            }
            results.append("""
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- Generated by Pixelmator Pro 3.0.1 -->
            <svg width="1000" height="1000" viewBox="0 0 1000 1000" xmlns="http://www.w3.org/2000/svg"
                 xmlns:xlink="http://www.w3.org/1999/xlink">
                <path id="Color-Fill" fill="$fillColor" stroke="none" d="M 0 0 L 1000 0 L 1000 1000 L 0 1000 Z"/>
                <g id="Logo-Placeholder-Replace-With-Your-Logo-">
                    <text id="text1" text-anchor="middle" xml:space="preserve">""")
            if(line1.isNotEmpty()) {
                results.append("""
                        <tspan x="50%" y="451" font-family="Rubik Mono One" font-size="90" fill="$fontColor" letter-spacing="2.56" xml:space="preserve">${line1.uppercase()}</tspan>
                """.trimIndent())
            }
            if(line2.isNotEmpty()) {
                results.append("""
   <tspan x="50%" y="551" font-family="Rubik Mono One" font-size="90" fill="$fontColor" letter-spacing="2.56"
                               xml:space="preserve">${line2.uppercase()}</tspan>""".trimIndent())
            }
            if(line3.isNotEmpty()) {
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
        """.trimIndent())
            if(download) {
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "ktor_logo.svg")
                        .toString()
                )
                call.respondBytes(results.toString().toByteArray(), ContentType.Image.SVG, HttpStatusCode.Accepted)
            } else {
                results.append("</div>")
                call.respondBytes(results.toString().toByteArray(), ContentType.Text.Html, HttpStatusCode.Accepted)
            }
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

