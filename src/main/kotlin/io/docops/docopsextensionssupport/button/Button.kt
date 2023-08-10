package io.docops.docopsextensionssupport.button

import io.docops.asciidoc.buttons.dsl.Case
import io.docops.asciidoc.buttons.theme.DIVISION2
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

@Serializable
class Button(val label: String, val link: String, val description: String? = null, val date: String?= null, val type: String? = null, val author: MutableList<String>? = null, val font: Font?= null)

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

fun main() {
    val b = Buttons(buttons =  mutableListOf(
        Button(label = "Apple", link="https://www.apple.com"),
        Button(label = "Google", link="https://www.google.com"),
        Button(label = "Microsoft", link="https://www.microsoft.com"),
        Button(label = "Amazon", link="https://www.amazon.com"),
        Button(label = "Facebook", link="https://www.facebook.com")
        ),
        buttonType = ButtonType.REGULAR,
        buttonDisplay = ButtonDisplay(DIVISION2, scale = 0.7f)
    )
    val json = Json.encodeToString(b)
    println(json)

    val obj = Json.decodeFromString<Buttons>(json)

    val pb = PanelBridge()
    val f = File("gen/paneljson.svg")
   f.writeBytes(pb.buttonToPanelButton(obj).toByteArray())
}