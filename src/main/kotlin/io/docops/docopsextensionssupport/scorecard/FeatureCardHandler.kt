package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class FeatureCardHandler(csvResponse: CsvResponse): BaseDocOpsHandler(csvResponse) {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val featureCardMaker = FeatureCardMaker()
        val parsedCards = featureCardMaker.parseTabularInput(payload)
        csvResponse.update(parsedCards.toCsv())
        return featureCardMaker.createFeatureCardsSvg(parsedCards)

    }
}