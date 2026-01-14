package io.docops.docopsextensionssupport.diagram.placemat

import io.docops.docopsextensionssupport.support.VisualDisplay
import kotlinx.serialization.Serializable

@Serializable
class PlaceMat(val name: String, val legend: String = "", val style: String = "font-size: 1em;")

fun PlaceMat.textToLines(): Pair<MutableList<String>, Int> {
    val lines = mutableListOf<String>()
    val words = name.split(" ")

    // Improved text layout algorithm
    if (words.size <= 1) {
        // Single word - no need to split
        lines.add(name)
    } else if (words.size <= 3) {
        // 2-3 words - put each on its own line
        words.forEach {
            lines.add(it)
        }
    } else {
        // More than 3 words - try to balance lines
        var currentLine = ""
        words.forEach { word ->
            if (currentLine.isEmpty()) {
                currentLine = word
            } else if ((currentLine + " " + word).length <= 12) {
                currentLine += " $word"
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
    }

    // Adjust vertical positioning based on number of lines
    val start = when (lines.size) {
        1 -> 55  // Single line - centered
        2 -> 45  // Two lines - slightly above center
        3 -> 36  // Three lines - start higher
        else -> 30  // More than three lines - start even higher
    }

    return Pair(lines, start)
}
fun PlaceMat.legendAsStyle(): String {
    return legend.replace(' ', '_')
}


@Serializable
class ColorLegendConfig(val color: String = "#E14D2A", val legend: String = "", val style : String ="fill:#fcfcfc;")
fun ColorLegendConfig.legendAsStyle(): String {
    return legend.replace(' ', '_')
}

@Serializable
class PlaceMatConfig(val style: String = "font-size: 1em;", val legend: MutableList<ColorLegendConfig> = mutableListOf(ColorLegendConfig(color= "#E14D2A",legend = "Company"), ColorLegendConfig(color = "#82CD47",legend = "Vendor"), ColorLegendConfig(color = "#687EFF",legend = "Both")))

fun PlaceMatConfig.colorFromLegendName(name : String): ColorLegendConfig {
    legend.forEach{
        if(name == it.legend) {
            return it
        }
    }
    throw RuntimeException("Input Name[$name] not found")
}
@Serializable
data class PlaceMats(val placemats: MutableList<PlaceMat>, val config: PlaceMatConfig = PlaceMatConfig())

@Serializable
data class PlaceMatRequest(val placeMats: MutableList<PlaceMat>, override var useDark: Boolean = false,
                           override val visualVersion: Int = 1, val config: PlaceMatConfig= PlaceMatConfig(), val title: String = "", val scale: Float= 1.0f, val fill: Boolean = true):
    VisualDisplay
