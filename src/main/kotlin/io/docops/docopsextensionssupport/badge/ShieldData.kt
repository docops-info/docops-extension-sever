package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.web.CsvResponse

/**
 * Data class representing an iOS-style shield/badge configuration
 */
data class ShieldData(
    val label: String,
    val message: String = "",
    val link: String? = null,
    val leftColor: String = "#6A1B9A", // Default iOS purple
    val rightColor: String = "#9C27B0", // Lighter iOS purple
    val icon: String? = null,
    val iconColor: String = "#ffffff",
    val style: ShieldStyle = ShieldStyle.IOS,
    val labelColor: String = "#ffffff",
    val messageColor: String = "rgba(255,255,255,0.85)",
    val width: Int? = null,
    val height: Int = 56, // iOS standard height
    val status: ShieldStatus? = null
)

/**
 * Enum representing different shield styles
 */
enum class ShieldStyle(val value: String) {
    IOS("ios"),
    FLAT("flat"),
    FLAT_SQUARE("flat-square"),
    PLASTIC("plastic"),
    FOR_THE_BADGE("for-the-badge"),
    SOCIAL("social")
}

/**
 * Enum for common shield statuses with predefined colors
 */
enum class ShieldStatus(val leftColor: String, val rightColor: String, val icon: String?) {
    SUCCESS("#34C759", "#30A14E", "check"),
    FAILED("#FF3B30", "#D70015", "x"),
    RUNNING("#FF9500", "#FF6B00", "spinner"),
    PENDING("#8E8E93", "#6D6D70", "clock"),
    CANCELLED("#8E8E93", "#6D6D70", "stop"),
    WARNING("#FF9500", "#FF6B00", null),
    INFO("#007AFF", "#0056CC", null)
}

/**
 * Data class for parsing tabular shield data with iOS enhancements
 */
data class ShieldTableRow(
    val columns: List<String>
) {
    fun toShieldData(): ShieldData {
        val baseShield = when (columns.size) {
            2 -> ShieldData(
                label = columns[0].trim(),
                message = columns[1].trim()
            )
            3 -> ShieldData(
                label = columns[0].trim(),
                message = columns[1].trim(),
                link = columns[2].trim().takeIf { it.isNotBlank() }
            )
            4 -> ShieldData(
                label = columns[0].trim(),
                message = columns[1].trim(),
                link = columns[2].trim().takeIf { it.isNotBlank() },
                leftColor = columns[3].trim().takeIf { it.isNotBlank() } ?: "#6A1B9A"
            )
            5 -> ShieldData(
                label = columns[0].trim(),
                message = columns[1].trim(),
                link = columns[2].trim().takeIf { it.isNotBlank() },
                leftColor = columns[3].trim().takeIf { it.isNotBlank() } ?: "#6A1B9A",
                rightColor = columns[4].trim().takeIf { it.isNotBlank() } ?: "#9C27B0"
            )
            6 -> ShieldData(
                label = columns[0].trim(),
                message = columns[1].trim(),
                link = columns[2].trim().takeIf { it.isNotBlank() },
                leftColor = columns[3].trim().takeIf { it.isNotBlank() } ?: "#6A1B9A",
                rightColor = columns[4].trim().takeIf { it.isNotBlank() } ?: "#9C27B0",
                icon = parseIcon(columns[5].trim())
            )
            7 -> ShieldData(
                label = columns[0].trim(),
                message = columns[1].trim(),
                link = columns[2].trim().takeIf { it.isNotBlank() },
                leftColor = columns[3].trim().takeIf { it.isNotBlank() } ?: "#6A1B9A",
                rightColor = columns[4].trim().takeIf { it.isNotBlank() } ?: "#9C27B0",
                icon = parseIcon(columns[5].trim()),
                iconColor = columns[6].trim().takeIf { it.isNotBlank() } ?: "#ffffff"
            )
            else -> ShieldData(
                label = columns.getOrNull(0)?.trim() ?: "Label",
                message = columns.getOrNull(1)?.trim() ?: "Message"
            )
        }

        // Check if the message indicates a status
        return applyStatusIfApplicable(baseShield)
    }

    private fun parseIcon(iconStr: String): String? {
        return when {
            iconStr.startsWith("<") && iconStr.endsWith(">") ->
                iconStr.substring(1, iconStr.length - 1)
            iconStr.isNotBlank() -> iconStr
            else -> null
        }
    }

    private fun applyStatusIfApplicable(shield: ShieldData): ShieldData {
        val status = when (shield.message.lowercase()) {
            "passing", "passed", "success", "successful" -> ShieldStatus.SUCCESS
            "failed", "failing", "error" -> ShieldStatus.FAILED
            "running", "building", "in progress" -> ShieldStatus.RUNNING
            "pending", "waiting", "queued" -> ShieldStatus.PENDING
            "cancelled", "canceled", "stopped" -> ShieldStatus.CANCELLED
            "warning", "unstable" -> ShieldStatus.WARNING
            else -> null
        }

        return if (status != null && shield.leftColor == "#6A1B9A" && shield.rightColor == "#9C27B0") {
            shield.copy(
                leftColor = status.leftColor,
                rightColor = status.rightColor,
                icon = shield.icon ?: status.icon,
                status = status
            )
        } else {
            shield
        }
    }
}

/**
 * Configuration for iOS-style shield table rendering
 */
data class ShieldTableConfig(
    var defaultStyle: ShieldStyle = ShieldStyle.IOS,
    var theme: String = "ios",
    var spacing: Int = 15,
    var arrangement: ShieldArrangement = ShieldArrangement.GRID,
    var backgroundStyle: String = "ios-container", // iOS-style container
    var animationEnabled: Boolean = true
)

enum class ShieldArrangement {
    HORIZONTAL,
    VERTICAL,
    GRID
}

/**
 * Converts a List<ShieldData> to CSV format
 * @return CsvResponse with headers and rows representing the shield data
 */
fun List<ShieldData>.toCsv(): CsvResponse {
    val headers = listOf(
        "Shield Number", "Label", "Message", "Link", "Left Color", "Right Color",
        "Icon", "Icon Color", "Style", "Label Color", "Message Color",
        "Width", "Height", "Status"
    )
    val csvRows = mutableListOf<List<String>>()

    if (this.isNotEmpty()) {
        this.forEachIndexed { index, shield ->
            csvRows.add(listOf(
                (index + 1).toString(),
                shield.label,
                shield.message,
                shield.link ?: "",
                shield.leftColor,
                shield.rightColor,
                shield.icon ?: "",
                shield.iconColor,
                shield.style.value,
                shield.labelColor,
                shield.messageColor,
                shield.width?.toString() ?: "",
                shield.height.toString(),
                shield.status?.name ?: ""
            ))
        }
    } else {
        // If no shields, add an empty row
        csvRows.add(listOf(
            "0", "", "", "", "", "", "", "", "", "", "", "", "", ""
        ))
    }

    return CsvResponse(headers, csvRows)
}