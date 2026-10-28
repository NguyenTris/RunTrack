package com.example.runtrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProgressYearFragment : Fragment() {

    private lateinit var rvQuarterStats: RecyclerView
    private lateinit var quarterStatsAdapter: QuarterStatsAdapter

    private lateinit var rvYearRecords: RecyclerView
    private lateinit var yearRecordsAdapter: MonthRecordsAdapter

    private lateinit var rvYearAchievements: RecyclerView
    private lateinit var yearAchievementsAdapter: AchievementsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_progress_year, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initQuarterStatsRecyclerView(view)
        initYearRecordsRecyclerView(view)
        initYearAchievementsRecyclerView(view)
    }

    private fun initQuarterStatsRecyclerView(view: View) {
        rvQuarterStats = view.findViewById(R.id.rv_quarter_stats)
        rvQuarterStats.layoutManager = LinearLayoutManager(requireContext())

        val sampleQuarters = listOf(
            QuarterStat("Q1", 294f, 400f, isCurrentQuarter = false),
            QuarterStat("Q2", 374f, 400f, isCurrentQuarter = false),
            QuarterStat("Q3", 384f, 400f, isCurrentQuarter = false),
            QuarterStat("Q4", 128.6f, 400f, isCurrentQuarter = true)
        )

        quarterStatsAdapter = QuarterStatsAdapter(sampleQuarters)
        rvQuarterStats.adapter = quarterStatsAdapter
    }

    private fun initYearRecordsRecyclerView(view: View) {
        rvYearRecords = view.findViewById(R.id.rv_year_records)
        rvYearRecords.layoutManager = LinearLayoutManager(requireContext())

        val sampleRecords = listOf(
            MonthRecord("1", "5 KM nhanh nhất", "THÁNG 8 · HỒ GƯƠM", "23:48", R.drawable.ic_trophy),
            MonthRecord("2", "10 KM nhanh nhất", "THÁNG 7 · HỒ TÂY", "49:55", R.drawable.ic_trophy),
            MonthRecord("3", "Half Marathon", "THÁNG 5 · ECOPARK", "1:51:20", R.drawable.ic_trophy),
            MonthRecord("4", "Tháng nhiều nhất", "THÁNG 7", "140 KM", R.drawable.ic_trophy)
        )

        yearRecordsAdapter = MonthRecordsAdapter(sampleRecords)
        rvYearRecords.adapter = yearRecordsAdapter
    }

    private fun initYearAchievementsRecyclerView(view: View) {
        rvYearAchievements = view.findViewById(R.id.rv_year_achievements)
        rvYearAchievements.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val sampleBadges = listOf(
            Achievement("1", "5K < 25'", R.drawable.ic_trophy, isUnlocked = true),
            Achievement("2", "HALF MARATHON", R.drawable.ic_route_network, isUnlocked = true),
            Achievement("3", "CHUỖI 30 NGÀY", R.drawable.ic_thunderbolt, isUnlocked = true),
            Achievement("4", "1,500 KM/NĂM", R.drawable.ic_lock, isUnlocked = false)
        )

        yearAchievementsAdapter = AchievementsAdapter(sampleBadges)
        rvYearAchievements.adapter = yearAchievementsAdapter
    }
}
