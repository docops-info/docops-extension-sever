package io.docops.docopsextensionssupport.web

import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.stereotype.Component

@Component
@Endpoint(id = "docopsstats")
class DocOpsStatsEndpoint(private val docOpsRouter: DocOpsRouter) {

    @ReadOperation
    fun getEventCounts(): Map<String, Int> {
        return docOpsRouter.getEventCounts()
    }


    @ReadOperation
    fun getEventCountByType(@Selector eventType: String): Int? {
        return docOpsRouter.getEventCounts()[eventType]
    }
}