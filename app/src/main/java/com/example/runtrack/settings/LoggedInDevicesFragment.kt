package com.example.runtrack.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R

/**
 * [SCR-SEC-02] Logged-in Devices & Active Sessions Management Fragment.
 */
class LoggedInDevicesFragment : Fragment() {

    private lateinit var rvDevices: RecyclerView
    private lateinit var adapter: LoggedInDevicesAdapter
    private lateinit var tvEmptyState: TextView
    private lateinit var btnLogoutAll: TextView

    private val otherDevices = mutableListOf(
        LoggedInDevice(
            id = "dev_1",
            name = "MacBook Pro 16\" (M3 Max)",
            deviceType = DeviceType.DESKTOP,
            location = "Hà Nội, Việt Nam",
            clientApp = "Safari 18.1",
            ipAddress = "118.70.14.92",
            lastActive = "10 phút trước"
        ),
        LoggedInDevice(
            id = "dev_2",
            name = "iPad Pro 11\" (M2)",
            deviceType = DeviceType.TABLET,
            location = "TP. Hồ Chí Minh",
            clientApp = "RunTrack Tablet v2.4",
            ipAddress = "14.161.22.45",
            lastActive = "3 ngày trước"
        ),
        LoggedInDevice(
            id = "dev_3",
            name = "Garmin Forerunner 965",
            deviceType = DeviceType.WATCH,
            location = "Đồng bộ đám mây",
            clientApp = "Garmin Connect Sync",
            ipAddress = "API Token #892",
            lastActive = "1 tuần trước"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hud_logged_in_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupListeners(view)
    }

    private fun initViews(view: View) {
        rvDevices = view.findViewById(R.id.rv_devices)
        tvEmptyState = view.findViewById(R.id.tv_empty_state)
        btnLogoutAll = view.findViewById(R.id.btnLogoutAllOthers)
    }

    private fun setupRecyclerView() {
        rvDevices.layoutManager = LinearLayoutManager(requireContext())
        adapter = LoggedInDevicesAdapter(
            devices = otherDevices,
            onLogoutClick = { device, pos ->
                otherDevices.removeAt(pos)
                adapter.notifyItemRemoved(pos)
                Toast.makeText(requireContext(), "Đã đăng xuất khỏi ${device.name}", Toast.LENGTH_SHORT).show()
                checkEmptyState()
            }
        )
        rvDevices.adapter = adapter
        checkEmptyState()
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnLogoutAll.setOnClickListener {
            if (otherDevices.isNotEmpty()) {
                val count = otherDevices.size
                otherDevices.clear()
                adapter.notifyDataSetChanged()
                checkEmptyState()
                Toast.makeText(requireContext(), "Đã đăng xuất thành công khỏi $count thiết bị khác!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Không có thiết bị nào khác đang đăng nhập.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkEmptyState() {
        if (otherDevices.isEmpty()) {
            rvDevices.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
            btnLogoutAll.isEnabled = false
            btnLogoutAll.alpha = 0.5f
        } else {
            rvDevices.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
            btnLogoutAll.isEnabled = true
            btnLogoutAll.alpha = 1.0f
        }
    }
}
