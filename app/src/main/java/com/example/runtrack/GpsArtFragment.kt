package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class GpsArtFragment : Fragment() {

    private lateinit var rvSuggestions: RecyclerView
    private lateinit var suggestionsAdapter: ArtSuggestionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gps_art, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupBottomNavigation(view)
    }

    private fun initViews(view: View) {
        rvSuggestions = view.findViewById(R.id.rv_art_suggestions)
        rvSuggestions.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val sampleSuggestions = listOf(
            ArtSuggestion("1", "Khủng Long", "6.85 KM", R.drawable.ic_dinosaur),
            ArtSuggestion("2", "Trái Tim Hồ Gươm", "5.20 KM", R.drawable.ic_heart),
            ArtSuggestion("3", "Sét Đánh", "4.15 KM", R.drawable.ic_thunderbolt),
            ArtSuggestion("4", "Biểu Tượng Ngôi Sao", "7.30 KM", R.drawable.ic_target)
        )

        suggestionsAdapter = ArtSuggestionsAdapter(sampleSuggestions) { item ->
            Toast.makeText(requireContext(), "Đã chọn: ${item.name}", Toast.LENGTH_SHORT).show()
        }
        rvSuggestions.adapter = suggestionsAdapter

        val tvBtnHome = view.findViewById<android.widget.TextView>(R.id.tv_btn_home)
        tvBtnHome?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val btnClose = view.findViewById<ImageView>(R.id.btn_close_gps_art)
        btnClose?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val btnStart = view.findViewById<MaterialButton>(R.id.btn_start_run_art)
        btnStart?.setOnClickListener {
            val intent = Intent(requireContext(), TrackingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation(view: View) {
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = R.id.nav_tracking
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_activity -> {
                    val intent = Intent(requireContext(), DashboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_tracking -> true
                R.id.nav_leaderboard -> {
                    val intent = Intent(requireContext(), LeaderboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(requireContext(), ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
