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

class ConnectorHandler {
    fun handleSVG(payload: String, type: String, scale: String, useDark: Boolean): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val svg = fromRequestToConnector(data, scale = scale.toFloat(), useDark = useDark)
        return ResponseEntity(svg.shapeSvg.toByteArray(), headers, HttpStatus.OK)
    }

    fun handlePNG(payload: String, type: String, scale: String, useDark: Boolean): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "png", StandardCharsets.UTF_8)
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val svg = fromRequestToConnector(
            data,
            scale = scale.toFloat(),
            useDark = useDark,
            type = type
        )
        //println(svg.shapeSvg)
        val png = SvgToPng().toPngFromSvg(
            svg.shapeSvg,
            Pair(svg.height.toString(), svg.width.toString())
        )
        return ResponseEntity(png, headers, HttpStatus.OK)
    }

    fun fromRequestToConnector(contents: String, scale: Float, useDark: Boolean, type: String = "SVG"): ShapeResponse {
        val connectors = Json.decodeFromString<Connectors>(contents)
        val maker = ConnectorMaker(connectors = connectors.connectors, useDark = useDark, type)
        val svg = maker.makeConnectorImage(scale = scale)
        return svg
    }

}