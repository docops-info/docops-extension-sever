package io.docops.docopsextensionssupport.vcard.renderer

import io.docops.docopsextensionssupport.qrcode.QRTheme
import io.docops.docopsextensionssupport.qrcode.ErrorCorrectionLevel
import io.docops.docopsextensionssupport.qrcode.QRCodeGenerator
import io.docops.docopsextensionssupport.qrcode.organicWaveTheme


class QRCodeService {

    fun generateQRCodeBase64(data: String, width: Int, height: Int, theme: QRTheme = organicWaveTheme): String {

        val generator = QRCodeGenerator(useXml = false, width,height, theme = theme)
        val str = normalizeVCardForQR(data)
        val svg = generator.generate(str, ErrorCorrectionLevel.L  )
        return svg
        /*val qrCode = QRCode.ofRoundedSquares()
            //.withSize(13)
            .withColor(Colors.rgba(255, 255, 255, 180))
            .withBackgroundColor(Colors.BLUE_VIOLET)
            //.withLogo(logoBytes, 150, 150)
            .withInformationDensity(0)
            .withErrorCorrectionLevel(ErrorCorrectionLevel.VERY_HIGH)
            .withMargin(13)
            .build(data)

        val bytes = qrCode.renderToBytes()
        val qrBase64 = "data:image/png;base64,${Base64.encodeToString(bytes)}"
        return qrBase64*/
    }

    private fun normalizeVCardForQR(vcardContent: String): String {
        return vcardContent
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }

}