package io.docops.extension.server.ktor.plugins

import io.docops.extension.server.web.extensions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureRouting() {


    routing {
        get("/") {
            call.respondBytes(
                "<span><a href='http://docops.io'>DocOps.io</a></span>".toByteArray(),
                ContentType.Text.Html
            )
        }
        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
        static("/swagger-ui") {
            resources("swagger-ui")
        }
        //register the extension routing
        extensions()
    }
}




