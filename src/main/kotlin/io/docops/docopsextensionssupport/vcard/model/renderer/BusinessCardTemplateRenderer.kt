package io.docops.docopsextensionssupport.vcard.model.renderer

import io.docops.docopsextensionssupport.vcard.model.QRCodeService
import io.docops.docopsextensionssupport.vcard.model.VCard
import io.docops.docopsextensionssupport.vcard.model.VCardConfig
import io.docops.docopsextensionssupport.vcard.model.VCardGeneratorService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class BusinessCardTemplateRenderer : VCardRenderer {
    override val designKey: String = "business_card_template"

    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        val theme = config.theme
        val colors = when (theme) {
            "dark" -> arrayOf("#111827", "#f9fafb", "#9ca3af", "#10b981", "#1f2937", "#374151", "#34d399")
            else -> arrayOf("#f9fafb", "#111827", "#6b7280", "#059669", "#ffffff", "#d1d5db", "#10b981")
        }

        val bgColor = colors[0]
        val primaryText = colors[1]
        val secondaryText = colors[2]
        val accentColor = colors[3]
        val templateBg = colors[4]
        val borderColor = colors[5]
        val highlightColor = colors[6]
        val id: String = Uuid.random().toHexString()

        val cardHeight = 200

        return buildString {
            appendLine("""<svg width="350" height="$cardHeight" viewBox="0 0 350 $cardHeight" id="id_$id" xmlns="http://www.w3.org/2000/svg">""")
            appendLine("""  <defs>""")
            appendLine("""    <linearGradient id="templateGrad" x1="0%" y1="0%" x2="100%" y2="100%">""")
            appendLine("""      <stop offset="0%" style="stop-color:$accentColor;stop-opacity:0.1" />""")
            appendLine("""      <stop offset="100%" style="stop-color:$highlightColor;stop-opacity:0.05" />""")
            appendLine("""    </linearGradient>""")
            appendLine("""    <pattern id="dotPattern" patternUnits="userSpaceOnUse" width="20" height="20">""")
            appendLine("""      <circle cx="2" cy="2" r="1" fill="$borderColor" opacity="0.3"/>""")
            appendLine("""    </pattern>""")
            appendLine("""    <filter id="templateShadow" x="-10%" y="-10%" width="120%" height="120%">""")
            appendLine("""      <feDropShadow dx="2" dy="2" stdDeviation="4" flood-color="#00000015"/>""")
            appendLine("""    </filter>""")
            appendLine("""  </defs>""")

            // Background with subtle pattern
            appendLine("""  <rect width="350" height="$cardHeight" rx="16" fill="$bgColor"/>""")
            appendLine("""  <rect width="350" height="$cardHeight" rx="16" fill="url(#dotPattern)" opacity="0.4"/>""")

            // Main template card
            appendLine("""  <rect x="15" y="15" width="320" height="170" rx="12" fill="$templateBg" stroke="$borderColor" stroke-width="1" filter="url(#templateShadow)"/>""")

            // Left accent panel
            appendLine("""  <rect x="15" y="15" width="80" height="170" rx="12" fill="url(#templateGrad)"/>""")
            appendLine("""  <rect x="15" y="15" width="80" height="170" rx="12" fill="$accentColor" opacity="0.08"/>""")

            // Profile section in left panel
            val initials = "${vcard.firstName.firstOrNull() ?: ""}${vcard.lastName.firstOrNull() ?: ""}".uppercase()
            appendLine("""  <rect x="25" y="25" width="60" height="60" rx="30" fill="$templateBg" stroke="$accentColor" stroke-width="2"/>""")
            appendLine("""  <text x="55" y="60" font-family="'Roboto', sans-serif" font-size="24" font-weight="700" fill="$accentColor" text-anchor="middle">$initials</text>""")

            // Name in left panel
            val firstName = vcard.firstName.take(8) // Truncate if too long for left panel
            val lastName = vcard.lastName.take(8)
            appendLine("""  <text x="55" y="105" font-family="'Roboto', sans-serif" font-size="12" font-weight="700" fill="$primaryText" text-anchor="middle">$firstName</text>""")
            appendLine("""  <text x="55" y="120" font-family="'Roboto', sans-serif" font-size="12" font-weight="700" fill="$primaryText" text-anchor="middle">$lastName</text>""")

            // Title in left panel
            vcard.title?.let {
                val truncatedTitle = if (it.length > 12) "${it.take(10)}..." else it
                appendLine("""  <text x="55" y="140" font-family="'Roboto', sans-serif" font-size="9" font-weight="400" fill="$secondaryText" text-anchor="middle">$truncatedTitle</text>""")
            }

            // Decorative elements in left panel
            appendLine("""  <circle cx="30" cy="160" r="3" fill="$highlightColor" opacity="0.6"/>""")
            appendLine("""  <circle cx="55" cy="165" r="2" fill="$accentColor" opacity="0.4"/>""")
            appendLine("""  <circle cx="75" cy="155" r="4" fill="$highlightColor" opacity="0.3"/>""")

            // Right content area
            var yPos = 45

            // Organization header
            vcard.organization?.let {
                appendLine("""  <rect x="110" y="30" width="210" height="25" rx="4" fill="$accentColor" opacity="0.1"/>""")
                appendLine("""  <text x="120" y="47" font-family="'Roboto', sans-serif" font-size="14" font-weight="600" fill="$primaryText">$it</text>""")
                yPos = 75
            }

            // Department/Role if available
            vcard.department?.let {
                appendLine("""  <text x="120" y="$yPos" font-family="'Roboto', sans-serif" font-size="11" font-weight="500" fill="$accentColor">$it</text>""")
                yPos += 25
            } ?: run {
                yPos += 15
            }

            // Contact information section
            appendLine("""  <rect x="110" y="$yPos" width="210" height="2" rx="1" fill="$borderColor"/>""")
            yPos += 20

            // PRIMARY Email only with template-style icon
            val primaryEmail = vcard.emails.firstOrNull()?.address ?: vcard.email
            primaryEmail?.let {
                appendLine("""  <rect x="120" y="${yPos - 8}" width="16" height="12" rx="2" fill="none" stroke="$accentColor" stroke-width="1"/>""")
                appendLine("""  <path d="M120 ${yPos - 6} L128 ${yPos - 2} L136 ${yPos - 6}" fill="none" stroke="$accentColor" stroke-width="1"/>""")
                appendLine("""  <text x="145" y="$yPos" font-family="'Roboto', sans-serif" font-size="10" font-weight="400" fill="$secondaryText">EMAIL</text>""")
                appendLine("""  <text x="145" y="${yPos + 12}" font-family="'Roboto', sans-serif" font-size="11" font-weight="500" fill="$primaryText">$it</text>""")
                yPos += 30
            }

            // PRIMARY Phone only with template-style icon
            val primaryPhone = vcard.phones.firstOrNull()?.number ?: vcard.mobile
            primaryPhone?.let {
                val label = vcard.phones.firstOrNull()?.type?.let { type ->
                    when (type) {
                        io.docops.docopsextensionssupport.vcard.model.PhoneType.CELL -> "MOBILE"
                        io.docops.docopsextensionssupport.vcard.model.PhoneType.WORK -> "WORK"
                        io.docops.docopsextensionssupport.vcard.model.PhoneType.HOME -> "HOME"
                        else -> "PHONE"
                    }
                } ?: "PHONE"
                appendLine("""  <rect x="122" y="${yPos - 10}" width="12" height="16" rx="2" fill="none" stroke="$accentColor" stroke-width="1"/>""")
                appendLine("""  <rect x="124" y="${yPos - 8}" width="8" height="1" rx="0.5" fill="$accentColor"/>""")
                appendLine("""  <rect x="125" y="${yPos - 2}" width="6" height="1" rx="0.5" fill="$accentColor"/>""")
                appendLine("""  <text x="145" y="$yPos" font-family="'Roboto', sans-serif" font-size="10" font-weight="400" fill="$secondaryText">$label</text>""")
                appendLine("""  <text x="145" y="${yPos + 12}" font-family="'Roboto', sans-serif" font-size="11" font-weight="500" fill="$primaryText">$it</text>""")
                yPos += 30
            }

            // Generate and add QR code
            val vCardGeneratorService = VCardGeneratorService()
            val qrCodeService = QRCodeService()
            val vCardData = vCardGeneratorService.generateVCard30(vcard)
            val qrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 90, 90)

            // QR Code section
            // QR Code in bottom right corner with clickable group
            appendLine("""  <g id="qr-trigger-$id" style="cursor: pointer;">""")
            appendLine("""    <rect x="255" y="110" width="70" height="65" rx="6" fill="white" stroke="$borderColor" stroke-width="1"/>""")
            appendLine("""    <image x="263" y="115" width="55" height="55" href="$qrCodeBase64"/>""")
            appendLine("""    <text x="267" y="194" font-family="'Roboto', sans-serif" font-size="8" fill="$secondaryText" text-anchor="middle">Scan for full contact</text>""")
            appendLine("""  </g>""")
            //appendLine("""  <rect x="235" y="110" width="80" height="65" rx="6" fill="white" stroke="$borderColor" stroke-width="1"/>""")
            //appendLine("""  <image x="247" y="115" width="55" height="55" href="$qrCodeBase64"/>""")
            //appendLine("""  <text x="275" y="180" font-family="'Roboto', sans-serif" font-size="7" fill="$secondaryText" text-anchor="middle">Full Contact</text>""")

            // Website/Social with template-style icon
            val webUrl = vcard.website ?: vcard.socialMedia.firstOrNull()?.url
            webUrl?.let {
                appendLine("""  <circle cx="128" cy="${yPos - 4}" r="8" fill="none" stroke="$accentColor" stroke-width="1"/>""")
                appendLine("""  <circle cx="128" cy="${yPos - 4}" r="4" fill="none" stroke="$accentColor" stroke-width="1"/>""")
                appendLine("""  <circle cx="128" cy="${yPos - 4}" r="1.5" fill="$accentColor"/>""")
                appendLine("""  <text x="145" y="$yPos" font-family="'Roboto', sans-serif" font-size="10" font-weight="400" fill="$secondaryText">WEBSITE</text>""")
                val displayUrl = it.removePrefix("https://").removePrefix("http://").removePrefix("www.")
                val truncatedUrl = if (displayUrl.length > 25) "${displayUrl.take(22)}..." else displayUrl
                appendLine("""  <text x="145" y="${yPos + 12}" font-family="'Roboto', sans-serif" font-size="11" font-weight="500" fill="$primaryText">$truncatedUrl</text>""")
                yPos += 30
            }

            // Bottom template branding/note
            if (yPos < 160) {
                vcard.note?.let {
                    val noteText = if (it.length > 40) "${it.take(37)}..." else it
                    appendLine("""  <text x="120" y="170" font-family="'Roboto', sans-serif" font-size="9" font-style="italic" fill="$secondaryText" opacity="0.7">$noteText</text>""")
                }
            }

            // Corner template design elements
            appendLine("""  <rect x="310" y="25" width="15" height="3" rx="1.5" fill="$highlightColor" opacity="0.6"/>""")
            appendLine("""  <rect x="315" y="32" width="10" height="2" rx="1" fill="$accentColor" opacity="0.4"/>""")
            appendLine("""  <rect x="320" y="37" width="5" height="2" rx="1" fill="$highlightColor" opacity="0.8"/>""")

            appendLine("""
  <!-- QR Code Modal -->
  <g id="qr-modal-$id" style="display: none;">
      <rect width="350" height="220" fill="rgba(0,0,0,0.9)" id="modal-bg-$id" style="cursor: pointer;"/>
      <g transform="translate(75,5)">
          <rect width="200" height="185" rx="8" fill="white"/>
          <image x="20" y="5" width="160" height="160" href="${qrCodeService.generateQRCodeBase64(vCardData, 320, 320)}"/>
          <text x="100" y="180" font-family="'Inter', sans-serif" font-size="9" fill="#64748b" text-anchor="middle">(click to close)</text>
      </g>
  </g>

  <script type="text/javascript">
  <![CDATA[
      (function() {
          var svg = document.getElementById('id_$id');
          if (!svg) {
              console.error('SVG not found: id_$id');
              return;
          }
          
          var qrTrigger = svg.getElementById('qr-trigger-$id');
          var modal = svg.getElementById('qr-modal-$id');
          var modalBg = svg.getElementById('modal-bg-$id');
          
          if (!qrTrigger) console.error('QR trigger not found: qr-trigger-$id');
          if (!modal) console.error('Modal not found: qr-modal-$id');
          if (!modalBg) console.error('Modal background not found: modal-bg-$id');
          
          if (qrTrigger && modal && modalBg) {
              qrTrigger.addEventListener('click', function(e) {
                  e.stopPropagation();
                  modal.style.display = 'block';
              });
              
              modalBg.addEventListener('click', function() {
                  modal.style.display = 'none';
              });
              
              document.addEventListener('keydown', function(e) {
                  if (e.key === 'Escape' && modal.style.display === 'block') {
                      modal.style.display = 'none';
                  }
              });
          }
      })();
  ]]>
  </script>
""".trimIndent())
            appendLine("""</svg>""")
        }
    }
}