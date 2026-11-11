package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.runtrack.databinding.ActivityLoginBinding
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

        val root = findViewById<android.view.View>(R.id.root_layout)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        binding.tvTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

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
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString()?.trim() ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email và mật khẩu không được để trống", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.login(request)
                }

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    val token = loginResponse.access_token
                    Toast.makeText(this@LoginActivity, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                    SessionManager.setLoggedIn(this@LoginActivity, true)
                    SessionManager.setUsername(this@LoginActivity, email)
                    SessionManager.setToken(this@LoginActivity, token)

                    val intent = Intent(this@LoginActivity, AuthSuccessActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Đăng nhập thất bại"
                    Toast.makeText(this@LoginActivity, "Lỗi: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnLogin.isEnabled = true
            }
        }
    }
}
