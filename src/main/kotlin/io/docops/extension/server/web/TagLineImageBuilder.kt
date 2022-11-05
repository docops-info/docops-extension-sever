package io.docops.extension.server.web

fun makeTagLine(fillColor: String, fontColor: String, line1: String, line2: String?, line3: String?) : String {
    var start = 420
    val now = System.currentTimeMillis()
    return """
        <?xml version="1.0" encoding="UTF-8"?>
<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
    <path id="Color-Fill" fill="$fillColor" stroke="none" d="M 0 0 L 512 0 L 512 512 L 0 512 Z"/>
    <g id="mylogo-$now">
        <text id="Subtitle-$now" text-anchor="middle" x="50%" y="61%" font-family="Helvetica Neue" font-size="28.16" fill="$fontColor" letter-spacing="1.408">${line3?.uppercase()}</text>
        <text id="innertext-$now" text-anchor="middle" x="50%">
            <tspan text-anchor="middle" x="50%" y="40%" font-family="DM Serif Display" font-size="90" fill="$fontColor">${line1.uppercase()}</tspan>
            <tspan text-anchor="middle" x="50%" y="55%" font-family="DM Serif Display" font-size="90" fill="$fontColor" >${line2?.uppercase()}</tspan>
        </text>
    </g>
</svg>

    """.trimIndent()
}