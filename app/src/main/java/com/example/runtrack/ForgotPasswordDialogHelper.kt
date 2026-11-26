package com.example.runtrack

import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.model.auth.ForgotPasswordRequest
import com.example.runtrack.network.model.auth.ResetPasswordRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ForgotPasswordDialogHelper {

    fun show(context: Context, scope: CoroutineScope, prefillEmail: String = "") {
        val etEmail = EditText(context).apply {
            hint = "Nhập email tài khoản"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(prefillEmail)
        }
        val container = createDialogContainer(context, etEmail)

        AlertDialog.Builder(context)
            .setTitle("Quên mật khẩu (Bước 1/2)")
            .setMessage("Nhập email để nhận mã OTP 6 số đặt lại mật khẩu.")
            .setView(container)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Gửi mã OTP") { _, _ ->
                val email = etEmail.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                } else {
                    requestOtp(context, scope, email)
                }
            }
            .show()
    }

    private fun requestOtp(context: Context, scope: CoroutineScope, email: String) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.instance.forgotPassword(ForgotPasswordRequest(email))
                }
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Toast.makeText(
                        context,
                        body.message.ifBlank { "Đã gửi mã OTP tới email" },
                        Toast.LENGTH_SHORT
                    ).show()
                    showResetPasswordStep(context, scope, email, body.demo_otp)
                } else {
                    Toast.makeText(context, "Không tìm thấy email hoặc lỗi máy chủ", Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Lỗi kết nối máy chủ khi gửi OTP", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showResetPasswordStep(
        context: Context,
        scope: CoroutineScope,
        email: String,
        demoOtp: String?
    ) {
        val tvDemoHint = TextView(context).apply {
            text = if (!demoOtp.isNullOrBlank()) {
                "Email: $email\n[DEMO OTP]: $demoOtp (hiệu lực 10 phút)"
            } else {
                "Mã OTP 6 số đã được gửi tới: $email"
            }
            setPadding(0, 0, 0, 16)
        }
        val etOtp = EditText(context).apply {
            hint = "Nhập mã OTP 6 số"
            inputType = InputType.TYPE_CLASS_NUMBER
            if (!demoOtp.isNullOrBlank()) setText(demoOtp)
        }
        val etNewPass = EditText(context).apply {
            hint = "Nhập mật khẩu mới (tối thiểu 6 ký tự)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val container = createDialogContainer(context, tvDemoHint, etOtp, etNewPass)

        AlertDialog.Builder(context)
            .setTitle("Đặt lại mật khẩu (Bước 2/2)")
            .setView(container)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xác nhận") { _, _ ->
                val otp = etOtp.text.toString().trim()
                val newPass = etNewPass.text.toString().trim()
                if (otp.length != 6 || newPass.length < 6) {
                    Toast.makeText(context, "OTP phải đủ 6 số và mật khẩu ≥ 6 ký tự", Toast.LENGTH_LONG).show()
                } else {
                    submitResetPassword(context, scope, email, otp, newPass)
                }
            }
            .show()
    }

    private fun submitResetPassword(
        context: Context,
        scope: CoroutineScope,
        email: String,
        otp: String,
        newPassword: String
    ) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.instance.resetPassword(
                        ResetPasswordRequest(email = email, otp = otp, new_password = newPassword)
                    )
                }
                if (response.isSuccessful) {
                    Toast.makeText(context, "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Mã OTP không hợp lệ hoặc đã hết hạn", Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Lỗi kết nối máy chủ khi đặt lại mật khẩu", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createDialogContainer(context: Context, vararg views: android.view.View): LinearLayout {
        val pad = (20 * context.resources.displayMetrics.density).toInt()
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, 0)
            views.forEach { addView(it) }
        }
    }
}
