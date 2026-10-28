package com.example.runtrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.runtrack.settings.FriendRequestsFragment;
import com.example.runtrack.settings.ActivityPrivacyFragment;
import com.example.runtrack.settings.LoggedInDevicesFragment;

/**
 * [SCR-08] Account Security & Background System Permissions Fragment.
 */
public class AccountPermissionsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hud_account_permissions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header Back (< HUB)
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Change Password Button
        View btnChangePassword = view.findViewById(R.id.btnChangePassword);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                if (requireActivity() instanceof HudSettingsActivity) {
                    ((HudSettingsActivity) requireActivity()).navigateToFragment(new ChangePasswordFragment(), "CHANGE_PASSWORD");
                } else {
                    Toast.makeText(requireContext(), "Mở giao diện đổi mật khẩu...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Logged-in Devices Button
        View btnActiveDevices = view.findViewById(R.id.btnActiveDevices);
        if (btnActiveDevices != null) {
            btnActiveDevices.setOnClickListener(v -> {
                if (requireActivity() instanceof HudSettingsActivity) {
                    ((HudSettingsActivity) requireActivity()).navigateToFragment(new LoggedInDevicesFragment(), "LOGGED_IN_DEVICES");
                } else {
                    Toast.makeText(requireContext(), "Mở quản lý thiết bị...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Activity Privacy Button
        View btnActivityPrivacy = view.findViewById(R.id.btnActivityPrivacy);
        if (btnActivityPrivacy != null) {
            btnActivityPrivacy.setOnClickListener(v -> {
                if (requireActivity() instanceof HudSettingsActivity) {
                    ((HudSettingsActivity) requireActivity()).navigateToFragment(new ActivityPrivacyFragment(), "ACTIVITY_PRIVACY");
                } else {
                    Toast.makeText(requireContext(), "Mở quyền riêng tư...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Friend Requests Button
        View btnFriendRequests = view.findViewById(R.id.btnFriendRequests);
        if (btnFriendRequests != null) {
            btnFriendRequests.setOnClickListener(v -> {
                if (requireActivity() instanceof HudSettingsActivity) {
                    ((HudSettingsActivity) requireActivity()).navigateToFragment(new FriendRequestsFragment(), "FRIEND_REQUESTS");
                } else {
                    Toast.makeText(requireContext(), "Mở thiết lập lời mời kết bạn...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Revoke JWT / Logout Button
        View btnRevokeJwt = view.findViewById(R.id.btnRevokeJwt);
        if (btnRevokeJwt != null) {
            btnRevokeJwt.setOnClickListener(v -> {
                SessionManager.setLoggedIn(requireContext(), false);
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        }
    }
}
