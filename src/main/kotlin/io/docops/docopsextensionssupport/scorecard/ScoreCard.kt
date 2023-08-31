package io.docops.docopsextensionssupport.scorecard

import kotlinx.serialization.Serializable

@Serializable
class ScoreCard (val title: String, val initiativeTitle: String,
                 val outcomeTitle: String,
                 val initiativeItems: MutableList<ScoreCardItem>,
                 val outcomeItems: MutableList<ScoreCardItem>,
    val scale: Float = 1.0f)

@Serializable
class ScoreCardItem(val displayText: String, val description: String? = "")