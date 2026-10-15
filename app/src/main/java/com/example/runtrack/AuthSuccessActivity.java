package com.example.runtrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AuthSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_success);

        View root = findViewById(R.id.root_layout);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        View btnEnterDashboard = findViewById(R.id.btnEnterDashboard);
        TextView tvSwitchAccount = findViewById(R.id.tvSwitchAccount);

        if (btnEnterDashboard != null) {
            btnEnterDashboard.setOnClickListener(v -> Toast.makeText(this, "Chuyển sang Bảng Điều Khiển...", Toast.LENGTH_SHORT).show());
        }

        if (tvSwitchAccount != null) {
            tvSwitchAccount.setOnClickListener(v -> logout());
        }
    }

    private void logout() {
        SessionManager.setLoggedIn(this, false);
        Intent intent = new Intent(AuthSuccessActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
