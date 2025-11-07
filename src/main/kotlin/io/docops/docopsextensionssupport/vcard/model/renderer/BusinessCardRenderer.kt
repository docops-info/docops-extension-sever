package io.docops.docopsextensionssupport.vcard.model.renderer

import io.docops.docopsextensionssupport.vcard.model.VCard
import io.docops.docopsextensionssupport.vcard.model.VCardConfig
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class BusinessCardRenderer : VCardRenderer {
    override val designKey: String = "business_card_design"

    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        val theme = config.theme
        val (bgColor, textColor, accentColor, secondaryColor) = when (theme) {
            "dark" -> arrayOf("#1a1a1a", "#ffffff", "#00d4aa", "#a0a0a0")
            else -> arrayOf("#ffffff", "#1a202c", "#667eea", "#4a5568")
        }

        val id: String = Uuid.random().toHexString()
        return buildString {
            appendLine("""<svg width="350" height="200" viewBox="0 0 350 200" id="id_$id" xmlns="http://www.w3.org/2000/svg">""")

            // Background
            appendLine("""  <rect width="350" height="200" rx="16" fill="$bgColor"/>""")

            // Accent bar
            appendLine("""  <rect x="0" y="0" width="6" height="200" rx="3" fill="$accentColor"/>""")

            // Name
            appendLine("""  <text x="30" y="50" font-family="'Roboto', sans-serif" font-size="22" font-weight="600" fill="$textColor">""")
            appendLine("""    ${vcard.firstName}  ${vcard.lastName} """)
            appendLine("""  </text>""")

            // Title
            vcard.title?.let {
                appendLine("""  <text x="30" y="75" font-family="'Roboto', sans-serif" font-size="14" font-weight="400" fill="$secondaryColor">""")
                appendLine("""    ${vcard.title}""")
                appendLine("""  </text>""")
            }

            // Company
            vcard.organization?.let {
                appendLine("""  <text x="30" y="95" font-family="'Roboto', sans-serif" font-size="12" font-weight="500" fill="$accentColor">""")
                appendLine("""    ${vcard.organization}""")
                appendLine("""  </text>""")
            }

            // Contact details
            var yPos = 125
            vcard.email?.let{
                appendLine("""  <circle cx="40" cy="${yPos - 5}" r="2" fill="$accentColor"/>""")
                appendLine("""  <text x="55" y="$yPos" font-family="'Roboto', sans-serif" font-size="11" fill="$textColor">""")
                appendLine("""    ${vcard.email}""")
                appendLine("""  </text>""")
                yPos += 20
            }

            vcard.mobile?.let {
                appendLine("""  <circle cx="40" cy="${yPos - 5}" r="2" fill="$accentColor"/>""")
                appendLine("""  <text x="55" y="$yPos" font-family="'Roboto', sans-serif" font-size="11" fill="$textColor">""")
                appendLine("""    ${vcard.mobile}""")
                appendLine("""  </text>""")
            }

            appendLine("""</svg>""")
        }
    }
}