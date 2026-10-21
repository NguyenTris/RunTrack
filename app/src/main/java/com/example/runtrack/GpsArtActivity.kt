package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class GpsArtActivity : AppCompatActivity() {

    private lateinit var rvSuggestions: RecyclerView
    private lateinit var suggestionsAdapter: ArtSuggestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_gps_art)

        val root = findViewById<android.view.View>(R.id.root_layout_gps_art) ?: findViewById(R.id.root_layout_gps_art_land)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        setupBottomNavigation()
    }

    private fun initViews() {
        rvSuggestions = findViewById(R.id.rv_art_suggestions)
        rvSuggestions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val sampleSuggestions = listOf(
            ArtSuggestion("1", "Khủng Long", "6.85 KM", R.drawable.ic_dinosaur),
            ArtSuggestion("2", "Trái Tim Hồ Gươm", "5.20 KM", R.drawable.ic_heart),
            ArtSuggestion("3", "Sét Đánh", "4.15 KM", R.drawable.ic_thunderbolt),
            ArtSuggestion("4", "Biểu Tượng Ngôi Sao", "7.30 KM", R.drawable.ic_target)
        )

        suggestionsAdapter = ArtSuggestionsAdapter(sampleSuggestions) { item ->
            Toast.makeText(this, "Đã chọn: ${item.name}", Toast.LENGTH_SHORT).show()
        }
        rvSuggestions.adapter = suggestionsAdapter

        val tvBtnHome = findViewById<android.widget.TextView>(R.id.tv_btn_home)
        tvBtnHome?.setOnClickListener { finish() }

        val btnClose = findViewById<ImageView>(R.id.btn_close_gps_art)
        btnClose?.setOnClickListener { finish() }

        val btnStart = findViewById<MaterialButton>(R.id.btn_start_run_art)
        btnStart?.setOnClickListener {
            val intent = Intent(this, TrackingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = R.id.nav_tracking
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_activity -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_tracking -> true
                R.id.nav_leaderboard -> {
                    val intent = Intent(this, LeaderboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
