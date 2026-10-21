package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Open HUD Settings & Peripherals
        view.findViewById<View>(R.id.layout_settings_container)?.setOnClickListener {
            startActivity(Intent(requireContext(), HudSettingsActivity::class.java))
        }

        view.findViewById<View>(R.id.layout_devices_container)?.setOnClickListener {
            startActivity(Intent(requireContext(), HudSettingsActivity::class.java))
        }

        view.findViewById<View>(R.id.card_hr_zones)?.setOnClickListener {
            startActivity(Intent(requireContext(), HudSettingsActivity::class.java))
        }

        // Logout Button Handler
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            performLogout()
        }

        // Home Navigation Handler
        val tvBtnHome = view.findViewById<TextView>(R.id.tv_btn_home)
        tvBtnHome?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun performLogout() {
        val context = requireContext()
        // 1. Clear user session
        SessionManager.setLoggedIn(context, false)

        // 2. Navigate to LoginActivity with cleared task backstack
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        // 3. Finish host activity
        requireActivity().finishAffinity()
    }
}
