package io.docops.docopsextensionssupport.badge.type


import io.docops.docopsextensionssupport.badge.Badge
import io.docops.docopsextensionssupport.badge.BadgeConfig
import io.docops.docopsextensionssupport.badge.BadgeStyle

class Neon(val darkMode: Boolean) : ThemedBadge(darkMode){

    override fun generate(badges: MutableList<Badge>, config: BadgeConfig): String {
        return super.generate(badges, config, BadgeStyle.NEON)
    }
}

class Glassmorphic(val darkMode: Boolean) : ThemedBadge(darkMode) {
    override fun generate(badges: MutableList<Badge>, config: BadgeConfig): String {
        return super.generate(badges, config, BadgeStyle.GLASSMORPHIC)
    }
}

class Brutalist(val darkMode: Boolean) : ThemedBadge(darkMode) {
    override fun generate(badges: MutableList<Badge>, config: BadgeConfig): String {
        return super.generate(badges, config, BadgeStyle.BRUTALIST)
    }
}

class Gradient(val darkMode: Boolean) : ThemedBadge(darkMode) {
    override fun generate(badges: MutableList<Badge>, config: BadgeConfig): String {
        return super.generate(badges, config, BadgeStyle.GRADIENT)
    }
}

class Minimal(val darkMode: Boolean) : ThemedBadge(darkMode) {
    override fun generate(badges: MutableList<Badge>, config: BadgeConfig): String {
        return super.generate(badges, config, BadgeStyle.MINIMAL)
    }
}

class Neumorphic(val darkMode: Boolean) : ThemedBadge(darkMode) {
    override fun generate(badges: MutableList<Badge>, config: BadgeConfig): String {
        return super.generate(badges, config, BadgeStyle.NEUMORPHIC)
    }
}