package com.example.runtrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.model.progress.LeaderboardEntryDto
import com.example.runtrack.network.model.progress.LeaderboardResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LeaderboardFragment : Fragment() {

    private lateinit var rvRanks: RecyclerView
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
        loadLeaderboard("global")
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
        view.findViewById<TextView>(R.id.tv_btn_home)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val isGlobal = checkedId == R.id.btn_tab_global
            setTabStyle(btnTabGlobal, isSelected = isGlobal)
            setTabStyle(btnTabFriends, isSelected = !isGlobal)
            loadLeaderboard(if (isGlobal) "global" else "friends")
        }
    }

    private fun setTabStyle(button: MaterialButton, isSelected: Boolean) {
        val ctx = requireContext()
        val bgRes = if (isSelected) R.color.kinetic_orange else R.color.surface_card
        val textRes = if (isSelected) R.color.white else R.color.text_secondary
        button.setBackgroundColor(ContextCompat.getColor(ctx, bgRes))
        button.setTextColor(ContextCompat.getColor(ctx, textRes))
    }

    private fun loadLeaderboard(scope: String) {
        val ctx = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(ctx).getLeaderboard(scope = scope, limit = 20)
                }
                if (!isAdded) return@launch
                if (response.isSuccessful && response.body() != null) {
                    bindLeaderboard(response.body()!!)
                } else {
                    showErrorState("Không thể tải bảng xếp hạng")
                }
            } catch (_: Exception) {
                if (isAdded) showErrorState("Lỗi kết nối máy chủ bảng xếp hạng")
            }
        }
    }

    private fun bindLeaderboard(data: LeaderboardResponse) {
        tvRunnersCountPodium.text = "${data.total_runners} người chạy"
        val userRankText = data.current_user_rank?.let { "#$it" } ?: "--"
        tvUserRankRatio.text = "$userRankText / ${data.total_runners}"

        bindPodiumSlot(data.entries.getOrNull(0), tvNameRank1, tvTimeRank1)
        bindPodiumSlot(data.entries.getOrNull(1), tvNameRank2, tvTimeRank2)
        bindPodiumSlot(data.entries.getOrNull(2), tvNameRank3, tvTimeRank3)

        val remaining = data.entries.drop(3).map { entry ->
            val displayName = if (entry.is_current_user) "${entry.full_name} · Bạn" else entry.full_name
            val subtitle = "${entry.activities_count} buổi chạy"
            LeaderboardRunner(
                rank = String.format(Locale.US, "%02d", entry.rank),
                name = displayName,
                location = subtitle,
                time = formatMetric(entry),
                avatarResId = R.drawable.ic_nav_profile,
                isHighlighted = entry.is_current_user
            )
        }

        rvRanks.adapter = LeaderboardAdapter(remaining) { runner ->
            Toast.makeText(requireContext(), "${runner.name} — ${runner.time}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bindPodiumSlot(
        entry: LeaderboardEntryDto?,
        tvName: TextView,
        tvMetric: TextView
    ) {
        if (entry == null) {
            tvName.text = "--"
            tvMetric.text = "0.0 km"
            return
        }
        tvName.text = entry.full_name
        tvMetric.text = formatMetric(entry)
    }

    private fun formatMetric(entry: LeaderboardEntryDto): String {
        val kmStr = String.format(Locale.US, "%.1f km", entry.total_distance_km)
        return if (entry.avg_pace_display.isNotBlank()) {
            "$kmStr · ${entry.avg_pace_display}"
        } else {
            kmStr
        }
    }

    private fun showErrorState(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        tvRunnersCountPodium.text = "0 người chạy"
        tvUserRankRatio.text = "-- / 0"
        bindPodiumSlot(null, tvNameRank1, tvTimeRank1)
        bindPodiumSlot(null, tvNameRank2, tvTimeRank2)
        bindPodiumSlot(null, tvNameRank3, tvTimeRank3)
        rvRanks.adapter = LeaderboardAdapter(emptyList()) {}
    }
}
