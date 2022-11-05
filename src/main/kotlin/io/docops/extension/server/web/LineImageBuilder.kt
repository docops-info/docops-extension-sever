package io.docops.extension.server.web



    fun makeLineImage(fillColor: String, fontColor: String, line1: String, line2: String?, line3: String?): String {
        val now = System.currentTimeMillis()
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg id="panelText" width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg"
                 xmlns:xlink="http://www.w3.org/1999/xlink">
                <path id="Color-Fill-$now" fill="$fillColor" stroke="none" d="M 0 0 L 512 0 L 512 512 L 0 512 Z"/>
                <g id="text-$now">
                    <text id="text1-$now" text-anchor="middle">
                        <tspan text-anchor="middle" x="50%" y="42%" font-family="Rubik Mono One" font-size="48" fill="$fontColor">${line1.uppercase()}</tspan>
                        <tspan text-anchor="middle" x="50%" y="54%" font-family="Rubik Mono One" font-size="48" fill="$fontColor">${line2?.uppercase()}</tspan>
                        <tspan text-anchor="middle" x="50%" y="66%" font-family="Rubik Mono One" font-size="48" fill="$fontColor">${line3?.uppercase()}</tspan>
                        </text>
                </g>
                <g id="Dividers">
                    <path id="Bottom-Divider-" fill="none" stroke="#3f4652" stroke-width="2.56" stroke-linecap="round"
                          stroke-linejoin="round" d="M 60.16 362.23999 L 451.839996 362.23999"/>
                    <path id="Top-Divider-" fill="none" stroke="#3f4652" stroke-width="2.56" stroke-linecap="round"
                          stroke-linejoin="round" d="M 60.16 149.76001 L 451.839996 149.76001"/>
                </g>
            </svg>

        """.trimIndent()
    }
