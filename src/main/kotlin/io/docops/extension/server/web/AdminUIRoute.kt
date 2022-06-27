package io.docops.extension.server.web

import io.docops.asciidoc.buttons.theme.ButtonType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminUI() {
    route("/api") {
        put("/colorgen") {
            val params = call.receiveParameters()
            val pts = params["points"]
            val btnType = params["buttonType"]
            val columns = params["columns"]

            if (pts != null && btnType != null) {
                val cd = ColorDivCreator()
                val buttonKind = ButtonType.valueOf(btnType)
                val panel = cd.genPanels(pts.toInt(), buttonKind, columns)
                call.respondBytes(panel, ContentType.Text.Html, HttpStatusCode.Accepted)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}
