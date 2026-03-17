package io.docops.docopsextensionssupport.badge

data class BadgeConfig(
    var type: String = "classic",
    var theme: String = "auto", // auto, light, dark, both
    var spacing: Int = 2,
    var fontFamily: String = "",
    var direction: String = "horizontal", // vertical or horizontal
    var perRow: Int = 5
)

enum class BadgeStyle {
    GLASSMORPHIC,
    NEON,
    BRUTALIST,
    GRADIENT,
    MINIMAL,
    NEUMORPHIC

}
