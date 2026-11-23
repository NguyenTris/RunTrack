package com.example.runtrack.search

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.model.search.ClubResponse
import com.example.runtrack.network.model.search.FriendResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private var searchJob: Job? = null
    private var searchAdapter: SearchAdapter? = null
    private val searchResults = mutableListOf<SearchItem>()

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
        findViewById<ImageView>(R.id.btn_search_back)?.setOnClickListener { finish() }
        btnClear.setOnClickListener { etQuery.text.clear() }

        btnTabAll.setOnClickListener { updateTabSelection(SearchType.ALL) }
        btnTabFriends.setOnClickListener { updateTabSelection(SearchType.FRIEND) }
        btnTabClubs.setOnClickListener { updateTabSelection(SearchType.CLUB) }

        etQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim().orEmpty()
                btnClear.visibility = if (currentQuery.isNotEmpty()) View.VISIBLE else View.GONE
                scheduleSearch(debounceMs = 300L)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        rvResults.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchAdapter(
            searchList = emptyList(),
            onItemClick = { item -> openSearchDetail(item) },
            onActionClick = { item -> handleActionClick(item) }
        )
        rvResults.adapter = searchAdapter
    }

    private fun openSearchDetail(item: SearchItem) {
        val intent = Intent(this, SearchDetailActivity::class.java).apply {
            putExtra("EXTRA_ITEM_ID", item.id)
            putExtra("EXTRA_ITEM_NAME", item.name)
            putExtra("EXTRA_ITEM_SUBTITLE", item.subtitle)
            putExtra("EXTRA_ITEM_TYPE", item.type.name)
        }
        startActivity(intent)
    }

    private fun handleActionClick(item: SearchItem) {
        if (item.type == SearchType.CLUB) {
            val clubId = item.id.toIntOrNull() ?: return
            toggleClubMembership(clubId)
            return
        }

        item.isActionDone = !item.isActionDone
        val msg = if (item.isActionDone) {
            "Đã gửi lời mời kết bạn đến ${item.name}"
        } else {
            "Đã hủy lời mời kết bạn"
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        renderResults()
    }

    private fun toggleClubMembership(clubId: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@SearchActivity).toggleJoinClub(clubId)
                }
                val updated = response.body()
                if (response.isSuccessful && updated != null) {
                    applyClubUpdate(updated)
                } else {
                    Toast.makeText(
                        this@SearchActivity,
                        "Không thể cập nhật trạng thái CLB (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: Exception) {
                Toast.makeText(
                    this@SearchActivity,
                    "Lỗi mạng. Không thể cập nhật trạng thái CLB.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun applyClubUpdate(club: ClubResponse) {
        val updatedItem = mapClubToSearchItem(club)
        val index = searchResults.indexOfFirst {
            it.type == SearchType.CLUB && it.id == updatedItem.id
        }
        if (index >= 0) {
            searchResults[index] = updatedItem
        }
        renderResults()
        val msg = if (club.is_joined) "Đã tham gia ${club.name}" else "Đã rời ${club.name}"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun updateTabSelection(type: SearchType) {
        currentTab = type

        tvTopTitle.text = when (type) {
            SearchType.ALL -> "RunTrack - Tìm kiếm bạn bè & CLB"
            SearchType.FRIEND -> "RunTrack - Tìm kiếm - Bạn bè"
            SearchType.CLUB -> "RunTrack - Tìm kiếm - CLB"
        }

        styleTabButton(btnTabAll, type == SearchType.ALL)
        styleTabButton(btnTabFriends, type == SearchType.FRIEND)
        styleTabButton(btnTabClubs, type == SearchType.CLUB)

        scheduleSearch(debounceMs = 0L)
    }

    private fun styleTabButton(button: TextView, selected: Boolean) {
        val bgRes = if (selected) R.drawable.bg_search_tab_selected else R.drawable.bg_search_tab_unselected
        button.background = ContextCompat.getDrawable(this, bgRes)
        button.setTextColor(if (selected) Color.parseColor("#121212") else Color.parseColor("#8A8A8A"))
    }

    private fun scheduleSearch(debounceMs: Long) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            if (debounceMs > 0L) {
                delay(debounceMs)
            }
            fetchSearchResults()
        }
    }

    private suspend fun fetchSearchResults() {
        val query = currentQuery
        val tab = currentTab
        try {
            val items = withContext(Dispatchers.IO) { loadRemoteItems(query, tab) }
            searchResults.clear()
            searchResults.addAll(items)
            renderResults(emptyMessage = "Không tìm thấy kết quả phù hợp")
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            searchResults.clear()
            renderResults(emptyMessage = "Không thể kết nối máy chủ. Vui lòng thử lại.")
        }
    }

    private suspend fun loadRemoteItems(query: String, tab: SearchType): List<SearchItem> {
        val service = ApiClient.getAuthenticatedService(this@SearchActivity)
        val combined = mutableListOf<SearchItem>()

        if (tab == SearchType.ALL || tab == SearchType.FRIEND) {
            val friendRes = service.searchFriends(q = query)
            if (!friendRes.isSuccessful) {
                throw IllegalStateException("Friends search failed: ${friendRes.code()}")
            }
            friendRes.body().orEmpty().mapTo(combined) { mapFriendToSearchItem(it) }
        }

        if (tab == SearchType.ALL || tab == SearchType.CLUB) {
            val clubRes = service.searchClubs(q = query)
            if (!clubRes.isSuccessful) {
                throw IllegalStateException("Clubs search failed: ${clubRes.code()}")
            }
            clubRes.body().orEmpty().mapTo(combined) { mapClubToSearchItem(it) }
        }

        return combined
    }

    private fun mapFriendToSearchItem(friend: FriendResponse): SearchItem {
        val subtitle = "${friend.mutual_friends} bạn chung"
        return SearchItem(
            id = "friend_${friend.user_id}",
            name = friend.full_name,
            subtitle = subtitle,
            type = SearchType.FRIEND,
            isActionDone = friend.friend_status != "none"
        )
    }

    private fun mapClubToSearchItem(club: ClubResponse): SearchItem {
        val desc = club.description.trim()
        val subtitle = if (desc.isNotEmpty()) {
            "${club.member_count} thành viên • $desc"
        } else {
            "${club.member_count} thành viên"
        }
        return SearchItem(
            id = club.club_id.toString(),
            name = club.name,
            subtitle = subtitle,
            type = SearchType.CLUB,
            isActionDone = club.is_joined
        )
    }

    private fun renderResults(emptyMessage: String = "Không tìm thấy kết quả phù hợp") {
        searchAdapter?.updateList(searchResults.toList())
        if (searchResults.isEmpty()) {
            tvEmpty.text = emptyMessage
            tvEmpty.visibility = View.VISIBLE
            rvResults.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvResults.visibility = View.VISIBLE
        }
    }
}
