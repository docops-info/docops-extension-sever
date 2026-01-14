package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.ChartColors.Companion.CYBER_PALETTE
import io.docops.docopsextensionssupport.support.SVGColor

/**
 * Contains color schemes for charts.
 * Colors are organized into meaningful groups for better visualization.
 */
class ChartColors {
    companion object {
        // Primary color scheme - vibrant colors with good contrast
        val PRIMARY = listOf(
            SVGColor("#e60049"), SVGColor("#0bb4ff"), SVGColor("#50e991"),
            SVGColor("#e6d800"), SVGColor("#9b19f5"), SVGColor("#ffa300"),
            SVGColor("#dc0ab4"), SVGColor("#00bfa0")
        )

        // Pastel color scheme - softer colors for less intense visualizations
        val PASTEL = listOf(
            SVGColor("#a7d5ed"), SVGColor("#e1a692"), SVGColor("#98d1d1"),
            SVGColor("#e4bcad"), SVGColor("#df979e"), SVGColor("#b392f0"),
            SVGColor("#F7B7A3"), SVGColor("#95adbe")
        )

        // Blue color scheme - variations of blue for monochromatic charts
        val BLUE = listOf(
            SVGColor("#1984c5"), SVGColor("#22a7f0"), SVGColor("#63bff0"),
            SVGColor("#b3d4ff"), SVGColor("#076678"), SVGColor("#5998de"),
            SVGColor("#639CD9"), SVGColor("#36A2EB")
        )

        // Red color scheme - variations of red for monochromatic charts
        val RED = listOf(
            SVGColor("#e14b31"), SVGColor("#c23728"), SVGColor("#9e2a2b"),
            SVGColor("#752123"), SVGColor("#9d0006"), SVGColor("#e74c3c"),
            SVGColor("#f34141"), SVGColor("#E74C3C")
        )

        // Green color scheme - variations of green for monochromatic charts
        val GREEN = listOf(
            SVGColor("#54bebe"), SVGColor("#76c8c8"), SVGColor("#98971a"),
            SVGColor("#689d6a"), SVGColor("#427b58"), SVGColor("#50e991"),
            SVGColor("#37cdbe"), SVGColor("#2ECC71")
        )

        // Purple color scheme - variations of purple for monochromatic charts
        val PURPLE = listOf(
            SVGColor("#58508d"), SVGColor("#bc5090"), SVGColor("#574f7d"),
            SVGColor("#503a65"), SVGColor("#3c2a4d"), SVGColor("#7149c6"),
            SVGColor("#7e22ce"), SVGColor("#9966FF")
        )

        // Categorical color scheme - distinct colors for categorical data
        val CATEGORICAL = listOf(
            SVGColor("#003f5c"), SVGColor("#bc5090"), SVGColor("#ffa600"),
            SVGColor("#ff6361"), SVGColor("#4BC0C0"), SVGColor("#FFCE56"),
            SVGColor("#FF9F40"), SVGColor("#C9CBCF")
        )

        val modernColors = listOf(
            SVGColor("#2563EB"), SVGColor("#10B981"), SVGColor("#F59E0B"),
            SVGColor("#EF4444"), SVGColor("#8B5CF6"), SVGColor("#EC4899"),
            SVGColor("#06B6D4"), SVGColor("#F97316"), SVGColor("#14B8A6"),
            SVGColor("#6366F1"), SVGColor("#84CC16"), SVGColor("#F43F5E"),
            SVGColor("#0EA5E9"), SVGColor("#A855F7"), SVGColor("#22C55E"),
            SVGColor("#FB923C"), SVGColor("#3B82F6"), SVGColor("#14B8A6"),
            SVGColor("#F59E0B"), SVGColor("#DC2626")
        )
        // High-Impact Cyber-Brutalist Palette (Version 3+)
        val CYBER_PALETTE = listOf(
            SVGColor("#f72585"), // Neon Pink
            SVGColor("#7209b7"), // Vivid Purple
            SVGColor("#3a0ca3"), // Deep Indigo
            SVGColor("#4361ee"), // Royal Blue
            SVGColor("#4cc9f0"), // Vivid Cyan
            SVGColor("#00f5d4"), // Aquamarine
            SVGColor("#00bbf9"), // Sky Blue
            SVGColor("#ff0054"), // Fashion Magenta
            SVGColor("#ff9e00")  // Amber
        )

        fun getColorForIndex(index: Int): SVGColor {
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
    CYBER_PALETTE.forEachIndexed { index, it ->
        val identity = if(id.isEmpty()) { "svgGradientColor_$index"} else { "${id}_$index" }
        list.add(SVGColor(it.color, identity))
    }
    return list
}
