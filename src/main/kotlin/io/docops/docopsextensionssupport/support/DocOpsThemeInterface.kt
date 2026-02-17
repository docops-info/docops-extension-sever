package io.docops.docopsextensionssupport.support

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface DocOpsTheme {
    @OptIn(ExperimentalUuidApi::class)
    val id: String
        get() = Uuid.random().toHexString()
    val name: String
    val canvas: String
    val primaryText: String
    val secondaryText: String
    val accentColor: String
    val glassEffect: String
    val surfaceImpact: String
    
    // Aesthetic specific (e.g., Syne for Pro, Arial for Classic)
    val fontFamily: String
    val fontImport: String
    val cornerRadius: Int
    val fontWidthMultiplier: Float // 1.0 for Arial, 1.35 for Syne
    val fontLineHeight: Float // New: 1.0 for standard, 1.2+ for designer fonts
    val chartPalette: List<SVGColor>
        get() = listOf(
        SVGColor("#e60049"), SVGColor("#0bb4ff"), SVGColor("#50e991"),
        SVGColor("#e6d800"), SVGColor("#9b19f5"), SVGColor("#ffa300"),
        SVGColor("#dc0ab4"), SVGColor("#00bfa0")
        )
}
