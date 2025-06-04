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
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import jdk.internal.org.jline.utils.InfoCmp
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

    @GetMapping("/buttons/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultButtonsJson = """
        {
          "buttons": [
            {
              "label": "Amazon",
              "link": "https://www.amazon.com",
              "description": "E-commerce, cloud computing, digital streaming",
              "embeddedImage": {"ref": "<Amazon>"}
            },
            {
              "label": "Apple",
              "link": "https://www.apple.com",
              "description": "Consumer electronics, software and services",
              "embeddedImage": {"ref": "<Apple>"}
            },
            {
              "label": "DocOps.io",
              "link": "https://docops.io",
              "description": "Documentation experience for developers",
              "embeddedImage": {"ref": "images/docops.svg"}
            }
          ],
          "buttonType": "HEX",
          "theme": {"hexLinesEnabled": true,"strokeColor": "#7695FF","colors": ["#353d4b"],"scale": 1,"columns": 3}
        }
        """.trimIndent()

        val editModeHtml = """
            <div id="buttonsContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/buttons" hx-target="#buttonsPreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit Buttons JSON:</label>
                        <textarea id="content" name="content" rows="12" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultButtonsJson}</textarea>
                    </div>
                    <div>
                        <label for="buttonType" class="block text-sm font-medium text-gray-700 mb-1">Button Type:</label>
                        <select id="buttonType" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm" onchange="updateButtonType(this.value)">
                            <option value="HEX">HEX</option>
                            <option value="REGULAR">REGULAR</option>
                            <option value="SLIM">SLIM</option>
                            <option value="ROUND">ROUND</option>
                            <option value="CIRCLE">CIRCLE</option>
                            <option value="RECTANGLE">RECTANGLE</option>
                            <option value="LARGE">LARGE</option>
                            <option value="PILL">PILL</option>
                            <option value="OVAL">OVAL</option>
                        </select>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Buttons
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/buttons/view-mode"
                                hx-target="#buttonsContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="buttonsPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Buttons" to see the preview
                        </div>
                    </div>
                    <script>
                        // Initialize the dropdown with the current button type
                        document.addEventListener('DOMContentLoaded', function() {
                            try {
                                const contentTextarea = document.getElementById('content');
                                const buttonTypeSelect = document.getElementById('buttonType');
                                if (contentTextarea && buttonTypeSelect) {
                                    const jsonContent = JSON.parse(contentTextarea.value);
                                    if (jsonContent.buttonType) {
                                        buttonTypeSelect.value = jsonContent.buttonType;
                                    }
                                }
                            } catch (error) {
                                console.error('Error initializing button type dropdown:', error);
                            }
                        });
                    </script>
                </form>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(editModeHtml, headers, HttpStatus.OK)
    }

    @GetMapping("/buttons/view-mode")
    @ResponseBody
    fun getViewMode(): ResponseEntity<String> {
        val viewModeHtml = """
            <div id="buttonsContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/buttons.svg" type="image/svg+xml" height="100%" width="100%" class="max-h-full max-w-full">
                <img src="images/buttons.svg" alt="Hexagonal Buttons" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    init {
        val themeList = applicationContext.getResources("classpath:static/buttondisplay/*.json")
        themeList.let {themes ->
            themes.forEach {
                themeMap[it.filename!!] = it.getContentAsString(Charset.defaultCharset())
            }
        }
    }
    /**
     * Converts a JSON payload and theme query parameter to a ButtonForm object and returns SVG content.
     *
     * This endpoint accepts a JSON payload containing button data and an optional theme parameter.
     * If a theme is provided, it's applied to the buttons before rendering. The method returns
     * an HTML fragment containing the SVG representation of the buttons and the theme data.
     *
     * @param payload The JSON payload string containing button definitions. This should be a valid JSON
     *                object that can be deserialized into a [Buttons] object. The payload defines the
     *                buttons to be rendered, including their labels, links, and other properties.
     * @param theme (Optional) The theme query parameter specifying the name of a theme file (without extension)
     *              from the classpath:static/buttondisplay/ directory. If provided, the theme's settings
     *              will be applied to the buttons. If not provided or empty, no theme will be applied.
     * @return The ResponseEntity containing an HTML fragment with the SVG representation of the buttons
     *         and the theme data in a separate div with data-hx-swap-oob attribute.
     */
    @Traceable
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
            buttons.useDark = false
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
    @Traceable
    @Timed(value="Docops.ButtonController.put.fromJsonToButton.time", description="Creating a Button using Form Submission JSON")
    @Counted(value="Docops.ButtonController.put.fromJsonToButton.count", description="Success Fail count of fromJsonToButton")
    @PutMapping("/buttons")
    @ResponseBody
    fun fromJsonToButton(httpServletRequest : HttpServletRequest): ResponseEntity<ByteArray> {
        try {
            val timing = measureTimedValue {
                val contents = httpServletRequest.getParameter("content")
                if(contents.isNotEmpty()) {
                    val buttons = Json.decodeFromString<Buttons>(contents)
                     createResponse(buttons, false, "SVG")

                } else {
                    ResponseEntity.badRequest().body("No Payload Found".toByteArray(StandardCharsets.UTF_8))
                }
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
    @Traceable
    @Timed(value="docops.button.get.getButtons.time", description="Creating a Button using http get", percentiles=[0.5, 0.9])
    @Counted(value="docops.button.get.getButtons.count", description="Success Fail count of getButtons")
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
     * Updates the theme of a button item.
     *
     * @param payload The payload string containing the button item.
     * @param theme The theme to be applied to the button item.
     * @return A ResponseEntity containing the updated button item JSON string.
     */
       @Traceable
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
