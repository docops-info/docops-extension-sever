package io.docops.docopsextensionssupport.vcard





import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class VCard @OptIn(ExperimentalUuidApi::class) constructor(
    val firstName: String,

    val lastName: String,

    val middleName: String? = null,
    val prefix: String? = null,
    val suffix: String? = null,
    val nickname: String? = null,

    // Changed to support multiple emails
    val emails: List<ContactEmail> = emptyList(),
    @Deprecated("Use emails list instead")
    val email: String? = emails.firstOrNull()?.address,

    // Changed to support multiple phones
    val phones: List<ContactPhone> = emptyList(),
    @Deprecated("Use phones list instead")
    val phone: String? = phones.find { it.type == PhoneType.WORK || it.type == PhoneType.VOICE }?.number,
    @Deprecated("Use phones list instead")
    val mobile: String? = phones.find { it.type == PhoneType.CELL }?.number,

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
    val uid: String = Uuid.random().toHexString(),
    val revision: Instant = Instant.now()
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

data class ContactEmail(
    val address: String,
    val types: List<String> = listOf("internet")
)

data class ContactPhone(
    val number: String,
    val type: PhoneType,
    val subtypes: List<String> = emptyList()
)

enum class PhoneType {
    CELL, WORK, HOME, VOICE, FAX, OTHER
}