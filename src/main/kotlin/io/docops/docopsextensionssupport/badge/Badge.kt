package io.docops.docopsextensionssupport.badge

import kotlinx.serialization.Serializable

@Serializable
class Badge(val  label:String, val message: Int, val  url: String)