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
 * [SCR-UNIT-01] Units & Measurements Settings Screen.
 */
class UnitsFragment : Fragment() {

    private lateinit var cardMetric: View
    private lateinit var radioMetric: TextView
    private lateinit var cardImperial: View
    private lateinit var radioImperial: TextView

    private lateinit var cardPaceMinKm: View
    private lateinit var radioPaceMinKm: TextView
    private lateinit var cardPaceMinMi: View
    private lateinit var radioPaceMinMi: TextView
    private lateinit var cardPaceKmh: View
    private lateinit var radioPaceKmh: TextView

    private lateinit var switchElevation: SwitchCompat
    private lateinit var switchTemperature: SwitchCompat
    private lateinit var switchWeight: SwitchCompat
    private lateinit var btnSave: TextView

    private var currentSystem = UnitSystem.METRIC
    private var currentPaceFormat = PaceDisplayFormat.MIN_PER_KM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hud_units, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadPreferences()
        setupListeners(view)
        updateRadioUI()
    }

    private fun initViews(view: View) {
        cardMetric = view.findViewById(R.id.card_unit_metric)
        radioMetric = view.findViewById(R.id.radio_metric_indicator)
        cardImperial = view.findViewById(R.id.card_unit_imperial)
        radioImperial = view.findViewById(R.id.radio_imperial_indicator)

        cardPaceMinKm = view.findViewById(R.id.card_pace_minkm)
        radioPaceMinKm = view.findViewById(R.id.radio_pace_minkm_indicator)
        cardPaceMinMi = view.findViewById(R.id.card_pace_minmi)
        radioPaceMinMi = view.findViewById(R.id.radio_pace_minmi_indicator)
        cardPaceKmh = view.findViewById(R.id.card_pace_kmh)
        radioPaceKmh = view.findViewById(R.id.radio_pace_kmh_indicator)

        switchElevation = view.findViewById(R.id.switch_elevation_meters)
        switchTemperature = view.findViewById(R.id.switch_temperature_celsius)
        switchWeight = view.findViewById(R.id.switch_weight_kg)
        btnSave = view.findViewById(R.id.btnSaveUnits)
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_units_settings", Context.MODE_PRIVATE)
        val savedSystem = prefs.getString("unit_system", UnitSystem.METRIC.name)
        currentSystem = try {
            UnitSystem.valueOf(savedSystem ?: UnitSystem.METRIC.name)
        } catch (_: Exception) {
            UnitSystem.METRIC
        }

        val savedPace = prefs.getString("pace_format", PaceDisplayFormat.MIN_PER_KM.name)
        currentPaceFormat = try {
            PaceDisplayFormat.valueOf(savedPace ?: PaceDisplayFormat.MIN_PER_KM.name)
        } catch (_: Exception) {
            PaceDisplayFormat.MIN_PER_KM
        }

        switchElevation.isChecked = prefs.getBoolean("elevation_meters", true)
        switchTemperature.isChecked = prefs.getBoolean("temperature_celsius", true)
        switchWeight.isChecked = prefs.getBoolean("weight_kg", true)
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        cardMetric.setOnClickListener {
            currentSystem = UnitSystem.METRIC
            currentPaceFormat = PaceDisplayFormat.MIN_PER_KM
            switchElevation.isChecked = true
            switchTemperature.isChecked = true
            switchWeight.isChecked = true
            updateRadioUI()
        }

        cardImperial.setOnClickListener {
            currentSystem = UnitSystem.IMPERIAL
            currentPaceFormat = PaceDisplayFormat.MIN_PER_MILE
            switchElevation.isChecked = false
            switchTemperature.isChecked = false
            switchWeight.isChecked = false
            updateRadioUI()
        }

        cardPaceMinKm.setOnClickListener {
            currentPaceFormat = PaceDisplayFormat.MIN_PER_KM
            updateRadioUI()
        }

        cardPaceMinMi.setOnClickListener {
            currentPaceFormat = PaceDisplayFormat.MIN_PER_MILE
            updateRadioUI()
        }

        cardPaceKmh.setOnClickListener {
            currentPaceFormat = PaceDisplayFormat.SPEED_KMH
            updateRadioUI()
        }

        btnSave.setOnClickListener {
            savePreferences()
        }
    }

    private fun updateRadioUI() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.kinetic_orange)
        val dimColor = ContextCompat.getColor(requireContext(), R.color.hud_text_dim)

        radioMetric.text = if (currentSystem == UnitSystem.METRIC) "●" else "○"
        radioMetric.setTextColor(if (currentSystem == UnitSystem.METRIC) activeColor else dimColor)

        radioImperial.text = if (currentSystem == UnitSystem.IMPERIAL) "●" else "○"
        radioImperial.setTextColor(if (currentSystem == UnitSystem.IMPERIAL) activeColor else dimColor)

        radioPaceMinKm.text = if (currentPaceFormat == PaceDisplayFormat.MIN_PER_KM) "●" else "○"
        radioPaceMinKm.setTextColor(if (currentPaceFormat == PaceDisplayFormat.MIN_PER_KM) activeColor else dimColor)

        radioPaceMinMi.text = if (currentPaceFormat == PaceDisplayFormat.MIN_PER_MILE) "●" else "○"
        radioPaceMinMi.setTextColor(if (currentPaceFormat == PaceDisplayFormat.MIN_PER_MILE) activeColor else dimColor)

        radioPaceKmh.text = if (currentPaceFormat == PaceDisplayFormat.SPEED_KMH) "●" else "○"
        radioPaceKmh.setTextColor(if (currentPaceFormat == PaceDisplayFormat.SPEED_KMH) activeColor else dimColor)
    }

    private fun savePreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_units_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("unit_system", currentSystem.name)
            .putString("pace_format", currentPaceFormat.name)
            .putBoolean("elevation_meters", switchElevation.isChecked)
            .putBoolean("temperature_celsius", switchTemperature.isChecked)
            .putBoolean("weight_kg", switchWeight.isChecked)
            .apply()

        Toast.makeText(requireContext(), "Đã lưu cấu hình hệ đơn vị & đo lường", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}
