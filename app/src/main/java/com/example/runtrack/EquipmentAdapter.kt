package com.example.runtrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EquipmentAdapter(
    private val items: List<Equipment>
) : RecyclerView.Adapter<EquipmentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_equipment_icon)
        val tvName: TextView = itemView.findViewById(R.id.tv_equipment_name)
        val tvUsage: TextView = itemView.findViewById(R.id.tv_equipment_usage)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_equipment)
        val tvSubtext: TextView = itemView.findViewById(R.id.tv_equipment_subtext)
        val ivStatusCheck: ImageView = itemView.findViewById(R.id.iv_equipment_status_check)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_equipment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.ivIcon.setImageResource(item.iconResId)
        holder.tvName.text = item.name
        holder.tvUsage.text = item.usageText

        if (item.isProgressVisible) {
            holder.progressBar.visibility = View.VISIBLE
            holder.progressBar.max = item.maxProgress
            holder.progressBar.progress = item.progressValue
        } else {
            holder.progressBar.visibility = View.GONE
        }

        if (!item.subtext.isNullOrEmpty()) {
            holder.tvSubtext.visibility = View.VISIBLE
            holder.tvSubtext.text = item.subtext
        } else {
            holder.tvSubtext.visibility = View.GONE
        }

        if (item.isCheckVisible) {
            holder.ivStatusCheck.visibility = View.VISIBLE
        } else {
            holder.ivStatusCheck.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size
}
