package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.support.SVGColor

class ChartColors {
}


val STUNNINGPIE = listOf(
    "#e60049", "#0bb4ff", "#50e991", "#e6d800",
    "#9b19f5", "#ffa300", "#dc0ab4", "#b3d4ff",
    "#00bfa0", "#1984c5", "#22a7f0", "#63bff0",
    "#a7d5ed", "#e2e2e2", "#e1a692", "#de6e56",
    "#e14b31", "#c23728", "#9e2a2b", "#752123",
    "#54bebe", "#76c8c8", "#98d1d1", "#badbdb",
    "#dedad2", "#e4bcad", "#df979e", "#d7658b",
    "#c80064", "#cc241d", "#98971a", "#d79921",
    "#a87c0a",
    "#928374", "#fb4934", "#b8bb26",
    "#1d2021", "#393b3a", "#a5989b", "#565e65",
    "#d65d0e", "#e7c547", "#7fb3d5", "#7d9029",
    "#b392f0", "#548dd4", "#ff6361", "#5998de",
    "#fbf1c7", "#458588",
    "#b16286", "#076678", "#8f3f71",
    "#689d6a",   "#fabd2f",
    "#427b58", "#9d0006",  "#af3a03",
    "#ffa600", "#a991f7", "#c837ab", "#e74c3c",
     "#F7B7A3", "#EA5F89", "#e9c46a",
    "#01005E", "#22267B", "#28518A", "#04879C",
    "#f34141", "#fc2947", "#df1c41", "#cd3636",
    "#003f5c", "#58508d", "#bc5090",
    "#e0f0ea", "#95adbe", "#574f7d", "#503a65",
    "#3c2a4d", "#f39c12","#FFF1C9",
    "#f6d860", "#37cdbe", "#3d4451",
    "#5f57ff", "#2563eb", "#7149c6", "#7e22ce",
    "#f6ff01", "#a6ff00", "#fe5829", "#ff55bb",
    "#0C3C78", "#639CD9", "#5454C5", "#5F85DB",
    "#576CBC", "#19376D", "#301E67", "#2F58CD",
    "#6d9eeb", "#637a91", "#9B3192", "#57167E",
    "#2B0B3F", "#353535", "#FF6384", "#36A2EB",
    "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40",
    "#C9CBCF", "#2ECC71", "#E74C3C", "#3498DB"
)

fun chartColorAsSVGColor (id: String = "svgGradientColor") : MutableList<SVGColor> {
    val list = mutableListOf<SVGColor>()
    STUNNINGPIE.forEachIndexed { index, it ->
        val identity = if(id.isEmpty()) { "svgGradientColor_$index"} else { "${id}_$index" }
        list.add(SVGColor(it, identity))
    }
    return list
}