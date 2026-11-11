package com.example.runtrack

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.bluetooth.BleHeartRateManager
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.UserProfileManager
import com.example.runtrack.network.UserProfileUpdate
import com.example.runtrack.network.WeatherManager
import android.graphics.Color
import android.widget.ImageView
import java.util.Calendar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DashboardActivity : AppCompatActivity() {

    private lateinit var rvHotRoutes: RecyclerView
    private lateinit var hotRoutesAdapter: HotRoutesAdapter

    // Header Metrics Views
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

    // Bluetooth BLE Heart Rate / HRV Manager
    private lateinit var bleHeartRateManager: BleHeartRateManager

    // Permission Launcher cho Bluetooth
    private val blePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startSmartwatchScan()
        } else {
            Toast.makeText(this, "Cần cấp quyền Bluetooth để kết nối Smartwatch", Toast.LENGTH_SHORT).show()
            tvHrvValue?.text = "Thiếu quyền BLE"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_home)

        val root = findViewById<View>(R.id.root_layout)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        setupHotRoutesRecyclerView()
        setupBottomNavigation()
        setupClickListeners()

        // Khởi tạo BLE Manager
        initBleManager()

        // Tải dữ liệu phục hồi, thời tiết & thống kê tuần
        loadRecoveryAndProfile()
        loadCurrentWeather()
        loadHomeStats()
    }

    private fun initViews() {
        rvHotRoutes = findViewById(R.id.rv_hot_routes)
        tvUserName = findViewById(R.id.tv_user_name)
        tvScorePercent = findViewById(R.id.tv_score_percent)
        tvScoreLabel = findViewById(R.id.tv_score_label)
        tvRecoveryValue = findViewById(R.id.tv_recovery_value)
        layoutHrvPill = findViewById(R.id.layout_hrv_pill)
        tvHrvValue = findViewById(R.id.tv_hrv_value)
        tvWeatherLocTemp = findViewById(R.id.tv_weather_loc_temp)
        tvWeatherHumidity = findViewById(R.id.tv_weather_humidity)

        ivEditWeeklyGoal = findViewById(R.id.iv_edit_weekly_goal)
        tvCurrentKm = findViewById(R.id.tv_current_km)
        tvTargetKm = findViewById(R.id.tv_target_km)
        progressWeeklyCircle = findViewById(R.id.progress_weekly_circle)
        tvCirclePercent = findViewById(R.id.tv_circle_percent)
        progressLinearKm = findViewById(R.id.progress_linear_km)
        tvRemainingKm = findViewById(R.id.tv_remaining_km)
    }

    private fun setupClickListeners() {
        val frameStartBtn = findViewById<View>(R.id.frame_start_btn)
        frameStartBtn?.setOnClickListener {
            startActivity(Intent(this, GpsArtActivity::class.java))
        }

        findViewById<View>(R.id.card_profile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<View>(R.id.card_weekly_goal)?.setOnClickListener {
            startActivity(Intent(this, WeeklyGoalDetailActivity::class.java))
        }

        ivEditWeeklyGoal?.setOnClickListener {
            showEditWeeklyGoalDialog()
        }

        findViewById<View>(R.id.btn_open_chat)?.setOnClickListener {
            startActivity(Intent(this, com.example.runtrack.chat.ChatActivity::class.java))
        }

        findViewById<View>(R.id.btn_search_header)?.setOnClickListener {
            startActivity(Intent(this, com.example.runtrack.search.SearchActivity::class.java))
        }

        val openClubListener = View.OnClickListener {
            startActivity(Intent(this, com.example.runtrack.club.ClubActivity::class.java))
        }
        findViewById<View>(R.id.card_my_club)?.setOnClickListener(openClubListener)
        findViewById<View>(R.id.tv_my_club_detail)?.setOnClickListener(openClubListener)
        findViewById<View>(R.id.btn_enter_my_club)?.setOnClickListener(openClubListener)

        // Bắt sự kiện chạm vào ô HRV để quét Smartwatch
        layoutHrvPill?.setOnClickListener {
            checkAndRequestBlePermissions()
        }

        // Nhấn giữ ô HRV để kích hoạt dữ liệu mẫu (cho mục đích test/demo nhanh)
        layoutHrvPill?.setOnLongClickListener {
            tvHrvValue?.text = "64 ms • Tốt"
            Toast.makeText(this, "Đã bật chế độ HRV demo: 64 ms (Tốt)", Toast.LENGTH_SHORT).show()
            true
        }
    }

    // ========================================================
    // 1. LOGIC PHỤC HỒI & READINESS (24 GIỜ LÀ 100%)
    // ========================================================
    private fun loadRecoveryAndProfile() {
        UserProfileManager.loadProfileAndRecovery(this) { profile ->
            runOnUiThread {
                tvUserName?.text = "Chào ${profile.fullName} 👋"
                tvScorePercent?.text = "${profile.recoveryPercentage}%"
                tvScoreLabel?.text = profile.recoveryLabel
                tvRecoveryValue?.text = profile.displayText

                // Quy tắc màu sắc: >85% Xanh, >60% Vàng, <=60% Đỏ
                val colorResId = when {
                    profile.recoveryPercentage > 85 -> R.color.neon_green
                    profile.recoveryPercentage > 60 -> R.color.accent_yellow
                    else -> R.color.hr_zone_5
                }
                val color = ContextCompat.getColor(this, colorResId)
                tvScorePercent?.setTextColor(color)
                tvScoreLabel?.setTextColor(color)
            }
        }
    }

    // ========================================================
    // 2. LOGIC SMARTWATCH BLUETOOTH (HRV RMSSD)
    // ========================================================
    private fun initBleManager() {
        bleHeartRateManager = BleHeartRateManager(this)
        bleHeartRateManager.setHrvListener(object : BleHeartRateManager.HrvListener {
            override fun onStateChanged(state: BleHeartRateManager.ConnectionState, message: String) {
                runOnUiThread {
                    when (state) {
                        BleHeartRateManager.ConnectionState.SCANNING -> tvHrvValue?.text = "Đang quét..."
                        BleHeartRateManager.ConnectionState.CONNECTING -> tvHrvValue?.text = "Đang kết nối..."
                        BleHeartRateManager.ConnectionState.DISCONNECTED -> tvHrvValue?.text = "Chưa kết nối"
                        BleHeartRateManager.ConnectionState.CONNECTED -> {
                            // Giữ nguyên hoặc đợi dữ liệu
                        }
                    }
                }
            }

            override fun onHrvDataReceived(hrvMs: Int, statusLabel: String, heartRate: Int) {
                runOnUiThread {
                    tvHrvValue?.text = "$hrvMs ms • $statusLabel"
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(this@DashboardActivity, error, Toast.LENGTH_SHORT).show()
                    tvHrvValue?.text = "Chưa kết nối"
                }
            }
        })
    }

    private fun checkAndRequestBlePermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            blePermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            startSmartwatchScan()
        }
    }

    private fun startSmartwatchScan() {
        if (!bleHeartRateManager.isBluetoothEnabled()) {
            Toast.makeText(this, "Vui lòng bật Bluetooth trên điện thoại!", Toast.LENGTH_LONG).show()
            tvHrvValue?.text = "Bluetooth tắt"
            return
        }
        Toast.makeText(this, "Đang quét tìm Smartwatch BLE...", Toast.LENGTH_SHORT).show()
        bleHeartRateManager.startScan()
    }

    // ========================================================
    // 3. LOGIC THỜI TIẾT REALTIME (AZURE VPS / OPENWEATHERMAP)
    // ========================================================
    private fun loadCurrentWeather() {
        var lat = 21.0285 // Mặc định Hà Nội
        var lon = 105.8542

        // Thử lấy vị trí GPS thực tế nếu đã cấp quyền
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastKnown = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                lastKnown?.let {
                    lat = it.latitude
                    lon = it.longitude
                }
            }
        } catch (e: Exception) {
            // Giữ tọa độ mặc định
        }

        WeatherManager.fetchWeather(
            lat = lat,
            lon = lon,
            onSuccess = { info ->
                runOnUiThread {
                    // Cập nhật Vị trí & Nhiệt độ
                    val formattedCity = info.city.uppercase()
                    val tempInt = info.temperature.roundToInt()
                    tvWeatherLocTemp?.text = "$formattedCity • $tempInt°C"

                    // Cập nhật Độ ẩm & AQI
                    tvWeatherHumidity?.text = "ẨM ${info.humidity}% • AQI ${info.aqi}"

                    // Đổi Icon thời tiết theo trạng thái
                    val iconDrawableRes = when (info.weatherMain.lowercase()) {
                        "rain", "drizzle", "thunderstorm" -> R.drawable.ic_weather_rain
                        "clear" -> R.drawable.ic_weather_sun
                        else -> R.drawable.ic_weather_cloud
                    }
                    tvWeatherLocTemp?.setCompoundDrawablesWithIntrinsicBounds(iconDrawableRes, 0, 0, 0)
                }
            },
            onError = { errorMsg ->
                runOnUiThread {
                    tvWeatherLocTemp?.text = "HÀ NỘI • 24°C"
                    tvWeatherHumidity?.text = "ẨM 68% • AQI 32"
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::bleHeartRateManager.isInitialized) {
            bleHeartRateManager.disconnect()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_activity -> {
                    startActivity(Intent(this, ProgressActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_leaderboard -> {
                    startActivity(Intent(this, LeaderboardActivity::class.java))
                    true
                }
                R.id.nav_tracking -> {
                    startActivity(Intent(this, GpsArtActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupHotRoutesRecyclerView() {
        rvHotRoutes.layoutManager = LinearLayoutManager(
            this,
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
            Toast.makeText(this, "Đã chọn: ${route.title}", Toast.LENGTH_SHORT).show()
        }

        rvHotRoutes.adapter = hotRoutesAdapter
    }

    // ========================================================
    // 3. LOGIC MỤC TIÊU TUẦN (WEEKLY GOAL) & ĐỒNG BỘ VPS
    // ========================================================
    private fun loadHomeStats() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.getAuthenticatedService(this@DashboardActivity).getHomeStats()
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    runOnUiThread {
                        tvCurrentKm?.text = "%.1f".format(stats.total_distance_km)
                        tvTargetKm?.text = "/ %.1f KM".format(stats.weekly_goal_km)
                        progressWeeklyCircle?.progress = stats.completion_percentage
                        tvCirclePercent?.text = "${stats.completion_percentage}%"
                        progressLinearKm?.progress = stats.completion_percentage
                        val remaining = Math.max(0f, stats.weekly_goal_km - stats.total_distance_km)
                        tvRemainingKm?.text = "Còn lại: %.1f km".format(remaining)

                        updateWeeklyDaysStatus(stats.active_days)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardActivity", "Lỗi tải home-stats: ${e.message}")
            }
        }
    }

    private fun updateWeeklyDaysStatus(activeDays: List<Int>?) {
        val rawDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayDayOfWeek = if (rawDay == Calendar.SUNDAY) 8 else rawDay

        val daysConfig = listOf(
            2 to Triple(findViewById<View>(R.id.layout_day_home_t2), findViewById<TextView>(R.id.tv_home_day_status_t2), findViewById<ImageView>(R.id.iv_home_day_icon_t2)),
            3 to Triple(findViewById<View>(R.id.layout_day_home_t3), findViewById<TextView>(R.id.tv_home_day_status_t3), findViewById<ImageView>(R.id.iv_home_day_icon_t3)),
            4 to Triple(findViewById<View>(R.id.layout_day_home_t4), findViewById<TextView>(R.id.tv_home_day_status_t4), findViewById<ImageView>(R.id.iv_home_day_icon_t4)),
            5 to Triple(findViewById<View>(R.id.layout_day_home_t5), findViewById<TextView>(R.id.tv_home_day_status_t5), findViewById<ImageView>(R.id.iv_home_day_icon_t5)),
            6 to Triple(findViewById<View>(R.id.layout_day_home_t6), findViewById<TextView>(R.id.tv_home_day_status_t6), findViewById<ImageView>(R.id.iv_home_day_icon_t6)),
            7 to Triple(findViewById<View>(R.id.layout_day_home_t7), findViewById<TextView>(R.id.tv_home_day_status_t7), findViewById<ImageView>(R.id.iv_home_day_icon_t7)),
            8 to Triple(findViewById<View>(R.id.layout_day_home_cn), findViewById<TextView>(R.id.tv_home_day_status_cn), findViewById<ImageView>(R.id.iv_home_day_icon_cn))
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
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Nhập mục tiêu km (VD: 40.0)"
            val currentText = tvTargetKm?.text?.toString() ?: ""
            val numericStr = currentText.replace("/", "").replace("KM", "").trim()
            if (numericStr.isNotEmpty()) {
                setText(numericStr)
            }
            setSelection(text.length)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cập Nhật Mục Tiêu Tuần")
            .setView(android.widget.FrameLayout(this).apply {
                setPadding(50, 20, 50, 0)
                addView(input)
            })
            .setPositiveButton("Lưu") { dialog, _ ->
                val str = input.text.toString().trim()
                val newGoal = str.toDoubleOrNull()
                if (newGoal == null || newGoal <= 0.0) {
                    Toast.makeText(this, "Vui lòng nhập số km hợp lệ lớn hơn 0", Toast.LENGTH_SHORT).show()
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
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val requestBody = UserProfileUpdate(weekly_goal_km = newGoalKm)
                val response = ApiClient.getAuthenticatedService(this@DashboardActivity).updateUserProfile(requestBody)
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@DashboardActivity, "Cập nhật mục tiêu tuần thành công!", Toast.LENGTH_SHORT).show()
                        loadHomeStats()
                        loadRecoveryAndProfile()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@DashboardActivity, "Lỗi cập nhật từ server: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@DashboardActivity, "Không thể kết nối VPS: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
