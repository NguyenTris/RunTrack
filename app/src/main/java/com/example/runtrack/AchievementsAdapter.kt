package com.example.runtrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AchievementsAdapter(
    private val items: List<Achievement>
) : RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_badge_icon)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_badge_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement_badge, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.ivIcon.setImageResource(item.iconResId)
        if (!item.isUnlocked) {
            holder.itemView.alpha = 0.5f
        } else {
            holder.itemView.alpha = 1.0f
        }
    }

    override fun getItemCount(): Int = items.size
}
