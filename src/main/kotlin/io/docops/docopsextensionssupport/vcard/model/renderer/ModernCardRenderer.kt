package io.docops.docopsextensionssupport.vcard.model.renderer

import io.docops.docopsextensionssupport.vcard.model.QRCodeService
import io.docops.docopsextensionssupport.vcard.model.VCard
import io.docops.docopsextensionssupport.vcard.model.VCardConfig
import io.docops.docopsextensionssupport.vcard.model.VCardGeneratorService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ModernCardRenderer(
    private val includeQR: Boolean = true
) : VCardRenderer {
    override val designKey: String = "modern_card"

    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        val vCardGeneratorService = VCardGeneratorService()
        val qrCodeService = QRCodeService()
        val qrCodeBase64 = if (includeQR) {
            val vCardData = vCardGeneratorService.generateVCard30(vcard)
            qrCodeService.generateQRCodeBase64(vCardData, 160, 160)
        } else null

        val id: String = Uuid.random().toHexString()

        return buildString {
            appendLine("""
                <svg xmlns="http://www.w3.org/2000/svg" id="id_$id" xmlns:xlink="http://www.w3.org/1999/xlink" 
                     width="350" height="200" viewBox="0 0 900 540">
            """.trimIndent())

            appendDefs()
            appendBackground()
            appendCard(vcard, qrCodeBase64)

            appendLine("</svg>")
        }
    }

    private fun StringBuilder.appendDefs() {
        appendLine("""
            <defs>
                <linearGradient id="grad" x1="0" x2="1">
                    <stop offset="0" stop-color="#0f172a"/>
                    <stop offset="1" stop-color="#0b3d91"/>
                </linearGradient>
                <filter id="softShadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="10" stdDeviation="20" flood-color="#071036" flood-opacity="0.45"/>
                </filter>
                <clipPath id="rounder">
                    <rect x="0" y="0" width="900" height="540" rx="18" ry="18"/>
                </clipPath>
            </defs>
        """.trimIndent())
    }

    private fun StringBuilder.appendBackground() {
        appendLine("""<rect width="100%" height="100%" fill="#f4f6fb" rx="18" ry="18"/>""")
    }

    private fun StringBuilder.appendCard(vCard: VCard, qrCodeBase64: String?) {
        appendLine("""<g transform="translate(60,60)" filter="url(#softShadow)" clip-path="url(#rounder)">""")
        appendLine("""<rect width="780" height="420" rx="14" ry="14" fill="url(#grad)"/>""")
        appendLine("""<rect x="28" y="28" width="380" height="364" rx="12" ry="12" fill="rgba(255,255,255,0.06)"/>""")

        appendLeftColumn(vCard)
        appendRightColumn(vCard, qrCodeBase64)

        appendLine("</g>")
    }

    private fun StringBuilder.appendLeftColumn(vCard: VCard) {
        appendLine("""<g transform="translate(56,56)">""")

        // Photo placeholder or actual photo
        appendPhotoSection(vCard)

        // Name and title
        val fullName = "${vCard.firstName} ${vCard.lastName}"
        appendLine("""<text x="180" y="64" font-family="Inter, Arial, sans-serif" fill="#ffffff" font-size="32" font-weight="700">${escapeXml(fullName)}</text>""")

        vCard.title?.let { title ->
            appendLine("""<text x="180" y="90" font-family="Inter, Arial, sans-serif" fill="rgba(255,255,255,0.85)" font-size="16" font-weight="500">${escapeXml(title)}</text>""")
        }

        // Divider
        appendLine("""<line x1="0" y1="140" x2="350" y2="140" stroke="rgba(255,255,255,0.06)" stroke-width="1"/>""")

        // Contact information
        appendContactInfo(vCard)

        appendLine("</g>")
    }

    private fun StringBuilder.appendPhotoSection(vCard: VCard) {
        if (vCard.photoBase64 != null) {
            appendLine("""
                <defs>
                    <pattern id="photo" patternUnits="userSpaceOnUse" width="160" height="160">
                        <image href="data:image/jpeg;base64,${vCard.photoBase64}" width="160" height="160"/>
                    </pattern>
                </defs>
            """.trimIndent())
        } else if (vCard.photoUrl != null) {
            appendLine("""
                <defs>
                    <pattern id="photo" patternUnits="userSpaceOnUse" width="160" height="160">
                        <image href="${vCard.photoUrl}" width="160" height="160"/>
                    </pattern>
                </defs>
            """.trimIndent())
        } else {
            // Default avatar pattern
            appendLine("""
                <defs>
                    <pattern id="photo" patternUnits="userSpaceOnUse" width="160" height="160">
                        <rect width="160" height="160" fill="rgba(255,255,255,0.1)"/>
                        <circle cx="80" cy="65" r="25" fill="rgba(255,255,255,0.3)"/>
                        <path d="M45 120 Q80 100 115 120 L115 160 L45 160 Z" fill="rgba(255,255,255,0.3)"/>
                    </pattern>
                </defs>
            """.trimIndent())
        }

        appendLine("""<circle cx="80" cy="80" r="80" fill="url(#photo)" stroke="rgba(255,255,255,0.28)" stroke-width="3"/>""")
    }

    private fun StringBuilder.appendContactInfo(vCard: VCard) {
        appendLine("""<g font-family="Inter, Arial, sans-serif" fill="rgba(255,255,255,0.9)" font-size="15" transform="translate(0,160)">""")

        var yOffset = 0

        // Phone
        vCard.phone?.let { phone ->
            appendLine("""<g transform="translate(0,$yOffset)">""")
            appendLine("""<text x="0" y="0" dy="0.35em" font-weight="600">Phone</text>""")
            appendLine("""<text x="0" y="20" fill="rgba(255,255,255,0.85)" font-weight="400">${escapeXml(phone)}</text>""")
            appendLine("</g>")
            yOffset += 50
        }

        // Email
        vCard.email?.let { email ->
            appendLine("""<g transform="translate(0,$yOffset)">""")
            appendLine("""<text x="0" y="0" dy="0.35em" font-weight="600">Email</text>""")
            appendLine("""<a xlink:href="mailto:$email">""")
            appendLine("""<text x="0" y="20" fill="rgba(255,255,255,0.85)" font-weight="400">${escapeXml(email)}</text>""")
            appendLine("</a>")
            appendLine("</g>")
            yOffset += 50
        }

        // Website
        vCard.website?.let { website ->
            appendLine("""<g transform="translate(0,$yOffset)">""")
            appendLine("""<text x="0" y="0" dy="0.35em" font-weight="600">Website</text>""")
            appendLine("""<a xlink:href="$website">""")
            appendLine("""<text x="0" y="20" fill="rgba(255,255,255,0.85)" font-weight="400">${escapeXml(website.removePrefix("https://").removePrefix("http://"))}</text>""")
            appendLine("</a>")
            appendLine("</g>")
        }

        appendLine("</g>")
    }

    private fun StringBuilder.appendRightColumn(vCard: VCard, qrCodeBase64: String?) {
        appendLine("""<g transform="translate(450,56)">""")

        // QR Code section
        if (qrCodeBase64 != null) {
            appendLine("""<rect x="0" y="0" width="160" height="160" rx="8" ry="8" fill="#ffffff"/>""")
            appendLine("""<image x="0" y="0" width="160" height="160" href="$qrCodeBase64"/>""")
            appendLine("""<text x="170" y="20" font-family="Inter, Arial, sans-serif" fill="#ffffff" font-size="12" font-weight="600">Scan to save</text>""")
        }

        // Social media section
        if (vCard.socialMedia.isNotEmpty()) {
            appendSocialMediaSection(vCard)
        }

        // Organization note or tagline
        val tagline = vCard.note ?: vCard.organization?.let { "$it - ${vCard.department ?: "Professional Services"}" } ?: "Designing simple products for complex problems"
        appendWrappedText(tagline, x = 0, y = 300, maxWidth = 280, fontSize = 14, lineHeight = 20)

        appendLine("</g>")
    }

    private fun StringBuilder.appendWrappedText(
        text: String,
        x: Int,
        y: Int,
        maxWidth: Int,
        fontSize: Int,
        lineHeight: Int
    ) {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        // Approximate character width (adjust based on font)
        val approxCharWidth = fontSize * 0.5
        val maxCharsPerLine = (maxWidth / approxCharWidth).toInt()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (testLine.length * approxCharWidth <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        // Render each line
        lines.forEachIndexed { index, line ->
            val lineY = y + (index * lineHeight)
            appendLine(
                """<text x="$x" y="$lineY" font-family="Inter, Arial, sans-serif" fill="rgba(255,255,255,0.75)" font-size="$fontSize" font-weight="500">${
                    escapeXml(
                        line
                    )
                }</text>"""
            )
        }
    }
    private fun StringBuilder.appendSocialMediaSection(vCard: VCard) {
        appendLine("""<g transform="translate(0,190)">""")
        appendLine("""<rect x="0" y="0" width="260" height="80" rx="10" ry="10" fill="rgba(255,255,255,0.06)"/>""")
        appendLine("""<g transform="translate(16,12)" fill="#ffffff" font-family="Inter, Arial, sans-serif" font-size="12">""")

        var yOffset = 0
        vCard.socialMedia.take(2).forEach { social ->
            val color = when (social.platform.lowercase()) {
                "twitter", "x" -> "#1da1f2"
                "linkedin" -> "#0a66c2"
                "github" -> "#333333"
                "instagram" -> "#E4405F"
                else -> "#666666"
            }

            appendLine("""<g transform="translate(0,$yOffset)">""")
            if (social.platform.lowercase() == "linkedin") {
                appendLine("""<rect x="0" y="0" width="20" height="20" rx="3" fill="$color"/>""")
            } else {
                appendLine("""<circle cx="10" cy="10" r="10" fill="$color"/>""")
            }
            val displayText = social.handle ?: social.url.substringAfterLast("/")
            appendLine("""<a xlink:href="${social.url}">""")
            appendLine("""<text x="28" y="14" fill="#ffffff">${escapeXml(displayText)}</text>""")
            appendLine("</a>")
            appendLine("</g>")
            yOffset += 30
        }

        appendLine("</g>")
        appendLine("</g>")
    }



    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}