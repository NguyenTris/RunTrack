package com.example.runtrack.chat

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R

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

    private val mockChatList = listOf(
        ChatItem(
            id = "1",
            name = "Đức Huy",
            lastMessage = "Mình đang ở Hồ Tây, tham gia nhé?",
            time = "09:12",
            unreadCount = 2,
            isOnline = true,
            isGroup = false,
            category = ChatFilterType.FRIENDS
        ),
        ChatItem(
            id = "2",
            name = "Sunrise Runners",
            lastMessage = "Mai: 5:30 mai gặp ở Nhà Hát Lớn",
            time = "08:40",
            unreadCount = 5,
            isOnline = false,
            isGroup = true,
            category = ChatFilterType.GROUPS
        ),
        ChatItem(
            id = "3",
            name = "Ngọc Mai",
            lastMessage = "Cảm ơn bạn đã kết bạn nhé!",
            time = "07:55",
            unreadCount = 0,
            isOnline = true,
            isGroup = false,
            category = ChatFilterType.FRIENDS
        ),
        ChatItem(
            id = "4",
            name = "Hoàng Nam",
            lastMessage = "Đã gửi lộ trình Vòng Hồ Tây 10K",
            time = "07:30",
            unreadCount = 1,
            isOnline = true,
            isGroup = false,
            category = ChatFilterType.FRIENDS
        ),
        ChatItem(
            id = "5",
            name = "CLB Chạy Sài Gòn",
            lastMessage = "Lịch tập tuần này đã cập nhật",
            time = "HÔM QUA",
            unreadCount = 0,
            isOnline = false,
            isGroup = true,
            category = ChatFilterType.GROUPS
        ),
        ChatItem(
            id = "6",
            name = "Phan Thu Trang",
            lastMessage = "Chúc mừng bạn lên hạng #5!",
            time = "HÔM QUA",
            unreadCount = 0,
            isOnline = true,
            isGroup = false,
            category = ChatFilterType.FRIENDS
        ),
        ChatItem(
            id = "7",
            name = "Lê Quốc Bảo",
            lastMessage = "Cuối tuần chạy 10K không?",
            time = "T6",
            unreadCount = 0,
            isOnline = true,
            isGroup = false,
            category = ChatFilterType.FRIENDS
        )
    )

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
        applyFilters()
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
            val intent = android.content.Intent(this, ChatDetailActivity::class.java).apply {
                putExtra("EXTRA_CHAT_NAME", item.name)
                putExtra("EXTRA_LAST_MSG", item.lastMessage)
                putExtra("EXTRA_IS_GROUP", item.isGroup)
                putExtra("EXTRA_IS_ONLINE", item.isOnline)
            }
            startActivity(intent)
        }
        rvChatList.adapter = chatAdapter
    }

    private fun setTabSelected(filterType: ChatFilterType) {
        if (currentFilter == filterType) return
        currentFilter = filterType

        // Update All tab
        if (filterType == ChatFilterType.ALL) {
            btnTabAll.background = ContextCompat.getDrawable(this, R.drawable.bg_chat_tab_selected)
            btnTabAll.setTextColor(Color.WHITE)
        } else {
            btnTabAll.background = ContextCompat.getDrawable(this, R.drawable.bg_chat_tab_unselected)
            btnTabAll.setTextColor(Color.parseColor("#AAAAAA"))
        }

        // Update Friends tab
        if (filterType == ChatFilterType.FRIENDS) {
            btnTabFriends.background = ContextCompat.getDrawable(this, R.drawable.bg_chat_tab_selected)
            btnTabFriends.setTextColor(Color.WHITE)
        } else {
            btnTabFriends.background = ContextCompat.getDrawable(this, R.drawable.bg_chat_tab_unselected)
            btnTabFriends.setTextColor(Color.parseColor("#AAAAAA"))
        }

        // Update Groups tab
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
        val filteredList = mockChatList.filter { item ->
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
