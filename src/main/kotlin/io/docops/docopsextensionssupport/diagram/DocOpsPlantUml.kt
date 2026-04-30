package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DocOpsPlantUml(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
    @OptIn(ExperimentalUuidApi::class)
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val id = Uuid.random().toHexString()

        return """
            <div class="docops-media-card">
                <div id="card_$id" class="plantuml svg-container">
                </div>
            </div>
            <script>
			window.plantuml.render(`${payload.replace("\\n", "")}`.split(/\r\n|\r|\n/), "card_$id");
            </script>
		"""
    }
}