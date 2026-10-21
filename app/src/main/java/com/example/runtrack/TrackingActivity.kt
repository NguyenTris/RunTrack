package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TrackingActivity : AppCompatActivity() {

    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tracking)

        val root = findViewById<View>(R.id.root_layout_tracking) ?: findViewById(R.id.root_layout_tracking_land)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
    }

    private fun initViews() {
        val btnBack = findViewById<ImageView>(R.id.btn_back_tracking)
        btnBack?.setOnClickListener { finish() }

        val btnPause = findViewById<MaterialButton>(R.id.btn_pause_run)
        btnPause?.setOnClickListener {
            isPaused = !isPaused
            if (isPaused) {
                btnPause.text = "TIẾP TỤC"
                Toast.makeText(this, "Tạm dừng buổi chạy", Toast.LENGTH_SHORT).show()
            } else {
                btnPause.text = "TẠM DỪNG"
                Toast.makeText(this, "Tiếp tục chạy...", Toast.LENGTH_SHORT).show()
            }
        }

        val btnStop = findViewById<MaterialCardView>(R.id.btn_hold_stop)
        btnStop?.setOnClickListener {
            val intent = Intent(this, RunSummaryActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnGpsArt = findViewById<View>(R.id.btn_gps_art)
        btnGpsArt?.setOnClickListener {
            startActivity(Intent(this, GpsArtActivity::class.java))
        }
    }
}
