package com.example.runtrack

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView

class LeaderboardAdapter(
    private val items: List<LeaderboardRunner>,
    private val onItemClick: (LeaderboardRunner) -> Unit
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardItem: MaterialCardView = itemView.findViewById(R.id.card_rank_item)
        val tvRankNumber: TextView = itemView.findViewById(R.id.tv_rank_number)
        val ivRankAvatar: ShapeableImageView = itemView.findViewById(R.id.iv_rank_avatar)
        val tvRunnerName: TextView = itemView.findViewById(R.id.tv_runner_name)
        val tvRunnerLocation: TextView = itemView.findViewById(R.id.tv_runner_location)
        val tvRunnerTime: TextView = itemView.findViewById(R.id.tv_runner_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_rank, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.tvRankNumber.text = item.rank
        holder.tvRunnerName.text = item.name
        holder.tvRunnerLocation.text = item.location
        holder.tvRunnerTime.text = item.time
        holder.ivRankAvatar.setImageResource(item.avatarResId)

        if (item.isHighlighted) {
            // Highlighted Friend / User Row
            val orangeColor = ContextCompat.getColor(context, R.color.kinetic_orange)
            holder.tvRankNumber.setTextColor(orangeColor)
            holder.tvRunnerName.setTextColor(orangeColor)
            holder.tvRunnerTime.setTextColor(orangeColor)
            holder.cardItem.strokeColor = orangeColor
            holder.cardItem.strokeWidth = dpToPx(context, 1)
            holder.ivRankAvatar.strokeColor = ContextCompat.getColorStateList(context, R.color.kinetic_orange)
        } else {
            // Standard Row
            val whiteColor = ContextCompat.getColor(context, R.color.text_primary)
            val secondaryColor = ContextCompat.getColor(context, R.color.text_secondary)
            val strokeColor = ContextCompat.getColor(context, R.color.card_stroke)

            holder.tvRankNumber.setTextColor(secondaryColor)
            holder.tvRunnerName.setTextColor(whiteColor)
            holder.tvRunnerTime.setTextColor(whiteColor)
            holder.cardItem.strokeColor = strokeColor
            holder.cardItem.strokeWidth = dpToPx(context, 1)
            holder.ivRankAvatar.strokeColor = ContextCompat.getColorStateList(context, R.color.card_stroke)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
