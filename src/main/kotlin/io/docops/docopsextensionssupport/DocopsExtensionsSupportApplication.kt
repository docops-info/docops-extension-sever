package io.docops.docopsextensionssupport

import io.docops.asciidoc.buttons.dsl.Panels
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy


@SpringBootApplication()
@EnableAspectJAutoProxy
class DocopsExtensionsSupportApplication

fun main(args: Array<String>) {
    runApplication<DocopsExtensionsSupportApplication>(*args)
}