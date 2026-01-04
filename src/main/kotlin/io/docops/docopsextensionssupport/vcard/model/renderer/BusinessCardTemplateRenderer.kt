package io.docops.docopsextensionssupport.vcard.model.renderer

import io.docops.docopsextensionssupport.vcard.model.QRCodeService
import io.docops.docopsextensionssupport.vcard.model.VCard
import io.docops.docopsextensionssupport.vcard.model.VCardConfig
import io.docops.docopsextensionssupport.vcard.model.VCardGeneratorService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class BusinessCardTemplateRenderer(val useDark: Boolean) : VCardRenderer {
    override val designKey: String = "business_card_template"

    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        val theme = if(useDark) "dark" else "light"
        val (bgColor, textColor, accentColor, secondaryColor, metaColor) = when (theme) {
            "dark" -> arrayOf("#0A0A0A", "#FFFFFF", "#10b981", "#94a3b8", "#1A1A1A")
            else -> arrayOf("#F8FAFC", "#0F172A", "#059669", "#475569", "#E2E8F0")
        }

        val id: String = Uuid.random().toHexString()
        val vCardGeneratorService = VCardGeneratorService()
        val qrCodeService = QRCodeService()
        val vCardData = vCardGeneratorService.generateVCard30(vcard)
        val qrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 85, 85)

        return buildString {
            appendLine("""<svg width="350" height="200" viewBox="0 0 350 200" id="id_$id" xmlns="http://www.w3.org/2000/svg">""")
            appendLine("""
                <defs>
                    <style>
                        #id_$id @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
                        #id_$id .reveal { animation: fadeIn 0.8s ease-out both; }
                        #id_$id .name-serif { font-family: 'Georgia', serif; font-size: 24px; font-weight: bold; }
                        #id_$id .mono-tag { font-family: 'Courier New', monospace; font-size: 9px; letter-spacing: 0.2em; text-transform: uppercase; }
                        #id_$id .info-lbl { font-family: sans-serif; font-size: 8px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.1em; }
                        #id_$id .info-val { font-family: sans-serif; font-size: 11px; }
                    </style>
                </defs>
            """.trimIndent())

            // Background & Layout
            appendLine("""  <rect width="350" height="200" rx="2" fill="$bgColor"/>""")
            appendLine("""  <rect x="0" y="0" width="115" height="200" fill="$accentColor" opacity="0.03"/>""")
            appendLine("""  <rect x="115" y="20" width="1" height="160" fill="$accentColor" opacity="0.3" class="reveal" style="animation-delay: 0.1s;"/>""")

            // Left Identity: Stacked Name
            appendLine("""  <text x="25" y="55" fill="$textColor" class="name-serif reveal" style="animation-delay: 0.2s;">${vcard.firstName}</text>""")
            appendLine("""  <text x="25" y="85" fill="$textColor" class="name-serif reveal" style="animation-delay: 0.3s;">${vcard.lastName}</text>""")
            vcard.title?.let {
                appendLine("""  <text x="25" y="110" fill="$accentColor" class="mono-tag reveal" style="animation-delay: 0.4s;">$it</text>""")
            }

            // Right Data Column
            var yPos = 45
            // Organization
            vcard.organization?.let {
                appendLine("""  <text x="140" y="$yPos" fill="$secondaryColor" class="info-lbl reveal" style="animation-delay: 0.5s;">Organization</text>""")
                appendLine("""  <text x="140" y="${yPos + 20}" fill="$textColor" class="info-val reveal" style="animation-delay: 0.5s;">$it</text>""")
                yPos += 55
            }

            // Contact Block
            appendLine("""  <text x="140" y="$yPos" fill="$secondaryColor" class="info-lbl reveal" style="animation-delay: 0.6s;">Secure Channel</text>""")
            val primaryEmail = vcard.emails.firstOrNull()?.address ?: vcard.email
            primaryEmail?.let {
                appendLine("""  <text x="140" y="${yPos + 20}" fill="$textColor" class="info-val reveal" style="animation-delay: 0.6s;">$it</text>""")
            }
            val primaryPhone = vcard.phones.firstOrNull()?.number ?: vcard.mobile
            primaryPhone?.let {
                appendLine("""  <text x="140" y="${yPos + 40}" fill="$textColor" class="info-val reveal" style="animation-delay: 0.6s;">$it</text>""")
            }

            // QR Access Section
            appendLine("""  <g id="qr-trigger-$id" class="reveal" style="cursor: pointer; animation-delay: 0.7s;">""")
            appendLine("""    <rect x="264" y="64" width="60" height="60" rx="1" fill="$accentColor" opacity="0.4"/>""")
            appendLine("""    <rect x="260" y="60" width="60" height="60" rx="1" fill="#FFFFFF" />""")
            appendLine("""    <image x="265" y="65" width="50" height="50" href="$qrCodeBase64"/>""")
            appendLine("""    <text x="290" y="140" text-anchor="middle" fill="$secondaryColor" class="mono-tag" style="font-size: 6px;">V_ACCESS_NODE</text>""")
            appendLine("""  </g>""")

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