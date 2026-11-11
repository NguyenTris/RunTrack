package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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

        loadUserProfile(view)

        // Open HUD Settings & Peripherals
        val openSettingsAction = {
            startActivity(Intent(requireContext(), HudSettingsActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_settings_hub)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.btn_settings_hub_item)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.layout_settings_container)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.layout_devices_container)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.card_hr_zones)?.setOnClickListener { openSettingsAction() }

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

    private fun loadUserProfile(view: View) {
        val token = SessionManager.getToken(requireContext())
        if (token.isBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getUserProfile("Bearer $token")
                }
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    val tvName = view.findViewById<TextView>(R.id.tv_runner_name)
                    tvName?.text = profile.full_name
                }
            } catch (e: Exception) {
                // Non-blocking network catch
            }
        }
    }

    private fun performLogout() {
        val context = requireContext()
        SessionManager.clear(context)

        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finishAffinity()
    }
}
