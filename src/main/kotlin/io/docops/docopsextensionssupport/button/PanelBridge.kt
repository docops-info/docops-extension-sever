package io.docops.docopsextensionssupport.button

import io.docops.asciidoc.buttons.dsl.*
import io.docops.asciidoc.buttons.dsl.Font
import io.docops.asciidoc.buttons.dsl.Link
import io.docops.asciidoc.buttons.service.PanelService
import net.sourceforge.plantuml.bpm.Line

class PanelBridge {


    fun buttonToPanelButton(buttons: Buttons): String {
        return when (buttons.buttonType) {
            ButtonType.REGULAR -> {
                handleRegular(buttons, io.docops.asciidoc.buttons.theme.ButtonType.BUTTON)
            }

            ButtonType.ROUND -> {
                handleRound(buttons)
            }
            ButtonType.PILL -> {
                handleRegular(buttons, io.docops.asciidoc.buttons.theme.ButtonType.PILL)
            }

            ButtonType.LARGE -> {
                handleLarge(buttons)
            }
            ButtonType.RECTANGLE -> {
                handleRectangle(buttons)
            }
            ButtonType.SLIM -> {
                handleSlim(buttons)
            }
        }
    }

    private fun handleSlim(buttons: Buttons): String {
        val panelButtons = mutableListOf<SlimButton>()
        buttons.buttons.forEach {
            val pb = SlimButton()
            it.author?.let { auth ->
                pb.authors = auth
            }
            pb.label = it.label
            pb.link = it.link
            it.description?.let { desc->
                pb.description = desc
            }
            it.date?.let { d->
                pb.date = d
            }


            it.type?.let { type ->
                pb.type = type
            }
            panelButtons.add(pb)

        }
        val panels = Panels()

        panels.buttonType = io.docops.asciidoc.buttons.theme.ButtonType.SLIM_CARD


        panels.slimButtons = panelButtons
        val cm = ColorMap()
        buttons.buttonDisplay?.let {
            cm.named(it.colors.toMutableList())
        }
        var themeFont: Font? = null

        panels.theme {
            buttons.buttonDisplay?.let {
                scale = it.scale
            }
            colorMap = cm
            font = themeFont
        }

        val ps = PanelService()
        return ps.fromPanelToSvg(panels)
    }

    private fun handleRectangle(buttons: Buttons): String {
        val panelButtons = mutableListOf<RectangleButton>()
        buttons.buttons.forEach {
            val pb = RectangleButton()
            it.author?.let { auth ->
                pb.authors = auth
            }
            pb.label = it.label
            pb.link = it.link
            it.description?.let { desc->
                pb.description = desc
            }
            it.date?.let { d->
                pb.date = d
            }



            it.type?.let { type ->
                pb.type = type
            }
            it.links?.let { links ->
                links.forEach { item ->
                   pb.links.add(Link(label=item.label, href = item.href))
                }
            }
            panelButtons.add(pb)

        }
        val panels = Panels()

        panels.buttonType = io.docops.asciidoc.buttons.theme.ButtonType.RECTANGLE


        panels.rectangleButtons = panelButtons
        val cm = ColorMap()
        buttons.buttonDisplay?.let {
            cm.named(it.colors.toMutableList())
        }
        var themeFont: Font? = null

        panels.theme {
            buttons.buttonDisplay?.let {
                scale = it.scale
            }
            colorMap = cm
            font = themeFont
        }

        val ps = PanelService()
        return ps.fromPanelToSvg(panels)
    }

    private fun handleLarge(buttons: Buttons): String {
        val panelButtons = mutableListOf<LargeButton>()
        buttons.buttons.forEach {
            val pb = LargeButton()
            it.author?.let { auth ->
                pb.authors = auth
            }
            pb.label = it.label
            pb.link = it.link
            it.description?.let { desc->
                pb.description = desc
            }
            it.date?.let { d->
                pb.date = d
            }


            it.cardLine1?.let { line->
                pb.line1 = Line(line = line.line, size = line.size)

            }
            it.cardLine2?.let {
                line ->
                pb.line2 = Line(line =  line.line, size = line.size)
            }
            it.type?.let { type ->
                pb.type = type
            }
            panelButtons.add(pb)

        }
        val panels = Panels()

        panels.buttonType = io.docops.asciidoc.buttons.theme.ButtonType.LARGE_CARD


        panels.largeButtons = panelButtons
        val cm = ColorMap()
        buttons.buttonDisplay?.let {
            cm.named(it.colors.toMutableList())
        }
        var themeFont: Font? = null

        panels.theme {
            buttons.buttonDisplay?.let {
                scale = it.scale
            }
            colorMap = cm
            font = themeFont
        }

        val ps = PanelService()
        return ps.fromPanelToSvg(panels)
    }


    private fun handleRegular(buttons: Buttons, btype: io.docops.asciidoc.buttons.theme.ButtonType) : String {
        val panelButtons = mutableListOf<PanelButton>()
        buttons.buttons.forEach {
            val pb = PanelButton()
            it.author?.let { auth ->
                pb.authors = auth
            }
            pb.label = it.label
            pb.link = it.link

            panelButtons.add(pb)

        }
        val panels = Panels()

        panels.buttonType = btype


        panels.panelButtons = panelButtons
        val cm = ColorMap()
        buttons.buttonDisplay?.let {
            cm.named(it.colors.toMutableList())
        }
        var themeFont: Font? = null

        panels.theme {
            buttons.buttonDisplay?.let {
                scale = it.scale
            }
            colorMap = cm
            font = themeFont
        }

        val ps = PanelService()
        return ps.fromPanelToSvg(panels)
    }
    private fun handleRound(buttons: Buttons): String {
        val panelButtons = mutableListOf<RoundButton>()
        buttons.buttons.forEach {
            val rb = RoundButton()
            it.author?.let { auth ->
                rb.authors = auth
            }
            rb.label = it.label
            rb.link = it.link

            panelButtons.add(rb)

        }
        val panels = Panels()

        panels.buttonType = io.docops.asciidoc.buttons.theme.ButtonType.ROUND


        panels.roundButtons = panelButtons
        val cm = ColorMap()
        buttons.buttonDisplay?.let {
            cm.named(it.colors.toMutableList())
        }
        var themeFont: Font? = null
        panels.theme {
            buttons.buttonDisplay?.let {
                scale = it.scale
            }
            colorMap = cm
            font = themeFont
        }

        val ps = PanelService()
        return ps.fromPanelToSvg(panels)
    }
}