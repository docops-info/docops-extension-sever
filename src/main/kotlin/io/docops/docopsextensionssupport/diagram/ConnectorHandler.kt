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


/**
 * ConnectorHandler class is responsible for handling requests related to SVG and PNG images.
 */
class ConnectorHandler {



    /**
     * Handles the SVG request and returns the SVG image as a byte array.
     *
     * @param payload The compressed and encoded SVG payload.
     * @param type The type of the SVG image.
     * @param scale The scale of the SVG image.
     * @param useDark A boolean indicating whether to use dark mode for the SVG image.
     * @return The ResponseEntity containing the SVG image as a byte array.
     */
    fun handleSVG(payload: String, type: String, scale: String, useDark: Boolean): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val svg = fromRequestToConnector(data, scale = scale.toFloat(), useDark = useDark)
        return ResponseEntity(svg.shapeSvg.toByteArray(), headers, HttpStatus.OK)
    }


    private fun createHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "png", StandardCharsets.UTF_8)
        return headers
    }

    private fun getSvgFromPayload(contents: String, scale: Float, useDark: Boolean, type: String = "SVG"): ShapeResponse {
        return fromRequestToConnector(contents, scale, useDark, type)
    }



    private fun fromRequestToConnector(contents: String, scale: Float, useDark: Boolean, type: String = "SVG"): ShapeResponse {
        val connectors = decodeFromJson(contents)
        val maker = createConnectorMaker(connectors.connectors, useDark, type)
        return makeConnectorImage(maker, scale)
    }

    private fun decodeFromJson(contents: String): Connectors {
        return Json.decodeFromString<Connectors>(contents)
    }

    private fun createConnectorMaker(
        connectors: MutableList<Connector>,
        useDark: Boolean,
        type: String
    ): ConnectorMaker {
        return ConnectorMaker(connectors, useDark, type)
    }

    private fun makeConnectorImage(maker: ConnectorMaker, scale: Float): ShapeResponse {
        return maker.makeConnectorImage(scale)
    }

}