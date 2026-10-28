package com.example.runtrack.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.runtrack.R

/**
 * [SCR-MAP-01] Map Styles & Layer Overlays Settings Screen.
 */
class MapStyleFragment : Fragment() {

    private lateinit var cardDarkHud: View
    private lateinit var radioDarkHud: TextView
    private lateinit var cardSatellite: View
    private lateinit var radioSatellite: TextView
    private lateinit var cardTerrain: View
    private lateinit var radioTerrain: TextView
    private lateinit var cardLight: View
    private lateinit var radioLight: TextView

    private lateinit var switchHeatmap: SwitchCompat
    private lateinit var switch3dBuildings: SwitchCompat
    private lateinit var switchElevationContours: SwitchCompat
    private lateinit var btnSave: TextView

    private var currentStyle = MapLayerStyle.DARK_HUD

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hud_map_style, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadPreferences()
        setupListeners(view)
        updateRadioUI()
    }

    private fun initViews(view: View) {
        cardDarkHud = view.findViewById(R.id.card_map_dark_hud)
        radioDarkHud = view.findViewById(R.id.radio_dark_hud_indicator)
        cardSatellite = view.findViewById(R.id.card_map_satellite)
        radioSatellite = view.findViewById(R.id.radio_satellite_indicator)
        cardTerrain = view.findViewById(R.id.card_map_terrain)
        radioTerrain = view.findViewById(R.id.radio_terrain_indicator)
        cardLight = view.findViewById(R.id.card_map_light)
        radioLight = view.findViewById(R.id.radio_light_indicator)

        switchHeatmap = view.findViewById(R.id.switch_heatmap)
        switch3dBuildings = view.findViewById(R.id.switch_3d_buildings)
        switchElevationContours = view.findViewById(R.id.switch_elevation_contours)
        btnSave = view.findViewById(R.id.btnSaveMapSettings)
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_map_settings", Context.MODE_PRIVATE)
        val savedStyleName = prefs.getString("layer_style", MapLayerStyle.DARK_HUD.name)
        currentStyle = try {
            MapLayerStyle.valueOf(savedStyleName ?: MapLayerStyle.DARK_HUD.name)
        } catch (_: Exception) {
            MapLayerStyle.DARK_HUD
        }
        switchHeatmap.isChecked = prefs.getBoolean("show_heatmap", true)
        switch3dBuildings.isChecked = prefs.getBoolean("show_3d_buildings", false)
        switchElevationContours.isChecked = prefs.getBoolean("show_elevation_contours", true)
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        cardDarkHud.setOnClickListener {
            currentStyle = MapLayerStyle.DARK_HUD
            updateRadioUI()
        }
        cardSatellite.setOnClickListener {
            currentStyle = MapLayerStyle.SATELLITE
            updateRadioUI()
        }
        cardTerrain.setOnClickListener {
            currentStyle = MapLayerStyle.TERRAIN
            updateRadioUI()
        }
        cardLight.setOnClickListener {
            currentStyle = MapLayerStyle.LIGHT_MINIMAL
            updateRadioUI()
        }

        btnSave.setOnClickListener {
            savePreferences()
        }
    }

    private fun updateRadioUI() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.kinetic_orange)
        val dimColor = ContextCompat.getColor(requireContext(), R.color.hud_text_dim)

        radioDarkHud.text = if (currentStyle == MapLayerStyle.DARK_HUD) "●" else "○"
        radioDarkHud.setTextColor(if (currentStyle == MapLayerStyle.DARK_HUD) activeColor else dimColor)

        radioSatellite.text = if (currentStyle == MapLayerStyle.SATELLITE) "●" else "○"
        radioSatellite.setTextColor(if (currentStyle == MapLayerStyle.SATELLITE) activeColor else dimColor)

        radioTerrain.text = if (currentStyle == MapLayerStyle.TERRAIN) "●" else "○"
        radioTerrain.setTextColor(if (currentStyle == MapLayerStyle.TERRAIN) activeColor else dimColor)

        radioLight.text = if (currentStyle == MapLayerStyle.LIGHT_MINIMAL) "●" else "○"
        radioLight.setTextColor(if (currentStyle == MapLayerStyle.LIGHT_MINIMAL) activeColor else dimColor)
    }

    private fun savePreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_map_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("layer_style", currentStyle.name)
            .putBoolean("show_heatmap", switchHeatmap.isChecked)
            .putBoolean("show_3d_buildings", switch3dBuildings.isChecked)
            .putBoolean("show_elevation_contours", switchElevationContours.isChecked)
            .apply()

        Toast.makeText(requireContext(), "Đã lưu kiểu bản đồ và các lớp phủ dữ liệu", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}
