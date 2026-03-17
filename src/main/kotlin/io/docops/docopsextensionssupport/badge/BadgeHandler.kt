package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.json.Json


class BadgeHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    private val docOpsBadgeGenerator: DocOpsBadgeGenerator = DocOpsBadgeGenerator()
    fun handleSVG(payload: String, backend: String, useDark: Boolean) : String  {
        val data = payload
        // Check if payload contains --- separator
        val parts = data.split("---").map { it.trim() }

        val config = if (parts.size > 1) {
            // Parse configuration from first part
            BadgeParser.parseConfig(parts[0])
        } else {
            BadgeConfig()
        }

        // Get badge data (either from second part or entire payload if no ---)
        val badgeData = if (parts.size > 1) parts[1] else parts[0]

        val badges = BadgeParser.createBadgesFromInput(badgeData)
        val svg = BadgeFactory.generate(badges, config, useDark)

        csvResponse.update(badges.toCsv())
        return svg
    }


    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.backend, context.useDark)
    }

}
