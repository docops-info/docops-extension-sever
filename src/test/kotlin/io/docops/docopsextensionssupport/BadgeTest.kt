package io.docops.docopsextensionssupport

import com.starxg.badge4j.Badge
import io.docops.asciidoc.buttons.theme.theme
import io.docops.asciidoc.utils.escapeXml
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

class BadgeTest {
    val widths = arrayOf<Number>(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.1316650390625,0.42833404541015624,0.5066665649414063,0.7066665649414062,0.7066665649414062,1.0383331298828125,0.8183334350585938,0.34499969482421877,0.4850006103515625,0.4850006103515625,0.5383331298828125,0.7350006103515625,0.42833404541015624,0.4850006103515625,0.42833404541015624,0.42833404541015624,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.42833404541015624,0.42833404541015624,0.7350006103515625,0.7350006103515625,0.7350006103515625,0.7066665649414062,1.1649993896484374,0.8199996948242188,0.8183334350585938,0.8716659545898438,0.8716659545898438,0.8183334350585938,0.7633331298828125,0.9316665649414062,0.8716659545898438,0.42833404541015624,0.65,0.8183334350585938,0.7066665649414062,0.9833328247070312,0.8716659545898438,0.9316665649414062,0.8183334350585938,0.9316665649414062,0.8716659545898438,0.8183334350585938,0.7633331298828125,0.8716659545898438,0.8183334350585938,1.0949996948242187,0.8183334350585938,0.8183334350585938,0.7633331298828125,0.42833404541015624,0.42833404541015624,0.42833404541015624,0.6199996948242188,0.7349990844726563,0.4850006103515625,0.7066665649414062,0.7066665649414062,0.65,0.7066665649414062,0.7066665649414062,0.4633331298828125,0.7066665649414062,0.7066665649414062,0.375,0.42166748046875,0.65,0.375,0.9833328247070312,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.7066665649414062,0.498333740234375,0.65,0.42833404541015624,0.7066665649414062,0.65,0.8716659545898438,0.65,0.65,0.65,0.4850006103515625,0.4100006103515625,0.4850006103515625,0.7350006103515625)

    @Test
    fun determineWidth() {
       val svg = createBadge("Hello", "World", "#4c1130", "#351c75")
        val f = File("badgeout.svg")
        f.writeBytes(svg.toByteArray())

    }

    /**
     * the iLabel is the text for the left side of the badge.
     * the iMessage is the text for the right side of the badge
     * the labelColor is the left text's color. default: #999999
     * the messageColor is the right text's color. default: #ececec
     * a href is for making the badge a link to some content
     * the icon is an optional element for badges with icons. ex. data:image/svg+xml;base64,xxx
     *
     */
    fun createBadge(iLabel: String, iMessage: String, labelColor: String = "#999999", messageColor: String ="#ececec", href: String = "", icon: String=""): String {
        val t = theme {
            columns = 1
            dropShadow = 0
        }
        val label = iLabel.escapeXml()
        val message = iMessage.escapeXml()
        val clrMap = t.gradientFromColor(labelColor)
        val mMap = t.gradientFromColor(messageColor)
        val maskId = UUID.randomUUID().toString()

        val grad = """
            <linearGradient id="label_${maskId}" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="${clrMap["color1"]}"/>
                <stop class="stop2" offset="50%" stop-color="${clrMap["color2"]}"/>
                <stop class="stop3" offset="100%" stop-color="${clrMap["color3"]}"/>
            </linearGradient> 
            <linearGradient id="message_${maskId}" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="${mMap["color1"]}"/>
                <stop class="stop2" offset="50%" stop-color="${mMap["color2"]}"/>
                <stop class="stop3" offset="100%" stop-color="${mMap["color3"]}"/>
            </linearGradient> 
        """.trimIndent()
        var labelWidth = measureText(iLabel ) * 100.0F
        var messageWidth = measureText(iMessage) * 100.0F
        var labelLink = label
        var messageLink = message
        if(href.isNotEmpty()) {
            labelLink = """<a href='$href'>$label</a>"""
            messageLink = """<a href='$href'>$message</a>"""
        }
        var startX = 50
        var img = ""
        if(icon.isNotEmpty()) {
            startX += 82
            labelWidth += 10
            img = """<image x='10' y='35' width='112' height='130' xlink:href='$icon'/>"""
        }
        //language=SVG
        var b = """
            <g transform="translate(0.0, 0.0)">
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
            <mask id='$maskId'>
                <rect width='${labelWidth + messageWidth+200}' height='200' rx='30' fill='#FFF'/>
            </mask>
            <g mask='url(#$maskId)'>
                <rect fill='url(#label_${maskId})' width='${labelWidth+100}' height='200' filter="url(#Bevel2)"/>
                <rect fill='url(#message_${maskId})' x='${labelWidth+100}' width='${messageWidth+100}' height='200' filter="url(#Bevel2)"/>
                <rect width='${labelWidth + messageWidth + 200}' height='200' fill='url(#a)' filter="url(#Bevel2)"/>
            </g>
            <g aria-hidden='true'  text-anchor='start' font-family='Arial,DejaVu Sans,sans-serif'
               font-size='110' filter="url(#Bevel2)">
                <text x='$startX' y='138' textLength='${labelWidth-60}'  fill="#cccccc" >$labelLink</text>
                <text x='${labelWidth + 155}' y='138' textLength='${messageWidth}'  fill="#cccccc" >$messageLink</text>
            </g>
            $img
             </svg>
            </g>
        """.trimIndent()
        return b
    }
    fun calcWidth(text: String): Float {
        var CHAR_WIDTH_TABLE = mutableListOf<Float>()
        val instream = Objects.requireNonNull(
            Badge::class.java.getResourceAsStream("/arial-widths.json"),
            "widths-verdana-110.json"
        )
        val buff = ByteArray(1024)
        var len: Int
        val sb = StringBuilder()
        while (instream.read(buff).also { len = it } != -1) {
            sb.append(String(buff, 0, len, StandardCharsets.UTF_8))
        }

        val json = sb.toString()
        val codes = json.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val list: MutableList<Float> = ArrayList(codes.size)
        for (i in codes.indices) {
            if (i == 0) {
                list.add(codes[i][1].toString().toFloat())
            } else if (i + 1 == codes.size) {
                list.add(codes[i].substring(0, 2).toFloat())
            } else {
                list.add(codes[i].toFloat())
            }
        }

       CHAR_WIDTH_TABLE = Collections.unmodifiableList(list)
        val fallbackWidth = CHAR_WIDTH_TABLE[64]
        var total = 0f
        var i = text.length
        while (i-- > 0) {
            val idx = text.codePointAt(i)
            total += if (idx >= CHAR_WIDTH_TABLE.size) fallbackWidth else CHAR_WIDTH_TABLE[idx]
        }
        return total
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
}

