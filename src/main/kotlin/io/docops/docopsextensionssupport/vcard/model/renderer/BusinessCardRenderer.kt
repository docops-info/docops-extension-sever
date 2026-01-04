package io.docops.docopsextensionssupport.vcard.model.renderer

import io.docops.docopsextensionssupport.vcard.model.QRCodeService
import io.docops.docopsextensionssupport.vcard.model.VCard
import io.docops.docopsextensionssupport.vcard.model.VCardConfig
import io.docops.docopsextensionssupport.vcard.model.VCardGeneratorService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class BusinessCardRenderer(val useDark: Boolean) : VCardRenderer {
    override val designKey: String = "business_card_design"


    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        val theme = if (useDark) "dark" else "light"
        val (bgColor, textColor, accentColor, secondaryColor, metaColor) = when (theme) {
            "dark" -> arrayOf("#0F0F0F", "#FFFFFF", "#00d4aa", "#CBD5E1", "#262626")
            else -> arrayOf("#F8F8F8", "#1A1A1A", "#667eea", "#475569", "#E5E7EB")
        }

        val vCardGeneratorService = VCardGeneratorService()
        val qrCodeService = QRCodeService()
        val vCardData = vCardGeneratorService.generateVCard30(vcard)
        val qrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 120, 120)

        val id: String = Uuid.random().toHexString()
        return buildString {
            appendLine("""<svg width="350" height="200" viewBox="0 0 350 200" id="id_$id" xmlns="http://www.w3.org/2000/svg">""")

            appendLine("""
                <defs>
                    <style>
                        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
                        .reveal { animation: fadeIn 0.8s ease-out both; }
                        /* Removed fill from CSS to handle it directly on elements for better reliability */
                        #id_$id .name-txt { font-family: 'Georgia', serif; font-weight: bold; font-size: 22px; }
                        #id_$id .title-txt { font-family: 'Courier New', monospace; font-size: 10px; letter-spacing: 0.15em; text-transform: uppercase; }
                        #id_$id .contact-txt { font-family: sans-serif; font-size: 10px; }
                        #id_$id .qr-lbl { font-family: 'Courier New', monospace; font-size: 7px; text-transform: uppercase; letter-spacing: 0.1em; }
                    </style>
                </defs>
            """.trimIndent())

            // Background & Top Accent
            appendLine("""  <rect width="350" height="200" rx="4" fill="$bgColor"/>""")
            appendLine("""  <rect width="350" height="2" fill="$accentColor" opacity="0.8"/>""")

            // Name & Title - Explicitly setting fill here
            appendLine("""  <text x="30" y="55" fill="$textColor" class="name-txt reveal" style="animation-delay: 0.1s;">${vcard.firstName} ${vcard.lastName}</text>""")
            vcard.title?.let {
                appendLine("""  <text x="30" y="75" fill="$accentColor" class="title-txt reveal" style="animation-delay: 0.2s;">$it</text>""")
            }

            // Decorative Splitter
            appendLine("""  <rect x="30" y="95" width="120" height="1" fill="$metaColor" class="reveal" style="animation-delay: 0.3s;"/>""")

            // Contact details
            var yPos = 120
            val primaryEmail = vcard.emails.firstOrNull()?.address ?: vcard.email
            primaryEmail?.let {
                appendLine("""  <text x="30" y="$yPos" fill="$secondaryColor" class="contact-txt reveal" style="animation-delay: 0.4s;">$it</text>""")
                yPos += 18
            }
            val primaryPhone = vcard.phones.firstOrNull()?.number ?: vcard.mobile
            primaryPhone?.let {
                appendLine("""  <text x="30" y="$yPos" fill="$secondaryColor" class="contact-txt reveal" style="animation-delay: 0.5s;">$it</text>""")
            }

            appendLine("""  <g id="qr-trigger-$id" class="reveal" style="cursor: pointer; animation-delay: 0.6s;">""")
            // Fix: In dark mode, we need a light background for the QR code to be visible/scannable
            val qrFrameFill = if (theme == "dark") "#FFFFFF" else metaColor
            val qrOpacity = if (theme == "dark") "0.95" else "1.0"
            appendLine("""    <rect x="250" y="45" width="70" height="70" rx="2" fill="$qrFrameFill" opacity="$qrOpacity"/>""")
            appendLine("""    <image x="255" y="50" width="60" height="60" href="$qrCodeBase64"/>""")
            appendLine("""    <text x="285" y="130" text-anchor="middle" class="qr-lbl" fill="$secondaryColor">vCard Access</text>""")
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