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
 * Buttons can be customized with various properties including labels, links, colors, and styles.
 * They can be rendered in different shapes using the [ButtonType] enum and organized in collections
 * using the [Buttons] class.
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
 *
 * Example of creating a simple button:
 * ```kotlin
 * val simpleButton = Button(
 *     label = "Click Me",
 *     link = "https://example.com",
 *     description = "A simple button example"
 * )
 * ```
 *
 * Example of creating a button with custom styling:
 * ```kotlin
 * val styledButton = Button(
 *     label = "Styled Button",
 *     link = "https://example.com/styled",
 *     description = "A button with custom styling",
 *     color = "#3498db",
 *     buttonStyle = ButtonStyle(
 *         labelStyle = "font-family: Arial; font-size: 14px; fill: #ffffff;",
 *         descriptionStyle = "font-family: Arial; font-size: 12px; fill: #cccccc;"
 *     )
 * )
 * ```
 *
 * Example of creating a button with additional metadata:
 * ```kotlin
 * val metadataButton = Button(
 *     label = "Documentation",
 *     link = "https://docs.example.com",
 *     description = "Access project documentation",
 *     date = "05/15/2023",
 *     type = "Documentation",
 *     author = mutableListOf("John Doe", "Jane Smith"),
 *     links = mutableListOf(
 *         Link("API Reference", "https://docs.example.com/api"),
 *         Link("User Guide", "https://docs.example.com/guide")
 *     )
 * )
 * ```
 */
@Serializable
data class Button(
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
    var enabled: Boolean = true,
    var embeddedImage: EmbeddedImage? = null,
    var active: Boolean = false,  // Add this property for active/current state
    var activeName: String = ""
)

/**
 * Represents the style configuration for a button.
 *
 * The ButtonStyle class defines the visual appearance of text elements within a [Button].
 * It allows customization of font family, size, color, and other CSS properties for different
 * parts of the button such as label, description, date, type, and author information.
 *
 * This class is used by the [Button] class to style its text elements and by the [ButtonDisplay]
 * class to define default styles for collections of buttons. When a button doesn't specify its own
 * style, it inherits styles from the ButtonDisplay's buttonStyle property.
 *
 * @property labelStyle The style for the label text of the button.
 * @property descriptionStyle The style for the description text of the button.
 * @property dateStyle The style for the date text of the button.
 * @property typeStyle The style for the type text of the button.
 * @property authorStyle The style for the author text of the button.
 * @property linkStyle The style for link text elements of the button.
 * @property fontSize The base font size for the button text elements.
 *
 * Example of creating a ButtonStyle:
 * ```kotlin
 * val style = ButtonStyle(
 *     labelStyle = "font-family: Arial; font-size: 14px; fill: #ffffff;",
 *     descriptionStyle = "font-family: Arial; font-size: 12px; fill: #cccccc;",
 *     fontSize = 14
 * )
 * ```
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
 * Represents the sorting options for buttons in a collection.
 *
 * This enum class defines the available criteria for sorting buttons within a [Buttons] collection.
 * The sorting is applied by the [Buttons.sort] method based on the [ButtonDisplay.sortBy] configuration.
 *
 * Usage:
 * ```kotlin
 * // Create a sort configuration for buttons
 * val sortConfig = Sort(ButtonSortBy.TYPE, SortDirection.ASCENDING)
 * 
 * // Use in a ButtonDisplay
 * val display = ButtonDisplay(sortBy = sortConfig)
 * ```
 */
@Serializable
enum class ButtonSortBy {
    /**
     * Sort buttons by their type property.
     * 
     * When this option is selected, buttons will be grouped together by their type value.
     * This is useful for organizing buttons into logical categories.
     * 
     * If a button doesn't have a type specified, it will be treated as having an empty string type.
     */
    TYPE,

    /**
     * Sort buttons by their label property.
     * 
     * When this option is selected, buttons will be sorted alphabetically by their label text.
     * This is the default sorting option and provides a predictable, alphabetical ordering.
     */
    LABEL,

    /**
     * Sort buttons by their date property.
     * 
     * When this option is selected, buttons will be sorted chronologically by their date value.
     * This is useful for timelines or showing buttons in chronological order.
     * 
     * If a button doesn't have a date specified, it will be treated as having the date "01/01/1970".
     */
    DATE,

    /**
     * Sort buttons by their author property.
     * 
     * When this option is selected, buttons will be sorted alphabetically by the first author in their author list.
     * This is useful for organizing buttons by creator or owner.
     * 
     * If a button doesn't have any authors specified, it will be treated as having an empty string author.
     */
    AUTHOR,

    /**
     * Preserve the original insertion order of buttons.
     * 
     * When this option is selected, no sorting is applied and buttons remain in the order they were added to the collection.
     * This is useful when a specific manual ordering is desired.
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
 * Represents the display settings for a collection of buttons.
 *
 * The ButtonDisplay class serves as a theme or configuration container that defines
 * how a collection of [Button] objects should be rendered. It provides default styling,
 * layout options, color schemes, and behavior settings that apply to all buttons in a
 * [Buttons] collection.
 *
 * Key relationships:
 * - ButtonDisplay contains a [ButtonStyle] object that defines default text styling for buttons
 * - [Buttons] class uses ButtonDisplay as its theme property
 * - Individual [Button] objects inherit styles and colors from ButtonDisplay when they don't
 *   specify their own
 *
 * The color assignment works as follows:
 * 1. If a button specifies its own color, that color is used
 * 2. If a button has a type but no color, the color is taken from colorTypeMap for that type
 * 3. If the type isn't in colorTypeMap, a color is assigned from the colors list and added to colorTypeMap
 * 4. If a button has neither color nor type, the first color in the colors list is used
 *
 * @property colors The list of colors to use for buttons that don't specify their own color.
 * @property colorTypeMap A map associating button types with specific colors.
 * @property scale The scale factor of the buttons (1.0 = 100%).
 * @property columns The number of columns to use when displaying buttons in a grid.
 * @property newWin Indicates if button links should open in a new window when clicked.
 * @property useDark Indicates if dark theme should be used for the buttons.
 * @property strokeColor The color of the button stroke/border.
 * @property sortBy The sort configuration for ordering buttons in the collection.
 * @property buttonStyle The default style settings for button text elements.
 * @property hexLinesEnabled Whether to show connecting lines in hexagonal button layouts.
 * @property raise Whether to apply a raised/3D effect to buttons.
 *
 * Example of creating a ButtonDisplay:
 * ```kotlin
 * val display = ButtonDisplay(
 *     colors = listOf("#3498db", "#2ecc71", "#e74c3c", "#f39c12"),
 *     scale = 1.2f,
 *     columns = 4,
 *     newWin = true,
 *     buttonStyle = ButtonStyle(
 *         labelStyle = "font-family: Arial; font-size: 14px; fill: #ffffff;",
 *         descriptionStyle = "font-family: Arial; font-size: 12px; fill: #eeeeee;"
 *     )
 * )
 * ```
 */
@Serializable
class ButtonDisplay(
    val colors: List<String> = DARK1(),
    val colorTypeMap: MutableMap<String, String> = mutableMapOf(),
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
    val hexLinesEnabled: Boolean = false,
    val raise: Boolean = true,
    val activeColor: String = "#ff6b6b", // Default active color
    val useActiveColor: Boolean = true    // Enable/disable active color override

)

/**
 * Class representing a collection of buttons with shared configuration and rendering options.
 *
 * The Buttons class serves as the main container for a group of [Button] objects that should be
 * rendered together. It coordinates the styling, layout, and appearance of individual buttons
 * through its relationship with [ButtonDisplay] (theme) and [ButtonType] (shape).
 *
 * Key relationships:
 * - Buttons contains a list of [Button] objects that it manages
 * - Buttons uses a [ButtonDisplay] object as its theme to provide default styling and layout
 * - Buttons specifies a [ButtonType] that determines the shape and visual appearance of all buttons
 * - During initialization, Buttons applies theme settings to each Button, including colors and styles
 *
 * The initialization process:
 * 1. If themeUrl is provided, the theme is loaded from that URL
 * 2. Theme settings are applied to all buttons in the collection
 * 3. Colors and styles are determined for each button based on theme and button properties
 * 4. Buttons are sorted according to the theme's sortBy configuration
 *
 * @property buttons The list of buttons in this collection.
 * @property buttonType The type/shape of all buttons in this collection.
 * @property theme The display theme for the buttons, providing default styling and layout.
 * @property themeUrl The URL from which to load the theme (optional).
 * @property useDark Flag indicating if the dark theme should be used.
 * @property id The unique identifier for the button collection.
 * @property typeMap A map of button types and their corresponding colors (built during initialization).
 *
 * Example of creating a Buttons collection:
 * ```kotlin
 * val buttonCollection = Buttons(
 *     buttons = mutableListOf(
 *         Button(label = "Home", link = "https://example.com/home"),
 *         Button(label = "Products", link = "https://example.com/products"),
 *         Button(label = "Contact", link = "https://example.com/contact")
 *     ),
 *     buttonType = ButtonType.PILL,
 *     theme = ButtonDisplay(
 *         colors = listOf("#3498db", "#2ecc71", "#e74c3c"),
 *         columns = 3,
 *         newWin = true
 *     )
 * )
 *
 * // Generate SVG representation
 * val buttonShape = buttonCollection.createSVGShape()
 * val svg = buttonShape.drawShape("SVG")
 * ```
 */
@Serializable
class Buttons(
    val buttons: MutableList<Button>,
    val buttonType: ButtonType,
    var theme: ButtonDisplay? = null,
    var themeUrl: String? = null,
    var useDark: Boolean = false,
    var docname: String = "",
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
            if(it.colorTypeMap.isNotEmpty()) {
                typeMap.putAll(it.colorTypeMap)
            }
        }
        buttons.forEach { button ->
            // Set active state FIRST, before color determination
            if(docname.isNotBlank() && docname == button.activeName) {
                button.active = true
            }

            val color = determineButtonColor(button)
            button.color = color
            button.buttonStyle = determineStyle(button)

            // Generate gradient based on the final color (which could be active color)
            if (color.isNotEmpty()) {
                button.gradient = buildGradientHslDef(color, button.id)
            }
            button.buttonGradientStyle = """.btn_${button.id}_cls { fill: url(#btn_${button.id}); }"""
        }

        sort()
    }




    private fun determineButtonColor(button: Button): String {
        var color: String = ""

        // Check if button is active and should use active color
        if (button.active && theme?.useActiveColor == true) {
            return theme?.activeColor ?: "#ff6b6b"
        }

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
                Hex(this)
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
    /**
     * Creates a copy of this Buttons instance with a new docname
     */
    fun withDocname(newDocname: String): Buttons {
        return Buttons(
            buttons = this.buttons.map { it.copy() }.toMutableList(),
            buttonType = this.buttonType,
            theme = this.theme,
            themeUrl = this.themeUrl,
            useDark = this.useDark,
            docname = newDocname,
            id = this.id
        )
    }

    companion object {
        /**
         * Factory method to create Buttons from JSON with docname
         */
        fun fromJsonWithDocname(jsonString: String, docname: String): Buttons {
            val tempButtons = Json.decodeFromString<Buttons>(jsonString)
            return Buttons(
                buttons = tempButtons.buttons,
                buttonType = tempButtons.buttonType,
                theme = tempButtons.theme,
                themeUrl = tempButtons.themeUrl,
                useDark = tempButtons.useDark,
                docname = docname,
                id = tempButtons.id
            )
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

/**
 * Creates an SVG linear gradient definition from a color.
 *
 * This utility function creates an SVG linear gradient definition using the specified color
 * and button ID. It uses the [SVGColor] class to generate the gradient.
 *
 * @param color The base color for the gradient in hex format (e.g., "#3498db")
 * @param id The button ID to use in the gradient identifier
 * @return An SVG linear gradient definition as a string
 */
fun buildGradientDef(color: String, id: String): String {
    val m = SVGColor(color, "btn_${id}")
    return m.linearGradient
}

fun parseStyleForFontSize(style: String?, defaultSize: Int = 24): Int {
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
/**
 * Creates an SVG linear gradient definition from a color using HSL color space.
 *
 * This utility function creates an SVG linear gradient definition using the specified color
 * and button ID. It uses the [SVGColor] class to generate the gradient with HSL color transformations.
 *
 * @param color The base color for the gradient in hex format (e.g., "#3498db")
 * @param id The button ID to use in the gradient identifier
 * @return An SVG linear gradient definition as a string
 */
fun buildGradientHslDef(color: String, id: String): String {
    val m = SVGColor(color, "btn_${id}")
    return m.linearGradient
}

/**
 * Retrieves content from a URL as a string.
 *
 * This utility function makes an HTTP request to the specified URL and returns the response body as a string.
 * It's used to fetch remote resources like theme configurations or other button-related data.
 * The function includes timeout settings and error handling.
 *
 * @param url The URL to fetch content from
 * @return The content from the URL as a string, or an empty string if an error occurs
 */
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

/**
 * Creates a gradient color map from a base color.
 *
 * This utility function takes a base color and generates a map of three colors for creating gradients:
 * - color1: A lighter tint of the base color (50% tint)
 * - color2: A slightly lighter tint of the base color (25% tint)
 * - color3: The original base color
 *
 * @param color The base color in hex format (e.g., "#3498db")
 * @return A map containing three color values for gradient generation
 */
fun gradientFromColor(color: String): Map<String, String> {
    val decoded = Color.decode(color)
    val tinted1 = tint(decoded, 0.5)
    val tinted2 = tint(decoded, 0.25)
    return mapOf("color1" to tinted1, "color2" to tinted2, "color3" to color)
}

/**
 * Creates a darker shade of a color.
 *
 * This utility function takes a Color object and returns a darker shade (50% darker)
 * as a hex color string.
 *
 * @param color The base Color object
 * @return A hex color string representing a darker shade of the input color
 */
private fun shade(color: Color): String {
    val rs: Double = color.red * 0.50
    val gs = color.green * 0.50
    val bs = color.blue * 0.50
    return "#${rs.toInt().toString(16)}${gs.toInt().toString(16)}${bs.toInt().toString(16)}"
}

/**
 * Creates a lighter tint of a color.
 *
 * This utility function takes a Color object and a factor value to create a lighter tint
 * of the color. The factor determines how much lighter the tint will be (0.0 = no change,
 * 1.0 = white).
 *
 * @param color The base Color object
 * @param factor The tint factor (0.0 to 1.0)
 * @return A hex color string representing a lighter tint of the input color
 */
private fun tint(color: Color, factor: Double): String {
    val rs = color.red + (factor * (255 - color.red))
    val gs = color.green + (factor * (255 - color.green))
    val bs = color.blue + (factor * (255 - color.blue))
    return "#${rs.toInt().toString(16)}${gs.toInt().toString(16)}${bs.toInt().toString(16)}"
}

/**
 * Generates a random color.
 *
 * This utility function generates a random color value.
 * Note: This function currently doesn't return the generated color.
 *
 * @return Nothing (function should be modified to return the generated color)
 */
fun randomColor() {
    val color = (Math.random() * 16777215).toInt() or (0xFF shl 24)
}
