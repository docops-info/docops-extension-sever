package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.chart.DefaultChartColors
import io.docops.docopsextensionssupport.support.generateGradient
import io.docops.docopsextensionssupport.support.hexToRgb
import io.docops.docopsextensionssupport.support.svgGradient
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class PlanItem(val id: String = UUID.randomUUID().toString(), val type: String, val title: String?, val color: String?) {
    var isParent: Boolean = false
    var content: String? =""
    //this field is used for measuring text width
     var shadowContent : String? = null

     val urlMap = mutableMapOf<String,String>()
    private var hasUrls = false
    fun addContent(content: String) {
        this.content = content.escapeXml()
        shadowContentWithUrl()
    }

    fun colorGradient() : String {
        color?.let {
            val grad = svgGradient(color, id)
            return grad
        }
        return ""
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

class PlanItems(val items : MutableList<PlanItem> = mutableListOf()) {
    fun toColumns(): Map<String, List<PlanItem>> {
        val cols = items.groupBy { it.type  }
        cols.forEach { (key, value) ->
            value.forEachIndexed { index, item ->
                if(index == 0) {
                    item.isParent = true
                }
            }
        }
        return items.groupBy { it.type  }
    }
    fun maxRows(): Int {
        val cols = toColumns()
        val maxRows = cols.values.maxOf { it.size }
        return maxRows
    }
    fun colorDefs(planItems: Map<String, List<PlanItem>> ): String {
        val sb = StringBuilder()
        var column = 0
        planItems.forEach { (t, u) ->
            val color = DefaultChartColors[column % DefaultChartColors.size]
            val grad = svgGradient(color, "planItem_$column")
            sb.append(grad)
            column++
        }
        return sb.toString()

    }
}

