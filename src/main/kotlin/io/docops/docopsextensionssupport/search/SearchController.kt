package io.docops.docopsextensionssupport.search

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Controller
@RequestMapping("/api")
class SearchController {


    private val words = mutableListOf<String>("Chocolate", "Coconut", "Mint", "Strawberry", "Vanilla", "Solo")

    @GetMapping("/typeahead", produces = [MediaType.TEXT_XML_VALUE])
    @ResponseBody
    fun find(@RequestParam("wordInput") wordInput: String): String {
        //http://localhost:8983/solr/ascii/select?indent=true&q.op=OR&q=contents%3Aroach*&useParams=
        println(wordInput)
        val client = HttpClient.newHttpClient();
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8983/solr/ascii/select?indent=true&q.op=OR&q=contents%3A${wordInput}*&useParams="))
            .build();

        val response = client.send(request, HttpResponse.BodyHandlers.ofString());
        val unk = Json { ignoreUnknownKeys = true; isLenient = true }
       // val jsonObject = unk.decodeFromString<SolrDoc>(response.body())
        val json = unk.parseToJsonElement(response.body())
        //val map = json.jsonObject.toMap()
        json as JsonObject
        val map = json["response"] as JsonObject
        val arr = map["docs"] as JsonArray
        val docs = mutableListOf<SolrDoc>()
        arr.forEach {
            elem ->
            val adoc = SolrDoc(id = elem.jsonObject["id"].toString(), metadata = elem.jsonObject["metadata"].toString(), contents = elem.jsonObject["contents"].toString())
            docs.add(adoc)
        }
        val results = words.filter { it.lowercase().contains(wordInput.lowercase()) }
        val sb = StringBuilder()
        docs.forEach {
            sb.append("<tr><td>${it.id}</td><td>${it.contents}</td></tr>")
        }
        return sb.toString()
    }


}

@Serializable
class SolrDoc(val id: String, val metadata: String, val contents: String)