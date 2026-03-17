package io.docops.docopsextensionssupport.badge.type

import io.docops.docopsextensionssupport.badge.Badge
import io.docops.docopsextensionssupport.badge.BadgeConfig
import io.docops.docopsextensionssupport.badge.BadgeStyle


object BadgeStyleGenerator {

     fun generateStyles(style: BadgeStyle, theme: String, fontFamily: String): String {
        val useMediaQuery = theme == "auto" || theme == "both"

        return when (style) {
            BadgeStyle.GLASSMORPHIC -> """
    <style>
      ${if (useMediaQuery) """
      @media (prefers-color-scheme: dark) {
        .bg-left { fill: rgba(255,255,255,0.08); }
        .bg-right { fill: rgba(74, 222, 128, 0.15); }
        .text-left { fill: #e5e7eb; }
        .text-right { fill: #4ade80; }
        .border { stroke: rgba(255,255,255,0.12); }
        .glow { filter: drop-shadow(0 0 8px rgba(74, 222, 128, 0.3)); }
      }
      @media (prefers-color-scheme: light) {
        .bg-left { fill: rgba(0,0,0,0.04); }
        .bg-right { fill: rgba(34, 197, 94, 0.12); }
        .text-left { fill: #374151; }
        .text-right { fill: #16a34a; }
        .border { stroke: rgba(0,0,0,0.08); }
        .glow { filter: drop-shadow(0 0 6px rgba(34, 197, 94, 0.2)); }
      }
      """ else if (theme == "dark") """
        .bg-left { fill: rgba(255,255,255,0.08); }
        .bg-right { fill: rgba(74, 222, 128, 0.15); }
        .text-left { fill: #e5e7eb; }
        .text-right { fill: #4ade80; }
        .border { stroke: rgba(255,255,255,0.12); }
        .glow { filter: drop-shadow(0 0 8px rgba(74, 222, 128, 0.3)); }
      """ else """
        .bg-left { fill: rgba(0,0,0,0.04); }
        .bg-right { fill: rgba(34, 197, 94, 0.12); }
        .text-left { fill: #374151; }
        .text-right { fill: #16a34a; }
        .border { stroke: rgba(0,0,0,0.08); }
        .glow { filter: drop-shadow(0 0 6px rgba(34, 197, 94, 0.2)); }
      """}
    </style>
            """.trimIndent()

            BadgeStyle.NEON -> """
    <style>
      ${if (useMediaQuery) """
      @media (prefers-color-scheme: dark) {
        .outline { stroke: #a855f7; fill: rgba(168, 85, 247, 0.08); }
        .label { fill: #c4b5fd; }
        .value { fill: #e9d5ff; }
        .divider { stroke: #a855f7; }
        .glow { filter: drop-shadow(0 0 12px rgba(168, 85, 247, 0.5)); }
      }
      @media (prefers-color-scheme: light) {
        .outline { stroke: #7c3aed; fill: rgba(124, 58, 237, 0.05); }
        .label { fill: #6d28d9; }
        .value { fill: #5b21b6; }
        .divider { stroke: #7c3aed; }
        .glow { filter: drop-shadow(0 0 8px rgba(124, 58, 237, 0.25)); }
      }
      """ else if (theme == "dark") """
        .outline { stroke: #a855f7; fill: rgba(168, 85, 247, 0.08); }
        .label { fill: #c4b5fd; }
        .value { fill: #e9d5ff; }
        .divider { stroke: #a855f7; }
        .glow { filter: drop-shadow(0 0 12px rgba(168, 85, 247, 0.5)); }
      """ else """
        .outline { stroke: #7c3aed; fill: rgba(124, 58, 237, 0.05); }
        .label { fill: #6d28d9; }
        .value { fill: #5b21b6; }
        .divider { stroke: #7c3aed; }
        .glow { filter: drop-shadow(0 0 8px rgba(124, 58, 237, 0.25)); }
      """}
    </style>
            """.trimIndent()

            BadgeStyle.BRUTALIST -> """
    <style>
      ${if (useMediaQuery) """
      @media (prefers-color-scheme: dark) {
        .bg-main { fill: #18181b; }
        .bg-accent { fill: #fbbf24; }
        .text-label { fill: #fafafa; }
        .text-value { fill: #18181b; }
        .shadow-box { fill: rgba(251, 191, 36, 0.2); }
      }
      @media (prefers-color-scheme: light) {
        .bg-main { fill: #fafafa; }
        .bg-accent { fill: #f59e0b; }
        .text-label { fill: #18181b; }
        .text-value { fill: #fefce8; }
        .shadow-box { fill: rgba(245, 158, 11, 0.15); }
      }
      """ else if (theme == "dark") """
        .bg-main { fill: #18181b; }
        .bg-accent { fill: #fbbf24; }
        .text-label { fill: #fafafa; }
        .text-value { fill: #18181b; }
        .shadow-box { fill: rgba(251, 191, 36, 0.2); }
      """ else """
        .bg-main { fill: #fafafa; }
        .bg-accent { fill: #f59e0b; }
        .text-label { fill: #18181b; }
        .text-value { fill: #fefce8; }
        .shadow-box { fill: rgba(245, 158, 11, 0.15); }
      """}
    </style>
            """.trimIndent()

            BadgeStyle.GRADIENT -> """
    <style>
      ${if (useMediaQuery) """
      @media (prefers-color-scheme: dark) {
        .text-label { fill: #94a3b8; }
        .text-value { fill: #f8fafc; }
        .grad-start { stop-color: #0f172a; }
        .grad-end { stop-color: #06b6d4; }
      }
      @media (prefers-color-scheme: light) {
        .text-label { fill: #475569; }
        .text-value { fill: #f0fdfa; }
        .grad-start { stop-color: #f1f5f9; }
        .grad-end { stop-color: #0891b2; }
      }
      """ else if (theme == "dark") """
        .text-label { fill: #94a3b8; }
        .text-value { fill: #f8fafc; }
        .grad-start { stop-color: #0f172a; }
        .grad-end { stop-color: #06b6d4; }
      """ else """
        .text-label { fill: #475569; }
        .text-value { fill: #f0fdfa; }
        .grad-start { stop-color: #f1f5f9; }
        .grad-end { stop-color: #0891b2; }
      """}
    </style>
    <linearGradient id="bg-gradient" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" class="grad-start"/>
      <stop offset="100%" class="grad-end"/>
    </linearGradient>
            """.trimIndent()

            BadgeStyle.MINIMAL -> """
    <style>
      ${if (useMediaQuery) """
      @media (prefers-color-scheme: dark) {
        .border-line { stroke: rgba(255,255,255,0.2); }
        .text-all { fill: #f5f5f5; }
        .divider { stroke: rgba(255,255,255,0.15); }
      }
      @media (prefers-color-scheme: light) {
        .border-line { stroke: rgba(0,0,0,0.15); }
        .text-all { fill: #171717; }
        .divider { stroke: rgba(0,0,0,0.1); }
      }
      """ else if (theme == "dark") """
        .border-line { stroke: rgba(255,255,255,0.2); }
        .text-all { fill: #f5f5f5; }
        .divider { stroke: rgba(255,255,255,0.15); }
      """ else """
        .border-line { stroke: rgba(0,0,0,0.15); }
        .text-all { fill: #171717; }
        .divider { stroke: rgba(0,0,0,0.1); }
      """}
    </style>
            """.trimIndent()

            BadgeStyle.NEUMORPHIC -> """
    <style>
      ${if (useMediaQuery) """
      @media (prefers-color-scheme: dark) {
        .neu-bg { fill: #1e293b; }
        .shadow-dark { fill: rgba(0,0,0,0.4); }
        .shadow-light { fill: rgba(255,255,255,0.03); }
        .text-main { fill: #e2e8f0; }
        .text-accent { fill: #38bdf8; }
      }
      @media (prefers-color-scheme: light) {
        .neu-bg { fill: #e2e8f0; }
        .shadow-dark { fill: rgba(0,0,0,0.1); }
        .shadow-light { fill: rgba(255,255,255,0.9); }
        .text-main { fill: #334155; }
        .text-accent { fill: #0284c7; }
      }
      """ else if (theme == "dark") """
        .neu-bg { fill: #1e293b; }
        .shadow-dark { fill: rgba(0,0,0,0.4); }
        .shadow-light { fill: rgba(255,255,255,0.03); }
        .text-main { fill: #e2e8f0; }
        .text-accent { fill: #38bdf8; }
      """ else """
        .neu-bg { fill: #e2e8f0; }
        .shadow-dark { fill: rgba(0,0,0,0.1); }
        .shadow-light { fill: rgba(255,255,255,0.9); }
        .text-main { fill: #334155; }
        .text-accent { fill: #0284c7; }
      """}
    </style>
    <filter id="neu-shadow">
      <feGaussianBlur in="SourceAlpha" stdDeviation="3"/>
      <feOffset dx="2" dy="2"/>
    </filter>
            """.trimIndent()
        }
    }
     fun generateBadge(
         badge: Badge,
         x: Int,
         y: Int,
         width: Int,
         height: Int,
         style: BadgeStyle,
         config: BadgeConfig
    ): String {
        val fontFamily = config.fontFamily.ifEmpty {
            when (style) {
                BadgeStyle.GLASSMORPHIC -> "'SF Pro Display', -apple-system, system-ui, sans-serif"
                BadgeStyle.NEON -> "'Outfit', sans-serif"
                BadgeStyle.BRUTALIST -> "'Archivo Black', sans-serif"
                BadgeStyle.GRADIENT -> "'Inter Tight', sans-serif"
                BadgeStyle.MINIMAL -> "'Söhne', 'Helvetica Now', sans-serif"
                BadgeStyle.NEUMORPHIC -> "'DM Sans', sans-serif"
            }
        }

        return when (style) {
            BadgeStyle.GLASSMORPHIC -> generateGlassmorphicBadge(badge, x, y, width, height, fontFamily)
            BadgeStyle.NEON -> generateNeonBadge(badge, x, y, width, height, fontFamily)
            BadgeStyle.BRUTALIST -> generateBrutalistBadge(badge, x, y, width, height, fontFamily)
            BadgeStyle.GRADIENT -> generateGradientBadge(badge, x, y, width, height, fontFamily)
            BadgeStyle.MINIMAL -> generateMinimalBadge(badge, x, y, width, height, fontFamily)
            BadgeStyle.NEUMORPHIC -> generateNeumorphicBadge(badge, x, y, width, height, fontFamily)
        }
    }

    private fun generateGlassmorphicBadge(badge: Badge, x: Int, y: Int, width: Int, height: Int, fontFamily: String): String {
        val leftWidth = (badge.label.length * 8.0) + 15.0
        val rightWidth = width - leftWidth

        return """
      <g transform="translate($x, $y)">
        <rect class="bg-left" x="0" y="0" width="$leftWidth" height="$height" rx="6"/>
        <rect class="bg-right glow" x="$leftWidth" y="0" width="$rightWidth" height="$height" rx="6"/>
        <rect class="border" x="0.5" y="0.5" width="${width - 1}" height="${height - 1}" rx="5.5" fill="none" stroke-width="1"/>
        <text class="text-left" x="${leftWidth / 2}" y="${height / 2 + 4}" text-anchor="middle" font-family="$fontFamily" font-size="12" font-weight="600">${badge.label}</text>
        <text class="text-right" x="${leftWidth + rightWidth / 2}" y="${height / 2 + 4}" text-anchor="middle" font-family="$fontFamily" font-size="12" font-weight="700">${badge.message}</text>
      </g>
            """.trimIndent()
    }

    private fun generateNeonBadge(badge: Badge, x: Int, y: Int, width: Int, height: Int, fontFamily: String): String {
        val labelWidth = (badge.label.length * 7.5) + 20.0
        val valueWidth = width - labelWidth

        return """
            <g transform="translate($x, $y)">
              <rect class="outline glow" x="2" y="2" width="${width - 4}" height="${height - 4}" rx="18" stroke-width="2"/>
              <line class="divider" x1="$labelWidth" y1="8" x2="$labelWidth" y2="${height - 8}" stroke-width="1.5" stroke-linecap="round"/>
              <text class="label" x="${labelWidth / 2}" y="${height / 2 + 3}" text-anchor="middle" font-family="$fontFamily" font-size="11" font-weight="600">${badge.label.uppercase()}</text>
              <text class="value" x="${labelWidth + (valueWidth / 2)}" y="${height / 2 + 3}" text-anchor="middle" font-family="$fontFamily" font-size="12" font-weight="700">${badge.message.uppercase()}</text>
            </g>
            """.trimIndent()
    }

    private fun generateBrutalistBadge(badge: Badge, x: Int, y: Int, width: Int, height: Int, fontFamily: String): String {
        val labelWidth = (badge.label.length * 8.5) + 15.0
        val rightWidth = width - labelWidth

        return """
      <g transform="translate($x, $y)">
        <rect class="shadow-box" x="4" y="4" width="${width - 4}" height="${height - 4}"/>
        <rect class="bg-main" x="0" y="0" width="$labelWidth" height="$height" stroke="#18181b" stroke-width="2"/>
        <rect class="bg-accent" x="$labelWidth" y="0" width="$rightWidth" height="$height" stroke="#18181b" stroke-width="2"/>
        <text class="text-label" x="${labelWidth / 2}" y="${height / 2 + 5}" text-anchor="middle" font-family="$fontFamily" font-size="13" font-weight="900" letter-spacing="1">${badge.label.uppercase()}</text>
        <text class="text-value" x="${labelWidth + rightWidth / 2}" y="${height / 2 + 6}" text-anchor="middle" font-family="$fontFamily" font-size="16" font-weight="900">${badge.message.uppercase()}</text>
      </g>
            """.trimIndent()
    }

    private fun generateGradientBadge(badge: Badge, x: Int, y: Int, width: Int, height: Int, fontFamily: String): String {
        val labelX = 38.0
        val messageX = width - 20.0 // Right-aligned with padding

        return """
      <g transform="translate($x, $y)">
        <rect fill="url(#bg-gradient)" x="0" y="0" width="$width" height="$height" rx="${height / 2}"/>
        <circle cx="20" cy="${height / 2}" r="5" fill="rgba(255,255,255,0.25)"/>
        <text class="text-label" x="$labelX" y="${height / 2 + 4}" font-family="$fontFamily" font-size="11" font-weight="500">${badge.label}</text>
        <text class="text-value" x="$messageX" y="${height / 2 + 5}" text-anchor="end" font-family="$fontFamily" font-size="13" font-weight="700">${badge.message}</text>
      </g>
            """.trimIndent()
    }

    private fun generateMinimalBadge(badge: Badge, x: Int, y: Int, width: Int, height: Int, fontFamily: String): String {
        val labelWidth = (badge.label.length * 7.5) + 15.0
        val messageWidth = width - labelWidth

        return """
      <g transform="translate($x, $y)">
        <rect x="0.5" y="0.5" width="${width - 1}" height="${height - 1}" rx="4" fill="none" class="border-line" stroke-width="1"/>
        <line class="divider" x1="$labelWidth" y1="6" x2="$labelWidth" y2="${height - 6}" stroke-width="1"/>
        <text class="text-all" x="${labelWidth / 2}" y="${height / 2 + 4}" text-anchor="middle" font-family="$fontFamily" font-size="10" font-weight="500" letter-spacing="0.3">${badge.label.uppercase()}</text>
        <text class="text-all" x="${labelWidth + (messageWidth / 2)}" y="${height / 2 + 4}" text-anchor="middle" font-family="$fontFamily" font-size="11" font-weight="600">${badge.message}</text>
      </g>
            """.trimIndent()
    }

    private fun generateNeumorphicBadge(badge: Badge, x: Int, y: Int, width: Int, height: Int, fontFamily: String): String {
        val radius = height / 2

        return """
      <g transform="translate($x, $y)">
        <rect class="shadow-dark" x="4" y="4" width="${width - 4}" height="${height - 4}" rx="$radius" filter="url(#neu-shadow)"/>
        <rect class="neu-bg" x="0" y="0" width="$width" height="$height" rx="$radius"/>
        <rect class="shadow-light" x="2" y="2" width="${width - 4}" height="${height - 4}" rx="${radius - 2}"/>
        <text class="text-main" x="20" y="${height / 2 + 5}" font-family="$fontFamily" font-size="11" font-weight="500">${badge.label}</text>
        <text class="text-accent" x="${width - 20}" y="${height / 2 + 5}" text-anchor="end" font-family="$fontFamily" font-size="13" font-weight="700">${badge.message}</text>
      </g>
            """.trimIndent()
    }
}