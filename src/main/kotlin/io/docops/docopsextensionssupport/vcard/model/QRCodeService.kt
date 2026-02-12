package io.docops.docopsextensionssupport.vcard.model

import io.nayuki.qrcodegen.QrCode
import qrcode.QRCode
import qrcode.color.Colors
import qrcode.color.Colors.css
import qrcode.raw.ErrorCorrectionLevel
import java.awt.Color
import java.awt.MultipleGradientPaint
import java.awt.RadialGradientPaint
import java.awt.geom.Point2D
import java.util.*


class QRCodeService {

    fun generateQRCodeBase64(data: String, width: Int, height: Int): String {

        val logoBytes = ClassLoader.getSystemResourceAsStream("docops.svg.png")?.readBytes() ?: ByteArray(0)
        val qrCode = QRCode.ofCircles()
            .withSize(13)
            .withColor(Colors.rgba(255, 255, 255, 180))
            .withBackgroundColor(Colors.TRANSPARENT)
            //.withLogo(logoBytes, 150, 150)
            .withInformationDensity(0)
            .withErrorCorrectionLevel(ErrorCorrectionLevel.VERY_HIGH)
            .withMargin(13)
            .build(data)
        // Before drawing the QRCode, draw our gradient as the background
        qrCode.graphics.directDraw {
            it.paint = kotlinGradient(qrCode.canvasSize)
            it.fillRect(0, 0, qrCode.canvasSize, qrCode.canvasSize)
        }

        val bytes = qrCode.renderToBytes()
        val qrBase64 = Base64.getEncoder().encodeToString(bytes)
        return "data:image/png;base64,$qrBase64"
    }

    private fun kotlinGradient(width: Int): RadialGradientPaint {
        val gradientCenter = Point2D.Float(0.0f, width.toFloat())
        // Distances and colors taken from the official Kotlin website
        val dist = floatArrayOf(0.0f, 0.1758f, 0.5031f, 0.9703f)
        val colors = arrayOf(Color(css("#ef4857")), Color(css("#de4970")), Color(css("#b44db0")), Color(css("#7f52ff")))
        return RadialGradientPaint(
            gradientCenter, width.toFloat(), gradientCenter,
            dist, colors,
            MultipleGradientPaint.CycleMethod.NO_CYCLE,
        )
    }
    fun generateQRCode(data: String, width: Int, height: Int): String {
        val qrCode = QrCode.encodeText(data, QrCode.Ecc.HIGH)
        val svg = toSvgString(qrCode, 4, width=width, height = height)
        return svg
    }

    private fun toSvgString(qr: QrCode, border: Int, lightColor: String= "#FFFFFF", darkColor: String =  "#00008a", width: Int, height: Int) : String {

        if (border < 0)
            throw  IllegalArgumentException("Border must be non-negative");
        val brd = border
        val sb =  StringBuilder()
            .append(
                """<svg xmlns="http://www.w3.org/2000/svg" version="1.1" viewBox="0 0 ${qr.size + brd * 2} ${qr.size + brd * 2}" height="$height" width="$width" stroke="none">
			    <rect width="100%" height="100%" fill="$lightColor"/>""")
            .append("""<path d=" """)
        for (y in 0 until qr.size) {
            for (x in 0 until qr.size) {
                if (qr.getModule(x, y)) {
                    if (x != 0 || y != 0)
                        sb.append(" ")
                    sb.append("""M${x + brd},${y + brd}h1v1h-1z""")
                }
            }
        }
        return sb
            .append("""" fill="$darkColor"/>""")
            .append("</svg>")
            .toString();
    }
}