package com.example.runtrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.runtrack.network.ProfileSyncHelper;
import com.example.runtrack.network.model.user.UserProfileDto;

import java.util.Locale;

/**
 * [SCR-07] Biometric Profile & Physical Metrics Fragment.
 */
public class BiometricProfileFragment extends Fragment {

    private EditText etWeightKg;
    private EditText etHeightCm;
    private EditText etAge;
    private TextView tvBmiVal;
    private TextView tvBmrVal;
    private View btnUpdate;
    private boolean isMale = true;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_hud_biometric_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners(view);
        loadProfileFromServer();
    }

    private void initViews(@NonNull View view) {
        etWeightKg = view.findViewById(R.id.etWeightKg);
        etHeightCm = view.findViewById(R.id.etHeightCm);
        etAge = view.findViewById(R.id.etAge);
        tvBmiVal = view.findViewById(R.id.tvBmiVal);
        tvBmrVal = view.findViewById(R.id.tvBmrVal);
        btnUpdate = view.findViewById(R.id.btnUpdateBiometrics);
    }

    private void setupListeners(@NonNull View view) {
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        View btnMale = view.findViewById(R.id.btnSexMale);
        if (btnMale != null) {
            btnMale.setOnClickListener(v -> {
                isMale = true;
                updateCalculatedMetrics();
                Toast.makeText(requireContext(), "Giới tính: NAM", Toast.LENGTH_SHORT).show();
            });
        }

        View btnFemale = view.findViewById(R.id.btnSexFemale);
        if (btnFemale != null) {
            btnFemale.setOnClickListener(v -> {
                isMale = false;
                updateCalculatedMetrics();
                Toast.makeText(requireContext(), "Giới tính: NỮ", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> submitBiometricsUpdate());
        }
    }

    private void loadProfileFromServer() {
        ProfileSyncHelper.fetchProfile(requireContext(), new ProfileSyncHelper.ProfileCallback() {
            @Override
            public void onSuccess(@NonNull UserProfileDto profile) {
                if (!isAdded()) return;
                if (profile.getWeight() != null && etWeightKg != null) {
                    etWeightKg.setText(String.format(Locale.US, "%.1f", profile.getWeight()));
                }
                if (profile.getHeight() != null && etHeightCm != null) {
                    etHeightCm.setText(String.format(Locale.US, "%.0f", profile.getHeight()));
                }
                updateCalculatedMetrics();
            }

            @Override
            public void onError(@NonNull String message) {
                if (isAdded()) {
                    updateCalculatedMetrics();
                }
            }
        });
    }

    private void submitBiometricsUpdate() {
        Double weight = parseDoubleOrNull(etWeightKg);
        Double height = parseDoubleOrNull(etHeightCm);
        if (weight == null || weight <= 0 || height == null || height <= 0) {
            Toast.makeText(requireContext(), "Vui lòng nhập chiều cao và cân nặng hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        updateCalculatedMetrics();
        if (btnUpdate != null) btnUpdate.setEnabled(false);

        ProfileSyncHelper.updateProfile(
                requireContext(),
                height,
                weight,
                null,
                new ProfileSyncHelper.ProfileCallback() {
                    @Override
                    public void onSuccess(@NonNull UserProfileDto profile) {
                        if (!isAdded()) return;
                        if (btnUpdate != null) btnUpdate.setEnabled(true);
                        Toast.makeText(
                                requireContext(),
                                "Đã đồng bộ chỉ số thể chất lên máy chủ!",
                                Toast.LENGTH_SHORT
                        ).show();
                        getParentFragmentManager().popBackStack();
                    }

                    @Override
                    public void onError(@NonNull String message) {
                        if (!isAdded()) return;
                        if (btnUpdate != null) btnUpdate.setEnabled(true);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void updateCalculatedMetrics() {
        Double weight = parseDoubleOrNull(etWeightKg);
        Double heightCm = parseDoubleOrNull(etHeightCm);
        Double age = parseDoubleOrNull(etAge);
        if (weight == null || heightCm == null || weight <= 0 || heightCm <= 0) return;

        double heightM = heightCm / 100.0;
        double bmi = weight / (heightM * heightM);
        if (tvBmiVal != null) {
            tvBmiVal.setText(String.format(Locale.US, "%.1f", bmi));
        }

        double safeAge = (age != null && age > 0) ? age : 28.0;
        double bmr = (10.0 * weight) + (6.25 * heightCm) - (5.0 * safeAge) + (isMale ? 5.0 : -161.0);
        if (tvBmrVal != null) {
            tvBmrVal.setText(String.format(Locale.US, "%,.0f", Math.max(800.0, bmr)));
        }
    }

    @Nullable
    private Double parseDoubleOrNull(@Nullable EditText editText) {
        if (editText == null) return null;
        String text = editText.getText() != null ? editText.getText().toString().trim() : "";
        if (text.isEmpty()) return null;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
