package io.docops.viz.vcard

import io.docops.docopsextensionssupport.vcard.VCard
import io.docops.docopsextensionssupport.vcard.VCardConfig
import io.docops.docopsextensionssupport.vcard.renderer.ModernCardRenderer
import io.docops.viz.vcard.renderer.NeoBrutalistCardRenderer
import io.docops.docopsextensionssupport.vcard.renderer.TechPatternCardRenderer
import io.docops.docopsextensionssupport.vcard.renderer.VCardRenderer

class VCardSvgGeneratorService(val useDark: Boolean = false) {
    companion object {
        private const val DESIGNS_PATH = "designs"
    }

    private val renderers: Map<String, VCardRenderer> = listOf(


        TechPatternCardRenderer(useDark),
        ModernCardRenderer(includeQR = true, useDark),
        NeoBrutalistCardRenderer(useDark)
    ).associateBy { it.designKey }

    fun generateSvg(vcard: VCard, config: VCardConfig): String {
        val renderer = renderers[config.design] ?: renderers["neo_brutalist"]!!
        return renderer.render(vcard, config)
    }


}
