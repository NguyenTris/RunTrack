package com.example.runtrack

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var rvRanks: RecyclerView
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var btnTabGlobal: MaterialButton
    private lateinit var btnTabFriends: MaterialButton

    private lateinit var tvRunnersCountPodium: TextView
    private lateinit var tvNameRank1: TextView
    private lateinit var tvTimeRank1: TextView
    private lateinit var tvNameRank2: TextView
    private lateinit var tvTimeRank2: TextView
    private lateinit var tvNameRank3: TextView
    private lateinit var tvTimeRank3: TextView
    private lateinit var tvUserRankRatio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_leaderboard)

        val root = findViewById<android.view.View>(R.id.root_layout_leaderboard) ?: findViewById(R.id.root_layout_leaderboard_land)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        setupListeners()
        loadGlobalData()
    }

    private fun initViews() {
        rvRanks = findViewById(R.id.rv_leaderboard_ranks)
        rvRanks.layoutManager = LinearLayoutManager(this)

        toggleGroup = findViewById(R.id.toggle_group_leaderboard)
        btnTabGlobal = findViewById(R.id.btn_tab_global)
        btnTabFriends = findViewById(R.id.btn_tab_friends)

        tvRunnersCountPodium = findViewById(R.id.tv_runners_count_podium)
        tvNameRank1 = findViewById(R.id.tv_name_rank1)
        tvTimeRank1 = findViewById(R.id.tv_time_rank1)
        tvNameRank2 = findViewById(R.id.tv_name_rank2)
        tvTimeRank2 = findViewById(R.id.tv_time_rank2)
        tvNameRank3 = findViewById(R.id.tv_name_rank3)
        tvTimeRank3 = findViewById(R.id.tv_time_rank3)
        tvUserRankRatio = findViewById(R.id.tv_user_rank_ratio)
    }

    private fun setupListeners() {
        val tvBtnHome = findViewById<TextView>(R.id.tv_btn_home)
        tvBtnHome?.setOnClickListener { finish() }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btn_tab_global) {
                    setTabStyle(btnTabGlobal, isSelected = true)
                    setTabStyle(btnTabFriends, isSelected = false)
                    loadGlobalData()
                } else if (checkedId == R.id.btn_tab_friends) {
                    setTabStyle(btnTabFriends, isSelected = true)
                    setTabStyle(btnTabGlobal, isSelected = false)
                    loadFriendsData()
                }
            }
        }
    }

    private fun setTabStyle(button: MaterialButton, isSelected: Boolean) {
        val orangeColor = ContextCompat.getColor(this, R.color.kinetic_orange)
        val surfaceColor = ContextCompat.getColor(this, R.color.surface_card)
        val whiteColor = ContextCompat.getColor(this, R.color.white)
        val secondaryColor = ContextCompat.getColor(this, R.color.text_secondary)

        if (isSelected) {
            button.setBackgroundColor(orangeColor)
            button.setTextColor(whiteColor)
        } else {
            button.setBackgroundColor(surfaceColor)
            button.setTextColor(secondaryColor)
        }
    }

    private fun loadGlobalData() {
        tvRunnersCountPodium.text = "1.248 người chạy"
        tvNameRank1.text = "Minh Khoa"
        tvTimeRank1.text = "16:42"
        tvNameRank2.text = "Quốc An"
        tvTimeRank2.text = "17:08"
        tvNameRank3.text = "Thùy Linh"
        tvTimeRank3.text = "17:35"
        tvUserRankRatio.text = "#128 / 1.248"

        val ranksList = listOf(
            LeaderboardRunner("04", "Phạm Đức Huy", "Hà Nội", "18:36", R.drawable.ic_nav_profile),
            LeaderboardRunner("05", "Vũ Ngọc Mai", "TP. Hồ Chí Minh", "19:12", R.drawable.ic_nav_profile),
            LeaderboardRunner("06", "Đỗ Hoàng Nam", "Đà Nẵng", "20:04", R.drawable.ic_nav_profile),
            LeaderboardRunner("07", "Bùi Tường Vy", "Hà Nội", "21:26", R.drawable.ic_nav_profile),
            LeaderboardRunner("08", "Nguyễn Gia Phúc", "Huế", "22:10", R.drawable.ic_nav_profile)
        )

        leaderboardAdapter = LeaderboardAdapter(ranksList) { runner ->
            Toast.makeText(this, "Chọn: ${runner.name}", Toast.LENGTH_SHORT).show()
        }
        rvRanks.adapter = leaderboardAdapter
    }

    private fun loadFriendsData() {
        tvRunnersCountPodium.text = "18 người chạy"
        tvNameRank1.text = "Đức Huy"
        tvTimeRank1.text = "18:36"
        tvNameRank2.text = "Ngọc Mai"
        tvTimeRank2.text = "19:12"
        tvNameRank3.text = "Hoàng Nam"
        tvTimeRank3.text = "20:04"
        tvUserRankRatio.text = "#6 / 18"

        val ranksList = listOf(
            LeaderboardRunner("04", "Bùi Tường Vy", "Hà Nội", "21:26", R.drawable.ic_nav_profile),
            LeaderboardRunner("05", "Nguyễn Gia Phúc", "Huế", "22:10", R.drawable.ic_nav_profile),
            LeaderboardRunner("06", "Trần Minh Anh · Bạn", "Hà Nội", "23:48", R.drawable.ic_nav_profile, isHighlighted = true),
            LeaderboardRunner("07", "Lê Quốc Bảo", "Hà Nội", "24:16", R.drawable.ic_nav_profile),
            LeaderboardRunner("08", "Phan Thu Trang", "TP. Hồ Chí Minh", "25:02", R.drawable.ic_nav_profile)
        )

        leaderboardAdapter = LeaderboardAdapter(ranksList) { runner ->
            Toast.makeText(this, "Chọn: ${runner.name}", Toast.LENGTH_SHORT).show()
        }
        rvRanks.adapter = leaderboardAdapter
    }
}
