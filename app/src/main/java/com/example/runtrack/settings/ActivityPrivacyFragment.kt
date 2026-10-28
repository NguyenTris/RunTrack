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
 * [SCR-PRIV-01] Activity Privacy Settings Screen.
 * Controls default visibility of runs, GPS privacy zone, and biometric telemetry concealment.
 */
class ActivityPrivacyFragment : Fragment() {

    private lateinit var cardPublic: View
    private lateinit var radioPublic: TextView
    private lateinit var cardFriends: View
    private lateinit var radioFriends: TextView
    private lateinit var cardPrivate: View
    private lateinit var radioPrivate: TextView

    private lateinit var switchHideStartEnd: SwitchCompat
    private lateinit var switchHideBiometrics: SwitchCompat
    private lateinit var btnSave: TextView

    private var currentScope = ActivityPrivacyScope.FRIENDS_ONLY

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hud_activity_privacy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadPreferences()
        setupListeners(view)
        updateRadioUI()
    }

    private fun initViews(view: View) {
        cardPublic = view.findViewById(R.id.card_scope_public)
        radioPublic = view.findViewById(R.id.radio_public_indicator)
        cardFriends = view.findViewById(R.id.card_scope_friends)
        radioFriends = view.findViewById(R.id.radio_friends_indicator)
        cardPrivate = view.findViewById(R.id.card_scope_private)
        radioPrivate = view.findViewById(R.id.radio_private_indicator)

        switchHideStartEnd = view.findViewById(R.id.switch_hide_start_end)
        switchHideBiometrics = view.findViewById(R.id.switch_hide_biometrics)
        btnSave = view.findViewById(R.id.btnSavePrivacy)
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_privacy", Context.MODE_PRIVATE)
        val savedScopeName = prefs.getString("scope", ActivityPrivacyScope.FRIENDS_ONLY.name)
        currentScope = try {
            ActivityPrivacyScope.valueOf(savedScopeName ?: ActivityPrivacyScope.FRIENDS_ONLY.name)
        } catch (_: Exception) {
            ActivityPrivacyScope.FRIENDS_ONLY
        }
        switchHideStartEnd.isChecked = prefs.getBoolean("hide_start_end", true)
        switchHideBiometrics.isChecked = prefs.getBoolean("hide_biometrics", false)
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        cardPublic.setOnClickListener {
            currentScope = ActivityPrivacyScope.PUBLIC
            updateRadioUI()
        }
        cardFriends.setOnClickListener {
            currentScope = ActivityPrivacyScope.FRIENDS_ONLY
            updateRadioUI()
        }
        cardPrivate.setOnClickListener {
            currentScope = ActivityPrivacyScope.ONLY_ME
            updateRadioUI()
        }

        btnSave.setOnClickListener {
            savePreferences()
        }
    }

    private fun updateRadioUI() {
        val context = context ?: return
        val activeColor = ContextCompat.getColor(context, R.color.kinetic_orange)
        val dimColor = ContextCompat.getColor(context, R.color.hud_text_dim)

        radioPublic.text = if (currentScope == ActivityPrivacyScope.PUBLIC) "●" else "○"
        radioPublic.setTextColor(if (currentScope == ActivityPrivacyScope.PUBLIC) activeColor else dimColor)

        radioFriends.text = if (currentScope == ActivityPrivacyScope.FRIENDS_ONLY) "●" else "○"
        radioFriends.setTextColor(if (currentScope == ActivityPrivacyScope.FRIENDS_ONLY) activeColor else dimColor)

        radioPrivate.text = if (currentScope == ActivityPrivacyScope.ONLY_ME) "●" else "○"
        radioPrivate.setTextColor(if (currentScope == ActivityPrivacyScope.ONLY_ME) activeColor else dimColor)
    }

    private fun savePreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_privacy", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("scope", currentScope.name)
            .putBoolean("hide_start_end", switchHideStartEnd.isChecked)
            .putBoolean("hide_biometrics", switchHideBiometrics.isChecked)
            .apply()

        Toast.makeText(requireContext(), "Đã lưu thiết lập quyền riêng tư thành công! ✓", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}
