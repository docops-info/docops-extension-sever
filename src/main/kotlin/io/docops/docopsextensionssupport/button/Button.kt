package io.docops.docopsextensionssupport.button

import io.docops.asciidoc.buttons.dsl.Case
import kotlinx.serialization.Serializable


@Serializable
class CardLines(val line: String="", val size: String="50px")
@Serializable
class Button(val label: String, val link: String, val description: String? = null, val date: String?= null, val type: String? = null, val author: MutableList<String>? = null, val font: Font?= null, val cardLine1: CardLines? = null,val cardLine2: CardLines? = null, val links: MutableList<Link>? = null)

@Serializable
enum class ButtonType {
    LARGE,
    PILL,
    RECTANGLE,
    REGULAR,
    ROUND,
    SLIM
}

@Serializable
class Font {
    var family = "Arial, Helvetica, sans-serif"
    var size = "11px"
    var color = "#FFFFFF"
    var spacing = "normal"
    var bold = false
    var italic = false
    var underline = false
    var vertical = false
    var case = Case.NONE
}
@Serializable
class ButtonDisplay (val colors: List<String>, val font: Font? = null, val scale: Float = 1.0f) {

}
@Serializable
class Buttons(val buttons: MutableList<Button>, val buttonType: ButtonType, val buttonDisplay: ButtonDisplay)

@Serializable
class Link(val label: String, val href: String)