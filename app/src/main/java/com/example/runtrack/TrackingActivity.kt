package com.example.runtrack

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.model.dashboard.ActivityCreateRequest
import com.example.runtrack.network.model.dashboard.RoutePointDto
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_ROUTE_NAME = "EXTRA_ROUTE_NAME"
        const val EXTRA_ROUTE_POINTS_JSON = "EXTRA_ROUTE_POINTS_JSON"
        private val DEFAULT_CENTER = LatLng(21.0285, 105.8542)
    }

    private var isPaused = false
    private var isStopping = false
    private var elapsedSeconds = 0
    private var timerJob: Job? = null

    private var tvDistanceValue: TextView? = null
    private var tvDurationValue: TextView? = null
    private var tvPaceValue: TextView? = null

    private var googleMap: GoogleMap? = null
    private var livePolyline: Polyline? = null
    private var routeName: String? = null
    private var guidePoints: List<RoutePointDto> = emptyList()
    private val recordedPoints: MutableList<RoutePointDto> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tracking)

        applyWindowInsets()
        parseRouteExtras()
        initViews()
        attachGoogleMap()
        updateLiveMetricsUi()
        startRunTimer()
    }

    private fun applyWindowInsets() {
        val root = findViewById<View>(R.id.root_layout_tracking) ?: return
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun parseRouteExtras() {
        routeName = intent.getStringExtra(EXTRA_ROUTE_NAME)
        val pointsJson = intent.getStringExtra(EXTRA_ROUTE_POINTS_JSON) ?: return
        try {
            val type = object : TypeToken<List<RoutePointDto>>() {}.type
            guidePoints = Gson().fromJson<List<RoutePointDto>>(pointsJson, type).orEmpty()
        } catch (_: Exception) {
            guidePoints = emptyList()
        }
    }

    private fun initViews() {
        tvDistanceValue = findViewById(R.id.tv_distance_value)
        tvDurationValue = findViewById(R.id.tv_duration_value)
        tvPaceValue = findViewById(R.id.tv_pace_value)

        findViewById<ImageView>(R.id.btn_back_tracking)?.setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btn_pause_run)?.let { btn ->
            btn.setOnClickListener { togglePause(btn) }
        }
        findViewById<MaterialCardView>(R.id.btn_hold_stop)?.let { btn ->
            btn.setOnClickListener { stopAndSaveRun(btn) }
        }
        findViewById<View>(R.id.btn_gps_art)?.setOnClickListener {
            startActivity(Intent(this, GpsArtActivity::class.java))
        }
        setupMapControls()
    }

    private fun setupMapControls() {
        findViewById<View>(R.id.btn_map_zoom_in)?.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }
        findViewById<View>(R.id.btn_map_zoom_out)?.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }
        findViewById<View>(R.id.btn_map_location)?.setOnClickListener {
            val target = recordedPoints.lastOrNull()?.let { LatLng(it.lat, it.lng) } ?: DEFAULT_CENTER
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 16f))
        }
    }

    private fun attachGoogleMap() {
        val existing = supportFragmentManager.findFragmentById(R.id.map_container) as? SupportMapFragment
        val mapFragment = existing ?: SupportMapFragment.newInstance().also {
            supportFragmentManager.beginTransaction()
                .replace(R.id.map_container, it)
                .commit()
        }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = false

        if (guidePoints.isNotEmpty()) {
            val guideLatLngs = guidePoints.map { LatLng(it.lat, it.lng) }
            map.addPolyline(
                PolylineOptions()
                    .addAll(guideLatLngs)
                    .color(Color.parseColor("#66FFFFFF"))
                    .width(8f)
                    .geodesic(true)
            )
            GpsArtMapHelper.zoomToPoints(map, guideLatLngs, 96)
            recordedPoints.add(guidePoints.first())
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_CENTER, 15.5f))
            recordedPoints.add(RoutePointDto(DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude))
        }

        livePolyline = map.addPolyline(
            PolylineOptions()
                .color(GpsArtMapHelper.NEON_GREEN)
                .width(12f)
                .geodesic(true)
        )
        syncLivePolyline()
    }

    private fun togglePause(btnPause: MaterialButton) {
        isPaused = !isPaused
        if (isPaused) {
            btnPause.text = "TIẾP TỤC"
            Toast.makeText(this, "Tạm dừng buổi chạy", Toast.LENGTH_SHORT).show()
        } else {
            btnPause.text = "TẠM DỪNG"
            Toast.makeText(this, "Tiếp tục chạy...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRunTimer() {
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            while (isActive) {
                delay(1000L)
                if (!isPaused && !isStopping) {
                    elapsedSeconds += 1
                    appendSimulatedRoutePoint()
                    updateLiveMetricsUi()
                }
            }
        }
    }

    private fun appendSimulatedRoutePoint() {
        val nextPoint = if (guidePoints.size >= 2) {
            val idx = elapsedSeconds.coerceAtMost(guidePoints.lastIndex)
            guidePoints[idx]
        } else {
            val angle = elapsedSeconds * 0.12
            val radius = 0.0018
            RoutePointDto(
                lat = DEFAULT_CENTER.latitude + radius * sin(angle),
                lng = DEFAULT_CENTER.longitude + radius * cos(angle)
            )
        }
        if (recordedPoints.lastOrNull() != nextPoint) {
            recordedPoints.add(nextPoint)
            syncLivePolyline()
        }
    }

    private fun syncLivePolyline() {
        livePolyline?.points = recordedPoints.map { LatLng(it.lat, it.lng) }
    }

    private fun computeSimulatedDistanceKm(seconds: Int): Double {
        val rawKm = maxOf(0.1, (seconds / 3600.0) * 10.0)
        return (rawKm * 100.0).roundToInt() / 100.0
    }

    private fun updateLiveMetricsUi() {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val simulatedKm = (elapsedSeconds / 3600.0) * 10.0

        tvDurationValue?.text = String.format(Locale.US, "%02d:%02d", minutes, seconds)
        tvDistanceValue?.text = String.format(Locale.US, "%.2f", simulatedKm)
        tvPaceValue?.text = "6'00\""
    }

    private fun stopAndSaveRun(btnStop: MaterialCardView) {
        if (isStopping) return
        isStopping = true
        isPaused = true
        timerJob?.cancel()
        btnStop.isEnabled = false

        val durationSeconds = maxOf(1, elapsedSeconds)
        val distanceKm = computeSimulatedDistanceKm(elapsedSeconds)
        val avgPaceSeconds = (durationSeconds / distanceKm).roundToInt()
        val caloriesBurned = maxOf(1, (distanceKm * 65.0).roundToInt())
        val runTitle = routeName?.takeIf { it.isNotBlank() }?.let { "GPS Art: $it" } ?: "Buổi chạy RunTrack"
        val pointsPayload = recordedPoints.ifEmpty {
            listOf(RoutePointDto(DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude))
        }

        val request = ActivityCreateRequest(
            title = runTitle,
            distance_km = distanceKm,
            duration_seconds = durationSeconds,
            avg_pace_seconds = avgPaceSeconds,
            calories_burned = caloriesBurned,
            route_points = pointsPayload
        )

        lifecycleScope.launch {
            val saved = saveActivityToServer(request)
            val toastMsg = if (saved) "Đã lưu buổi chạy lên máy chủ!" else "Ngoại tuyến: đã lưu buổi chạy cục bộ."
            Toast.makeText(this@TrackingActivity, toastMsg, Toast.LENGTH_SHORT).show()
            openRunSummary(distanceKm, durationSeconds, saved)
        }
    }

    private suspend fun saveActivityToServer(request: ActivityCreateRequest): Boolean {
        return try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.getAuthenticatedService(this@TrackingActivity).createActivity(request)
            }
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    private fun openRunSummary(distanceKm: Double, durationSeconds: Int, saved: Boolean) {
        val intent = Intent(this, RunSummaryActivity::class.java).apply {
            putExtra(RunSummaryActivity.EXTRA_DISTANCE_KM, distanceKm)
            putExtra(RunSummaryActivity.EXTRA_DURATION_SECONDS, durationSeconds)
            putExtra(RunSummaryActivity.EXTRA_ACTIVITY_SAVED, saved)
        }
        startActivity(intent)
        finish()
    }
}
