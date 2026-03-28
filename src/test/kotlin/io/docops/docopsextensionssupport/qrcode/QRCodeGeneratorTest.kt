package io.docops.docopsextensionssupport.qrcode

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class QRCodeGeneratorTest {
    @Test
    fun generate() {
        val scannableTheme = organicWaveTheme.copy(moduleRadius = 0.15)
        val generator = QRCodeGenerator(useXml = false, 150, 150, theme = scannableTheme)
        // Use M or H for better URL scanning reliability
        val svg = generator.generate("https://www.youtube.com", ErrorCorrectionLevel.M)
        val f = File("gen/qr.svg")
        f.writeText(svg)
    }

}