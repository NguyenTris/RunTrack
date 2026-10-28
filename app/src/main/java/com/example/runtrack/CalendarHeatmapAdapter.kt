package com.example.runtrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarHeatmapAdapter(
    private val items: List<CalendarDay>,
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarHeatmapAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rootLayout: View = itemView.findViewById(R.id.frame_calendar_day_root)
        val tvDayNumber: TextView = itemView.findViewById(R.id.tv_day_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        if (item.dayNumber == 0) {
            // Empty padded day
            holder.tvDayNumber.text = ""
            holder.rootLayout.visibility = View.INVISIBLE
            return
        }

        holder.rootLayout.visibility = View.VISIBLE
        holder.tvDayNumber.text = item.dayNumber.toString()

        when {
            item.isSelected -> holder.rootLayout.setBackgroundResource(R.drawable.bg_day_selected_cyan)
            item.heatLevel == 3 -> holder.rootLayout.setBackgroundResource(R.drawable.bg_heat_level_3)
            item.heatLevel == 2 -> holder.rootLayout.setBackgroundResource(R.drawable.bg_heat_level_2)
            item.heatLevel == 1 -> holder.rootLayout.setBackgroundResource(R.drawable.bg_heat_level_1)
            else -> holder.rootLayout.setBackgroundResource(R.drawable.bg_heat_level_0)
        }

        holder.itemView.setOnClickListener { onDayClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
