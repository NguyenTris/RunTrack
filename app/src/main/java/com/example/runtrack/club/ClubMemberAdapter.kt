package com.example.runtrack.club

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R
import com.google.android.material.imageview.ShapeableImageView

class ClubMemberAdapter(
    private var memberList: List<ClubMember>,
    private val onItemClick: (ClubMember) -> Unit
) : RecyclerView.Adapter<ClubMemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.iv_member_avatar)
        val tvName: TextView = itemView.findViewById(R.id.tv_member_name)
        val tvRole: TextView = itemView.findViewById(R.id.tv_member_role)
        val tvKm: TextView = itemView.findViewById(R.id.tv_member_km)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_club_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val item = memberList[position]
        holder.tvName.text = item.name
        holder.tvRole.text = item.role
        holder.tvKm.text = item.kmWeekly

        if (item.avatarResId != null) {
            holder.ivAvatar.setImageResource(item.avatarResId)
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person_chat)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = memberList.size

    fun updateList(newList: List<ClubMember>) {
        memberList = newList
        notifyDataSetChanged()
    }
}
