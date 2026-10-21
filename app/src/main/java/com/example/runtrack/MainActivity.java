package com.example.runtrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_run_tracking);

        View root = findViewById(R.id.root_layout);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Header click listeners
        View headerStatus = findViewById(R.id.tv_header_status);
        if (headerStatus != null) {
            headerStatus.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, NotificationCenterActivity.class);
                startActivity(intent);
            });
        }

        View headerTitle = findViewById(R.id.tv_header_title);
        if (headerTitle != null) {
            headerTitle.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, HudSettingsActivity.class);
                startActivity(intent);
            });
        }

        // Action Buttons to open HUD modules
        Button btnPrimary = findViewById(R.id.btn_primary_login);
        if (btnPrimary != null) {
            btnPrimary.setText("BẢNG CÀI ĐẶT & NGOẠI VI (HUD)");
            btnPrimary.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, HudSettingsActivity.class);
                startActivity(intent);
            });
        }

        Button btnSecondary = findViewById(R.id.btn_secondary_login);
        if (btnSecondary != null) {
            btnSecondary.setText("TRUNG TÂM THÔNG BÁO // ALERTS");
            btnSecondary.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, NotificationCenterActivity.class);
                startActivity(intent);
            });
        }
    }
}
