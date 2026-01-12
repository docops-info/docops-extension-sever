package io.docops.docopsextensionssupport.support

/**
 * A generic interface for any visual component (Buttons, Roadmap, etc.) 
 * to express its theme and version requirements.
 */
interface VisualDisplay {
    val useDark: Boolean
    val visualVersion: Int
}
