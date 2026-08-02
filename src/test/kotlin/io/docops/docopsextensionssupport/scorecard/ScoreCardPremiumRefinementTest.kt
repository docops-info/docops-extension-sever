package io.docops.docopsextensionssupport.scorecard

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

class ScoreCardPremiumRefinementTest {

    @Test
    fun `given premium theme when scorecard generated then it has premium features`() {
        val scorecard = ScoreCard(
            title = "Modernization Strategy",
            beforeTitle = "Legacy State",
            afterTitle = "Cloud Native",
            theme = "premium",
            beforeSections = mutableListOf(
                BeforeSection().apply {
                    title = "Infrastructure"
                    items = mutableListOf(
                        ScoreCardItem("Monolithic architecture", "Hard to scale and maintain"),
                        ScoreCardItem("On-premise servers", "High CapEx and maintenance")
                    )
                }
            ),
            afterSections = mutableListOf(
                AfterSection().apply {
                    title = "Modern Stack"
                    items = mutableListOf(
                        ScoreCardItem("Microservices", "Independently deployable units"),
                        ScoreCardItem("Serverless computing", "Pay-as-you-go efficiency")
                    )
                }
            )
        )

        val maker = ScoreCardMaker(useDark = false)
        val svg = maker.make(scorecard)
        File("gen/scorecard_premium.svg").writeText(svg)

        assertTrue(svg.contains("font-family: 'Inter'"), "Premium theme should use Inter font")
        assertTrue(svg.contains("rx=\"8\""), "Premium theme cards should use 8px corner radius")
        assertTrue(svg.contains("stdDeviation=\"12\""), "Premium theme should have high blur shadow")
        assertTrue(svg.contains("opacity=\"0.05\""), "Premium theme should have cleaner background grid")
        assertTrue(svg.contains("id=\"premiumShadow_"), "Premium theme should define premiumShadow")
        assertTrue(svg.contains("linearGradient id=\"cardGrad_"), "Premium theme should have card gradients")
        assertTrue(svg.contains("#3B82F6"), "Premium theme should use primary accent color")
        assertTrue(svg.contains("path d=\"M0,0 L24,0 L16,-8 M24,0 L16,8\""), "Premium arrow should be 24 units long")
    }

    @Test
    fun `given premium dark theme when scorecard generated then it uses correct colors`() {
        val scorecard = ScoreCard(
            title = "Dark Premium Scorecard",
            beforeTitle = "Before",
            afterTitle = "After",
            theme = "premium",
            beforeSections = mutableListOf(BeforeSection().apply { title = "S1"; items = mutableListOf(ScoreCardItem("I1")) }),
            afterSections = mutableListOf(AfterSection().apply { title = "S2"; items = mutableListOf(ScoreCardItem("I2")) })
        )

        val maker = ScoreCardMaker(useDark = true)
        val svg = maker.make(scorecard)
        File("gen/scorecard_premium_dark.svg").writeText(svg)

        assertTrue(svg.contains("flood-color=\"#000000\""), "Dark mode shadow should be black")
        assertTrue(svg.contains("stop-color=\"#1e293b\""), "Premium dark theme should use dark gradient stop")
        assertTrue(svg.contains("fill: #F9FAFB") || svg.contains("fill: #f9fafb"), "Title should be light in dark mode")
    }

    @Test
    fun `given classic theme when scorecard generated then it remains brutalist`() {
        val scorecard = ScoreCard(
            title = "Brutalist Scorecard",
            beforeTitle = "Old",
            afterTitle = "New",
            theme = "classic",
            beforeSections = mutableListOf(BeforeSection().apply { 
                title = "Section 1"
                items = mutableListOf(ScoreCardItem("Item 1"))
            }),
            afterSections = mutableListOf(AfterSection().apply { 
                title = "Section 2"
                items = mutableListOf(ScoreCardItem("Item 2"))
            })
        )

        val maker = ScoreCardMaker(useDark = false)
        val svg = maker.make(scorecard)
        File("gen/scorecard_classic.svg").writeText(svg)

        assertTrue(svg.contains("rx=\"4\""), "Classic theme should keep 4px radius")
        assertTrue(svg.contains("url(#grid_"), "Classic theme should keep grid background")
        assertTrue(svg.contains("path d=\"M0,0 L36,0 L28,-8 M36,0 L28,8\""), "Classic arrow should be 36 units long")
    }
}
