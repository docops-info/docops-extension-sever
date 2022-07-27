package io.docops.extension.server.web

import io.docops.asciidoc.buttons.theme.ButtonType
import io.docops.asciidoctorj.extension.adr.ADRParser
import io.docops.asciidoctorj.extension.adr.AdrMaker
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.lang.Exception

fun Route.adminUI() {
    route("/api") {
        put("/colorgen") {
            try {
                var bold = false
                var underline = false
                var italics = false
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
                 if(weight != null) {
                    bold = true
                }
                val italic = params["italic"]
                val und = params["underline"]

                if("on" == italic) {
                    italics = true
                }
                if("on" == und) {
                    underline = true
                }
                val font = params["font"]!!
                val fpoint = params["fpoint"]!!
                val size = params["size"]!!

                val cd = ColorDivCreator(
                    num = pts.toInt(), buttonKind =buttonKind,
                    columns =columns, groupBY =groupBY,
                    orderBy =orderBy, dropShadow =dropShadow,
                    color = color, weight =bold, font =font,
                    italics= italics, decoration =underline, size = size+fpoint,
                case = case)
                val panel = cd.genPanels()

                call.respondBytes(panel, ContentType.Text.Html, HttpStatusCode.Accepted)
            } catch (e: Exception) {
                e.printStackTrace()
                //language=html
                call.respondBytes("""
        <input class="checker" type="checkbox" id="o" checked hidden>
        <div class="modal">
          <div class="modal-body">
            <div class="modal-content">Invalid selection(s) for panel!</div>
            <div class="modal-footer">
              <label for="o" class='mlabel'>close</label>
            </div>
          </div>
        </div>
    """.toByteArray(), ContentType.Text.Html, HttpStatusCode.BadRequest)
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
                val adrText =  """
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

