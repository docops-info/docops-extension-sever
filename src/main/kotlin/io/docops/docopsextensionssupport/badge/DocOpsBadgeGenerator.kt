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

package io.docops.docopsextensionssupport.badge

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.gradientFromColor
import org.silentsoft.simpleicons.SimpleIcons
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

private const val BadgePerRow = 5

/**
 * This class is responsible for generating badges using DocOps theme.
 */
@Service
class DocOpsBadgeGenerator {


    fun createBadgeFromList(badges: MutableList<Badge>): Pair<String, Float> {

        val sb= StringBuilder()
        var x = 0f
        var xPos = 0f
        var rowCount = 0
        var rowNum = 0
        var width= 1f
        badges.forEachIndexed { i, badge ->
            val label = badge.label.escapeXml()
            val message = badge.message.escapeXml()
            val maskId = UUID.randomUUID().toString()

            val labelColor = SVGColor(badge.labelColor!!, "label_${maskId}")
            val messageColor = SVGColor(badge.messageColor!!, "message_${maskId}")
            //val clrMap = gradientFromColor(badge.labelColor!!)
            //val mMap = gradientFromColor(badge.messageColor!!)
            val grad = labelColor.linearGradient + messageColor.linearGradient
            var labelWidth = measureText(badge.label) * 100.0F
            val messageWidth = measureText(badge.message) * 100.0F
            var labelLink = label
            var messageLink = message
            badge.url?.let {
                if(it.isNotEmpty() && !badge.isPdf) {
                    labelLink = """<a href='${badge.url}' target='_blank'>$label</a>"""
                    messageLink = """<a href='${badge.url}' target='_blank'>$message</a>"""
                }
            }


            var startX = 50
            var textWidth = 0
            var img = ""
            badge.logo?.let {
                val logo = getBadgeLogo(it)
                startX += 127
                labelWidth += 100
                textWidth = 49
                img = """<image x='30' y='49' width='100' height='100' xlink:href='$logo'/>"""
            }
            var labelFill = "url(#label_${maskId})"
            var messageFill = "url(#message_${maskId})"
            var filterText = "url(#Bevel2)"
            var maskText = "url(#a)"
            var mask = createMask(maskId, labelWidth, messageWidth, labelFill, filterText, messageFill, maskText)
            if(rowCount > BadgePerRow) {
                rowCount = 0
                rowNum++
                xPos = 0f
            } else {
                rowCount++
            }
            sb.append("<g transform='translate($xPos,${rowNum*21})'>")
            if(badge.isPdf) {
                val b = makeSVGForPDF(labelColor, messageColor, labelWidth, messageWidth, textWidth, startX, badge.fontColor, badge.message, messageLink, labelLink, img, label)
                sb.append(b)
            }
            else {
                val b = makeSvg(labelColor, messageColor, labelWidth, messageWidth, textWidth, startX, badge.fontColor, badge.message, messageLink, labelLink, grad, mask, filterText, img, label)
                sb.append(b)
            }
            sb.append("</g>")
            val position = (labelWidth + messageWidth + 200) / 10 + 1f
            x += position
            xPos += position
            width = maxOf(width, xPos)
        }
        return Pair(sb.toString(), width)
    }

    private fun makeGradient(
        maskId: String,
        clrMap: Map<String, String>,
        mMap: Map<String, String>
    ): String {
        val grad = """
                <linearGradient id="label_${maskId}" x2="0%" y2="100%">
                    <stop class="stop1" offset="0%" stop-color="${clrMap["color1"]}"/>
                    <stop class="stop2" offset="50%" stop-color="${clrMap["color2"]}"/>
                    <stop class="stop3"  stop-color="${clrMap["color3"]}" stop-opacity="1" offset="100%"/>
                </linearGradient> 
                <linearGradient id="message_${maskId}" x2="0%" y2="100%">
                    <stop class="stop1" offset="0%" stop-color="${mMap["color1"]}"/>
                    <stop class="stop2" offset="50%" stop-color="${mMap["color2"]}"/>
                    <stop class="stop3"  stop-color="${mMap["color3"]}" stop-opacity="1" offset="100%"/>
                </linearGradient> 
            """.trimIndent()
        return grad
    }

    private fun makeSVGForPDF(
        clrMap: SVGColor,
        mMap: SVGColor,
        labelWidth: Float,
        messageWidth: Float,
        textWidth: Int,
        startX: Int,
        fontColor: String,
        message: String,
        messageLink: String,
        labelLink: String,
        img: String,
        label : String
    ): String {
        val labelFill = clrMap.darker()
        val messageFill = mMap.darker()
        val filterText = ""
        val maskText = ""
        val mask = """
            <rect fill='$labelFill' width='${labelWidth + 100}' height='200' filter='$filterText'/>
            <rect fill='$messageFill' x='${labelWidth + 100}' width='${messageWidth + 100}' height='200' filter='$filterText'/>
            """
        //language=svg
        return """<svg xmlns='http://www.w3.org/2000/svg' role='img' xmlns:xlink="http://www.w3.org/1999/xlink" aria-label='$label: $message' width='${(labelWidth + messageWidth + 200) / 10}' height='20' viewBox='0 0 ${labelWidth + messageWidth + 200} 200'  >
             <title>$label: $message</title>
            $mask
            <g text-anchor='start' font-family='Arial,sans-serif' font-size='110'>
                <text x='$startX' y='138' textLength='${(labelWidth - 60) - textWidth}'  fill="$fontColor" style='font-variant: small-caps;'>$labelLink</text>
                <text x='${labelWidth + 155}' y='138' textLength='${messageWidth}'  fill="$fontColor" style='font-variant: small-caps;'>$messageLink</text>
            </g>
               $img
             </svg>
        """.trimIndent()
    }
    private fun makeSvg(
        clrMap: SVGColor,
        mMap: SVGColor,
        labelWidth: Float,
        messageWidth: Float,
        textWidth: Int,
        startX: Int,
        fontColor: String,
        message: String,
        messageLink: String,
        labelLink: String,
        grad: String,
        mask: String,
        filterText: String,
        img: String,
        label: String
    ): String {
        //language=SVG
        return """
            <svg width='${(labelWidth + messageWidth + 200) / 10}' height='20' viewBox='0 0 ${labelWidth + messageWidth + 200} 200'  xmlns='http://www.w3.org/2000/svg' role='img' xmlns:xlink="http://www.w3.org/1999/xlink" aria-label='$label: $message'>
             <title>$label: $message</title>
             <defs>
                <filter id="Bevel" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                    <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut" lighting-color="white">
                        <fePointLight x="-5000" y="-10000" z="20000"/>
                    </feSpecularLighting>
                    <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                    <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
                </filter>
                <filter id="Bevel2" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="0.5" result="blur"/>
                    <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut" lighting-color="white">
                        <fePointLight x="-5000" y="-10000" z="0000"/>
                    </feSpecularLighting>
                    <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                    <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
                </filter>
                <filter id="Bevel3" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="0.2" result="blur"/>
                    <feSpecularLighting in="blur" surfaceScale="10" specularConstant="3.5" specularExponent="10" result="specOut" lighting-color="#ffffff">
                      <fePointLight x="-5000" y="-10000" z="0000"/>
                    </feSpecularLighting>
                    <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                    <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
                  </filter>
            </defs>
            <linearGradient id='a' x2='0' y2='100%'>
                <stop offset='0' stop-opacity='.1' stop-color='#EEE'/>
                <stop offset='1' stop-opacity='.1'/>
            </linearGradient>
            $grad
            $mask
          
            <g aria-hidden='true'  text-anchor='start' font-family='Arial,DejaVu Sans,sans-serif' font-size='110' filter='$filterText'>
                <text x='$startX' y='138' textLength='${(labelWidth - 60) - textWidth}'  fill="$fontColor" style='font-variant: small-caps;'>$labelLink</text>
                <text x='${labelWidth + 155}' y='138' textLength='${messageWidth}'  fill="$fontColor" style='font-variant: small-caps;'>$messageLink</text>
            </g>
            $img
             </svg>
        """.trimIndent()
    }
    /**
     * Creates an SVG badge with the specified label and message.
     *
     * @param iLabel The label text for the badge.
     * @param iMessage The message text for the badge.
     * @param labelColor The color of the label background. Default is #999999.
     * @param messageColor The color of the message background. Default is #ececec.
     * @param href The URL to link the label and message to. Default is empty string.
     * @param icon The icon image URL to display on the badge. Default is empty string.
     * @param fontColor The color of the label and message text. Default is #000000.
     *
     * @return The SVG representation of the badge.
     */
    fun createBadge(
        iLabel: String,
        iMessage: String,
        labelColor: String = "#999999",
        messageColor: String = "#ececec",
        href: String = "",
        icon: String = "",
        fontColor: String = "#000000",
        backend: String = ""
    ): String {
        val isPdf = backend == "pdf"

        val label = iLabel.escapeXml()
        val message = iMessage.escapeXml()
        val clrMap = gradientFromColor(labelColor)
        val mMap = gradientFromColor(messageColor)
        val maskId = UUID.randomUUID().toString()
        val grad = """
            <linearGradient id="label_${maskId}" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="${clrMap["color1"]}"/>
                <stop class="stop2" offset="50%" stop-color="${clrMap["color2"]}"/>
                <stop class="stop3"  stop-color="${clrMap["color3"]}" stop-opacity="1" offset="100%"/>
            </linearGradient> 
            <linearGradient id="message_${maskId}" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="${mMap["color1"]}"/>
                <stop class="stop2" offset="50%" stop-color="${mMap["color2"]}"/>
                <stop class="stop3"  stop-color="${mMap["color3"]}" stop-opacity="1" offset="100%"/>
            </linearGradient> 
        """.trimIndent()
        var labelWidth = measureText(iLabel) * 100.0F
        val messageWidth = measureText(iMessage) * 100.0F
        var labelLink = label
        var messageLink = message
        if (href.isNotEmpty() && !isPdf) {
            labelLink = """<a href='$href' target='_blank'>$label</a>"""
            messageLink = """<a href='$href' target='_blank'>$message</a>"""
        }
        var startX = 50
        var textWidth = 0
        var img = ""
        if (icon.isNotEmpty()) {
            val logo = getBadgeLogo(icon)
            startX += 127
            labelWidth += 100
            textWidth = 49
            img = """<image x='30' y='49' width='100' height='100' xlink:href='$logo'/>"""
        }
        var labelFill = "url(#label_${maskId})"
        var messageFill = "url(#message_${maskId})"
        var filterText = "url(#Bevel2)"
        var maskText = "url(#a)"
        var mask = createMask(maskId, labelWidth, messageWidth, labelFill, filterText, messageFill, maskText)
        if(isPdf) {
            labelFill = clrMap["color3"]!!
            messageFill = mMap["color3"]!!
            filterText = ""
            maskText = ""
            mask = """
            <rect fill='$labelFill' width='${labelWidth + 100}' height='200' filter='$filterText'/>
            <rect fill='$messageFill' x='${labelWidth + 100}' width='${messageWidth + 100}' height='200' filter='$filterText'/>
            """
            //language=svg
            return """
            <svg width='${(labelWidth + messageWidth + 200) / 10}' height='20' viewBox='0 0 ${labelWidth + messageWidth + 200} 200' 
            xmlns='http://www.w3.org/2000/svg' role='img' xmlns:xlink="http://www.w3.org/1999/xlink" aria-label='$label: $message'>
             <title>$label: $message</title>
            $mask
            <g text-anchor='start' font-family='Arial,sans-serif' font-size='110'>
                <text x='$startX' y='138' textLength='${(labelWidth - 60) - textWidth}'  fill="$fontColor" style='font-variant: small-caps;'>$labelLink</text>
                <text x='${labelWidth + 155}' y='138' textLength='${messageWidth}'  fill="$fontColor" style='font-variant: small-caps;'>$messageLink</text>
            </g>
             </svg>
        """.trimIndent()
        }

        //language=SVG
        return """
            <svg width='${(labelWidth + messageWidth + 200) / 10}' height='20' viewBox='0 0 ${labelWidth + messageWidth + 200} 200' 
            xmlns='http://www.w3.org/2000/svg' role='img' xmlns:xlink="http://www.w3.org/1999/xlink" aria-label='$label: $message'>
             <title>$label: $message</title>
             <defs>
                <filter id="Bevel" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                    <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut" lighting-color="white">
                        <fePointLight x="-5000" y="-10000" z="20000"/>
                    </feSpecularLighting>
                    <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                    <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
                </filter>
                <filter id="Bevel2" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="0.5" result="blur"/>
                    <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut" lighting-color="white">
                        <fePointLight x="-5000" y="-10000" z="0000"/>
                    </feSpecularLighting>
                    <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                    <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
                </filter>
                <filter id="Bevel3" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="0.2" result="blur"/>
                    <feSpecularLighting in="blur" surfaceScale="10" specularConstant="3.5" specularExponent="10" result="specOut" lighting-color="#ffffff">
                      <fePointLight x="-5000" y="-10000" z="0000"/>
                    </feSpecularLighting>
                    <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                    <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
                  </filter>
            </defs>
            <linearGradient id='a' x2='0' y2='100%'>
                <stop offset='0' stop-opacity='.1' stop-color='#EEE'/>
                <stop offset='1' stop-opacity='.1'/>
            </linearGradient>
            $grad
            $mask
            <g aria-hidden='true'  text-anchor='start' font-family='Arial,DejaVu Sans,sans-serif'
               font-size='110' filter='$filterText'>
                <text x='$startX' y='138' textLength='${(labelWidth - 60) - textWidth}'  fill="$fontColor" style='font-variant: small-caps;'>$labelLink</text>
                <text x='${labelWidth + 155}' y='138' textLength='${messageWidth}'  fill="$fontColor" style='font-variant: small-caps;'>$messageLink</text>
            </g>
            $img
             </svg>
        """.trimIndent()
    }

    private fun createMask(
        maskId: String,
        labelWidth: Float,
        messageWidth: Float,
        labelFill: String,
        filterText: String,
        messageFill: String,
        maskText: String
    ): String {
        var mask = """
                <mask id='$maskId'>
                    <rect width='${labelWidth + messageWidth + 200}' height='200' rx='30' fill='#FFF'/>
                </mask>
                <g mask='url(#$maskId)'>
                    <rect fill='$labelFill' width='${labelWidth + 100}' height='200' filter='$filterText'/>
                    <rect fill='$messageFill' x='${labelWidth + 100}' width='${messageWidth + 100}' height='200' filter='$filterText'/>
                    <rect width='${labelWidth + messageWidth + 200}' height='200' fill='$maskText' filter='$filterText'/>
                </g>
            """.trimIndent()
        return mask
    }

    fun measureText(str: String, fontSize : Int = 10): Float {
        var total = 0f
        str.codePoints().forEach {
                code ->
            total += when {
                code >= widths.size -> {
                    widths[64].toFloat()
                }
                else -> {
                    widths[code].toFloat()
                }
            }
        }
        return total
    }

    private fun getBadgeLogo(input: String?): String {
        //http://docops.io/images/docops.svg
        var logo =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="
        input?.let {
            if (input.startsWith("<") && input.endsWith(">")) {

                val simpleIcon = SimpleIcons.get(input.replace("<", "").replace(">", ""))
                if (simpleIcon != null) {
                    val ico = simpleIcon.svg
                    val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(ByteArrayInputStream(ico?.toByteArray()))
                    var src = ""
                    xml?.let {
                        src = manipulateSVG(xml, simpleIcon.hex)
                    }
                    logo = "data:image/svg+xml;base64," + Base64.getEncoder()
                        .encodeToString(src.toByteArray())
                }
            } else if (input.startsWith("http")) {
                logo = getLogoFromUrl(input)
                logo = "data:image/svg+xml;base64," + Base64.getEncoder()
                    .encodeToString(logo.toByteArray())
            }
        }
        return logo
    }

    private fun getLogoFromUrl(url: String): String {
        val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .build()
        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    companion object {
        val widths = arrayOf<Number>(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.1316650390625,0.42833404541015624,0.5066665649414063,0.7066665649414062,0.7066665649414062,1.0383331298828125,0.8183334350585938,0.34499969482421877,0.4850006103515625,0.4850006103515625,0.5383331298828125,0.7350006103515625,0.42833404541015624,0.4850006103515625,0.42833404541015624,0.42833404541015624,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.42833404541015624,0.42833404541015624,0.7350006103515625,0.7350006103515625,0.7350006103515625,0.7066665649414062,1.1649993896484374,0.8199996948242188,0.8183334350585938,0.8716659545898438,0.8716659545898438,0.8183334350585938,0.7633331298828125,0.9316665649414062,0.8716659545898438,0.42833404541015624,0.65,0.8183334350585938,0.7066665649414062,0.9833328247070312,0.8716659545898438,0.9316665649414062,0.8183334350585938,0.9316665649414062,0.8716659545898438,0.8183334350585938,0.7633331298828125,0.8716659545898438,0.8183334350585938,1.0949996948242187,0.8183334350585938,0.8183334350585938,0.7633331298828125,0.42833404541015624,0.42833404541015624,0.42833404541015624,0.6199996948242188,0.7349990844726563,0.4850006103515625,0.7066665649414062,0.7066665649414062,0.65,0.7066665649414062,0.7066665649414062,0.4633331298828125,0.7066665649414062,0.7066665649414062,0.375,0.42166748046875,0.65,0.375,0.9833328247070312,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.498333740234375,0.65,0.42833404541015624,0.7066665649414062,0.65,0.8716659545898438,0.65,0.65,0.65,0.4850006103515625,0.4100006103515625,0.4850006103515625,0.7350006103515625)
    }
}