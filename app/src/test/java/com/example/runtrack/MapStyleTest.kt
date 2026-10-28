package com.example.runtrack

import com.example.runtrack.settings.MapLayerStyle
import com.example.runtrack.settings.MapStyleConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapStyleTest {

    @Test
    fun testDefaultMapStyleConfiguration() {
        val config = MapStyleConfig()
        assertEquals(MapLayerStyle.DARK_HUD, config.layerStyle)
        assertTrue(config.showHeatmap)
        assertFalse(config.show3dBuildings)
        assertTrue(config.showElevationContours)
    }

    @Test
    fun testMapStyleTransitions() {
        val config = MapStyleConfig()

        config.layerStyle = MapLayerStyle.SATELLITE
        assertEquals(MapLayerStyle.SATELLITE, config.layerStyle)

        config.layerStyle = MapLayerStyle.TERRAIN
        assertEquals(MapLayerStyle.TERRAIN, config.layerStyle)

        config.layerStyle = MapLayerStyle.LIGHT_MINIMAL
        assertEquals(MapLayerStyle.LIGHT_MINIMAL, config.layerStyle)
    }

    @Test
    fun testOverlayToggles() {
        val config = MapStyleConfig(
            layerStyle = MapLayerStyle.TERRAIN,
            showHeatmap = false,
            show3dBuildings = true,
            showElevationContours = false
        )

        assertFalse(config.showHeatmap)
        assertTrue(config.show3dBuildings)
        assertFalse(config.showElevationContours)
    }

    @Test
    fun testMapStyleParsingFallback() {
        val valid = try {
            MapLayerStyle.valueOf("SATELLITE")
        } catch (_: Exception) {
            MapLayerStyle.DARK_HUD
        }
        assertEquals(MapLayerStyle.SATELLITE, valid)

        val invalid = try {
            MapLayerStyle.valueOf("NON_EXISTING_STYLE")
        } catch (_: Exception) {
            MapLayerStyle.DARK_HUD
        }
        assertEquals(MapLayerStyle.DARK_HUD, invalid)
    }
}
