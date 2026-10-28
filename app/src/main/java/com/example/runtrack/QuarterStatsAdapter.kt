package com.example.runtrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class QuarterStatsAdapter(
    private val items: List<QuarterStat>
) : RecyclerView.Adapter<QuarterStatsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_quarter_name)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_quarter)
        val tvValue: TextView = itemView.findViewById(R.id.tv_quarter_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quarter_stat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.quarterName
        holder.tvValue.text = if (item.distanceKm % 1 == 0f) item.distanceKm.toInt().toString() else item.distanceKm.toString()

        holder.progressBar.max = item.maxKm.toInt()
        holder.progressBar.progress = item.distanceKm.toInt()

        if (item.isCurrentQuarter) {
            holder.progressBar.progressDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.hud_bg_bar_orange)

            val orangeColor = ContextCompat.getColor(holder.itemView.context, R.color.kinetic_orange)
            holder.tvName.setTextColor(orangeColor)
            holder.tvValue.setTextColor(orangeColor)
        } else {
            holder.progressBar.progressDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.hud_bg_bar_cyan)

            val primaryColor = ContextCompat.getColor(holder.itemView.context, R.color.text_primary)
            holder.tvName.setTextColor(primaryColor)
            holder.tvValue.setTextColor(primaryColor)
        }
    }

    override fun getItemCount(): Int = items.size
}
