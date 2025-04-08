package io.docops.docopsextensionssupport.gallery

import io.docops.docopsextensionssupport.svgsupport.compressString
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class ImageUrlGenerator (val name: String = ""){

    fun getUrl(name: String, kind: String): String {

        val res = ImageUrlGenerator::class.java.getResourceAsStream("/gallery/$name")
        if(null == res) {
            throw IllegalArgumentException("Unable to find image with name $name")
        }
        val payloadBytes = res.readAllBytes().decodeToString()
        val payload =  compressString(payloadBytes)
        val url = """api/docops/svg?kind=$kind&payload=$payload&scale=1.0&outlineColor=${"#024CAA".encodeUrl()}&title=title&numChars=30&type=SVG&useDark=false&backend=html&filename=name.svg""".trimIndent()
        return url
    }
    private fun String.encodeUrl(): String {
        return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
    }
}