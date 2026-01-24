package io.docops.docopsextensionssupport.chart

object ColorPaletteFactory {

    enum class PaletteType {
        CORPORATE,
        MODERN_TECH,
        TABLEAU,
        MATERIAL,
        PASTEL,
        MONOCHROMATIC_BLUE,
        SUNSET_VIBRANT,
        FOREST_DEPTHS,
        OCEAN_BREEZE,
        COSMIC_NEBULA,
        URBAN_NIGHT,
        TROPICAL_PARADISE
    }

    private val palettes = mapOf(
        PaletteType.CORPORATE to listOf(
            "#2E5090", "#4472C4", "#70AD47", "#FFC000", "#ED7D31", "#A5A5A5"
        ),
        PaletteType.MODERN_TECH to listOf(
            "#0F172A", "#3B82F6", "#8B5CF6", "#EC4899", "#F59E0B", "#10B981"
        ),
        PaletteType.TABLEAU to listOf(
            "#4E79A7", "#F28E2B", "#E15759", "#76B7B2", "#59A14F",
            "#EDC948", "#B07AA1", "#FF9DA7", "#9C755F", "#BAB0AC"
        ),
        PaletteType.MATERIAL to listOf(
            "#1E88E5", "#43A047", "#FB8C00", "#E53935", "#8E24AA", "#00ACC1"
        ),
        PaletteType.PASTEL to listOf(
            "#A8DADC", "#457B9D", "#F1FAEE", "#E63946", "#1D3557", "#FFB703"
        ),
        PaletteType.MONOCHROMATIC_BLUE to listOf(
            "#1A365D", "#2D5F8D", "#4682B4", "#6CA6CD", "#9AC5E0", "#C4E1F5"
        ),

        // New vibrant, interesting palettes for charts
        PaletteType.SUNSET_VIBRANT to listOf(
            "#FF6B35", "#F7931E", "#FFD23F", "#06FFA5", "#8338EC", "#FF0080"
        ),
        PaletteType.FOREST_DEPTHS to listOf(
            "#2D5A27", "#4A7C59", "#6B9E78", "#8FC0A9", "#C8D5B9", "#E8F4F8"
        ),
        PaletteType.OCEAN_BREEZE to listOf(
            "#006D77", "#83C5BE", "#EDF6F9", "#FFDDD2", "#E29578", "#006D77"
        ),
        PaletteType.COSMIC_NEBULA to listOf(
            "#240046", "#7209B7", "#560BAD", "#480CA8", "#3A0CA3", "#4361EE"
        ),
        PaletteType.URBAN_NIGHT to listOf(
            "#0D1B2A", "#1B263B", "#415A77", "#778DA9", "#C9ADA7", "#F4E9CD"
        ),
        PaletteType.TROPICAL_PARADISE to listOf(
            "#F72585", "#7209B7", "#3A0CA3", "#4361EE", "#4CC9F0", "#06FFA5"
        )
    )

    /**
     * Get a color palette by type
     * @param type The palette type
     * @return List of hex color strings
     */
    fun getPalette(type: PaletteType): List<String> {
        return palettes[type] ?: emptyList()
    }

    /**
     * Get a specific color from a palette by index
     * @param type The palette type
     * @param index The color index (0-based)
     * @return Hex color string or null if index is out of bounds
     */
    fun getColor(type: PaletteType, index: Int): String? {
        return palettes[type]?.getOrNull(index)
    }

    /**
     * Get all available palette types
     * @return List of all palette types
     */
    fun getAvailablePalettes(): List<PaletteType> {
        return PaletteType.values().toList()
    }

    /**
     * Get a color cycling through the palette if index exceeds palette size
     * @param type The palette type
     * @param index The color index (can be larger than palette size)
     * @return Hex color string or null if palette is empty
     */
    fun getColorCyclic(type: PaletteType, index: Int): String? {
        val palette = palettes[type] ?: return null
        if (palette.isEmpty()) return null
        return palette[index % palette.size]
    }
}
