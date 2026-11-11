package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.runtrack.databinding.ActivityLoginBinding
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val root = binding.root
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
    }

    private fun setupListeners() {
        // Toggle password visibility
        binding.tvTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Login Button click listener
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        // Navigate to Register Activity
        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.tvTogglePassword.setText(R.string.toggle_show_pass)
            isPasswordVisible = false
        } else {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.tvTogglePassword.setText(R.string.toggle_hide_pass)
            isPasswordVisible = true
        }
        binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
    }

    private fun performLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        if (email.isEmpty()) {
            binding.etEmail.error = "Vui lòng nhập email"
            binding.etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Vui lòng nhập mật khẩu"
            binding.etPassword.requestFocus()
            return
        }

        // Disable button during network call to prevent double submissions
        binding.btnLogin.isEnabled = false
        Toast.makeText(this, "Đang kết nối tới VPS...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = withContext(Dispatchers.IO) {
                    ApiClient.instance.login(request)
                }

                binding.btnLogin.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    val token = loginResponse.access_token

                    Log.d("LoginActivity", "Đăng nhập thành công! Token: $token")
                    Toast.makeText(this@LoginActivity, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                    // Save session & navigate
                    SessionManager.setLoggedIn(this@LoginActivity, true)
                    SessionManager.setUsername(this@LoginActivity, email)

                    val intent = Intent(this@LoginActivity, AuthSuccessActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Sai thông tin đăng nhập hoặc lỗi server"
                    Log.e("LoginActivity", "Đăng nhập thất bại: $errorMsg")
                    Toast.makeText(this@LoginActivity, "Đăng nhập thất bại: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.btnLogin.isEnabled = true
                Log.e("LoginActivity", "Lỗi kết nối mạng: ${e.message}", e)
                Toast.makeText(this@LoginActivity, "Lỗi kết nối VPS: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
