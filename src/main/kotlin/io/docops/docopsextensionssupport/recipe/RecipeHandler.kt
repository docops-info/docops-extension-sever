package io.docops.docopsextensionssupport.recipe

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update


class RecipeHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val generator = RecipeSvgGenerator(context.useDark)
        val recipe = RecipeParser().parse(payload)
        val svg = generator.createSvg(recipe, context.scale.toDouble(), false)
        csvResponse.update(recipe.toCSV())
        return svg
    }
}