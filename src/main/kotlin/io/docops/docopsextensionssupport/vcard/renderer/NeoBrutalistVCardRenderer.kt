package io.docops.viz.vcard.renderer


import io.docops.docopsextensionssupport.qrcode.cyberNeonTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.vcard.VCard
import io.docops.docopsextensionssupport.vcard.VCardConfig
import io.docops.docopsextensionssupport.vcard.VCardGeneratorService
import io.docops.docopsextensionssupport.vcard.renderer.QRCodeService
import io.docops.docopsextensionssupport.vcard.renderer.VCardRenderer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A Neo-Brutalist design renderer that strictly separates layout transforms from animation transforms
 * to prevent layout collapse. Uses nested groups to ensure stability.
 */
class NeoBrutalistCardRenderer(val useDark: Boolean) : VCardRenderer {

    override val designKey: String = "neo_brutalist"

    private val theme = ThemeFactory.getTheme(useDark = useDark)
    @OptIn(ExperimentalUuidApi::class)
    override fun render(vcard: VCard, config: VCardConfig): String {
        // High-contrast, opinionated palette
        val colors = if (useDark) {
            arrayOf("#050505", "#141414", "#EAEAEA", "#888888", "#D4FF00", "#FFFFFF")
        } else {
            // Light mode: Vibrant Blue accent to match the user's preference in the screenshot, but bolder
            arrayOf("#F2F2F2", "#FFFFFF", "#000000", "#555555", "#2563EB", "#000000")
        }
        val bgMain = colors[0]
        val bgCard = theme.canvas
        val textMain = theme.primaryText
        val textDim = theme.secondaryText
        val accent = theme.accentColor
        val border = colors[5]

        val id = Uuid.random().toHexString()

        val vCardGeneratorService = VCardGeneratorService()

        val qrCodeService = QRCodeService()
        val vCardData = vCardGeneratorService.generateMinimalVCard(vcard)
        val qrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 70, 70, theme = cyberNeonTheme)
        val largeQrCodeBase64 = qrCodeService.generateQRCodeBase64(vCardData, 150, 150, theme = cyberNeonTheme)

        return buildString {
            appendLine("""<svg width="350" height="200" viewBox="0 0 350 200" id="id_$id" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Business Card">""")
            appendLine("""
                <defs>
                    <style>
                    ${theme.fontImport}
                        /* Design Tokens */
                        #id_$id {
                            --c-bg: $bgCard;
                            --c-text: $textMain;
                            --c-text-dim: $textDim;
                            --c-accent: $accent;
                            --c-border: $border;
                        }
                        
                        /* Entrance Animation */
                        @keyframes slideUp_$id {
                            0% { transform: translate(0, 15px); opacity: 0; }
                            100% { transform: translate(0, 0); opacity: 1; }
                        }
                        
                        .animate-in {
                            animation: slideUp_$id 0.5s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
                            opacity: 0; 
                        }

                        /* Typography */
                        .font-headline {
                            font-family: ${theme.fontFamily};
                            font-weight: 900;
                            text-transform: uppercase;
                            letter-spacing: -0.01em;
                        }
                        .font-mono {
                            font-family: 'Courier New', monospace;
                            font-weight: 600;
                            letter-spacing: -0.02em;
                        }
                        
                        /* Interaction: Scale/Move on Hover */
                        .btn-wrapper { cursor: pointer; }
                        .btn-wrapper:hover .btn-inner { transform: translate(-3px, -3px); }
                        .btn-inner { transition: transform 0.15s ease-out; }
                        
                    </style>
                    <pattern id="grid-$id" width="10" height="10" patternUnits="userSpaceOnUse">
                         <circle cx="1" cy="1" r="0.5" fill="$textMain" opacity="0.2"/>
                    </pattern>
                </defs>
            """.trimIndent())

            // 1. Background
            appendLine("""<rect width="350" height="200" fill="$bgMain"/>""")
            appendLine("""<rect width="350" height="200" fill="url(#grid-$id)"/>""")

            // 2. Card Container (Centered)
            // Layout Group -> Animation Group -> Content
            appendLine("""<g transform="translate(10, 10)">""")
            appendLine("""<g class="animate-in" style="animation-delay: 0.1s;">""")
            // Hard shadow
            appendLine("""<rect x="6" y="6" width="320" height="170" fill="var(--c-border)"/>""")
            // Main surface
            appendLine("""<rect width="320" height="170" fill="var(--c-bg)" stroke="var(--c-border)" stroke-width="3"/>""")
            appendLine("""</g>""")
            appendLine("""</g>""")

            // 3. Identity Section (Name & Title)
            // Absolute positioning used inside, so wrapper just handles entrance
            appendLine("""<g class="animate-in" style="animation-delay: 0.2s;">""")
            val fullName = "${vcard.firstName} ${vcard.lastName}".uppercase()
            // Name
            appendLine("""<text x="35" y="60" fill="var(--c-text)" font-size="24" class="font-headline">$fullName</text>""")

            // Role Pill
            vcard.title?.let { title ->
                appendLine("""<g transform="translate(35, 75)">""")
                val estWidth = title.length * 7 + 10
                appendLine("""<rect x="0" y="-12" width="$estWidth" height="18" fill="var(--c-accent)" stroke="var(--c-border)" stroke-width="2"/>""")
                appendLine("""<text x="5" y="0" fill="var(--c-bg)" font-size="10" class="font-mono" font-weight="bold">${title.uppercase()}</text>""")
                appendLine("""</g>""")
            }
            appendLine("""</g>""")

            // 4. Contact Data
            // VITAL: Outer group for Layout, Inner for Animation.
            appendLine("""<g transform="translate(35, 120)">""")
            appendLine("""<g class="animate-in" style="animation-delay: 0.3s;">""")
            var yPos = 0

            // Divider
            appendLine("""<line x1="0" y1="-10" x2="160" y2="-10" stroke="var(--c-text-dim)" stroke-width="2" stroke-dasharray="2 2"/>""")

            val primaryEmail = vcard.emails.firstOrNull()?.address ?: vcard.email
            val primaryPhone = vcard.phones.firstOrNull()?.number ?: vcard.mobile

            primaryEmail?.let {
                appendLine("""<text x="0" y="$yPos" fill="var(--c-text)" font-size="11" class="font-mono">$it</text>""")
                yPos += 18
            }
            primaryPhone?.let {
                appendLine("""<text x="0" y="$yPos" fill="var(--c-text)" font-size="11" class="font-mono">$it</text>""")
                yPos += 18
            }
            vcard.organization?.let {
                appendLine("""<text x="0" y="$yPos" fill="var(--c-text-dim)" font-size="10" class="font-mono" font-weight="bold">${it.uppercase()}</text>""")
            }
            appendLine("""</g>""")
            appendLine("""</g>""")

            // 5. QR Code "Sticker"
            // VITAL: Strict separation of transforms to prevent layout collapse
            appendLine("""<g transform="translate(235, 95)" id="qr-trigger-$id" class="btn-wrapper">""") // 1. Layout Position
            appendLine("""<g class="animate-in" style="animation-delay: 0.4s;">""") // 2. Entrance Animation
            appendLine("""<g class="btn-inner">""") // 3. Hover Interaction

            // Shadow
            appendLine("""<rect x="4" y="4" width="80" height="80" fill="var(--c-border)"/>""")
            // Sticker Bg
            appendLine("""<rect width="80" height="80" fill="#FFFFFF" stroke="var(--c-border)" stroke-width="3"/>""")
            // QR   width=70 h=70
            appendLine("""<g transform="translate(5, 5)">$qrCodeBase64</g>""")

            // "SCAN" Tape
            appendLine("""<g transform="translate(-10, 65) rotate(-10)">""")
            appendLine("""<rect width="45" height="16" fill="var(--c-accent)" stroke="var(--c-border)" stroke-width="2"/>""")
            appendLine("""<text x="22" y="11" fill="var(--c-bg)" font-size="10" font-weight="900" text-anchor="middle" font-family="sans-serif">SCAN</text>""")
            appendLine("""</g>""")

            appendLine("""</g>""")
            appendLine("""</g>""")
            appendLine("""</g>""")

            // 6. Modal
            appendLine("""
                <g id="qr-modal-$id" style="display: none;">
                    <rect width="350" height="200" fill="rgba(0,0,0,0.9)" id="modal-bg-$id" style="cursor: pointer;"/>
                    <g transform="translate(75,5)">
                        <g class="animate-in">
                            <rect x="6" y="6" width="200" height="190" fill="var(--c-border)"/>
                            <rect width="200" height="190" fill="#FFFFFF" stroke="var(--c-border)" stroke-width="3"/>
                            <g transform="translate(20,10)">
                                <g>$largeQrCodeBase64</g>
                            </g>
                            <text x="100" y="182" font-family="'Courier New', monospace" font-size="10" fill="#000000" font-weight="bold" text-anchor="middle">// TAP TO CLOSE //</text>
                        </g>
                    </g>
                </g>

                <script type="text/javascript">
                <![CDATA[
                    (function() {
                        var svg = document.getElementById('id_$id');
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
                        }
                    })();
                ]]>
                </script>
            """.trimIndent())

            appendLine("</svg>")
        }
    }
}