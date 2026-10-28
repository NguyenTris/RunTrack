package com.example.runtrack.settings

enum class UnitSystem {
    METRIC,
    IMPERIAL
}

enum class PaceDisplayFormat {
    MIN_PER_KM,
    MIN_PER_MILE,
    SPEED_KMH
}

data class UnitsConfig(
    var unitSystem: UnitSystem = UnitSystem.METRIC,
    var paceFormat: PaceDisplayFormat = PaceDisplayFormat.MIN_PER_KM,
    var elevationUnitMeters: Boolean = true,
    var temperatureCelsius: Boolean = true,
    var weightUnitKg: Boolean = true
)
