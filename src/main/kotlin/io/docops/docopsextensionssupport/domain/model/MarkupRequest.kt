package io.docops.docopsextensionssupport.domain.model

data class MarkupRequest(
    val markup: String
)

data class SvgResponse(
    val svg: String,
    val width: Int,
    val height: Int
)

sealed class DomainElement(
    open val type: String
) {
    data class Domain(
        val title: String,
        val color: String,
        val subdomains: MutableList<Subdomain> = mutableListOf()
    ) : DomainElement("domain")

    data class Subdomain(
        val title: String,
        val color: String,
        val items: MutableList<Item> = mutableListOf()
    ) : DomainElement("subdomain")

    data class Item(
        val title: String,
        val color: String
    ) : DomainElement("item")

    object Separator : DomainElement("separator")
}