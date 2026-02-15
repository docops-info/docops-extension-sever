package io.docops.docopsextensionssupport.qrcode

data class QRTheme(
    val foreground: String = "#000000",
    val background: String = "#FFFFFF",
    val moduleRadius: Double = 0.0,
    val finderStyle: String = "square",
    val useGradient: Boolean = false,
    val gradientColors: Pair<String, String>? = null,
    val backgroundPattern: String? = null,
    val filter: String? = null // Add this for shadows, glows, etc.
)

val neoMinimalTheme = QRTheme(
    foreground = "#000000",
    background = "#FFFFFF",
    moduleRadius = 0.35, // 35% rounded corners
    finderStyle = "rounded-square"
)

val cyberNeonTheme = QRTheme(
    foreground = "#00FFF0", // cyan
    background = "#0D0221", // deep purple-black
    moduleRadius = 0.0,
    finderStyle = "hexagon"
)

val organicWaveTheme = QRTheme(
    foreground = "#2D6A4F", // forest green
    background = "#F1FAEE", // cream
    moduleRadius = 0.5, // fully rounded (dots)
    finderStyle = "flower",
    backgroundPattern = "dots" // using the existing backgroundPattern property
)