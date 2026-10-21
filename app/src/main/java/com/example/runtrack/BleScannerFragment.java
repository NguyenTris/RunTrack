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
 * [SCR-06] Radar BLE Scanner Fragment.
 */
public class BleScannerFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hud_ble_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header Back (< QUAY LẠI)
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Bottom Stop Scan Button
        View btnStopScanning = view.findViewById(R.id.btnStopScanning);
        if (btnStopScanning != null) {
            btnStopScanning.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Đã dừng quét thiết bị BLE", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }

        // Pairing Buttons
        View btnPairHrm = view.findViewById(R.id.btnPairHrm);
        if (btnPairHrm != null) {
            btnPairHrm.setOnClickListener(v -> Toast.makeText(requireContext(), "Đã kết nối thành công với Polar H10", Toast.LENGTH_SHORT).show());
        }

        View btnPairCoros = view.findViewById(R.id.btnPairCoros);
        if (btnPairCoros != null) {
            btnPairCoros.setOnClickListener(v -> Toast.makeText(requireContext(), "Đang gửi yêu cầu ghép đôi COROS POD 2...", Toast.LENGTH_SHORT).show());
        }
    }
}
