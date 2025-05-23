package io.docops.docopsextensionssupport.roadmap

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
                currentType = set[counter]

                if(bodyContent.isNotEmpty()) {
                    val lineToParse = set[counter-1]
                    val item = parseLine(lineToParse)
                    items.add(item)
                    //currentType = item.type
                    item.addContent(bodyContent.joinToString("\n"))
                }
                bodyContent = mutableListOf()
                counter++
                //header line
            }  else {
                //body content
                // Check if line starts with * and mark it as a bullet point
                if(string.startsWith("* ")) {
                    bodyContent.add("•" + string.substring(1))
                } else {
                    bodyContent.add(string)
                }
            }
        }
        val item = parseLine(currentType)
        items.add(item)
        item.addContent(bodyContent.joinToString("\n"))
        return PlanItems(items = items)
    }

    private fun parseLine(line: String): PlanItem {
        val sp = line.trim().split(" ")
        val planItem: PlanItem = if(sp.size > 2) {
            val remain = sp.subList(2, sp.size)
            var color: String? = null
            var title = ""
            remain.forEach {
                if(it.startsWith("#")) {
                    color = it
                } else {
                    title += "$it "
                }
            }
            PlanItem(type = sp[1], title = title.trim(), color = color)
        } else {
            PlanItem(type=sp[1], title = null, color= null)
        }
        return planItem
    }

}
