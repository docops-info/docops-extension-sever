package io.docops.docopsextensionssupport.button

import io.docops.asciidoc.buttons.dsl.PanelButton
import io.docops.asciidoc.buttons.dsl.Panels
import io.docops.asciidoc.buttons.dsl.panels
import io.docops.asciidoc.buttons.service.PanelService

class PanelBridge {

    fun buttonToPanelButton(buttons: Buttons): String {
        if (buttons.buttonType == ButtonType.REGULAR) {
            val panelButtons = mutableListOf<PanelButton>()
            buttons.buttons.forEach {
               val  pb = PanelButton()
                it.author?.let { auth ->
                    pb.authors = auth
                }
                pb.label = it.label
                pb.link = it.link
                panelButtons.add(pb)

            }
            val panels = Panels()
            panels.buttonType = io.docops.asciidoc.buttons.theme.ButtonType.BUTTON
            panels.panelButtons = panelButtons
            panels.theme { scale = buttons.buttonDisplay.scale }
            val ps = PanelService()
            return ps.fromPanelToSvg(panels)
        }

        return ""
    }
}