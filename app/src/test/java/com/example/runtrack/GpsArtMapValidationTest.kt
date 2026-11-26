package com.example.runtrack

import com.example.runtrack.network.model.community.GpsArtRouteDto
import com.example.runtrack.network.model.dashboard.RoutePointDto
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GpsArtMapValidationTest {

    private val gson = Gson()

    private fun toLatLngList(points: List<RoutePointDto>): List<LatLng> {
        return points.map { LatLng(it.lat, it.lng) }
    }

    private fun isValidCoordinate(point: RoutePointDto): Boolean {
        return point.lat.isFinite() &&
            point.lng.isFinite() &&
            point.lat in -90.0..90.0 &&
            point.lng in -180.0..180.0
    }

    private fun parseRoutePointsJson(pointsJson: String?): List<RoutePointDto> {
        if (pointsJson.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<RoutePointDto>>() {}.type
            gson.fromJson<List<RoutePointDto>>(pointsJson, type).orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun sampleHoGuomPoints(): List<RoutePointDto> {
        return listOf(
            RoutePointDto(21.0285, 105.8542),
            RoutePointDto(21.0315, 105.8515),
            RoutePointDto(21.0330, 105.8542),
            RoutePointDto(21.0315, 105.8569),
            RoutePointDto(21.0285, 105.8542)
        )
    }

    @Test
    fun testRoutePointDtoToLatLngConversion() {
        val points = sampleHoGuomPoints()
        val latLngs = toLatLngList(points)

        assertEquals(points.size, latLngs.size)
        for (i in points.indices) {
            assertEquals(points[i].lat, latLngs[i].latitude, 1e-6)
            assertEquals(points[i].lng, latLngs[i].longitude, 1e-6)
        }
    }

    @Test
    fun testValidCoordinateBoundaries() {
        val boundaryPoints = listOf(
            RoutePointDto(-90.0, -180.0),
            RoutePointDto(90.0, 180.0),
            RoutePointDto(0.0, 0.0),
            RoutePointDto(-33.8688, 151.2093),
            RoutePointDto(37.7749, -122.4194)
        )

        boundaryPoints.forEach { point ->
            assertTrue("Expected valid coordinate: $point", isValidCoordinate(point))
            val latLng = LatLng(point.lat, point.lng)
            assertEquals(point.lat, latLng.latitude, 1e-6)
            assertTrue(latLng.longitude in -180.0..180.0)
        }
    }

    @Test
    fun testInvalidAndOutOfRangeCoordinates() {
        val invalidPoints = listOf(
            RoutePointDto(90.0001, 105.8542),
            RoutePointDto(-90.0001, 105.8542),
            RoutePointDto(21.0285, 180.0001),
            RoutePointDto(21.0285, -180.0001),
            RoutePointDto(Double.NaN, 105.8542),
            RoutePointDto(21.0285, Double.POSITIVE_INFINITY),
            RoutePointDto(Double.NEGATIVE_INFINITY, 0.0)
        )

        invalidPoints.forEach { point ->
            assertFalse("Expected invalid coordinate: $point", isValidCoordinate(point))
        }

        val clampedNorth = LatLng(95.0, 105.8542)
        val clampedSouth = LatLng(-95.0, 105.8542)
        assertEquals(90.0, clampedNorth.latitude, 1e-6)
        assertEquals(-90.0, clampedSouth.latitude, 1e-6)
    }

    @Test
    fun testEmptyAndSinglePointLists() {
        val emptyLatLngs = toLatLngList(emptyList())
        assertTrue(emptyLatLngs.isEmpty())

        val singlePoint = listOf(RoutePointDto(21.0285, 105.8542))
        val singleLatLngs = toLatLngList(singlePoint)
        assertEquals(1, singleLatLngs.size)
        assertEquals(21.0285, singleLatLngs.first().latitude, 1e-6)
        assertEquals(105.8542, singleLatLngs.first().longitude, 1e-6)
    }

    @Test
    fun testIntentRoutePointsJsonRoundTrip() {
        val originalPoints = sampleHoGuomPoints()
        val serialized = gson.toJson(originalPoints)
        val deserialized = parseRoutePointsJson(serialized)

        assertEquals(originalPoints.size, deserialized.size)
        assertEquals(originalPoints, deserialized)
        assertEquals("EXTRA_ROUTE_NAME", TrackingActivity.EXTRA_ROUTE_NAME)
        assertEquals("EXTRA_ROUTE_POINTS_JSON", TrackingActivity.EXTRA_ROUTE_POINTS_JSON)
    }

    @Test
    fun testIntentRoutePointsJsonEdgeCases() {
        assertTrue(parseRoutePointsJson(null).isEmpty())
        assertTrue(parseRoutePointsJson("").isEmpty())
        assertTrue(parseRoutePointsJson("   ").isEmpty())
        assertTrue(parseRoutePointsJson("[]").isEmpty())
        assertTrue(parseRoutePointsJson("{invalid-json").isEmpty())
        assertTrue(parseRoutePointsJson("null").isEmpty())

        val singleJson = """[{"lat":21.0285,"lng":105.8542}]"""
        val singleParsed = parseRoutePointsJson(singleJson)
        assertEquals(1, singleParsed.size)
        assertEquals(RoutePointDto(21.0285, 105.8542), singleParsed.first())
    }

    @Test
    fun testGpsArtRouteDtoJsonDeserialization() {
        val payload = """
            [
              {
                "id": "trex-west-lake",
                "name": "Khủng Long Hồ Tây",
                "distance_km": 6.85,
                "city": "Hà Nội",
                "icon_key": "dinosaur",
                "points": [
                  {"lat": 21.0583, "lng": 105.8235},
                  {"lat": 21.0612, "lng": 105.8261}
                ]
              }
            ]
        """.trimIndent()

        val type = object : TypeToken<List<GpsArtRouteDto>>() {}.type
        val routes = gson.fromJson<List<GpsArtRouteDto>>(payload, type)

        assertEquals(1, routes.size)
        assertEquals("trex-west-lake", routes.first().id)
        assertEquals("Khủng Long Hồ Tây", routes.first().name)
        assertEquals(6.85, routes.first().distance_km, 1e-6)
        assertEquals("dinosaur", routes.first().icon_key)
        assertEquals(2, routes.first().points.size)
        assertTrue(routes.first().points.all { isValidCoordinate(it) })
    }
}
