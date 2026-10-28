package com.example.runtrack

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * [SCR-SEC-01] Change Password Screen.
 * Validates current password, new password strength, and confirmation match.
 */
class ChangePasswordFragment : Fragment() {

    private lateinit var tilCurrentPassword: TextInputLayout
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var tilNewPassword: TextInputLayout
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText

    private lateinit var tvReqLengthIcon: TextView
    private lateinit var tvReqLength: TextView
    private lateinit var tvReqCaseIcon: TextView
    private lateinit var tvReqCase: TextView
    private lateinit var tvReqSpecialIcon: TextView
    private lateinit var tvReqSpecial: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hud_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners(view)
    }

    private fun initViews(view: View) {
        tilCurrentPassword = view.findViewById(R.id.til_current_password)
        etCurrentPassword = view.findViewById(R.id.et_current_password)
        tilNewPassword = view.findViewById(R.id.til_new_password)
        etNewPassword = view.findViewById(R.id.et_new_password)
        tilConfirmPassword = view.findViewById(R.id.til_confirm_password)
        etConfirmPassword = view.findViewById(R.id.et_confirm_password)

        tvReqLengthIcon = view.findViewById(R.id.tv_req_length_icon)
        tvReqLength = view.findViewById(R.id.tv_req_length)
        tvReqCaseIcon = view.findViewById(R.id.tv_req_case_icon)
        tvReqCase = view.findViewById(R.id.tv_req_case)
        tvReqSpecialIcon = view.findViewById(R.id.tv_req_special_icon)
        tvReqSpecial = view.findViewById(R.id.tv_req_special)
    }

    private fun setupListeners(view: View) {
        // Back & Cancel
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        view.findViewById<View>(R.id.btnCancel)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Forgot password
        view.findViewById<View>(R.id.btnForgotPassword)?.setOnClickListener {
            Toast.makeText(requireContext(), "Hướng dẫn đặt lại mật khẩu đã được gửi tới email của bạn.", Toast.LENGTH_SHORT).show()
        }

        // Real-time strength tracker
        etNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePasswordStrength(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Save password action
        view.findViewById<View>(R.id.btnSavePassword)?.setOnClickListener {
            handleSavePassword()
        }
    }

    private fun validatePasswordStrength(password: String): Boolean {
        val hasLength = password.length >= 8
        val hasUpperAndLower = password.any { it.isUpperCase() } && password.any { it.isLowerCase() }
        val hasDigitOrSpecial = password.any { it.isDigit() || !it.isLetterOrDigit() }

        updateChecklistView(tvReqLengthIcon, tvReqLength, hasLength)
        updateChecklistView(tvReqCaseIcon, tvReqCase, hasUpperAndLower)
        updateChecklistView(tvReqSpecialIcon, tvReqSpecial, hasDigitOrSpecial)

        return hasLength && hasUpperAndLower && hasDigitOrSpecial
    }

    private fun updateChecklistView(iconView: TextView, textView: TextView, isValid: Boolean) {
        val context = context ?: return
        if (isValid) {
            iconView.text = "●"
            iconView.setTextColor(ContextCompat.getColor(context, R.color.hud_signal))
            textView.setTextColor(ContextCompat.getColor(context, R.color.hud_signal))
        } else {
            iconView.text = "○"
            iconView.setTextColor(ContextCompat.getColor(context, R.color.hud_text_dim))
            textView.setTextColor(ContextCompat.getColor(context, R.color.hud_text_dim))
        }
    }

    private fun handleSavePassword() {
        tilCurrentPassword.error = null
        tilNewPassword.error = null
        tilConfirmPassword.error = null

        val currentPass = etCurrentPassword.text?.toString().orEmpty()
        val newPass = etNewPassword.text?.toString().orEmpty()
        val confirmPass = etConfirmPassword.text?.toString().orEmpty()

        if (currentPass.isBlank()) {
            tilCurrentPassword.error = "Vui lòng nhập mật khẩu hiện tại"
            return
        }

        if (!validatePasswordStrength(newPass)) {
            tilNewPassword.error = "Mật khẩu mới chưa đáp ứng đầy đủ tiêu chuẩn"
            return
        }

        if (newPass == currentPass) {
            tilNewPassword.error = "Mật khẩu mới không được trùng với mật khẩu cũ"
            return
        }

        if (newPass != confirmPass) {
            tilConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            return
        }

        Toast.makeText(requireContext(), "Mật khẩu đã được cập nhật thành công! ✓", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}
