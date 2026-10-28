package com.example.runtrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProgressMonthFragment : Fragment() {

    private lateinit var rvHeatmap: RecyclerView
    private lateinit var heatmapAdapter: CalendarHeatmapAdapter

    private lateinit var rvMonthRecords: RecyclerView
    private lateinit var monthRecordsAdapter: MonthRecordsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_progress_month, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initHeatmapRecyclerView(view)
        initRecordsRecyclerView(view)
    }

    private fun initHeatmapRecyclerView(view: View) {
        rvHeatmap = view.findViewById(R.id.rv_calendar_heatmap)

        // CRITICAL: GridLayoutManager with spanCount = 7 for 7 days of the week
        rvHeatmap.layoutManager = GridLayoutManager(requireContext(), 7)

        // October 2026 calendar days data (31 days) matching Figma screenshot
        val daysList = ArrayList<CalendarDay>()

        // Pad initial empty offset days
        daysList.add(CalendarDay(0, 0)) // T2
        daysList.add(CalendarDay(0, 0)) // T3
        daysList.add(CalendarDay(0, 0)) // T4

        // Days 1 to 31
        val heatLevels = mapOf(
            1 to 1, 2 to 0, 3 to 2, 4 to 3,
            5 to 0, 6 to 2, 7 to 1, 8 to 0, 9 to 2, 10 to 3, 11 to 2,
            12 to 0, 13 to 2, 14 to 1, 15 to 0, 16 to 3, 17 to 0, 18 to 2,
            19 to 3, 20 to 3, 21 to 2, // 21 is selected with cyan border
            22 to 0, 23 to 0, 24 to 0, 25 to 0,
            26 to 0, 27 to 0, 28 to 0, 29 to 0, 30 to 0, 31 to 0
        )

        for (day in 1..31) {
            val level = heatLevels[day] ?: 0
            val isSelected = (day == 21)
            daysList.add(CalendarDay(day, level, isSelected))
        }

        heatmapAdapter = CalendarHeatmapAdapter(daysList) { clickedDay ->
            if (clickedDay.dayNumber > 0) {
                Toast.makeText(requireContext(), "Tháng 10 - Ngày ${clickedDay.dayNumber}", Toast.LENGTH_SHORT).show()
            }
        }
        rvHeatmap.adapter = heatmapAdapter
    }

    private fun initRecordsRecyclerView(view: View) {
        rvMonthRecords = view.findViewById(R.id.rv_month_records)
        rvMonthRecords.layoutManager = LinearLayoutManager(requireContext())

        val sampleRecords = listOf(
            MonthRecord("1", "5 KM nhanh nhất", "04/10 · HỒ GƯƠM", "24:12", R.drawable.ic_trophy),
            MonthRecord("2", "10 KM nhanh nhất", "10/10 · HỒ TÂY", "51:30", R.drawable.ic_trophy),
            MonthRecord("3", "Buổi chạy dài nhất", "04/10 · LONG RUN", "16.5 KM", R.drawable.ic_trophy)
        )

        monthRecordsAdapter = MonthRecordsAdapter(sampleRecords)
        rvMonthRecords.adapter = monthRecordsAdapter
    }
}
