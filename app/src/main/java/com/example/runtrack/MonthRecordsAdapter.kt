package com.example.runtrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MonthRecordsAdapter(
    private val items: List<MonthRecord>
) : RecyclerView.Adapter<MonthRecordsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_record_icon)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_record_title)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tv_record_subtitle)
        val tvValue: TextView = itemView.findViewById(R.id.tv_record_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.ivIcon.setImageResource(item.iconResId)
        holder.tvTitle.text = item.title
        holder.tvSubtitle.text = item.subtitle
        holder.tvValue.text = item.recordValue
    }

    override fun getItemCount(): Int = items.size
}
