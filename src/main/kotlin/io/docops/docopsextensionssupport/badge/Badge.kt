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

package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable

@Serializable
class Badge(val  label:String, val message: String, val  url: String? = "", val labelColor: String?, val messageColor: String?, val logo: String?, val fontColor: String = "#fcfcfc", val isPdf: Boolean = false)

@Serializable
class FormBadge(val label:String, val message: String, val  url: String, val labelColor: String?, val messageColor: String?, val logo: String?, val fontColor: String = "#fcfcfc")

fun FormBadge.labelOrNull() : String? {
    return this.label.ifEmpty {
        null
    }
}

/**
 * Convert MutableList<Badge> to basic CSV
 */
fun MutableList<Badge>.toCsv(): CsvResponse {
    val headers = listOf("Label", "Message", "URL", "Label Color", "Message Color", "Logo", "Font Color", "Is PDF")

    val rows = this.map { badge ->
        listOf(
            badge.label,
            badge.message,
            badge.url ?: "",
            badge.labelColor ?: "",
            badge.messageColor ?: "",
            badge.logo ?: "",
            badge.fontColor,
            badge.isPdf.toString()
        )
    }

    return CsvResponse(headers, rows)
}
