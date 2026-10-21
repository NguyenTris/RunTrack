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

import java.util.Locale;

/**
 * [SCR-03] Weekly Target Goals Configuration Fragment.
 */
public class TargetGoalsFragment extends Fragment {

    private double distanceKm = 50.0;
    private double hours = 5.0;
    private int calories = 3500;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hud_target_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header Back (< HUB)
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Stepper: Distance
        TextView tvDistance = view.findViewById(R.id.tvTargetDistanceVal);
        View btnMinusDist = view.findViewById(R.id.btnMinusDistance);
        View btnPlusDist = view.findViewById(R.id.btnPlusDistance);

        if (btnMinusDist != null && tvDistance != null) {
            btnMinusDist.setOnClickListener(v -> {
                if (distanceKm > 5.0) distanceKm -= 2.5;
                tvDistance.setText(String.format(Locale.US, "%.1f KM", distanceKm));
            });
        }
        if (btnPlusDist != null && tvDistance != null) {
            btnPlusDist.setOnClickListener(v -> {
                if (distanceKm < 200.0) distanceKm += 2.5;
                tvDistance.setText(String.format(Locale.US, "%.1f KM", distanceKm));
            });
        }

        // Stepper: Hours
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

        // Stepper: Calories
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

        // Bottom Apply Button
        View btnApply = view.findViewById(R.id.btnApplyGoals);
        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Mục tiêu tập luyện mới đã được kích hoạt!", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }
    }
}
