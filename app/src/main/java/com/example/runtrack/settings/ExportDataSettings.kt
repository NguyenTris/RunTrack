package com.example.runtrack.settings

enum class ExportFormat {
    GPX,
    TCX,
    CSV,
    FIT
}

enum class ExportDateRange {
    LAST_30_DAYS,
    THIS_MONTH,
    ALL_TIME
}

data class ExportDataConfig(
    var format: ExportFormat = ExportFormat.GPX,
    var dateRange: ExportDateRange = ExportDateRange.LAST_30_DAYS,
    var includeHeartRate: Boolean = true,
    var includeCadence: Boolean = true,
    var includeElevation: Boolean = true
)
