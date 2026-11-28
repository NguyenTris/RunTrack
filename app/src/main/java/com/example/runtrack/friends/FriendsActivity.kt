package com.example.runtrack.friends

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R
import com.example.runtrack.chat.ChatDetailActivity
import com.example.runtrack.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendsActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var tvHeaderSearch: TextView
    private lateinit var rvSearch: RecyclerView
    private lateinit var searchAdapter: FriendsAdapter

    private lateinit var tvEmptyRequests: TextView
    private lateinit var rvRequests: RecyclerView
    private lateinit var requestsAdapter: FriendsAdapter

    private lateinit var tvEmptyFriends: TextView
    private lateinit var rvFriends: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter

    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friends)

        val root = findViewById<View>(R.id.root_layout_friends)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        setupListeners()
        setupRecyclerViews()
        loadFriendsOverview()
    }

    private fun initViews() {
        findViewById<ImageView>(R.id.btn_back_friends)?.setOnClickListener { finish() }

        etSearch = findViewById(R.id.et_search_friends)

        tvHeaderSearch = findViewById(R.id.tv_header_search_results)
        rvSearch = findViewById(R.id.rv_search_results)

        tvEmptyRequests = findViewById(R.id.tv_empty_friend_requests)
        rvRequests = findViewById(R.id.rv_friend_requests)

        tvEmptyFriends = findViewById(R.id.tv_empty_current_friends)
        rvFriends = findViewById(R.id.rv_current_friends)
    }

    private fun setupListeners() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                searchJob?.cancel()
                if (query.isNotEmpty()) {
                    searchJob = lifecycleScope.launch {
                        delay(300)
                        performSearch(query)
                    }
                } else {
                    tvHeaderSearch.visibility = View.GONE
                    rvSearch.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerViews() {
        // Search adapter
        searchAdapter = FriendsAdapter(
            items = emptyList(),
            onPrimaryAction = { item -> handlePrimaryAction(item) },
            onSecondaryAction = { item -> handleSecondaryAction(item) }
        )
        rvSearch.layoutManager = LinearLayoutManager(this)
        rvSearch.adapter = searchAdapter

        // Requests adapter
        requestsAdapter = FriendsAdapter(
            items = emptyList(),
            onPrimaryAction = { item -> handlePrimaryAction(item) },
            onSecondaryAction = { item -> handleSecondaryAction(item) }
        )
        rvRequests.layoutManager = LinearLayoutManager(this)
        rvRequests.adapter = requestsAdapter

        // Current Friends adapter
        friendsAdapter = FriendsAdapter(
            items = emptyList(),
            onPrimaryAction = { item -> handlePrimaryAction(item) },
            onSecondaryAction = { item -> handleSecondaryAction(item) }
        )
        rvFriends.layoutManager = LinearLayoutManager(this)
        rvFriends.adapter = friendsAdapter
    }

    private fun loadFriendsOverview() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@FriendsActivity).getFriendsOverview()
                }
                if (response.isSuccessful && response.body() != null) {
                    val overview = response.body()!!

                    val requestItems = overview.pendingReceived.map { u ->
                        FriendDisplayItem(
                            id = u.id,
                            fullName = u.fullName,
                            email = u.email,
                            avatarUrl = u.avatarUrl,
                            friendStatus = "pending_received",
                            itemType = FriendItemType.REQUEST
                        )
                    }
                    requestsAdapter.updateList(requestItems)
                    tvEmptyRequests.visibility = if (requestItems.isEmpty()) View.VISIBLE else View.GONE
                    rvRequests.visibility = if (requestItems.isEmpty()) View.GONE else View.VISIBLE

                    val friendItems = overview.friends.map { u ->
                        FriendDisplayItem(
                            id = u.id,
                            fullName = u.fullName,
                            email = u.email,
                            avatarUrl = u.avatarUrl,
                            friendStatus = "accepted",
                            itemType = FriendItemType.FRIEND
                        )
                    }
                    friendsAdapter.updateList(friendItems)
                    tvEmptyFriends.visibility = if (friendItems.isEmpty()) View.VISIBLE else View.GONE
                    rvFriends.visibility = if (friendItems.isEmpty()) View.GONE else View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendsActivity, "Không thể tải danh sách bạn bè", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@FriendsActivity).searchFriendsUsers(query)
                }
                if (response.isSuccessful && response.body() != null) {
                    val searchUsers = response.body()!!
                    val searchItems = searchUsers.map { u ->
                        FriendDisplayItem(
                            id = u.id,
                            fullName = u.fullName,
                            email = u.email,
                            avatarUrl = u.avatarUrl,
                            friendStatus = u.friendStatus,
                            itemType = FriendItemType.SEARCH_RESULT
                        )
                    }
                    searchAdapter.updateList(searchItems)
                    tvHeaderSearch.visibility = View.VISIBLE
                    rvSearch.visibility = View.VISIBLE
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun handlePrimaryAction(item: FriendDisplayItem) {
        when (item.itemType) {
            FriendItemType.FRIEND -> {
                // Nhắn tin -> Mở ChatDetailActivity
                val intent = Intent(this, ChatDetailActivity::class.java).apply {
                    putExtra("EXTRA_CHAT_NAME", item.fullName)
                    putExtra("EXTRA_RECEIVER_ID", item.id)
                    putExtra("EXTRA_AVATAR_URL", item.avatarUrl)
                    putExtra("EXTRA_IS_GROUP", false)
                    putExtra("EXTRA_IS_ONLINE", true)
                }
                startActivity(intent)
            }
            FriendItemType.REQUEST -> {
                // Chấp nhận lời mời
                acceptFriendRequest(item.id)
            }
            FriendItemType.SEARCH_RESULT -> {
                when (item.friendStatus) {
                    "none" -> sendFriendRequest(item.id)
                    "pending_received" -> acceptFriendRequest(item.id)
                }
            }
        }
    }

    private fun handleSecondaryAction(item: FriendDisplayItem) {
        when (item.itemType) {
            FriendItemType.FRIEND -> {
                // Hủy kết bạn với dialog xác nhận
                confirmUnfriend(item)
            }
            FriendItemType.REQUEST -> {
                // Từ chối lời mời
                removeFriendship(item.id, "Đã từ chối lời mời kết bạn")
            }
            FriendItemType.SEARCH_RESULT -> {
                if (item.friendStatus == "pending_sent" || item.friendStatus == "pending_received") {
                    removeFriendship(item.id, "Đã hủy lời mời kết bạn")
                }
            }
        }
    }

    private fun sendFriendRequest(targetUserId: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@FriendsActivity).sendFriendRequest(targetUserId)
                }
                if (response.isSuccessful) {
                    Toast.makeText(this@FriendsActivity, response.body()?.message ?: "Đã gửi lời mời", Toast.LENGTH_SHORT).show()
                    loadFriendsOverview()
                    val currentQuery = etSearch.text.toString().trim()
                    if (currentQuery.isNotEmpty()) performSearch(currentQuery)
                } else {
                    Toast.makeText(this@FriendsActivity, "Gửi lời mời thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendsActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun acceptFriendRequest(requesterId: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@FriendsActivity).acceptFriendRequest(requesterId)
                }
                if (response.isSuccessful) {
                    Toast.makeText(this@FriendsActivity, "Đã chấp nhận lời mời kết bạn", Toast.LENGTH_SHORT).show()
                    loadFriendsOverview()
                    val currentQuery = etSearch.text.toString().trim()
                    if (currentQuery.isNotEmpty()) performSearch(currentQuery)
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendsActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmUnfriend(item: FriendDisplayItem) {
        AlertDialog.Builder(this)
            .setTitle("Hủy kết bạn")
            .setMessage("Bạn có chắc chắn muốn hủy kết bạn với ${item.fullName}?")
            .setPositiveButton("Hủy kết bạn") { dialog, _ ->
                dialog.dismiss()
                removeFriendship(item.id, "Đã hủy kết bạn với ${item.fullName}")
            }
            .setNegativeButton("Quay lại", null)
            .show()
    }

    private fun removeFriendship(targetUserId: Int, successMessage: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@FriendsActivity).removeFriendship(targetUserId)
                }
                if (response.isSuccessful) {
                    Toast.makeText(this@FriendsActivity, successMessage, Toast.LENGTH_SHORT).show()
                    loadFriendsOverview()
                    val currentQuery = etSearch.text.toString().trim()
                    if (currentQuery.isNotEmpty()) performSearch(currentQuery)
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendsActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
