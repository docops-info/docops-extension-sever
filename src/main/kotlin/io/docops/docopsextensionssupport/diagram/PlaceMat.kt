package io.docops.docopsextensionssupport.diagram

import kotlinx.serialization.Serializable

@Serializable
class PlaceMat(val name: String, val colorIndex: Int = 0, val style: String = "font-size: 1em;")

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
class PlaceMatConfig(val style: String = "font-size: 1em;", val baseColors: MutableList<String> = mutableListOf("#E14D2A", "#82CD47", "#687EFF", "#C02739", "#FEC260", "#e9d3ff", "#7fc0b7"))

@Serializable
data class PlaceMats(val placemats: MutableList<PlaceMat>, val config: PlaceMatConfig = PlaceMatConfig())