package io.docops.docopsextensionssupport

import com.athaydes.rawhttp.reqinedit.ReqInEditParser
import com.athaydes.rawhttp.reqinedit.ReqInEditUnit
import io.docops.asciidoc.buttons.theme.DIVISION2
import io.docops.docopsextensionssupport.button.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException


class ButtonTests {

    /**
     * Creates buttons using the buttonFactory method.
     * Calls the buttonFactory method with different ButtonTypes
     * to create regular, pill, large, rectangle, round, and slim buttons.
     */
    @Test
    fun makeButtons() {
        buttonFactory(ButtonType.REGULAR)
        buttonFactory(ButtonType.PILL)
        buttonFactory(ButtonType.LARGE)
        buttonFactory(ButtonType.RECTANGLE)
        buttonFactory(ButtonType.ROUND)
        buttonFactory(ButtonType.SLIM)
    }

    /**
     * run this when server is running.
     */
    //@Test
    fun runHttpClientTests() {
        val parser = ReqInEditParser()

        try {

            val entries = parser.parse(File("gen/buttons.http"))
            val allSuccess = ReqInEditUnit().run(entries)
            if (allSuccess) {
                println("All tests passed")
            } else {
                println("There were test failures")
            }
        } catch (e: IOException) {
            // handle error
        }
    }
    private fun buttonFactory(type: ButtonType) {
        val fo =  Font()
        fo.size  = "18px"
        fo.color = "#000000"
        val fo2 =  Font()
        fo2.size  = "12px"
        fo2.color = "#000000"
        val links = mutableListOf<Link>(Link(label = "Ben & Jerry's", href = "https://www.benjerry.com"), Link("Flavors", href = "https://www.benjerry.com/flavors"))
        val b = Buttons(buttons =  mutableListOf(
            Button(label = "Cookies & Cream", link="https://www.apple.com", type="Cookies", description = "Chocolate & Cheesecake Ice Creams with Chocolate Cookies & a Cheesecake Core", cardLine1 = CardLines("Cookies &", size="34px"), cardLine2 = CardLines("Cream", size="34px"), links = links),
            Button(label = "Mint Chocolate Chance", link="https://www.google.com", type = "Mint", description = "Mint Ice Cream Loaded with Fudge Brownies", cardLine1 = CardLines("Mint Chocolate", size="34px"), cardLine2 = CardLines("Chance", size = "34px")),
            Button(label = "New York Super Fudge Chunk", link="https://www.microsoft.com", type = "Fudge", description = "Chocolate Ice Cream with White & Dark Fudge Chunks, Pecans, Walnuts & Fudge-Covered Almonds", font = fo, cardLine1 = CardLines("New York", size="34px"), cardLine2 = CardLines("Super Fudge", size="34px")),
            Button(label = "Cherry Garcia", link="https://www.amazon.com", description = "Cherry Ice Cream with Cherries & Fudge Flakes", author = mutableListOf("Steve"), type = "Fruit", cardLine1 = CardLines("Cherry", size="34px"), cardLine2 = CardLines("Garcia", size="34px")),
            Button(label = "Chunky Monkey", link="https://www.facebook.com", author = mutableListOf("Duffy", "Rose"), description = "Banana Ice Cream with Fudge Chunks & Walnuts", type = "FB", cardLine1 = CardLines("Chunky", size="34px"), cardLine2 = CardLines("Monkey", size="34px"))
        ),
            buttonType = type,
            buttonDisplay = ButtonDisplay(DIVISION2, scale = 0.7f, font = fo2)
        )
        val json = Json.encodeToString(b)
        println(json)

        val obj = Json.decodeFromString<Buttons>(json)

        val pb = PanelBridge()
        val f = File("gen/paneljson$type.svg")
        f.writeBytes(pb.buttonToPanelButton(obj).toByteArray())
    }
}