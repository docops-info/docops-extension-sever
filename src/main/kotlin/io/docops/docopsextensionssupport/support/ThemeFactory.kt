package io.docops.docopsextensionssupport.support

/**
 * ThemeFactory centralizes the selection of visual aesthetics.
 * This allows us to introduce new "Pro" features (v2) without breaking 
 * the existing look for "Classic" users (v1).
 */
object ThemeFactory {
    fun getTheme(display: VisualDisplay?): DocOpsTheme {
        val isDark = display?.useDark == true
        val version = display?.visualVersion ?: 1

        val theme = when (version) {
            4 -> {
                if(isDark) ProDarkTheme() else HexLightTheme()
            }
            3 -> {
                if (isDark) CyberDarkTheme() else CyberLightTheme()
            }
            2 -> {
                if (isDark) ModernDarkTheme() else ModernLightTheme()
            }
            else -> {
                if (isDark) ProDarkTheme() else ClassicLightTheme()
            }
        }
        return theme
    }
    fun getTheme(useDark: Boolean): DocOpsTheme {
        return if (useDark) ProDarkTheme() else ClassicLightTheme()
    }

    /**
     * Strategic Theme Selection based on Versioning
     * Version 1: Classic (Clean, Arial)
     * Version 2: Cyber-Brutalist (Sharp, Syne, High Impact)
     */
    fun getTheme(useDark: Boolean, isBrutalist: Boolean): DocOpsTheme {
        return if (isBrutalist) {
            if (useDark) BrutalistDarkTheme() else BrutalistLightTheme()
        } else {
            if (useDark) ClassicDarkTheme() else ClassicLightTheme()
        }
    }

    /**
     * Get theme by name string
     * @param themeName Name of the theme (e.g., "TallinnTheme", "AutumnTheme", "classic", "brutalist")
     * @param useDark Fallback to dark/light if theme name is not found
     * @return The requested theme or a default theme
     */
    fun getThemeByName(themeName: String, useDark: Boolean = false): DocOpsTheme {
        return when (themeName.lowercase()) {
            "ayu", "ayutheme" -> if(useDark) AyuDarkTheme() else AyuLightTheme()
            "signal", "signaltheme" -> if (useDark) SignalDarkTheme() else SignalLightTheme()
            "brand", "brandtheme" -> if (useDark) BrandDarkTheme() else BrandLightTheme()
            "tallinn", "tallinntheme" -> if(useDark)ProDarkTheme() else TallinnTheme()
            "autumn", "autumntheme" -> if(useDark)ProDarkTheme() else AutumnTheme()
            "istanbul", "istanbultheme" -> if(useDark)TokyoDarkTheme() else IstanbulTheme()
            "everest", "everesttheme" -> if(useDark)TokyoDarkTheme() else EverestTheme()
            "reo", "reotheme" -> if(useDark)AuroraTheme() else ReoTheme()
            "sakura", "sakuratheme" -> if(useDark)TokyoDarkTheme() else SakuraTheme()
            "tokyo", "tokyotheme" -> if(useDark)TokyoDarkTheme() else ModernLightTheme()
            "aurora", "auroratheme" -> if(useDark)AuroraTheme() else ModernLightTheme()
            "hex", "hexlighttheme" ->  HexLightTheme()
            "cyberlight", "cyberlighttheme" -> CyberLightTheme()
            "cyberdark", "cyberdarktheme" -> CyberDarkTheme()
            "brutalistlight", "brutalistlighttheme" -> BrutalistLightTheme()
            "brutalistdark", "brutalistdarktheme" -> BrutalistDarkTheme()
            "modernlight", "modernlighttheme" -> ModernLightTheme()
            "moderndark", "moderndarktheme" -> ModernDarkTheme()
            "classiclight", "classiclighttheme" -> ClassicLightTheme()
            "classicdark", "classicdarktheme" -> ClassicDarkTheme()
            "prodark", "prodarktheme" -> ProDarkTheme()
            "classic" -> if (useDark) ClassicDarkTheme() else ClassicLightTheme()
            "brutalist" -> if (useDark) BrutalistDarkTheme() else BrutalistLightTheme()
            "modern" -> if (useDark) ModernDarkTheme() else ModernLightTheme()
            "cyber" -> if (useDark) CyberDarkTheme() else CyberLightTheme()
            "pro" -> if (useDark) ProDarkTheme() else ClassicLightTheme()
            "food" -> FoodTheme()
            "spring" -> SpringTheme()
            "summer" -> SummerTheme()
            "guyana", "goldenarrowhead", "goldenarrowheadtheme" -> GoldenArrowheadTheme()
            else -> if (useDark) ProDarkTheme() else ClassicLightTheme()
        }
    }
}

/**
 * Golden Arrowhead Theme — Guyana-inspired recipe theme
 * Palette: rainforest green, Demerara gold, pepper pot red, cassava cream
 */
class GoldenArrowheadTheme : DocOpsTheme {
    override val name = "GoldenArrowheadTheme"
    override val canvas = "#faf3e2"           // cassava cream
    override val primaryText = "#1a110a"       // burnt wood charcoal
    override val secondaryText = "#1b4332"     // deep Essequibo rainforest
    override val accentColor = "#e9a11a"       // flag gold / ripe mango
    override val glassEffect = "rgba(250, 243, 226, 0.8)"
    override val surfaceImpact = "rgba(27, 67, 50, 0.10)"
    override val fontFamily = "'Cormorant Garamond', Georgia, serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,600;0,700;1,400;1,600&amp;family=Jost:wght@400;500;600&amp;family=Jost:ital@1&amp;display=swap');"
    override val fontWidthMultiplier = 1.0f
    override val fontLineHeight = 1.2f
    override val cornerRadius = 18
    override val chartPalette: List<SVGColor>
        get() = listOf(
            SVGColor("#1b4332"), SVGColor("#2d6a4f"), SVGColor("#b8730a"),
            SVGColor("#e9a11a"), SVGColor("#8c1a1e"), SVGColor("#6b5437"),
            SVGColor("#f0e4c8"), SVGColor("#3a2710")
        )
}

/**
 * Food/Recipe Theme - Warm terracotta, parchment, Georgia serif
 */
class FoodTheme : DocOpsTheme {
    override val name = "FoodTheme"
    override val canvas = "#FBF5EC"
    override val primaryText = "#3D1A08"
    override val secondaryText = "#7A3010"
    override val accentColor = "#B05C28"
    override val glassEffect = "rgba(251, 245, 236, 0.8)"
    override val surfaceImpact = "rgba(176, 92, 40, 0.1)"
    override val fontFamily = "Georgia, serif"
    override val fontImport = ""
    override val fontWidthMultiplier = 1.0f
    override val fontLineHeight = 1.2f
    override val cornerRadius = 6
}

/**
 * Spring Theme - Cherry blossom pink, mint green, ivory blush
 */
class SpringTheme : DocOpsTheme {
    override val name = "SpringTheme"
    override val canvas = "#FDF6F9"
    override val primaryText = "#7A2848"
    override val secondaryText = "#8C5870"
    override val accentColor = "#D4789A"
    override val glassEffect = "rgba(253, 246, 249, 0.8)"
    override val surfaceImpact = "rgba(212, 120, 154, 0.1)"
    override val fontFamily = "Georgia, serif"
    override val fontImport = ""
    override val fontWidthMultiplier = 1.0f
    override val fontLineHeight = 1.2f
    override val cornerRadius = 8
}

/**
 * Summer Theme - Mediterranean cobalt blue, sunflower yellow, bright white
 */
class SummerTheme : DocOpsTheme {
    override val name = "SummerTheme"
    override val canvas = "#F7FBFE"
    override val primaryText = "#0A3870"
    override val secondaryText = "#1058A0"
    override val accentColor = "#1568B0"
    override val glassEffect = "rgba(247, 251, 254, 0.8)"
    override val surfaceImpact = "rgba(21, 104, 176, 0.1)"
    override val fontFamily = "Georgia, serif"
    override val fontImport = ""
    override val fontWidthMultiplier = 1.0f
    override val fontLineHeight = 1.2f
    override val cornerRadius = 8
}

/**
 * Modern "Pro" Dark Theme - High impact, sharp accents
 */
open class ModernDarkTheme : DocOpsTheme {
    override val name = "ModernDarkTheme"
    override val canvas = "#020617"
    override val primaryText = "#f8fafc"
    override val secondaryText = "#38bdf8" // Cyan accent
    override val accentColor = "#38bdf8"
    override val glassEffect = "rgba(15, 23, 42, 0.8)"
    override val surfaceImpact = "rgba(56, 189, 248, 0.15)"
    override val fontFamily = "'Archivo', -apple-system, sans-serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@800&amp;display=swap');"
    override val fontWidthMultiplier = 1.15f
    override val fontLineHeight = 1.25f // Give Syne some breathing room
    override val cornerRadius = 12
}

/**
 * Modern "Pro" Light Theme - Soft depth, Indigo accents
 */
class ModernLightTheme : DocOpsTheme {
    override val name = "ModernLightTheme"
    override val canvas = "#f1f5f9"
    override val primaryText = "#0f172a"
    override val secondaryText = "#4338ca" // Indigo accent
    override val accentColor = "#818cf8"
    override val glassEffect = "rgba(255, 255, 255, 0.7)"
    override val surfaceImpact = "rgba(129, 140, 248, 0.1)"
    override val fontFamily = "'Archivo', -apple-system, sans-serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@800&amp;display=swap');"
    override val fontWidthMultiplier = 1.15f
    override val fontLineHeight = 1.25f // Give Syne some breathing room
    override val cornerRadius = 12
}

/**
 * Classic "v1" Dark Theme - Legacy Slate/Indigo
 */
class ClassicDarkTheme : DocOpsTheme {
    override val name = "ClassicDarkTheme"
    override val canvas = "#1e293b"
    override val primaryText = "#ffffff"
    override val secondaryText = "#94a3b8"
    override val accentColor = "#6366f1"
    override val glassEffect = "rgba(30, 41, 59, 0.5)"
    override val surfaceImpact = "rgba(255, 255, 255, 0.05)"
    override val fontFamily = "Arial, Helvetica, sans-serif"
    override val fontImport = ""
    override val fontWidthMultiplier: Float = 1.0f
    override val fontLineHeight = 1.1f // Standard leading
    override val cornerRadius = 4
}

/**
 * Classic "v1" Light Theme - Standard Clean Look
 */
class ClassicLightTheme : DocOpsTheme {
    override val name = "ClassicLightTheme"
    override val canvas = "#ffffff"
    override val primaryText = "#111111"
    override val secondaryText = "#4338ca"
    override val accentColor = "#2563eb"
    override val glassEffect = "rgba(255, 255, 255, 0.9)"
    override val surfaceImpact = "rgba(0, 0, 0, 0.05)"
    override val fontFamily = "Arial, Helvetica, sans-serif"
    override val fontImport = ""
    override val fontWidthMultiplier: Float = 1.0f
    override val fontLineHeight = 1.1f // Standard leading
    override val cornerRadius = 4

}

class BrutalistDarkTheme : DocOpsTheme {
    override val name = "BrutalistDarkTheme"
    override val canvas = "#020617"
    override val primaryText = "#ffffff"
    override val secondaryText = "#94a3b8"
    override val accentColor = "#38bdf8"
    override val glassEffect = "rgba(15, 23, 42, 0.9)"
    override val surfaceImpact = "rgba(30, 41, 59, 0.5)"
    override val fontFamily = "'Archivo', -apple-system, sans-serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@400;600;700;800&amp;display=swap');"
    override val fontWidthMultiplier = 1.15f
    override val fontLineHeight = 1.25f // Give Syne some breathing room
    override val cornerRadius = 0 // Sharp edges for brutalist
}

/**
 * Pro "v2" Dark Theme - High contrast, sharp accents, unique typography
 */
class ProDarkTheme : DocOpsTheme {
    override val name = "ProDarkTheme"
    override val canvas = "#0f172a" // Deep Slate
    override val primaryText = "#f8fafc"
    override val secondaryText = "#22d3ee"
    override val accentColor = "#38bdf8" // Sky Blue accent
    override val glassEffect = "rgba(15, 23, 42, 0.8)"
    override val surfaceImpact = "rgba(56, 189, 248, 0.1)"
    override val fontFamily = "'Archivo', -apple-system, sans-serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@400;600;700;800&amp;display=swap');"
    override val fontWidthMultiplier = 1.15f
    override val fontLineHeight = 1.25f
    override val cornerRadius = 12
}

class BrutalistLightTheme : DocOpsTheme {
    override val name = "BrutalistLightTheme"
    override val canvas = "#f8fafc"
    override val primaryText = "#0f172a"
    override val secondaryText = "#475569"
    override val accentColor = "#6366f1"
    override val glassEffect = "rgba(255, 255, 255, 0.9)"
    override val surfaceImpact = "rgba(226, 232, 240, 0.5)"
    override val fontFamily = "'Archivo', -apple-system, sans-serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@800&amp;display=swap');"
    override val fontWidthMultiplier = 1.15f
    override val fontLineHeight = 1.25f // Give Syne some breathing room
    override val cornerRadius = 0
}

/**
 * Cyber-Brutalist "v3" themes
 */
open class CyberDarkTheme : ModernDarkTheme() {
    override val name = "CyberDarkTheme"
    override val canvas = "#0a0e27" // Deep Midnight Blue
    override val primaryText = "#f8fafc"
    override val secondaryText = "#38bdf8"
    override val accentColor = "#6366f1"
    override val surfaceImpact = "rgba(99, 102, 241, 0.2)"
}

open class CyberLightTheme : DocOpsTheme {
    override val name = "CyberLightTheme"
    override val canvas = "#fafbfc"
    override val primaryText = "#0f172a"
    override val secondaryText = "#475569"
    override val accentColor = "#6366f1"
    override val glassEffect = "rgba(255, 255, 255, 0.9)"
    override val surfaceImpact = "rgba(99, 102, 241, 0.1)"
    override val fontFamily = "'Archivo', -apple-system, sans-serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@800&amp;display=swap');"
    override val fontWidthMultiplier = 1.15f
    override val fontLineHeight = 1.25f
    override val cornerRadius = 16
}

class HexLightTheme: CyberLightTheme() {
    override val name = "HexLightTheme"
    override val primaryText = "#f8fafc"
    override val secondaryText = "#38bdf8"
    override val accentColor = "#6366f1"
    override val surfaceImpact = "rgba(99, 102, 241, 0.2)"
}

class TallinnTheme: CyberLightTheme() {
    override val name = "TallinnTheme"
    override val canvas = "#D0DCEF"
    override val primaryText = "#425EB8"
    override val secondaryText = "#81549C"
    override val accentColor = "#324979"
    override val surfaceImpact = "rgba(99, 102, 241, 0.2)"
    override val chartPalette = listOf(
        SVGColor("#425EB8"),  // Royal Blue (matches primary - for North America 35%)
        SVGColor("#81549C"),  // Amethyst Purple (matches secondary - for Europe 28%)
        SVGColor("#5B8DBE"),  // Baltic Sea Blue (for Asia Pacific 22%)
        SVGColor("#7FA8C9"),  // Powder Blue (for Latin America 10%)
        SVGColor("#324979"),  // Navy (matches accent - for Middle East & Africa 5%)
        SVGColor("#6B7BA8"),  // Slate Blue
        SVGColor("#8E9CC6"),  // Periwinkle
        SVGColor("#4A6FA5")   // Cobalt
    )
}

class AutumnTheme: CyberLightTheme() {
    override val name = "AutumnTheme"
    override val canvas = "#F4D3BD"
    override val primaryText = "#A87F25"
    override val secondaryText = "#B0633A"
    override val accentColor = "#BE553E"
    override val surfaceImpact = "rgba(217, 163, 40, 0.2)"
    override val chartPalette = listOf(
        SVGColor("#BE553E"),  // Rust Red (matches your accent - for North America 35%)
        SVGColor("#B0633A"),  // Burnt Orange (matches secondary - for Europe 28%)
        SVGColor("#D4A05E"),  // Golden Yellow (for Asia Pacific 22%)
        SVGColor("#E8B54D"),  // Amber Gold (for Latin America 10%)
        SVGColor("#8B4E3C"),  // Deep Brown (for Middle East & Africa 5%)
        SVGColor("#C17F4A"),  // Copper
        SVGColor("#A67F4B"),  // Bronze
        SVGColor("#D9A95B")   // Harvest Gold
    )
}

open class TokyoDarkTheme: CyberDarkTheme() {
    override val name = "TokyoDarkTheme"
    override val canvas = "#2A3051"
    override val primaryText = "#ECA1EB"
    override val secondaryText = "#5CC19D"
    override val accentColor = "#7285DC"
    override val surfaceImpact = "rgba(99, 102, 241, 0.2)"
    override val chartPalette = listOf(
        SVGColor("#ECA1EB"),  // Neon Pink (matches primary - for North America 35%)
        SVGColor("#5CC19D"),  // Cyber Mint (matches secondary - for Europe 28%)
        SVGColor("#7285DC"),  // Electric Lavender (matches accent - for Asia Pacific 22%)
        SVGColor("#FF6B9D"),  // Hot Pink (for Latin America 10%)
        SVGColor("#4ECDC4"),  // Aqua Cyan (for Middle East & Africa 5%)
        SVGColor("#A8E6CF"),  // Pastel Green
        SVGColor("#FFB7CE"),  // Bubblegum Pink
        SVGColor("#9D84F2")   // Violet Glow
    )
}

class IstanbulTheme: CyberDarkTheme() {
    override val name = "IstanbulTheme"
    override val canvas        = "#DBF0F1" // Window Background
    override val primaryText   = "#1190A1" // Keywords
    override val secondaryText = "#0887B5" // Strings
    override val accentColor   = "#C16979" // Tags
    override val surfaceImpact = "rgba(163, 221, 229, 0.40)"
    override val glassEffect = "rgba(250, 253, 253, 0.88)" // Editor Background #FAFDFD
    override val chartPalette = listOf(
        SVGColor("#1190A1", "Keyword Teal"),     // Keywords
        SVGColor("#0887B5", "String Blue"),      // Strings
        SVGColor("#C16979", "Tag Rose"),         // Tags
        SVGColor("#B87958", "Attribute Clay"),   // Attributes
        SVGColor("#B6514D", "Parameter Brick"),  // Parameters
        SVGColor("#9C6E7C", "Metadata Mauve"),   // Metadata
        SVGColor("#A3DDE5", "Selection Aqua"),   // Selection Background (bright fill)
        SVGColor("#EFEAD0", "Active Sand")       // Active Background (warm light)
    )
}

class SakuraTheme: CyberLightTheme() {
    override val name = "SakuraTheme"
    override val canvas = "#F8DBE6"
    override val primaryText = "#98556C"
    override val secondaryText = "#CB6B91"
    override val accentColor = "#607FA9"
    override val surfaceImpact = "rgba(230, 159, 0, 0.2)"
    override val chartPalette = listOf(
        SVGColor("#CB6B91"),  // Cherry Blossom Pink (matches secondary)
        SVGColor("#607FA9"),  // Serene Blue (matches accent)
        SVGColor("#98556C"),  // Mauve (matches primary)
        SVGColor("#E199B3"),  // Light Pink
        SVGColor("#7A99BD"),  // Powder Blue
        SVGColor("#B37589"),  // Dusty Rose
        SVGColor("#4D6A8F"),  // Slate Blue
        SVGColor("#D4A3B8")   // Pale Pink
    )
}

class EverestTheme: CyberLightTheme() {
    override val name = "EverestTheme"
    override val canvas = "#FDFEFF"
    override val primaryText = "#8C4069"
    override val secondaryText = "#1A6D9F"
    override val accentColor = "#007A47"
    override val surfaceImpact = "rgba(26, 109, 159, 0.12)"
    override val chartPalette = listOf(
        SVGColor("#1A6D9F", "Summit Blue"),        // Deep sky blue from secondaryText
        SVGColor("#007A47", "Forest Green"),       // Alpine forest from accentColor
        SVGColor("#8C4069", "Rhododendron Pink"),  // Mountain flower from primaryText
        SVGColor("#E69F00", "Sunrise Gold"),       // Golden hour extracted from surfaceImpact
        SVGColor("#56B4E9", "Glacier Ice"),        // Bright glacier blue
        SVGColor("#009E73", "Valley Moss"),        // Rich moss green
        SVGColor("#D55E00", "Sunset Copper"),      // Warm sunset tone
        SVGColor("#0072B2", "Deep Lake"),          // Mountain lake blue
        SVGColor("#CC79A7", "Alpine Rose"),        // Soft mountain flower
        SVGColor("#F0E442", "Snow Reflection")     // Bright snow highlight
    )
}

class ReoTheme: CyberLightTheme() {
    override val name = "ReoTheme"
    override val canvas = "#F7FAF6"
    override val primaryText = "#5F9849"
    override val secondaryText = "#247A4C"
    override val accentColor = "#007A47"
    override val surfaceImpact = "rgba(0, 122, 71, 0.12)"
    override val chartPalette = listOf(
        SVGColor("#007A47"),  // Evergreen (accent)
        SVGColor("#247A4C"),  // Deep Leaf (secondary)
        SVGColor("#5F9849"),  // Meadow (primary)
        SVGColor("#8BBF6A"),  // Spring Green
        SVGColor("#B9D9A6"),  // Sage Mist
        SVGColor("#4E7D3A"),  // Forest Shadow
        SVGColor("#6FB38E"),  // Soft Teal
        SVGColor("#A7C68A")   // Fern Light
    )
}

class AuroraTheme: TokyoDarkTheme() {
    override val name = "AuroraTheme"
    override val canvas = "#142B37"
    override val primaryText = "#73D379"
    override val secondaryText = "#05C0A6"
    override val accentColor = "#BB719B"
    override val surfaceImpact = "rgba(5, 192, 166, 0.18)"
    override val chartPalette = listOf(
        SVGColor("#73D379", "Aurora Green"),
        SVGColor("#05C0A6", "Glacier Teal"),
        SVGColor("#BB719B", "Solar Flare"),
        SVGColor("#38BDF8", "Ice Blue"),
        SVGColor("#A3E635", "Lime Ribbon"),
        SVGColor("#94A3B8", "Moon Haze"),     // new: cool neutral for separation on dark
        SVGColor("#60A5FA", "Arctic Sky"),
        SVGColor("#8B5CF6", "Twilight Violet")
    )
}
    /**
     * Brand Light Theme - Based on branding color #335D79
     * Clean, airy surfaces with a warm “signal” accent for contrast.
     */
    class BrandLightTheme : DocOpsTheme {
        override val name = "BrandLightTheme"
        override val canvas = "#F6F9FC"
        override val primaryText = "#0F2230"
        override val secondaryText = "#335D79" // brand-forward
        override val accentColor = "#E39A3B"   // warm accent to avoid “all-blue” UI
        override val glassEffect = "rgba(255, 255, 255, 0.78)"
        override val surfaceImpact = "rgba(51, 93, 121, 0.14)"
        override val fontFamily = "'Archivo', -apple-system, sans-serif"
        override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@400;700;800&amp;display=swap');"
        override val fontWidthMultiplier = 1.12f
        override val fontLineHeight = 1.24f
        override val cornerRadius = 14
        override val chartPalette = listOf(
            SVGColor("#335D79"), // Brand Ocean
            SVGColor("#2B5169"), // Deepened Brand
            SVGColor("#4D7A98"), // Harbor Blue
            SVGColor("#7FB0CD"), // Sky Tint
            SVGColor("#1E3A4D"), // Ink
            SVGColor("#E39A3B"), // Signal Amber
            SVGColor("#C56F3A"), // Burnt Copper
            SVGColor("#2F7A6B")  // Sea Glass Teal
        )
    }

    /**
     * Brand Dark Theme - Based on branding color #335D79
     * Inky navy canvas, readable “lifted brand” accents, warm highlight accent.
     */
    class BrandDarkTheme : DocOpsTheme {
        override val name = "BrandDarkTheme"
        override val canvas = "#071925"
        override val primaryText = "#EAF3F8"
        override val secondaryText = "#7FB0CD" // lifted brand tint for dark UI
        override val accentColor = "#F2B45B"
        override val glassEffect = "rgba(11, 34, 50, 0.78)"
        override val surfaceImpact = "rgba(127, 176, 205, 0.16)"
        override val fontFamily = "'Archivo', -apple-system, sans-serif"
        override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@400;700;800&amp;display=swap');"
        override val fontWidthMultiplier = 1.12f
        override val fontLineHeight = 1.24f
        override val cornerRadius = 14
        override val chartPalette = listOf(
            SVGColor("#7FB0CD"), // Lifted Brand
            SVGColor("#335D79"), // Brand Anchor
            SVGColor("#9BC9DF"), // Ice Blue
            SVGColor("#4D7A98"), // Harbor Blue
            SVGColor("#1E3A4D"), // Ink
            SVGColor("#F2B45B"), // Warm Highlight
            SVGColor("#FFCF8A"), // Pale Gold
            SVGColor("#3AAE9F")  // Luminous Teal
        )
    }
/**
 * DocOps Signal — Chart-first Light Theme
 *
 * Tuned for: ModernLight canvas + Signal 6-series palette
 * Goal: decision-ready docs (clear, restrained, high contrast)
 */
class SignalLightTheme : DocOpsTheme {
    override val name = "SignalLightTheme"

    // Canvas aligned to ModernLight recommendation
    override val canvas = "#f1f5f9"
    override val primaryText = "#0f172a"
    override val secondaryText = "#2A3447" // Slate-ish supporting text
    override val accentColor = "#00B7D6"   // Signal Cyan (reserved, strong)

    override val glassEffect = "rgba(255, 255, 255, 0.75)"
    override val surfaceImpact = "rgba(11, 18, 32, 0.06)"

    // Keep typography consistent with existing modern themes in this project
    override val fontFamily = "'Archivo', -apple-system, sans-serif"
    override val fontImport =
        "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@400;600;700;800&amp;display=swap');"
    override val fontWidthMultiplier = 1.15f
    override val fontLineHeight = 1.25f
    override val cornerRadius = 12

    // Signal Palette 6 (purpose-built for multi-series line charts)
    override val chartPalette = listOf(
        SVGColor("#00B7D6", "Signal Cyan"),
        SVGColor("#2563EB", "Royal Blue"),
        SVGColor("#16A34A", "Emerald"),
        SVGColor("#D97706", "Amber"),
        SVGColor("#7C3AED", "Violet"),
        SVGColor("#DB2777", "Rose")
    )
}

/**
 * DocOps Signal — Chart-first Dark Theme
 *
 * Tuned for: Aurora canvas + Signal 6-series palette
 * Goal: high legibility on deep teal canvas without glow/pattern reliance.
 */
class SignalDarkTheme : DocOpsTheme {
    override val name = "SignalDarkTheme"

    // Canvas aligned to Aurora recommendation
    override val canvas = "#142B37"
    override val primaryText = "#EAF3F8"
    override val secondaryText = "#B7C2D9"
    override val accentColor = "#00B7D6" // Signal Cyan

    override val glassEffect = "rgba(20, 43, 55, 0.72)"
    override val surfaceImpact = "rgba(0, 183, 214, 0.14)"

    override val fontFamily = "'Archivo', -apple-system, sans-serif"
    override val fontImport =
        "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@400;600;700;800&amp;display=swap');"
    override val fontWidthMultiplier = 1.15f
    override val fontLineHeight = 1.25f
    override val cornerRadius = 12

    override val chartPalette = listOf(
        SVGColor("#00B7D6", "Signal Cyan"),
        SVGColor("#2563EB", "Royal Blue"),
        SVGColor("#16A34A", "Emerald"),
        SVGColor("#D97706", "Amber"),
        SVGColor("#7C3AED", "Violet"),
        SVGColor("#DB2777", "Rose")
    )
}

class AyuLightTheme: DocOpsTheme {
    override val name: String = "AyuLightTheme"
    override val canvas: String = "#fcfcfc"
    override val primaryText: String = "#f07171"
    override val secondaryText: String = "#fa8532"
    override val accentColor: String = "#a37acc"
    override val glassEffect: String = "#86b300"
    override val surfaceImpact: String = "#55b4d4"
    override val fontFamily = "'Archivo', -apple-system, sans-serif"
    override val fontImport =
        "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@400;600;700;800&amp;display=swap');"
    override val fontWidthMultiplier = 1.15f
    override val fontLineHeight = 1.25f
    override val cornerRadius = 12
}

class AyuDarkTheme: DocOpsTheme {
    override val name: String = "AyuDarkTheme"
    override val canvas: String = "#10141c"
    override val primaryText: String = "#f29668"
    override val secondaryText: String = "#e6c08a"
    override val accentColor: String = "#aad94c"
    override val glassEffect: String = "#95e6cb"
    override val surfaceImpact: String = "#39bae6"
    override val fontFamily = "'Archivo', -apple-system, sans-serif"
    override val fontImport =
        "@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@400;600;700;800&amp;display=swap');"
    override val fontWidthMultiplier = 1.15f
    override val fontLineHeight = 1.25f
    override val cornerRadius = 12
}
/*
#f07171,
#f2a191,
#fa8532,
#eba400,
#e59645,
#a37acc,
#86b300,
#4cbf99,
#22a4e6,
#55b4d4
 */