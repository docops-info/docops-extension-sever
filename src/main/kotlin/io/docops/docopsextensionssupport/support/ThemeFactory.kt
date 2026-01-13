package io.docops.docopsextensionssupport.support

import io.docops.docopsextensionssupport.button.ButtonDisplay

/**
 * ThemeFactory centralizes the selection of visual aesthetics.
 * This allows us to introduce new "Pro" features (v2) without breaking 
 * the existing look for "Classic" users (v1).
 */
object ThemeFactory {

    fun getTheme(display: VisualDisplay?): DocOpsTheme {
        val isDark = display?.useDark == true
        val version = display?.visualVersion ?: 1

        return when {
            version >= 2 -> {
                if (isDark) ModernDarkTheme() else ModernLightTheme()
            }
            else -> {
                 if (isDark) ProDarkTheme() else ClassicLightTheme()
            }
        }
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
}

/**
 * Modern "Pro" Dark Theme - High impact, sharp accents
 */
class ModernDarkTheme : DocOpsTheme {
    override val canvas = "#020617"
    override val primaryText = "#f8fafc"
    override val secondaryText = "#38bdf8" // Cyan accent
    override val accentColor = "#38bdf8"
    override val glassEffect = "rgba(15, 23, 42, 0.8)"
    override val surfaceImpact = "rgba(56, 189, 248, 0.15)"
    override val fontFamily = "'Syne', sans-serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Syne:wght@800&amp;display=swap');"
    override val fontWidthMultiplier = 1.35f
    override val fontLineHeight = 1.25f // Give Syne some breathing room
    override val cornerRadius = 12
}

/**
 * Modern "Pro" Light Theme - Soft depth, Indigo accents
 */
class ModernLightTheme : DocOpsTheme {
    override val canvas = "#f1f5f9"
    override val primaryText = "#0f172a"
    override val secondaryText = "#4338ca" // Indigo accent
    override val accentColor = "#818cf8"
    override val glassEffect = "rgba(255, 255, 255, 0.7)"
    override val surfaceImpact = "rgba(129, 140, 248, 0.1)"
    override val fontFamily = "'Syne', sans-serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Syne:wght@800&amp;display=swap');"
    override val fontWidthMultiplier = 1.35f
    override val fontLineHeight = 1.25f // Give Syne some breathing room
    override val cornerRadius = 12
}

/**
 * Classic "v1" Dark Theme - Legacy Slate/Indigo
 */
class ClassicDarkTheme : DocOpsTheme {
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
    override val canvas = "#020617"
    override val primaryText = "#ffffff"
    override val secondaryText = "#94a3b8"
    override val accentColor = "#38bdf8"
    override val glassEffect = "rgba(15, 23, 42, 0.9)"
    override val surfaceImpact = "rgba(30, 41, 59, 0.5)"
    override val fontFamily = "'Syne', sans-serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Syne:wght@800&amp;display=swap');"
    override val fontWidthMultiplier = 1.35f
    override val fontLineHeight = 1.25f // Give Syne some breathing room
    override val cornerRadius = 0 // Sharp edges for brutalist
}

/**
 * Pro "v2" Dark Theme - High contrast, sharp accents, unique typography
 */
class ProDarkTheme : DocOpsTheme {
    override val canvas = "#0f172a" // Deep Slate
    override val primaryText = "#f8fafc"
    override val secondaryText = "#22d3ee"
    override val accentColor = "#38bdf8" // Sky Blue accent
    override val glassEffect = "rgba(15, 23, 42, 0.8)"
    override val surfaceImpact = "rgba(56, 189, 248, 0.1)"
    override val fontFamily = "'Syne', sans-serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Syne:wght@800&amp;display=swap');"
    override val fontWidthMultiplier = 1.35f
    override val fontLineHeight = 1.25f // Give Syne some breathing room
    override val cornerRadius = 12
}

class BrutalistLightTheme : DocOpsTheme {
    override val canvas = "#f8fafc"
    override val primaryText = "#0f172a"
    override val secondaryText = "#475569"
    override val accentColor = "#6366f1"
    override val glassEffect = "rgba(255, 255, 255, 0.9)"
    override val surfaceImpact = "rgba(226, 232, 240, 0.5)"
    override val fontFamily = "'Syne', sans-serif"
    override val fontImport = "@import url('https://fonts.googleapis.com/css2?family=Syne:wght@800&amp;display=swap');"
    override val fontWidthMultiplier = 1.35f
    override val fontLineHeight = 1.25f // Give Syne some breathing room
    override val cornerRadius = 0
}