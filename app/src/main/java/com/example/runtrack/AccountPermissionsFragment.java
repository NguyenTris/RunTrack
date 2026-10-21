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
            btnChangePassword.setOnClickListener(v -> Toast.makeText(requireContext(), "Mở hộp thoại đổi mật khẩu tài khoản...", Toast.LENGTH_SHORT).show());
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
