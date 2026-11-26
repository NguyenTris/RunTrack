package com.example.runtrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.runtrack.network.ProfileSyncHelper;
import com.example.runtrack.network.model.user.UserProfileDto;

import java.util.Locale;

/**
 * [SCR-03] Weekly Target Goals Configuration Fragment.
 */
public class TargetGoalsFragment extends Fragment {

    private double distanceKm = 40.0;
    private double hours = 5.0;
    private int calories = 3500;

    private TextView tvDistance;
    private View btnApply;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_hud_target_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDistance = view.findViewById(R.id.tvTargetDistanceVal);
        btnApply = view.findViewById(R.id.btnApplyGoals);

        setupBackAndApply(view);
        setupDistanceStepper(view);
        setupSecondarySteppers(view);
        loadWeeklyGoalFromServer();
    }

    private void setupBackAndApply(@NonNull View view) {
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }
        if (btnApply != null) {
            btnApply.setOnClickListener(v -> submitWeeklyGoal());
        }
    }

    private void setupDistanceStepper(@NonNull View view) {
        View btnMinusDist = view.findViewById(R.id.btnMinusDistance);
        View btnPlusDist = view.findViewById(R.id.btnPlusDistance);

        if (btnMinusDist != null && tvDistance != null) {
            btnMinusDist.setOnClickListener(v -> {
                if (distanceKm > 5.0) distanceKm -= 2.5;
                renderDistanceValue();
            });
        }
        if (btnPlusDist != null && tvDistance != null) {
            btnPlusDist.setOnClickListener(v -> {
                if (distanceKm < 200.0) distanceKm += 2.5;
                renderDistanceValue();
            });
        }
    }

    private void setupSecondarySteppers(@NonNull View view) {
        TextView tvHours = view.findViewById(R.id.tvTargetHoursVal);
        View btnMinusHours = view.findViewById(R.id.btnMinusHours);
        View btnPlusHours = view.findViewById(R.id.btnPlusHours);

        if (btnMinusHours != null && tvHours != null) {
            btnMinusHours.setOnClickListener(v -> {
                if (hours > 1.0) hours -= 0.5;
                tvHours.setText(String.format(Locale.US, "%.1f GIỜ", hours));
            });
        }
        if (btnPlusHours != null && tvHours != null) {
            btnPlusHours.setOnClickListener(v -> {
                if (hours < 40.0) hours += 0.5;
                tvHours.setText(String.format(Locale.US, "%.1f GIỜ", hours));
            });
        }

        TextView tvCalories = view.findViewById(R.id.tvTargetCaloriesVal);
        View btnMinusCal = view.findViewById(R.id.btnMinusCalories);
        View btnPlusCal = view.findViewById(R.id.btnPlusCalories);

        if (btnMinusCal != null && tvCalories != null) {
            btnMinusCal.setOnClickListener(v -> {
                if (calories > 500) calories -= 250;
                tvCalories.setText(String.format(Locale.US, "%d KCAL", calories));
            });
        }
        if (btnPlusCal != null && tvCalories != null) {
            btnPlusCal.setOnClickListener(v -> {
                if (calories < 20000) calories += 250;
                tvCalories.setText(String.format(Locale.US, "%d KCAL", calories));
            });
        }
    }

    private void loadWeeklyGoalFromServer() {
        ProfileSyncHelper.fetchProfile(requireContext(), new ProfileSyncHelper.ProfileCallback() {
            @Override
            public void onSuccess(@NonNull UserProfileDto profile) {
                if (!isAdded()) return;
                if (profile.getWeekly_goal_km() > 0f) {
                    distanceKm = profile.getWeekly_goal_km();
                    renderDistanceValue();
                }
            }

            @Override
            public void onError(@NonNull String message) {
                if (isAdded()) renderDistanceValue();
            }
        });
    }

    private void submitWeeklyGoal() {
        if (btnApply != null) btnApply.setEnabled(false);
        ProfileSyncHelper.updateProfile(
                requireContext(),
                null,
                null,
                distanceKm,
                new ProfileSyncHelper.ProfileCallback() {
                    @Override
                    public void onSuccess(@NonNull UserProfileDto profile) {
                        if (!isAdded()) return;
                        if (btnApply != null) btnApply.setEnabled(true);
                        Toast.makeText(
                                requireContext(),
                                "Đã lưu mục tiêu tuần lên máy chủ!",
                                Toast.LENGTH_SHORT
                        ).show();
                        getParentFragmentManager().popBackStack();
                    }

                    @Override
                    public void onError(@NonNull String message) {
                        if (!isAdded()) return;
                        if (btnApply != null) btnApply.setEnabled(true);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void renderDistanceValue() {
        if (tvDistance != null) {
            tvDistance.setText(String.format(Locale.US, "%.1f KM", distanceKm));
        }
    }
}
