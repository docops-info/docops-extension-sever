package io.docops.extension.server.web

import kotlin.math.floor




class ColorDivCreator {

    fun gen(num: Int): ByteArray {
        val txt = StringBuilder("`colorMap {\n")
        val sb = StringBuilder()
        for (x in 1..num) {
            //language=html
            val c = getRandomColor()
            sb.append("""
    <div class="colorBox">
        <div class="color" style="background-color:$c"></div>
        <p class="code">${c}</p>
    </div>
    
    """.trimIndent()
            )
            txt.append("\tcolor(\"$c\")\n")
        }
        //language=html
        txt.append("}`")
        sb.append("\n")
        //language=html
        sb.append("""
        <script>
        var txt = $txt;
        </script>
        """.trimIndent())
        return sb.toString().toByteArray()
    }


    private fun getRandomColor(): String {
        val letters = "0123456789ABCDEF"
        var  color = "#"
        for (i in 0 .. 5) {
            color += letters[floor(Math.random() * 16).toInt()]
        }
        return color
    }
}