package com.example.runtrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class HotRoutesAdapter(
    private val items: List<HotRoute>,
    private val onItemClick: (HotRoute) -> Unit
) : RecyclerView.Adapter<HotRoutesAdapter.HotRouteViewHolder>() {

    class HotRouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivRouteImage: ImageView = itemView.findViewById(R.id.iv_route_image)
        val tvBadgeTag: TextView = itemView.findViewById(R.id.tv_badge_tag)
        val tvDistancePill: TextView = itemView.findViewById(R.id.tv_distance_pill)
        val tvRouteTitle: TextView = itemView.findViewById(R.id.tv_route_title)
        val tvRouteSubtitle: TextView = itemView.findViewById(R.id.tv_route_subtitle)
        val tvRunsCount: TextView = itemView.findViewById(R.id.tv_runs_count)
        val btnLoadMap: MaterialButton = itemView.findViewById(R.id.btn_load_map)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotRouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hot_route, parent, false)
        return HotRouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: HotRouteViewHolder, position: Int) {
        val item = items[position]
        holder.tvRouteTitle.text = item.title
        holder.tvRouteSubtitle.text = item.subtitle
        holder.tvDistancePill.text = item.distance
        holder.tvBadgeTag.text = item.badgeTag
        holder.tvRunsCount.text = item.runsCount
        holder.ivRouteImage.setImageResource(item.imageResId)

        holder.btnLoadMap.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
