package io.docops.docopsextensionssupport.vcard.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class VCardParserService {

    companion object {
        private const val CONFIG_SEPARATOR = "---"
        private val VALID_DESIGNS = setOf(
            "tech_pattern_background",
            "neo_brutalist",
            "modern_card"
        )
        private val VALID_THEMES = setOf("light", "dark")
    }

    fun parseVCardInput(input: String): ParsedVCard {
        val config: VCardConfig
        val vcardContent: String

        if (input.contains(CONFIG_SEPARATOR)) {
            val parts = input.split(CONFIG_SEPARATOR, limit = 2)
            val configSection = parts[0].trim()
            vcardContent = parts[1].trim()
            config = parseConfigSection(configSection)
        } else {
            // No config section, use defaults
            config = VCardConfig()
            vcardContent = input.trim()
        }

        return ParsedVCard(config, vcardContent)
    }

    private fun parseConfigSection(configSection: String): VCardConfig {
        var design = "business_card_design"
        var theme = "light"

        configSection.lines().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                return@forEach // Skip empty lines and comments
            }

            if (trimmedLine.contains("=")) {
                val (key, value) = trimmedLine.split("=", limit = 2)
                    .map { it.trim() }

                when (key.lowercase()) {
                    "design" -> design = validateDesign(value)
                    "theme" -> theme = validateTheme(value)
                    else -> {
                        // Log unknown config option
                        println("Unknown config option: $key")
                    }
                }
            }
        }

        return VCardConfig(design, theme)
    }

    private fun validateDesign(design: String): String {
        return if (VALID_DESIGNS.contains(design)) design else "business_card_design"
    }

    private fun validateTheme(theme: String): String {
        return if (VALID_THEMES.contains(theme.lowercase())) theme.lowercase() else "light"
    }

    fun parseVCard(vCardContent: String): VCard {
        val lines = vCardContent.lines()
            .filter { it.isNotBlank() }
            .map { it.trim() }

        if (!lines.any { it.startsWith("BEGIN:VCARD") } || !lines.any { it.startsWith("END:VCARD") }) {
            throw IllegalArgumentException("Invalid vCard format: Missing BEGIN:VCARD or END:VCARD")
        }

        val properties = mutableMapOf<String, MutableList<String>>()
        var currentProperty: String? = null
        var currentValue = StringBuilder()

        // Parse properties, handling line folding
        for (line in lines) {
            when {
                line.startsWith("BEGIN:VCARD") || line.startsWith("END:VCARD") -> continue
                line.startsWith(" ") || line.startsWith("\t") -> {
                    // Line folding - continuation of previous line
                    currentValue.append(line.substring(1))
                }
                else -> {
                    // New property
                    if (currentProperty != null) {
                        properties.getOrPut(currentProperty) { mutableListOf() }.add(currentValue.toString())
                    }

                    val colonIndex = line.indexOf(':')
                    if (colonIndex > 0) {
                        currentProperty = line.substring(0, colonIndex).uppercase()
                        currentValue = StringBuilder(line.substring(colonIndex + 1))
                    }
                }
            }
        }

        // Add the last property
        if (currentProperty != null) {
            properties.getOrPut(currentProperty) { mutableListOf() }.add(currentValue.toString())
        }

        return buildVCard(properties)
    }

    private fun buildVCard(properties: Map<String, List<String>>): VCard {
        // Parse structured name (N property)
        val structuredName = properties["N"]?.firstOrNull()?.split(";") ?: emptyList()
        val lastName = structuredName.getOrNull(0) ?: ""
        val firstName = structuredName.getOrNull(1) ?: ""
        val middleName = structuredName.getOrNull(2)?.takeIf { it.isNotBlank() }
        val prefix = structuredName.getOrNull(3)?.takeIf { it.isNotBlank() }
        val suffix = structuredName.getOrNull(4)?.takeIf { it.isNotBlank() }

        // Parse organization
        val orgData = properties["ORG"]?.firstOrNull()?.split(";")
        val organization = orgData?.getOrNull(0)?.takeIf { it.isNotBlank() }
        val department = orgData?.getOrNull(1)?.takeIf { it.isNotBlank() }

        // Parse phone numbers
        val phones = properties.filter { it.key.startsWith("TEL") }
        val phone = phones.entries.find {
            it.key.contains("VOICE") || it.key.contains("WORK") || (!it.key.contains("CELL") && !it.key.contains("FAX"))
        }?.value?.firstOrNull()
        val phoneList = phones.flatMap { (key, values) ->
            values.map { number ->
                val types = extractTypes(key)
                val phoneType = when {
                    types.contains("CELL") -> PhoneType.CELL
                    types.contains("WORK") -> PhoneType.WORK
                    types.contains("HOME") -> PhoneType.HOME
                    types.contains("FAX") -> PhoneType.FAX
                    types.contains("VOICE") -> PhoneType.VOICE
                    else -> PhoneType.OTHER
                }
                ContactPhone(number, phoneType, types)
            }
        }
        val mobile = phones.entries.find { it.key.contains("CELL") }?.value?.firstOrNull()
        val fax = phones.entries.find { it.key.contains("FAX") }?.value?.firstOrNull()

        // Parse addresses
        val addressEntries = properties.filter { it.key.startsWith("ADR") }
        val address = addressEntries.entries.firstOrNull()?.let { entry ->
            val parts = entry.value.first().split(";")
            val addressType = when {
                entry.key.contains("HOME") -> AddressType.HOME
                entry.key.contains("WORK") -> AddressType.WORK
                else -> AddressType.OTHER
            }

            Address(
                street = parts.getOrNull(2)?.takeIf { it.isNotBlank() },
                city = parts.getOrNull(3)?.takeIf { it.isNotBlank() },
                state = parts.getOrNull(4)?.takeIf { it.isNotBlank() },
                postalCode = parts.getOrNull(5)?.takeIf { it.isNotBlank() },
                country = parts.getOrNull(6)?.takeIf { it.isNotBlank() },
                type = addressType
            )
        }

        // Parse photo
        val photoEntries = properties.filter { it.key.startsWith("PHOTO") }
        val photoBase64 = photoEntries.entries.find { it.key.contains("BASE64") || it.key.contains("ENCODING=BASE64") }?.value?.firstOrNull()
        val photoUrl = photoEntries.entries.find { it.key.contains("VALUE=URI") || it.key.contains("URI") }?.value?.firstOrNull()

        // Parse social media from URLs
        val urls = properties["URL"] ?: emptyList()
        val socialMedia = urls.mapNotNull { url ->
            when {
                url.contains("twitter.com") || url.contains("x.com") -> {
                    val handle = url.substringAfterLast("/")
                    SocialMedia("Twitter", url, "@$handle")
                }
                url.contains("linkedin.com") -> {
                    val handle = url.substringAfterLast("/")
                    SocialMedia("LinkedIn", url, handle)
                }
                url.contains("github.com") -> {
                    val handle = url.substringAfterLast("/")
                    SocialMedia("GitHub", url, handle)
                }
                url.contains("instagram.com") -> {
                    val handle = url.substringAfterLast("/")
                    SocialMedia("Instagram", url, "@$handle")
                }
                else -> null
            }
        }

        // Parse ALL emails
        val emailEntries = properties.filter { it.key.startsWith("EMAIL") }
        val emailList = emailEntries.flatMap { (key, values) ->
            values.map { address ->
                val types = extractTypes(key)
                ContactEmail(address, types)
            }
        }

        // Parse categories
        val categories = properties["CATEGORIES"]?.firstOrNull()?.split(",")?.map { it.trim() } ?: emptyList()

        // Extract non-social media website
        val website = urls.find { url ->
            !socialMedia.any { social -> social.url == url }
        }

        return VCard(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            prefix = prefix,
            suffix = suffix,
            nickname = properties["NICKNAME"]?.firstOrNull(),
            email = properties["EMAIL"]?.firstOrNull(),
            phone = phone,
            mobile = mobile,
            fax = fax,
            organization = organization,
            title = properties["TITLE"]?.firstOrNull(),
            department = department,
            website = website,
            address = address,
            photoBase64 = photoBase64,
            photoUrl = photoUrl,
            socialMedia = socialMedia,
            note = properties["NOTE"]?.firstOrNull(),
            categories = categories,
            birthday = properties["BDAY"]?.firstOrNull(),
            uid = properties["UID"]?.firstOrNull() ?: UUID.randomUUID().toString(),
            revision = parseRevision(properties["REV"]?.firstOrNull()),
            emails = emailList,
            phones = phoneList,
        )
    }

    private fun extractTypes(propertyKey: String): List<String> {
        val typeMatch = Regex("TYPE=([^:;]+)").find(propertyKey)
        return typeMatch?.groupValues?.get(1)?.split(",")?.map { it.trim().uppercase() } ?: emptyList()
    }

    private fun parseRevision(revString: String?): LocalDateTime {
        return revString?.let { rev ->
            try {
                when {
                    rev.contains('T') && rev.endsWith('Z') -> {
                        // ISO format: 20231106T143000Z
                        LocalDateTime.parse(rev.removeSuffix("Z"), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"))
                    }
                    rev.length == 8 -> {
                        // Date only: 20231106
                        LocalDateTime.parse("${rev}T000000", DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"))
                    }
                    else -> LocalDateTime.now()
                }
            } catch (e: Exception) {
                LocalDateTime.now()
            }
        } ?: LocalDateTime.now()
    }
}
