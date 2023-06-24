package io.docops.docopsextensionssupport.releasestrategy


enum class ReleaseEnum {
    M1, M2, M3, M4, M5, M6, M7, M8, M9,
    RC1, RC2, RC3, RC4, RC5, RC6, RC7, RC8, RC9,
    GA;

    fun color(releaseEnum: ReleaseEnum): String {
        return when (releaseEnum) {
            in arrayOf(M1,M2,M3,M4,M5,M6,M7,M8,M9) -> {
                "#c30213"
            }
            in arrayOf(RC1,RC2,RC3,RC4,RC5,RC6,RC7,RC8,RC9) -> {
                "#2cc3cc"
            }
            GA -> {
                "#3dd915"
            }

            else -> "#c30213"
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
}

class SelectedStrategy(val releaseEnum: String, val selected: Boolean = false)
class Release(
    val type: ReleaseEnum,
    val lines: MutableList<String>,
    val date: String,
    val selected: Boolean = false,
    val goal: String
)
class ReleaseStrategy (val title: String, val releases: MutableList<Release>, val style: String = "TL")

fun ReleaseStrategy.styles(): MutableMap<String, String> = mutableMapOf("TL" to "Timeline", "R" to "Roadmap", "TLG" to "Timeline Grouped")

fun ReleaseStrategy.grouped(): Map<Char, List<Release>> {
    return releases.groupBy { it.type.toString().get(0) }
}
