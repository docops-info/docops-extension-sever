package io.docops.docopsextensionssupport.support

interface DocOpsTheme {
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
