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
import com.example.runtrack.network.UserProfileManager
import com.example.runtrack.network.WeatherManager
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

        // Tải dữ liệu phục hồi & thời tiết
        loadRecoveryAndProfile()
        loadCurrentWeather()
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
            startActivity(Intent(this, HudSettingsActivity::class.java))
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
}
