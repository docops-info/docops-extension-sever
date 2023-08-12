package io.docops.docopsextensionssupport.button.shape

import io.docops.asciidoc.buttons.theme.DESTINY
import io.docops.asciidoc.buttons.theme.DIABLO
import io.docops.asciidoc.buttons.theme.DIVISION2
import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.button.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File

open class Regular(buttons: Buttons) : AbstractButtonShape(buttons) {

    override fun createShape(): String {
        val sb = StringBuilder()
        sb.append(start())
        sb.append(defs())
        sb.append(draw())
        sb.append(end())
        return sb.toString()
    }
    open fun draw() : String{
        var scale = 1.0f
        buttons.buttonDisplay?.let {
            scale = it.scale
        }
        val sb = StringBuilder("<g transform=\"scale($scale)\">")
        val rows = toRows()
        rows.forEachIndexed { index, buttons ->
            sb.append(drawButton(index, buttons))
        }
        sb.append("</g>")
        return sb.toString()
    }
    fun drawButton(index: Int, buttonList: MutableList<Button>): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.buttonDisplay?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }
        var startX = 10

        var startY = 10
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + (index * BUTTON_PADDING) + BUTTON_SPACING
        }
        buttonList.forEach { button: Button ->
            val color = button
            btns.append(
                """
        <g transform="translate($startX,$startY)" cursor="pointer">
            <a xlink:href="${button.link}" target="$win" style='text-decoration: none; font-family:Arial; fill: #fcfcfc;'>
            <rect x="0" y="0" fill="${button.color}" width="300" height="30" class="raise btn_${button.id}_cls" filter="url(#Bevel2)" rx="10" ry="10"/>
            <text class="category" visibility="hidden">${button.type?.escapeXml()}</text>
            <text class="author" visibility="hidden">${button.author?.firstOrNull()}</text>
            <text class="date" visibility="hidden">${button.date?.escapeXml()}</text>
            <text x="150" y="20" text-anchor="middle" class="glass" style="${button.buttonStyle?.labelStyle}">${button.label.escapeXml()}</text>
            </a>
        </g>
        """.trimIndent()
            )

            startX += BUTTON_WIDTH + BUTTON_PADDING

        }
        return btns.toString()
    }
    protected fun start() : String {
        val height= height()
        val width = width()
        return """<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="${buttons.id}">"""
    }

    protected fun end() = """</svg>"""
    protected fun defs() : String{
        var strokeColor: String = "gold"
        buttons.buttonDisplay?.let {
            strokeColor = it.strokeColor
        }
        return """
            <defs>
            ${filters()}
            ${gradient()}
            <style>
            ${glass()}
            ${raise(strokeColor = strokeColor)}
            ${gradientStyle()}
            </style>
            </defs>
        """.trimIndent()
    }

}

fun main() {
    val btns = buttonFactory(ButtonType.REGULAR)
    val btnShape: ButtonShape = Regular(btns)
    val str = btnShape.drawShape()
    val f = File("gen/reg.svg")
    f.writeBytes(str.toByteArray())
}
private fun buttonFactory(type: ButtonType): Buttons {

    val btnStr = """
    {
      "label": "Cookies & Cream",
      "link": "https://www.apple.com",
      "color": "#400000"
    }
    """.trimIndent()
    val btn = Json.decodeFromString<Button>(btnStr)
    val links = mutableListOf<Link>(Link(label = "Ben & Jerry's", href = "https://www.benjerry.com"), Link("Flavors", href = "https://www.benjerry.com/flavors"))
    val b = Buttons(buttons =  mutableListOf(
        Button(label = "Cookies & Cream", link="https://www.apple.com", type="Cookies", description = "Chocolate & Cheesecake Ice Creams with Chocolate Cookies & a Cheesecake Core", cardLine1 = CardLines("Cookies &", size="34px"), cardLine2 = CardLines("Cream", size="34px"), links = links),
        Button(label = "Mint Chocolate Chance", link="https://www.google.com", type = "Mint", description = "Mint Ice Cream Loaded with Fudge Brownies", cardLine1 = CardLines("Mint Chocolate", size="34px"), cardLine2 = CardLines("Chance", size = "34px")),
        Button(label = "New York Super Fudge Chunk", link="https://www.microsoft.com", type = "Fudge", description = "Chocolate Ice Cream with White & Dark Fudge Chunks, Pecans, Walnuts & Fudge-Covered Almonds", cardLine1 = CardLines("New York", size="34px"), cardLine2 = CardLines("Super Fudge", size="34px")),
        Button(label = "Cherry Garcia", link="https://www.amazon.com", description = "Cherry Ice Cream with Cherries & Fudge Flakes", author = mutableListOf("Steve"), type = "Fruit", cardLine1 = CardLines("Cherry", size="34px"), cardLine2 = CardLines("Garcia", size="34px")),
        Button(label = "Chunky Monkey", link="https://www.facebook.com", author = mutableListOf("Duffy", "Rose"), description = "Banana Ice Cream with Fudge Chunks & Walnuts", type = "FB", cardLine1 = CardLines("Chunky", size="34px"), cardLine2 = CardLines("Monkey", size="34px"))
    ),
        buttonType = type,
        buttonDisplay = ButtonDisplay(DARKREDLIGHT(), scale = 0.7f, columns = 2, buttonStyle = ButtonStyle(labelStyle = "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #fcfcfc; letter-spacing: normal;"))
    )
    return b
}