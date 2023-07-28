package io.docops.docopsextensionssupport.releasestrategy

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.roadmap.linesToUrlIfExist
import io.docops.docopsextensionssupport.roadmap.wrapText
import java.util.*

class ReleaseTimelineSummaryMaker : ReleaseTimelineMaker() {

     override fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean) : String{
        val width = determineWidth(releaseStrategy = releaseStrategy)
        val id = UUID.randomUUID().toString()
        val str = StringBuilder(head(width, id, title= releaseStrategy.title, releaseStrategy.scale))
        str.append(defs(isPdf, id,  releaseStrategy.scale, releaseStrategy))
        str.append(title(releaseStrategy.title, width))
        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(buildReleaseItem(release,index, isPdf, id, releaseStrategy))
            str.append(buildReleaseItemHidden(release,index, isPdf, id, releaseStrategy))
        }

        str.append("</g>")
        str.append(tail())
        return str.toString()
    }

    private fun head(width: Float, id: String, title: String, scale: Float) : String{
        val height = (270  + 215)* scale
        //language=svg
        return """
            <svg width="$width" height="$height" viewBox='0 0 $width $height' xmlns='http://www.w3.org/2000/svg' xmlns:xlink="http://www.w3.org/1999/xlink" role='img'
            aria-label='Docops: Release Strategy' id="ID$id">
            <desc>https://docops.io/extension</desc>
            <title>${title.escapeXml()}</title>
        """.trimIndent()
    }

    fun buildReleaseItem(release: Release, currentIndex: Int, isPdf: Boolean, id: String, releaseStrategy: ReleaseStrategy): String {
        var startX = 0
        if (currentIndex > 0) {
            startX = currentIndex * 425 -(20*currentIndex)
        }
        val lineText = StringBuilder()
        var lineStart = 25
        release.lines.forEachIndexed { index, s ->
            lineText.append(
                """
                <tspan x="$lineStart" dy="10" class="entry" font-size="10px" font-weight="normal"
                   font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" text-anchor="start">- ${s.escapeXml()}</tspan>
            """.trimIndent()
            )
            if (index <= 7) {
                lineStart += 10
            } else {
                lineStart -= 10
            }
        }
        val goals = release.goal.escapeXml()
        val lines = linesToUrlIfExist(wrapText(goals, 20F), mutableMapOf())
        val spans = linesToSpanText(lines,24, 150)
        val textY = 88 - (lines.size * 12)
        var positionX = startX
        if(currentIndex>0) {
            positionX += currentIndex * 5
        }
        var completed = ""
        if(release.completed) {
            completed = "<use xlink:href=\"#done\" x=\"405\" y=\"-100\"/>"
        }
        //language=svg
        return """
         <g transform="translate(${positionX+10},60)" class="${shadeColor(release)}">
             <text text-anchor="middle" x="250" y="-12" class="milestoneTL">${release.date}</text>
             <path d="m 0,0 h 400 v 200 h -400 l 0,0 l 100,-100 z" stroke="${fishTailColor(release, releaseStrategy)}" fill="url(#${shadeColor(release)}_rect)"/>
             <path d="m 400,0 v 200 l 100,-100 z" fill="url(#${shadeColor(release)}_rect)" stroke="${fishTailColor(release, releaseStrategy)}" />
            <text x="410" y="110" class="milestoneTL" font-size="36px" fill="${releaseStrategy.displayConfig.fontColor}">${release.type}</text>
            $completed
            <g transform="translate(100,0)" cursor="pointer" onclick="strategyShowItem('ID${id}_${currentIndex}')">
                <rect x="0" y="0" height="200" width="300" fill="url(#${shadeColor(release)}_rect)" class="raise"/>
                <text text-anchor="middle" x="150" y="$textY" class="milestoneTL lines" font-size="10px"
                      font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" font-weight="bold" fill="${releaseStrategy.displayConfig.fontColor}">
                   $spans
                </text>
            </g>
        </g>
        """.trimIndent()
    }
    fun buildReleaseItemHidden(release: Release, currentIndex: Int, isPdf: Boolean, id: String, releaseStrategy: ReleaseStrategy): String {
            var startX = 0
            if (currentIndex > 0) {
                startX = currentIndex * 425 -(20*currentIndex)
            }
            val lineText = StringBuilder()
            var lineStart = 25
            release.lines.forEachIndexed { index, s ->
                lineText.append(
                    """
                <tspan x="$lineStart" dy="10" class="entry" font-size="10px" font-weight="normal"
                   font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" text-anchor="start">- ${s.escapeXml()}</tspan>
            """.trimIndent()
                )
                if (index <= 7) {
                    lineStart += 10
                } else {
                    lineStart -= 10
                }
            }
            var x = 200
            var anchor = "text-anchor=\"middle\""
            if (isPdf) {
                x = 15
                anchor = ""
            }
        var positionX = startX
        if(currentIndex>0) {
            positionX += currentIndex * 5
        }
            //language=svg
            return """
         <g transform="translate(${positionX+10},275)" class="${shadeColor(release)}" visibility="hidden" id="ID${id}_${currentIndex}">
             <path d="m 0,0 h 400 v 200 h -400 l 0,0 l 100,-100 z" stroke="${fishTailColor(release, releaseStrategy)}" fill="#fcfcfc"/>
             <path d="m 400,0 v 200 l 100,-100 z" fill="url(#${shadeColor(release)}_rect)" stroke="${fishTailColor(release, releaseStrategy)}" />
            <text x="410" y="110" class="milestoneTL" font-size="36px" fill="#fcfcfc">${release.type}</text>
            <text $anchor x="$x" y="12" class="milestoneTL lines" font-size="10px" font-family='Arial, "Helvetica Neue", Helvetica, sans-serif' font-weight="bold">
                $lineText
            </text>
        </g>
        """.trimIndent()
    }

    private fun linesToSpanText(lines: MutableList<String>, dy: Int, x: Int): String {
        val text = StringBuilder()
        lines.forEach {
            text.append("""<tspan x="$x" dy="$dy" text-anchor="middle" font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" font-size="24" font-weight="normal">$it</tspan>""")
        }
        return text.toString()
    }

}