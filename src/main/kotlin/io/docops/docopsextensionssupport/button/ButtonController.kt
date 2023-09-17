package io.docops.docopsextensionssupport.button

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.observation.annotation.Observed
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

@Controller
@RequestMapping("/api")
@Observed(name = "buttons.controller")
class ButtonController @Autowired constructor(private val applicationContext: ApplicationContext, private val objectMapper: ObjectMapper){
    private val log = LoggerFactory.getLogger(ButtonController::class.java)
    private val themeMap = mutableMapOf<String, String>()

    init {
        val themeList = applicationContext.getResources("classpath:static/buttondisplay/*.json")
        themeList.let {themes ->
            themes.forEach {
                themeMap[it.filename!!] = it.getContentAsString(Charset.defaultCharset())
            }
        }
    }
    @PutMapping("/buttons/form")
    @ResponseBody
    fun fromJsonToButtonForm(@RequestParam(name = "payload") payload: String, @RequestParam(name = "theme", required = false) theme: String): ResponseEntity<ByteArray> {
        return fromRequestParameter(payload, theme)
    }

    private fun fromRequestParameter(payload: String, theme: String?): ResponseEntity<ByteArray> {
        try {

            val jsonObject = Json.parseToJsonElement(payload)
            var newPayload = "{}"
            var themeStr = ""
            theme?.let {
                if(it.isNotEmpty()) {
                    themeStr = themeMap[theme]!!

                    val themeNode =
                        objectMapper.readValue(themeStr, object : TypeReference<Map<String, Any>>() {})
                    val payloadNode =
                        objectMapper.readValue(payload, object : TypeReference<MutableMap<String, Any>>() {})
                    payloadNode.replace("theme", themeNode)

                    newPayload = objectMapper.writeValueAsString(payloadNode)
                }
                else {
                    newPayload = payload
                }
            }
            val buttons = Json.decodeFromString<Buttons>(newPayload)
            buttons.useDark = true
            val buttonShape = buttons.createSVGShape()
            val imgSrc = buttonShape.drawShape("SVG")
            val div = """
                <div id='imageblock'>
                $imgSrc
                </div>
                <div id="themeBox"  data-hx-swap-oob="true">$themeStr</div>
            """.trimIndent()
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
            return ResponseEntity(div.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun createResponse(buttons: Buttons, useDark: Boolean, type: String): ResponseEntity<ByteArray> {
        buttons.useDark = useDark
        val buttonShape = buttons.createSVGShape()
        val imgSrc = buttonShape.drawShape(type)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
        return ResponseEntity(imgSrc.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)

    }

    @PutMapping("/buttons")
    @ResponseBody
    fun fromJsonToButton(@RequestBody buttons: Buttons): ResponseEntity<ByteArray> {
        try {
            return createResponse(buttons, true, "SVG")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @GetMapping("/buttons")
    @ResponseBody
    fun getButtons(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
        @RequestParam(name = "type", defaultValue = "SVG") type: String
    ): ResponseEntity<ByteArray> {
        var data = ""
        try {
            data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val content = Json.decodeFromString<Buttons>(data)
            return createResponse(content, useDark, type)
        } catch (e: Exception) {
            log.info("Data received after uncompressed: -> $data")
            e.printStackTrace()
            throw e
        }
    }

    @GetMapping("/buttons/png")
    @ResponseBody
    fun getButtonsPng(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
        @RequestParam(name = "type", defaultValue = "PDF") type: String
    ): ResponseEntity<ByteArray> {
        var data = ""
        try {
            data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val buttons = Json.decodeFromString<Buttons>(data)
            val buttonShape = buttons.createSVGShape()
            buttons.useDark = true
            val imgSrc = buttonShape.drawShape("PDF")
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType("image", "png", StandardCharsets.UTF_8)

            val png = SvgToPng().toPngFromSvg(imgSrc, Pair(buttonShape.height().toString(), buttonShape.width().toString()))
            return ResponseEntity(png, headers, HttpStatus.OK)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    @PutMapping("/buttons/form/png")
    @ResponseBody
    fun fromJsonToButtonFormPng(@RequestParam(name = "payload") payload: String): ResponseEntity<String> {
        val buttons = Json.decodeFromString<Buttons>(payload)
        buttons.useDark = true
        val buttonShape = buttons.createSVGShape()
        val imgSrc = buttonShape.drawShape("PDF")
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "png", StandardCharsets.UTF_8)
        try {
            val png = SvgToPng().toPngFromSvg(imgSrc, Pair(buttonShape.height().toString(), buttonShape.width().toString()))
            val str = """
                <div>
                <img src="data:image/png;base64,${Base64.getEncoder().encodeToString(png)}" alt="image from button"/>
                </div>
            """.trimIndent()
            return ResponseEntity(str, headers, HttpStatus.OK)

        } catch (e: Exception) {
            println(imgSrc)
            throw e
        }

    }
    @PutMapping("button/theme")
    fun themeItem(@RequestParam(name = "payload") payload: String, @RequestParam(name = "theme") theme: String): ResponseEntity<String> {
        val buttons = Json.decodeFromString<Buttons>(payload)
        buttons.useDark = true
        buttons.theme = Json.decodeFromString(themeMap[theme]!!)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.APPLICATION_JSON
        val prettyJson = Json { // this returns the JsonBuilder
            prettyPrint = true
            // optional: specify indent
            prettyPrintIndent = " "
        }
        val json = prettyJson.encodeToString(buttons)

        return ResponseEntity(json, headers, HttpStatus.OK)
    }
}