package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.adr.model.escapeXml

class PlannerParser {


    fun parse(content: String): PlanItems {
        val items = mutableListOf<PlanItem>()
        val set = mutableListOf<String>()
        var bodyContent = mutableListOf<String>()
        var counter = 0
        var currentType = ""
        content.lines().forEachIndexed { index, string ->
            if(string.startsWith("- ")) {
                set.add(string)
                //string is next what is previous?
                //cannot use index, need to create counter of header
                currentType = set[counter]

                if(bodyContent.isNotEmpty()) {
                    val lineToParse = set[counter-1]
                    val item = parseLine(lineToParse)
                    items.add(item)
                    //currentType = item.type
                    item.addContent(bodyContent.joinToString(" "))
                }
                bodyContent = mutableListOf()
                counter++
                //header line
            }  else {
                //body content
                bodyContent.add(string)
            }
        }
        val item = parseLine(currentType)
        items.add(item)
        item.addContent(bodyContent.joinToString(" "))
        return PlanItems(items = items)
    }

    private fun parseLine(line: String): PlanItem {
        val sp = line.trim().split(" ")
        var planItem: PlanItem = if(sp.size > 2) {
            PlanItem(type = sp[1], title = sp.subList(2, sp.size).joinToString(" ") )
        } else {
            PlanItem(sp[1], null)
        }
        return planItem
    }

}



