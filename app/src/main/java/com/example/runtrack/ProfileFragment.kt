package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.runtrack.network.ApiClient
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val openSettingsAction = {
            startActivity(Intent(requireContext(), HudSettingsActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_settings_hub)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.btn_settings_hub_item)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.layout_settings_container)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.layout_devices_container)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.card_hr_zones)?.setOnClickListener { openSettingsAction() }

        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            btnLogout.isEnabled = false
            performLogout()
        }

        val tvBtnHome = view.findViewById<TextView>(R.id.tv_btn_home)
        tvBtnHome?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun performLogout() {
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(appContext).logout()
                }
            } catch (_: Exception) {
                // Luôn xóa phiên đăng nhập cục bộ kể cả khi ngoại tuyến
            } finally {
                SessionManager.setLoggedIn(appContext, false)
                SessionManager.clearAuthToken(appContext)

                if (isAdded) {
                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finishAffinity()
                }
            }
        }
    }
}
