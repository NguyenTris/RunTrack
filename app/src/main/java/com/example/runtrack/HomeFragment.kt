package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    private lateinit var rvHotRoutes: RecyclerView
    private lateinit var hotRoutesAdapter: HotRoutesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Hot Routes RecyclerView
        rvHotRoutes = view.findViewById(R.id.rv_hot_routes)
        setupHotRoutesRecyclerView()
        setupBottomNavigation(view)

        val frameStartBtn = view.findViewById<android.view.View>(R.id.frame_start_btn)
        frameStartBtn?.setOnClickListener {
            val intent = Intent(requireContext(), GpsArtActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<android.view.View>(R.id.btn_open_chat)?.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.runtrack.chat.ChatActivity::class.java))
        }

        view.findViewById<android.view.View>(R.id.btn_search_header)?.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.runtrack.search.SearchActivity::class.java))
        }
    }

    private fun setupBottomNavigation(view: View) {
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(requireContext(), ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_leaderboard -> {
                    val intent = Intent(requireContext(), LeaderboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_activity -> true
                R.id.nav_tracking -> {
                    val intent = Intent(requireContext(), GpsArtActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupHotRoutesRecyclerView() {
        // EXPLICIT HORIZONTAL LINEAR LAYOUT MANAGER
        rvHotRoutes.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        val sampleRoutes = listOf(
            HotRoute(
                id = "1",
                title = "Vòng Hồ Gươm Trái Tim",
                subtitle = "Khởi hành từ Đinh Tiên Hoàng",
                distance = "5.2 KM",
                badgeTag = "GPS ART #TRENDING",
                runsCount = "1,420 chạy",
                imageResId = R.drawable.ic_map_placeholder
            ),
            HotRoute(
                id = "2",
                title = "Ven Hồ Tây Đón Gió",
                subtitle = "Ít ngã tư • Gió mát thoáng",
                distance = "15.8 KM",
                badgeTag = "SCENIC RUN",
                runsCount = "3,890 chạy",
                imageResId = R.drawable.ic_map_placeholder
            ),
            HotRoute(
                id = "3",
                title = "Cung Đường Công Viên Thống Nhất",
                subtitle = "Đường bằng phẳng, bóng mát",
                distance = "3.5 KM",
                badgeTag = "EASY RUN",
                runsCount = "850 chạy",
                imageResId = R.drawable.ic_map_placeholder
            )
        )

        hotRoutesAdapter = HotRoutesAdapter(sampleRoutes) { route ->
            Toast.makeText(requireContext(), "Đã chọn: ${route.title}", Toast.LENGTH_SHORT).show()
        }

        rvHotRoutes.adapter = hotRoutesAdapter
    }
}
