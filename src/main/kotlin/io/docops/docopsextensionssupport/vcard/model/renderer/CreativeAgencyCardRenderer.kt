package io.docops.docopsextensionssupport.vcard.model.renderer

import io.docops.docopsextensionssupport.vcard.model.QRCodeService
import io.docops.docopsextensionssupport.vcard.model.VCard
import io.docops.docopsextensionssupport.vcard.model.VCardConfig
import io.docops.docopsextensionssupport.vcard.model.VCardGeneratorService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreativeAgencyCardRenderer(val useDark: Boolean) : VCardRenderer {
    override val designKey: String = "creative_agency_pro_contact_card"

    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        val theme = if(useDark) "dark" else "light"
        val (bgColor, textColor, accentColor, secondaryColor, metaColor) = when (theme) {
            "dark" -> arrayOf("#0F172A", "#FFFFFF", "#4ecdc4", "#CBD5E1", "#ff6b6b")
            else -> arrayOf("#F8FAFC", "#0F172A", "#2563EB", "#475569", "#ff6b6b")
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
                        #id_$id .name-serif { font-family: 'Georgia', serif; font-size: 24px; font-weight: bold; font-style: italic; }
                        #id_$id .tagline { font-family: sans-serif; font-weight: 900; font-size: 10px; letter-spacing: 0.2em; text-transform: uppercase; }
                        #id_$id .contact-txt { font-family: sans-serif; font-size: 11px; }
                    </style>
                    <linearGradient id="panel-grad-$id" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" stop-color="${if(theme=="dark") "#1E293B" else "#FFFFFF"}" />
                        <stop offset="100%" stop-color="$bgColor" />
                    </linearGradient>
                </defs>
            """.trimIndent())

            // Background & Brutalist Accents
            appendLine("""  <rect width="350" height="200" rx="2" fill="url(#panel-grad-$id)"/>""")
            appendLine("""  <polygon points="240,0 350,0 350,200 210,200" fill="$accentColor" opacity="0.1" class="reveal" style="animation-delay: 0.1s;"/>""")
            appendLine("""  <rect x="0" y="40" width="350" height="2" fill="$metaColor" opacity="0.6" class="reveal" style="animation-delay: 0.2s;"/>""")

            // Identity Content
            appendLine("""  <text x="35" y="75" fill="$textColor" class="name-serif reveal" style="animation-delay: 0.3s;">${vcard.firstName} ${vcard.lastName}</text>""")
            vcard.title?.let {
                appendLine("""  <text x="35" y="98" fill="$accentColor" class="tagline reveal" style="animation-delay: 0.4s;">$it</text>""")
            }

            // Contact Details
            var yPos = 135
            appendLine("""  <g class="reveal" style="animation-delay: 0.5s;">""")
            val primaryEmail = vcard.emails.firstOrNull()?.address ?: vcard.email
            primaryEmail?.let {
                appendLine("""    <text x="35" y="$yPos" fill="$secondaryColor" class="contact-txt">$it</text>""")
                yPos += 20
            }
            val primaryPhone = vcard.phones.firstOrNull()?.number ?: vcard.mobile
            primaryPhone?.let {
                appendLine("""    <text x="35" y="$yPos" fill="$secondaryColor" class="contact-txt">$it</text>""")
            }
            appendLine("""  </g>""")

            // QR Access Block
            appendLine("""  <g id="qr-trigger-$id" class="reveal" style="cursor: pointer; animation-delay: 0.6s;">""")
            appendLine("""    <rect x="254" y="64" width="70" height="70" rx="1" fill="$metaColor" opacity="0.4"/>""")
            appendLine("""    <rect x="250" y="60" width="70" height="70" rx="1" fill="#FFFFFF" />""")
            appendLine("""    <image x="255" y="65" width="60" height="60" href="$qrCodeBase64"/>""")
            appendLine("""    <text x="285" y="145" text-anchor="middle" font-family="sans-serif" font-weight="900" font-size="7" fill="$accentColor" letter-spacing="0.1em">V-ACCESS</text>""")
            appendLine("""  </g>""")

            // Polish: Corner dots
            appendLine("""  <circle cx="330" cy="25" r="2" fill="$metaColor" opacity="0.8"/>""")
            appendLine("""  <circle cx="315" cy="25" r="2" fill="$accentColor" opacity="0.5"/>""")


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