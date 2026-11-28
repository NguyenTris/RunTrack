package com.example.runtrack.chat

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R
import com.example.runtrack.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {

    private lateinit var rvChatList: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var etSearch: EditText
    private lateinit var tvEmptyState: TextView

    private lateinit var btnTabAll: TextView
    private lateinit var btnTabFriends: TextView
    private lateinit var btnTabGroups: TextView

    private var currentFilter = ChatFilterType.ALL
    private var currentQuery = ""
    private var allConversations = listOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val root = findViewById<View>(R.id.root_layout_chat)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        setupListeners()
        setupRecyclerView()
        loadConversationsFromServer()
    }

    override fun onResume() {
        super.onResume()
        loadConversationsFromServer()
    }

    private fun initViews() {
        rvChatList = findViewById(R.id.rv_chat_list)
        etSearch = findViewById(R.id.et_search_chat)
        tvEmptyState = findViewById(R.id.tv_empty_state)

        btnTabAll = findViewById(R.id.btn_tab_all)
        btnTabFriends = findViewById(R.id.btn_tab_friends)
        btnTabGroups = findViewById(R.id.btn_tab_groups)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        btnTabAll.setOnClickListener {
            setTabSelected(ChatFilterType.ALL)
        }

        btnTabFriends.setOnClickListener {
            setTabSelected(ChatFilterType.FRIENDS)
        }

        btnTabGroups.setOnClickListener {
            setTabSelected(ChatFilterType.GROUPS)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim().orEmpty()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        rvChatList.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(emptyList()) { item ->
            val intent = Intent(this, ChatDetailActivity::class.java).apply {
                putExtra("EXTRA_CONVERSATION_ID", item.id)
                putExtra("EXTRA_CHAT_NAME", item.name)
                putExtra("EXTRA_LAST_MSG", item.lastMessage)
                putExtra("EXTRA_IS_GROUP", item.isGroup)
                putExtra("EXTRA_IS_ONLINE", item.isOnline)
                item.receiverId?.let { putExtra("EXTRA_RECEIVER_ID", it) }
                item.clubId?.let { putExtra("EXTRA_CLUB_ID", it) }
            }
            startActivity(intent)
        }
        rvChatList.adapter = chatAdapter
    }

    private fun loadConversationsFromServer() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@ChatActivity).getConversations()
                }
                if (response.isSuccessful && response.body() != null) {
                    val dtos = response.body()!!
                    allConversations = dtos.map { dto ->
                        val category = if (dto.isGroup) ChatFilterType.GROUPS else ChatFilterType.FRIENDS
                        ChatItem(
                            id = dto.conversationId,
                            name = dto.name,
                            lastMessage = dto.lastMessage,
                            time = dto.time,
                            unreadCount = dto.unreadCount,
                            isOnline = dto.isOnline,
                            isGroup = dto.isGroup,
                            category = category,
                            receiverId = dto.receiverId,
                            clubId = dto.clubId
                        )
                    }
                    applyFilters()
                }
            } catch (_: Exception) {
                // Giữ nguyên danh sách hiện tại nếu ngoại tuyến
            }
        }
    }

    private fun setTabSelected(filterType: ChatFilterType) {
        if (currentFilter == filterType) return
        currentFilter = filterType

        if (filterType == ChatFilterType.ALL) {
            btnTabAll.background = ContextCompat.getDrawable(this, R.drawable.bg_chat_tab_selected)
            btnTabAll.setTextColor(Color.WHITE)
        } else {
            btnTabAll.background = ContextCompat.getDrawable(this, R.drawable.bg_chat_tab_unselected)
            btnTabAll.setTextColor(Color.parseColor("#AAAAAA"))
        }

        if (filterType == ChatFilterType.FRIENDS) {
            btnTabFriends.background = ContextCompat.getDrawable(this, R.drawable.bg_chat_tab_selected)
            btnTabFriends.setTextColor(Color.WHITE)
        } else {
            btnTabFriends.background = ContextCompat.getDrawable(this, R.drawable.bg_chat_tab_unselected)
            btnTabFriends.setTextColor(Color.parseColor("#AAAAAA"))
        }

        if (filterType == ChatFilterType.GROUPS) {
            btnTabGroups.background = ContextCompat.getDrawable(this, R.drawable.bg_chat_tab_selected)
            btnTabGroups.setTextColor(Color.WHITE)
        } else {
            btnTabGroups.background = ContextCompat.getDrawable(this, R.drawable.bg_chat_tab_unselected)
            btnTabGroups.setTextColor(Color.parseColor("#AAAAAA"))
        }

        applyFilters()
    }

    private fun applyFilters() {
        val filteredList = allConversations.filter { item ->
            val matchesCategory = when (currentFilter) {
                ChatFilterType.ALL -> true
                ChatFilterType.FRIENDS -> item.category == ChatFilterType.FRIENDS
                ChatFilterType.GROUPS -> item.category == ChatFilterType.GROUPS
            }

            val matchesQuery = if (currentQuery.isEmpty()) {
                true
            } else {
                item.name.contains(currentQuery, ignoreCase = true) ||
                        item.lastMessage.contains(currentQuery, ignoreCase = true)
            }

            matchesCategory && matchesQuery
        }

        chatAdapter.updateList(filteredList)

        if (filteredList.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            rvChatList.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            rvChatList.visibility = View.VISIBLE
        }
    }
}
