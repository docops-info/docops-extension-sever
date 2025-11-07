package io.docops.docopsextensionssupport.vcard.model

import io.docops.docopsextensionssupport.vcard.model.renderer.*

class VCardSvgGeneratorService {
    companion object {
        private const val DESIGNS_PATH = "designs"
    }

    private val renderers: Map<String, VCardRenderer> = listOf(
        BusinessCardRenderer(),
        BusinessCard2Renderer(),
        BusinessCardTemplateRenderer(),
        TechPatternCardRenderer(),
        CreativeAgencyCardRenderer(),
        ModernCardRenderer(includeQR = true)
    ).associateBy { it.designKey }

    fun generateSvg(vcard: VCard, config: VCardConfig): String {
        val renderer = renderers[config.design] ?: renderers["business_card_design"]!!
        return renderer.render(vcard, config)
    }


}