package com.example.runtrack

import android.graphics.Color
import com.example.runtrack.network.model.community.GpsArtRouteDto
import com.example.runtrack.network.model.dashboard.RoutePointDto
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.util.Locale

object GpsArtMapHelper {

    val NEON_GREEN: Int = Color.parseColor("#39FF14")

    fun resolveIconRes(iconKey: String): Int {
        return when (iconKey.lowercase()) {
            "dinosaur", "trex" -> R.drawable.ic_dinosaur
            "heart" -> R.drawable.ic_heart
            "bolt", "thunder", "lightning" -> R.drawable.ic_thunderbolt
            else -> R.drawable.ic_target
        }
    }

    fun toSuggestion(route: GpsArtRouteDto): ArtSuggestion {
        return ArtSuggestion(
            id = route.id,
            name = route.name,
            distance = String.format(Locale.US, "%.2f KM", route.distance_km),
            iconResId = resolveIconRes(route.icon_key)
        )
    }

    fun drawRouteOnMap(map: GoogleMap, route: GpsArtRouteDto) {
        map.clear()
        val latLngs = route.points.map { LatLng(it.lat, it.lng) }
        if (latLngs.isEmpty()) return

        map.addPolyline(
            PolylineOptions()
                .addAll(latLngs)
                .color(NEON_GREEN)
                .width(10f)
                .geodesic(true)
        )
        map.addMarker(MarkerOptions().position(latLngs.first()).title(route.name))
        zoomToPoints(map, latLngs)
    }

    fun zoomToPoints(map: GoogleMap, points: List<LatLng>, paddingPx: Int = 64) {
        if (points.isEmpty()) return
        if (points.size == 1) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 15f))
            return
        }
        val builder = LatLngBounds.Builder()
        points.forEach { builder.include(it) }
        try {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), paddingPx))
        } catch (_: Exception) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 14.5f))
        }
    }

    fun fallbackRoutes(): List<GpsArtRouteDto> {
        return listOf(
            GpsArtRouteDto(
                id = "heart-ho-guom",
                name = "Trái Tim Hồ Gươm",
                distance_km = 5.20,
                city = "Hà Nội",
                icon_key = "heart",
                points = listOf(
                    RoutePointDto(21.0285, 105.8542),
                    RoutePointDto(21.0315, 105.8515),
                    RoutePointDto(21.0330, 105.8542),
                    RoutePointDto(21.0315, 105.8569),
                    RoutePointDto(21.0285, 105.8542)
                )
            )
        )
    }
}
