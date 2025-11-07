package io.docops.docopsextensionssupport.vcard.model.renderer

import io.docops.docopsextensionssupport.vcard.model.VCard
import io.docops.docopsextensionssupport.vcard.model.VCardConfig

/**
 * Strategy interface for rendering VCards into SVG.
 */
interface VCardRenderer {
    /** The design key this renderer supports (e.g., "business_card_design"). */
    val designKey: String

    /** Render the SVG for the provided vCard and configuration. */
    fun render(vcard: VCard, config: VCardConfig): String
}