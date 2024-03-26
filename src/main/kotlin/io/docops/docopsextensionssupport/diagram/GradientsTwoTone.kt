package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.support.hexToHsl

class GradientsTwoTone {
}


val gradients = mutableMapOf<String, String>(
    "#feb47b" to
    """<linearGradient id="grad0" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
<stop class="stop1" offset="0%" stop-color="#feb47b"/>
<stop class="stop3" offset="100%" stop-color="#ff7e5f"/>
</linearGradient>""",
    "#dc8875" to
    """
        <linearGradient id="grad1" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#dc8875"/>
            <stop class="stop3" offset="100%" stop-color="#b5304c"/>
        </linearGradient>""",
        "#aa7398" to
        """
        <linearGradient id="grad2" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#aa7398"/>
            <stop class="stop3" offset="100%" stop-color="#602d5f"/>
        </linearGradient>""",
    "#eddcd2" to
    """
        <linearGradient id="grad3" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#eddcd2"/>
            <stop class="stop3" offset="100%" stop-color="#cb997e"/>
        </linearGradient>""",
    "#b7b7a4" to
    """
        <linearGradient id="grad4" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#b7b7a4"/>
            <stop class="stop3" offset="100%" stop-color="#847e70"/>
        </linearGradient>""",
    "#c77dff" to
    """
        <linearGradient id="grad5" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#c77dff"/>
            <stop class="stop3" offset="100%" stop-color="#7b2cbf"/>
        </linearGradient>""",
    "#98c1d9" to
    """
        <linearGradient id="grad6" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#98c1d9"/>
            <stop class="stop3" offset="100%" stop-color="#3d5a80"/>
        </linearGradient>""",
    "#ffafbd" to
    """
        <linearGradient id="grad7" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#ffafbd"/>
            <stop class="stop3" offset="100%" stop-color="#ffc3a0"/>
        </linearGradient>""",
    "#2193b0" to
    """
        <linearGradient id="grad8" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#2193b0"/>
            <stop class="stop3" offset="100%" stop-color="#6dd5ed"/>
        </linearGradient>""",
    "#cc2b5e" to
    """
        <linearGradient id="grad9" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#cc2b5e"/>
            <stop class="stop3" offset="100%" stop-color="#753a88"/>
        </linearGradient>""",
    "#ee9ca7" to
    """
        <linearGradient id="grad10" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#ee9ca7"/>
            <stop class="stop3" offset="100%" stop-color="#ffdde1"/>
        </linearGradient>""",
    "#734b6d" to
            """
        <linearGradient id="grad11" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#734b6d"/>
            <stop class="stop3" offset="100%" stop-color="#42275a"/>
        </linearGradient>""",
    "#bdc3c7" to
    """
        <linearGradient id="grad12" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#bdc3c7"/>
            <stop class="stop3" offset="100%" stop-color="#2c3e50"/>
        </linearGradient>""",
    "#ffb88c" to
    """
        <linearGradient id="grad13" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#ffb88c"/>
            <stop class="stop3" offset="100%" stop-color="#de6262"/>
        </linearGradient>""",
    "#06beb6" to
    """
        <linearGradient id="grad14" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#06beb6"/>
            <stop class="stop3" offset="100%" stop-color="#48b1bf"/>
        </linearGradient>""",
    "#f45c43" to
    """
        <linearGradient id="grad15" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#f45c43"/>
            <stop class="stop3" offset="100%" stop-color="#eb3349"/>
        </linearGradient>""",
    "#f7bb97" to
    """
        <linearGradient id="grad16" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#f7bb97"/>
            <stop class="stop3" offset="100%" stop-color="#dd5e89"/>
        </linearGradient>""",
    "#a8e063" to
    """
        <linearGradient id="grad17" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#a8e063"/>
            <stop class="stop3" offset="100%" stop-color="#56ab2f"/>
        </linearGradient>""",
    "#516395" to
    """
        <linearGradient id="grad18" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#516395"/>
            <stop class="stop3" offset="100%" stop-color="#614385"/>
        </linearGradient>""",
    "#eecda3" to
    """
        <linearGradient id="grad19" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#eecda3"/>
            <stop class="stop3" offset="100%" stop-color="#ef629f"/>
        </linearGradient>""",
    "#eacda3" to
    """
        <linearGradient id="grad20" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#eacda3"/>
            <stop class="stop3" offset="100%" stop-color="#d6ae7b"/>
        </linearGradient>""",
    "#02aab0" to
    """
        <linearGradient id="grad21" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#02aab0"/>
            <stop class="stop3" offset="100%" stop-color="#00cdac"/>
        </linearGradient>""",
    "#d66d75" to
    """
        <linearGradient id="grad22" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#d66d75"/>
            <stop class="stop3" offset="100%" stop-color="#e29587"/>
        </linearGradient>""",
    "#004e92" to
    """
        <linearGradient id="grad23" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#004e92"/>
            <stop class="stop3" offset="100%" stop-color="#000428"/>
        </linearGradient>""",
    "#243b55" to
    """
        <linearGradient id="grad24" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#243b55"/>
            <stop class="stop3" offset="100%" stop-color="#141e30"/>
        </linearGradient>""",
    "#aa076b" to
    """
        <linearGradient id="grad25" x2="0%" y2="100%" gradientUnits="userSpaceOnUse">
            <stop class="stop1" offset="0%" stop-color="#aa076b"/>
            <stop class="stop3" offset="100%" stop-color="#61045f"/>
        </linearGradient>
    """,
    "#f7ddcd" to """
       <linearGradient id="grad26" x2="0%" y2="100%">
            <stop stop-color="#f7ddcd" stop-opacity="1" offset="0%"/>
            <stop stop-color="hsl(24.0,72.4%,77.3%)" stop-opacity="1" offset="100%"/>
        </linearGradient> 
    """.trimIndent()
)

fun gradientMapToHsl(): Map<String, String> {
    var count = 0
    val gradMap = mutableMapOf<String,String>()
    gradients.forEach{
            k, v ->
        val hsl = hexToHsl(k)
        gradMap[k] = """
            <linearGradient id="grad$count" x2="0%" y2="100%">
            <stop stop-color="$k" stop-opacity="1" offset="0%"/>
            <stop stop-color="$hsl" stop-opacity="1" offset="100%"/>
        </linearGradient>
        """.trimIndent()
        count++
    }
    return gradMap
}
fun defLineGradMap(isPdf : Boolean, stopOp: Float = 0.8f): MutableCollection<String> {
    if(isPdf) {
        return allGradients()
    }
    val gradMap = mutableMapOf<String,String>()
    var count = 0
    gradients.forEach{
        k, v ->
        val hsl = hexToHsl(k)
        gradMap[k] = """
            <linearGradient id="grad$count" x2="0%" y2="100%">
            <stop stop-color="$k" stop-opacity="$stopOp" offset="0%"/>
            <stop stop-color="$hsl" stop-opacity="1" offset="100%"/>
        </linearGradient>
        """.trimIndent()
        count++
    }
    return gradMap.values
}
fun allGradientsKeys(): MutableSet<String> {
    return gradients.keys
}
fun allGradients(): MutableCollection<String> {
    return gradients.values
}