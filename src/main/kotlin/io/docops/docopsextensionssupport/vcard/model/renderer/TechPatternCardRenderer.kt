package io.docops.docopsextensionssupport.vcard.model.renderer

import io.docops.docopsextensionssupport.vcard.model.PhoneType
import io.docops.docopsextensionssupport.vcard.model.QRCodeService
import io.docops.docopsextensionssupport.vcard.model.VCard
import io.docops.docopsextensionssupport.vcard.model.VCardConfig
import io.docops.docopsextensionssupport.vcard.model.VCardGeneratorService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TechPatternCardRenderer : VCardRenderer {
    override val designKey: String = "tech_pattern_background"

    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        val theme = config.theme
        val (bgColor, textColor, accentColor, gridColor) = when (theme) {
            "dark" -> arrayOf("#0a0e27", "#ffffff", "#00ff88", "#00ff88")
            else -> arrayOf("#f8fafc", "#1a202c", "#2563eb", "#2563eb")
        }
        val id: String = Uuid.random().toHexString()
        return buildString {
            appendLine("""<svg width="350" height="200" viewBox="0 0 350 200" id="id_$id" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMidYMid meet">""")
            appendLine("""  <defs>""")
            appendLine("""    <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">""")
            appendLine("""      <path d="M 20 0 L 0 0 0 20" fill="none" stroke="$gridColor" stroke-width="0.5" opacity="0.1"/>""")
            appendLine("""    </pattern>""")
            appendLine("""  </defs>""")

            // Background
            appendLine("""  <rect width="350" height="200" rx="12" fill="$bgColor"/>""")
            appendLine("""  <rect width="350" height="200" rx="12" fill="url(#grid)"/>""")

            // Accent elements
            appendLine("""  <rect x="0" y="176" width="350" height="4" fill="$accentColor"/>""")
            appendLine("""  <rect x="20" y="36" width="4" height="140" fill="$accentColor" opacity="0.3"/>""")

            // Name
            appendLine("""  <text x="40" y="60" font-family="'JetBrains Mono', monospace" font-size="20" font-weight="600" fill="$textColor">""")
            appendLine("""    ${vcard.firstName}  ${vcard.lastName}""")
            appendLine("""  </text>""")

            // Title with code-like styling
            vcard.title?.let {
                appendLine("""  <text x="40" y="80" font-family="'JetBrains Mono', monospace" font-size="12" fill="$accentColor">""")
                appendLine("""    &lt;${vcard.title}/&gt;""")
                appendLine("""  </text>""")
            }

            // Company
            vcard.organization?.let {
                appendLine("""  <text x="40" y="100" font-family="'JetBrains Mono', monospace" font-size="11" fill="#64748b">""")
                appendLine("""    ${vcard.organization}""")
                appendLine("""  </text>""")
            }

            var yPos = 125

            // PRIMARY Email with terminal-style
            val primaryEmail = vcard.emails.firstOrNull()?.address ?: vcard.email
            primaryEmail?.let {
                appendLine("""  <text x="40" y="$yPos" font-family="'JetBrains Mono', monospace" font-size="10" fill="$accentColor">""")
                appendLine("""    $ contact --email""")
                appendLine("""  </text>""")
                appendLine("""  <text x="40" y="${yPos + 15}" font-family="'JetBrains Mono', monospace" font-size="10" fill="$textColor">""")
                appendLine("""    $it""")
                appendLine("""  </text>""")
                yPos += 35
            }

            // PRIMARY Phone with terminal-style
            val primaryPhone = vcard.phones.firstOrNull()
            val phoneFlag = primaryPhone?.let { phone ->
                when (phone.type) {
                    io.docops.docopsextensionssupport.vcard.model.PhoneType.CELL -> "--phone-cell"
                    io.docops.docopsextensionssupport.vcard.model.PhoneType.WORK -> "--phone-work"
                    else -> "--phone"
                }
            } ?: "--phone"

            val phoneNumber = primaryPhone?.number ?: vcard.mobile
            phoneNumber?.let {
                appendLine("""  <text x="40" y="$yPos" font-family="'JetBrains Mono', monospace" font-size="10" fill="$accentColor">""")
                appendLine("""    $ contact $phoneFlag""")
                appendLine("""  </text>""")
                appendLine("""  <text x="40" y="${yPos + 15}" font-family="'JetBrains Mono', monospace" font-size="10" fill="$textColor">""")
                appendLine("""    $it""")
                appendLine("""  </text>""")
            }

            // Generate and add QR code
            val vCardGeneratorService = VCardGeneratorService()
            val qrCodeService = QRCodeService()
            val vCardData = vCardGeneratorService.generateVCard30(vcard)
            val qrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 80, 80)

            // QR Code terminal-style
            // QR Code in bottom right corner with clickable group
            appendLine("""  <g id="qr-trigger-$id" style="cursor: pointer;">""")
            appendLine("""    <rect x="250" y="84" width="85" height="75" rx="4" fill="rgba(255,255,255,0.1)" stroke="$accentColor" stroke-width="1"/>""")
            appendLine("""  <rect x="263" y="95" width="60" height="60" rx="2" fill="white"/>""")
            appendLine("""    <image x="268" y="100" width="50" height="50" href="$qrCodeBase64"/>""")
            appendLine("""    <text x="288" y="187" font-family="'JetBrains Mono', monospace" font-size="7" fill="$accentColor" text-anchor="middle">$ scan</text>""")
            appendLine("""  </g>""")

           /* appendLine("""  <rect x="250" y="84" width="85" height="75" rx="4" fill="rgba(255,255,255,0.1)" stroke="$accentColor" stroke-width="1"/>""")
            appendLine("""  <rect x="263" y="95" width="60" height="60" rx="2" fill="white"/>""")
            appendLine("""  <image x="268" y="100" width="50" height="50" href="$qrCodeBase64"/>""")
            appendLine("""  <text x="288" y="187" font-family="'JetBrains Mono', monospace" font-size="7" fill="$accentColor" text-anchor="middle">$ scan</text>""")
*/
            // Add modal BEFORE closing SVG tag
            appendLine("""
  <!-- QR Code Modal -->
  <g id="qr-modal-$id" style="display: none;">
      <rect width="350" height="220" fill="rgba(0,0,0,0.9)" id="modal-bg-$id" style="cursor: pointer;"/>
      <g transform="translate(75,5)">
          <rect width="200" height="185" rx="8" fill="white"/>
          <image x="20" y="5" width="160" height="160" href="${qrCodeService.generateQRCodeBase64(vCardData, 320, 320)}"/>
          <text x="100" y="180" font-family="'Roboto', sans-serif" font-size="9" fill="#64748b" text-anchor="middle">(click to close)</text>
      </g>
  </g>

  <script type="text/javascript">
  <![CDATA[
      (function() {
          var svg = document.getElementById('id_$id');
          if (!svg) return;
          
          var qrTrigger = svg.getElementById('qr-trigger-$id');
          var modal = svg.getElementById('qr-modal-$id');
          var modalBg = svg.getElementById('modal-bg-$id');
          
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