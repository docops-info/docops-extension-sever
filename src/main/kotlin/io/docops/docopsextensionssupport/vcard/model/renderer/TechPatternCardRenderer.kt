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
        val (bgColor, textColor, accentColor, secondaryColor) = when (theme) {
            "dark" -> arrayOf("#0A0E17", "#FFFFFF", "#00ff88", "#475569")
            else -> arrayOf("#F8FAFC", "#0F172A", "#2563EB", "#64748B")
        }
        val id: String = Uuid.random().toHexString()

        val vCardGeneratorService = VCardGeneratorService()
        val qrCodeService = QRCodeService()
        val vCardData = vCardGeneratorService.generateVCard30(vcard)
        val qrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 80, 80)

        return buildString {
            appendLine("""<svg width="350" height="200" viewBox="0 0 350 200" id="id_$id" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMidYMid meet">""")
            appendLine("""
                <defs>
                    <style>
                        #id_$id @keyframes scanline { 0% { transform: translateY(-100px); opacity: 0; } 50% { opacity: 0.4; } 100% { transform: translateY(200px); opacity: 0; } }
                        #id_$id @keyframes reveal { from { opacity: 0; filter: blur(4px); } to { opacity: 1; filter: blur(0); } }
                        #id_$id .tech-reveal { animation: reveal 0.8s ease-out both; }
                        #id_$id .name-txt { font-family: 'JetBrains Mono', monospace; font-weight: 800; font-size: 18px; letter-spacing: 0.05em; text-transform: uppercase; fill: $textColor; }
                        #id_$id .code-txt { font-family: 'JetBrains Mono', monospace; font-size: 9px; fill: $accentColor; opacity: 0.8; }
                        #id_$id .terminal-txt { font-family: 'JetBrains Mono', monospace; font-size: 10px; fill: $textColor; }
                    </style>
                    <linearGradient id="scan-grad-$id" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stop-color="$accentColor" stop-opacity="0"/>
                        <stop offset="50%" stop-color="$accentColor" stop-opacity="0.2"/>
                        <stop offset="100%" stop-color="$accentColor" stop-opacity="0"/>
                    </linearGradient>
                </defs>
            """.trimIndent())

            // Background & Atmospheric Scan Line
            appendLine("""  <rect width="350" height="200" rx="2" fill="$bgColor"/>""")
            appendLine("""  <rect width="350" height="80" fill="url(#scan-grad-$id)" style="animation: scanline 4s linear infinite;"/>""")

            // Identity Block
            appendLine("""  <g transform="translate(35, 45)" class="tech-reveal" style="animation-delay: 0.1s;">""")
            appendLine("""    <text class="name-txt">${vcard.firstName} ${vcard.lastName}</text>""")
            vcard.title?.let {
                appendLine("""    <text y="20" class="code-txt">// ROLE: ${it.uppercase()}</text>""")
            }
            appendLine("""  </g>""")

            // Terminal Content
            var yPos = 100
            appendLine("""  <g transform="translate(35, $yPos)" class="tech-reveal" style="animation-delay: 0.3s;">""")
            appendLine("""    <rect x="-10" y="0" width="3" height="12" fill="$accentColor"><animate attributeName="opacity" values="1;0;1" dur="1s" repeatCount="indefinite" /></rect>""")
            appendLine("""    <text class="code-txt">$ contact --fetch-identity</text>""")

            val primaryEmail = vcard.emails.firstOrNull()?.address ?: vcard.email
            primaryEmail?.let {
                appendLine("""    <text y="18" class="terminal-txt">$it</text>""")
            }
            val primaryPhone = vcard.phones.firstOrNull()?.number ?: vcard.mobile
            primaryPhone?.let {
                appendLine("""    <text y="34" class="terminal-txt">$it</text>""")
            }
            appendLine("""  </g>""")

            // Bottom Detail: Segmented Progress Detail
            appendLine("""  <g transform="translate(35, 175)" opacity="0.3" class="tech-reveal" style="animation-delay: 0.5s;">""")
            appendLine("""    <rect width="20" height="3" fill="$accentColor" rx="1"/>""")
            appendLine("""    <rect x="25" width="20" height="3" fill="$accentColor" rx="1"/>""")
            appendLine("""    <rect x="50" width="10" height="3" fill="$accentColor" rx="1"/>""")
            appendLine("""  </g>""")

            // QR Access Block
            appendLine("""  <g id="qr-trigger-$id" transform="translate(245, 65)" class="tech-reveal" style="cursor: pointer; animation-delay: 0.6s;">""")
            appendLine("""    <rect width="75" height="75" rx="1" fill="none" stroke="$accentColor" stroke-width="0.5" stroke-dasharray="8 4"/>""")
            appendLine("""    <rect x="4" y="4" width="67" height="67" rx="1" fill="white" opacity="0.95"/>""")
            appendLine("""    <image x="7" y="7" width="61" height="61" href="$qrCodeBase64"/>""")
            appendLine("""    <text x="37.5" y="90" text-anchor="middle" class="code-txt" style="font-size: 7px;">AUTH_SCAN_v2</text>""")
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