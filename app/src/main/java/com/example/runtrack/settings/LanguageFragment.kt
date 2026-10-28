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
 * [SCR-LANG-01] Language & Font Scaling Settings Screen.
 */
class LanguageFragment : Fragment() {

    private lateinit var cardVi: View
    private lateinit var radioVi: TextView
    private lateinit var cardEn: View
    private lateinit var radioEn: TextView
    private lateinit var cardJa: View
    private lateinit var radioJa: TextView
    private lateinit var cardFr: View
    private lateinit var radioFr: TextView

    private lateinit var cardScaleStd: View
    private lateinit var radioScaleStd: TextView
    private lateinit var cardScaleLarge: View
    private lateinit var radioScaleLarge: TextView
    private lateinit var cardScaleXlarge: View
    private lateinit var radioScaleXlarge: TextView

    private lateinit var switchTechMonospace: SwitchCompat
    private lateinit var switchBoldNumbers: SwitchCompat
    private lateinit var btnSave: TextView

    private var currentLanguage = AppLanguage.VIETNAMESE
    private var currentScale = HudFontScale.STANDARD

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hud_language, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadPreferences()
        setupListeners(view)
        updateRadioUI()
    }

    private fun initViews(view: View) {
        cardVi = view.findViewById(R.id.card_lang_vi)
        radioVi = view.findViewById(R.id.radio_vi_indicator)
        cardEn = view.findViewById(R.id.card_lang_en)
        radioEn = view.findViewById(R.id.radio_en_indicator)
        cardJa = view.findViewById(R.id.card_lang_ja)
        radioJa = view.findViewById(R.id.radio_ja_indicator)
        cardFr = view.findViewById(R.id.card_lang_fr)
        radioFr = view.findViewById(R.id.radio_fr_indicator)

        cardScaleStd = view.findViewById(R.id.card_scale_std)
        radioScaleStd = view.findViewById(R.id.radio_scale_std_indicator)
        cardScaleLarge = view.findViewById(R.id.card_scale_large)
        radioScaleLarge = view.findViewById(R.id.radio_scale_large_indicator)
        cardScaleXlarge = view.findViewById(R.id.card_scale_xlarge)
        radioScaleXlarge = view.findViewById(R.id.radio_scale_xlarge_indicator)

        switchTechMonospace = view.findViewById(R.id.switch_tech_monospace)
        switchBoldNumbers = view.findViewById(R.id.switch_bold_numbers)
        btnSave = view.findViewById(R.id.btnSaveLanguageSettings)
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_language_settings", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("language", AppLanguage.VIETNAMESE.name)
        currentLanguage = try {
            AppLanguage.valueOf(savedLang ?: AppLanguage.VIETNAMESE.name)
        } catch (_: Exception) {
            AppLanguage.VIETNAMESE
        }

        val savedScale = prefs.getString("font_scale", HudFontScale.STANDARD.name)
        currentScale = try {
            HudFontScale.valueOf(savedScale ?: HudFontScale.STANDARD.name)
        } catch (_: Exception) {
            HudFontScale.STANDARD
        }

        switchTechMonospace.isChecked = prefs.getBoolean("tech_monospace", true)
        switchBoldNumbers.isChecked = prefs.getBoolean("bold_numbers", true)
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        cardVi.setOnClickListener {
            currentLanguage = AppLanguage.VIETNAMESE
            updateRadioUI()
        }
        cardEn.setOnClickListener {
            currentLanguage = AppLanguage.ENGLISH
            updateRadioUI()
        }
        cardJa.setOnClickListener {
            currentLanguage = AppLanguage.JAPANESE
            updateRadioUI()
        }
        cardFr.setOnClickListener {
            currentLanguage = AppLanguage.FRENCH
            updateRadioUI()
        }

        cardScaleStd.setOnClickListener {
            currentScale = HudFontScale.STANDARD
            updateRadioUI()
        }
        cardScaleLarge.setOnClickListener {
            currentScale = HudFontScale.LARGE
            updateRadioUI()
        }
        cardScaleXlarge.setOnClickListener {
            currentScale = HudFontScale.EXTRA_LARGE
            updateRadioUI()
        }

        btnSave.setOnClickListener {
            savePreferences()
        }
    }

    private fun updateRadioUI() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.kinetic_orange)
        val dimColor = ContextCompat.getColor(requireContext(), R.color.hud_text_dim)

        radioVi.text = if (currentLanguage == AppLanguage.VIETNAMESE) "●" else "○"
        radioVi.setTextColor(if (currentLanguage == AppLanguage.VIETNAMESE) activeColor else dimColor)

        radioEn.text = if (currentLanguage == AppLanguage.ENGLISH) "●" else "○"
        radioEn.setTextColor(if (currentLanguage == AppLanguage.ENGLISH) activeColor else dimColor)

        radioJa.text = if (currentLanguage == AppLanguage.JAPANESE) "●" else "○"
        radioJa.setTextColor(if (currentLanguage == AppLanguage.JAPANESE) activeColor else dimColor)

        radioFr.text = if (currentLanguage == AppLanguage.FRENCH) "●" else "○"
        radioFr.setTextColor(if (currentLanguage == AppLanguage.FRENCH) activeColor else dimColor)

        radioScaleStd.text = if (currentScale == HudFontScale.STANDARD) "●" else "○"
        radioScaleStd.setTextColor(if (currentScale == HudFontScale.STANDARD) activeColor else dimColor)

        radioScaleLarge.text = if (currentScale == HudFontScale.LARGE) "●" else "○"
        radioScaleLarge.setTextColor(if (currentScale == HudFontScale.LARGE) activeColor else dimColor)

        radioScaleXlarge.text = if (currentScale == HudFontScale.EXTRA_LARGE) "●" else "○"
        radioScaleXlarge.setTextColor(if (currentScale == HudFontScale.EXTRA_LARGE) activeColor else dimColor)
    }

    private fun savePreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_language_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("language", currentLanguage.name)
            .putString("font_scale", currentScale.name)
            .putBoolean("tech_monospace", switchTechMonospace.isChecked)
            .putBoolean("bold_numbers", switchBoldNumbers.isChecked)
            .apply()

        Toast.makeText(requireContext(), "Đã lưu thiết lập ngôn ngữ & cỡ chữ", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}
