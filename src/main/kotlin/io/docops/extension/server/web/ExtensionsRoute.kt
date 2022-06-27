package io.docops.extension.server.web

import io.docops.asciidoc.buttons.dsl.FontWeight
import io.docops.asciidoc.buttons.dsl.PanelButton
import io.docops.asciidoc.buttons.dsl.Panels
import io.docops.asciidoc.buttons.dsl.panels
import io.docops.asciidoc.buttons.service.PanelService
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.ButtonType
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
    route("/api") {
        get("/ping") {
            call.respondBytes("OK".toByteArray(), ContentType.Text.Html, HttpStatusCode.OK)
        }

        post("/uncompress") {
            try {
                val contents = call.receiveText()
                     val res = CompressionUtil.decompressB64(contents)

                call.respondBytes(res.toByteArray(), ContentType.Any, HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest)
            }
        }

    }
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

