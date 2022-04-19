package io.docops.extension.server.ktor.plugins

import io.docops.asciidoc.buttons.dsl.FontWeight
import io.docops.asciidoc.buttons.dsl.PanelButton
import io.docops.asciidoc.buttons.dsl.Panels
import io.docops.asciidoc.buttons.dsl.panels
import io.docops.asciidoc.buttons.service.PanelService
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.Grouping
import io.docops.asciidoc.buttons.theme.GroupingOrder
import io.docops.extension.server.web.extensions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*
import java.util.zip.GZIPInputStream


fun Application.configureRouting() {
    val scriptLoader = ScriptLoader()


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
        //register the extension routing
        extensions()
    }
}




