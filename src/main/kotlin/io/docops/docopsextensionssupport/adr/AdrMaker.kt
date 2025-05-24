package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.adr.model.Adr
import io.docops.docopsextensionssupport.adr.model.Status
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import java.util.UUID

class AdrMaker {

    private val xIndent = 35
    private val dy = 15.0f
    private val headerX = 20 // Consistent x-coordinate for all section headers
    fun makeAdrSvg(adr: Adr, dropShadow: Boolean = true, config: AdrParserConfig, useDark: Boolean) : String {
        val editorColor = if(useDark) EditorDark() else EditorLite()
        val sb = StringBuilder()
        sb.append(defs(editorColor))
        sb.append(setBackground(editorColor, config, useDark))
        sb.append(title(adr, editorColor))
        sb.append(status(adr, editorColor, mapBgFromStatus(adr = adr)))

        // We don't need the vertical line anymore since we're using cards
        // sb.append(makeOutline(editorColor))

        // Add some spacing after the title and status
        val contextOutcome = context(adr, editorColor)
        sb.append(contextOutcome.text)

        // No need for horizontal divider lines between sections since we have cards now
        val decisionOutcome = decision(adr, contextOutcome.lastYPosition+20, editorColor)
        sb.append(decisionOutcome.text)

        val consequencesOutcome = consequences(adr, decisionOutcome.lastYPosition+20, editorColor)
        sb.append(consequencesOutcome.text)

        val participantsOutcome = participants(adr, consequencesOutcome.lastYPosition+20, editorColor)
        sb.append(participantsOutcome.text)

        val wrapper = StringBuilder()
        // Add more space at the bottom of the SVG
        wrapper.append(head(width = 720.0f, height = participantsOutcome.lastYPosition+dy+20, config.scale)).append(sb).append(tail())
        return wrapper.toString()
    }

    fun head(width: Float = 720.0f, height: Float = 450.0f, scale: Float = 1.0f): String {
        return """
            <svg id="adr" xmlns="http://www.w3.org/2000/svg" width='${width * scale/ DISPLAY_RATIO_16_9 }' height='${height * scale/DISPLAY_RATIO_16_9}'
                 xmlns:xlink="http://www.w3.org/1999/xlink" font-family="arial"
                 viewBox="0 0 $width $height"
            >
        """.trimIndent()
    }
    fun defs(editorColor: EditorColor) : String {
        //language=html
        return """
            <defs>
            <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="2" />
                <feOffset dx="2" dy="2" result="offsetblur" />
                <feComponentTransfer>
                    <feFuncA type="linear" slope="0.2" />
                </feComponentTransfer>
                <feMerge>
                    <feMergeNode />
                    <feMergeNode in="SourceGraphic" />
                </feMerge>
            </filter>
            <style>
            .adrlink { fill: #6366F1; text-decoration: underline; font-weight: 500; }
            .adrlink:hover, .adrlink:active { outline: dotted 1px #6366F1; fill: #4F46E5; }
            </style>
            ${editorColor.backGrad().linearGradient}
            </defs>
        """.trimIndent()
    }
    fun tail() = "</svg>"

    fun setBackground(editorColor: EditorColor, config: AdrParserConfig, useDark: Boolean) : String {
        var fill = "url(#${editorColor.id})"
        if(config.isPdf || !useDark) {
            fill = editorColor.background
        }
        val sb = StringBuilder()
        sb.append("<rect width=\"100%\" height=\"100%\" fill='$fill' stroke=\"${editorColor.lineColor}\" stroke-width=\"2\" rx=\"5\" ry=\"5\"/>")

        // Add subtle grid pattern for light mode
        if (!useDark) {
            sb.append("""
                <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
                    <path d="M 20 0 L 0 0 0 20" fill="none" stroke="${editorColor.lineColor}" stroke-width="0.5" stroke-opacity="0.1"/>
                </pattern>
                <rect width="100%" height="100%" fill="url(#grid)" />
            """.trimIndent())
        }

        return sb.toString()
    }
    fun title(adr: Adr, editorColor: EditorColor): String {

        return """<text x="50%" y="30" text-anchor="middle" fill="${editorColor.titleColor}" filter="url(#dropShadow)"
          style="font-weight: 700; font-size: 26px; font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji; letter-spacing: 0.5px;">
            ${adr.title}
        </text>"""
    }
    fun status(adr: Adr, editorColor: EditorColor, mapBgFromStatus: String): String {
        return """
    <text x="5" y="50" fill="$mapBgFromStatus"
          style="font-size:0.9em; font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji; letter-spacing: 0.2px;">
        <tspan font-weight="600">Status:</tspan> ${adr.status} <tspan font-weight="600" dx="50">Date:</tspan> ${adr.date}
    </text>
        """.trimIndent()
    }
    fun makeOutline(editorColor: EditorColor): String {
        return """
        <line x1="0" y1="60" x2="725.0" y2="60" stroke="${editorColor.lineColor}" stroke-width="2" stroke-linecap="round"/>
        <line x1="95" y1="60" x2="95.0" y2="705" stroke="${editorColor.lineColor}" stroke-width="2" stroke-linecap="round"/>
        """.trimIndent()
    }
    fun context(adr: Adr, editorColor: EditorColor): RowTextOutcome {
        val text = StringBuilder()
        var lineCount = 0;

        // Calculate header height and position
        val headerHeight = 30f
        val headerY = 70f
        val contentY = headerY + headerHeight + 10

        // Create header card with a slightly darker shade
        val headerColor = editorColor.lineColor
        val headerBgColor = if (editorColor is EditorLite) "#EEF2FF" else "#4B5563" // Indigo-50 for light, Gray-600 for dark

        text.append("""
         <rect x="10" y="$headerY" width="700" height="$headerHeight" rx="4" ry="4" 
               fill="$headerBgColor" filter="url(#dropShadow)" />
         <text x="$headerX" y="${headerY + 20}"
              style="fill: ${headerColor};font-weight: 600; font-size: 14px;font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;"
              >Context
        </text>
        """.trimIndent())

        // Calculate content height
        val contentHeight = (adr.context.size * dy) + 30 // Add more padding at the bottom

        // Create content card with a lighter shade
        val contentBgColor = if (editorColor is EditorLite) "#F5F7FF" else "#374151" // Lighter indigo for light, Gray-700 for dark

        text.append("""
            <rect x="30" y="$contentY" width="680" height="$contentHeight" rx="4" ry="4" 
                  fill="$contentBgColor" filter="url(#dropShadow)" />
            <text x="$xIndent" y="${contentY + 15}"
              style="fill: ${editorColor.textColor};font-weight: normal; font-size: 12px;font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji; line-height: 1.5;">
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

        // Return the position after the content rectangle, including its full height
        return RowTextOutcome(text.toString(), contentY + contentHeight)
    }
    fun decision(adr: Adr, startY: Float, editorColor: EditorColor): RowTextOutcome {
        val text = StringBuilder()
        var lineCount = 0;

        // Calculate header height and position
        val headerHeight = 30f
        val headerY = startY - 10 // Adjust for better spacing
        val contentY = headerY + headerHeight + 10

        // Create header card with a slightly darker shade
        val headerColor = editorColor.lineColor
        val headerBgColor = if (editorColor is EditorLite) "#EEF2FF" else "#4B5563" // Indigo-50 for light, Gray-600 for dark

        text.append("""
         <rect x="10" y="$headerY" width="700" height="$headerHeight" rx="4" ry="4" 
               fill="$headerBgColor" filter="url(#dropShadow)" />
         <text x="$headerX" y="${headerY + 20}"
              style="fill: ${headerColor};font-weight: 600; font-size: 14px;font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;"
              >Decision
        </text>
        """.trimIndent())

        // Calculate content height
        val contentHeight = (adr.decision.size * dy) + 30 // Add more padding at the bottom

        // Create content card with a lighter shade
        val contentBgColor = if (editorColor is EditorLite) "#F5F7FF" else "#374151" // Lighter indigo for light, Gray-700 for dark

        text.append("""
            <rect x="30" y="$contentY" width="680" height="$contentHeight" rx="4" ry="4" 
                  fill="$contentBgColor" filter="url(#dropShadow)" />
            <text x="$xIndent" y="${contentY + 15}"
              style="fill: ${editorColor.textColor};font-weight: normal; font-size: 12px;font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji; line-height: 1.5;">
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
        // Return the position after the content rectangle, including its full height
        return RowTextOutcome(text.toString(), contentY + contentHeight)
    }
    fun consequences(adr: Adr, startY: Float, editorColor: EditorColor): RowTextOutcome {
        val text = StringBuilder()
        var lineCount = 0;

        // Calculate header height and position
        val headerHeight = 30f
        val headerY = startY - 10 // Adjust for better spacing
        val contentY = headerY + headerHeight + 10

        // Create header card with a slightly darker shade
        val headerColor = editorColor.lineColor
        val headerBgColor = if (editorColor is EditorLite) "#EEF2FF" else "#4B5563" // Indigo-50 for light, Gray-600 for dark

        text.append("""
         <rect x="10" y="$headerY" width="700" height="$headerHeight" rx="4" ry="4" 
               fill="$headerBgColor" filter="url(#dropShadow)" />
         <text x="$headerX" y="${headerY + 20}"
              style="fill: ${headerColor};font-weight: 600; font-size: 14px;font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;"
              >Consequences
        </text>
        """.trimIndent())

        // Calculate content height
        val contentHeight = (adr.consequences.size * dy) + 30 // Add more padding at the bottom

        // Create content card with a lighter shade
        val contentBgColor = if (editorColor is EditorLite) "#F5F7FF" else "#374151" // Lighter indigo for light, Gray-700 for dark

        text.append("""
            <rect x="30" y="$contentY" width="680" height="$contentHeight" rx="4" ry="4" 
                  fill="$contentBgColor" filter="url(#dropShadow)" />
            <text x="$xIndent" y="${contentY + 15}"
              style="fill: ${editorColor.textColor};font-weight: normal; font-size: 12px;font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji; line-height: 1.5;">
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
        // Return the position after the content rectangle, including its full height
        return RowTextOutcome(text.toString(), contentY + contentHeight)
    }
    /**
     * Creates a stick figure SVG representation
     * @param x The x coordinate for the stick figure
     * @param y The y coordinate for the stick figure
     * @param name The name to display under the stick figure
     * @param textColor The color for the text and stick figure
     * @return SVG string representing a stick figure with name underneath
     */
    private fun createStickFigure(x: Float, y: Float, name: String, textColor: String): String {
        val figureHeight = 50f // Height of the stick figure
        val headRadius = 10f
        val bodyLength = 20f
        val limbLength = 15f

        return """
            <!-- Stick figure for $name -->
            <g transform="translate($x, $y)">
                <!-- Head -->
                <circle cx="0" cy="0" r="$headRadius" fill="none" stroke="$textColor" stroke-width="1.5" />

                <!-- Body -->
                <line x1="0" y1="$headRadius" x2="0" y2="${headRadius + bodyLength}" 
                      stroke="$textColor" stroke-width="1.5" stroke-linecap="round" />

                <!-- Arms -->
                <line x1="-${limbLength}" y1="${headRadius + 10}" x2="${limbLength}" y2="${headRadius + 10}" 
                      stroke="$textColor" stroke-width="1.5" stroke-linecap="round" />

                <!-- Legs -->
                <line x1="0" y1="${headRadius + bodyLength}" x2="-${limbLength}" y2="${headRadius + bodyLength + limbLength}" 
                      stroke="$textColor" stroke-width="1.5" stroke-linecap="round" />
                <line x1="0" y1="${headRadius + bodyLength}" x2="${limbLength}" y2="${headRadius + bodyLength + limbLength}" 
                      stroke="$textColor" stroke-width="1.5" stroke-linecap="round" />

                <!-- Name -->
                <text x="0" y="${headRadius + bodyLength + limbLength + 15}" 
                      text-anchor="middle" fill="$textColor" 
                      style="font-size: 12px; font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;">
                    $name
                </text>
            </g>
        """.trimIndent()
    }

    fun participants(adr: Adr, startY: Float, editorColor: EditorColor): RowTextOutcome {
        val text = StringBuilder()

        // Calculate header height and position
        val headerHeight = 30f
        val headerY = startY - 10 // Adjust for better spacing
        val contentY = headerY + headerHeight + 10

        // Create header card with a slightly darker shade
        val headerColor = editorColor.lineColor
        val headerBgColor = if (editorColor is EditorLite) "#EEF2FF" else "#4B5563" // Indigo-50 for light, Gray-600 for dark

        text.append("""
         <rect x="10" y="$headerY" width="700" height="$headerHeight" rx="4" ry="4" 
               fill="$headerBgColor" filter="url(#dropShadow)" />
         <text x="$headerX" y="${headerY + 20}"
              style="fill: ${headerColor};font-weight: 600; font-size: 14px;font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;"
              >Participants
        </text>
        """.trimIndent())

        // Calculate dimensions for stick figures
        val figureWidth = 100f // Width allocated for each stick figure
        val figureHeight = 100f // Height needed for stick figure + name
        val participants = adr.participants.filter { it.isNotBlank() }

        // Calculate content dimensions
        val maxParticipantsPerRow = 6 // Maximum number of participants per row
        val rows = (participants.size + maxParticipantsPerRow - 1) / maxParticipantsPerRow // Ceiling division
        val contentHeight = rows * figureHeight + 20 // Add padding

        // Create content card with a lighter shade
        val contentBgColor = if (editorColor is EditorLite) "#F5F7FF" else "#374151" // Lighter indigo for light, Gray-700 for dark

        text.append("""
            <rect x="30" y="$contentY" width="680" height="$contentHeight" rx="4" ry="4" 
                  fill="$contentBgColor" filter="url(#dropShadow)" />
        """.trimIndent())

        // Process participants: split comma-separated names into individual participants
        val participantList = mutableListOf<String>()
        adr.participants.forEach { participantEntry ->
            // Split by comma and trim each name
            participantEntry.split(",").forEach { name ->
                val trimmedName = name.trim()
                if (trimmedName.isNotEmpty()) {
                    participantList.add(trimmedName)
                }
            }
        }

        // Add stick figures for each participant
        participantList.forEachIndexed { index, participant ->
            val row = index / maxParticipantsPerRow
            val col = index % maxParticipantsPerRow

            // Calculate position for this stick figure
            val xPos = 30f + 50f + (col * figureWidth) // Start with padding + half figure width
            val yPos = contentY + 30f + (row * figureHeight) // Start with padding


            text.append(createStickFigure(xPos, yPos, participant.trim(), mapBgFromStatus(adr)))
        }

        return RowTextOutcome(text.toString(), contentY + contentHeight)
    }
    fun mapBgFromStatus(adr: Adr): String {
        when {
            Status.Proposed == adr.status -> return "#6366F1"  // Indigo
            Status.Accepted == adr.status -> return "#10B981"  // Emerald
            Status.Superseded == adr.status -> return "#F59E0B"  // Amber
            Status.Deprecated == adr.status -> return "#EF4444"  // Red
            Status.Rejected == adr.status -> return "#DC2626"  // Red-600
        }
        return "#6B7280"  // Gray-500
    }
}

open class EditorColor(val background: String = "#F7F7F7", val lineColor: String = "#111111", val textColor: String = "#000000", val titleColor: String = "#000000", val id: String = UUID.randomUUID().toString()){

}
class EditorLite(background: String = "#ffffff", lineColor: String="#4361ee", textColor: String="#374151", titleColor: String="#3B82F6"): EditorColor(background, lineColor, textColor, titleColor)
class EditorDark(background: String = "#1E293B",  lineColor: String = "#818CF8",  textColor: String = "#E5E7EB", titleColor: String = "#A5B4FC"): EditorColor(background, lineColor, textColor, titleColor)
class RowTextOutcome(val text: String, val lastYPosition: Float)

fun EditorColor.backGrad(): SVGColor {
    return SVGColor(background, id)
}
