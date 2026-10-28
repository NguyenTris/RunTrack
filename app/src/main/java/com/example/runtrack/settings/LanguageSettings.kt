package com.example.runtrack.settings

enum class AppLanguage {
    VIETNAMESE,
    ENGLISH,
    JAPANESE,
    FRENCH
}

enum class HudFontScale {
    STANDARD,
    LARGE,
    EXTRA_LARGE
}

data class LanguageFontConfig(
    var language: AppLanguage = AppLanguage.VIETNAMESE,
    var fontScale: HudFontScale = HudFontScale.STANDARD,
    var useTechMonospace: Boolean = true,
    var boldNumbersOnDashboard: Boolean = true
)
