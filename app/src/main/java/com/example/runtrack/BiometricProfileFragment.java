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
 * [SCR-07] Biometric Profile & Physical Metrics Fragment.
 */
public class BiometricProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hud_biometric_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header Back (< HUB)
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Gender selectors
        View btnMale = view.findViewById(R.id.btnSexMale);
        if (btnMale != null) {
            btnMale.setOnClickListener(v -> Toast.makeText(requireContext(), "Giới tính: NAM", Toast.LENGTH_SHORT).show());
        }

        View btnFemale = view.findViewById(R.id.btnSexFemale);
        if (btnFemale != null) {
            btnFemale.setOnClickListener(v -> Toast.makeText(requireContext(), "Giới tính: NỮ", Toast.LENGTH_SHORT).show());
        }

        // Bottom Update Button
        View btnUpdate = view.findViewById(R.id.btnUpdateBiometrics);
        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Đã cập nhật chỉ số thể chất & tính toán lại BMI/BMR!", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }
    }
}
