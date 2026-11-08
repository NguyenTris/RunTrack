package com.example.runtrack.club

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R
import com.google.android.material.button.MaterialButton

class ClubEventAdapter(
    private var eventList: List<ClubEvent>,
    private val onActionClick: (ClubEvent) -> Unit
) : RecyclerView.Adapter<ClubEventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_event_title)
        val tvTime: TextView = itemView.findViewById(R.id.tv_event_time)
        val tvLocation: TextView = itemView.findViewById(R.id.tv_event_location)
        val tvRegistered: TextView = itemView.findViewById(R.id.tv_event_registered)
        val btnAction: MaterialButton = itemView.findViewById(R.id.btn_event_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_club_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = eventList[position]
        holder.tvTitle.text = item.title
        holder.tvTime.text = item.time
        holder.tvLocation.text = item.location
        holder.tvRegistered.text = item.registeredCount

        if (item.isJoined) {
            holder.btnAction.text = "Đã đăng ký"
            holder.btnAction.setBackgroundColor(Color.parseColor("#22262B"))
            holder.btnAction.setTextColor(Color.parseColor("#8E8E93"))
        } else {
            holder.btnAction.text = "Tham gia"
            holder.btnAction.setBackgroundColor(Color.parseColor("#F4C073"))
            holder.btnAction.setTextColor(Color.parseColor("#121212"))
        }

        holder.btnAction.setOnClickListener { onActionClick(item) }
    }

    override fun getItemCount(): Int = eventList.size

    fun updateList(newList: List<ClubEvent>) {
        eventList = newList
        notifyDataSetChanged()
    }
}
