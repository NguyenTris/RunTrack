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
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.UserProfileManager
import com.example.runtrack.network.UserProfileUpdate
import com.example.runtrack.network.WeatherManager
import android.graphics.Color
import android.widget.ImageView
import java.util.Calendar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    // Weekly Goal Views
    private var ivEditWeeklyGoal: View? = null
    private var tvCurrentKm: TextView? = null
    private var tvTargetKm: TextView? = null
    private var progressWeeklyCircle: com.google.android.material.progressindicator.CircularProgressIndicator? = null
    private var tvCirclePercent: TextView? = null
    private var progressLinearKm: com.google.android.material.progressindicator.LinearProgressIndicator? = null
    private var tvRemainingKm: TextView? = null

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
        loadHomeStats()
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

        ivEditWeeklyGoal = view.findViewById(R.id.iv_edit_weekly_goal)
        tvCurrentKm = view.findViewById(R.id.tv_current_km)
        tvTargetKm = view.findViewById(R.id.tv_target_km)
        progressWeeklyCircle = view.findViewById(R.id.progress_weekly_circle)
        tvCirclePercent = view.findViewById(R.id.tv_circle_percent)
        progressLinearKm = view.findViewById(R.id.progress_linear_km)
        tvRemainingKm = view.findViewById(R.id.tv_remaining_km)
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
            startActivity(Intent(requireContext(), WeeklyGoalDetailActivity::class.java))
        }

        ivEditWeeklyGoal?.setOnClickListener {
            showEditWeeklyGoalDialog()
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
        var lat = 21.0285 // Mặc định Hà Nội
        var lon = 105.8542

        // Thử lấy vị trí GPS thực tế nếu đã cấp quyền
        try {
            val ctx = context
            if (ctx != null && (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED)) {
                val locationManager = ctx.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
                val lastKnown = locationManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                    ?: locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                lastKnown?.let {
                    lat = it.latitude
                    lon = it.longitude
                }
            }
        } catch (_: Exception) {
            // Giữ tọa độ mặc định
        }

        WeatherManager.fetchWeather(
            lat = lat,
            lon = lon,
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

    private fun loadHomeStats() {
        val ctx = context ?: return
        val v = view ?: return
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.getAuthenticatedService(ctx).getHomeStats()
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    activity?.runOnUiThread {
                        tvCurrentKm?.text = "%.1f".format(stats.total_distance_km)
                        tvTargetKm?.text = "/ %.1f KM".format(stats.weekly_goal_km)
                        progressWeeklyCircle?.progress = stats.completion_percentage
                        tvCirclePercent?.text = "${stats.completion_percentage}%"
                        progressLinearKm?.progress = stats.completion_percentage
                        val remaining = Math.max(0f, stats.weekly_goal_km - stats.total_distance_km)
                        tvRemainingKm?.text = "Còn lại: %.1f km".format(remaining)

                        updateWeeklyDaysStatus(v, stats.active_days)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Lỗi tải home-stats: ${e.message}")
            }
        }
    }

    private fun updateWeeklyDaysStatus(rootView: View, activeDays: List<Int>?) {
        val rawDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayDayOfWeek = if (rawDay == Calendar.SUNDAY) 8 else rawDay

        val daysConfig = listOf(
            2 to Triple(rootView.findViewById<View>(R.id.layout_day_home_t2), rootView.findViewById<TextView>(R.id.tv_home_day_status_t2), rootView.findViewById<ImageView>(R.id.iv_home_day_icon_t2)),
            3 to Triple(rootView.findViewById<View>(R.id.layout_day_home_t3), rootView.findViewById<TextView>(R.id.tv_home_day_status_t3), rootView.findViewById<ImageView>(R.id.iv_home_day_icon_t3)),
            4 to Triple(rootView.findViewById<View>(R.id.layout_day_home_t4), rootView.findViewById<TextView>(R.id.tv_home_day_status_t4), rootView.findViewById<ImageView>(R.id.iv_home_day_icon_t4)),
            5 to Triple(rootView.findViewById<View>(R.id.layout_day_home_t5), rootView.findViewById<TextView>(R.id.tv_home_day_status_t5), rootView.findViewById<ImageView>(R.id.iv_home_day_icon_t5)),
            6 to Triple(rootView.findViewById<View>(R.id.layout_day_home_t6), rootView.findViewById<TextView>(R.id.tv_home_day_status_t6), rootView.findViewById<ImageView>(R.id.iv_home_day_icon_t6)),
            7 to Triple(rootView.findViewById<View>(R.id.layout_day_home_t7), rootView.findViewById<TextView>(R.id.tv_home_day_status_t7), rootView.findViewById<ImageView>(R.id.iv_home_day_icon_t7)),
            8 to Triple(rootView.findViewById<View>(R.id.layout_day_home_cn), rootView.findViewById<TextView>(R.id.tv_home_day_status_cn), rootView.findViewById<ImageView>(R.id.iv_home_day_icon_cn))
        )

        for ((dayVal, views) in daysConfig) {
            val container = views.first
            val tvStatus = views.second
            val ivIcon = views.third

            val hasRun = activeDays?.contains(dayVal) == true
            val isToday = (dayVal == todayDayOfWeek)

            when {
                hasRun -> {
                    container?.setBackgroundColor(Color.parseColor("#FF5722")) // Cam
                    tvStatus?.text = ""
                    tvStatus?.visibility = View.GONE
                    ivIcon?.visibility = View.VISIBLE
                    ivIcon?.setColorFilter(Color.WHITE)
                }
                isToday -> {
                    container?.setBackgroundColor(Color.parseColor("#004D61")) // Xanh đậm
                    tvStatus?.text = "HÔM NAY"
                    tvStatus?.textSize = 9f
                    tvStatus?.setTextColor(Color.parseColor("#00E5FF"))
                    tvStatus?.visibility = View.VISIBLE
                    ivIcon?.visibility = View.GONE
                }
                else -> {
                    container?.setBackgroundColor(Color.parseColor("#1F242D")) // Xám chìm
                    tvStatus?.text = "—"
                    tvStatus?.textSize = 11f
                    tvStatus?.setTextColor(Color.parseColor("#5A6678"))
                    tvStatus?.visibility = View.VISIBLE
                    ivIcon?.visibility = View.GONE
                }
            }
        }
    }

    private fun showEditWeeklyGoalDialog() {
        val ctx = context ?: return
        val input = android.widget.EditText(ctx).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Nhập mục tiêu km (VD: 40.0)"
            val currentText = tvTargetKm?.text?.toString() ?: ""
            val numericStr = currentText.replace("/", "").replace("KM", "").trim()
            if (numericStr.isNotEmpty()) {
                setText(numericStr)
            }
            setSelection(text.length)
        }

        androidx.appcompat.app.AlertDialog.Builder(ctx)
            .setTitle("Cập Nhật Mục Tiêu Tuần")
            .setView(android.widget.FrameLayout(ctx).apply {
                setPadding(50, 20, 50, 0)
                addView(input)
            })
            .setPositiveButton("Lưu") { dialog, _ ->
                val str = input.text.toString().trim()
                val newGoal = str.toDoubleOrNull()
                if (newGoal == null || newGoal <= 0.0) {
                    Toast.makeText(ctx, "Vui lòng nhập số km hợp lệ lớn hơn 0", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                updateWeeklyGoalOnServer(newGoal)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun updateWeeklyGoalOnServer(newGoalKm: Double) {
        val ctx = context ?: return
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val requestBody = UserProfileUpdate(weekly_goal_km = newGoalKm)
                val response = ApiClient.getAuthenticatedService(ctx).updateUserProfile(requestBody)
                if (response.isSuccessful) {
                    activity?.runOnUiThread {
                        Toast.makeText(ctx, "Cập nhật mục tiêu tuần thành công!", Toast.LENGTH_SHORT).show()
                        loadHomeStats()
                        loadRecoveryAndProfile()
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(ctx, "Lỗi cập nhật từ server: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(ctx, "Không thể kết nối VPS: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
