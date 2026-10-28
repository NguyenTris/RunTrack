package com.example.runtrack.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R

/**
 * [SCR-HELP-01] Help Center & FAQ Screen.
 */
class HelpCenterFragment : Fragment() {

    private lateinit var rvFaq: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: FaqAdapter

    private val allFaqs = listOf(
        FaqItem(
            id = "faq_gps",
            category = "GPS & BẢN ĐỒ",
            question = "Làm sao để tối ưu độ chính xác GPS khi chạy trong hẻm hoặc nhà cao tầng?",
            answer = "Bạn hãy vào Cài đặt > Tùy chọn chạy > Độ chính xác GPS, bật 'Bộ lọc Kalman' và chọn chế độ lấy mẫu 1s (GPS Art). Điều này giúp thuật toán tự loại bỏ các điểm nhảy vọt và kết hợp thêm tín hiệu đa vệ tinh GLONASS."
        ),
        FaqItem(
            id = "faq_ble",
            category = "THIẾT BỊ NGOẠI VI",
            question = "Tại sao nhịp tim từ đai đeo ngực BLE không cập nhật trên màn hình chạy?",
            answer = "Hãy kiểm tra xem Bluetooth trên điện thoại đã được bật và pin của đai đo nhịp tim còn hoạt động. Sau đó vào Cài đặt > Thiết bị ngoại vi BLE để quét và ghép nối lại cảm biến."
        ),
        FaqItem(
            id = "faq_gpx",
            category = "DỮ LIỆU & GPX",
            question = "Cách xuất file chạy sang Strava hoặc Garmin Connect?",
            answer = "Vào Cài đặt > Xuất dữ liệu & GPX, chọn định dạng GPX (hoặc TCX nếu muốn kèm theo nhịp tim & guồng chân cadence), chọn mốc thời gian và nhấn 'Xuất & Chia sẻ file'. Bạn có thể gửi file qua Drive hoặc mở trực tiếp bằng app Strava."
        ),
        FaqItem(
            id = "faq_autopause",
            category = "TÙY CHỌN CHẠY",
            question = "Ứng dụng có tự động dừng bộ đếm khi tôi đứng chờ đèn đỏ không?",
            answer = "Có. Tính năng Tự động dừng (Auto-pause) sẽ tự động tạm ngắt tính thời gian khi vận tốc của bạn dưới 1.5 km/h và tiếp tục đếm ngay khi bạn bắt đầu sải bước trở lại."
        ),
        FaqItem(
            id = "faq_security",
            category = "TÀI KHOẢN & BẢO MẬT",
            question = "Làm thế nào để đổi mật khẩu và thu hồi phiên đăng nhập lạ?",
            answer = "Vào Cài đặt > Quyền hệ thống & Bảo mật. Tại đây bạn có thể vào mục 'Đổi mật khẩu' hoặc 'Thiết bị đã đăng nhập' để đăng xuất từ xa khỏi các phiên truy cập không an toàn."
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hud_help_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupSearch()
        setupActionButtons(view)
    }

    private fun initViews(view: View) {
        rvFaq = view.findViewById(R.id.rvFaqList)
        etSearch = view.findViewById(R.id.etSearchFaq)
    }

    private fun setupRecyclerView() {
        adapter = FaqAdapter(allFaqs)
        rvFaq.layoutManager = LinearLayoutManager(requireContext())
        rvFaq.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFaqs(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterFaqs(query: String) {
        if (query.trim().isEmpty()) {
            adapter.updateList(allFaqs)
        } else {
            val lower = query.trim().lowercase()
            val filtered = allFaqs.filter {
                it.question.lowercase().contains(lower) ||
                it.answer.lowercase().contains(lower) ||
                it.category.lowercase().contains(lower)
            }
            adapter.updateList(filtered)
        }
    }

    private fun setupActionButtons(view: View) {
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<View>(R.id.btnEmailSupport)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@runtrack.app")
                putExtra(Intent.EXTRA_SUBJECT, "[RunTrack Support] Yêu cầu hỗ trợ kỹ thuật")
            }
            try {
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Email: support@runtrack.app", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<View>(R.id.btnCallSupport)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:19006868")
            }
            try {
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Hotline: 1900 6868", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<View>(R.id.btnReportBug)?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Đang thu thập nhật ký log hệ thống để gửi báo cáo lỗi...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
