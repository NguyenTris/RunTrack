package com.example.runtrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentActivitiesAdapter(
    private val items: List<RecentActivity>,
    private val onItemClick: (RecentActivity) -> Unit
) : RecyclerView.Adapter<RecentActivitiesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDayCode: TextView = itemView.findViewById(R.id.tv_activity_day_code)
        val tvDateText: TextView = itemView.findViewById(R.id.tv_activity_date_text)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_activity_title)
        val tvMetrics: TextView = itemView.findViewById(R.id.tv_activity_metrics)
        val tvDistanceVal: TextView = itemView.findViewById(R.id.tv_activity_distance_val)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_activity_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvDayCode.text = item.dayCode
        holder.tvDateText.text = item.dateText
        holder.tvTitle.text = item.title
        holder.tvMetrics.text = item.durationAndPace
        holder.tvDistanceVal.text = item.distanceKm

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
