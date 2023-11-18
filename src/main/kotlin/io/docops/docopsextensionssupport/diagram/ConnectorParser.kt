package io.docops.docopsextensionssupport.diagram

class ConnectorParser {

    fun parse(contents: String): MutableList<Connector> {
        val connectors = mutableListOf<Connector>()
        contents.lines().forEach {
            
            it.takeIf { it.isNotBlank() }?.let {line->
                connectors.add(Connector(line.trimStart()))
            }
        }
        return connectors
    }
}