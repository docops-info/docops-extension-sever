package io.docops.extension.server.ktor.plugins

import io.docops.extension.server.echart.chartRoutes
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
                //language=html
                """
                    <html lang='en'>
                    <head>
                    <title>DocOps.io Useful tools</title>
                    <!-- Google Fonts -->
                    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,300italic,700,700italic">
                    <!-- CSS Reset -->
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/8.0.1/normalize.css">
                    <!-- Milligram CSS -->
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/milligram/1.4.1/milligram.css">
                    </head>
                    <body>
                    <div id='root'>
                    <header class="Header">
                    <section class="container">
                    <div class="row">
                    <div class="column column-0">
                    <a class="Link Logo" href="/">
                    </a></li></ul></nav></div></div></section>
                    </header>
                    <main class="Main"><section class="container">
                    <div><a href='http://docops.io'>DocOps.io</a></div>
                    <div><a href='/editor/index.html'>Editor</a></div>
                    <div><a href='/editor/dataset.html'>Dataset</a></div>
                    <div><a href='/editor/chart.html'>Chart</a></div>
                    <div><a href='/editor/treechart.html'>Tree Chart</a></div>
                    <div><a href='/editor/stacked.html'>Stacked</a></div>
                    <div><a href='/static/echart/demo.html'>Demo Echart</a></div>
                    </main>
                    
                    </div>
                    </body>
                    </html>

                """.trimMargin()
                    .toByteArray(),
                ContentType.Text.Html
            )
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




