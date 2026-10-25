package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.runtrack.chat.ChatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_run_tracking)

        val root = findViewById<View>(R.id.root_layout)
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Header click listeners
        findViewById<View>(R.id.tv_header_status)?.setOnClickListener {
            startActivity(Intent(this, NotificationCenterActivity::class.java))
        }

        findViewById<View>(R.id.tv_header_title)?.setOnClickListener {
            startActivity(Intent(this, HudSettingsActivity::class.java))
        }

        // Action Buttons
        findViewById<Button>(R.id.btn_primary_login)?.apply {
            text = "BẢNG CÀI ĐẶT & NGOẠI VI (HUD)"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, HudSettingsActivity::class.java))
            }
        }

        findViewById<Button>(R.id.btn_secondary_login)?.apply {
            text = "TRUNG TÂM THÔNG BÁO // ALERTS"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, NotificationCenterActivity::class.java))
            }
        }

        // Button to open Chat screen
        findViewById<Button>(R.id.btn_open_chat)?.apply {
            text = getString(R.string.btn_open_chat)
            setOnClickListener {
                val intent = Intent(this@MainActivity, ChatActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
