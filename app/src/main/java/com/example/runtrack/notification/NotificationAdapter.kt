package com.example.runtrack.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R

class NotificationAdapter(
    private var items: MutableList<NotificationItem>,
    private val onAcceptClick: (NotificationItem, Int) -> Unit,
    private val onRejectClick: (NotificationItem, Int) -> Unit,
    private val onPrimaryActionClick: (NotificationItem, Int) -> Unit,
    private val onSecondaryActionClick: ((NotificationItem, Int) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].type == NotificationType.HEADER) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val view = inflater.inflate(R.layout.item_notification_section_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_notification_friend, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is HeaderViewHolder) {
            holder.bind(item)
        } else if (holder is ItemViewHolder) {
            holder.bind(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<NotificationItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeItemAt(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun markAllAsRead() {
        items.forEach { it.isRead = true }
        notifyDataSetChanged()
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_section_title)
        private val tvBadge: TextView = itemView.findViewById(R.id.tv_section_badge)

        fun bind(item: NotificationItem) {
            tvTitle.text = item.title
            if (!item.badgeCount.isNullOrEmpty()) {
                tvBadge.visibility = View.VISIBLE
                tvBadge.text = item.badgeCount
            } else {
                tvBadge.visibility = View.GONE
            }
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val layoutActions: LinearLayout = itemView.findViewById(R.id.layout_actions)
        private val btnPrimary: TextView = itemView.findViewById(R.id.btn_primary)
        private val btnSecondary: TextView = itemView.findViewById(R.id.btn_secondary)

        fun bind(item: NotificationItem, position: Int) {
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle
            tvTime.text = item.timeAgo

            item.avatarResId?.let { ivAvatar.setImageResource(it) }

            // Handle actions visibility
            if (item.isHandled) {
                layoutActions.visibility = View.VISIBLE
                btnSecondary.visibility = View.GONE
                btnPrimary.apply {
                    isEnabled = false
                    text = "ĐÃ XỬ LÝ ✓"
                    setBackgroundResource(R.drawable.bg_notif_btn_outline)
                    setTextColor(itemView.context.getColor(R.color.hud_signal))
                }
            } else if (item.primaryActionText != null) {
                layoutActions.visibility = View.VISIBLE
                btnPrimary.isEnabled = true
                btnPrimary.text = item.primaryActionText

                if (item.primaryActionText == "CHẤP NHẬN") {
                    btnPrimary.setBackgroundResource(R.drawable.hud_bg_bar_orange)
                    btnPrimary.setTextColor(itemView.context.getColor(R.color.white))
                    btnPrimary.setOnClickListener { onAcceptClick(item, bindingAdapterPosition) }

                    if (item.secondaryActionText != null) {
                        btnSecondary.visibility = View.VISIBLE
                        btnSecondary.text = item.secondaryActionText
                        btnSecondary.setOnClickListener { onRejectClick(item, bindingAdapterPosition) }
                    } else {
                        btnSecondary.visibility = View.GONE
                    }
                } else {
                    // System actions or Kudos ("XEM THIẾT BỊ", "CẬP NHẬT", "CHÚC MỪNG", "XEM CHI TIẾT")
                    btnPrimary.setBackgroundResource(R.drawable.hud_bg_bar_cyan)
                    btnPrimary.setTextColor(itemView.context.getColor(R.color.hud_canvas))
                    btnPrimary.setOnClickListener { onPrimaryActionClick(item, bindingAdapterPosition) }

                    if (item.secondaryActionText != null) {
                        btnSecondary.visibility = View.VISIBLE
                        btnSecondary.text = item.secondaryActionText
                        btnSecondary.setOnClickListener {
                            onSecondaryActionClick?.invoke(item, bindingAdapterPosition)
                                ?: onRejectClick(item, bindingAdapterPosition)
                        }
                    } else {
                        btnSecondary.visibility = View.GONE
                    }
                }
            } else {
                layoutActions.visibility = View.GONE
            }
        }
    }
}
