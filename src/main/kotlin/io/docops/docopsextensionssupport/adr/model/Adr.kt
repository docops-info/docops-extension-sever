/*
 * Copyright 2020 The DocOps Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.docops.docopsextensionssupport.adr.model


data class Adr(
    val title: String = "",
    val date: String = "",
    var status: Status = Status.Rejected,
    var context: List<String> = emptyList(),
    var decision: List<String> = emptyList(),
    var consequences: List<String> = emptyList(),
    var participants: List<String> = emptyList(),
    val urlMap: MutableMap<Int, String>
) {
    fun statusClass(status: Status, current: Status): String {
        return if (status == current) {
            "selected"
        } else {
            "unselected"
        }
    }

    fun participantAsStr(): String {
        if (participants.isEmpty()) {
            return ""
        }
        return participants.joinToString(",")
    }
    fun lineCount() : Int{
        return context.size + decision.size + consequences.size + participants.size
    }
}

enum class Status {
    Proposed, Accepted, Superseded, Deprecated, Rejected;

    fun determineStatusColor(status: Status, current: Status): String {
        if(status == current) {
            return color(status)
        }
        return "#aaaaaa"
    }
    fun outlineSelectedStatus(status: Status, current: Status): String {
        if(status == current) {
            return "shape"
        }
        return ""
    }
    fun insetShadow(status: Status, current: Status) : String {
        if(status == current) {
            return ""
        }
        return "url(#inset-shadow)"
    }
    fun color(status: Status) = when(status) {
        Proposed -> "#2986cc"
        Accepted -> "#38761d"
        Superseded -> "#F5C344"
        Deprecated -> "#EA9999"
        Rejected -> "#CB444A"
    }
    fun supersededColor(status: Status, current: Status) : String = when {
        status == current && status == Superseded -> {
            "yellowToBlack"
        }
        status == current && status == Deprecated -> {
            "yellowToBlack"
        }
        else -> ""
    }

}

fun String.escapeXml(): String {
    val sb = StringBuilder()
    for (element in this) {
        when (val c: Char = element) {
            '<' -> sb.append("&lt;")
            '>' -> sb.append("&gt;")
            '\"' -> sb.append("&quot;")
            '&' -> sb.append("&amp;")
            '\'' -> sb.append("&apos;")
            else -> if (c.code > 0x7e) {
                sb.append("&#" + c.code + ";")
            } else sb.append(c)
        }
    }
    return sb.toString()
}
