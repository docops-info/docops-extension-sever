package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.adr.model.escapeXml
import kotlinx.serialization.Serializable

@Serializable
class PlanItem(val type: String, val title: String?, val color: String?) {
     var content: String? =""
    //this field is used for measuring text width
     var shadowContent : String? = null

     val urlMap = mutableMapOf<String,String>()
    private var hasUrls = false
    fun addContent(content: String) {
        this.content = content.escapeXml()
        shadowContentWithUrl()
    }

    private fun shadowContentWithUrl()  {

        content?.let {
            it.lines().forEachIndexed { index, input ->
                var s = input
                if(input.contains("[[") && input.contains("]]")) {
                    val regex = "(?<=\\[\\[)(.*?)(?=]])".toRegex()
                    hasUrls = true
                    val matches = regex.findAll(s)
                    matches.forEach {
                            item ->
                        val urlItem = item.value.split(" ")
                        val url = urlItem[0]
                        val display = urlItem[1]
                        s = input.replace("[[${item.value}]]", display)
                        content = input.replace("[[${item.value}]]", """[[$display]]""")
                        urlMap["[[$display]]"] = url
                    }
                }
                shadowContent = s.escapeXml()
            }
        }

    }
}

class PlanItems(val items : MutableList<PlanItem> = mutableListOf<PlanItem>()) {
    fun toColumns(): Map<String, List<PlanItem>> {
        return items.groupBy { it.type  }
    }
    fun maxRows(): Int {
        val cols = toColumns()
        val maxRows = cols.values.maxOf { it.size }
        return maxRows
    }

}

