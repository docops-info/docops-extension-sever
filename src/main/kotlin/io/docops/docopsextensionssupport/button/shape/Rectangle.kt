package io.docops.docopsextensionssupport.button.shape

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.button.EmbeddedImage
import io.docops.docopsextensionssupport.button.Link

/**
 * This class represents a rectangle shape that consists of buttons.
 * It extends the Regular class and inherits its properties and methods.
 */
class Rectangle(buttons: Buttons) : Regular(buttons) {

    /**
     * Draws the buttons using the specified theme scale and returns the generated SVG code as a string.
     *
     * @return The SVG code representing the buttons.
     */
    override fun draw() : String{
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val sb = StringBuilder("<g transform=\"scale($scale)\">")
        val rows = toRows()
        var count = 0
        rows.forEachIndexed { index, buttons ->

            sb.append(drawButtonInternal(index, buttons, count))
            count += buttons.size
        }
        sb.append("</g>")
        return sb.toString()
    }
    private fun drawButtonInternal(index: Int, buttonList: MutableList<Button>, count: Int): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }
        var startX = 10

        var startY = 10
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + (index * BUTTON_PADDING) + BUTTON_SPACING
        }
        var localCount = count

        buttonList.forEach { button: Button ->
            localCount++
            var imageOrLabel = """
            <rect x="10" y="10" height="98" width="98"
                                  class="mybox shape btn_${button.id}_cls" rx="18" ry="18" fill="#45618E"/>
            <g class="glass" transform="translate(10,10)">
           
            <text x="49" y="68" text-anchor="middle" alignment-baseline="central"
                  font-family="Helvetica, sans-serif" font-size="60px" filter="url(#Bevel2)">
                <a xlink:href="${button.link}" target="$win" fill="#000000">$localCount</a>
            </text>
            </g>
            """.trimIndent()
            button.embeddedImage?.let {
                imageOrLabel = makeEmbedImage( it)
            }
            btns.append(
                """
        <g transform="translate($startX,$startY)" cursor="pointer" filter="url(#Bevel2)">
            <rect x="0" y="0" width="310" filter="url(#Bevel2)" stroke="#b2b2b2" class="glass" height="120" rx="15"
              ry="15" fill="#f0f0f0" fill-opacity='0.3'/>
            <a xlink:href="${button.link}" class="linkText" target="$win">
            <text x="115" y="16" class="glass" style="${button.buttonStyle?.labelStyle}">${button.label.escapeXml()}</text>
            </a>
            <a xlink:href="${button.link}" class="linkText" target="$win"> 
            $imageOrLabel
            </a>
            ${linksToText(button.links)}
        </g>
        """.trimIndent()
            )

            startX += BUTTON_WIDTH + BUTTON_PADDING

        }
        return btns.toString()
    }

    private fun makeEmbedImage( buttonImage: EmbeddedImage): String {
        return """
            <image x="10" y="10" width="98" height="98" href="${buttonImage.ref}"/>""".trimIndent()

    }
    private fun linksToText(links: MutableList<Link>?): String {
        val sb = StringBuilder("""<text x="115" y="20">""")
        var linkText = "linkText"
        buttons.theme?.let {
            if(it.useDark) {
                linkText = "linkTextDark"
            }
        }
        links?.let {
            it.forEach { link ->
                sb.append("""
            <tspan x="115" dy="14">
                <a xlink:href="${link.href}" class="$linkText" target="_blank">${link.label.escapeXml()}</a>
            </tspan>
                """.trimIndent())
            }
        }
       sb.append("</text>")
        return sb.toString()
    }
    override fun height(): Float {
        val size = toRows().size
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        if (size > 1) {
            return (size * Slim.BUTTON_HEIGHT + (size * 10)) * scale + 10
        }
        val h = Slim.BUTTON_HEIGHT + 30
        return h * scale
    }

    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        return (columns * BUTTON_WIDTH + columns * BUTTON_PADDING + columns * BUTTON_PADDING) * scale
    }
    companion object {
        const val BUTTON_HEIGHT: Int = 120
        const val BUTTON_WIDTH = 310
        const val BUTTON_PADDING = 10
        const val  BUTTON_SPACING = 10
    }
}