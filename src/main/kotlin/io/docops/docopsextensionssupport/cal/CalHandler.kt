package io.docops.docopsextensionssupport.cal


import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import kotlinx.serialization.json.Json
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class CalHandler : DocOpsHandler {

    fun handleSVG(payload: String) : String {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val calMaker = CalMaker()
        var svg = ""
        if(data.trim().isEmpty()) {
            svg = calMaker.makeCalendar(null)
        } else {
            val calEntry = Json.decodeFromString<CalEntry>(data)
            svg = calMaker.makeCalendar(calEntry)
        }
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}