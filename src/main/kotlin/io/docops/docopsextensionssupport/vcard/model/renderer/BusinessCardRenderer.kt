package io.docops.docopsextensionssupport.vcard.model.renderer

import io.docops.docopsextensionssupport.vcard.model.QRCodeService
import io.docops.docopsextensionssupport.vcard.model.VCard
import io.docops.docopsextensionssupport.vcard.model.VCardConfig
import io.docops.docopsextensionssupport.vcard.model.VCardGeneratorService
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

        // Generate QR code for full vCard
        val vCardGeneratorService = VCardGeneratorService()
        val qrCodeService = QRCodeService()
        val vCardData = vCardGeneratorService.generateVCard30(vcard)
        val qrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 120, 120)

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

            // Contact details - PRIMARY ONLY
            var yPos = 125

            // Display FIRST/PRIMARY email only
            val primaryEmail = vcard.emails.firstOrNull()?.address ?: vcard.email
            primaryEmail?.let {
                appendLine("""  <circle cx="40" cy="${yPos - 5}" r="2" fill="$accentColor"/>""")
                appendLine("""  <text x="55" y="$yPos" font-family="'Roboto', sans-serif" font-size="11" fill="$textColor">""")
                appendLine("""    $it""")
                appendLine("""  </text>""")
                yPos += 20
            }

            // Display FIRST/PRIMARY phone only
            val primaryPhone = vcard.phones.firstOrNull()?.number ?: vcard.mobile
            primaryPhone?.let {
                appendLine("""  <circle cx="40" cy="${yPos - 5}" r="2" fill="$accentColor"/>""")
                appendLine("""  <text x="55" y="$yPos" font-family="'Roboto', sans-serif" font-size="11" fill="$textColor">""")
                appendLine("""    $it""")
                appendLine("""  </text>""")
            }

            // QR Code in bottom right with ID for clickability
            appendLine("""  <g id="qr-trigger-$id" style="cursor: pointer;">""")
            appendLine("""    <rect x="240" y="110" width="90" height="70" rx="6" fill="white"/>""")
            appendLine("""    <image x="250" y="115" width="60" height="60" href="$qrCodeBase64"/>""")
            appendLine("""    <text x="280" y="184" font-family="'Inter', sans-serif" font-size="7" fill="$secondaryColor" text-anchor="middle">Scan for all info</text>""")
            appendLine("""  </g>""")
// Add modal BEFORE closing the SVG
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