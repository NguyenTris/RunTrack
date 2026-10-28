package com.example.runtrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.runtrack.settings.GpsAccuracyFragment;
import com.example.runtrack.settings.MapStyleFragment;
import com.example.runtrack.settings.UnitsFragment;

/**
 * [SCR-02] Run Preferences & Audio Cues Fragment.
 */
public class RunPreferencesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hud_run_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header Back (< HUB)
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // GPS Accuracy Submenu
        View btnGpsAccuracy = view.findViewById(R.id.btnGpsAccuracy);
        if (btnGpsAccuracy != null) {
            btnGpsAccuracy.setOnClickListener(v -> {
                if (requireActivity() instanceof HudSettingsActivity) {
                    ((HudSettingsActivity) requireActivity()).navigateToFragment(new GpsAccuracyFragment(), "GPS_ACCURACY");
                } else {
                    Toast.makeText(requireContext(), "Mở thiết lập độ chính xác GPS...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Map Style Submenu
        View btnMapStyle = view.findViewById(R.id.btnMapStyle);
        if (btnMapStyle != null) {
            btnMapStyle.setOnClickListener(v -> {
                if (requireActivity() instanceof HudSettingsActivity) {
                    ((HudSettingsActivity) requireActivity()).navigateToFragment(new MapStyleFragment(), "MAP_STYLE");
                } else {
                    Toast.makeText(requireContext(), "Mở thiết lập kiểu bản đồ...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Units Settings Submenu
        View btnUnitsSettings = view.findViewById(R.id.btnUnitsSettings);
        if (btnUnitsSettings != null) {
            btnUnitsSettings.setOnClickListener(v -> {
                if (requireActivity() instanceof HudSettingsActivity) {
                    ((HudSettingsActivity) requireActivity()).navigateToFragment(new UnitsFragment(), "UNITS_SETTINGS");
                } else {
                    Toast.makeText(requireContext(), "Mở thiết lập hệ đơn vị...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Interval selection toasts
        View btn500 = view.findViewById(R.id.btnInterval500m);
        if (btn500 != null) {
            btn500.setOnClickListener(v -> Toast.makeText(requireContext(), "Tần suất thông báo: Mỗi 500M", Toast.LENGTH_SHORT).show());
        }

        View btn1km = view.findViewById(R.id.btnInterval1km);
        if (btn1km != null) {
            btn1km.setOnClickListener(v -> Toast.makeText(requireContext(), "Tần suất thông báo: Mỗi 1 KM", Toast.LENGTH_SHORT).show());
        }

        View btn2km = view.findViewById(R.id.btnInterval2km);
        if (btn2km != null) {
            btn2km.setOnClickListener(v -> Toast.makeText(requireContext(), "Tần suất thông báo: Mỗi 2 KM", Toast.LENGTH_SHORT).show());
        }

        // Unit system selection toasts
        View btnMetric = view.findViewById(R.id.btnUnitMetric);
        if (btnMetric != null) {
            btnMetric.setOnClickListener(v -> Toast.makeText(requireContext(), "Đơn vị: Metric (KM, M, KG)", Toast.LENGTH_SHORT).show());
        }

        View btnImperial = view.findViewById(R.id.btnUnitImperial);
        if (btnImperial != null) {
            btnImperial.setOnClickListener(v -> Toast.makeText(requireContext(), "Đơn vị: Imperial (MI, FT, LBS)", Toast.LENGTH_SHORT).show());
        }

        // Bottom Save Button
        View btnSave = view.findViewById(R.id.btnSavePreferences);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Đã lưu toàn bộ tùy chọn chạy & giọng nói", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }
    }
}
