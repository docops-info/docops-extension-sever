package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.support.SVGColor

/**
 * Contains color schemes for charts.
 * Colors are organized into meaningful groups for better visualization.
 */
class ChartColors {
    companion object {
        // Primary color scheme - vibrant colors with good contrast
        val PRIMARY = listOf(
            "#e60049", "#0bb4ff", "#50e991", "#e6d800", "#9b19f5", "#ffa300", "#dc0ab4", "#00bfa0"
        )

        // Pastel color scheme - softer colors for less intense visualizations
        val PASTEL = listOf(
            "#a7d5ed", "#e1a692", "#98d1d1", "#e4bcad", "#df979e", "#b392f0", "#F7B7A3", "#95adbe"
        )

        // Blue color scheme - variations of blue for monochromatic charts
        val BLUE = listOf(
            "#1984c5", "#22a7f0", "#63bff0", "#b3d4ff", "#076678", "#5998de", "#639CD9", "#36A2EB"
        )

        // Red color scheme - variations of red for monochromatic charts
        val RED = listOf(
            "#e14b31", "#c23728", "#9e2a2b", "#752123", "#9d0006", "#e74c3c", "#f34141", "#E74C3C"
        )

        // Green color scheme - variations of green for monochromatic charts
        val GREEN = listOf(
            "#54bebe", "#76c8c8", "#98971a", "#689d6a", "#427b58", "#50e991", "#37cdbe", "#2ECC71"
        )

        // Purple color scheme - variations of purple for monochromatic charts
        val PURPLE = listOf(
            "#58508d", "#bc5090", "#574f7d", "#503a65", "#3c2a4d", "#7149c6", "#7e22ce", "#9966FF"
        )

        // Categorical color scheme - distinct colors for categorical data
        val CATEGORICAL = listOf(
            "#003f5c", "#bc5090", "#ffa600", "#ff6361", "#4BC0C0", "#FFCE56", "#FF9F40", "#C9CBCF"
        )

        val modernColors = listOf(
            "#2563EB", // Blue
            "#10B981", // Emerald
            "#F59E0B", // Amber
            "#EF4444", // Red
            "#8B5CF6", // Violet
            "#EC4899", // Pink
            "#06B6D4", // Cyan
            "#F97316", // Orange
            "#14B8A6", // Teal
            "#6366F1", // Indigo
            "#84CC16", // Lime
            "#F43F5E", // Rose
            "#0EA5E9", // Sky Blue
            "#A855F7", // Purple
            "#22C55E", // Green
            "#FB923C", // Light Orange
            "#3B82F6", // Bright Blue
            "#14B8A6", // Turquoise
            "#F59E0B", // Gold
            "#DC2626"  // Dark Red
        )

        fun getColorForIndex(index: Int): String {
            return modernColors[index % modernColors.size]
        }
    }
}

/**
 * Main color list for charts, combining colors from different schemes.
 * Reduced from the original large list to a more manageable and organized set.
 */
val STUNNINGPIE = listOf(
    // Primary vibrant colors
    "#e60049", "#0bb4ff", "#50e991", "#e6d800", "#9b19f5", "#ffa300", "#dc0ab4", "#00bfa0",

    // Blues
    "#1984c5", "#22a7f0", "#63bff0", "#b3d4ff", "#076678", "#5998de", "#639CD9", "#36A2EB",

    // Reds
    "#e14b31", "#c23728", "#9e2a2b", "#752123", "#9d0006", "#e74c3c", "#f34141", "#E74C3C",

    // Greens
    "#54bebe", "#76c8c8", "#98971a", "#689d6a", "#427b58", "#50e991", "#37cdbe", "#2ECC71",

    // Purples
    "#58508d", "#bc5090", "#574f7d", "#503a65", "#3c2a4d", "#7149c6", "#7e22ce", "#9966FF",

    // Additional categorical colors
    "#003f5c", "#ffa600", "#ff6361", "#4BC0C0", "#FFCE56", "#FF9F40", "#C9CBCF"
)

fun chartColorAsSVGColor (id: String = "svgGradientColor") : MutableList<SVGColor> {
    val list = mutableListOf<SVGColor>()
    STUNNINGPIE.forEachIndexed { index, it ->
        val identity = if(id.isEmpty()) { "svgGradientColor_$index"} else { "${id}_$index" }
        list.add(SVGColor(it, identity))
    }
    return list
}
