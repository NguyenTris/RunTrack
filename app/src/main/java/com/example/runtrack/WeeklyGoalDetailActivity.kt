package com.example.runtrack

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.UserProfileUpdate
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.max

class WeeklyGoalDetailActivity : AppCompatActivity() {

    private var tvDetailCurrentKm: TextView? = null
    private var tvDetailGoalKm: TextView? = null
    private var tvDetailRemainingKm: TextView? = null
    private var tvDetailPercentage: TextView? = null
    private var progressCircleDetail: CircularProgressIndicator? = null
    private var progressLinearDetail: LinearProgressIndicator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weekly_goal_detail)

        val root = findViewById<android.view.View>(R.id.root_layout_goal_detail)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        setupClickListeners()
        loadHomeStats()
    }

    private fun initViews() {
        tvDetailCurrentKm = findViewById(R.id.tv_detail_current_km)
        tvDetailGoalKm = findViewById(R.id.tv_detail_goal_km)
        tvDetailRemainingKm = findViewById(R.id.tv_detail_remaining_km)
        tvDetailPercentage = findViewById(R.id.tv_detail_percentage)
        progressCircleDetail = findViewById(R.id.progress_circle_detail)
        progressLinearDetail = findViewById(R.id.progress_linear_detail)
    }

    private fun setupClickListeners() {
        val tvBtnHome = findViewById<TextView>(R.id.tv_btn_home)
        tvBtnHome?.setOnClickListener {
            finish()
        }

        val btnEditAction = findViewById<MaterialButton>(R.id.btn_edit_weekly_goal_action)
        btnEditAction?.setOnClickListener {
            showEditWeeklyGoalDialog()
        }
    }

    private fun loadHomeStats() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.getAuthenticatedService(this@WeeklyGoalDetailActivity).getHomeStats()
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    runOnUiThread {
                        tvDetailCurrentKm?.text = "%.1f KM".format(stats.total_distance_km)
                        tvDetailGoalKm?.text = "Mục tiêu: %.1f KM".format(stats.weekly_goal_km)
                        val remaining = max(0f, stats.weekly_goal_km - stats.total_distance_km)
                        tvDetailRemainingKm?.text = "Còn lại: %.1f KM".format(remaining)
                        progressCircleDetail?.progress = stats.completion_percentage
                        progressLinearDetail?.progress = stats.completion_percentage
                        tvDetailPercentage?.text = "${stats.completion_percentage}%"

                        updateWeeklyDaysStatus(stats.active_days)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("WeeklyGoalDetail", "Lỗi tải home-stats: ${e.message}")
            }
        }
    }

    private fun updateWeeklyDaysStatus(activeDays: List<Int>?) {
        val rawDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        // Calendar.SUNDAY = 1 -> mapped to 8
        // Calendar.MONDAY = 2 -> mapped to 2
        // ...
        val todayDayOfWeek = if (rawDay == Calendar.SUNDAY) 8 else rawDay

        val daysConfig = listOf(
            2 to Triple(findViewById<View>(R.id.layout_day_t2), findViewById<TextView>(R.id.tv_day_status_t2), findViewById<ImageView>(R.id.iv_day_icon_t2)),
            3 to Triple(findViewById<View>(R.id.layout_day_t3), findViewById<TextView>(R.id.tv_day_status_t3), findViewById<ImageView>(R.id.iv_day_icon_t3)),
            4 to Triple(findViewById<View>(R.id.layout_day_t4), findViewById<TextView>(R.id.tv_day_status_t4), findViewById<ImageView>(R.id.iv_day_icon_t4)),
            5 to Triple(findViewById<View>(R.id.layout_day_t5), findViewById<TextView>(R.id.tv_day_status_t5), findViewById<ImageView>(R.id.iv_day_icon_t5)),
            6 to Triple(findViewById<View>(R.id.layout_day_t6), findViewById<TextView>(R.id.tv_day_status_t6), findViewById<ImageView>(R.id.iv_day_icon_t6)),
            7 to Triple(findViewById<View>(R.id.layout_day_t7), findViewById<TextView>(R.id.tv_day_status_t7), findViewById<ImageView>(R.id.iv_day_icon_t7)),
            8 to Triple(findViewById<View>(R.id.layout_day_cn), findViewById<TextView>(R.id.tv_day_status_cn), findViewById<ImageView>(R.id.iv_day_icon_cn))
        )

        for ((dayVal, views) in daysConfig) {
            val container = views.first
            val tvStatus = views.second
            val ivIcon = views.third

            val hasRun = activeDays?.contains(dayVal) == true
            val isToday = (dayVal == todayDayOfWeek)

            when {
                hasRun -> {
                    container?.setBackgroundColor(Color.parseColor("#FF5722")) // Cam khi có chạy
                    tvStatus?.text = ""
                    tvStatus?.visibility = View.GONE
                    ivIcon?.visibility = View.VISIBLE
                    ivIcon?.setColorFilter(Color.WHITE)
                }
                isToday -> {
                    container?.setBackgroundColor(Color.parseColor("#005B94")) // Xanh đậm khi hôm nay chưa chạy
                    tvStatus?.text = "HÔM NAY"
                    tvStatus?.textSize = 9f
                    tvStatus?.setTextColor(Color.parseColor("#00E5FF"))
                    tvStatus?.visibility = View.VISIBLE
                    ivIcon?.visibility = View.GONE
                }
                else -> {
                    container?.setBackgroundColor(Color.parseColor("#1F242D")) // Xám chìm
                    tvStatus?.text = "—"
                    tvStatus?.textSize = 14f
                    tvStatus?.setTextColor(Color.parseColor("#5A6678"))
                    tvStatus?.visibility = View.VISIBLE
                    ivIcon?.visibility = View.GONE
                }
            }
        }
    }

    private fun showEditWeeklyGoalDialog() {
        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Nhập mục tiêu km (VD: 40.0)"
            val currentText = tvDetailGoalKm?.text?.toString() ?: ""
            val numericStr = currentText.replace("Mục tiêu:", "").replace("KM", "").trim()
            if (numericStr.isNotEmpty()) {
                setText(numericStr)
            }
            setSelection(text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Cập Nhật Mục Tiêu Tuần")
            .setView(FrameLayout(this).apply {
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
                val response = ApiClient.getAuthenticatedService(this@WeeklyGoalDetailActivity).updateUserProfile(requestBody)
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@WeeklyGoalDetailActivity, "Cập nhật mục tiêu tuần thành công!", Toast.LENGTH_SHORT).show()
                        loadHomeStats()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@WeeklyGoalDetailActivity, "Lỗi cập nhật từ server: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@WeeklyGoalDetailActivity, "Không thể kết nối VPS: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
