package io.docops.docopsextensionssupport.vcard.model.renderer

import io.docops.docopsextensionssupport.vcard.model.QRCodeService
import io.docops.docopsextensionssupport.vcard.model.VCard
import io.docops.docopsextensionssupport.vcard.model.VCardConfig
import io.docops.docopsextensionssupport.vcard.model.VCardGeneratorService
import java.util.Locale
import java.util.Locale.getDefault
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class BusinessCard2Renderer : VCardRenderer {
    override val designKey: String = "business_card_design2"

    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        val theme = config.theme
        val colors = when (theme) {
            "dark" -> arrayOf("#0f172a", "#ffffff", "#94a3b8", "#3b82f6", "#1e293b", "#334155")
            else -> arrayOf("#f8fafc", "#1e293b", "#64748b", "#2563eb", "#ffffff", "#e2e8f0")
        }
        val bgColor = colors[0]
        val primaryText = colors[1]
        val secondaryText = colors[2]
        val accentColor = colors[3]
        val cardBg = colors[4]
        val borderColor = colors[5]

        // Generate QR code
        val vCardGeneratorService = VCardGeneratorService()
        val qrCodeService = QRCodeService()
        val vCardData = vCardGeneratorService.generateVCard30(vcard)
        val qrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 100, 100)

        val id: String = Uuid.random().toHexString()
        return buildString {
            appendLine("""<svg width="350" height="200" viewBox="0 0 350 200" id="id_$id" xmlns="http://www.w3.org/2000/svg">""")
            appendLine("""  <defs>""")
            appendLine("""    <linearGradient id="headerGrad" x1="0%" y1="0%" x2="100%" y2="0%">""")
            appendLine("""      <stop offset="0%" style="stop-color:$accentColor;stop-opacity:1" />""")
            appendLine("""      <stop offset="100%" style="stop-color:${accentColor}80;stop-opacity:1" />""")
            appendLine("""    </linearGradient>""")
            appendLine("""    <filter id="cardShadow" x="-20%" y="-20%" width="140%" height="140%">""")
            appendLine("""      <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="#00000020"/>""")
            appendLine("""    </filter>""")
            appendLine("""  </defs>""")

            // Background
            appendLine("""  <rect width="350" height="200" rx="12" fill="$bgColor"/>""")

            // Main card with shadow
            appendLine("""  <rect x="10" y="10" width="330" height="180" rx="8" fill="$cardBg" stroke="$borderColor" stroke-width="1" filter="url(#cardShadow)"/>""")

            // Header section with gradient
            appendLine("""  <rect x="10" y="10" width="330" height="60" rx="8" fill="url(#headerGrad)"/>""")
            appendLine("""  <rect x="10" y="60" width="330" height="8" fill="url(#headerGrad)" opacity="0.3"/>""")

            // Avatar/Initial circle
            val initials = "${vcard.firstName.firstOrNull() ?: ""}${vcard.lastName.firstOrNull() ?: ""}".uppercase()
            appendLine("""  <circle cx="50" cy="40" r="20" fill="rgba(255,255,255,0.2)" stroke="rgba(255,255,255,0.4)" stroke-width="2"/>""")
            appendLine("""  <text x="50" y="46" font-family="'Inter', sans-serif" font-size="16" font-weight="600" fill="white" text-anchor="middle">$initials</text>""")

            // Name in header
            val fullName = "${vcard.firstName} ${vcard.lastName}".trim()
            appendLine("""  <text x="85" y="35" font-family="'Inter', sans-serif" font-size="18" font-weight="700" fill="white">$fullName</text>""")

            // Title in header
            vcard.title?.let {
                appendLine("""  <text x="85" y="52" font-family="'Inter', sans-serif" font-size="12" font-weight="400" fill="rgba(255,255,255,0.9)">$it</text>""")
            }

            // Content area starts at y=85
            var yPos = 95

            // Organization with icon
            vcard.organization?.let {
                appendLine("""  <rect x="25" y="${yPos - 8}" width="12" height="12" rx="2" fill="$accentColor" opacity="0.2"/>""")
                appendLine("""  <rect x="27" y="${yPos - 6}" width="8" height="2" rx="1" fill="$accentColor"/>""")
                appendLine("""  <rect x="27" y="${yPos - 3}" width="8" height="2" rx="1" fill="$accentColor"/>""")
                appendLine("""  <text x="45" y="$yPos" font-family="'Inter', sans-serif" font-size="13" font-weight="600" fill="$primaryText">$it</text>""")
                yPos += 25
            }

            // Contact section with modern icons
            appendLine("""  <line x1="25" y1="$yPos" x2="325" y2="$yPos" stroke="$borderColor" stroke-width="1" opacity="0.5"/>""")
            yPos += 20

            // PRIMARY Email only
            val primaryEmail = vcard.emails.firstOrNull()?.address ?: vcard.email
            primaryEmail?.let {
                appendLine("""  <rect x="25" y="${yPos - 8}" width="14" height="10" rx="2" fill="none" stroke="$accentColor" stroke-width="1.5"/>""")
                appendLine("""  <path d="M25 ${yPos - 6} L32 ${yPos - 1} L39 ${yPos - 6}" fill="none" stroke="$accentColor" stroke-width="1.5"/>""")
                appendLine("""  <text x="50" y="$yPos" font-family="'Inter', sans-serif" font-size="11" font-weight="400" fill="$secondaryText">$it</text>""")
                yPos += 20
            }

            // PRIMARY Phone only
            val primaryPhone = vcard.phones.firstOrNull()?.number ?: vcard.mobile
            primaryPhone?.let {
                appendLine("""  <rect x="27" y="${yPos - 10}" width="10" height="14" rx="2" fill="none" stroke="$accentColor" stroke-width="1.5"/>""")
                appendLine("""  <rect x="29" y="${yPos - 8}" width="6" height="1" rx="0.5" fill="$accentColor"/>""")
                appendLine("""  <rect x="30" y="${yPos - 2}" width="4" height="1" rx="0.5" fill="$accentColor"/>""")
                appendLine("""  <text x="50" y="$yPos" font-family="'Inter', sans-serif" font-size="11" font-weight="400" fill="$secondaryText">$it</text>""")
            }

            // QR Code in bottom right
            appendLine("""  <g id="qr-trigger-$id" style="cursor: pointer;">""")
            appendLine("""    <rect x="240" y="110" width="90" height="70" rx="6" fill="white"/>""")
            appendLine("""    <image x="250" y="115" width="60" height="60" href="$qrCodeBase64"/>""")
            appendLine("""    <text x="280" y="184" font-family="'Inter', sans-serif" font-size="7" fill="$secondaryText" text-anchor="middle">Scan for all info</text>""")
            appendLine("""  </g>""")

            // Bottom accent line
            appendLine("""  <rect x="25" y="175" width="200" height="3" rx="1.5" fill="url(#headerGrad)" opacity="0.6"/>""")

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