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
 * [SCR-05] BLE Peripherals Management Fragment.
 */
public class BleDevicesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hud_ble_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header Back (< HUB)
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Open BLE Scanner
        View btnScanNewDevices = view.findViewById(R.id.btnScanNewDevices);
        if (btnScanNewDevices != null) {
            btnScanNewDevices.setOnClickListener(v -> {
                if (requireActivity() instanceof HudSettingsActivity) {
                    ((HudSettingsActivity) requireActivity()).navigateToFragment(new BleScannerFragment(), "BLE_SCANNER");
                }
            });
        }

        // Actions on HRM
        View btnTestHrm = view.findViewById(R.id.btnTestHrm);
        if (btnTestHrm != null) {
            btnTestHrm.setOnClickListener(v -> Toast.makeText(requireContext(), "Test tín hiệu Polar H10: 62 BPM [Ổn định]", Toast.LENGTH_SHORT).show());
        }

        View btnDisconnectHrm = view.findViewById(R.id.btnDisconnectHrm);
        if (btnDisconnectHrm != null) {
            btnDisconnectHrm.setOnClickListener(v -> Toast.makeText(requireContext(), "Đã ngắt kết nối Polar H10", Toast.LENGTH_SHORT).show());
        }

        // Actions on Pod
        View btnCalibratePod = view.findViewById(R.id.btnCalibratePod);
        if (btnCalibratePod != null) {
            btnCalibratePod.setOnClickListener(v -> Toast.makeText(requireContext(), "Đang hiệu chuẩn Alphafly Pod...", Toast.LENGTH_SHORT).show());
        }

        View btnDisconnectPod = view.findViewById(R.id.btnDisconnectPod);
        if (btnDisconnectPod != null) {
            btnDisconnectPod.setOnClickListener(v -> Toast.makeText(requireContext(), "Đã ngắt kết nối Alphafly Pod", Toast.LENGTH_SHORT).show());
        }
    }
}
