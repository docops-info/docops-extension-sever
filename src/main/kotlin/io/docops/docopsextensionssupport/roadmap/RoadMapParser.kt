package io.docops.docopsextensionssupport.roadmap


class RoadMapParser {

    fun parse(content: String): RoadMaps {
        return group(content = content)

    }

    private fun group(content: String): RoadMaps {
        val now = mutableListOf<MutableList<String>>()
        val next = mutableListOf<MutableList<String>>()
        val later = mutableListOf<MutableList<String>>()
        var newList: MutableList<String> = mutableListOf()
        val urlMap = mutableMapOf<String,String>()
        content.lines().forEachIndexed { index, input ->
            var s = input
            if(input.contains("[[") && input.contains("]]")) {
                val regex = "(?<=\\[\\[)(.*?)(?=]])".toRegex()
                val matches = regex.findAll(s)
                matches.forEach {
                    item ->
                    val urlItem = item.value.split(" ")
                    val url = urlItem[0]
                    val display = urlItem[1]
                    s = input.replace("[[${item.value}]]", "[[${display}]]")
                    urlMap["[[${display}]]"] = url

                }
            }
            if (s.trim().startsWith("- now")) {
                newList = mutableListOf()
                now.add(newList)
            } else if(s.trim().startsWith("- next")) {
                newList = mutableListOf()
                next.add(newList)
            }
            else if(s.trim().startsWith("- later")) {
                newList = mutableListOf()
                later.add(newList)
            }
            else {
                newList.add(s)
            }
        }
        return RoadMaps(
            now = now,
            next = next,
            later = later,
            urlMap= urlMap)
    }
}
data class RoadMaps(val now: MutableList<MutableList<String>>, val next: MutableList<MutableList<String>>, val later: MutableList<MutableList<String>>, val urlMap: MutableMap<String, String>)