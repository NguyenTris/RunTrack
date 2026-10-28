package com.example.runtrack.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.runtrack.R

/**
 * [SCR-EXPORT-01] Export Running Telemetry & GPX Settings Screen.
 */
class ExportDataFragment : Fragment() {

    private lateinit var cardGpx: View
    private lateinit var radioGpx: TextView
    private lateinit var cardTcx: View
    private lateinit var radioTcx: TextView
    private lateinit var cardCsv: View
    private lateinit var radioCsv: TextView
    private lateinit var cardFit: View
    private lateinit var radioFit: TextView

    private lateinit var card30d: View
    private lateinit var radio30d: TextView
    private lateinit var cardMonth: View
    private lateinit var radioMonth: TextView
    private lateinit var cardAll: View
    private lateinit var radioAll: TextView

    private lateinit var switchHeartRate: SwitchCompat
    private lateinit var switchCadence: SwitchCompat
    private lateinit var switchElevation: SwitchCompat
    private lateinit var btnExport: TextView

    private var currentFormat = ExportFormat.GPX
    private var currentRange = ExportDateRange.LAST_30_DAYS

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hud_export_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadPreferences()
        setupListeners(view)
        updateRadioUI()
    }

    private fun initViews(view: View) {
        cardGpx = view.findViewById(R.id.card_format_gpx)
        radioGpx = view.findViewById(R.id.radio_gpx_indicator)
        cardTcx = view.findViewById(R.id.card_format_tcx)
        radioTcx = view.findViewById(R.id.radio_tcx_indicator)
        cardCsv = view.findViewById(R.id.card_format_csv)
        radioCsv = view.findViewById(R.id.radio_csv_indicator)
        cardFit = view.findViewById(R.id.card_format_fit)
        radioFit = view.findViewById(R.id.radio_fit_indicator)

        card30d = view.findViewById(R.id.card_range_30d)
        radio30d = view.findViewById(R.id.radio_range_30d_indicator)
        cardMonth = view.findViewById(R.id.card_range_month)
        radioMonth = view.findViewById(R.id.radio_range_month_indicator)
        cardAll = view.findViewById(R.id.card_range_all)
        radioAll = view.findViewById(R.id.radio_range_all_indicator)

        switchHeartRate = view.findViewById(R.id.switch_include_hr)
        switchCadence = view.findViewById(R.id.switch_include_cadence)
        switchElevation = view.findViewById(R.id.switch_include_elevation)
        btnExport = view.findViewById(R.id.btnExportNow)
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_export_settings", Context.MODE_PRIVATE)
        val savedFormat = prefs.getString("format", ExportFormat.GPX.name)
        currentFormat = try {
            ExportFormat.valueOf(savedFormat ?: ExportFormat.GPX.name)
        } catch (_: Exception) {
            ExportFormat.GPX
        }

        val savedRange = prefs.getString("date_range", ExportDateRange.LAST_30_DAYS.name)
        currentRange = try {
            ExportDateRange.valueOf(savedRange ?: ExportDateRange.LAST_30_DAYS.name)
        } catch (_: Exception) {
            ExportDateRange.LAST_30_DAYS
        }

        switchHeartRate.isChecked = prefs.getBoolean("include_hr", true)
        switchCadence.isChecked = prefs.getBoolean("include_cadence", true)
        switchElevation.isChecked = prefs.getBoolean("include_elevation", true)
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        cardGpx.setOnClickListener {
            currentFormat = ExportFormat.GPX
            updateRadioUI()
        }
        cardTcx.setOnClickListener {
            currentFormat = ExportFormat.TCX
            updateRadioUI()
        }
        cardCsv.setOnClickListener {
            currentFormat = ExportFormat.CSV
            updateRadioUI()
        }
        cardFit.setOnClickListener {
            currentFormat = ExportFormat.FIT
            updateRadioUI()
        }

        card30d.setOnClickListener {
            currentRange = ExportDateRange.LAST_30_DAYS
            updateRadioUI()
        }
        cardMonth.setOnClickListener {
            currentRange = ExportDateRange.THIS_MONTH
            updateRadioUI()
        }
        cardAll.setOnClickListener {
            currentRange = ExportDateRange.ALL_TIME
            updateRadioUI()
        }

        btnExport.setOnClickListener {
            executeExport()
        }
    }

    private fun updateRadioUI() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.kinetic_orange)
        val dimColor = ContextCompat.getColor(requireContext(), R.color.hud_text_dim)

        radioGpx.text = if (currentFormat == ExportFormat.GPX) "●" else "○"
        radioGpx.setTextColor(if (currentFormat == ExportFormat.GPX) activeColor else dimColor)

        radioTcx.text = if (currentFormat == ExportFormat.TCX) "●" else "○"
        radioTcx.setTextColor(if (currentFormat == ExportFormat.TCX) activeColor else dimColor)

        radioCsv.text = if (currentFormat == ExportFormat.CSV) "●" else "○"
        radioCsv.setTextColor(if (currentFormat == ExportFormat.CSV) activeColor else dimColor)

        radioFit.text = if (currentFormat == ExportFormat.FIT) "●" else "○"
        radioFit.setTextColor(if (currentFormat == ExportFormat.FIT) activeColor else dimColor)

        radio30d.text = if (currentRange == ExportDateRange.LAST_30_DAYS) "●" else "○"
        radio30d.setTextColor(if (currentRange == ExportDateRange.LAST_30_DAYS) activeColor else dimColor)

        radioMonth.text = if (currentRange == ExportDateRange.THIS_MONTH) "●" else "○"
        radioMonth.setTextColor(if (currentRange == ExportDateRange.THIS_MONTH) activeColor else dimColor)

        radioAll.text = if (currentRange == ExportDateRange.ALL_TIME) "●" else "○"
        radioAll.setTextColor(if (currentRange == ExportDateRange.ALL_TIME) activeColor else dimColor)
    }

    private fun executeExport() {
        val prefs = requireContext().getSharedPreferences("runtrack_export_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("format", currentFormat.name)
            .putString("date_range", currentRange.name)
            .putBoolean("include_hr", switchHeartRate.isChecked)
            .putBoolean("include_cadence", switchCadence.isChecked)
            .putBoolean("include_elevation", switchElevation.isChecked)
            .apply()

        Toast.makeText(
            requireContext(),
            "Đã xuất thành công gói dữ liệu " + currentFormat.name + " (" + currentRange.name + ")",
            Toast.LENGTH_SHORT
        ).show()
        parentFragmentManager.popBackStack()
    }
}
