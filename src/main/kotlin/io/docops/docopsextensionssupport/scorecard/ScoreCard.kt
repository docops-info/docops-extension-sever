package io.docops.docopsextensionssupport.scorecard

import kotlinx.serialization.Serializable

@Serializable
class ScoreCard (val title: String, val initiativeTitle: String,
                 val outcomeTitle: String,
                 val initiativeItems: MutableList<String>,
                 val outcomeItems: MutableList<String>,
    val scale: Float = 1.0f)

