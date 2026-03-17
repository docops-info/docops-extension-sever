package io.docops.docopsextensionssupport.badge

object BadgeParser {

    fun parseConfig(configStr: String): BadgeConfig {
        val config = BadgeConfig()

        configStr.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach

            val parts = trimmed.split("=", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim().lowercase()
                val value = parts[1].trim()

                when (key) {
                    "type", "style" -> config.type = value
                    "theme" -> config.theme = value
                    "spacing" -> config.spacing = value.toIntOrNull() ?: 8
                    "fontfamily", "font" -> config.fontFamily = value
                    "direction", "layout" -> config.direction = value
                    "perrow" -> config.perRow = value.toIntOrNull() ?: 5
                }
            }
        }

        return config
    }

    fun createBadgesFromInput(data: String): MutableList<Badge> {
        val badges = mutableListOf<Badge>()
        processPipeDelimitedData(data, badges)
        return badges
    }

    private fun processPipeDelimitedData(data: String, badges: MutableList<Badge>) {
        data.lines().forEach { line ->
            if (line.isEmpty()) return@forEach

            val split = line.split("|")

            when {
                split.size < 2 -> {
                    throw BadgeFormatException("Badge Format invalid, expecting at least 2 pipe delimited values (label|message) [$line]")
                }
                else -> {
                    val label: String = split[0].trim()
                    val message: String = split[1].trim()
                    val url: String? = if (split.size > 2 && split[2].trim().isNotEmpty()) split[2].trim() else null
                    val labelColor: String = if (split.size > 3 && split[3].trim().isNotEmpty()) split[3].trim() else "#555555"
                    val messageColor: String = if (split.size > 4 && split[4].trim().isNotEmpty()) split[4].trim() else "#007ec6"
                    val logo: String? = if (split.size > 5 && split[5].trim().isNotEmpty()) split[5].trim() else null
                    val fontColor: String = if (split.size > 6 && split[6].trim().isNotEmpty()) split[6].trim() else "#ffffff"

                    val b = Badge(
                        label = label,
                        message = message,
                        url = url,
                        labelColor = labelColor,
                        messageColor = messageColor,
                        logo = logo,
                        fontColor = fontColor
                    )
                    badges.add(b)
                }
            }
        }
    }
}