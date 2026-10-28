package com.example.runtrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText etPassword;
    private TextView tvTogglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        View root = findViewById(R.id.root_layout);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        etPassword = findViewById(R.id.etPassword);
        tvTogglePassword = findViewById(R.id.tvTogglePassword);
        View btnLogin = findViewById(R.id.btnLogin);
        View tvRegister = findViewById(R.id.tvRegister);

        if (tvTogglePassword != null && etPassword != null) {
            tvTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> performLogin());
        }

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            tvTogglePassword.setText(R.string.toggle_show_pass);
            isPasswordVisible = false;
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            tvTogglePassword.setText(R.string.toggle_hide_pass);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void performLogin() {
        SessionManager.setLoggedIn(this, true);
        SessionManager.setUsername(this, "NIGHTRUNNER_07");

        Intent intent = new Intent(LoginActivity.this, AuthSuccessActivity.class);
        startActivity(intent);
        finish();
    }
}
