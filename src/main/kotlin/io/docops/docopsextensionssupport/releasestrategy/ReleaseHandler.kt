package io.docops.docopsextensionssupport.releasestrategy

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json

class ReleaseHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
    val log = KotlinLogging.logger {}
    fun handleSVG(payload: String, useDark: Boolean, backend: String): String {
        val release = Json.decodeFromString<ReleaseStrategy>(payload)
        release.useDark = useDark
        val isPdf = backend == "pdf"
        var output = ""
        when (release.style) {
            "TL" -> {
                output = createTimelineSvg(release, isPdf)
            }

            "TLS" -> {
                output = createTimelineSummarySvg(release, isPdf)
            }

            "R" -> {
                output = createRoadMap(release, isPdf, "OFF")
            }

            "TLG" -> {
                output = createTimelineGrouped(release, isPdf)
            }
        }
        val csv = release.toCsv()
        csvResponse.update(csv)
        return output
    }

    fun createTimelineSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String =
        ReleaseTimelineMaker().make(releaseStrategy, isPdf)

    private fun createTimelineSummarySvg(release: ReleaseStrategy, pdf: Boolean = false): String =
        ReleaseTimelineSummaryMaker().make(release, isPdf = pdf)

    fun createTimelineGrouped(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String =
        ReleaseTimelineGroupedMaker().make(releaseStrategy, isPdf)

    fun createRoadMap(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false, animate: String = "ON"): String =
        ReleaseRoadMapMaker().make(releaseStrategy, isPdf, animate)

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.useDark, context.backend)
    }
}