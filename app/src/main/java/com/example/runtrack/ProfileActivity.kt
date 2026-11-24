package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.runtrack.network.ApiClient
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_profile)

        val root = findViewById<android.view.View>(R.id.root_layout_profile)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Open HUD Settings & Peripherals
        val openSettingsAction = {
            startActivity(Intent(this, HudSettingsActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btn_settings_hub)?.setOnClickListener { openSettingsAction() }
        findViewById<android.view.View>(R.id.btn_settings_hub_item)?.setOnClickListener { openSettingsAction() }
        findViewById<android.view.View>(R.id.layout_settings_container)?.setOnClickListener { openSettingsAction() }
        findViewById<android.view.View>(R.id.layout_devices_container)?.setOnClickListener { openSettingsAction() }
        findViewById<android.view.View>(R.id.card_hr_zones)?.setOnClickListener { openSettingsAction() }

        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            btnLogout.isEnabled = false
            performLogout()
        }

        val tvBtnHome = findViewById<TextView>(R.id.tv_btn_home)
        tvBtnHome?.setOnClickListener {
            finish()
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@ProfileActivity).logout()
                }
            } catch (_: Exception) {
                // Luôn xóa phiên đăng nhập cục bộ kể cả khi ngoại tuyến
            } finally {
                SessionManager.setLoggedIn(this@ProfileActivity, false)
                SessionManager.clearAuthToken(this@ProfileActivity)

                val intent = Intent(this@ProfileActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finishAffinity()
            }
        }
    }
}
