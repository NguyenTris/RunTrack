package com.example.runtrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArtSuggestionsAdapter(
    private val items: List<ArtSuggestion>,
    private val onItemClick: (ArtSuggestion) -> Unit
) : RecyclerView.Adapter<ArtSuggestionsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_suggestion_icon)
        val tvName: TextView = itemView.findViewById(R.id.tv_suggestion_name)
        val tvDistance: TextView = itemView.findViewById(R.id.tv_suggestion_distance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_art_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvDistance.text = item.distance
        holder.ivIcon.setImageResource(item.iconResId)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
