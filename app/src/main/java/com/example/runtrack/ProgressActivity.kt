package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ProgressActivity : AppCompatActivity() {

    private lateinit var rvAchievements: RecyclerView
    private lateinit var achievementsAdapter: AchievementsAdapter

    private lateinit var rvRecentActivities: RecyclerView
    private lateinit var recentActivitiesAdapter: RecentActivitiesAdapter

    private var isMonthInitialized = false
    private var isYearInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_progress)

        val root = findViewById<View>(R.id.root_layout_progress) ?: findViewById(R.id.root_layout_progress_land)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initWeekViews()
        setupTimeRangeToggles()
    }

    private fun setupTimeRangeToggles() {
        val btnTimeWeek = findViewById<MaterialButton>(R.id.btn_time_week)
        val btnTimeMonth = findViewById<MaterialButton>(R.id.btn_time_month)
        val btnTimeYear = findViewById<MaterialButton>(R.id.btn_time_year)

        val containerWeek = findViewById<View>(R.id.container_week_view)
        val containerMonth = findViewById<View>(R.id.container_month_view)
        val containerYear = findViewById<View>(R.id.container_year_view)

        fun selectWeek() {
            btnTimeWeek?.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.kinetic_orange))
            btnTimeWeek?.setTextColor(ContextCompat.getColor(this, R.color.bg_dark))

            btnTimeMonth?.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.surface_card))
            btnTimeMonth?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))

            btnTimeYear?.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.surface_card))
            btnTimeYear?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))

            containerWeek?.visibility = View.VISIBLE
            containerMonth?.visibility = View.GONE
            containerYear?.visibility = View.GONE
        }

        fun selectMonth() {
            btnTimeMonth?.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.kinetic_orange))
            btnTimeMonth?.setTextColor(ContextCompat.getColor(this, R.color.bg_dark))

            btnTimeWeek?.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.surface_card))
            btnTimeWeek?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))

            btnTimeYear?.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.surface_card))
            btnTimeYear?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))

            containerWeek?.visibility = View.GONE
            containerMonth?.visibility = View.VISIBLE
            containerYear?.visibility = View.GONE

            if (!isMonthInitialized) {
                initMonthViews()
                isMonthInitialized = true
            }
        }

        fun selectYear() {
            btnTimeYear?.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.kinetic_orange))
            btnTimeYear?.setTextColor(ContextCompat.getColor(this, R.color.bg_dark))

            btnTimeWeek?.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.surface_card))
            btnTimeWeek?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))

            btnTimeMonth?.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.surface_card))
            btnTimeMonth?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))

            containerWeek?.visibility = View.GONE
            containerMonth?.visibility = View.GONE
            containerYear?.visibility = View.VISIBLE

            if (!isYearInitialized) {
                initYearViews()
                isYearInitialized = true
            }
        }

        btnTimeWeek?.setOnClickListener { selectWeek() }
        btnTimeMonth?.setOnClickListener { selectMonth() }
        btnTimeYear?.setOnClickListener { selectYear() }
    }

    private fun initWeekViews() {
        val btnBack = findViewById<ImageView>(R.id.btn_back_progress)
        btnBack?.setOnClickListener { finish() }

        // Setup Achievements Horizontal RecyclerView
        rvAchievements = findViewById(R.id.rv_achievements)
        rvAchievements.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val sampleBadges = listOf(
            Achievement("1", "5K < 25'", R.drawable.ic_trophy, isUnlocked = true),
            Achievement("2", "10K ĐẦU TIÊN", R.drawable.ic_route_network, isUnlocked = true),
            Achievement("3", "CHUỖI 7 NGÀY", R.drawable.ic_lock, isUnlocked = false),
            Achievement("4", "100 KM/THÁNG", R.drawable.ic_lock, isUnlocked = false)
        )

        achievementsAdapter = AchievementsAdapter(sampleBadges)
        rvAchievements.adapter = achievementsAdapter

        // Setup Recent Activities Vertical RecyclerView
        rvRecentActivities = findViewById(R.id.rv_recent_activities)
        rvRecentActivities.layoutManager = LinearLayoutManager(this)

        val sampleActivities = listOf(
            RecentActivity("1", "T4", "21/10", "Chạy sáng - Hồ Gươm", "28:47 · 5'18\"/KM", "5.42"),
            RecentActivity("2", "T4", "21/10", "Chạy tối cùng nhóm", "31:02 · 5'33\"/KM", "5.58"),
            RecentActivity("3", "T3", "20/10", "Long run - Hồ Tây", "52:18 · 5'19\"/KM", "9.8"),
            RecentActivity("4", "T2", "19/10", "Tempo - Cầu Giấy", "1:01:20 · 5'17\"/KM", "11.6")
        )

        recentActivitiesAdapter = RecentActivitiesAdapter(sampleActivities) { _ ->
            val intent = Intent(this, RunSummaryActivity::class.java)
            startActivity(intent)
        }
        rvRecentActivities.adapter = recentActivitiesAdapter

        // View All Activities
        val btnViewAll = findViewById<android.widget.TextView>(R.id.btn_view_all_activities)
        btnViewAll?.setOnClickListener {
            Toast.makeText(this, "Xem tất cả lịch sử chạy", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initMonthViews() {
        val rvHeatmap = findViewById<RecyclerView>(R.id.rv_calendar_heatmap)
        if (rvHeatmap != null) {
            rvHeatmap.layoutManager = GridLayoutManager(this, 7)

            val daysList = ArrayList<CalendarDay>()

            // Offset empty days
            daysList.add(CalendarDay(0, 0))
            daysList.add(CalendarDay(0, 0))
            daysList.add(CalendarDay(0, 0))

            val heatLevels = mapOf(
                1 to 1, 2 to 0, 3 to 2, 4 to 3,
                5 to 0, 6 to 2, 7 to 1, 8 to 0, 9 to 2, 10 to 3, 11 to 2,
                12 to 0, 13 to 2, 14 to 1, 15 to 0, 16 to 3, 17 to 0, 18 to 2,
                19 to 3, 20 to 3, 21 to 2,
                22 to 0, 23 to 0, 24 to 0, 25 to 0,
                26 to 0, 27 to 0, 28 to 0, 29 to 0, 30 to 0, 31 to 0
            )

            for (day in 1..31) {
                val level = heatLevels[day] ?: 0
                val isSelected = (day == 21)
                daysList.add(CalendarDay(day, level, isSelected))
            }

            rvHeatmap.adapter = CalendarHeatmapAdapter(daysList) { clickedDay ->
                if (clickedDay.dayNumber > 0) {
                    Toast.makeText(this, "Tháng 10 - Ngày ${clickedDay.dayNumber}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val rvMonthRecords = findViewById<RecyclerView>(R.id.rv_month_records)
        if (rvMonthRecords != null) {
            rvMonthRecords.layoutManager = LinearLayoutManager(this)

            val sampleRecords = listOf(
                MonthRecord("1", "5 KM nhanh nhất", "04/10 · HỒ GƯƠM", "24:12", R.drawable.ic_trophy),
                MonthRecord("2", "10 KM nhanh nhất", "10/10 · HỒ TÂY", "51:30", R.drawable.ic_trophy),
                MonthRecord("3", "Buổi chạy dài nhất", "04/10 · LONG RUN", "16.5 KM", R.drawable.ic_trophy)
            )

            rvMonthRecords.adapter = MonthRecordsAdapter(sampleRecords)
        }
    }

    private fun initYearViews() {
        val rvQuarterStats = findViewById<RecyclerView>(R.id.rv_quarter_stats)
        if (rvQuarterStats != null) {
            rvQuarterStats.layoutManager = LinearLayoutManager(this)

            val sampleQuarters = listOf(
                QuarterStat("Q1", 294f, 400f, isCurrentQuarter = false),
                QuarterStat("Q2", 374f, 400f, isCurrentQuarter = false),
                QuarterStat("Q3", 384f, 400f, isCurrentQuarter = false),
                QuarterStat("Q4", 128.6f, 400f, isCurrentQuarter = true)
            )

            rvQuarterStats.adapter = QuarterStatsAdapter(sampleQuarters)
        }

        val rvYearRecords = findViewById<RecyclerView>(R.id.rv_year_records)
        if (rvYearRecords != null) {
            rvYearRecords.layoutManager = LinearLayoutManager(this)

            val sampleRecords = listOf(
                MonthRecord("1", "5 KM nhanh nhất", "THÁNG 8 · HỒ GƯƠM", "23:48", R.drawable.ic_trophy),
                MonthRecord("2", "10 KM nhanh nhất", "THÁNG 7 · HỒ TÂY", "49:55", R.drawable.ic_trophy),
                MonthRecord("3", "Half Marathon", "THÁNG 5 · ECOPARK", "1:51:20", R.drawable.ic_trophy),
                MonthRecord("4", "Tháng nhiều nhất", "THÁNG 7", "140 KM", R.drawable.ic_trophy)
            )

            rvYearRecords.adapter = MonthRecordsAdapter(sampleRecords)
        }

        val rvYearAchievements = findViewById<RecyclerView>(R.id.rv_year_achievements)
        if (rvYearAchievements != null) {
            rvYearAchievements.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            val sampleBadges = listOf(
                Achievement("1", "5K < 25'", R.drawable.ic_trophy, isUnlocked = true),
                Achievement("2", "HALF MARATHON", R.drawable.ic_route_network, isUnlocked = true),
                Achievement("3", "CHUỖI 30 NGÀY", R.drawable.ic_thunderbolt, isUnlocked = true),
                Achievement("4", "1,500 KM/NĂM", R.drawable.ic_lock, isUnlocked = false)
            )

            rvYearAchievements.adapter = AchievementsAdapter(sampleBadges)
        }
    }
}
