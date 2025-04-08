package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import org.springframework.http.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class AdrHandler {

    fun handleSVG(payload: String, scale: String, useDark: Boolean, backEnd: String): ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val config = AdrParserConfig(newWin = true, isPdf = "pdf".equals(backEnd, ignoreCase = true), lineSize = 95, increaseWidthBy = 0, scale = scale.toFloat())
        val adr = ADRParser().parse(data, config)
        var svg = AdrMaker().makeAdrSvg(adr, dropShadow = true, config, useDark)
        adr.urlMap.forEach { (t, u) ->
            svg = svg.replace("_${t}_", u)
        }
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }


}