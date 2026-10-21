package com.example.runtrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class LapSplitsAdapter(
    private val items: List<LapSplit>
) : RecyclerView.Adapter<LapSplitsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rootLayout: View = itemView.findViewById(R.id.layout_lap_item_root)
        val tvKm: TextView = itemView.findViewById(R.id.tv_lap_km)
        val tvPace: TextView = itemView.findViewById(R.id.tv_lap_pace)
        val tvHr: TextView = itemView.findViewById(R.id.tv_lap_hr)
        val tvElevation: TextView = itemView.findViewById(R.id.tv_lap_elevation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lap_split, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvKm.text = item.kmText
        holder.tvPace.text = item.paceText
        holder.tvHr.text = item.hrText
        holder.tvElevation.text = item.elevationText

        if (item.isHighlighted) {
            holder.rootLayout.setBackgroundResource(R.drawable.bg_card_highlight_orange)
            val orangeColor = ContextCompat.getColor(holder.itemView.context, R.color.kinetic_orange)
            holder.tvKm.setTextColor(orangeColor)
            holder.tvPace.setTextColor(orangeColor)
            holder.tvHr.setTextColor(orangeColor)
            holder.tvElevation.setTextColor(orangeColor)
        } else {
            holder.rootLayout.background = null
            val primaryColor = ContextCompat.getColor(holder.itemView.context, R.color.text_primary)
            val secondaryColor = ContextCompat.getColor(holder.itemView.context, R.color.text_secondary)
            holder.tvKm.setTextColor(primaryColor)
            holder.tvPace.setTextColor(primaryColor)
            holder.tvHr.setTextColor(secondaryColor)
            holder.tvElevation.setTextColor(secondaryColor)
        }
    }

    override fun getItemCount(): Int = items.size
}
