package io.docops.extension.server.web

import io.docops.asciidoc.buttons.dsl.PanelButton
import io.docops.asciidoc.buttons.dsl.Panels
import io.docops.asciidoc.buttons.service.PanelService
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.Grouping
import io.docops.asciidoc.buttons.theme.GroupingOrder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.nio.charset.Charset
import java.util.*
import java.util.zip.GZIPInputStream

fun Route.panels() {
    val scriptLoader = ScriptLoader()

    route("/api") {
        get("/panel") {
            val data = call.request.queryParameters["data"] as String
            val type = call.request.queryParameters["type"]
            val isPDF = "PDF" == type
            val contents = uncompressString(data)
            val imgSrc = contentsToImageStr(contents, scriptLoader, isPDF)
            call.respondBytes(imgSrc.toByteArray(), ContentType.Image.SVG, HttpStatusCode.OK)
        }

        get("/panel/pancolor") {
            val data = call.request.queryParameters["color"] as String
            val label = call.request.queryParameters["label"] as String
            /*val imgSrc = """
                <svg xmlns="http://www.w3.org/2000/svg" width="100" height="10">
                    <rect x="0" y="0" width="100" height="10" fill="$data"  rx="5" ry="5"/>
                </svg>
            """.trimIndent()*/
            //language=svg
            val imgSrc = """
                <svg xmlns="http://www.w3.org/2000/svg" width="300" height="20">
                    <rect x="0" y="0" width="300" height="20" fill="$data"  rx="5" ry="5"/>
                    <a href="https://www.apple.com">
                        <text x="150" y="15" text-anchor="middle">$label</text>
                    </a>
                 </svg>
            """.trimIndent()
            //val imgSrc = """<svg><rect width="100" height="100" fill="red"></rect></svg>"""
            call.respondBytes(imgSrc.toByteArray(), ContentType.Image.SVG, HttpStatusCode.OK)
        }


        get("/panel/lines") {
            val data = call.request.queryParameters["data"] as String
            val server = call.request.queryParameters["server"] as String
            val contents = uncompressString(data)
            val panels = sourceToPanel(contents = contents, scriptLoader = scriptLoader)
            val panelService = PanelService()
            call.respondBytes(
                panelService.toLines("Link List", panels, server).joinToString("\n").toByteArray(),
                ContentType.Text.Plain,
                HttpStatusCode.OK
            )
        }
        post("/panel") {
            val contents = call.receiveText()
            val imgSrc = contentsToImageStr(contents, scriptLoader, false)
            call.respondBytes(imgSrc.toByteArray(), ContentType.Image.SVG, HttpStatusCode.OK)
        }
        post("/panel/csv") {
            val contents = call.receiveText()
            val panels: Panels = io.docops.asciidoc.buttons.dsl.panels {
                columns = 3
                panelButtons = strToPanelButtons(contents)
                theme {
                    layout {
                        columns = 2
                        groupBy = Grouping.TITLE
                        groupOrder = GroupingOrder.ASCENDING
                    }
                    colorMap {
                        color("#e0a1c6")
                        color("#f3b69e")
                        color("#e1beb0")
                        color("#a8c4e1")
                        color("#c2d1e4")
                        color("#ede7a7")
                        color("#b0e2fc")
                        color("#eda0d3")
                        color("#e5b6c4")
                        color("#e8e59f")
                        color("#f0fc9f")
                        color("#ef999f")
                        color("#a6dde2")
                        color("#c1e1e1")
                        color("#e4b0bf")
                        color("#eec4b6")
                        color("#cae6e6")
                        color("#eed9c3")
                        color("#e8a1e5")
                        color("#e7c1b9")
                    }
                }
            }
            val panelService = PanelService()
            val imgSrc = panelService.fromPanelToSvg(panels)
            call.respondBytes(imgSrc.toByteArray(), ContentType.Image.SVG, HttpStatusCode.OK)
        }
        post("/panel/plain") {
            val contents = call.receiveText()
            val imgSrc = contentsToImageStr(contents, scriptLoader, false)
            call.respondText("""<div>
                $imgSrc
                </div>""".trimMargin(),ContentType.Image.SVG, HttpStatusCode.OK)
            //val str = Base64.getEncoder().encodeToString(imgSrc.toByteArray())
            //call.respondText("data:image/svg+xml;base64,$str", ContentType.Text.Plain, HttpStatusCode.OK)
        }
    }
}

private fun uncompressString(zippedBase64Str: String): String {
    val bytes: ByteArray = Base64.getUrlDecoder().decode(zippedBase64Str)
    var zi: GZIPInputStream? = null
    zi = GZIPInputStream(ByteArrayInputStream(bytes))
    val reader = InputStreamReader(zi, Charset.defaultCharset())
    val input = BufferedReader(reader)

    val content = StringBuilder()
    try {
        var line = input.readLine()
        while (line != null) {
            content.append(line + "\n")
            line = input.readLine()
        }
    } finally {
        reader.close()
    }
    return content.toString()
}

private fun contentsToImageStr(
    contents: String,
    scriptLoader: ScriptLoader, isPDf: Boolean
): String {

    try {
        val panels: Panels = sourceToPanel(contents, scriptLoader)
        val panelService = PanelService()
        panels.isPdf = isPDf
        return panelService.fromPanelToSvg(panels)
    } catch (e: Exception) {
        e.printStackTrace()
        throw java.lang.RuntimeException(e)
    }
}

private fun strToPanelButtons(str: String): MutableList<PanelButton> {
    val result = mutableListOf<PanelButton>()
    str.lines().forEach { line ->
        val items = line.split("|")
        val pb = PanelButton()
        pb.label = items[0].trim()
        pb.link = items[1].trim()
        if (items.size == 3) {
            pb.description = items[2]
        }
        result.add(pb)
    }
    return result
}

fun sourceToPanel(contents: String, scriptLoader: ScriptLoader): Panels {
    //language=kotlin
    val source = """
            import io.docops.asciidoc.buttons.dsl.*
            import io.docops.asciidoc.buttons.models.*
            import io.docops.asciidoc.buttons.theme.*
            import io.docops.asciidoc.buttons.*
            import io.docops.asciidoc.buttons.models.ButtonImage
            
            $contents
        """.trimIndent()
    try {
        return scriptLoader.parseKotlinScript(source = source)
    } catch (e: Exception) {
        println(source)
        throw e
    }

}