package io.docops.docopsextensionssupport.button

import io.docops.asciidoc.buttons.theme.DIVISION2
import io.docops.docopsextensionssupport.button.shape.ButtonShape
import io.docops.docopsextensionssupport.button.shape.Regular
import kotlinx.serialization.Serializable
import java.awt.Color
import java.util.*


@Serializable
class CardLines(val line: String = "", val size: String = "50px")

@Serializable
class Button(
    var id: String = UUID.randomUUID().toString(),
    val label: String,
    val link: String,
    val description: String? = null,
    val date: String? = null,
    val type: String? = null,
    val author: MutableList<String>? = null,
    val cardLine1: CardLines? = null,
    val cardLine2: CardLines? = null,
    val links: MutableList<Link>? = null,
    var color: String? = null,
    var gradient: String? = null,
    var buttonGradientStyle: String? = null,
    var buttonStyle: ButtonStyle? = null
)

@Serializable
class ButtonStyle(
    val labelStyle: String? = null,
    val descriptionStyle: String? = null,
    val dateStyle: String? = null,
    val typeStyle: String? = null,
    val authorStyle: String? = null
)

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
class ButtonDisplay(
    val colors: List<String> = DIVISION2,
    val scale: Float = 1.0f,
    val columns: Int = 3,
    val newWin: Boolean = false,
    var strokeColor: String = "gold",
    val buttonStyle: ButtonStyle = ButtonStyle(labelStyle = "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #000000; letter-spacing: normal;")
)

@Serializable
class Buttons(
    val buttons: MutableList<Button>,
    val buttonType: ButtonType,
    val buttonDisplay: ButtonDisplay,
    val id: String = UUID.randomUUID().toString()
) {

    val typeMap = mutableMapOf<String, String>()

    init {
        buttons.forEach {
            val color = determineButtonColor(it)
            it.color = determineButtonColor(it)
            it.buttonStyle = determineStyle(it)
            if (color.isNotEmpty()) {
                it.gradient = buildGradientDef(color, it.id)
            }
            it.buttonGradientStyle = """.btn_${it.id}_cls { fill: url(#btn_${it.id}); }"""
        }
    }

    private fun determineButtonColor(button: Button): String {
        var color = ""

        if (null == button.color && null != button.type) {
            val col = typeMap[button.type]
            if (null == col) {
                color = buttonDisplay.colors[typeMap.size % buttonDisplay.colors.size]
                typeMap[button.type] = color
            } else {
                color = col
            }
        } else if (null == button.color && null == button.type) {
            color = buttonDisplay.colors[0]
        } else {
            color = button.color!!
        }
        return color
    }

    private fun determineStyle(button: Button): ButtonStyle {
        var labelStyle = button.buttonStyle?.labelStyle
        if (null == labelStyle) {
            labelStyle = buttonDisplay.buttonStyle.labelStyle
        }
        var descriptionStyle: String? = button.buttonStyle?.descriptionStyle
        var dateStyle: String? = button.buttonStyle?.dateStyle
        val typeStyle: String? = button.buttonStyle?.typeStyle
        val authorStyle: String? = button.buttonStyle?.authorStyle

        return ButtonStyle(
            labelStyle = labelStyle,
            descriptionStyle = descriptionStyle,
            dateStyle = dateStyle,
            typeStyle = typeStyle,
            authorStyle = authorStyle
        )
    }

    fun createSVGShape(): String {
        val creator: ButtonShape
        when (ButtonType.REGULAR) {
            buttonType -> {
                creator = Regular(this)
            }

            ButtonType.LARGE -> TODO()
            ButtonType.PILL -> TODO()
            ButtonType.RECTANGLE -> TODO()
            ButtonType.REGULAR -> TODO()
            ButtonType.ROUND -> TODO()
            ButtonType.SLIM -> TODO()
        }
        return creator.drawShape()
    }
}


@Serializable
class Link(val label: String, val href: String)

fun buildGradientDef(color: String, id: String): String {
    val m = gradientFromColor(color)
    return """
           <linearGradient id="btn_${id}" x2="1" y2="1">
            <stop class="stop1" offset="0%" stop-color="${m["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${m["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${m["color3"]}"/>
            </linearGradient> 
        """
}

fun gradientFromColor(color: String): Map<String, String> {
    val decoded = Color.decode(color)
    val tinted1 = tint(decoded, 0.5)
    val tinted2 = tint(decoded, 0.25)
    return mapOf("color1" to tinted1, "color2" to tinted2, "color3" to color)
}

private fun shade(color: Color): String {
    val rs: Double = color.red * 0.50
    val gs = color.green * 0.50
    val bs = color.blue * 0.50
    return "#${rs.toInt().toString(16)}${gs.toInt().toString(16)}${bs.toInt().toString(16)}"
}

private fun tint(color: Color, factor: Double): String {
    val rs = color.red + (factor * (255 - color.red))
    val gs = color.green + (factor * (255 - color.green))
    val bs = color.blue + (factor * (255 - color.blue))
    return "#${rs.toInt().toString(16)}${gs.toInt().toString(16)}${bs.toInt().toString(16)}"
}

fun randomColor() {
    val color = (Math.random() * 16777215).toInt() or (0xFF shl 24)
}