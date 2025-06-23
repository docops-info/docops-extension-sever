package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class FeatureCardHandler: DocOpsHandler {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val featureCardMaker = FeatureCardMaker()
        val parsedCards = featureCardMaker.parseTabularInput(payload)
        return featureCardMaker.createFeatureCardsSvg(parsedCards)

    }
}