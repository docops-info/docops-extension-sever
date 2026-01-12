package io.docops.docopsextensionssupport.support

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface DocOpsTheme {
    @OptIn(ExperimentalUuidApi::class)
    val id: String
        get() = Uuid.random().toHexString()
    val canvas: String
    val primaryText: String
    val secondaryText: String
    val accentColor: String
    val glassEffect: String
    val surfaceImpact: String
    
    // Aesthetic specific (e.g., Syne for Pro, Arial for Classic)
    val fontFamily: String
    val cornerRadius: Int
}
