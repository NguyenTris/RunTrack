package com.example.runtrack.search

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

class SearchActivity : AppCompatActivity() {

    private lateinit var tvTopTitle: TextView
    private lateinit var etQuery: EditText
    private lateinit var btnClear: ImageView
    private lateinit var rvResults: RecyclerView
    private lateinit var tvEmpty: TextView

    private lateinit var btnTabAll: TextView
    private lateinit var btnTabFriends: TextView
    private lateinit var btnTabClubs: TextView

    private var currentTab = SearchType.ALL
    private var currentQuery = ""

    private var searchAdapter: SearchAdapter? = null

    private val mockSearchList = listOf(
        SearchItem(
            id = "f1",
            name = "Đức Huy",
            subtitle = "128 KM tháng này • Hà Nội",
            type = SearchType.FRIEND
        ),
        SearchItem(
            id = "f2",
            name = "Ngọc Mai",
            subtitle = "84 KM tháng này • TP. Hồ Chí Minh",
            type = SearchType.FRIEND
        ),
        SearchItem(
            id = "c1",
            name = "Sunrise Runners",
            subtitle = "1,420 thành viên • Hà Nội",
            type = SearchType.CLUB
        ),
        SearchItem(
            id = "f3",
            name = "Hoàng Nam",
            subtitle = "156 KM tháng này • Đà Nẵng",
            type = SearchType.FRIEND
        ),
        SearchItem(
            id = "c2",
            name = "CLB Chạy Sài Gòn",
            subtitle = "2,850 thành viên • TP. Hồ Chí Minh",
            type = SearchType.CLUB
        ),
        SearchItem(
            id = "f4",
            name = "Phan Thu Trang",
            subtitle = "92 KM tháng này • Cần Thơ",
            type = SearchType.FRIEND
        ),
        SearchItem(
            id = "c3",
            name = "VietRunners Club",
            subtitle = "3,100 thành viên • Toàn Quốc",
            type = SearchType.CLUB
        ),
        SearchItem(
            id = "f5",
            name = "Lê Quốc Bảo",
            subtitle = "110 KM tháng này • Hải Phòng",
            type = SearchType.FRIEND
        ),
        SearchItem(
            id = "c4",
            name = "Sông Hàn Runners",
            subtitle = "890 thành viên • Đà Nẵng",
            type = SearchType.CLUB
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val root = findViewById<View>(R.id.root_activity_search)
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
        updateTabSelection(SearchType.ALL)
    }

    private fun initViews() {
        tvTopTitle = findViewById(R.id.tv_search_top_title)
        etQuery = findViewById(R.id.et_search_query)
        btnClear = findViewById(R.id.btn_clear_search)
        rvResults = findViewById(R.id.rv_search_results)
        tvEmpty = findViewById(R.id.tv_search_empty)

        btnTabAll = findViewById(R.id.btn_tab_all)
        btnTabFriends = findViewById(R.id.btn_tab_friends)
        btnTabClubs = findViewById(R.id.btn_tab_clubs)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btn_search_back)?.setOnClickListener {
            finish()
        }

        btnClear.setOnClickListener {
            etQuery.text.clear()
        }

        btnTabAll.setOnClickListener {
            updateTabSelection(SearchType.ALL)
        }

        btnTabFriends.setOnClickListener {
            updateTabSelection(SearchType.FRIEND)
        }

        btnTabClubs.setOnClickListener {
            updateTabSelection(SearchType.CLUB)
        }

        etQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim().orEmpty()
                btnClear.visibility = if (currentQuery.isNotEmpty()) View.VISIBLE else View.GONE
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        rvResults.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchAdapter(
            searchList = emptyList(),
            onItemClick = { item ->
                val intent = android.content.Intent(this, SearchDetailActivity::class.java).apply {
                    putExtra("EXTRA_ITEM_ID", item.id)
                    putExtra("EXTRA_ITEM_NAME", item.name)
                    putExtra("EXTRA_ITEM_SUBTITLE", item.subtitle)
                    putExtra("EXTRA_ITEM_TYPE", item.type.name)
                }
                startActivity(intent)
            },
            onActionClick = { item ->
                item.isActionDone = !item.isActionDone
                val msg = if (item.isActionDone) {
                    if (item.type == SearchType.CLUB) "Đã tham gia ${item.name}" else "Đã gửi lời mời kết bạn đến ${item.name}"
                } else {
                    if (item.type == SearchType.CLUB) "Đã rời ${item.name}" else "Đã hủy lời mời kết bạn"
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                applyFilters()
            }
        )
        rvResults.adapter = searchAdapter
    }

    private fun updateTabSelection(type: SearchType) {
        currentTab = type

        // Update Top Bar Title
        tvTopTitle.text = when (type) {
            SearchType.ALL -> "RunTrack - Tìm kiếm bạn bè & CLB"
            SearchType.FRIEND -> "RunTrack - Tìm kiếm - Bạn bè"
            SearchType.CLUB -> "RunTrack - Tìm kiếm - CLB"
        }

        // Update Tab Buttons UI (#F4C073 selected, #1E1E1E unselected)
        btnTabAll.background = ContextCompat.getDrawable(this, if (type == SearchType.ALL) R.drawable.bg_search_tab_selected else R.drawable.bg_search_tab_unselected)
        btnTabAll.setTextColor(if (type == SearchType.ALL) Color.parseColor("#121212") else Color.parseColor("#8A8A8A"))

        btnTabFriends.background = ContextCompat.getDrawable(this, if (type == SearchType.FRIEND) R.drawable.bg_search_tab_selected else R.drawable.bg_search_tab_unselected)
        btnTabFriends.setTextColor(if (type == SearchType.FRIEND) Color.parseColor("#121212") else Color.parseColor("#8A8A8A"))

        btnTabClubs.background = ContextCompat.getDrawable(this, if (type == SearchType.CLUB) R.drawable.bg_search_tab_selected else R.drawable.bg_search_tab_unselected)
        btnTabClubs.setTextColor(if (type == SearchType.CLUB) Color.parseColor("#121212") else Color.parseColor("#8A8A8A"))

        applyFilters()
    }

    private fun applyFilters() {
        val filtered = mockSearchList.filter { item ->
            val matchesTab = when (currentTab) {
                SearchType.ALL -> true
                SearchType.FRIEND -> item.type == SearchType.FRIEND
                SearchType.CLUB -> item.type == SearchType.CLUB
            }

            val matchesQuery = if (currentQuery.isEmpty()) {
                true
            } else {
                item.name.contains(currentQuery, ignoreCase = true) ||
                        item.subtitle.contains(currentQuery, ignoreCase = true)
            }

            matchesTab && matchesQuery
        }

        searchAdapter?.updateList(filtered)

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvResults.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvResults.visibility = View.VISIBLE
        }
    }
}
