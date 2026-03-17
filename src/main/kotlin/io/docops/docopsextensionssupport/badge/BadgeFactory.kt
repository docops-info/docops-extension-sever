package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.badge.type.Brutalist
import io.docops.docopsextensionssupport.badge.type.Classic
import io.docops.docopsextensionssupport.badge.type.Glassmorphic
import io.docops.docopsextensionssupport.badge.type.Gradient
import io.docops.docopsextensionssupport.badge.type.Minimal
import io.docops.docopsextensionssupport.badge.type.Neon
import io.docops.docopsextensionssupport.badge.type.Neumorphic


object BadgeFactory {

    fun generate(badges: MutableList<Badge>, config: BadgeConfig, darkMode: Boolean = false) : String {
        return when(config.type.lowercase()) {
            "glassmorphic", "glass" -> Glassmorphic(darkMode).generate(badges, config)
            "neon" -> Neon(darkMode).generate(badges, config)
            "brutalist" -> Brutalist(darkMode).generate(badges, config)
            "gradient" -> Gradient(darkMode).generate(badges, config)
            "minimal" -> Minimal(darkMode).generate(badges, config)
            "neumorphic" -> Neumorphic(darkMode).generate(badges, config)
            "classic" -> Classic(darkMode).generateClassicBadge(badges, config,)
            else -> Classic(darkMode).generateClassicBadge(badges, config)
        }
    }


}