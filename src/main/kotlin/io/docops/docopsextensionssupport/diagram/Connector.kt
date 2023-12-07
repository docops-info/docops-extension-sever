package io.docops.docopsextensionssupport.diagram

import kotlinx.serialization.Serializable

@Serializable
class Connector (val text: String, val baseColor: String? = "#E14D2A", val description: String = "") {

     var start = 0

    fun textToLines(): MutableList<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        words.forEach {
            lines.add(it)
        }
        start = if(lines.size == 2) {
            45
        }else if(lines.size==3) {
            36
        } else {
            65
        }
        return lines
    }
}

@Serializable
data class Connectors(val connectors: MutableList<Connector>)