package io.docops.docopsextensionssupport.vcard.renderer

import io.docops.docopsextensionssupport.qrcode.cyberNeonTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.vcard.PhoneType
import io.docops.docopsextensionssupport.vcard.VCard
import io.docops.docopsextensionssupport.vcard.VCardConfig
import io.docops.docopsextensionssupport.vcard.VCardGeneratorService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Premium design renderer that follows the Apple-inspired Design System.
 * Clean, precise, and typographic-first.
 */
class PremiumCardRenderer(val useDark: Boolean) : VCardRenderer {

    override val designKey: String = "premium"

    private val theme = ThemeFactory.getThemeByName("premium", useDark)

    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        val vCardGeneratorService = VCardGeneratorService()
        val qrCodeService = QRCodeService()
        
        val vCardData = vCardGeneratorService.generateMinimalVCard(vcard)
        val qrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 120, 120, theme = cyberNeonTheme)
        val largeQrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 400, 400, theme = cyberNeonTheme)

        val id = Uuid.random().toHexString()

        return buildString {
            appendLine("""<svg xmlns="http://www.w3.org/2000/svg" id="id_$id" width="350" height="200" viewBox="0 0 900 540" xmlns:xlink="http://www.w3.org/1999/xlink">""")
            
            appendDefs(id)
            appendBackground()
            appendCard(vcard, qrCodeBase64, largeQrCodeBase64, id)
            
            appendInteractiveScript(id)
            appendLine("</svg>")
        }
    }

    private fun StringBuilder.appendDefs(id: String) {
        appendLine("""
            <defs>
                ${theme.fontImport}
                <filter id="premiumShadow_$id" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="12" stdDeviation="24" flood-color="${if (useDark) "#000000" else "#0F172A"}" flood-opacity="0.12"/>
                </filter>
                <clipPath id="cardClip_$id">
                    <rect width="780" height="420" rx="8" ry="8"/>
                </clipPath>
            </defs>
        """.trimIndent())
    }

    private fun StringBuilder.appendBackground() {
        appendLine("""<rect width="100%" height="100%" fill="${if (useDark) "#020617" else "#F9FAFB"}" rx="8" ry="8"/>""")
    }

    private fun StringBuilder.appendCard(vcard: VCard, qrCodeBase64: String, largeQrCodeBase64: String, id: String) {
        // Card Body
        appendLine("""<g transform="translate(60,60)" filter="url(#premiumShadow_$id)">""")
        appendLine("""<rect width="780" height="420" rx="8" ry="8" fill="${theme.canvas}"/>""")
        
        // Left Info Section
        appendLine("""<g transform="translate(48,48)">""")
        
        // Name & Title
        val fullName = "${vcard.firstName} ${vcard.lastName}"
        appendLine("""<text x="0" y="32" font-family="${theme.fontFamily}" fill="${theme.primaryText}" font-size="36" font-weight="800" letter-spacing="-0.02em">${escapeXml(fullName)}</text>""")
        
        vcard.title?.let { title ->
            appendLine("""<text x="0" y="64" font-family="${theme.fontFamily}" fill="${theme.secondaryText}" font-size="18" font-weight="500">${escapeXml(title)}</text>""")
        }
        
        // Divider
        appendLine("""<line x1="0" y1="96" x2="380" y2="96" stroke="${theme.secondaryText}" stroke-width="1" stroke-opacity="0.1"/>""")
        
        // Contact Details
        appendContactDetails(vcard)
        
        appendLine("</g>")
        
        // Right Section: QR and Brand
        appendLine("""<g transform="translate(460,48)">""")
        
        // QR Code Container
        appendLine("""<g id="qr-trigger-$id" style="cursor: pointer;">""")
        appendLine("""<rect x="70" y="30" width="140" height="140" rx="8" ry="8" fill="${if (useDark) "#FFFFFF" else "#F3F4F6"}"/>""")
        appendLine("""<g transform="translate(80, 40)">$qrCodeBase64</g>""")
        
        // Scan label
        appendLine("""<text x="140" y="195" font-family="${theme.fontFamily}" fill="${theme.secondaryText}" font-size="12" font-weight="600" text-anchor="middle" letter-spacing="0.05em" text-transform="uppercase">Scan to save</text>""")
        appendLine("""</g>""")
        
        // Organization / Tagline
        val tagline = vcard.note ?: vcard.organization ?: ""
        if (tagline.isNotEmpty()) {
            appendWrappedText(tagline, x = 0, y = 280, maxWidth = 280, fontSize = 14, lineHeight = 22)
        }
        
        appendLine("</g>")
        appendLine("</g>")
        
        // Modal (Outside main card group)
        appendQRModal(largeQrCodeBase64, id)
    }

    private fun StringBuilder.appendContactDetails(vcard: VCard) {
        appendLine("""<g transform="translate(0, 136)" font-family="${theme.fontFamily}">""")
        
        var y = 0
        
        // Email
        val email = vcard.emails.firstOrNull()?.address ?: vcard.email
        email?.let {
            appendDetailItem("Email", it, y)
            y += 64
        }
        
        // Phone
        val phone = vcard.phones.firstOrNull()?.number ?: vcard.mobile
        phone?.let {
            appendDetailItem("Phone", it, y)
            y += 64
        }
        
        // Website
        vcard.website?.let {
            appendDetailItem("Website", it.removePrefix("https://").removePrefix("http://"), y)
        }
        
        appendLine("</g>")
    }

    private fun StringBuilder.appendDetailItem(label: String, value: String, y: Int) {
        appendLine("""<g transform="translate(0, $y)">""")
        appendLine("""<text x="0" y="0" fill="${theme.secondaryText}" font-size="12" font-weight="700" text-transform="uppercase" letter-spacing="0.05em">$label</text>""")
        appendLine("""<text x="0" y="24" fill="${theme.primaryText}" font-size="16" font-weight="500">${escapeXml(value)}</text>""")
        appendLine("</g>")
    }

    private fun StringBuilder.appendWrappedText(text: String, x: Int, y: Int, maxWidth: Int, fontSize: Int, lineHeight: Int) {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        val approxCharWidth = fontSize * 0.5

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (testLine.length * approxCharWidth <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)

        lines.forEachIndexed { index, line ->
            val lineY = y + (index * lineHeight)
            appendLine("""<text x="$x" y="$lineY" font-family="${theme.fontFamily}" fill="${theme.secondaryText}" font-size="$fontSize" font-weight="400">${escapeXml(line)}</text>""")
        }
    }

    private fun StringBuilder.appendQRModal(largeQrCodeBase64: String, id: String) {
        appendLine("""
            <g id="qr-modal-$id" style="display: none;">
                <rect width="900" height="540" fill="rgba(0,0,0,0.85)" id="modal-backdrop-$id" style="cursor: pointer;"/>
                <g transform="translate(250, 40)">
                    <rect width="400" height="460" rx="16" ry="16" fill="#FFFFFF"/>
                    <g transform="translate(0, 0)">$largeQrCodeBase64</g>
                    <text x="200" y="430" font-family="${theme.fontFamily}" fill="#111827" font-size="16" font-weight="600" text-anchor="middle">Scan to import contact</text>
                </g>
            </g>
        """.trimIndent())
    }

    private fun StringBuilder.appendInteractiveScript(id: String) {
        appendLine("""
            <script type="text/javascript">
            <![CDATA[
                (function() {
                    const svg = document.getElementById('id_$id');
                    const trigger = svg.getElementById('qr-trigger-$id');
                    const modal = svg.getElementById('qr-modal-$id');
                    const backdrop = svg.getElementById('modal-backdrop-$id');
                    
                    if (trigger && modal && backdrop) {
                        trigger.addEventListener('click', function() {
                            modal.style.display = 'block';
                        });
                        backdrop.addEventListener('click', function() {
                            modal.style.display = 'none';
                        });
                        document.addEventListener('keydown', function(e) {
                            if (e.key === 'Escape') modal.style.display = 'none';
                        });
                    }
                })();
            ]]>
            </script>
        """.trimIndent())
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
