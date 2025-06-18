package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class AdrHandler : DocOpsHandler{

    fun handleSVG(payload: String, scale: String, useDark: Boolean, backEnd: String): String {
        val generator = AdrSvgGenerator()
        val adr = AdrParser().parseContent(payload)
        val svg = generator.generate(adr, width = 700)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.scale, context.useDark, context.backend)
    }


}