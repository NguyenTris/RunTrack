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
 * [SCR-GPS-01] GPS Accuracy & Telemetry Filters Settings Screen.
 */
class GpsAccuracyFragment : Fragment() {

    private lateinit var cardArt: View
    private lateinit var radioArt: TextView
    private lateinit var cardBalanced: View
    private lateinit var radioBalanced: TextView
    private lateinit var cardSaver: View
    private lateinit var radioSaver: TextView

    private lateinit var switchKalman: SwitchCompat
    private lateinit var switchAutoPause: SwitchCompat
    private lateinit var switchMultiGnss: SwitchCompat
    private lateinit var btnSave: TextView

    private var currentMode = GpsSamplingMode.BALANCED_STREET

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hud_gps_accuracy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadPreferences()
        setupListeners(view)
        updateRadioUI()
    }

    private fun initViews(view: View) {
        cardArt = view.findViewById(R.id.card_gps_art)
        radioArt = view.findViewById(R.id.radio_art_indicator)
        cardBalanced = view.findViewById(R.id.card_gps_balanced)
        radioBalanced = view.findViewById(R.id.radio_balanced_indicator)
        cardSaver = view.findViewById(R.id.card_gps_saver)
        radioSaver = view.findViewById(R.id.radio_saver_indicator)

        switchKalman = view.findViewById(R.id.switch_kalman_filter)
        switchAutoPause = view.findViewById(R.id.switch_auto_pause_speed)
        switchMultiGnss = view.findViewById(R.id.switch_multi_gnss)
        btnSave = view.findViewById(R.id.btnSaveGpsSettings)
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_gps_settings", Context.MODE_PRIVATE)
        val savedModeName = prefs.getString("sampling_mode", GpsSamplingMode.BALANCED_STREET.name)
        currentMode = try {
            GpsSamplingMode.valueOf(savedModeName ?: GpsSamplingMode.BALANCED_STREET.name)
        } catch (_: Exception) {
            GpsSamplingMode.BALANCED_STREET
        }
        switchKalman.isChecked = prefs.getBoolean("kalman_filter", true)
        switchAutoPause.isChecked = prefs.getBoolean("auto_pause_speed", true)
        switchMultiGnss.isChecked = prefs.getBoolean("multi_gnss", true)
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        cardArt.setOnClickListener {
            currentMode = GpsSamplingMode.HIGH_PRECISION_ART
            updateRadioUI()
        }
        cardBalanced.setOnClickListener {
            currentMode = GpsSamplingMode.BALANCED_STREET
            updateRadioUI()
        }
        cardSaver.setOnClickListener {
            currentMode = GpsSamplingMode.BATTERY_SAVER
            updateRadioUI()
        }

        btnSave.setOnClickListener {
            savePreferences()
        }
    }

    private fun updateRadioUI() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.kinetic_orange)
        val dimColor = ContextCompat.getColor(requireContext(), R.color.hud_text_dim)

        radioArt.text = if (currentMode == GpsSamplingMode.HIGH_PRECISION_ART) "●" else "○"
        radioArt.setTextColor(if (currentMode == GpsSamplingMode.HIGH_PRECISION_ART) activeColor else dimColor)

        radioBalanced.text = if (currentMode == GpsSamplingMode.BALANCED_STREET) "●" else "○"
        radioBalanced.setTextColor(if (currentMode == GpsSamplingMode.BALANCED_STREET) activeColor else dimColor)

        radioSaver.text = if (currentMode == GpsSamplingMode.BATTERY_SAVER) "●" else "○"
        radioSaver.setTextColor(if (currentMode == GpsSamplingMode.BATTERY_SAVER) activeColor else dimColor)
    }

    private fun savePreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_gps_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("sampling_mode", currentMode.name)
            .putBoolean("kalman_filter", switchKalman.isChecked)
            .putBoolean("auto_pause_speed", switchAutoPause.isChecked)
            .putBoolean("multi_gnss", switchMultiGnss.isChecked)
            .apply()

        Toast.makeText(requireContext(), "Đã lưu thiết lập GPS và thuật toán lọc telemetry", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}
