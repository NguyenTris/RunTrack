package com.example.runtrack.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R

class ChatMessageAdapter(
    private val messagesList: MutableList<ChatMessage>
) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutReceived: LinearLayout = itemView.findViewById(R.id.layout_received_message)
        val tvReceivedText: TextView = itemView.findViewById(R.id.tv_received_text)
        val tvReceivedTime: TextView = itemView.findViewById(R.id.tv_received_time)

        val layoutSent: LinearLayout = itemView.findViewById(R.id.layout_sent_message)
        val tvSentText: TextView = itemView.findViewById(R.id.tv_sent_text)
        val tvSentTime: TextView = itemView.findViewById(R.id.tv_sent_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_bubble, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messagesList[position]

        if (message.isFromMe) {
            holder.layoutSent.visibility = View.VISIBLE
            holder.layoutReceived.visibility = View.GONE
            holder.tvSentText.text = message.text
            holder.tvSentTime.text = message.time
        } else {
            holder.layoutReceived.visibility = View.VISIBLE
            holder.layoutSent.visibility = View.GONE
            holder.tvReceivedText.text = message.text
            holder.tvReceivedTime.text = message.time
        }
    }

    override fun getItemCount(): Int = messagesList.size

    fun addMessage(message: ChatMessage) {
        messagesList.add(message)
        notifyItemInserted(messagesList.size - 1)
    }
}
