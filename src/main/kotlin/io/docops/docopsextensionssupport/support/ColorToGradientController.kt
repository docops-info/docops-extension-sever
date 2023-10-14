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

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.accepted
import org.springframework.web.bind.annotation.*
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


@RestController
@RequestMapping("/api")
class ColorToGradientController  {

    @GetMapping("/grad/{color}")
    fun colors(@PathVariable("color") color: String): ResponseEntity<Map<String, String>> {
        return accepted().body(gradientFromColor(color))
    }

    @PutMapping("/grad")
    fun putColors(httpServletRequest: HttpServletRequest): ResponseEntity<String> {
        val color = httpServletRequest.getParameter("gradColor")
        val gradient = gradientFromColor(color)
        return accepted().body("""
            <div>
        <svg width="200" height="200" viewBox="0 0 200.0 200.0" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" role="img" aria-label="Docops: Color Gradient" >
    <defs>
        <linearGradient id="grad1" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/>
        </linearGradient>
        <linearGradient id="grad2" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/>
        </linearGradient>
    </defs>
    <rect x="0" y="0" width="100%" height="50%" fill="url(#grad1)"/>
    <rect x="00" y="100" width="100%" height="50%" fill="url(#grad2)"/>
</svg>    
</div>
<div class="divider"></div>
<div>
<pre>
    <code class="xml">
        &lt;linearGradient id="grad1" x2="0%" y2="100%"&gt;
            &lt;stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/&gt;
            &lt;stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/&gt;
            &lt;stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/&gt;
        &lt;/linearGradient&gt;
        &lt;linearGradient id="grad2" x1="0%" y1="0%" x2="100%" y2="0%"&gt;
            &lt;stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/&gt;
            &lt;stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/&gt;
            &lt;stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/&gt;
        &lt;/linearGradient&gt;
    </code>
</pre>
</div>
<script>
document.querySelectorAll('pre code').forEach((el) => {
    hljs.highlightElement(el);
});
</script>
        """.trimIndent())
    }
    @GetMapping("/grad/svg/{color}")
    fun svgLinearGradient(@PathVariable("color") color: String): ResponseEntity<String> {
        val gradient = gradientFromColor(color)
        return accepted().body("""
        <linearGradient id="headerGreen" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="${gradient["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${gradient["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${gradient["color3"]}"/>
        </linearGradient>
        """.trimIndent())
        //#e56516
    }

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