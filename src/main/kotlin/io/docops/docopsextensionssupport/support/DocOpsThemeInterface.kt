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
}
