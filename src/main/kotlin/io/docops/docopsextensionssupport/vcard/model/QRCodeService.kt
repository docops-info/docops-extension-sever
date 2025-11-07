package io.docops.docopsextensionssupport.vcard.model

import qrcode.QRCode
import qrcode.color.Colors
import java.util.*


class QRCodeService {

    fun generateQRCodeBase64(data: String, width: Int, height: Int): String {
        val qrCode = QRCode.ofSquares()
            .withGradientColor(Colors.BISQUE, Colors.BLUE)
            .withBackgroundColor(Colors.TRANSPARENT)
            .withSize(25) // Default is 25
            .build(data)
        val bytes = qrCode.renderToBytes()
        val qrBase64 = Base64.getEncoder().encodeToString(bytes)
        return "data:image/png;base64,$qrBase64"
    }
}