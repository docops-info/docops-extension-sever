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

package io.docops.docopsextensionssupport.support

import io.docops.docopsextensionssupport.chart.ChartColors
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.accepted
import org.springframework.web.bind.annotation.*
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random


@RestController
@RequestMapping("/api")
class ColorToGradientController {

    @GetMapping("/grad/{color}")
    fun colors(@PathVariable("color") color: String): ResponseEntity<Map<String, String>> {
        return accepted().body(gradientFromColor(color))
    }

    @GetMapping("/grad")
    fun gradList(): ResponseEntity<String> {
        val l = ChartColors.modernColors.sortedBy { it.replace("#","").toInt(16) }.chunked(10)

        val h = 10 * 30 + 50
        val w = 50 * l.size
        val sb = StringBuilder()
        val grads = StringBuilder()
        l.forEachIndexed { index, s ->
            s.forEachIndexed { i, color ->
                val gradient = SVGColor(color, "grad${index}_$i")
                val fontColor = determineTextColor(color)
                grads.append(gradient.linearGradient)
                sb.append("""<g transform="translate(${index*50},${i*30})">""")
                sb.append("""<rect x="0" y="0" width="50" height="30" fill="url(#grad${index}_$i)"/>""")
                sb.append("""<text x="25" y="15" text-anchor="middle" style="font-size:8px; font-family: Arial, Helvetica;fill:$fontColor;">$color</text>""")
                sb.append("</g>")
            }
        }
        val str = """
            <svg xmlns="http://www.w3.org/2000/svg" width="$w.0" height="$h.0" viewBox="0 0 $w.0 $h.0" xmlns:xlink="http://www.w3.org/1999/xlink" id="b2298d05-51bd-42ac-a3f1-35407b972f5d" zoomAndPan="magnify" preserveAspectRatio="none">
                 <defs>$grads</defs>
                 $sb
            </svg>
        """.trimIndent()
    return ResponseEntity.ok( str)

    }
    @PutMapping("/grad")
    fun putColors(httpServletRequest: HttpServletRequest): ResponseEntity<String> {
        val color = httpServletRequest.getParameter("gradColor")
        val gradient = SVGColor(color, "grad1")
        val hsl = hexToHsl(color)
        val textColor = determineTextColor(color)
        return accepted().body(
            """
        <div>
        <svg width="200" height="200" viewBox="0 0 200.0 200.0" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" role="img" aria-label="Docops: Color Gradient" >
        <defs>
            ${gradient.linearGradient}
            <linearGradient id="grad2" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop class="stop1" offset="0%" stop-color="${gradient.lighter()}"/>
                <stop class="stop2" offset="50%" stop-color="${gradient.original()}"/>
                <stop class="stop3" offset="100%" stop-color="${gradient.darker()}"/>
            </linearGradient>
            <linearGradient x2="0%" y2="100%" id="grad3">
                <stop stop-color="${gradient.lighter()}" stop-opacity="1" offset="0%"/>
                <stop stop-color="$hsl" stop-opacity="1" offset="100%"/>
            </linearGradient>
        </defs>
        <rect x="0" y="0" width="100%" height="30%" fill="url(#grad1)"/>
        <rect x="0" y="60" width="100%" height="30%" fill="url(#grad2)"/>
        <rect x="0" y="120" width="100%" height="30%" fill="url(#grad3)"/>
        <text x="100" y="161" text-anchor="middle" font-family="Helvetica, Arial, sans-serif" font-size="10" fill="$textColor">$textColor based on input ${color}</text>
</svg>    
</div>
<div class="divider"></div>
<div>
<pre>
    <code class="xml">
        &lt;linearGradient id="grad1" x2="0%" y2="100%"&gt;
            &lt;stop class="stop1" offset="0%" stop-color="${gradient.lighter()}"/&gt;
            &lt;stop class="stop2" offset="50%" stop-color="${gradient.original()}"/&gt;
            &lt;stop class="stop3" offset="100%" stop-color="${gradient.darker()}"/&gt;
        &lt;/linearGradient&gt;
        &lt;linearGradient id="grad2" x1="0%" y1="0%" x2="100%" y2="0%"&gt;
            &lt;stop class="stop1" offset="0%" stop-color="${gradient.lighter()}"/&gt;
            &lt;stop class="stop2" offset="50%" stop-color="${gradient.original()}"/&gt;
            &lt;stop class="stop3" offset="100%" stop-color="${gradient.darker()}"/&gt;
        &lt;/linearGradient&gt;
        &lt;linearGradient x2="0%" y2="100%" id="grad3"&gt;
            &lt;stop stop-color="${gradient.original()}" stop-opacity="1" offset="0%"/&gt;
            &lt;stop stop-color="$hsl" stop-opacity="1" offset="100%"/&gt;
        &lt;/linearGradient&gt;
    </code>
</pre>
</div>
<script>
document.querySelectorAll('pre code').forEach((el) => {
    hljs.highlightElement(el);
});
</script>
        """.trimIndent()
        )
    }

    @GetMapping("/grad/svg/{color}")
    fun svgLinearGradient(@PathVariable("color") color: String): ResponseEntity<String> {
        val gradient = SVGColor(color, "headerGreen")
        return accepted().body(gradient.linearGradient)
        //#e56516
    }

}

fun rgbToHSL(r: Int, g: Int, b: Int): String {
    // Make r, g, and b fractions of 1
    var rf = r / 255.0
    var gf = g / 255.0
    var bf = b / 255.0
    // Find greatest and smallest channel values
    val cmin = minOf(rf, gf, bf)
    val cmax = maxOf(rf, gf, bf)
    val delta = cmax - cmin
    var h = 0.0
    var s: Double
    var l: Double
    // Calculate hue
    // No difference
    if (delta == 0.0) {
        h = 0.0
        // Red is max
    } else if (cmax == rf) {
        h = ((gf - bf) / delta) % 6
        // Green is max
    } else if (cmax == gf) {
        h = (bf - rf) / delta + 2
        // Blue is max
    } else {
        h = (rf - gf) / delta + 4
    }
    h = (h * 60).roundToInt().toDouble()
    // Make negative hues positive behind 360°
    if (h < 0)
        h += 360
    // Calculate lightness
    l = (cmax + cmin) / 2
    // Calculate saturation
    s = if (delta == 0.0) 0.0 else delta / (1 - Math.abs(2 * l - 1))
    // Multiply l and s by 100 and round to 1 decimal place
    s = (s * 100).toBigDecimal().setScale(1, java.math.RoundingMode.HALF_EVEN).toDouble()
    l = (l * 100).toBigDecimal().setScale(1, java.math.RoundingMode.HALF_EVEN).toDouble()
    return "hsl($h,${s}%,${l}%)"
}

fun hexToHsl(hex: String, isPdf: Boolean = false): String {
    if(isPdf) {
        return hex
    }
    val c = Color.decode(hex)
    return rgbToHSL(c.red, c.green, c.blue)
}

fun colorLuminance(hexValue: String, lumValue: Double): String {
    // validate hex string
    var hex = hexValue.replace("[^0-9a-f]".toRegex(), "")
    if (hex.length < 6) {
        hex = hex[0].toString() + hex[0] + hex[1] + hex[1] + hex[2] + hex[2]
    }
    val lum = lumValue
    // convert to decimal and change luminosity
    var rgb = "#"
    for (i in 0 until 3) {
        var c = hex.substring(i * 2, i * 2 + 2).toInt(16)
        c = (min(max(0, c + (c * lum).toInt()), 255)).toString(16).toInt()
        rgb += ("00" + c.toString(16)).substring(c.toString(16).length)
    }
    return rgb
}

fun RandomColorDark(offset: Int): Color {
    val maxValue = 256 - offset
    val ran = Random(maxValue)
    return Color(ran.nextInt(maxValue), ran.nextInt(maxValue), ran.nextInt(maxValue))
}