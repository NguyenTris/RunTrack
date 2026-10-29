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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R
import com.example.runtrack.chat.ChatDetailActivity
import com.google.android.material.button.MaterialButton

class ClubActivity : AppCompatActivity() {

    private lateinit var tvTopTitle: TextView
    private lateinit var btnTabOverview: TextView
    private lateinit var btnTabMembers: TextView
    private lateinit var btnTabEvents: TextView
    private lateinit var btnTabInvite: TextView

    private lateinit var layoutOverview: View
    private lateinit var layoutListContainer: View
    private lateinit var etSearch: EditText
    private lateinit var rvClubList: RecyclerView

    private var currentTab = 0

    private val mockMembers = listOf(
        ClubMember("m1", "Đức Huy", "Trưởng nhóm", "58 KM/tuần"),
        ClubMember("m2", "Ngọc Mai", "Quản trị viên", "42 KM/tuần"),
        ClubMember("m3", "Hoàng Nam", "Thành viên", "36 KM/tuần"),
        ClubMember("m4", "Phan Thu Trang", "Thành viên", "28 KM/tuần"),
        ClubMember("m5", "Lê Quốc Bảo", "Thành viên", "31 KM/tuần")
    )

    private val mockEvents = listOf(
        ClubEvent("e1", "Long Run Hồ Tây Cuối Tuần", "Chủ Nhật • 05:00 AM", "Phủ Tây Hồ • Cự ly 15K/21K", "128 người đã đăng ký"),
        ClubEvent("e2", "Chạy Bộ Đón Bình Minh", "Thứ 3 • 05:30 AM", "Công viên Thống Nhất • Cự ly 5K/10K", "84 người đã đăng ký")
    )

    private val mockInvites = listOf(
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

        val root = findViewById<View>(R.id.root_activity_club)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        setupListeners()
        selectTab(0)
    }

    private fun initViews() {
        tvTopTitle = findViewById(R.id.tv_club_top_title)
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
                putExtra("EXTRA_CHAT_NAME", "Sunrise Runners")
                putExtra("EXTRA_LAST_MSG", "Mai: 5:30 mai gặp ở Nhà Hát Lớn")
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

    private fun selectTab(tabIndex: Int) {
        currentTab = tabIndex

        tvTopTitle.text = when (tabIndex) {
            0 -> "RunTrack - Nhóm - Sunrise Runners"
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
        } else {
            layoutOverview.visibility = View.GONE
            layoutListContainer.visibility = View.VISIBLE
            etSearch.text.clear()

            when (tabIndex) {
                1 -> {
                    etSearch.hint = "Tìm kiếm thành viên trong nhóm..."
                    memberAdapter = ClubMemberAdapter(mockMembers) { member ->
                        Toast.makeText(this, "Thành viên: ${member.name}", Toast.LENGTH_SHORT).show()
                    }
                    rvClubList.adapter = memberAdapter
                }
                2 -> {
                    etSearch.hint = "Tìm kiếm sự kiện chạy..."
                    eventAdapter = ClubEventAdapter(mockEvents) { event ->
                        event.isJoined = !event.isJoined
                        Toast.makeText(this, if (event.isJoined) "Đã đăng ký ${event.title}" else "Đã hủy đăng ký", Toast.LENGTH_SHORT).show()
                        eventAdapter?.notifyDataSetChanged()
                    }
                    rvClubList.adapter = eventAdapter
                }
                3 -> {
                    etSearch.hint = "Tìm kiếm bạn bè để mời..."
                    inviteAdapter = ClubInviteAdapter(mockInvites) { invite ->
                        invite.isInvited = !invite.isInvited
                        Toast.makeText(this, if (invite.isInvited) "Đã gửi lời mời tới ${invite.name}" else "Đã hủy lời mời", Toast.LENGTH_SHORT).show()
                        inviteAdapter?.notifyDataSetChanged()
                    }
                    rvClubList.adapter = inviteAdapter
                }
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
                val filtered = if (query.isEmpty()) mockMembers else mockMembers.filter { it.name.contains(query, ignoreCase = true) }
                memberAdapter?.updateList(filtered)
            }
            2 -> {
                val filtered = if (query.isEmpty()) mockEvents else mockEvents.filter { it.title.contains(query, ignoreCase = true) }
                eventAdapter?.updateList(filtered)
            }
            3 -> {
                val filtered = if (query.isEmpty()) mockInvites else mockInvites.filter { it.name.contains(query, ignoreCase = true) }
                inviteAdapter?.updateList(filtered)
            }
        }
    }
}
