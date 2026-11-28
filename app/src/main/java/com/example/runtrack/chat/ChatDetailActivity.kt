package com.example.runtrack.chat

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runtrack.R
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.ApiConfig
import com.example.runtrack.network.model.chat.SendMessageRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var btnSend: FloatingActionButton
    private lateinit var messageAdapter: ChatMessageAdapter

    private var btnUnfriend: MaterialButton? = null
    private var tvNotFriendsBanner: TextView? = null

    private var conversationId: String? = null
    private var receiverId: Int? = null
    private var clubId: Int? = null
    private var partnerAvatarUrl: String? = null
    private var partnerName: String = "Người dùng"

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

        conversationId = intent.getStringExtra("EXTRA_CONVERSATION_ID")
        partnerAvatarUrl = intent.getStringExtra("EXTRA_AVATAR_URL")
        val rId = intent.getIntExtra("EXTRA_RECEIVER_ID", -1)
        if (rId != -1) receiverId = rId
        val cId = intent.getIntExtra("EXTRA_CLUB_ID", -1)
        if (cId != -1) clubId = cId

        partnerName = intent.getStringExtra("EXTRA_CHAT_NAME") ?: "Người dùng"
        val isGroup = intent.getBooleanExtra("EXTRA_IS_GROUP", false)
        val isOnline = intent.getBooleanExtra("EXTRA_IS_ONLINE", false)

        initHeader(partnerName, isGroup, isOnline)
        initMessagesRecyclerView()
        initInput()

        startMessagePolling()
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

        btnUnfriend = findViewById(R.id.btn_unfriend_chat)
        tvNotFriendsBanner = findViewById(R.id.tv_not_friends_banner)

        if (isGroup) {
            ivGroup?.visibility = View.VISIBLE
            ivAvatar?.visibility = View.GONE
            viewOnline?.visibility = View.GONE
            tvStatus?.text = "Nhóm chạy RunTrack"
            btnUnfriend?.visibility = View.GONE
        } else {
            ivGroup?.visibility = View.GONE
            ivAvatar?.visibility = View.VISIBLE

            if (receiverId != null) {
                btnUnfriend?.visibility = View.VISIBLE
                btnUnfriend?.setOnClickListener {
                    confirmUnfriend()
                }
            } else {
                btnUnfriend?.visibility = View.GONE
            }

            if (!partnerAvatarUrl.isNullOrBlank() && ivAvatar != null) {
                val fullUrl = if (partnerAvatarUrl!!.startsWith("http://") || partnerAvatarUrl!!.startsWith("https://")) {
                    partnerAvatarUrl!!
                } else {
                    ApiConfig.BASE_URL.trimEnd('/') + "/" + partnerAvatarUrl!!.trimStart('/')
                }
                Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_person_chat)
                    .error(R.drawable.ic_person_chat)
                    .circleCrop()
                    .into(ivAvatar)
            } else {
                ivAvatar?.setImageResource(R.drawable.ic_person_chat)
            }

            if (isOnline) {
                viewOnline?.visibility = View.VISIBLE
                tvStatus?.text = "Đang hoạt động"
            } else {
                viewOnline?.visibility = View.GONE
                tvStatus?.text = "Ngoại tuyến"
            }
        }
    }

    private fun initMessagesRecyclerView() {
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
                sendMessageToServer(text)
            }
        }
    }

    private fun loadMessagesFromServer(scrollToBottom: Boolean = false) {
        val convId = conversationId ?: return
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@ChatDetailActivity)
                        .getChatMessages(convId)
                }
                if (response.isSuccessful && response.body() != null) {
                    val dtos = response.body()!!
                    val newMessages = dtos.map { dto ->
                        ChatMessage(
                            id = dto.id,
                            text = dto.content,
                            time = dto.time,
                            isFromMe = dto.isFromMe,
                            senderAvatarUrl = dto.senderAvatarUrl
                        )
                    }

                    val isNewMessageAdded = newMessages.size > messageAdapter.itemCount
                    messageAdapter.updateList(newMessages)

                    if (scrollToBottom || isNewMessageAdded) {
                        if (messageAdapter.itemCount > 0) {
                            rvMessages.smoothScrollToPosition(messageAdapter.itemCount - 1)
                        }
                    }
                } else if (response.code() == 403) {
                    disableChatForNonFriends()
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun sendMessageToServer(content: String) {
        etInput.text.clear()
        lifecycleScope.launch {
            try {
                val req = SendMessageRequest(
                    conversationId = conversationId,
                    receiverId = receiverId,
                    clubId = clubId,
                    content = content
                )
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@ChatDetailActivity).sendMessage(req)
                }
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!
                    if (conversationId == null) {
                        conversationId = dto.conversationId
                    }
                    val sentMsg = ChatMessage(
                        id = dto.id,
                        text = dto.content,
                        time = dto.time,
                        isFromMe = true,
                        senderAvatarUrl = dto.senderAvatarUrl
                    )
                    messageAdapter.addMessage(sentMsg)
                    rvMessages.smoothScrollToPosition(messageAdapter.itemCount - 1)
                } else if (response.code() == 403) {
                    disableChatForNonFriends()
                    Toast.makeText(this@ChatDetailActivity, "Hai người chưa kết bạn nên không thể nhắn tin", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ChatDetailActivity, "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ChatDetailActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmUnfriend() {
        val targetId = receiverId ?: return
        AlertDialog.Builder(this)
            .setTitle("Hủy kết bạn")
            .setMessage("Bạn có chắc chắn muốn hủy kết bạn với $partnerName?")
            .setPositiveButton("Hủy kết bạn") { dialog, _ ->
                dialog.dismiss()
                performUnfriend(targetId)
            }
            .setNegativeButton("Quay lại", null)
            .show()
    }

    private fun performUnfriend(targetUserId: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@ChatDetailActivity).removeFriendship(targetUserId)
                }
                if (response.isSuccessful) {
                    Toast.makeText(this@ChatDetailActivity, "Đã hủy kết bạn với $partnerName", Toast.LENGTH_SHORT).show()
                    disableChatForNonFriends()
                } else {
                    Toast.makeText(this@ChatDetailActivity, "Hủy kết bạn thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ChatDetailActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun disableChatForNonFriends() {
        btnUnfriend?.visibility = View.GONE
        tvNotFriendsBanner?.visibility = View.VISIBLE
        etInput.isEnabled = false
        etInput.hint = "Bạn cần kết bạn để nhắn tin..."
        btnSend.isEnabled = false
        btnSend.alpha = 0.5f
    }

    private fun startMessagePolling() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    loadMessagesFromServer()
                    delay(3000)
                }
            }
        }
    }
}
