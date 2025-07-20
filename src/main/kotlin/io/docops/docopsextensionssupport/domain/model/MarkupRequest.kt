package io.docops.docopsextensionssupport.domain.model

import io.docops.docopsextensionssupport.web.CsvResponse

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

fun List<DomainElement>.toCsv(): CsvResponse {
    // Define headers for the CSV
    val headers = listOf("Type", "Title", "Color", "Parent")
    val rows = mutableListOf<List<String>>()

    // Process each element and its children
    for (element in this) {
        when (element) {
            is DomainElement.Separator -> {
                rows.add(listOf("Separator", "---", "", ""))
            }
            is DomainElement.Domain -> {
                rows.add(listOf("Domain", element.title, element.color, ""))

                // Process subdomains
                for (subdomain in element.subdomains) {
                    rows.add(listOf("Subdomain", subdomain.title, subdomain.color, element.title))

                    // Process items
                    for (item in subdomain.items) {
                        rows.add(listOf("Item", item.title, item.color, subdomain.title))
                    }
                }
            }
            else -> {
                // Handle any other potential DomainElement types
            }
        }
    }

    return CsvResponse(headers, rows)
}