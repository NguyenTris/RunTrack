package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.network.UserProfileManager
import com.example.runtrack.network.WeatherManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private lateinit var rvHotRoutes: RecyclerView
    private lateinit var hotRoutesAdapter: HotRoutesAdapter

    // Metric views
    private var tvUserName: TextView? = null
    private var tvScorePercent: TextView? = null
    private var tvScoreLabel: TextView? = null
    private var tvRecoveryValue: TextView? = null
    private var layoutHrvPill: View? = null
    private var tvHrvValue: TextView? = null
    private var tvWeatherLocTemp: TextView? = null
    private var tvWeatherHumidity: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupHotRoutesRecyclerView()
        setupBottomNavigation(view)
        setupClickListeners(view)

        loadRecoveryAndProfile()
        loadCurrentWeather()
    }

    private fun initViews(view: View) {
        rvHotRoutes = view.findViewById(R.id.rv_hot_routes)
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvScorePercent = view.findViewById(R.id.tv_score_percent)
        tvScoreLabel = view.findViewById(R.id.tv_score_label)
        tvRecoveryValue = view.findViewById(R.id.tv_recovery_value)
        layoutHrvPill = view.findViewById(R.id.layout_hrv_pill)
        tvHrvValue = view.findViewById(R.id.tv_hrv_value)
        tvWeatherLocTemp = view.findViewById(R.id.tv_weather_loc_temp)
        tvWeatherHumidity = view.findViewById(R.id.tv_weather_humidity)
    }

    private fun setupClickListeners(view: View) {
        val frameStartBtn = view.findViewById<View>(R.id.frame_start_btn)
        frameStartBtn?.setOnClickListener {
            startActivity(Intent(requireContext(), GpsArtActivity::class.java))
        }

        view.findViewById<View>(R.id.card_profile)?.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        view.findViewById<View>(R.id.card_weekly_goal)?.setOnClickListener {
            startActivity(Intent(requireContext(), HudSettingsActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_open_chat)?.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.runtrack.chat.ChatActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_search_header)?.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.runtrack.search.SearchActivity::class.java))
        }

        val openClubListener = View.OnClickListener {
            startActivity(Intent(requireContext(), com.example.runtrack.club.ClubActivity::class.java))
        }
        view.findViewById<View>(R.id.card_my_club)?.setOnClickListener(openClubListener)
        view.findViewById<View>(R.id.tv_my_club_detail)?.setOnClickListener(openClubListener)
        view.findViewById<View>(R.id.btn_enter_my_club)?.setOnClickListener(openClubListener)

        layoutHrvPill?.setOnClickListener {
            val activity = requireActivity()
            if (activity is DashboardActivity) {
                // Đã xử lý ở cấp Activity nếu đang chạy DashboardActivity
            } else {
                tvHrvValue?.text = "64 ms • Tốt"
                Toast.makeText(requireContext(), "Chạm để kích hoạt quét Smartwatch", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadRecoveryAndProfile() {
        context?.let { ctx ->
            UserProfileManager.loadProfileAndRecovery(ctx) { profile ->
                activity?.runOnUiThread {
                    tvUserName?.text = "Chào ${profile.fullName} 👋"
                    tvScorePercent?.text = "${profile.recoveryPercentage}%"
                    tvScoreLabel?.text = profile.recoveryLabel
                    tvRecoveryValue?.text = profile.displayText

                    val colorResId = when {
                        profile.recoveryPercentage > 85 -> R.color.neon_green
                        profile.recoveryPercentage > 60 -> R.color.accent_yellow
                        else -> R.color.hr_zone_5
                    }
                    val color = ContextCompat.getColor(ctx, colorResId)
                    tvScorePercent?.setTextColor(color)
                    tvScoreLabel?.setTextColor(color)
                }
            }
        }
    }

    private fun loadCurrentWeather() {
        WeatherManager.fetchWeather(
            lat = 21.0285,
            lon = 105.8542,
            onSuccess = { info ->
                activity?.runOnUiThread {
                    val formattedCity = info.city.uppercase()
                    val tempInt = info.temperature.roundToInt()
                    tvWeatherLocTemp?.text = "$formattedCity • $tempInt°C"
                    tvWeatherHumidity?.text = "ẨM ${info.humidity}% • AQI ${info.aqi}"

                    val iconDrawableRes = when (info.weatherMain.lowercase()) {
                        "rain", "drizzle", "thunderstorm" -> R.drawable.ic_weather_rain
                        "clear" -> R.drawable.ic_weather_sun
                        else -> R.drawable.ic_weather_cloud
                    }
                    tvWeatherLocTemp?.setCompoundDrawablesWithIntrinsicBounds(iconDrawableRes, 0, 0, 0)
                }
            },
            onError = {
                activity?.runOnUiThread {
                    tvWeatherLocTemp?.text = "HÀ NỘI • 24°C"
                    tvWeatherHumidity?.text = "ẨM 68% • AQI 32"
                }
            }
        )
    }

    private fun setupBottomNavigation(view: View) {
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(requireContext(), ProfileActivity::class.java))
                    true
                }
                R.id.nav_leaderboard -> {
                    startActivity(Intent(requireContext(), LeaderboardActivity::class.java))
                    true
                }
                R.id.nav_activity -> {
                    startActivity(Intent(requireContext(), ProgressActivity::class.java))
                    false
                }
                R.id.nav_tracking -> {
                    startActivity(Intent(requireContext(), GpsArtActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupHotRoutesRecyclerView() {
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
