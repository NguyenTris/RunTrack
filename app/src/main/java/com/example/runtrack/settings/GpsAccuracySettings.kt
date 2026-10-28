package com.example.runtrack.settings

enum class GpsSamplingMode {
    HIGH_PRECISION_ART,
    BALANCED_STREET,
    BATTERY_SAVER
}

data class GpsAccuracyConfig(
    var mode: GpsSamplingMode = GpsSamplingMode.BALANCED_STREET,
    var enableKalmanFilter: Boolean = true,
    var autoPauseUnderSpeed: Boolean = true,
    var enableMultiGnss: Boolean = true
)
