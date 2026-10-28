package com.example.runtrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.runtrack.settings.ThemeFragment;
import com.example.runtrack.settings.LanguageFragment;

/**
 * Main dashboard fragment inside HudSettingsActivity.
 * Displays 6 navigation bars and logout action.
 */
public class SettingsHubMainFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_hud_settings_hub, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header back button (< HOME) -> Finishes HudSettingsActivity
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().finish());
        }

        // Bar 1: BLE Devices
        View barBleDevices = view.findViewById(R.id.barBleDevices);
        if (barBleDevices != null) {
            barBleDevices.setOnClickListener(v -> navigateTo(new BleDevicesFragment(), "BLE_DEVICES"));
        }

        // Bar 2: Heart Rate Zones & Alert
        View barHeartRate = view.findViewById(R.id.barHeartRate);
        if (barHeartRate != null) {
            barHeartRate.setOnClickListener(v -> navigateTo(new HeartRateZonesFragment(), "HEART_RATE_ZONES"));
        }

        // Bar 3: Target Goals
        View barGoals = view.findViewById(R.id.barGoals);
        if (barGoals != null) {
            barGoals.setOnClickListener(v -> navigateTo(new TargetGoalsFragment(), "TARGET_GOALS"));
        }

        // Bar 4: Run Preferences & Audio Cues
        View barRunPreferences = view.findViewById(R.id.barRunPreferences);
        if (barRunPreferences != null) {
            barRunPreferences.setOnClickListener(v -> navigateTo(new RunPreferencesFragment(), "RUN_PREFERENCES"));
        }

        // Bar 5: Biometric Profile & BMI/BMR
        View barBiometrics = view.findViewById(R.id.barBiometrics);
        if (barBiometrics != null) {
            barBiometrics.setOnClickListener(v -> navigateTo(new BiometricProfileFragment(), "BIOMETRICS"));
        }

        // Bar 6: Account Permissions & Security
        View barPermissions = view.findViewById(R.id.barPermissions);
        if (barPermissions != null) {
            barPermissions.setOnClickListener(v -> navigateTo(new AccountPermissionsFragment(), "PERMISSIONS"));
        }

        // Bar 7: Theme & HUD Palettes
        View barTheme = view.findViewById(R.id.barTheme);
        if (barTheme != null) {
            barTheme.setOnClickListener(v -> navigateTo(new ThemeFragment(), "THEME_SETTINGS"));
        }

        // Bar 8: Language & Font Scale
        View barLanguage = view.findViewById(R.id.barLanguage);
        if (barLanguage != null) {
            barLanguage.setOnClickListener(v -> navigateTo(new LanguageFragment(), "LANGUAGE_SETTINGS"));
        }

        // Emergency Logout Button
        View btnLogout = view.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> handleLogout());
        }
    }

    private void navigateTo(Fragment fragment, String tag) {
        if (requireActivity() instanceof HudSettingsActivity) {
            ((HudSettingsActivity) requireActivity()).navigateToFragment(fragment, tag);
        }
    }

    private void handleLogout() {
        SessionManager.setLoggedIn(requireContext(), false);
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
