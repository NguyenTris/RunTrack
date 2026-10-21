package com.example.runtrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * [SCR-04] Heart Rate Zones & Safety Threshold Fragment.
 */
public class HeartRateZonesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hud_heart_rate_zones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header Back (< HUB)
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Bottom Save Button
        View btnSave = view.findViewById(R.id.btnSaveSafetyConfig);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Đã lưu cấu hình ngưỡng an toàn nhịp tim", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }

        // Test Critical Alert by clicking Zone 5 or Threshold value
        View tagZ5 = view.findViewById(R.id.tagZ5);
        if (tagZ5 != null) {
            tagZ5.setOnClickListener(v -> triggerCriticalAlertDemo());
        }

        View tvThresholdBpmVal = view.findViewById(R.id.tvThresholdBpmVal);
        if (tvThresholdBpmVal != null) {
            tvThresholdBpmVal.setOnClickListener(v -> triggerCriticalAlertDemo());
        }
    }

    private void triggerCriticalAlertDemo() {
        CriticalAlertDialogFragment dialog = CriticalAlertDialogFragment.newInstance();
        dialog.show(getParentFragmentManager(), "CRITICAL_ALERT_DIALOG");
    }
}
