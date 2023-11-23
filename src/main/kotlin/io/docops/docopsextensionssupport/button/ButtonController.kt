/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.button

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api")
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
    /**
     * Converts a JSON payload and theme query parameter to a ButtonForm object.
     *
     * @param payload The JSON payload string.
     * @param theme (Optional) The theme query parameter.
     * @return The ResponseEntity containing the converted ButtonForm object.
     */
    @Timed(value="docops.button.put.fromJsonToButtonForm.time", description="Creating a Button using Form Submission", percentiles=[0.5,0.9])
    @Counted(value="docops.button.put.fromJsonToButtonForm.count", description="Success Fail count of fromJsonToButtonForm")
    @PutMapping("/buttons/form")
    @ResponseBody
    fun fromJsonToButtonForm(@RequestParam(name = "payload") payload: String, @RequestParam(name = "theme", required = false) theme: String): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
             fromRequestParameter(payload, theme)
        }
        log.info("fromJsonToButtonForm executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }

    private fun fromRequestParameter(payload: String, theme: String?): ResponseEntity<ByteArray> {
        try {

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

    /**
     * Converts the given Buttons object from JSON format to a Button object.
     *
     * @param buttons The Buttons object in JSON format.
     * @return The ResponseEntity containing the generated SVG representation as a ByteArray.
     */
    @Timed(value="Docops.ButtonController.put.fromJsonToButton.time", description="Creating a Button using Form Submission JSON")
    @Counted(value="Docops.ButtonController.put.fromJsonToButton.count", description="Success Fail count of fromJsonToButton")
    @PutMapping("/buttons")
    @ResponseBody
    fun fromJsonToButton(@RequestBody buttons: Buttons): ResponseEntity<ByteArray> {
        try {
            val timing = measureTimedValue {
                createResponse(buttons, true, "SVG")
            }
            log.info("fromJsonToButton executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Retrieves buttons based on the provided payload, useDark parameter, and type.
     *
     * @param payload The payload string to be processed.
     * @param useDark A boolean indicating whether to use dark mode.
     * @param type The type of buttons to retrieve.
     * @return A ResponseEntity object containing the retrieved buttons as a byte array.
     * @throws Exception if an error occurs while retrieving the buttons.
     */
    @Timed(value="docops.button.get.getButtons.time", description="Creating a Button using http get", percentiles=[0.5, 0.9])
    @Counted(value="docops.button.get.getButtons.count", description="Success Fail count of getButtons", extraTags = ["GetButtons"])
    @GetMapping("/buttons")
    @ResponseBody
    fun getButtons(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
        @RequestParam(name = "type", defaultValue = "SVG") type: String
    ): ResponseEntity<ByteArray> {
        var data = ""
        try {
            val timing = measureTimedValue {
                data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
                val content = Json.decodeFromString<Buttons>(data)
                 createResponse(content, useDark, type)
            }
            log.info("getButtons executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        } catch (e: Exception) {
            log.info("Data received after uncompressed: -> $data")
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Retrieves a PNG image of buttons based on the given payload data.
     *
     * @param payload The data string representing the buttons.
     * @param useDark Indicates whether to use dark mode for the buttons. Default is false.
     * @param type The type of the PNG image. Default is "PDF".
     * @return ResponseEntity The response entity containing the PNG image.
     * @throws Exception If there is an error in processing the request.
     */
    @Timed(value="docops.button.get.getButtonsPng.time", description="Creating a Button as PNG using http get")
    @Counted(value="docops.button.get.getButtonsPng.count", description="Success Fail count of getButtonsPng")
    @GetMapping("/buttons/png")
    @ResponseBody
    fun getButtonsPng(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
        @RequestParam(name = "type", defaultValue = "PDF") type: String
    ): ResponseEntity<ByteArray> {
        var data = ""
        try {
            val timing= measureTimedValue {
                data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
                val buttons = Json.decodeFromString<Buttons>(data)
                val buttonShape = buttons.createSVGShape()
                buttons.useDark = true
                val imgSrc = buttonShape.drawShape("PDF")
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType("image", "png", StandardCharsets.UTF_8)

                val png = SvgToPng().toPngFromSvg(
                    imgSrc,
                    Pair(buttonShape.height().toString(), buttonShape.width().toString())
                )
                ResponseEntity(png, headers, HttpStatus.OK)
            }
            log.info("getButtonsPng executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    /**
     * Converts a JSON payload into a PNG image of a button form.
     *
     * @param payload The JSON payload representing the button form.
     * @return The response entity containing the PNG image.
     */
    @Timed(value="docops.button.put.fromJsonToButtonFormPng.time", description="Creating a Button as PNG using http put")
    @Counted(value="docops.button.put.fromJsonToButtonFormPng.count", description="Success Fail count of fromJsonToButtonFormPng")
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
            val timing =  measureTimedValue {
                val png = SvgToPng().toPngFromSvg(
                    imgSrc,
                    Pair(buttonShape.height().toString(), buttonShape.width().toString())
                )
                val str = """
                <div>
                <img src="data:image/png;base64,${Base64.getEncoder().encodeToString(png)}" alt="image from button"/>
                </div>
            """.trimIndent()
                ResponseEntity(str, headers, HttpStatus.OK)
            }
            log.info("fromJsonToButtonFormPng executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        } catch (e: Exception) {
            println(imgSrc)
            throw e
        }

    }
    /**
     * Updates the theme of a button item.
     *
     * @param payload The payload string containing the button item.
     * @param theme The theme to be applied to the button item.
     * @return A ResponseEntity containing the updated button item JSON string.
     */
    @Timed(value="docops.button.put.themeItem.time", description="Updating button theme using http put")
    @Counted(value="docops.button.put.themeItem.count", description="Success Fail count of themeItem")
    @OptIn(ExperimentalSerializationApi::class)
    @PutMapping("button/theme")
    fun themeItem(@RequestParam(name = "payload") payload: String, @RequestParam(name = "theme") theme: String): ResponseEntity<String> {
        val timing = measureTimedValue {
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

             ResponseEntity(json, headers, HttpStatus.OK)
        }
        log.info("themItem executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }

}