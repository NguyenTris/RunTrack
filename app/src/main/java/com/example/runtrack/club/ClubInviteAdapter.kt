package com.example.runtrack.club

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R
import com.google.android.material.imageview.ShapeableImageView

class ClubInviteAdapter(
    private var inviteList: List<ClubInviteFriend>,
    private val onInviteClick: (ClubInviteFriend) -> Unit
) : RecyclerView.Adapter<ClubInviteAdapter.InviteViewHolder>() {

    class InviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.iv_invite_avatar)
        val tvName: TextView = itemView.findViewById(R.id.tv_invite_name)
        val tvSub: TextView = itemView.findViewById(R.id.tv_invite_sub)
        val btnAction: TextView = itemView.findViewById(R.id.btn_invite_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_club_invite, parent, false)
        return InviteViewHolder(view)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val item = inviteList[position]
        holder.tvName.text = item.name
        holder.tvSub.text = item.location
        holder.ivAvatar.setImageResource(R.drawable.ic_person_chat)

        val context = holder.itemView.context
        if (item.isInvited) {
            holder.btnAction.text = "Đã gửi lời mời"
            holder.btnAction.background = ContextCompat.getDrawable(context, R.drawable.bg_search_btn_action_joined)
            holder.btnAction.setTextColor(Color.parseColor("#8E8E93"))
        } else {
            holder.btnAction.text = "Mời"
            holder.btnAction.background = ContextCompat.getDrawable(context, R.drawable.bg_search_btn_action)
            holder.btnAction.setTextColor(Color.parseColor("#121212"))
        }

        holder.btnAction.setOnClickListener { onInviteClick(item) }
    }

    override fun getItemCount(): Int = inviteList.size

    fun updateList(newList: List<ClubInviteFriend>) {
        inviteList = newList
        notifyDataSetChanged()
    }
}
