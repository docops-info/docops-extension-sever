package io.docops.extension.server.ktor.plugins

import io.docops.extension.server.echart.chartRoutes
import io.docops.extension.server.web.extensions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureRouting() {


    routing {
        get("/") {
            val idx = Application::class.java.classLoader.getResourceAsStream("index.html")
            idx?.let {
                call.respondBytes(
                    idx.readBytes(),
                    ContentType.Text.Html
                )
            }
        }
        get("/partials/*") {
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
        post("/download") {
            val body = call.receiveText()
            call.response.header("Content-Disposition", "attachment; filename=\"snippet.kts\"")
            call.respondBytes(body.toByteArray())
        }
        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
        // Static plugin. Try to access `/static/index.html`
        static("/editor") {
            resources("editor")
        }
        static("/swagger-ui") {
            resources("swagger-ui")
        }
        //register the extension routing
        extensions()
        chartRoutes()
    }
}




