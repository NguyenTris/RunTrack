package com.example.runtrack.chat

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var btnSend: FloatingActionButton
    private lateinit var messageAdapter: ChatMessageAdapter

    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_detail)

        val root = findViewById<View>(R.id.root_chat_detail)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val name = intent.getStringExtra("EXTRA_CHAT_NAME") ?: "Người dùng"
        val lastMsg = intent.getStringExtra("EXTRA_LAST_MSG") ?: ""
        val isGroup = intent.getBooleanExtra("EXTRA_IS_GROUP", false)
        val isOnline = intent.getBooleanExtra("EXTRA_IS_ONLINE", false)

        initHeader(name, isGroup, isOnline)
        initMessages(lastMsg)
        initInput()
    }

    private fun initHeader(name: String, isGroup: Boolean, isOnline: Boolean) {
        findViewById<ImageView>(R.id.btn_detail_back)?.setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.tv_detail_name)?.text = name

        val tvStatus = findViewById<TextView>(R.id.tv_detail_status)
        val viewOnline = findViewById<View>(R.id.view_detail_online_dot)
        val ivGroup = findViewById<ImageView>(R.id.iv_detail_group_icon)
        val ivAvatar = findViewById<ShapeableImageView>(R.id.iv_detail_avatar)

        if (isGroup) {
            ivGroup?.visibility = View.VISIBLE
            ivAvatar?.visibility = View.GONE
            viewOnline?.visibility = View.GONE
            tvStatus?.text = "Nhóm chạy RunTrack"
        } else {
            ivGroup?.visibility = View.GONE
            ivAvatar?.visibility = View.VISIBLE
            ivAvatar?.setImageResource(R.drawable.ic_person_chat)
            if (isOnline) {
                viewOnline?.visibility = View.VISIBLE
                tvStatus?.text = "Đang hoạt động"
            } else {
                viewOnline?.visibility = View.GONE
                tvStatus?.text = "Ngoại tuyến"
            }
        }
    }

    private fun initMessages(lastMsg: String) {
        messages.add(
            ChatMessage(
                id = "1",
                text = "Chào bạn! Rất vui được kết nối trên RunTrack.",
                time = "09:00",
                isFromMe = false
            )
        )

        if (lastMsg.isNotEmpty()) {
            messages.add(
                ChatMessage(
                    id = "2",
                    text = lastMsg,
                    time = "09:12",
                    isFromMe = false
                )
            )
        }

        rvMessages = findViewById(R.id.rv_detail_messages)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        rvMessages.layoutManager = layoutManager

        messageAdapter = ChatMessageAdapter(messages)
        rvMessages.adapter = messageAdapter
    }

    private fun initInput() {
        etInput = findViewById(R.id.et_message_input)
        btnSend = findViewById(R.id.btn_send_message)

        btnSend.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isNotEmpty()) {
                val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val newMsg = ChatMessage(
                    id = System.currentTimeMillis().toString(),
                    text = text,
                    time = timeNow,
                    isFromMe = true
                )
                messageAdapter.addMessage(newMsg)
                etInput.text.clear()
                rvMessages.smoothScrollToPosition(messageAdapter.itemCount - 1)
            }
        }
    }
}
