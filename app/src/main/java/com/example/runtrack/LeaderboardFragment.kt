package com.example.runtrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class LeaderboardFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners(view)
        loadGlobalData()
    }

    private fun initViews(view: View) {
        rvRanks = view.findViewById(R.id.rv_leaderboard_ranks)
        rvRanks.layoutManager = LinearLayoutManager(requireContext())

        toggleGroup = view.findViewById(R.id.toggle_group_leaderboard)
        btnTabGlobal = view.findViewById(R.id.btn_tab_global)
        btnTabFriends = view.findViewById(R.id.btn_tab_friends)

        tvRunnersCountPodium = view.findViewById(R.id.tv_runners_count_podium)
        tvNameRank1 = view.findViewById(R.id.tv_name_rank1)
        tvTimeRank1 = view.findViewById(R.id.tv_time_rank1)
        tvNameRank2 = view.findViewById(R.id.tv_name_rank2)
        tvTimeRank2 = view.findViewById(R.id.tv_time_rank2)
        tvNameRank3 = view.findViewById(R.id.tv_name_rank3)
        tvTimeRank3 = view.findViewById(R.id.tv_time_rank3)
        tvUserRankRatio = view.findViewById(R.id.tv_user_rank_ratio)
    }

    private fun setupListeners(view: View) {
        val tvBtnHome = view.findViewById<TextView>(R.id.tv_btn_home)
        tvBtnHome?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

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
        val context = requireContext()
        val orangeColor = ContextCompat.getColor(context, R.color.kinetic_orange)
        val surfaceColor = ContextCompat.getColor(context, R.color.surface_card)
        val whiteColor = ContextCompat.getColor(context, R.color.white)
        val secondaryColor = ContextCompat.getColor(context, R.color.text_secondary)

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
            Toast.makeText(requireContext(), "Chọn: ${runner.name}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), "Chọn: ${runner.name}", Toast.LENGTH_SHORT).show()
        }
        rvRanks.adapter = leaderboardAdapter
    }
}
