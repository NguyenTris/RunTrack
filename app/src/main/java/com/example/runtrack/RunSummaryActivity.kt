package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class RunSummaryActivity : AppCompatActivity() {

    private lateinit var rvLapSplits: RecyclerView
    private lateinit var lapSplitsAdapter: LapSplitsAdapter

    private lateinit var rvEquipment: RecyclerView
    private lateinit var equipmentAdapter: EquipmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_run_summary)

        val root = findViewById<View>(R.id.root_layout_run_summary) ?: findViewById(R.id.root_layout_run_summary_land)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
    }

    private fun initViews() {
        val btnBack = findViewById<ImageView>(R.id.btn_back_summary)
        btnBack?.setOnClickListener { finish() }

        // Setup Lap Splits RecyclerView
        rvLapSplits = findViewById(R.id.rv_lap_splits)
        rvLapSplits.layoutManager = LinearLayoutManager(this)

        val sampleLaps = listOf(
            LapSplit("KM 1", "5'32\"", "144 bpm", "+8m", isHighlighted = false),
            LapSplit("KM 2", "5'24\"", "152 bpm", "+6m", isHighlighted = false),
            LapSplit("KM 3", "5'15\"", "158 bpm", "+10m", isHighlighted = false),
            LapSplit("KM 4 ⚡", "5'10\"", "164 bpm", "+11m", isHighlighted = false),
            LapSplit("KM 5 ⚡", "5'08\" ★ PB", "172 bpm", "+7m", isHighlighted = true),
            LapSplit("0.42 KM", "4'52\"", "176 bpm", "0m", isHighlighted = false)
        )

        lapSplitsAdapter = LapSplitsAdapter(sampleLaps)
        rvLapSplits.adapter = lapSplitsAdapter

        // Setup Equipment RecyclerView
        rvEquipment = findViewById(R.id.rv_equipment)
        rvEquipment.layoutManager = LinearLayoutManager(this)

        val sampleEquipment = listOf(
            Equipment(
                id = "1",
                name = "Nike Vaporfly 3",
                usageText = "184 / 500 KM",
                progressValue = 184,
                maxProgress = 500,
                isProgressVisible = true,
                subtext = null,
                isCheckVisible = false,
                iconResId = R.drawable.ic_shoes
            ),
            Equipment(
                id = "2",
                name = "Garmin HRM-Pro",
                usageText = "",
                progressValue = 0,
                maxProgress = 100,
                isProgressVisible = false,
                subtext = "Kết nối liên tục • ECG Sensor Active",
                isCheckVisible = true,
                iconResId = R.drawable.ic_bluetooth
            )
        )

        equipmentAdapter = EquipmentAdapter(sampleEquipment)
        rvEquipment.adapter = equipmentAdapter

        // Action buttons
        val btnShare = findViewById<MaterialButton>(R.id.btn_share_summary)
        btnShare?.setOnClickListener {
            Toast.makeText(this, "Chia sẻ thành công lên Bảng tin & Strava!", Toast.LENGTH_SHORT).show()
        }

        val btnExportGpx = findViewById<MaterialButton>(R.id.btn_export_gpx)
        btnExportGpx?.setOnClickListener {
            Toast.makeText(this, "Đang xuất file GPX...", Toast.LENGTH_SHORT).show()
        }

        val btnBackHome = findViewById<MaterialButton>(R.id.btn_back_home)
        btnBackHome?.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }
}
