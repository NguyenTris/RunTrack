package com.example.runtrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.model.progress.YearProgressResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ProgressYearFragment : Fragment() {

    private lateinit var rvQuarterStats: RecyclerView
    private lateinit var rvYearRecords: RecyclerView
    private lateinit var rvYearAchievements: RecyclerView

    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_progress_year, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvQuarterStats = view.findViewById(R.id.rv_quarter_stats)
        rvQuarterStats.layoutManager = LinearLayoutManager(requireContext())

        rvYearRecords = view.findViewById(R.id.rv_year_records)
        rvYearRecords.layoutManager = LinearLayoutManager(requireContext())

        rvYearAchievements = view.findViewById(R.id.rv_year_achievements)
        rvYearAchievements.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        loadYearProgress()
    }

    private fun loadYearProgress() {
        val ctx = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(ctx).getProgressYear(year = currentYear)
                }
                if (!isAdded) return@launch
                if (response.isSuccessful && response.body() != null) {
                    bindYearData(response.body()!!)
                } else {
                    renderFallback("Không thể tải dữ liệu tiến độ năm")
                }
            } catch (_: Exception) {
                if (isAdded) {
                    renderFallback("Lỗi kết nối máy chủ khi tải tiến độ năm")
                }
            }
        }
    }

    private fun bindYearData(data: YearProgressResponse) {
        val quarters = data.quarters.map { q ->
            QuarterStat(
                quarterName = q.quarter,
                distanceKm = q.distance_km.toFloat(),
                maxKm = q.target_km.toFloat().coerceAtLeast(1f),
                isCurrentQuarter = q.is_current
            )
        }.ifEmpty { defaultEmptyQuarters() }
        rvQuarterStats.adapter = QuarterStatsAdapter(quarters)

        val records = data.records.mapIndexed { idx, rec ->
            MonthRecord(
                id = rec.id.ifBlank { (idx + 1).toString() },
                title = rec.title,
                subtitle = rec.subtitle,
                recordValue = rec.value,
                iconResId = R.drawable.ic_trophy
            )
        }.ifEmpty {
            listOf(
                MonthRecord(
                    id = "empty",
                    title = "Chưa có kỷ lục năm ${data.year}",
                    subtitle = "Hoàn thành buổi chạy đầu tiên để mở khóa",
                    recordValue = "0 KM",
                    iconResId = R.drawable.ic_trophy
                )
            )
        }
        rvYearRecords.adapter = MonthRecordsAdapter(records)

        val badges = data.achievements.mapIndexed { idx, ach ->
            Achievement(
                id = ach.id.ifBlank { (idx + 1).toString() },
                title = ach.title,
                iconResId = resolveAchievementIcon(ach.icon_key, ach.is_unlocked),
                isUnlocked = ach.is_unlocked
            )
        }
        rvYearAchievements.adapter = AchievementsAdapter(badges)
    }

    private fun resolveAchievementIcon(iconKey: String, isUnlocked: Boolean): Int {
        if (!isUnlocked) return R.drawable.ic_lock
        return when (iconKey.lowercase()) {
            "route", "half_marathon" -> R.drawable.ic_route_network
            "streak", "thunder", "bolt" -> R.drawable.ic_thunderbolt
            else -> R.drawable.ic_trophy
        }
    }

    private fun defaultEmptyQuarters(): List<QuarterStat> {
        val currentQ = (Calendar.getInstance().get(Calendar.MONTH) / 3) + 1
        return (1..4).map { q ->
            QuarterStat("Q$q", 0f, 400f, isCurrentQuarter = (q == currentQ))
        }
    }

    private fun renderFallback(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        rvQuarterStats.adapter = QuarterStatsAdapter(defaultEmptyQuarters())
        rvYearRecords.adapter = MonthRecordsAdapter(
            listOf(MonthRecord("0", "Chưa có dữ liệu năm", "Vui lòng thử lại sau", "--", R.drawable.ic_trophy))
        )
        rvYearAchievements.adapter = AchievementsAdapter(emptyList())
    }
}
