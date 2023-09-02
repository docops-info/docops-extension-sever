package io.docops.docopsextensionssupport.scorecard

import kotlinx.serialization.Serializable

@Serializable
class ScoreCard (val title: String, val initiativeTitle: String,
                 val outcomeTitle: String,
                 val initiativeItems: MutableList<ScoreCardItem>,
                 val outcomeItems: MutableList<ScoreCardItem>,
    val scale: Float = 1.0f, val scoreCardTheme: ScoreCardTheme = ScoreCardTheme()
)

@Serializable
class ScoreCardItem(val displayText: String, val description: String? = "")

@Serializable
class ScoreCardTheme(val titleColor: String = "#2c445a",
                     val backgroundColor: String = "#E7D6B7",
                     val initiativeTitleColor: String = "#2c445a",
                     val outcomeTitleColor: String= "#2c445a",
                     val initiativeDisplayTextColor: String = "#000000",
                     val outcomeDisplayTextColor: String = "#000000",
                     val initiativeBackgroundColor: String = "#fcfcfc",
                     val outcomeBackgroundColor: String = "#fcfcfc",
                     val arrowColor: String = "#e0349c")