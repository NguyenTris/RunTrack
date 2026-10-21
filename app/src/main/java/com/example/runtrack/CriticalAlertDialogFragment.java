package com.example.runtrack;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * DialogFragment for displaying critical heart rate warnings and emergency recovery protocols.
 */
public class CriticalAlertDialogFragment extends DialogFragment {

    public static CriticalAlertDialogFragment newInstance() {
        return new CriticalAlertDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_hud_critical_alert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View btnResetAlert = view.findViewById(R.id.btnResetAlert);
        if (btnResetAlert != null) {
            btnResetAlert.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Cảnh báo đã được xác nhận (Reset)", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }

        View btnStopWorkout = view.findViewById(R.id.btnStopWorkout);
        if (btnStopWorkout != null) {
            btnStopWorkout.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Đã kích hoạt dừng buổi tập an toàn", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
