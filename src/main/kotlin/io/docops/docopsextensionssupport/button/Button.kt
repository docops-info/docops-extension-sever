package io.docops.docopsextensionssupport.button

import io.docops.docopsextensionssupport.button.shape.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.awt.Color
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*

@Serializable
class CardLines(val line: String = "", val size: String = "50px")

@Serializable
class EmbeddedImage(val ref: String, val type: String = "image/png")
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
    var buttonStyle: ButtonStyle? = null,
    var embeddedImage: EmbeddedImage? = null
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
    val colors: List<String> = DARK1(),
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
    var buttonDisplay: ButtonDisplay? = null,
    var buttonDisplayUrl: String? = null,
    val id: String = UUID.randomUUID().toString()
) {

    val typeMap = mutableMapOf<String, String>()

    init {
        buttonDisplayUrl?.let {
            val content = getResourceFromUrl(it)
            buttonDisplay = Json.decodeFromString<ButtonDisplay>(content)
        }
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
        var color: String = ""

        if (null == button.color && null != button.type) {
            val col = typeMap[button.type]
            if ( null == col ) {
                buttonDisplay?.let {
                    color = it.colors[typeMap.size % it.colors.size]
                    typeMap[button.type] = color
                }
            } else {
                color = col
            }
        } else if (null == button.color && null == button.type) {
            buttonDisplay?.let {
                color = it.colors[0]
            }
        } else {
            color = button.color!!
        }
        return color
    }

    private fun determineStyle(button: Button): ButtonStyle {
        var labelStyle = button.buttonStyle?.labelStyle
        if (null == labelStyle) {
            buttonDisplay?.let {
                labelStyle = it.buttonStyle.labelStyle
            }
        }
        var descriptionStyle: String? = button.buttonStyle?.descriptionStyle
        if(null == descriptionStyle) {
            buttonDisplay?.let {
                it.buttonStyle.descriptionStyle?.let { ds ->
                    descriptionStyle = ds
                }
            }
        }
        var dateStyle: String? = button.buttonStyle?.dateStyle
        if(null == dateStyle) {
            buttonDisplay?.let {
                it.buttonStyle.dateStyle?.let { dts ->
                    dateStyle = dts
                }
            }
        }
        var typeStyle: String? = button.buttonStyle?.typeStyle
        if(null == typeStyle) {
            buttonDisplay?.let {
                it.buttonStyle.typeStyle?.let { ts ->
                    typeStyle = ts
                }
            }
        }
        var authorStyle: String? = button.buttonStyle?.authorStyle
        if(null == authorStyle) {
            buttonDisplay?.let {
                it.buttonStyle.authorStyle?.let { ast ->
                    authorStyle = ast
                }
            }
        }

        return ButtonStyle(
            labelStyle = labelStyle,
            descriptionStyle = descriptionStyle,
            dateStyle = dateStyle,
            typeStyle = typeStyle,
            authorStyle = authorStyle
        )
    }

    fun createSVGShape(): String {
        val creator: ButtonShape = when (buttonType) {
            ButtonType.REGULAR -> {
                Regular(this)
            }
            ButtonType.PILL -> {
                Pill(this)
            }
            ButtonType.LARGE -> {
                Large(this)
            }
            ButtonType.RECTANGLE -> {
                Rectangle(this)
            }
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

fun getResourceFromUrl(url: String): String {
    val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(20))
        .build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(Duration.ofMinutes(1))
        .build()
    return try {
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        response.body()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
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