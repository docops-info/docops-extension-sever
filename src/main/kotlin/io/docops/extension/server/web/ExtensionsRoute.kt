package io.docops.extension.server.web

import io.docops.asciidoc.buttons.dsl.FontWeight
import io.docops.asciidoc.buttons.dsl.PanelButton
import io.docops.asciidoc.buttons.dsl.Panels
import io.docops.asciidoc.buttons.dsl.panels
import io.docops.asciidoc.buttons.service.PanelService
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.Grouping
import io.docops.asciidoc.buttons.theme.GroupingOrder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterOutputStream


fun Route.extensions() {
    val scriptLoader = ScriptLoader()
    route("/api"){
        get("/ping") {
            call.respondBytes("OK".toByteArray(), ContentType.Text.Html, HttpStatusCode.OK)
        }

        get("/panel") {
            val data = call.request.queryParameters["data"] as String
            val type = call.request.queryParameters["type"]
            val isPDF = "PDF" == type
            val contents = uncompressString(data)
            val imgSrc = contentsToImageStr(contents, scriptLoader, isPDF)
            call.respondBytes(imgSrc.toByteArray(), ContentType.Image.SVG, HttpStatusCode.OK)
        }
        get("/panel/lines") {
            val data = call.request.queryParameters["data"] as String
            val contents = uncompressString(data)
            val panels = sourceToPanel(contents = contents, scriptLoader = scriptLoader)
            val panelService = PanelService()
            call.respondBytes(panelService.toLines("Link List", panels).joinToString("\n").toByteArray(), ContentType.Text.Plain, HttpStatusCode.OK)
        }
        post("/panel") {
            val contents = call.receiveText()
            val imgSrc = contentsToImageStr(contents, scriptLoader, false)
            call.respondBytes(imgSrc.toByteArray(), ContentType.Image.SVG, HttpStatusCode.OK)
        }
        post("/panel/csv") {
            val contents = call.receiveText()
            val panels: Panels = panels {
                columns = 3
                panelButtons = strToPanelButtons(contents)
                theme {
                    layout {
                        columns = 2
                        groupBy = Grouping.TITLE
                        groupOrder = GroupingOrder.ASCENDING
                    }
                    font {
                        color = "#000000"
                        weight = FontWeight.bold
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
            //val str = "data:image/svg+xml;utf8," + String(imgSrc.toByteArray())
            call.respondBytes(imgSrc.toByteArray(), ContentType.Image.SVG, HttpStatusCode.OK)
            //call.respondBytes(imgSrc.toByteArray(), ContentType.Any, HttpStatusCode.OK)
        }
        post("/uncompress") {
            try {
                val contents = call.receiveText()
                //val ctn = uncompressString(contents)

                val res= CompressionUtil.decompressB64(contents)

                call.respondBytes(res.toByteArray(), ContentType.Any, HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

private fun contentsToImageStr(
    contents: String,
    scriptLoader: ScriptLoader, isPDf: Boolean
): String {

    val panels: Panels = sourceToPanel(contents, scriptLoader)
    val panelService = PanelService()
    panels.isPdf = isPDf
    return panelService.fromPanelToSvg(panels)
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
            content.append(line+"\n")
            line = input.readLine()
        }
    } finally {
        reader.close()
    }
    return content.toString()
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
    val source = """
            import io.docops.asciidoc.buttons.dsl.*
            import io.docops.asciidoc.buttons.models.*
            import io.docops.asciidoc.buttons.theme.*
            import io.docops.asciidoc.buttons.*
            
            $contents
        """.trimIndent()
    return scriptLoader.parseKotlinScript(source = source)
}

object CompressionUtil {
    @Throws(IOException::class)
    fun compressAndReturnB64(text: String): String {
        return String(Base64.getUrlEncoder().encode(compress(text)))
    }

    @Throws(IOException::class)
    fun decompressB64(b64Compressed: String?): String {
        val decompressedBArray = decompress(Base64.getUrlDecoder().decode(b64Compressed))
        return String(decompressedBArray, StandardCharsets.UTF_8)
    }

    @Throws(IOException::class)
    fun compress(text: String): ByteArray {
        return compress(text.toByteArray())
    }

    @Throws(IOException::class)
    fun compress(bArray: ByteArray?): ByteArray {
        val os = ByteArrayOutputStream()
        DeflaterOutputStream(os).use { dos -> dos.write(bArray) }
        return os.toByteArray()
    }

    @Throws(IOException::class)
    fun decompress(compressedTxt: ByteArray?): ByteArray {
        val os = ByteArrayOutputStream()
        InflaterOutputStream(os).use { ios -> ios.write(compressedTxt) }
        return os.toByteArray()
    }
}

