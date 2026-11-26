package com.example.runtrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.model.progress.MonthProgressResponse
import com.example.runtrack.network.model.progress.ProgressDayDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class ProgressMonthFragment : Fragment() {

    private lateinit var rvHeatmap: RecyclerView
    private lateinit var rvMonthRecords: RecyclerView

    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var dayDetailMap: Map<Int, ProgressDayDto> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_progress_month, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvHeatmap = view.findViewById(R.id.rv_calendar_heatmap)
        rvHeatmap.layoutManager = GridLayoutManager(requireContext(), 7)
        rvMonthRecords = view.findViewById(R.id.rv_month_records)
        rvMonthRecords.layoutManager = LinearLayoutManager(requireContext())

        loadMonthProgress()
    }

    private fun loadMonthProgress() {
        val ctx = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(ctx)
                        .getProgressMonth(year = currentYear, month = currentMonth)
                }
                if (!isAdded) return@launch
                if (response.isSuccessful && response.body() != null) {
                    bindMonthData(response.body()!!)
                } else {
                    renderEmptyState("Không thể tải dữ liệu tiến độ tháng")
                }
            } catch (_: Exception) {
                if (isAdded) {
                    renderEmptyState("Lỗi kết nối máy chủ khi tải tiến độ tháng")
                }
            }
        }
    }

    private fun bindMonthData(data: MonthProgressResponse) {
        currentYear = data.year
        currentMonth = data.month
        dayDetailMap = data.days.associateBy { it.day }

        val todayCal = Calendar.getInstance()
        val isSameMonth = todayCal.get(Calendar.YEAR) == data.year &&
            (todayCal.get(Calendar.MONTH) + 1) == data.month
        val todayDay = if (isSameMonth) todayCal.get(Calendar.DAY_OF_MONTH) else -1

        val daysList = buildCalendarDays(data.first_weekday, data.days_in_month, todayDay)
        rvHeatmap.adapter = CalendarHeatmapAdapter(daysList) { clickedDay ->
            showDayToast(clickedDay.dayNumber)
        }

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
                    title = "Chưa có kỷ lục tháng $currentMonth/$currentYear",
                    subtitle = String.format(
                        Locale.US,
                        "Tổng: %.1f km • %d buổi chạy",
                        data.total_distance_km,
                        data.total_activities
                    ),
                    recordValue = "0 KM",
                    iconResId = R.drawable.ic_trophy
                )
            )
        }
        rvMonthRecords.adapter = MonthRecordsAdapter(records)
    }

    private fun buildCalendarDays(
        firstWeekday: Int,
        daysInMonth: Int,
        selectedDay: Int
    ): List<CalendarDay> {
        val daysList = ArrayList<CalendarDay>()
        val offset = firstWeekday.coerceIn(0, 6)
        repeat(offset) { daysList.add(CalendarDay(0, 0)) }
        val totalDays = daysInMonth.coerceIn(28, 31)
        for (day in 1..totalDays) {
            val heat = dayDetailMap[day]?.heat_level ?: 0
            daysList.add(CalendarDay(day, heat, isSelected = (day == selectedDay)))
        }
        return daysList
    }

    private fun showDayToast(dayNumber: Int) {
        if (dayNumber <= 0) return
        val detail = dayDetailMap[dayNumber]
        val message = if (detail != null && detail.distance_km > 0.0) {
            String.format(
                Locale.US,
                "Ngày %02d/%02d: %.2f km (%d buổi)",
                dayNumber,
                currentMonth,
                detail.distance_km,
                detail.activities_count
            )
        } else {
            String.format(Locale.US, "Ngày %02d/%02d: Chưa có buổi chạy", dayNumber, currentMonth)
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun renderEmptyState(errorMsg: String) {
        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        val fallbackDays = buildCalendarDays(0, 30, -1)
        rvHeatmap.adapter = CalendarHeatmapAdapter(fallbackDays) { showDayToast(it.dayNumber) }
        rvMonthRecords.adapter = MonthRecordsAdapter(
            listOf(
                MonthRecord("0", "Chưa có dữ liệu", "Vui lòng kiểm tra kết nối mạng", "--", R.drawable.ic_trophy)
            )
        )
    }
}
