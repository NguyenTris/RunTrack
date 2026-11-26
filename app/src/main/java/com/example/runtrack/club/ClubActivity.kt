package com.example.runtrack.club

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
import com.example.runtrack.chat.ChatDetailActivity
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.model.community.ClubDetailResponse
import com.example.runtrack.network.model.community.ClubEventItemDto
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ClubActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLUB_ID = "EXTRA_CLUB_ID"
    }

    private lateinit var tvTopTitle: TextView
    private lateinit var tvClubMainName: TextView
    private lateinit var tvClubMainSub: TextView
    private lateinit var btnTabOverview: TextView
    private lateinit var btnTabMembers: TextView
    private lateinit var btnTabEvents: TextView
    private lateinit var btnTabInvite: TextView

    private lateinit var layoutOverview: View
    private lateinit var layoutListContainer: View
    private lateinit var etSearch: EditText
    private lateinit var rvClubList: RecyclerView

    private var clubId: Int = 1
    private var clubName: String = "Sunrise Runners"
    private var currentTab = 0

    private var clubMembers: List<ClubMember> = emptyList()
    private var clubEvents: MutableList<ClubEvent> = mutableListOf()
    private val inviteFriends = listOf(
        ClubInviteFriend("i1", "Minh Anh", "Bạn bè • Hà Nội"),
        ClubInviteFriend("i2", "Thành Nam", "Bạn bè • Hà Nội"),
        ClubInviteFriend("i3", "Quỳnh Chi", "Bạn bè • Hà Nội")
    )

    private var memberAdapter: ClubMemberAdapter? = null
    private var eventAdapter: ClubEventAdapter? = null
    private var inviteAdapter: ClubInviteAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_club)

        clubId = intent.getIntExtra(EXTRA_CLUB_ID, 1)
        applyWindowInsets()
        initViews()
        setupListeners()
        selectTab(0)
        loadClubDetail()
    }

    private fun applyWindowInsets() {
        val root = findViewById<View>(R.id.root_activity_club) ?: return
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        tvTopTitle = findViewById(R.id.tv_club_top_title)
        tvClubMainName = findViewById(R.id.tv_club_main_name)
        tvClubMainSub = findViewById(R.id.tv_club_main_sub)
        btnTabOverview = findViewById(R.id.btn_tab_overview)
        btnTabMembers = findViewById(R.id.btn_tab_members)
        btnTabEvents = findViewById(R.id.btn_tab_events)
        btnTabInvite = findViewById(R.id.btn_tab_invite)

        layoutOverview = findViewById(R.id.layout_club_overview)
        layoutListContainer = findViewById(R.id.layout_club_list_container)
        etSearch = findViewById(R.id.et_club_search)
        rvClubList = findViewById(R.id.rv_club_list)
        rvClubList.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btn_club_back)?.setOnClickListener { finish() }
        btnTabOverview.setOnClickListener { selectTab(0) }
        btnTabMembers.setOnClickListener { selectTab(1) }
        btnTabEvents.setOnClickListener { selectTab(2) }
        btnTabInvite.setOnClickListener { selectTab(3) }

        findViewById<MaterialButton>(R.id.btn_club_chat)?.setOnClickListener {
            val intent = Intent(this, ChatDetailActivity::class.java).apply {
                putExtra("EXTRA_CHAT_NAME", clubName)
                putExtra("EXTRA_LAST_MSG", "Chào mừng bạn đến phòng chat $clubName")
                putExtra("EXTRA_IS_GROUP", true)
            }
            startActivity(intent)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s?.toString()?.trim().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadClubDetail() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@ClubActivity).getClubDetail(clubId)
                }
                if (response.isSuccessful && response.body() != null) {
                    bindClubDetail(response.body()!!)
                } else {
                    Toast.makeText(this@ClubActivity, "Không thể tải thông tin CLB", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(this@ClubActivity, "Lỗi kết nối máy chủ CLB", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindClubDetail(detail: ClubDetailResponse) {
        clubName = detail.name.ifBlank { clubName }
        tvClubMainName.text = clubName
        val descSuffix = if (detail.description.isNotBlank()) " • ${detail.description}" else ""
        tvClubMainSub.text = "${detail.member_count} thành viên$descSuffix"

        clubMembers = detail.members.map { m ->
            ClubMember(
                id = m.user_id.toString(),
                name = m.full_name,
                role = m.role,
                kmWeekly = String.format(Locale.US, "%.1f KM/tuần", m.weekly_distance_km)
            )
        }

        clubEvents = detail.events.map { it.toUiModel() }.toMutableList()
        selectTab(currentTab)
    }

    private fun ClubEventItemDto.toUiModel(): ClubEvent {
        return ClubEvent(
            id = event_id.toString(),
            title = title,
            time = event_time,
            location = location,
            registeredCount = "$registered_count người đã đăng ký",
            isJoined = is_registered
        )
    }

    private fun toggleEventRegister(event: ClubEvent) {
        val eventId = event.id.toIntOrNull() ?: return
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@ClubActivity).toggleClubEventRegister(eventId)
                }
                if (response.isSuccessful && response.body() != null) {
                    val updated = response.body()!!.toUiModel()
                    val index = clubEvents.indexOfFirst { it.id == updated.id }
                    if (index >= 0) clubEvents[index] = updated
                    eventAdapter?.updateList(clubEvents.toList())
                    val msg = if (updated.isJoined) "Đã đăng ký ${updated.title}" else "Đã hủy đăng ký ${updated.title}"
                    Toast.makeText(this@ClubActivity, msg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ClubActivity, "Thao tác sự kiện thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(this@ClubActivity, "Lỗi kết nối khi đăng ký sự kiện", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectTab(tabIndex: Int) {
        currentTab = tabIndex
        tvTopTitle.text = when (tabIndex) {
            0 -> "RunTrack - Nhóm - $clubName"
            1 -> "RunTrack - Nhóm - Thành viên"
            2 -> "RunTrack - Nhóm - Sự kiện"
            else -> "RunTrack - Nhóm - Mời bạn bè"
        }

        updateTabButtonUI(btnTabOverview, tabIndex == 0)
        updateTabButtonUI(btnTabMembers, tabIndex == 1)
        updateTabButtonUI(btnTabEvents, tabIndex == 2)
        updateTabButtonUI(btnTabInvite, tabIndex == 3)

        if (tabIndex == 0) {
            layoutOverview.visibility = View.VISIBLE
            layoutListContainer.visibility = View.GONE
            return
        }

        layoutOverview.visibility = View.GONE
        layoutListContainer.visibility = View.VISIBLE
        etSearch.text.clear()
        bindListTab(tabIndex)
    }

    private fun bindListTab(tabIndex: Int) {
        when (tabIndex) {
            1 -> {
                etSearch.hint = "Tìm kiếm thành viên trong nhóm..."
                memberAdapter = ClubMemberAdapter(clubMembers) { member ->
                    Toast.makeText(this, "Thành viên: ${member.name}", Toast.LENGTH_SHORT).show()
                }
                rvClubList.adapter = memberAdapter
            }
            2 -> {
                etSearch.hint = "Tìm kiếm sự kiện chạy..."
                eventAdapter = ClubEventAdapter(clubEvents.toList()) { event ->
                    toggleEventRegister(event)
                }
                rvClubList.adapter = eventAdapter
            }
            else -> {
                etSearch.hint = "Tìm kiếm bạn bè để mời..."
                inviteAdapter = ClubInviteAdapter(inviteFriends) { invite ->
                    invite.isInvited = !invite.isInvited
                    val msg = if (invite.isInvited) "Đã gửi lời mời tới ${invite.name}" else "Đã hủy lời mời"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    inviteAdapter?.notifyDataSetChanged()
                }
                rvClubList.adapter = inviteAdapter
            }
        }
    }

    private fun updateTabButtonUI(tv: TextView, isSelected: Boolean) {
        if (isSelected) {
            tv.background = ContextCompat.getDrawable(this, R.drawable.bg_search_tab_selected)
            tv.setTextColor(Color.parseColor("#121212"))
        } else {
            tv.background = ContextCompat.getDrawable(this, R.drawable.bg_search_tab_unselected)
            tv.setTextColor(Color.parseColor("#8A8A8A"))
        }
    }

    private fun filterList(query: String) {
        when (currentTab) {
            1 -> {
                val filtered = if (query.isEmpty()) clubMembers else clubMembers.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                memberAdapter?.updateList(filtered)
            }
            2 -> {
                val filtered = if (query.isEmpty()) clubEvents.toList() else clubEvents.filter {
                    it.title.contains(query, ignoreCase = true)
                }
                eventAdapter?.updateList(filtered)
            }
            3 -> {
                val filtered = if (query.isEmpty()) inviteFriends else inviteFriends.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                inviteAdapter?.updateList(filtered)
            }
        }
    }
}
