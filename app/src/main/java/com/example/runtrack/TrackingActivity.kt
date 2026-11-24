package com.example.runtrack

import android.content.Intent
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

class TrackingActivity : AppCompatActivity() {

    private var isPaused = false
    private var isStopping = false
    private var elapsedSeconds = 0
    private var timerJob: Job? = null

    private var tvDistanceValue: TextView? = null
    private var tvDurationValue: TextView? = null
    private var tvPaceValue: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tracking)

        val root = findViewById<View>(R.id.root_layout_tracking)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        updateLiveMetricsUi()
        startRunTimer()
    }

    private fun initViews() {
        tvDistanceValue = findViewById(R.id.tv_distance_value)
        tvDurationValue = findViewById(R.id.tv_duration_value)
        tvPaceValue = findViewById(R.id.tv_pace_value)

        findViewById<ImageView>(R.id.btn_back_tracking)?.setOnClickListener { finish() }

        val btnPause = findViewById<MaterialButton>(R.id.btn_pause_run)
        btnPause?.setOnClickListener {
            togglePause(btnPause)
        }

        val btnStop = findViewById<MaterialCardView>(R.id.btn_hold_stop)
        btnStop?.setOnClickListener {
            stopAndSaveRun(btnStop)
        }

        findViewById<View>(R.id.btn_gps_art)?.setOnClickListener {
            startActivity(Intent(this, GpsArtActivity::class.java))
        }
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
                    updateLiveMetricsUi()
                }
            }
        }
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

        val request = ActivityCreateRequest(
            title = "Buổi chạy RunTrack",
            distance_km = distanceKm,
            duration_seconds = durationSeconds,
            avg_pace_seconds = avgPaceSeconds,
            calories_burned = caloriesBurned
        )

        lifecycleScope.launch {
            val saved = saveActivityToServer(request)
            val toastMsg = if (saved) {
                "Đã lưu buổi chạy lên máy chủ!"
            } else {
                "Ngoại tuyến: đã lưu buổi chạy cục bộ."
            }
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
