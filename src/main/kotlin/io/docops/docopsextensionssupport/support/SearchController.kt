package io.docops.docopsextensionssupport.support

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/api")
class SearchController {


    private val words = mutableListOf<String>("Chocolate", "Coconut", "Mint", "Strawberry", "Vanilla", "Solo")

    @GetMapping("/typeahead", produces = [MediaType.TEXT_XML_VALUE])
    @ResponseBody
    fun find(@RequestParam("wordInput") wordInput: String): String {
        println(wordInput)
        val results = words.filter { it.lowercase().contains(wordInput.lowercase()) }
        val sb = StringBuilder()
        results.forEach {
            sb.append("<tr><td>$it</td><td>${it}</td></tr>")
        }
        return sb.toString()
    }
}