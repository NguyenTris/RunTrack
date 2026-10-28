package com.example.runtrack.settings

enum class MapLayerStyle {
    DARK_HUD,
    SATELLITE,
    TERRAIN,
    LIGHT_MINIMAL
}

data class MapStyleConfig(
    var layerStyle: MapLayerStyle = MapLayerStyle.DARK_HUD,
    var showHeatmap: Boolean = true,
    var show3dBuildings: Boolean = false,
    var showElevationContours: Boolean = true
)
