package io.docops.docopsextensionssupport.diagram

import kotlinx.serialization.Serializable

@Serializable
class PlaceMat(val name: String, val legend: String = "", val style: String = "font-size: 1em;")

fun PlaceMat.textToLines(): Pair<MutableList<String>, Int> {
    val lines = mutableListOf<String>()
    val words = name.split(" ")
    words.forEach {
        lines.add(it)
    }
    val start = when (lines.size) {
        2 -> {
            45
        }
        3 -> {
            36
        }
        else -> {
            65
        }
    }
    return Pair(lines, start)
}

@Serializable
class ColorLegendConfig(val color: String = "#E14D2A", val legend: String = "", val style : String ="fill:#fcfcfc;")

@Serializable
class PlaceMatConfig(val style: String = "font-size: 1em;", val legend: MutableList<ColorLegendConfig> = mutableListOf(ColorLegendConfig(color= "#E14D2A",legend = "Company"), ColorLegendConfig(color = "#82CD47",legend = "Vendor"), ColorLegendConfig(color = "#687EFF",legend = "Both")))

@Serializable
data class PlaceMats(val placemats: MutableList<PlaceMat>, val config: PlaceMatConfig = PlaceMatConfig())

@Serializable
data class PlaceMatRequest(val placeMats: MutableList<PlaceMat>, var useDark: Boolean = false, val config: PlaceMatConfig= PlaceMatConfig())