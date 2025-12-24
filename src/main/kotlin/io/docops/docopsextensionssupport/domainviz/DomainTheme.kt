package io.docops.docopsextensionssupport.domainviz

interface DomainTheme {
    val bgStart: String
    val bgEnd: String
    val cardBg: String
    val textPrimary: String
    val textSecondary: String
    val accentPrimary: String // Purple/Action
    val accentSuccess: String // Teal/Link
    val strokeOpacity: String
    val font: String get() = "'Space Grotesk', sans-serif"
}

class DomainDarkTheme : DomainTheme {
    override val bgStart = "#020617"
    override val bgEnd = "#0F172A"
    override val cardBg = "#1E293B"
    override val textPrimary = "#F8FAFC"
    override val textSecondary = "#94A3B8"
    override val accentPrimary = "#A855F7"
    override val accentSuccess = "#2DD4BF"
    override val strokeOpacity = "0.2"
}

class DomainLightTheme : DomainTheme {
    override val bgStart = "#F8FAFC"
    override val bgEnd = "#F1F5F9"
    override val cardBg = "#FFFFFF"
    override val textPrimary = "#0F172A"
    override val textSecondary = "#475569"
    override val accentPrimary = "#7C3AED"
    override val accentSuccess = "#059669"
    override val strokeOpacity = "0.1"
}