package com.example.runtrack.chat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R
import com.google.android.material.imageview.ShapeableImageView

class ChatAdapter(
    private var chatList: List<ChatItem>,
    private val onItemClick: (ChatItem) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.iv_avatar)
        val ivGroupIcon: ImageView = itemView.findViewById(R.id.iv_group_icon)
        val viewOnlineDot: View = itemView.findViewById(R.id.view_online_dot)
        val tvName: TextView = itemView.findViewById(R.id.tv_chat_name)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        val tvTime: TextView = itemView.findViewById(R.id.tv_chat_time)
        val tvUnreadBadge: TextView = itemView.findViewById(R.id.tv_unread_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = chatList[position]

        holder.tvName.text = item.name
        holder.tvLastMessage.text = item.lastMessage
        holder.tvTime.text = item.time

        // Configure Avatar & Online Indicator
        if (item.isGroup) {
            holder.ivGroupIcon.visibility = View.VISIBLE
            holder.ivAvatar.visibility = View.GONE
            holder.viewOnlineDot.visibility = View.GONE
        } else {
            holder.ivGroupIcon.visibility = View.GONE
            holder.ivAvatar.visibility = View.VISIBLE
            
            if (item.avatarResId != null) {
                holder.ivAvatar.setImageResource(item.avatarResId)
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_person_chat)
            }

            holder.viewOnlineDot.visibility = if (item.isOnline) View.VISIBLE else View.GONE
        }

        // Configure Unread Badge
        if (item.unreadCount > 0) {
            holder.tvUnreadBadge.visibility = View.VISIBLE
            holder.tvUnreadBadge.text = item.unreadCount.toString()
            holder.tvTime.setTextColor(Color.parseColor("#FF5722"))
        } else {
            holder.tvUnreadBadge.visibility = View.GONE
            holder.tvTime.setTextColor(Color.parseColor("#8E8E93"))
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = chatList.size

    fun updateList(newList: List<ChatItem>) {
        chatList = newList
        notifyDataSetChanged()
    }
}
