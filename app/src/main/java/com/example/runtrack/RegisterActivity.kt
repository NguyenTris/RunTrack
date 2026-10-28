package com.example.runtrack

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val root = findViewById<View>(R.id.root_layout_register) ?: findViewById(R.id.root_layout_register_land)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        setupTermsAndPrivacySpannable()
        setupPasswordToggle()
        setupNavigation()
    }

    private fun setupTermsAndPrivacySpannable() {
        val tvTermsLink = findViewById<TextView>(R.id.tv_terms_link) ?: return
        val fullText = "Tôi đồng ý với Điều khoản sử dụng và Chính sách bảo mật."
        val spannable = SpannableString(fullText)

        val cyanColor = ContextCompat.getColor(this, R.color.electric_cyan)

        // 1. "Điều khoản sử dụng"
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

        // 2. "Chính sách bảo mật"
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
        val etPassword = findViewById<TextInputEditText>(R.id.et_password) ?: return
        val tvToggle = findViewById<TextView>(R.id.tv_toggle_password) ?: return

        tvToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // Show password
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                tvToggle.text = "[ẨN]"
            } else {
                // Hide password
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                tvToggle.text = "[HIỆN]"
            }
            // Move cursor to end of text
            etPassword.setSelection(etPassword.text?.length ?: 0)
        }
    }

    private fun setupNavigation() {
        // Return to Login
        val tvGoToLogin = findViewById<TextView>(R.id.tv_go_to_login)
        tvGoToLogin?.setOnClickListener {
            finish()
        }

        // Create Account Button
        val btnRegister = findViewById<MaterialButton>(R.id.btn_register)
        btnRegister?.setOnClickListener {
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
