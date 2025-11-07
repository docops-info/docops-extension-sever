package io.docops.docopsextensionssupport.vcard.model.renderer

import io.docops.docopsextensionssupport.vcard.model.QRCodeService
import io.docops.docopsextensionssupport.vcard.model.VCard
import io.docops.docopsextensionssupport.vcard.model.VCardConfig
import io.docops.docopsextensionssupport.vcard.model.VCardGeneratorService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreativeAgencyCardRenderer : VCardRenderer {
    override val designKey: String = "creative_agency_pro_contact_card"

    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        val theme = config.theme
        val colors = when (theme) {
            "dark" -> arrayOf("#1a1a1a", "#ffffff", "#a0aec0", "#ff6b6b", "#4ecdc4", "#45b7d1", "#ff6b6b")
            else -> arrayOf("#ffffff", "#2d3748", "#4a5568", "#ff6b6b", "#4ecdc4", "#45b7d1", "#ff6b6b")
        }

        val bgColor = colors[0]
        val primaryText = colors[1]
        val secondaryText = colors[2]
        val accentColor = colors[3]
        val shapeColor1 = colors[4]
        val shapeColor2 = colors[5]
        val shapeColor3 = colors[6]

        val id: String = Uuid.random().toHexString()

        return buildString {
            appendLine("""<svg width="350" height="200" viewBox="0 0 350 200" id="id_$id" xmlns="http://www.w3.org/2000/svg">""")
            appendLine("""  <defs>""")
            appendLine("""    <linearGradient id="creativeGrad" x1="0%" y1="0%" x2="100%" y2="100%">""")
            appendLine("""      <stop offset="0%" style="stop-color:$shapeColor1;stop-opacity:1" />""")
            appendLine("""      <stop offset="50%" style="stop-color:$shapeColor2;stop-opacity:1" />""")
            appendLine("""      <stop offset="100%" style="stop-color:$shapeColor3;stop-opacity:1" />""")
            appendLine("""    </linearGradient>""")
            appendLine("""  </defs>""")

            // Background
            appendLine("""  <rect width="350" height="200" rx="20" fill="$bgColor"/>""")

            // Geometric shapes for creative design
            appendLine("""  <polygon points="0,0 100,0 50,50" fill="url(#creativeGrad)" opacity="0.8"/>""")
            appendLine("""  <circle cx="300" cy="150" r="40" fill="$accentColor" opacity="0.2"/>""")
            appendLine("""  <rect x="280" y="20" width="30" height="30" rx="15" fill="$shapeColor2" opacity="0.3"/>""")

            // Additional decorative elements
            appendLine("""  <circle cx="320" cy="40" r="15" fill="$shapeColor3" opacity="0.25"/>""")
            appendLine("""  <polygon points="10,180 40,160 25,200" fill="$shapeColor1" opacity="0.4"/>""")

            // Main content area
            var yPosition = 80

            // Name (large, bold)
            val displayName = "${vcard.firstName} ${vcard.lastName}".trim()

            if (displayName.isNotEmpty()) {
                appendLine("""  <text x="40" y="$yPosition" font-family="'Poppins', sans-serif" font-size="22" font-weight="700" fill="$primaryText">""")
                appendLine("""    $displayName""")
                appendLine("""  </text>""")
                yPosition += 25
            }

            // Title/Role
            vcard.title?.let {
                appendLine("""  <text x="40" y="$yPosition" font-family="'Poppins', sans-serif" font-size="14" font-weight="500" fill="$secondaryText">""")
                appendLine("""    ${vcard.title}""")
                appendLine("""  </text>""")
                yPosition += 20
            }

            // Organization with accent color
            vcard.organization?.let{
                appendLine("""  <text x="40" y="$yPosition" font-family="'Poppins', sans-serif" font-size="12" font-weight="400" fill="$accentColor">""")
                appendLine("""    ${vcard.organization}""")
                appendLine("""  </text>""")
                yPosition += 25
            }

            // PRIMARY Email only
            val primaryEmail = vcard.emails.firstOrNull()?.address ?: vcard.email
            primaryEmail?.let {
                appendLine("""  <circle cx="45" cy="${yPosition - 4}" r="6" fill="$shapeColor2" opacity="0.3"/>""")
                appendLine("""  <text x="42" y="$yPosition" font-family="'Poppins', sans-serif" font-size="10" font-weight="600" fill="$accentColor">@</text>""")
                appendLine("""  <text x="60" y="$yPosition" font-family="'Poppins', sans-serif" font-size="11" fill="$secondaryText">$it</text>""")
                yPosition += 20
            }

            // PRIMARY Phone only
            val primaryPhone = vcard.phones.firstOrNull()?.number ?: vcard.mobile
            primaryPhone?.let {
                appendLine("""  <rect x="42" y="${yPosition - 8}" width="6" height="10" rx="2" fill="$shapeColor1" opacity="0.6"/>""")
                appendLine("""  <text x="60" y="$yPosition" font-family="'Poppins', sans-serif" font-size="11" fill="$secondaryText">$it</text>""")
                yPosition += 20
            }

            // Generate and add QR code
            val vCardGeneratorService = VCardGeneratorService()
            val qrCodeService = QRCodeService()
            val vCardData = vCardGeneratorService.generateVCard30(vcard)
            val qrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 85, 85)

            // QR Code with creative frame
            appendLine("""  <rect x="245" y="105" width="90" height="75" rx="8" fill="rgba(255,255,255,0.95)"/>""")
            appendLine("""  <rect x="250" y="110" width="80" height="60" rx="4" fill="white"/>""")
            appendLine("""  <image x="279" y="115" width="55" height="55" href="$qrCodeBase64"/>""")
            appendLine("""  <text x="305" y="178" font-family="'Poppins', sans-serif" font-size="8" fill="$secondaryText" text-anchor="middle">Scan me!</text>""")


            if (vcard.socialMedia.isNotEmpty()) {
                val url = vcard.socialMedia.first().url
                // Web/Link icon
                appendLine("""  <circle cx="45" cy="${yPosition - 4}" r="3" fill="none" stroke="$shapeColor3" stroke-width="2" opacity="0.7"/>""")
                appendLine("""  <circle cx="45" cy="${yPosition - 4}" r="1.5" fill="$shapeColor3" opacity="0.7"/>""")

                appendLine("""  <text x="60" y="$yPosition" font-family="'Poppins', sans-serif" font-size="11" fill="$secondaryText">""")
                appendLine("""    ${url.removePrefix("https://").removePrefix("http://").removePrefix("www.")}""")
                appendLine("""  </text>""")
                yPosition += 20
            }

            // Creative accent line at bottom
            if (theme == "light") {
                appendLine("""  <rect x="40" y="185" width="270" height="2" rx="1" fill="url(#creativeGrad)" opacity="0.6"/>""")
            } else {
                appendLine("""  <rect x="40" y="185" width="270" height="2" rx="1" fill="$accentColor" opacity="0.8"/>""")
            }

            // Additional creative elements based on theme
            if (theme == "dark") {
                // Add some glowing effects for dark theme
                appendLine("""  <circle cx="250" cy="60" r="20" fill="none" stroke="$shapeColor2" stroke-width="1" opacity="0.3"/>""")
                appendLine("""  <circle cx="250" cy="60" r="12" fill="none" stroke="$shapeColor2" stroke-width="1" opacity="0.5"/>""")
            } else {
                // Add subtle shadow effects for light theme
                appendLine("""  <ellipse cx="175" cy="195" rx="150" ry="3" fill="#000000" opacity="0.05"/>""")
            }

            // Note section if available
            vcard.note?.let {
                if (vcard.note.isNotEmpty() && vcard.note.length <= 50) {
                    val noteY = minOf(yPosition, 175)
                    appendLine("""  <text x="40" y="$noteY" font-family="'Poppins', sans-serif" font-size="9" font-style="italic" fill="$secondaryText" opacity="0.8">""")
                    appendLine("""    ${vcard.note}""")
                    appendLine("""  </text>""")
                }
            }
            appendLine("""</svg>""")
        }
    }
}