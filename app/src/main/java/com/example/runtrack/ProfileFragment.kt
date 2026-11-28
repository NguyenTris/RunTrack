package com.example.runtrack

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
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

class ProfileFragment : Fragment() {

    private var ivUserAvatar: ImageView? = null
    private var tvRunnerName: TextView? = null
    private var tvRunnerIdCode: TextView? = null
    private var tvRunnerTag: TextView? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { uploadAvatarImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivUserAvatar = view.findViewById(R.id.iv_user_avatar)
        tvRunnerName = view.findViewById(R.id.tv_runner_name_profile)
        tvRunnerIdCode = view.findViewById(R.id.tv_runner_id_code)
        tvRunnerTag = view.findViewById(R.id.tv_runner_tag)

        val frameAvatarBox = view.findViewById<View>(R.id.frame_avatar_box)
        frameAvatarBox?.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Open HUD Settings & Peripherals
        val openSettingsAction = {
            startActivity(Intent(requireContext(), HudSettingsActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_settings_hub)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.btn_settings_hub_item)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.layout_settings_container)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.layout_devices_container)?.setOnClickListener { openSettingsAction() }
        view.findViewById<View>(R.id.card_hr_zones)?.setOnClickListener { openSettingsAction() }

        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            btnLogout.isEnabled = false
            performLogout()
        }

        val tvBtnHome = view.findViewById<TextView>(R.id.tv_btn_home)
        tvBtnHome?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        loadProfileData()
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun loadProfileData() {
        val context = context ?: return
        val cachedName = SessionManager.getFullName(context)
        val cachedUserId = SessionManager.getUserId(context)
        val cachedAvatarUrl = SessionManager.getAvatarUrl(context)

        updateProfileUi(cachedName, cachedUserId, cachedAvatarUrl)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(context).getCurrentUser()
                }
                val user = response.body()
                if (response.isSuccessful && user != null) {
                    val name = if (!user.full_name.isNullOrBlank()) user.full_name else user.email.substringBefore("@")
                    SessionManager.saveFullName(context, name)
                    SessionManager.saveUserId(context, user.id)
                    user.avatar_url?.let { SessionManager.saveAvatarUrl(context, it) }

                    updateProfileUi(name, user.id, user.avatar_url ?: cachedAvatarUrl)
                } else {
                    val profileRes = withContext(Dispatchers.IO) {
                        ApiClient.getAuthenticatedService(context).getUserProfile()
                    }
                    val dto = profileRes.body()
                    if (profileRes.isSuccessful && dto != null) {
                        val name = dto.full_name
                        SessionManager.saveFullName(context, name)
                        dto.id?.let { SessionManager.saveUserId(context, it) }
                        dto.avatar_url?.let { SessionManager.saveAvatarUrl(context, it) }

                        updateProfileUi(name, dto.id ?: cachedUserId, dto.avatar_url ?: cachedAvatarUrl)
                    }
                }
            } catch (_: Exception) {
                // Sử dụng dữ liệu offline đã lưu trong SessionManager
            }
        }
    }

    private fun updateProfileUi(fullName: String, userId: Int, avatarUrl: String?) {
        tvRunnerName?.text = fullName
        val displayId = if (userId > 0) "ID: PR-$userId" else "ID: PR-3452"
        tvRunnerIdCode?.text = displayId
        tvRunnerTag?.text = if (userId > 0) "NGƯỜI CHẠY / PR-$userId" else "NGƯỜI CHẠY / PR-3452"

        if (!avatarUrl.isNullOrBlank() && ivUserAvatar != null && isAdded) {
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
        val ctx = context ?: return
        val multipartPart = createMultipartBodyPart(ctx, uri)
        if (multipartPart == null) {
            Toast.makeText(ctx, "Không thể mở file ảnh đã chọn", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(ctx, "Đang tải ảnh đại diện lên...", Toast.LENGTH_SHORT).show()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(ctx).uploadAvatar(multipartPart)
                }
                val user = response.body()
                if (response.isSuccessful && user != null) {
                    val name = if (!user.full_name.isNullOrBlank()) user.full_name else user.email.substringBefore("@")
                    SessionManager.saveFullName(ctx, name)
                    SessionManager.saveUserId(ctx, user.id)
                    user.avatar_url?.let { SessionManager.saveAvatarUrl(ctx, it) }

                    updateProfileUi(name, user.id, user.avatar_url)
                    Toast.makeText(ctx, "Đã cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(ctx, "Upload thất bại: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Lỗi kết nối khi upload ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
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
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedService(appContext).logout()
                }
            } catch (_: Exception) {
                // Luôn xóa phiên đăng nhập cục bộ kể cả khi ngoại tuyến
            } finally {
                SessionManager.setLoggedIn(appContext, false)
                SessionManager.clearAuthToken(appContext)

                if (isAdded) {
                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finishAffinity()
                }
            }
        }
    }
}
