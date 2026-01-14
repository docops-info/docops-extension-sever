package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext

class DocOpsMermaid(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return """
    <div class="docops-media-card">
        <div class="mermaid svg-container" onclick="openModal(this);">
            $payload
        </div>
        <div class="docops-control-bar">
            <button class="docops-btn" onclick="openModal(this.closest('.docops-media-card').querySelector('.svg-container'))" title="View Large">VIEW</button>
            <button class="docops-btn" onclick="docopsCopy.svg(this)" title="Copy SVG Source">SVG</button>
            <button class="docops-btn" onclick="docopsCopy.png(this)" title="Copy as PNG">PNG</button>
        </div>
    </div>

        """.trimIndent()
    }
}