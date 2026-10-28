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
 * [SCR-THEME-01] Theme & HUD Palettes Settings Screen.
 */
class ThemeFragment : Fragment() {

    private lateinit var cardAerospace: View
    private lateinit var radioAerospace: TextView
    private lateinit var cardAmoled: View
    private lateinit var radioAmoled: TextView
    private lateinit var cardCyan: View
    private lateinit var radioCyan: TextView
    private lateinit var cardSolar: View
    private lateinit var radioSolar: TextView

    private lateinit var switchAutoSunset: SwitchCompat
    private lateinit var switchHighContrastNeon: SwitchCompat
    private lateinit var switchReduceMotion: SwitchCompat
    private lateinit var btnSave: TextView

    private var currentTheme = HudThemeMode.AEROSPACE_DARK

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hud_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadPreferences()
        setupListeners(view)
        updateRadioUI()
    }

    private fun initViews(view: View) {
        cardAerospace = view.findViewById(R.id.card_theme_aerospace)
        radioAerospace = view.findViewById(R.id.radio_aerospace_indicator)
        cardAmoled = view.findViewById(R.id.card_theme_amoled)
        radioAmoled = view.findViewById(R.id.radio_amoled_indicator)
        cardCyan = view.findViewById(R.id.card_theme_cyan)
        radioCyan = view.findViewById(R.id.radio_cyan_indicator)
        cardSolar = view.findViewById(R.id.card_theme_solar)
        radioSolar = view.findViewById(R.id.radio_solar_indicator)

        switchAutoSunset = view.findViewById(R.id.switch_auto_sunset)
        switchHighContrastNeon = view.findViewById(R.id.switch_high_contrast_neon)
        switchReduceMotion = view.findViewById(R.id.switch_reduce_motion)
        btnSave = view.findViewById(R.id.btnSaveThemeSettings)
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_theme_settings", Context.MODE_PRIVATE)
        val savedThemeName = prefs.getString("theme_mode", HudThemeMode.AEROSPACE_DARK.name)
        currentTheme = try {
            HudThemeMode.valueOf(savedThemeName ?: HudThemeMode.AEROSPACE_DARK.name)
        } catch (_: Exception) {
            HudThemeMode.AEROSPACE_DARK
        }
        switchAutoSunset.isChecked = prefs.getBoolean("auto_sunset", true)
        switchHighContrastNeon.isChecked = prefs.getBoolean("high_contrast_neon", true)
        switchReduceMotion.isChecked = prefs.getBoolean("reduce_motion", false)
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        cardAerospace.setOnClickListener {
            currentTheme = HudThemeMode.AEROSPACE_DARK
            updateRadioUI()
        }
        cardAmoled.setOnClickListener {
            currentTheme = HudThemeMode.AMOLED_BLACK
            updateRadioUI()
        }
        cardCyan.setOnClickListener {
            currentTheme = HudThemeMode.CYAN_STEALTH
            updateRadioUI()
        }
        cardSolar.setOnClickListener {
            currentTheme = HudThemeMode.SOLAR_LIGHT
            updateRadioUI()
        }

        btnSave.setOnClickListener {
            savePreferences()
        }
    }

    private fun updateRadioUI() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.kinetic_orange)
        val dimColor = ContextCompat.getColor(requireContext(), R.color.hud_text_dim)

        radioAerospace.text = if (currentTheme == HudThemeMode.AEROSPACE_DARK) "●" else "○"
        radioAerospace.setTextColor(if (currentTheme == HudThemeMode.AEROSPACE_DARK) activeColor else dimColor)

        radioAmoled.text = if (currentTheme == HudThemeMode.AMOLED_BLACK) "●" else "○"
        radioAmoled.setTextColor(if (currentTheme == HudThemeMode.AMOLED_BLACK) activeColor else dimColor)

        radioCyan.text = if (currentTheme == HudThemeMode.CYAN_STEALTH) "●" else "○"
        radioCyan.setTextColor(if (currentTheme == HudThemeMode.CYAN_STEALTH) activeColor else dimColor)

        radioSolar.text = if (currentTheme == HudThemeMode.SOLAR_LIGHT) "●" else "○"
        radioSolar.setTextColor(if (currentTheme == HudThemeMode.SOLAR_LIGHT) activeColor else dimColor)
    }

    private fun savePreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_theme_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("theme_mode", currentTheme.name)
            .putBoolean("auto_sunset", switchAutoSunset.isChecked)
            .putBoolean("high_contrast_neon", switchHighContrastNeon.isChecked)
            .putBoolean("reduce_motion", switchReduceMotion.isChecked)
            .apply()

        Toast.makeText(requireContext(), "Đã lưu cài đặt chủ đề giao diện HUD", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}
