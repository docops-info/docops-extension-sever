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

package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.roadmap.wrapText
import io.docops.docopsextensionssupport.support.VisualDisplay
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.svgsupport.textWidth
import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.math.max
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
class ScoreCard @OptIn(ExperimentalUuidApi::class) constructor(val id: String = Uuid.random().toHexString(),
                                                               val title: String,
                                                               val beforeTitle: String,
                                                               val beforeSections: MutableList<BeforeSection>,
                                                               val afterTitle: String,
                                                               val afterSections: MutableList<AfterSection>,
                                                               override var useDark: Boolean = false,
                                                               override var visualVersion: Int = 1

): VisualDisplay

// Convert a ScoreCard to a CSV representation for metadata embedding
fun ScoreCard.toCsv(): CsvResponse {
    val headers = listOf("column", "section", "item", "description")
    val rows = mutableListOf<List<String>>()

    // BEFORE sections
    beforeSections.forEach { section ->
        val sectionTitle = section.title.ifBlank { beforeTitle }
        section.items.forEach { item ->
            rows.add(listOf("BEFORE", sectionTitle, item.displayText, item.description ?: ""))
        }
    }
    // AFTER sections
    afterSections.forEach { section ->
        val sectionTitle = section.title.ifBlank { afterTitle }
        section.items.forEach { item ->
            rows.add(listOf("AFTER", sectionTitle, item.displayText, item.description ?: ""))
        }
    }
    return CsvResponse(headers, rows)
}



/**
}
 * Represents a scorecard item with display text and an optional description.
 *
 * @property displayText The text to be displayed for the scorecard item.
 * @property description An optional description for the scorecard item.
 */
@Serializable
class ScoreCardItem(val displayText: String, val description: String? = "")

fun ScoreCardItem.displayTextToList(size: Int): MutableList<String> {
    return wrapText(this.displayText, size.toFloat())
}


@Serializable
open class Section
{
    var title: String = ""
    var items: MutableList<ScoreCardItem> = mutableListOf()

}

@Serializable
class BeforeSection : Section()
@Serializable
class AfterSection : Section()

@Serializable
class ScoreCardTheme(val useDark: Boolean = false, val scale: Float = 1.0f)