package com.example.runtrack

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.runtrack.databinding.ActivityRegisterBinding
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val root = binding.root
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupTermsAndPrivacySpannable()
        setupPasswordToggle()
        setupNavigation()
    }

    private fun setupTermsAndPrivacySpannable() {
        val tvTermsLink = binding.tvTermsLink ?: return
        val fullText = "Tôi đồng ý với Điều khoản sử dụng và Chính sách bảo mật."
        val spannable = SpannableString(fullText)
        val cyanColor = ContextCompat.getColor(this, R.color.electric_cyan)

        val termsStart = fullText.indexOf("Điều khoản sử dụng")
        val termsEnd = termsStart + "Điều khoản sử dụng".length
        if (termsStart >= 0) {
            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Toast.makeText(this@RegisterActivity, "Mở Điều khoản sử dụng", Toast.LENGTH_SHORT).show()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = cyanColor
                    ds.isUnderlineText = true
                }
            }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val privacyStart = fullText.indexOf("Chính sách bảo mật")
        val privacyEnd = privacyStart + "Chính sách bảo mật".length
        if (privacyStart >= 0) {
            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Toast.makeText(this@RegisterActivity, "Mở Chính sách bảo mật", Toast.LENGTH_SHORT).show()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = cyanColor
                    ds.isUnderlineText = true
                }
            }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        tvTermsLink.text = spannable
        tvTermsLink.movementMethod = LinkMovementMethod.getInstance()
        tvTermsLink.highlightColor = Color.TRANSPARENT
    }

    private fun setupPasswordToggle() {
        binding.tvTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etPassword.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.tvTogglePassword.text = "[ẨN]"
            } else {
                binding.etPassword.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.tvTogglePassword.text = "[HIỆN]"
            }
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
        }
    }

    private fun hideKeyboard() {
        val currentView = currentFocus ?: findViewById<View>(android.R.id.content)
        currentView?.let {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun setupNavigation() {
        // Quay lại màn hình Đăng nhập
        binding.tvGoToLogin.setOnClickListener {
            finish()
        }

        // Nút Đăng ký với validation và gọi API
        binding.btnRegister.setOnClickListener {
            hideKeyboard()
            binding.inputLayoutUsername.error = null
            binding.inputLayoutEmail.error = null
            binding.inputLayoutPassword.error = null

            val fullName = binding.etFullName.text?.toString()?.trim().orEmpty()
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString()?.trim().orEmpty()

            val userRes = RegisterValidator.validateUsername(fullName)
            if (!userRes.isValid) {
                binding.inputLayoutUsername.error = userRes.errorMessage
                binding.etFullName.requestFocus()
                return@setOnClickListener
            }

            val emailRes = RegisterValidator.validateEmail(email)
            if (!emailRes.isValid) {
                binding.inputLayoutEmail.error = emailRes.errorMessage
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            val passRes = RegisterValidator.validatePassword(password)
            if (!passRes.isValid) {
                binding.inputLayoutPassword.error = passRes.errorMessage
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            performRegister(fullName, email, password)
        }
    }

    private fun performRegister(fullName: String, email: String, password: String) {
        binding.btnRegister.isEnabled = false
        Toast.makeText(this, "Đang gửi yêu cầu đăng ký lên VPS...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val request = RegisterRequest(email = email, password = password, full_name = fullName)
                val response = withContext(Dispatchers.IO) {
                    ApiClient.instance.register(request)
                }

                binding.btnRegister.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show()
                    Log.d("RegisterActivity", "Đăng ký thành công cho email: $email")
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Đăng ký thất bại"
                    Log.e("RegisterActivity", "Đăng ký thất bại: $errorMsg")
                    Toast.makeText(this@RegisterActivity, "Đăng ký thất bại: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.btnRegister.isEnabled = true
                Log.e("RegisterActivity", "Lỗi kết nối mạng: ${e.message}", e)
                Toast.makeText(this@RegisterActivity, "Lỗi kết nối VPS: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
