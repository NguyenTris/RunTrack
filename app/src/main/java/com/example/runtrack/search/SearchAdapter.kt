package com.example.runtrack.search

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R
import com.google.android.material.imageview.ShapeableImageView

class SearchAdapter(
    private var searchList: List<SearchItem>,
    private val onItemClick: (SearchItem) -> Unit,
    private val onActionClick: (SearchItem) -> Unit
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.iv_search_avatar)
        val ivGroupIcon: ImageView = itemView.findViewById(R.id.iv_search_group_icon)
        val tvName: TextView = itemView.findViewById(R.id.tv_search_name)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tv_search_subtitle)
        val btnAction: TextView = itemView.findViewById(R.id.btn_search_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = searchList[position]

        holder.tvName.text = item.name
        holder.tvSubtitle.text = item.subtitle

        if (item.type == SearchType.CLUB) {
            holder.ivGroupIcon.visibility = View.VISIBLE
            holder.ivAvatar.visibility = View.GONE
        } else {
            holder.ivGroupIcon.visibility = View.GONE
            holder.ivAvatar.visibility = View.VISIBLE
            if (item.avatarResId != null) {
                holder.ivAvatar.setImageResource(item.avatarResId)
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_person_chat)
            }
        }

        // Action Button setup
        val context = holder.itemView.context
        if (item.isActionDone) {
            holder.btnAction.text = if (item.type == SearchType.CLUB) "Đã tham gia" else "Đã gửi"
            holder.btnAction.background = ContextCompat.getDrawable(context, R.drawable.bg_search_btn_action_joined)
            holder.btnAction.setTextColor(Color.parseColor("#8A8A8A"))
        } else {
            holder.btnAction.text = if (item.type == SearchType.CLUB) "Tham gia" else "Kết bạn"
            holder.btnAction.background = ContextCompat.getDrawable(context, R.drawable.bg_search_btn_action)
            holder.btnAction.setTextColor(Color.parseColor("#121212"))
        }

        holder.btnAction.setOnClickListener {
            onActionClick(item)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = searchList.size

    fun updateList(newList: List<SearchItem>) {
        searchList = newList
        notifyDataSetChanged()
    }
}
