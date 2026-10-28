package com.example.runtrack.settings

enum class HudThemeMode {
    AEROSPACE_DARK,
    AMOLED_BLACK,
    CYAN_STEALTH,
    SOLAR_LIGHT
}

data class ThemeConfig(
    var themeMode: HudThemeMode = HudThemeMode.AEROSPACE_DARK,
    var autoSwitchSunset: Boolean = true,
    var highContrastNeon: Boolean = true,
    var reduceMotion: Boolean = false
)
