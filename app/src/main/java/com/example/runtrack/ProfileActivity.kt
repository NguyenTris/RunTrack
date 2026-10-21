package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_profile)

        val root = findViewById<android.view.View>(R.id.root_layout_profile) ?: findViewById(R.id.root_layout_profile_land)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Open HUD Settings & Peripherals
        findViewById<android.view.View>(R.id.layout_settings_container)?.setOnClickListener {
            startActivity(Intent(this, HudSettingsActivity::class.java))
        }

        findViewById<android.view.View>(R.id.layout_devices_container)?.setOnClickListener {
            startActivity(Intent(this, HudSettingsActivity::class.java))
        }

        findViewById<android.view.View>(R.id.card_hr_zones)?.setOnClickListener {
            startActivity(Intent(this, HudSettingsActivity::class.java))
        }

        // Logout Button Handler
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            performLogout()
        }

        // Back / Home Handler
        val tvBtnHome = findViewById<TextView>(R.id.tv_btn_home)
        tvBtnHome?.setOnClickListener {
            finish()
        }
    }

    private fun performLogout() {
        // 1. Clear user session
        SessionManager.setLoggedIn(this, false)

        // 2. Navigate to LoginActivity with cleared task backstack
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        // 3. Finish activity and clear backstack
        finishAffinity()
    }
}
