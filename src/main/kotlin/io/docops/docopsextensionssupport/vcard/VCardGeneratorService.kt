package io.docops.docopsextensionssupport.vcard


import java.time.format.DateTimeFormatter
import java.time.ZoneOffset

class VCardGeneratorService {

    private val vCardFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")

    fun generateMinimalVCard(vcard: VCard): String {
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:2.1")  // Use 2.1 instead of 3.0 (shorter)

            // Only include essential fields
            val fullName = buildFullName(vcard)
            appendLine("FN:$fullName")
            //vcard.fullName?.let { appendLine("FN:$it") }

            // Simplified phone (no type labels)
            vcard.phone?.let { appendLine("TEL:$it") }

            // Simplified email
            vcard.email?.let { appendLine("EMAIL:$it") }

            appendLine("END:VCARD")
        }
    }
    fun generateVCard30(vCard: VCard): String {
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")

            // Full name (required)
            val fullName = buildFullName(vCard)
            appendLine("FN:$fullName")

            // Structured name (required)
            appendStructuredName(vCard)

            // Organization and title
            vCard.organization?.let { org ->
                if (vCard.department != null) {
                    appendLine("ORG:$org;${vCard.department}")
                } else {
                    appendLine("ORG:$org")
                }
            }

            vCard.title?.let { appendLine("TITLE:$it") }

            // Contact information
            vCard.phone?.let { appendLine("TEL;TYPE=VOICE:$it") }
            vCard.mobile?.let { appendLine("TEL;TYPE=CELL:$it") }
            vCard.fax?.let { appendLine("TEL;TYPE=FAX:$it") }
            vCard.email?.let { appendLine("EMAIL;TYPE=INTERNET:$it") }

            // Address
            vCard.address?.let { appendAddress(it) }

            // Website
            vCard.website?.let { appendLine("URL:$it") }

            // Photo
            vCard.photoBase64?.let {
                appendLine("PHOTO;ENCODING=BASE64;TYPE=JPEG:")
                appendWrappedBase64(it)
            } ?: vCard.photoUrl?.let {
                appendLine("PHOTO;VALUE=URI:$it")
            }

            // Social media as URLs
            vCard.socialMedia.forEach { social ->
                appendLine("URL:${social.url}")
            }

            // Additional fields
            vCard.nickname?.let { appendLine("NICKNAME:$it") }
            vCard.birthday?.let { appendLine("BDAY:$it") }
            vCard.note?.let { appendLine("NOTE:$it") }

            if (vCard.categories.isNotEmpty()) {
                appendLine("CATEGORIES:${vCard.categories.joinToString(",")}")
            }

            // Unique identifier
            appendLine("UID:${vCard.uid}")

            // Revision timestamp
            appendLine("REV:${vCardFormatter.format(vCard.revision.atZone(ZoneOffset.UTC))}")

            appendLine("END:VCARD")
        }
    }

    private fun StringBuilder.appendStructuredName(vCard: VCard) {
        val parts = listOf(
            vCard.lastName,
            vCard.firstName,
            vCard.middleName ?: "",
            vCard.prefix ?: "",
            vCard.suffix ?: ""
        )
        appendLine("N:${parts.joinToString(";")}")
    }

    private fun StringBuilder.appendAddress(address: Address) {
        val type = when (address.type) {
            AddressType.HOME -> "HOME"
            AddressType.WORK -> "WORK"
            AddressType.OTHER -> "OTHER"
        }

        val parts = listOf(
            "", // PO Box (not used)
            "", // Extended address (not used)
            address.street ?: "",
            address.city ?: "",
            address.state ?: "",
            address.postalCode ?: "",
            address.country ?: ""
        )

        appendLine("ADR;TYPE=$type:${parts.joinToString(";")}")
    }

    private fun StringBuilder.appendWrappedBase64(base64Data: String) {
        // vCard 3.0 spec recommends 75 character line limit
        val cleanData = base64Data.replace("\n", "").replace("\r", "")
        cleanData.chunked(75).forEach { chunk ->
            if (chunk == cleanData.chunked(75).first()) {
                appendLine(" $chunk")
            } else {
                appendLine(" $chunk")
            }
        }
    }

    private fun buildFullName(vCard: VCard): String {
        return buildString {
            vCard.prefix?.let { append("$it ") }
            append(vCard.firstName)
            vCard.middleName?.let { append(" $it") }
            append(" ${vCard.lastName}")
            vCard.suffix?.let { append(" $it") }
        }.trim()
    }
}