package com.example.runtrack;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * [SCR-09] HUD Notifications Center Activity.
 * Displays categorised alerts, personal records, and system telemetry notifications.
 */
public class NotificationCenterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hud_notification_center);

        View root = findViewById(R.id.telemetryStrip);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }

        // Header Back (< HOME)
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Horizontal Filter Tabs
        View tabAll = findViewById(R.id.tabAll);
        if (tabAll != null) {
            tabAll.setOnClickListener(v -> Toast.makeText(this, "Bộ lọc: TẤT CẢ THÔNG BÁO", Toast.LENGTH_SHORT).show());
        }

        View tabAlerts = findViewById(R.id.tabAlerts);
        if (tabAlerts != null) {
            tabAlerts.setOnClickListener(v -> Toast.makeText(this, "Bộ lọc: CẢNH BÁO NGUY HIỂM", Toast.LENGTH_SHORT).show());
        }

        View tabRecords = findViewById(R.id.tabRecords);
        if (tabRecords != null) {
            tabRecords.setOnClickListener(v -> Toast.makeText(this, "Bộ lọc: KỶ LỤC CÁ NHÂN (PR)", Toast.LENGTH_SHORT).show());
        }

        View tabSystem = findViewById(R.id.tabSystem);
        if (tabSystem != null) {
            tabSystem.setOnClickListener(v -> Toast.makeText(this, "Bộ lọc: HỆ THỐNG & ĐỒNG BỘ", Toast.LENGTH_SHORT).show());
        }

        // Critical Alert Card Item -> Opens CriticalAlertDialogFragment
        View notifItemAlert = findViewById(R.id.notifItemAlert);
        if (notifItemAlert != null) {
            notifItemAlert.setOnClickListener(v -> {
                CriticalAlertDialogFragment dialog = CriticalAlertDialogFragment.newInstance();
                dialog.show(getSupportFragmentManager(), "CRITICAL_ALERT_DIALOG");
            });
        }

        // Mark all as read button
        View btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> Toast.makeText(this, "Đã đánh dấu đã đọc tất cả thông báo", Toast.LENGTH_SHORT).show());
        }
    }
}
