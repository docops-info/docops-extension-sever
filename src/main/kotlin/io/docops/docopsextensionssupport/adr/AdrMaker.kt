package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.adr.model.Adr
import io.docops.docopsextensionssupport.adr.model.Status
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import java.util.UUID

class AdrMaker {

    private val xIndent = 95
    private val dy = 15.0f
    fun makeAdrSvg(adr: Adr, dropShadow: Boolean = true, config: AdrParserConfig, useDark: Boolean) : String {
        val editorColor = if(useDark) EditorDark() else EditorLite()
        val sb = StringBuilder()
        sb.append(defs(editorColor))
        sb.append(setBackground(editorColor, config, useDark))
        sb.append(title(adr, editorColor))
        sb.append(status(adr, editorColor, mapBgFromStatus(adr = adr)))
        sb.append(makeOutline(editorColor))
        val contextOutcome = context(adr, editorColor)
        sb.append(contextOutcome.text)

        sb.append("""<line x1="0" y1="${contextOutcome.lastYPosition+75-15}" x2="725.0" y2="${contextOutcome.lastYPosition+75-15}" stroke="${editorColor.lineColor}"/>""")
        val decisionOutcome = decision(adr, contextOutcome.lastYPosition+75, editorColor)
        sb.append(decisionOutcome.text)
        sb.append("""<line x1="0" y1="${decisionOutcome.lastYPosition-15}" x2="725.0" y2="${decisionOutcome.lastYPosition-15}" stroke="${editorColor.lineColor}"/>""")
        val consequencesOutcome = consequences(adr, decisionOutcome.lastYPosition, editorColor)
        sb.append(consequencesOutcome.text)
        sb.append("""<line x1="0" y1="${consequencesOutcome.lastYPosition-15}" x2="725.0" y2="${consequencesOutcome.lastYPosition-15}" stroke="${editorColor.lineColor}"/>""")
        val participantsOutcome = participants(adr, consequencesOutcome.lastYPosition, editorColor)
        sb.append(participantsOutcome.text)
        val wrapper = StringBuilder()
        wrapper.append(head(width = 720.0f, height = participantsOutcome.lastYPosition+dy)).append(sb).append(tail())
        return wrapper.toString()
    }

    fun head(width: Float = 720.0f, height: Float = 450.0f): String {
        return """
            <svg id="adr" xmlns="http://www.w3.org/2000/svg" width='${width/ DISPLAY_RATIO_16_9}' height='${height/DISPLAY_RATIO_16_9}'
                 xmlns:xlink="http://www.w3.org/1999/xlink" font-family="arial"
                 viewBox="0 0 $width $height"
            >
        """.trimIndent()
    }
    fun defs(editorColor: EditorColor) : String {
        //language=html
        return """
            <defs>
            <style>
            .adrlink { fill: #5AB2FF; text-decoration: underline; }
            .adrlink:hover, .adrlink:active { outline: dotted 1px #5AB2FF; }
            </style>
            ${editorColor.backGrad().linearGradient}
            </defs>
        """.trimIndent()
    }
    fun tail() = "</svg>"

    fun setBackground(editorColor: EditorColor, config: AdrParserConfig, useDark: Boolean) : String {
        var fill= "url(#${editorColor.id})"
        if(config.isPdf || !useDark) {
            fill = editorColor.background
        }
        return "<rect width=\"100%\" height=\"100%\" fill='$fill' stroke=\"${editorColor.lineColor}\" stroke-width=\"3\"/>"
    }
    fun title(adr: Adr, editorColor: EditorColor): String {

        return """<text x="50%" y="25" text-anchor="middle" fill="${editorColor.titleColor}"
          style="font-weight: bold; font-size: 24px;font-family: font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji">
            ${adr.title}
        </text>"""
    }
    fun status(adr: Adr, editorColor: EditorColor, mapBgFromStatus: String): String {
        return """
    <text x="5" y="45" fill="$mapBgFromStatus"
          style="font-size:0.8em; font-family: font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji">
        <tspan font-weight="bold">Status:</tspan> ${adr.status} <tspan font-weight="bold" dx="40">Date:</tspan> ${adr.date}
    </text>
        """.trimIndent()
    }
    fun makeOutline(editorColor: EditorColor): String {
        return """
        <line x1="0" y1="55" x2="725.0" y2="55" stroke="${editorColor.lineColor}"/>
        <line x1="90" y1="55" x2="90.0" y2="705" stroke="${editorColor.lineColor}"/>
        """.trimIndent()
    }
    fun context(adr: Adr, editorColor: EditorColor): RowTextOutcome {
        val text = StringBuilder()
        var lineCount = 0;
        text.append("""
         <text x="5" y="75"
              style="fill: ${editorColor.textColor};font-weight: bold; font-size: 11px;font-family: font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji; font-weight: bold;"
              text-decoration="underline">Context:
        </text>
        """.trimIndent())


        text.append("""
            <text x="$xIndent" y="75"
          style="fill: ${editorColor.textColor};font-weight: normal; font-size: 11px;font-family: font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;">
        """.trimIndent())

        adr.context.forEach {  s ->
            var y = 0.0f
            if(lineCount>0)
            {
                y= dy
            }
            text.append("""
            <tspan x="$xIndent" dy="$y">$s</tspan>
        """.trimIndent())
            lineCount++
        }
        text.append("</text>")

        return RowTextOutcome(text.toString(), lineCount * dy)
    }
    fun decision(adr: Adr, startY: Float, editorColor: EditorColor): RowTextOutcome {
        val text = StringBuilder()
        var lineCount = 0;
        text.append("""
         <text x="5" y="$startY"
              style="fill: ${editorColor.textColor};font-weight: bold; font-size: 11px;font-family: font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji; font-weight: bold;"
              text-decoration="underline">Decision:
        </text>
        <text x="$xIndent" y="$startY"
          style="fill: ${editorColor.textColor};font-weight: normal; font-size: 11px;font-family: font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;">
        """.trimIndent())
        adr.decision.forEach { d ->
            var y = 0.0f
            if(lineCount>0)
            {
                y= dy
            }
            text.append("""
            <tspan x="$xIndent" dy="$y">$d</tspan>
        """.trimIndent())
            lineCount++
        }
        text.append("</text>")
        return RowTextOutcome(text.toString(), lineCount * dy + startY)
    }
    fun consequences(adr: Adr, startY: Float, editorColor: EditorColor): RowTextOutcome {
        val text = StringBuilder()
        var lineCount = 0;
        text.append("""
         <text x="5" y="$startY"
              style="fill: ${editorColor.textColor};font-weight: bold; font-size: 11px;font-family: font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji; font-weight: bold;"
              text-decoration="underline">Consequences:
        </text>
        <text x="$xIndent" y="$startY"
          style="fill: ${editorColor.textColor};font-weight: normal; font-size: 11px;font-family: font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;">
        """.trimIndent())
        adr.consequences.forEach { d ->
            var y = 0.0f
            if(lineCount>0)
            {
                y= dy
            }
            text.append("""
            <tspan x="$xIndent" dy="$y">$d</tspan>
        """.trimIndent())
            lineCount++
        }
        text.append("</text>")
        return RowTextOutcome(text.toString(), lineCount * dy + startY)
    }
    fun participants(adr: Adr, startY: Float, editorColor: EditorColor): RowTextOutcome {
        val text = StringBuilder()
        var lineCount = 0;
        text.append("""
         <text x="5" y="$startY"
              style="fill: ${editorColor.textColor};font-weight: bold; font-size: 11px;font-family: font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji; font-weight: bold;"
              text-decoration="underline">Participants:
        </text>
        <text x="$xIndent" y="$startY"
          style="fill: ${editorColor.textColor};font-weight: normal; font-size: 11px;font-family: font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;">
        """.trimIndent())
            val y = 0.0f
            text.append("""
            <tspan x="$xIndent" dy="$y">${adr.participantAsStr()}</tspan>
        """.trimIndent())
        text.append("</text>")
        return RowTextOutcome(text.toString(), lineCount * dy + startY)
    }
    fun mapBgFromStatus(adr: Adr): String {
        when {
            Status.Proposed == adr.status -> return "#39A7FF"
            Status.Accepted == adr.status -> return "#65B741"
            Status.Superseded == adr.status -> return "#F49D1A"
            Status.Deprecated == adr.status -> return "#FB6D48"
            Status.Rejected == adr.status -> return "#C40C0C"
        }
        return "#fcfcfc"
    }
}

open class EditorColor(val background: String = "#F7F7F7", val lineColor: String = "#111111", val textColor: String = "#000000", val titleColor: String = "#000000", val id: String = UUID.randomUUID().toString()){

}
class EditorLite(background: String = "#fcfcfc", lineColor: String="#E4D0D0", textColor: String="#000000", titleColor: String="#000000"): EditorColor(background, lineColor, textColor, titleColor)
class EditorDark(background: String = "#1f2937",  lineColor: String = "#fcfcfc",  textColor: String = "#ABB2BF", titleColor: String = "#FCFCFC"): EditorColor(background, lineColor, textColor, titleColor)
class RowTextOutcome(val text: String, val lastYPosition: Float)

fun EditorColor.backGrad(): SVGColor {
    return SVGColor(background, id)
}