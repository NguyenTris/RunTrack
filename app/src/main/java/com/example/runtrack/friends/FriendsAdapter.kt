package com.example.runtrack.friends

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runtrack.R
import com.example.runtrack.network.ApiConfig
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

enum class FriendItemType {
    FRIEND,
    REQUEST,
    SEARCH_RESULT
}

data class FriendDisplayItem(
    val id: Int,
    val fullName: String,
    val email: String,
    val avatarUrl: String?,
    val friendStatus: String, // "none", "pending_sent", "pending_received", "accepted"
    val itemType: FriendItemType
)

class FriendsAdapter(
    private var items: List<FriendDisplayItem>,
    private val onPrimaryAction: (FriendDisplayItem) -> Unit,
    private val onSecondaryAction: (FriendDisplayItem) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    fun updateList(newItems: List<FriendDisplayItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_user, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.iv_friend_avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tv_friend_name)
        private val tvEmail: TextView = itemView.findViewById(R.id.tv_friend_email)
        private val btnPrimary: MaterialButton = itemView.findViewById(R.id.btn_friend_primary)
        private val btnSecondary: MaterialButton = itemView.findViewById(R.id.btn_friend_secondary)

        fun bind(item: FriendDisplayItem) {
            tvName.text = item.fullName
            tvEmail.text = item.email

            if (!item.avatarUrl.isNullOrBlank()) {
                val fullUrl = if (item.avatarUrl.startsWith("http://") || item.avatarUrl.startsWith("https://")) {
                    item.avatarUrl
                } else {
                    ApiConfig.BASE_URL.trimEnd('/') + "/" + item.avatarUrl.trimStart('/')
                }
                Glide.with(itemView.context)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_person_chat)
                    .error(R.drawable.ic_person_chat)
                    .circleCrop()
                    .into(ivAvatar)
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person_chat)
            }

            btnPrimary.isEnabled = true
            btnPrimary.setOnClickListener { onPrimaryAction(item) }
            btnSecondary.setOnClickListener { onSecondaryAction(item) }

            when (item.itemType) {
                FriendItemType.FRIEND -> {
                    btnPrimary.text = "Nhắn tin"
                    btnPrimary.setBackgroundColor(Color.parseColor("#FF5722"))
                    btnSecondary.visibility = View.VISIBLE
                    btnSecondary.text = "Hủy kết bạn"
                    btnSecondary.setTextColor(Color.parseColor("#FF5252"))
                    btnSecondary.setStrokeColorResource(android.R.color.transparent)
                }
                FriendItemType.REQUEST -> {
                    btnPrimary.text = "Chấp nhận"
                    btnPrimary.setBackgroundColor(Color.parseColor("#4CAF50"))
                    btnSecondary.visibility = View.VISIBLE
                    btnSecondary.text = "Từ chối"
                    btnSecondary.setTextColor(Color.parseColor("#8E8E93"))
                    btnSecondary.setStrokeColorResource(android.R.color.transparent)
                }
                FriendItemType.SEARCH_RESULT -> {
                    when (item.friendStatus) {
                        "none" -> {
                            btnPrimary.text = "Kết bạn"
                            btnPrimary.setBackgroundColor(Color.parseColor("#FF5722"))
                            btnSecondary.visibility = View.GONE
                        }
                        "pending_sent" -> {
                            btnPrimary.text = "Đã gửi"
                            btnPrimary.setBackgroundColor(Color.parseColor("#555555"))
                            btnPrimary.isEnabled = false
                            btnSecondary.visibility = View.VISIBLE
                            btnSecondary.text = "Hủy lời mời"
                            btnSecondary.setTextColor(Color.parseColor("#FF5252"))
                        }
                        "pending_received" -> {
                            btnPrimary.text = "Chấp nhận"
                            btnPrimary.setBackgroundColor(Color.parseColor("#4CAF50"))
                            btnSecondary.visibility = View.VISIBLE
                            btnSecondary.text = "Từ chối"
                            btnSecondary.setTextColor(Color.parseColor("#8E8E93"))
                        }
                        "accepted" -> {
                            btnPrimary.text = "Bạn bè"
                            btnPrimary.setBackgroundColor(Color.parseColor("#388E3C"))
                            btnPrimary.isEnabled = false
                            btnSecondary.visibility = View.GONE
                        }
                        else -> {
                            btnPrimary.text = "Kết bạn"
                            btnPrimary.setBackgroundColor(Color.parseColor("#FF5722"))
                            btnSecondary.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}
