package io.docops.docopsextensionssupport.diagram

class SvgBuilder {
    private val elements = mutableListOf<String>()

    fun element(tag: String, attributes: Map<String, Any> = emptyMap(), content: String = ""): SvgBuilder {
        val attrs = attributes.entries.joinToString(" ") { "${it.key}=\"${it.value}\"" }
        val element = if (content.isEmpty()) {
            "<$tag $attrs/>"
        } else {
            "<$tag $attrs>$content</$tag>"
        }
        elements.add(element)
        return this
    }

    fun group(attributes: Map<String, Any> = emptyMap(), block: SvgBuilder.() -> Unit): SvgBuilder {
        val attrs = attributes.entries.joinToString(" ") { "${it.key}=\"${it.value}\"" }
        elements.add("<g $attrs>")
        block()
        elements.add("</g>")
        return this
    }

    fun build(): String = elements.joinToString("\n")
}