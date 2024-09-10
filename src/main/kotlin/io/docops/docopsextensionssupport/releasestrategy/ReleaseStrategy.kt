/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.releasestrategy

import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.GA
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.M1
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.M2
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.M3
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.M4
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.M5
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.M6
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.M7
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.M8
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.M9
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.RC1
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.RC2
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.RC3
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.RC4
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.RC5
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.RC6
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.RC7
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.RC8
import io.docops.docopsextensionssupport.releasestrategy.ReleaseEnum.RC9
import io.docops.docopsextensionssupport.roadmap.wrapText
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream
import kotlin.math.max


/**
 * An enumeration representing different release stages.
 */
enum class ReleaseEnum {
    M1, M2, M3, M4, M5, M6, M7, M8, M9,
    RC1, RC2, RC3, RC4, RC5, RC6, RC7, RC8, RC9,
    GA;

    fun color(releaseEnum: ReleaseEnum): String {
        return when (releaseEnum) {
            in arrayOf(M1,M2,M3,M4,M5,M6,M7,M8,M9) -> {
                "#6cadde"
            }
            in arrayOf(RC1,RC2,RC3,RC4,RC5,RC6,RC7,RC8,RC9) -> {
                "#C766A0"
            }
            GA -> {
                "#3dd915"
            }

            else -> "#136e33"
        }
    }
    
    fun clazz(releaseEnum: ReleaseEnum): String{
        return when (releaseEnum) {
            in arrayOf(M1,M2,M3,M4,M5,M6,M7,M8,M9) -> {
                "bev"
            }
            in arrayOf(RC1,RC2,RC3,RC4,RC5,RC6,RC7,RC8,RC9) -> {
                "bev2"
            }
            GA -> {
                "bev3"
            }

            else -> "bev"
        }
    }
    fun speed(releaseEnum: ReleaseEnum): String {
        return when (releaseEnum) {
            in arrayOf(M1,M2,M3,M4,M5,M6,M7,M8,M9) -> {
                "40s"
            }
            in arrayOf(RC1,RC2,RC3,RC4,RC5,RC6,RC7,RC8,RC9) -> {
                "36s"
            }
            GA -> {
                "30s"
            }

            else -> "40s"
        }
    }
}

/**
 * This class represents a selected strategy.
 *
 * @property releaseEnum The release enumeration string associated with the strategy.
 * @property selected A boolean flag indicating whether the strategy is selected or not. The default value is false.
 */
class SelectedStrategy(val releaseEnum: String, val selected: Boolean = false)
/**
 * Represents a release.
 *
 * @property type The type of the release.
 * @property lines The lines describing the release.
 * @property date The date of the release in the format "yyyy-MM-dd".
 * @property selected Whether the release is selected or not.
 * @property goal The goal of the release.
 * @property completed Whether the release is completed or not.
 */
@Serializable
class Release(
    val type: ReleaseEnum,
    val lines: MutableList<String>,
    val date: String,
    val selected: Boolean = false,
    val goal: String,
    val completed: Boolean = false
)

fun Release.fillColor(releaseStrategy: ReleaseStrategy) : String {
    val color =  when (this.type) {
        in arrayOf(M1,M2,M3,M4,M5,M6,M7,M8,M9) -> {
            releaseStrategy.displayConfig.colors[0]
        }
        in arrayOf(RC1,RC2,RC3,RC4,RC5,RC6,RC7,RC8,RC9) -> {
            releaseStrategy.displayConfig.colors[1]
        }
        GA -> {
            releaseStrategy.displayConfig.colors[2]
        }

        else -> "#cccccc"
    }
    return color
}
/**
 * A class that represents the display configuration for a program.
 *
 * @property fontColor The font color to be used in the display. Defaults to "#fcfcfc".
 * @property milestoneColor The color to be used for milestones in the display. Defaults to "#fcfcfc".
 * @property colors The list of colors to be used in the display. Defaults to ["#fc86be", "#dc93f6", "#aeb1ed"].
 * @property circleColors The list of colors to be used for circles in the display. Defaults to ["#fc86be", "#dc93f6", "#aeb1ed"].
 * @property carColors The list of colors to be used for cars in the display. Defaults to ["#fcfcfc", "#000000", "#ff0000"].
 */
@Serializable
class DisplayConfig (val fontColor: String = "#fcfcfc", val milestoneColor: String= "#fcfcfc", val colors : List<String> = mutableListOf("#5f57ff", "#2563eb", "#7149c6"), val circleColors : List<String> = mutableListOf("#fc86be", "#dc93f6", "#aeb1ed"), val carColors : List<String> = mutableListOf("#fcfcfc", "#000000", "#ff0000"))
/**
 * Represents a release strategy.
 *
 * @param title The title of the release strategy.
 * @param releases The list of releases in the strategy.
 * @param style The style of the strategy. Default value is "TL".
 * @param scale The scale of the strategy. Default value is 1.0f.
 * @param numChars The number of characters in the strategy. Default value is 35.
 * @param displayConfig The display configuration for the strategy.
 * @param useDark Specifies if the dark theme should be used. Default value is false.
 */
open class LineType(val text: String)
class BulletLine(text: String): LineType(text)
class PlainLine(text: String): LineType(text)
@Serializable
class ReleaseStrategy (val title: String, val releases: MutableList<Release>, val style: String = "TL", val scale: Float = 1.0f, val numChars: Int= 35, val displayConfig: DisplayConfig = DisplayConfig(), var useDark: Boolean = false)

fun ReleaseStrategy.styles(): MutableMap<String, String> = mutableMapOf("TL" to "Timeline", "TLS" to "Timeline Summary",  "R" to "Roadmap", "TLG" to "Timeline Grouped")

fun ReleaseStrategy.releaseLinesToDisplay(lines: MutableList<String>): MutableList<LineType> {
    val newLines = mutableListOf<LineType>()
    lines.forEachIndexed { index, line ->
        if(line.isNotBlank()) {
            val l = itemTextWidth(line, 390, 12)
            val bLine = BulletLine(l[0])
            newLines.add(bLine)
            l.forEachIndexed { i, iLine ->
                if (i > 0) {
                    val pLine: PlainLine = PlainLine(iLine)
                    newLines.add(pLine)
                }
            }
        }
    }
    return newLines
}
fun ReleaseStrategy.maxLinesForHeight(): Int {
    var maxSize = releaseLinesToDisplay(releases[0].lines).size
    releases.forEach {
        val current = releaseLinesToDisplay(it.lines).size
        if(current > maxSize){
            maxSize = current
        }
    }
    return (maxSize + 3) * 12
}

fun ReleaseStrategy.asciidocTable(): String {
    val header = """
.Release Strategy $title
[%header]
!===
|Date |Type |Goal |Content
      
""".trimIndent()
val sb = StringBuilder(header)
    releases.forEachIndexed { index, release ->
sb.append("a|${release.date} |${release.type} |${release.goal} |${release.lines.joinToString()}")
    }
sb.append("!===")
return sb.toString()
}
fun ReleaseStrategy.grouped(): Map<Char, List<Release>> {
    return releases.groupBy { it.type.toString()[0] }
}
