package com.example.runtrack

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.ApiConfig
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private var ivUserAvatar: ImageView? = null
    private var tvRunnerName: TextView? = null
    private var tvRunnerIdCode: TextView? = null
    private var tvRunnerTag: TextView? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { uploadAvatarImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_profile)

        val root = findViewById<android.view.View>(R.id.root_layout_profile)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        ivUserAvatar = findViewById(R.id.iv_user_avatar)
        tvRunnerName = findViewById(R.id.tv_runner_name_profile)
        tvRunnerIdCode = findViewById(R.id.tv_runner_id_code)
        tvRunnerTag = findViewById(R.id.tv_runner_tag)

        val frameAvatarBox = findViewById<android.view.View>(R.id.frame_avatar_box)
        frameAvatarBox?.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Open HUD Settings & Peripherals
        val openSettingsAction = {
            startActivity(Intent(this, HudSettingsActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btn_settings_hub)?.setOnClickListener { openSettingsAction() }
        findViewById<android.view.View>(R.id.btn_settings_hub_item)?.setOnClickListener { openSettingsAction() }
        findViewById<android.view.View>(R.id.layout_settings_container)?.setOnClickListener { openSettingsAction() }
        findViewById<android.view.View>(R.id.layout_devices_container)?.setOnClickListener { openSettingsAction() }
        findViewById<android.view.View>(R.id.card_hr_zones)?.setOnClickListener { openSettingsAction() }

        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            btnLogout.isEnabled = false
            performLogout()
        }

        val tvBtnHome = findViewById<TextView>(R.id.tv_btn_home)
        tvBtnHome?.setOnClickListener {
            finish()
        }

        loadProfileData()
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun loadProfileData() {
        val cachedName = SessionManager.getFullName(this)
        val cachedUserId = SessionManager.getUserId(this)
        val cachedAvatarUrl = SessionManager.getAvatarUrl(this)

        updateProfileUi(cachedName, cachedUserId, cachedAvatarUrl)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@ProfileActivity).getCurrentUser()
                }
                val user = response.body()
                if (response.isSuccessful && user != null) {
                    val name = if (!user.full_name.isNullOrBlank()) user.full_name else user.email.substringBefore("@")
                    SessionManager.saveFullName(this@ProfileActivity, name)
                    SessionManager.saveUserId(this@ProfileActivity, user.id)
                    user.avatar_url?.let { SessionManager.saveAvatarUrl(this@ProfileActivity, it) }

                    updateProfileUi(name, user.id, user.avatar_url ?: cachedAvatarUrl)
                } else {
                    val profileRes = withContext(Dispatchers.IO) {
                        ApiClient.getAuthenticatedService(this@ProfileActivity).getUserProfile()
                    }
                    val dto = profileRes.body()
                    if (profileRes.isSuccessful && dto != null) {
                        val name = dto.full_name
                        SessionManager.saveFullName(this@ProfileActivity, name)
                        dto.id?.let { SessionManager.saveUserId(this@ProfileActivity, it) }
                        dto.avatar_url?.let { SessionManager.saveAvatarUrl(this@ProfileActivity, it) }

                        updateProfileUi(name, dto.id ?: cachedUserId, dto.avatar_url ?: cachedAvatarUrl)
                    }
                }
            } catch (_: Exception) {
                // Offline fallback
            }
        }
    }

    private fun updateProfileUi(fullName: String, userId: Int, avatarUrl: String?) {
        tvRunnerName?.text = fullName
        val displayId = if (userId > 0) "ID: PR-$userId" else "ID: PR-3452"
        tvRunnerIdCode?.text = displayId
        tvRunnerTag?.text = if (userId > 0) "NGƯỜI CHẠY / PR-$userId" else "NGƯỜI CHẠY / PR-3452"

        if (!avatarUrl.isNullOrBlank() && ivUserAvatar != null && !isFinishing) {
            val fullUrl = if (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://")) {
                avatarUrl
            } else {
                ApiConfig.BASE_URL.trimEnd('/') + "/" + avatarUrl.trimStart('/')
            }
            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_nav_profile)
                .error(R.drawable.ic_nav_profile)
                .centerCrop()
                .into(ivUserAvatar!!)
        }
    }

    private fun uploadAvatarImage(uri: Uri) {
        val multipartPart = createMultipartBodyPart(this, uri)
        if (multipartPart == null) {
            Toast.makeText(this, "Không thể mở file ảnh đã chọn", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Đang tải ảnh đại diện lên...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@ProfileActivity).uploadAvatar(multipartPart)
                }
                val user = response.body()
                if (response.isSuccessful && user != null) {
                    val name = if (!user.full_name.isNullOrBlank()) user.full_name else user.email.substringBefore("@")
                    SessionManager.saveFullName(this@ProfileActivity, name)
                    SessionManager.saveUserId(this@ProfileActivity, user.id)
                    user.avatar_url?.let { SessionManager.saveAvatarUrl(this@ProfileActivity, it) }

                    updateProfileUi(name, user.id, user.avatar_url)
                    Toast.makeText(this@ProfileActivity, "Đã cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "Upload thất bại: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Lỗi kết nối khi upload ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createMultipartBodyPart(context: Context, uri: Uri): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "avatar_upload_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            val mimeType = context.contentResolver.getType(uri) ?: "image/*"
            val requestFile = RequestBody.create(MediaType.parse(mimeType), tempFile)
            MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(this@ProfileActivity).logout()
                }
            } catch (_: Exception) {
                // Luôn xóa phiên đăng nhập cục bộ kể cả khi ngoại tuyến
            } finally {
                SessionManager.setLoggedIn(this@ProfileActivity, false)
                SessionManager.clearAuthToken(this@ProfileActivity)

                val intent = Intent(this@ProfileActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finishAffinity()
            }
        }
    }
}
