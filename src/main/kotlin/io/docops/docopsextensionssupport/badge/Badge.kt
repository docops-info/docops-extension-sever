package io.docops.docopsextensionssupport.badge

import kotlinx.serialization.Serializable

@Serializable
class Badge(val  label:String, val message: Int, val  url: String)
class FormBadge(val label:String, val message: String, val  url: String, val labelColor: String?, val messageColor: String?, val logo: String?)