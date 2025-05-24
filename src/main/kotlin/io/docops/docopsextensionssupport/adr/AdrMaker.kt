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
        sb.append(defs(editorColor, adr))
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
    fun defs(editorColor: EditorColor, adr: Adr) : String {
        //language=html
        val statusColor = mapBgFromStatus(adr)
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

            <!-- Bullet point symbols -->
            <symbol id="bullet-dash" viewBox="0 0 24 24" width="12" height="12">
                <!-- Double right chevron -->
                <path d="M13.5 12L7.5 6L9 4.5L16.5 12L9 19.5L7.5 18L13.5 12Z M18.5 12L12.5 6L14 4.5L21.5 12L14 19.5L12.5 18L18.5 12Z" 
                      fill="${statusColor}" />
            </symbol>

            <symbol id="bullet-star" viewBox="0 0 24 24" width="12" height="12">
                <!-- Star shape -->
                <path d="M12 17.27L18.18 21L16.54 13.97L22 9.24L14.81 8.63L12 2L9.19 8.63L2 9.24L7.46 13.97L5.82 21L12 17.27Z" 
                      fill="${statusColor}" />
            </symbol>

            <symbol id="bullet-plus" viewBox="0 0 24 24" width="12" height="12">
                <!-- Circular bullet point -->
                <circle cx="12" cy="12" r="6" fill="${statusColor}" />
            </symbol>

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
        val bulletPoints = mutableListOf<Triple<String, Float, Float>>() // type, x, y

        // Calculate header height and position
        val headerHeight = 30f
        val headerY = 70f
        val contentY = headerY + headerHeight + 10

        // Create header card with a slightly darker shade
        val headerBgColor = if (editorColor is EditorLite) "#F3F4F6" else "#4B5563" // Gray-100 for light, Gray-600 for dark

        // Use status color for header text
        val statusColor = mapBgFromStatus(adr)

        text.append("""
         <rect x="10" y="$headerY" width="700" height="$headerHeight" rx="4" ry="4" 
               fill="$headerBgColor" filter="url(#dropShadow)" />
         <text x="$headerX" y="${headerY + 20}"
              style="fill: ${statusColor};font-weight: 600; font-size: 14px;font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;"
              >Context
        </text>
        """.trimIndent())

        // Calculate content height
        val contentHeight = (adr.context.size * dy) + 30 // Add more padding at the bottom

        // Create content card with a lighter shade
        val contentBgColor = if (editorColor is EditorLite) "#F9FAFB" else "#374151" // Gray-50 for light, Gray-700 for dark

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

            // Check if the line has a bullet point marker
            if (s.startsWith("BULLET_DASH:") || s.startsWith("BULLET_STAR:") || s.startsWith("BULLET_PLUS:")) {
                val bulletType = when {
                    s.startsWith("BULLET_DASH:") -> "bullet-dash"
                    s.startsWith("BULLET_STAR:") -> "bullet-star"
                    else -> "bullet-plus"
                }

                // Extract the text without the bullet marker
                val textContent = when {
                    s.startsWith("BULLET_DASH:") -> s.substring("BULLET_DASH:".length)
                    s.startsWith("BULLET_STAR:") -> s.substring("BULLET_STAR:".length)
                    else -> s.substring("BULLET_PLUS:".length)
                }

                // Always start a new line for bullet points
                val bulletY = if (lineCount == 0) dy else y

                // Calculate the y position for the bullet point
                val bulletPointY = contentY + 15 + (lineCount * dy) + 6

                // Store bullet point information for later rendering
                bulletPoints.add(Triple(bulletType, xIndent.toFloat(), bulletPointY))

                // Add the text with proper indentation
                text.append("""
                <tspan x="$xIndent" dy="$bulletY">
                    <tspan dx="16">$textContent</tspan>
                </tspan>
                """.trimIndent())
            } else {
                text.append("""
                <tspan x="$xIndent" dy="$y">$s</tspan>
                """.trimIndent())
            }
            lineCount++
        }
        text.append("</text>")

        // Add bullet points after the text
        bulletPoints.forEach { (type, x, y) ->
            text.append("""
            <use xlink:href="#$type" x="$x" y="$y"/>
            """.trimIndent())
        }

        // Return the position after the content rectangle, including its full height
        return RowTextOutcome(text.toString(), contentY + contentHeight)
    }
    fun decision(adr: Adr, startY: Float, editorColor: EditorColor): RowTextOutcome {
        val text = StringBuilder()
        var lineCount = 0;
        val bulletPoints = mutableListOf<Triple<String, Float, Float>>() // type, x, y

        // Calculate header height and position
        val headerHeight = 30f
        val headerY = startY - 10 // Adjust for better spacing
        val contentY = headerY + headerHeight + 10

        // Create header card with a slightly darker shade
        val headerBgColor = if (editorColor is EditorLite) "#F3F4F6" else "#4B5563" // Gray-100 for light, Gray-600 for dark

        // Use status color for header text
        val statusColor = mapBgFromStatus(adr)

        text.append("""
         <rect x="10" y="$headerY" width="700" height="$headerHeight" rx="4" ry="4" 
               fill="$headerBgColor" filter="url(#dropShadow)" />
         <text x="$headerX" y="${headerY + 20}"
              style="fill: ${statusColor};font-weight: 600; font-size: 14px;font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;"
              >Decision
        </text>
        """.trimIndent())

        // Calculate content height
        val contentHeight = (adr.decision.size * dy) + 30 // Add more padding at the bottom

        // Create content card with a lighter shade
        val contentBgColor = if (editorColor is EditorLite) "#F9FAFB" else "#374151" // Gray-50 for light, Gray-700 for dark

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

            // Check if the line has a bullet point marker
            if (d.startsWith("BULLET_DASH:") || d.startsWith("BULLET_STAR:") || d.startsWith("BULLET_PLUS:")) {
                val bulletType = when {
                    d.startsWith("BULLET_DASH:") -> "bullet-dash"
                    d.startsWith("BULLET_STAR:") -> "bullet-star"
                    else -> "bullet-plus"
                }

                // Extract the text without the bullet marker
                val textContent = when {
                    d.startsWith("BULLET_DASH:") -> d.substring("BULLET_DASH:".length)
                    d.startsWith("BULLET_STAR:") -> d.substring("BULLET_STAR:".length)
                    else -> d.substring("BULLET_PLUS:".length)
                }

                // Always start a new line for bullet points
                val bulletY = if (lineCount == 0) dy else y

                // Calculate the y position for the bullet point
                val bulletPointY = contentY + 15 + (lineCount * dy) + 6

                // Store bullet point information for later rendering
                bulletPoints.add(Triple(bulletType, xIndent.toFloat(), bulletPointY))

                // Add the text with proper indentation
                text.append("""
                <tspan x="$xIndent" dy="$bulletY">
                    <tspan dx="16">$textContent</tspan>
                </tspan>
                """.trimIndent())
            } else {
                text.append("""
                <tspan x="$xIndent" dy="$y">$d</tspan>
                """.trimIndent())
            }
            lineCount++
        }
        text.append("</text>")

        // Add bullet points after the text
        bulletPoints.forEach { (type, x, y) ->
            text.append("""
            <use xlink:href="#$type" x="$x" y="$y"/>
            """.trimIndent())
        }
        // Return the position after the content rectangle, including its full height
        return RowTextOutcome(text.toString(), contentY + contentHeight)
    }
    fun consequences(adr: Adr, startY: Float, editorColor: EditorColor): RowTextOutcome {
        val text = StringBuilder()
        var lineCount = 0;
        val bulletPoints = mutableListOf<Triple<String, Float, Float>>() // type, x, y

        // Calculate header height and position
        val headerHeight = 30f
        val headerY = startY - 10 // Adjust for better spacing
        val contentY = headerY + headerHeight + 10

        // Create header card with a slightly darker shade
        val headerBgColor = if (editorColor is EditorLite) "#F3F4F6" else "#4B5563" // Gray-100 for light, Gray-600 for dark

        // Use status color for header text
        val statusColor = mapBgFromStatus(adr)

        text.append("""
         <rect x="10" y="$headerY" width="700" height="$headerHeight" rx="4" ry="4" 
               fill="$headerBgColor" filter="url(#dropShadow)" />
         <text x="$headerX" y="${headerY + 20}"
              style="fill: ${statusColor};font-weight: 600; font-size: 14px;font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;"
              >Consequences
        </text>
        """.trimIndent())

        // Calculate content height
        val contentHeight = (adr.consequences.size * dy) + 30 // Add more padding at the bottom

        // Create content card with a lighter shade
        val contentBgColor = if (editorColor is EditorLite) "#F9FAFB" else "#374151" // Gray-50 for light, Gray-700 for dark

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

            // Check if the line has a bullet point marker
            if (d.startsWith("BULLET_DASH:") || d.startsWith("BULLET_STAR:") || d.startsWith("BULLET_PLUS:")) {
                val bulletType = when {
                    d.startsWith("BULLET_DASH:") -> "bullet-dash"
                    d.startsWith("BULLET_STAR:") -> "bullet-star"
                    else -> "bullet-plus"
                }

                // Extract the text without the bullet marker
                val textContent = when {
                    d.startsWith("BULLET_DASH:") -> d.substring("BULLET_DASH:".length)
                    d.startsWith("BULLET_STAR:") -> d.substring("BULLET_STAR:".length)
                    else -> d.substring("BULLET_PLUS:".length)
                }

                // Always start a new line for bullet points
                val bulletY = if (lineCount == 0) dy else y

                // Calculate the y position for the bullet point
                val bulletPointY = contentY + 15 + (lineCount * dy) + 6

                // Store bullet point information for later rendering
                bulletPoints.add(Triple(bulletType, xIndent.toFloat(), bulletPointY))

                // Add the text with proper indentation
                text.append("""
                <tspan x="$xIndent" dy="$bulletY">
                    <tspan dx="16">$textContent</tspan>
                </tspan>
                """.trimIndent())
            } else {
                text.append("""
                <tspan x="$xIndent" dy="$y">$d</tspan>
                """.trimIndent())
            }
            lineCount++
        }
        text.append("</text>")

        // Add bullet points after the text
        bulletPoints.forEach { (type, x, y) ->
            text.append("""
            <use xlink:href="#$type" x="$x" y="$y"/>
            """.trimIndent())
        }
        // Return the position after the content rectangle, including its full height
        return RowTextOutcome(text.toString(), contentY + contentHeight)
    }
    /**
     * Creates a Font Awesome style person figure SVG representation
     * @param x The x coordinate for the person figure
     * @param y The y coordinate for the person figure
     * @param name The name to display under the person figure
     * @param textColor The color for the text and person figure
     * @return SVG string representing a person figure with name underneath
     */
    private fun createPersonFigure(x: Float, y: Float, name: String, textColor: String): String {
        // Font Awesome style person icon path
        val personPath = "M256 288c79.5 0 144-64.5 144-144S335.5 0 256 0 112 64.5 112 144s64.5 144 144 144zm128 32h-55.1c-22.2 10.2-46.9 16-72.9 16s-50.6-5.8-72.9-16H128C57.3 320 0 377.3 0 448v16c0 26.5 21.5 48 48 48h416c26.5 0 48-21.5 48-48v-16c0-70.7-57.3-128-128-128z"

        // Scale factor for the icon (adjust as needed)
        val scale = 0.05

        // Calculate the maximum width for text wrapping
        val maxTextWidth = 140f

        // Split the name into words for potential wrapping
        val words = name.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        // Simple text wrapping algorithm
        for (word in words) {
            if (currentLine.isEmpty()) {
                currentLine = word
            } else if ((currentLine.length + word.length + 1) * 6 < maxTextWidth) { // Rough estimate of text width
                currentLine += " $word"
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        val textY = 50f // Base position for text
        val lineHeight = 15f // Height between text lines

        val textElements = lines.mapIndexed { index, line ->
            """<text x="0" y="${textY + (index * lineHeight)}" 
                  text-anchor="middle" fill="$textColor" 
                  style="font-size: 12px; font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;">
                $line
            </text>"""
        }.joinToString("\n")

        return """
            <!-- Person figure for $name -->
            <g transform="translate($x, $y)">
                <!-- Font Awesome style person icon -->
                <g transform="scale($scale) translate(-256, -256)">
                    <path d="$personPath" fill="$textColor" />
                </g>

                <!-- Name with potential wrapping -->
                $textElements
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
        val headerBgColor = if (editorColor is EditorLite) "#F3F4F6" else "#4B5563" // Gray-100 for light, Gray-600 for dark

        // Use status color for header text
        val statusColor = mapBgFromStatus(adr)

        text.append("""
         <rect x="10" y="$headerY" width="700" height="$headerHeight" rx="4" ry="4" 
               fill="$headerBgColor" filter="url(#dropShadow)" />
         <text x="$headerX" y="${headerY + 20}"
              style="fill: ${statusColor};font-weight: 600; font-size: 14px;font-family: Roboto,Helvetica Neue,Vazirmatn,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji;"
              >Participants
        </text>
        """.trimIndent())

        // Calculate dimensions for person figures with more space for text
        val figureWidth = 150f // Increased width allocated for each person figure
        val figureHeight = 120f // Increased height needed for person figure + multi-line name
        val participants = adr.participants.filter { it.isNotBlank() }

        // Calculate content dimensions with fewer participants per row to accommodate longer text
        val maxParticipantsPerRow = 4 // Reduced from 6 to 4 participants per row

        // Process participants: split comma-separated names into individual participants
        val participantList = mutableListOf<String>()
        adr.participants.forEach { participantEntry ->
            // The input format is expected to be "Name (Role), Name (Role)"
            // We need to be careful about splitting by commas that are not inside parentheses

            // Use regex to match participants with their roles
            // This regex matches: name followed by optional role in parentheses
            val regex = """([^,]+(?:\([^)]*\))?)(?:,\s*|$)""".toRegex()
            val matches = regex.findAll(participantEntry)

            matches.forEach { match ->
                val participant = match.groupValues[1].trim()
                if (participant.isNotEmpty()) {
                    participantList.add(participant)
                }
            }
        }

        val rows = (participantList.size + maxParticipantsPerRow - 1) / maxParticipantsPerRow // Ceiling division
        val contentHeight = rows * figureHeight + 30 // Add more padding

        // Create content card with a lighter shade
        val contentBgColor = if (editorColor is EditorLite) "#F9FAFB" else "#374151" // Gray-50 for light, Gray-700 for dark

        text.append("""
            <rect x="30" y="$contentY" width="680" height="$contentHeight" rx="4" ry="4" 
                  fill="$contentBgColor" filter="url(#dropShadow)" />
        """.trimIndent())

        // Add person figures for each participant
        participantList.forEachIndexed { index, participant ->
            val row = index / maxParticipantsPerRow
            val col = index % maxParticipantsPerRow

            // Calculate position for this person figure with better spacing
            val xPos = 30f + 75f + (col * figureWidth) // Start with padding + half figure width
            val yPos = contentY + 40f + (row * figureHeight) // Start with padding

            text.append(createPersonFigure(xPos, yPos, participant.trim(), mapBgFromStatus(adr)))
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
class EditorLite(background: String = "#ffffff", lineColor: String="#6B7280", textColor: String="#374151", titleColor: String="#4B5563"): EditorColor(background, lineColor, textColor, titleColor)
class EditorDark(background: String = "#1E293B",  lineColor: String = "#818CF8",  textColor: String = "#E5E7EB", titleColor: String = "#A5B4FC"): EditorColor(background, lineColor, textColor, titleColor)
class RowTextOutcome(val text: String, val lastYPosition: Float)

fun EditorColor.backGrad(): SVGColor {
    return SVGColor(background, id)
}
