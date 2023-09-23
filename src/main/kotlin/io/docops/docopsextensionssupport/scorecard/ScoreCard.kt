package io.docops.docopsextensionssupport.scorecard

import kotlinx.serialization.Serializable
import java.util.*

/**
 * Represents a scorecard for tracking scores and outcomes.
 *
 * @property id The unique identifier for the scorecard. Default value is generated using UUID.randomUUID().
 * @property title The title of the scorecard.
 * @property initiativeTitle The title for the initiative items section of the scorecard.
 * @property outcomeTitle The title for the outcome items section of the scorecard.
 * @property initiativeItems The list of initiative items in the scorecard.
 * @property outcomeItems The list of outcome items in the scorecard.
 * @property scale The scale value used for calculating scores. Default value is 1.0f.
 * @property scoreCardTheme The theme settings for the scorecard. Default value is ScoreCardTheme().
 */
@Serializable
class ScoreCard (val id: String = UUID.randomUUID().toString(),
                 val title: String, val initiativeTitle: String,
                 val outcomeTitle: String,
                 val initiativeItems: MutableList<ScoreCardItem>,
                 val outcomeItems: MutableList<ScoreCardItem>,
    val scale: Float = 1.0f, val scoreCardTheme: ScoreCardTheme = ScoreCardTheme()
)

/**
 * Represents a scorecard item with display text and an optional description.
 *
 * @property displayText The text to be displayed for the scorecard item.
 * @property description An optional description for the scorecard item.
 */
@Serializable
class ScoreCardItem(val displayText: String, val description: String? = "")

/**
 * Represents a scorecard theme.
 *
 * A scorecard theme is used to customize the appearance of a scorecard. It defines various properties such as the
 * title color, background color, initiative title color, outcome title color, initiative display text color, outcome
 * display text color, initiative background color, outcome background color, and arrow color.
 *
 * @param titleColor The color of the scorecard's title.
 * @param backgroundColor The background color of the scorecard.
 * @param initiativeTitleColor The color of the initiative title.
 * @param outcomeTitleColor The color of the outcome title.
 * @param initiativeDisplayTextColor The color of the initiative display text.
 * @param outcomeDisplayTextColor The color of the outcome display text.
 * @param initiativeBackgroundColor The background color of the initiative.
 * @param outcomeBackgroundColor The background color of the outcome.
 * @param arrowColor The color of the arrows used in the scorecard.
 */
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