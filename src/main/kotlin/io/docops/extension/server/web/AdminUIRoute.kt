package io.docops.extension.server.web

import io.docops.asciidoc.buttons.theme.ButtonType
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
                val params = call.receiveParameters()
                val pts = params["points"]!!
                val btnType = params["buttonType"]!!
                val columns = params["columns"]!!
                val groupBY = params["sortBy"]!!
                val orderBy = params["order"]!!
                val buttonKind = ButtonType.valueOf(btnType)

                val cd = ColorDivCreator(pts.toInt(), buttonKind, columns, groupBY, orderBy)
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
