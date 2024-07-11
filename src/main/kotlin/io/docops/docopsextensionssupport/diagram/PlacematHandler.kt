package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.ShapeResponse
import io.docops.docopsextensionssupport.web.panel.uncompressString
import kotlinx.serialization.json.Json
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class PlacematHandler {
    fun handleSVG(payload: String, type: String, backend: String): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        val isPDF = backend == "pdf"
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val svg = fromRequestToPlaceMat(data, type, isPDF)
        return ResponseEntity(svg.shapeSvg.toByteArray(), headers, HttpStatus.OK)
    }
    fun handlePNG(payload: String, type: String): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "png", StandardCharsets.UTF_8)
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val svg = fromRequestToPlaceMat(data, type, true)

        val png = SvgToPng().toPngFromSvg(
            svg.shapeSvg,
            Pair(svg.height.toString(), svg.width.toString())
        )
        return ResponseEntity(png, headers, HttpStatus.OK)
    }
    fun fromRequestToPlaceMat(contents: String, type: String = "SVG", isPDF: Boolean): ShapeResponse {
        val pms = Json.decodeFromString<PlaceMatRequest>(contents)
        val maker = PlaceMatMaker(placeMatRequest = pms, type, isPDF)
        return maker.makePlacerMat()
    }
}