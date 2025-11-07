package io.docops.docopsextensionssupport.vcard.model

import java.time.LocalDateTime

data class VCard(
    val firstName: String,
    
    val lastName: String,
    
    val middleName: String? = null,
    val prefix: String? = null,
    val suffix: String? = null,
    val nickname: String? = null,
    
    val email: String? = null,
    
    val phone: String? = null,
    
    val mobile: String? = null,
    val fax: String? = null,
    val organization: String? = null,
    val title: String? = null,
    val department: String? = null,
    val website: String? = null,
    val address: Address? = null,
    val photoBase64: String? = null,
    val photoUrl: String? = null,
    val socialMedia: List<SocialMedia> = emptyList(),
    val note: String? = null,
    val categories: List<String> = emptyList(),
    val birthday: String? = null, // YYYY-MM-DD format
    val uid: String = java.util.UUID.randomUUID().toString(),
    val revision: LocalDateTime = LocalDateTime.now()
)

data class Address(
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val type: AddressType = AddressType.WORK
)

enum class AddressType {
    HOME, WORK, OTHER
}

data class SocialMedia(
    val platform: String,
    val url: String,
    val handle: String? = null
)

data class VCardConfig(
    val design: String = "business_card_design",
    val theme: String = "light"
)

data class ParsedVCard(
    val config: VCardConfig,
    val vcardContent: String
)