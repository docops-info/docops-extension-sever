package io.docops.docopsextensionssupport.vcard.renderer


import io.docops.docopsextensionssupport.vcard.VCard
import io.docops.docopsextensionssupport.vcard.VCardConfig
import io.docops.docopsextensionssupport.vcard.VCardGeneratorService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TechPatternCardRenderer(val useDark: Boolean) : VCardRenderer {
    override val designKey: String = "tech_pattern_background"

    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        val theme = if(useDark) "dark" else "light"
        val (paperColor, blackColor, accentColor) = when (theme) {
            "dark" -> arrayOf("#f5f5f7", "#000", "#84cc16")
            else -> arrayOf("#f5f5f7", "#000", "#84cc16")
        }
        val id: String = Uuid.random().toHexString()

        val vCardGeneratorService = VCardGeneratorService()
        val qrCodeService = QRCodeService()
        val vCardData = vCardGeneratorService.generateMinimalVCard(vcard)
        val qrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 180, 180)

        return buildString {
            appendLine("""<svg width="350" height="200" viewBox="0 0 920 525" id="id_$id" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMidYMid meet">""")
            appendLine("""
                <defs>
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Anton&amp;family=Space+Mono&amp;display=swap');
                        
                        #id_$id .card-container { cursor: pointer; transform-style: preserve-3d; transition: transform 700ms; }
                        #id_$id .card-container.flipped { transform: rotateY(180deg); }
                        #id_$id .side { backface-visibility: hidden; }
                        #id_$id .back-side { transform: rotateY(180deg); }
                        
                        #id_$id @keyframes slideDown { from { transform: translateY(-100%); } to { transform: translateY(0); } }
                        #id_$id @keyframes slideInLeft { from { transform: translateX(-100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
                        #id_$id @keyframes slideInRight { from { transform: translateY(-100%); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
                        #id_$id @keyframes fadeInUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
                        #id_$id @keyframes pulse { 0%, 100% { transform: scale(1); opacity: 1; } 50% { transform: scale(1.5); opacity: 0.5; } }
                        
                        #id_$id .accent-strip { animation: slideDown 1s ease-out; }
                        #id_$id .name-first { animation: slideInLeft 0.8s cubic-bezier(.16,1,.3,1) both; }
                        #id_$id .name-second { animation: slideInLeft 0.8s cubic-bezier(.16,1,.3,1) 0.1s both; }
                        #id_$id .vertical-title { animation: slideInRight 0.8s cubic-bezier(.16,1,.3,1) 0.3s both; }
                        #id_$id .contact-1 { animation: fadeInUp 0.6s ease-out 0.5s both; }
                        #id_$id .contact-2 { animation: fadeInUp 0.6s ease-out 0.6s both; }
                        #id_$id .contact-3 { animation: fadeInUp 0.6s ease-out 0.7s both; }
                        #id_$id .dot { animation: pulse 2s ease-in-out infinite; }
                        
                        #id_$id .name-txt { font-family: 'Anton', 'Impact', sans-serif; font-weight: 900; font-size: 64px; letter-spacing: -0.05em; fill: $blackColor; }
                        #id_$id .title-txt { font-family: 'Space Mono', monospace; font-weight: 900; font-size: 20px; letter-spacing: 0.15em; fill: $accentColor; }
                        #id_$id .contact-txt { font-family: monospace; font-size: 14px; fill: $blackColor; }
                        #id_$id .qr-label { font-family: monospace; font-size: 11px; fill: $blackColor; }
                        #id_$id .back-title { font-family: 'Anton', 'Impact', sans-serif; font-weight: 900; font-size: 48px; fill: $paperColor; }
                        #id_$id .back-text { font-family: monospace; font-size: 18px; fill: $paperColor; }
                        #id_$id .back-hint { font-family: monospace; font-size: 13px; fill: #9ca3af; }
                    </style>
                </defs>
            """.trimIndent())

            // Card container group (for flip transformation)
            appendLine("""  <g class="card-container" id="card-$id">""")

            // === FRONT SIDE ===
            appendLine("""    <g class="side front-side">""")

            // Background
            appendLine("""      <rect width="920" height="525" fill="$paperColor"/>""")

            // Diagonal black background (right side)
            appendLine("""      <path d="M 690 0 L 920 0 L 920 525 L 460 525 Z" fill="$blackColor"/>""")

            // Accent strip (left edge)
            appendLine("""      <rect class="accent-strip" width="8" height="525" fill="$accentColor"/>""")

            // Decorative dot
            appendLine("""      <circle class="dot" cx="32" cy="32" r="6" fill="$accentColor"/>""")

            // Name (stacked)
            appendLine("""      <g transform="translate(32, 48)">""")
            appendLine("""        <text class="name-txt name-first" y="58">${vcard.firstName.uppercase()}</text>""")
            appendLine("""        <text class="name-txt name-second" y="124">${vcard.lastName.uppercase()}</text>""")
            appendLine("""      </g>""")

            // Vertical title (right side) - moved left to avoid diagonal
            vcard.title?.let { title ->
                appendLine("""      <g class="vertical-title" transform="translate(900, 48)">""")
                appendLine("""        <text class="title-txt" writing-mode="tb" text-anchor="start">${title.uppercase()}</text>""")
                appendLine("""      </g>""")
            }


            // Contact cluster (bottom left)
            val primaryEmail = vcard.emails.firstOrNull()?.address ?: vcard.email
            val primaryPhone = vcard.phones.firstOrNull()?.number ?: vcard.mobile

            appendLine("""      <g transform="translate(32, 420)">""")

            // Email
            primaryEmail?.let {
                appendLine("""        <g class="contact-1">""")
                appendLine("""          <path d="M3 8.5v7A2.5 2.5 0 0 0 5.5 18h13A2.5 2.5 0 0 0 21 15.5v-7 M21 8l-9 5-9-5" stroke="$blackColor" stroke-width="1.5" fill="none"/>""")
                appendLine("""          <text class="contact-txt" x="32" y="13">$it</text>""")
                appendLine("""        </g>""")
            }

            // Phone
            primaryPhone?.let {
                appendLine("""        <g class="contact-2" transform="translate(0, 24)">""")
                appendLine("""          <path d="M22 16.92V21a1 1 0 0 1-1.09 1 19.86 19.86 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.86 19.86 0 0 1 2 3.09 1 1 0 0 1 3 2h4.09a1 1 0 0 1 1 .75 12.05 12.05 0 0 0 .7 2.81 1 1 0 0 1-.24 1.09L7.91 8.91a16 16 0 0 0 6 6l1.26-1.26a1 1 0 0 1 1.09-.24 12.05 12.05 0 0 0 2.81.7 1 1 0 0 1 .75 1z" stroke="$blackColor" stroke-width="1.5" fill="none"/>""")
                appendLine("""          <text class="contact-txt" x="32" y="13">$it</text>""")
                appendLine("""        </g>""")
            }

            // Organization
            vcard.organization?.let { org ->
                appendLine("""        <g class="contact-3" transform="translate(0, 48)">""")
                appendLine("""          <path d="M3 21h18 M6 21V10a1 1 0 0 1 1-1h10a1 1 0 0 1 1 1v11 M9 21V6 M15 21V6" stroke="$blackColor" stroke-width="1.5" fill="none"/>""")
                appendLine("""          <text class="contact-txt" x="32" y="13">$org</text>""")
                appendLine("""        </g>""")
            }

            appendLine("""      </g>""")

            // QR block (bottom right) - INCREASED SIZE & SHIFTED LEFT
            appendLine("""      <g transform="translate(620, 305)">""")  // Changed from 700 to 620
            appendLine("""        <rect width="200" height="200" fill="$paperColor" stroke="$blackColor" stroke-width="6"/>""")
            appendLine("""        <rect x="10" y="10" width="180" height="180" fill="white"/>""")
            appendLine("""        <g transform="translate(10, 10)">$qrCodeBase64</g>""")
            appendLine("""        <text class="qr-label" x="100" y="216" text-anchor="middle">SCAN</text>""")
            appendLine("""      </g>""")

            appendLine("""    </g>""")  // Close front-side


            // === BACK SIDE === (Replace your current back-side section)
            appendLine("""    <g class="side back-side">""")

// Dramatic gradient background
            appendLine("""      <defs>""")
            appendLine("""        <linearGradient id="scanGrad-$id" x1="0%" y1="0%" x2="100%" y2="100%">""")
            appendLine("""          <stop offset="0%" style="stop-color:#0a0e27;stop-opacity:1" />""")
            appendLine("""          <stop offset="100%" style="stop-color:#1a1f3a;stop-opacity:1" />""")
            appendLine("""        </linearGradient>""")
            appendLine("""      </defs>""")
            appendLine("""      <rect width="920" height="525" fill="url(#scanGrad-$id)"/>""")

// Grid pattern for tech aesthetic
            appendLine("""      <defs>""")
            appendLine("""        <pattern id="grid-$id" width="40" height="40" patternUnits="userSpaceOnUse">""")
            appendLine("""          <path d="M 40 0 L 0 0 0 40" fill="none" stroke="rgba(132,204,22,0.1)" stroke-width="1"/>""")
            appendLine("""        </pattern>""")
            appendLine("""      </defs>""")
            appendLine("""      <rect width="920" height="525" fill="url(#grid-$id)"/>""")

// Massive centered QR code (350×350px)
            appendLine("""      <g transform="translate(285, 87.5)">""")
// White container with shadow effect
            appendLine("""        <rect x="4" y="4" width="350" height="350" fill="rgba(0,0,0,0.3)" rx="12"/>""")
            appendLine("""        <rect width="350" height="350" fill="white" rx="8"/>""")
// Large QR
            appendLine("""        <rect x="25" y="25" width="300" height="300" fill="white"/>""")
            appendLine("""        <g transform="translate(25, 25)">""")
// Generate 300×300 QR here
            val largeQrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 300, 300)
            appendLine(largeQrCodeBase64)
            appendLine("""        </g>""")
            appendLine("""      </g>""")

// Glowing accent indicators
            appendLine("""      <g opacity="0.8">""")
            appendLine("""        <circle cx="285" cy="262.5" r="8" fill="none" stroke="$accentColor" stroke-width="2"/>""")
            appendLine("""        <circle cx="635" cy="262.5" r="8" fill="none" stroke="$accentColor" stroke-width="2"/>""")
            appendLine("""        <circle cx="460" cy="87.5" r="8" fill="none" stroke="$accentColor" stroke-width="2"/>""")
            appendLine("""        <circle cx="460" cy="437.5" r="8" fill="none" stroke="$accentColor" stroke-width="2"/>""")
            appendLine("""      </g>""")

// Instructions
            appendLine("""      <g transform="translate(460, 465)">""")
            appendLine("""        <text class="back-hint" text-anchor="middle" y="0">→ SCAN TO SAVE CONTACT ←</text>""")
            appendLine("""        <text class="back-hint" text-anchor="middle" y="20" opacity="0.6">Click card to flip back</text>""")
            appendLine("""      </g>""")

// Corner accent
            appendLine("""      <rect width="8" height="525" fill="$accentColor"/>""")
            appendLine("""      <rect x="912" width="8" height="525" fill="$accentColor"/>""")

            appendLine("""    </g>""")
            appendLine("""    </g>""")

            // Flip interaction script
            appendLine("""
  <script type="text/javascript">
  <![CDATA[
      (function() {
          var svg = document.getElementById('id_$id');
          if (!svg) return;
          
          var cardContainer = svg.getElementById('card-$id');
          
          if (cardContainer) {
              svg.addEventListener('click', function(e) {
                  cardContainer.classList.toggle('flipped');
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