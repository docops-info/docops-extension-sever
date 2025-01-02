/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.button

import io.docops.docopsextensionssupport.button.shape.*
import io.docops.docopsextensionssupport.support.SVGColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.Color
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Represents a card line.
 *
 * @property line The content of the card line.
 * @property size The size of the card line.
 */
@Serializable
class CardLines(val line: String = "", val size: String = "50px")

/**
 * Represents an embedded image.
 *
 * @property ref The reference of the image.
 * @property type The type of the image. Default value is "image/png".
 */
@Serializable
class EmbeddedImage(val ref: String, val type: String = "image/png")

/**
 * Represents a button that can be used in an application's user interface.
 *
 * @property id A unique identifier for the button. If not specified, a random UUID will be generated.
 * @property label The text that appears on the button.
 * @property link The destination URL or action associated with the button.
 * @property description An optional description for the button.
 * @property date An optional date associated with the button.
 * @property type An optional type of the button.
 * @property author An optional list of authors associated with the button.
 * @property cardLine1 An optional CardLines object representing the first line of a card associated with the button.
 * @property cardLine2 An optional CardLines object representing the second line of a card associated with the button.
 * @property links An optional list of Link objects associated with the button.
 * @property color The color of the button.
 * @property gradient The gradient of the button.
 * @property buttonGradientStyle The gradient style of the button.
 * @property buttonStyle The style of the button.
 * @property embeddedImage The embedded image associated with the button.
 */
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

/**
 * Represents the style configuration for a button.
 *
 * @property labelStyle The style for the label text of the button.
 * @property descriptionStyle The style for the description text of the button.
 * @property dateStyle The style for the date text of the button.
 * @property typeStyle The style for the type text of the button.
 * @property authorStyle The style for the author text of the button.
 */
@Serializable
class ButtonStyle(
    val labelStyle: String? = null,
    val descriptionStyle: String? = null,
    val dateStyle: String? = null,
    val typeStyle: String? = null,
    val authorStyle: String? = null,
    val linkStyle: String? = null,
    val fontSize: Int = 12
)

/**
 * An enum class representing different types of buttons.
 * These types can be used to define the appearance and behavior of buttons in a user interface.
 */
@Serializable
enum class ButtonType {
    LARGE,
    PILL,
    RECTANGLE,
    REGULAR,
    ROUND,
    CIRCLE,
    SLIM,
    OVAL,
    HEX
}

/**
 * Represents the sorting options for a button.
 *
 * This enum class defines the available sorting options for a button.
 * The sorting options include type, label, date, author, and order.
 *
 * Usage:
 *
 * ```
 * val sortBy = ButtonSortBy.TYPE
 * ```
 */
@Serializable
enum class ButtonSortBy {
    /**
     * Represents a type.
     *
     * This class is used to represent a type in a software system.
     *
     * Usage:
     *
     * ```
     * val type = Type()
     **/
    TYPE, /**
     * Represents a label.
     *
     * Labels are used to display text in a user interface.
     */
    LABEL, /**
     * This class represents a date.
     *
     * @property day The day of the date.
     * @property month The month of the date.
     * @property year The year of the date.
     */
    DATE, /**
     *  This class represents the details of an author.
     *
     *  @property name The name of the author.
     *  @property email The email address of the author.
     *  @property bio A brief description of the author's background.
     */
    AUTHOR, /**
     * The `ORDER` class represents the insertion order of a button.
     *
     * This class provides means to sort buttons by their inseertion order,
     *
     * @constructor Creates a new instance of the `ORDER` class.
     *
     * @property items The list of items in the order.
     */
    ORDER
}

/**
 * Represents the sorting direction for a list or array.
 *
 * This enumeration class is used to define the sorting direction as either
 * ascending or descending.
 */
@Serializable
enum class SortDirection {
    ASCENDING, DESCENDING
}

/**
 * Represents a configuration for sorting.
 *
 * @property sort The field to sort by.
 * @property direction The direction of sorting.
 */
@Serializable
class Sort(val sort: ButtonSortBy = ButtonSortBy.LABEL, val direction: SortDirection = SortDirection.ASCENDING)

/**
 * Represents the display settings for a button.
 *
 * @property colors The list of colors to use for the button.
 * @property scale The scale factor of the button.
 * @property columns The number of columns to display the buttons.
 * @property newWin Indicates if the button should open in a new window when clicked.
 * @property useDark Indicates if dark theme should be used for the button.
 * @property strokeColor The color of the button stroke.
 * @property sortBy The sort order for the buttons.
 * @property buttonStyle The style settings for the button label.
 */
@Serializable
class ButtonDisplay(
    val colors: List<String> = DARK1(),
    val scale: Float = 1.0f,
    val columns: Int = 3,
    val newWin: Boolean = false,
    var useDark: Boolean = false,
    var strokeColor: String = "gold",
    var sortBy: Sort = Sort(ButtonSortBy.LABEL, SortDirection.ASCENDING),
    val buttonStyle: ButtonStyle = ButtonStyle(
        labelStyle = "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #000000; letter-spacing: normal;",
        fontSize = 12
    ),
    val raise: Boolean = true
)

/**
 * Class representing a collection of buttons.
 *
 * @property buttons The list of buttons.
 * @property buttonType The type of buttons in the collection.
 * @property theme The display theme for the buttons.
 * @property themeUrl The URL for the theme.
 * @property useDark Flag indicating if the dark theme should be used.
 * @property id The unique identifier for the button collection.
 * @property typeMap A map of button types and their corresponding colors.
 * @constructor Creates a Buttons instance.
 */
@Serializable
class Buttons(
    val buttons: MutableList<Button>,
    val buttonType: ButtonType,
    var theme: ButtonDisplay? = null,
    var themeUrl: String? = null,
    var useDark: Boolean = false,
    val id: String = UUID.randomUUID().toString()
) {

    val typeMap = mutableMapOf<String, String>()

    init {
        themeUrl?.let {
            val content = getResourceFromUrl(it)
            theme = Json.decodeFromString<ButtonDisplay>(content)
        }
        theme?.let {
            if(!it.useDark) {
                it.useDark = useDark
            }
        }
        buttons.forEach {
            val color = determineButtonColor(it)
            it.color = determineButtonColor(it)
            it.buttonStyle = determineStyle(it)
            if (color.isNotEmpty()) {
                it.gradient = buildGradientHslDef(color, it.id)
            }
            it.buttonGradientStyle = """.btn_${it.id}_cls { fill: url(#btn_${it.id}); }"""
        }

        sort()
    }

    private fun parseStyleForFontSize(style: String?, defaultSize: Int = 24): Int {
        var sz = defaultSize
        style?.let {
            val styles = it.split(";")
            styles.forEach {
                if(it.trim().startsWith("font-size")) {
                    val size = it.substringAfter("font-size:")
                    if(size.contains("px")) {
                        val num = size.substringBefore("px").trim().toInt()
                        sz =num
                    }
                }
            }
        }
        return sz
    }
    private fun determineButtonColor(button: Button): String {
        var color: String = ""

        if (null == button.color && null != button.type) {
            val col = typeMap[button.type]
            if (null == col) {
                theme?.let {
                    color = it.colors[typeMap.size % it.colors.size]
                    typeMap[button.type] = color
                }
            } else {
                color = col
            }
        } else if (null == button.color && null == button.type) {
            theme?.let {
                color = it.colors[0]
            }
        } else {
            color = button.color!!
        }
        return color
    }

    private fun determineStyle(button: Button): ButtonStyle {
        var fontSize = 12
        var labelStyle = button.buttonStyle?.labelStyle
        if (null == labelStyle) {
            theme?.let {
                labelStyle = it.buttonStyle.labelStyle
                fontSize = parseStyleForFontSize(labelStyle)
            }
        }
        var descriptionStyle: String? = button.buttonStyle?.descriptionStyle
        if (null == descriptionStyle) {
            theme?.let {
                it.buttonStyle.descriptionStyle?.let { ds ->
                    descriptionStyle = ds
                }
            }
        }
        var dateStyle: String? = button.buttonStyle?.dateStyle
        if (null == dateStyle) {
            theme?.let {
                it.buttonStyle.dateStyle?.let { dts ->
                    dateStyle = dts
                }
            }
        }
        var typeStyle: String? = button.buttonStyle?.typeStyle
        if (null == typeStyle) {
            theme?.let {
                it.buttonStyle.typeStyle?.let { ts ->
                    typeStyle = ts
                }
            }
        }
        var authorStyle: String? = button.buttonStyle?.authorStyle
        if (null == authorStyle) {
            theme?.let {
                it.buttonStyle.authorStyle?.let { ast ->
                    authorStyle = ast
                }
            }
        }
        var linkStyle: String? = button.buttonStyle?.linkStyle
        if(null == linkStyle) {
            theme?.let {
                it.buttonStyle.linkStyle?.let {
                    ls -> linkStyle = ls
                }
            }
        }

        return ButtonStyle(
            labelStyle = labelStyle,
            descriptionStyle = descriptionStyle,
            dateStyle = dateStyle,
            typeStyle = typeStyle,
            authorStyle = authorStyle,
            linkStyle = linkStyle,
            fontSize = fontSize
        )
    }

    fun createSVGShape() : ButtonShape {
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

            ButtonType.ROUND -> {
                Round(this)
            }
            ButtonType.CIRCLE -> {
                Circle(this)
            }

            ButtonType.SLIM -> {
                Slim(this)
            }
            ButtonType.OVAL -> {
                Oval(this)
            }
            ButtonType.HEX -> {
                HoneyComb(this)
            }
        }
        return creator
    }
    fun create(creator: ButtonShape, type: String = "SVG"): String {
        return creator.drawShape(type)
    }

    private fun dateFromStr(date: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        return LocalDate.parse(date, formatter)
    }
    private fun sort() {
        theme?.let {
            when (it.sortBy.sort) {
                ButtonSortBy.LABEL -> {
                    if (it.sortBy.direction == SortDirection.DESCENDING) {
                        buttons.sortByDescending { item ->
                            item.label
                        }
                    } else {
                        buttons.sortBy { item ->
                            item.label
                        }
                    }
                }

                ButtonSortBy.TYPE -> {

                    if (it.sortBy.direction == SortDirection.DESCENDING) {
                        buttons.sortByDescending { item -> if(!item.type.isNullOrEmpty()){item.type} else {""} }
                    } else {
                        buttons.sortBy { item -> if(!item.type.isNullOrEmpty()){item.type} else {""} }
                    }
                }
                ButtonSortBy.DATE -> {
                    if (it.sortBy.direction == SortDirection.DESCENDING) {
                        buttons.sortByDescending { item -> if(!item.date.isNullOrEmpty()){dateFromStr(item.date)} else {dateFromStr("01/01/1970")} }
                    } else {
                        buttons.sortBy { item -> if(!item.date.isNullOrEmpty()){dateFromStr(item.date)} else {dateFromStr("01/01/1970")}}
                    }
                }
                ButtonSortBy.AUTHOR -> {
                    if (it.sortBy.direction == SortDirection.DESCENDING) {
                        buttons.sortByDescending { item -> if(!item.author.isNullOrEmpty()){item.author[0]} else {""} }
                    } else {
                        buttons.sortBy { item -> if(!item.author.isNullOrEmpty()){item.author[0]} else {""}}
                    }
                }
                ButtonSortBy.ORDER -> {}
            }
        }
    }
}


/**
 * Represents a link with a label and a URL.
 *
 * @property label The label of the link.
 * @property href The URL of the link.
 */
@Serializable
class Link(val label: String, val href: String)

fun buildGradientDef(color: String, id: String): String {
    val m = SVGColor(color, "btn_${id}")
    return m.linearGradient
}
fun buildGradientHslDef(color: String, id: String): String {
    val m = SVGColor(color, "btn_${id}")
   return m.linearGradient
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