package com.example.runtrack

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.notification.NotificationAdapter
import com.example.runtrack.notification.NotificationCategory
import com.example.runtrack.notification.NotificationItem
import com.example.runtrack.notification.NotificationType

class NotificationCenterActivity : AppCompatActivity() {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var tvSubtitle: TextView
    private lateinit var tvStatusSummary: TextView
    private lateinit var tabAll: TextView
    private lateinit var tabFriends: TextView
    private lateinit var tabSystem: TextView

    private var currentCategory = NotificationCategory.FRIENDS
    private var pendingInvitesCount = 2
    private var unreadSystemCount = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hud_notification_center)

        val root = findViewById<View>(R.id.root_notifications)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        setupRecyclerView()
        setupTabs()
        selectTab(NotificationCategory.FRIENDS)
    }

    private fun initViews() {
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        tvSubtitle = findViewById(R.id.tv_subtitle)
        tvStatusSummary = findViewById(R.id.tv_status_summary)
        tabAll = findViewById(R.id.tab_all)
        tabFriends = findViewById(R.id.tab_friends)
        tabSystem = findViewById(R.id.tab_system)
        rvNotifications = findViewById(R.id.rv_notifications)

        findViewById<View>(R.id.btnMarkAllRead)?.setOnClickListener {
            notificationAdapter.markAllAsRead()
            pendingInvitesCount = 0
            unreadSystemCount = 0
            updateSummaryText()
            tabFriends.text = "BẠN BÈ"
            tabSystem.text = "HỆ THỐNG"
            Toast.makeText(this, "Đã đánh dấu đã đọc tất cả thông báo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        rvNotifications.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter(
            items = mutableListOf(),
            onAcceptClick = { item, pos ->
                item.isHandled = true
                notificationAdapter.notifyItemChanged(pos)
                if (pendingInvitesCount > 0) pendingInvitesCount--
                updateSummaryText()
                val name = item.title.substringBefore(" muốn")
                Toast.makeText(this, "Đã chấp nhận kết bạn với $name!", Toast.LENGTH_SHORT).show()
            },
            onRejectClick = { item, pos ->
                notificationAdapter.removeItemAt(pos)
                if (pendingInvitesCount > 0) pendingInvitesCount--
                updateSummaryText()
                val name = item.title.substringBefore(" muốn")
                Toast.makeText(this, "Đã từ chối lời mời từ $name", Toast.LENGTH_SHORT).show()
            },
            onPrimaryActionClick = { item, pos ->
                item.isHandled = true
                notificationAdapter.notifyItemChanged(pos)
                when (item.primaryActionText) {
                    "CHÚC MỪNG" -> {
                        val name = item.title.substringBefore(" vừa").substringBefore(" phá")
                        Toast.makeText(this, "Đã gửi lời chúc mừng tới $name! 🎉", Toast.LENGTH_SHORT).show()
                    }
                    "XEM THIẾT BỊ" -> {
                        Toast.makeText(this, "Đang mở danh sách thiết bị đã đăng nhập...", Toast.LENGTH_SHORT).show()
                    }
                    "CẬP NHẬT" -> {
                        Toast.makeText(this, "Bạn đang dùng phiên bản RunTrack 2.4 mới nhất!", Toast.LENGTH_SHORT).show()
                    }
                    "XEM CHI TIẾT" -> {
                        Toast.makeText(this, "Điều khoản & Chính sách quyền riêng tư RunTrack v2.4", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this, "Đã xử lý thông báo", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onSecondaryActionClick = { item, pos ->
                item.isHandled = true
                notificationAdapter.notifyItemChanged(pos)
                if (item.secondaryActionText == "ĐÂY LÀ TÔI") {
                    Toast.makeText(this, "Đã xác nhận phiên đăng nhập an toàn ✓", Toast.LENGTH_SHORT).show()
                }
            }
        )
        rvNotifications.adapter = notificationAdapter
    }

    private fun setupTabs() {
        tabAll.setOnClickListener { selectTab(NotificationCategory.ALL) }
        tabFriends.setOnClickListener { selectTab(NotificationCategory.FRIENDS) }
        tabSystem.setOnClickListener { selectTab(NotificationCategory.SYSTEM) }
    }

    private fun selectTab(category: NotificationCategory) {
        currentCategory = category

        // Reset all tabs to unselected style
        tabAll.setBackgroundResource(R.drawable.bg_notif_btn_outline)
        tabAll.setTextColor(getColor(R.color.hud_text_dim))
        tabFriends.setBackgroundResource(R.drawable.bg_notif_btn_outline)
        tabFriends.setTextColor(getColor(R.color.hud_text_dim))
        tabSystem.setBackgroundResource(R.drawable.bg_notif_btn_outline)
        tabSystem.setTextColor(getColor(R.color.hud_text_dim))

        when (category) {
            NotificationCategory.ALL -> {
                tabAll.setBackgroundResource(R.drawable.hud_bg_bar_orange)
                tabAll.setTextColor(getColor(R.color.white))
                tvSubtitle.text = "Tổng hợp hoạt động và cập nhật."
                notificationAdapter.updateList(getAllNotifications())
            }
            NotificationCategory.FRIENDS -> {
                tabFriends.setBackgroundResource(R.drawable.hud_bg_bar_orange)
                tabFriends.setTextColor(getColor(R.color.white))
                tvSubtitle.text = "Hoạt động từ bạn bè của bạn."
                notificationAdapter.updateList(getFriendsNotifications())
            }
            NotificationCategory.SYSTEM -> {
                tabSystem.setBackgroundResource(R.drawable.hud_bg_bar_orange)
                tabSystem.setTextColor(getColor(R.color.white))
                tvSubtitle.text = "Bảo mật, thiết bị và cập nhật ứng dụng."
                notificationAdapter.updateList(getSystemNotifications())
            }
        }
        updateSummaryText()
    }

    private fun updateSummaryText() {
        when (currentCategory) {
            NotificationCategory.FRIENDS -> {
                tvStatusSummary.text = if (pendingInvitesCount > 0) {
                    "$pendingInvitesCount LỜI MỜI ĐANG CHỜ"
                } else {
                    "KHÔNG CÓ LỜI MỜI NÀO ĐANG CHỜ"
                }
            }
            NotificationCategory.SYSTEM -> {
                tvStatusSummary.text = if (unreadSystemCount > 0) {
                    "$unreadSystemCount CẢNH BÁO BẢO MẬT & THIẾT BỊ"
                } else {
                    "HỆ THỐNG: HOẠT ĐỘNG BÌNH THƯỜNG"
                }
            }
            NotificationCategory.ALL -> {
                val total = pendingInvitesCount + unreadSystemCount
                tvStatusSummary.text = if (total > 0) {
                    "$total MỤC CẦN CHÚ Ý"
                } else {
                    "KHÔNG CÓ THÔNG BÁO MỚI"
                }
            }
        }
    }

    private fun getFriendsNotifications(): List<NotificationItem> {
        return listOf(
            // SECTION 1: LỜI MỜI KẾT BẠN
            NotificationItem(
                id = "header_invites",
                type = NotificationType.HEADER,
                category = NotificationCategory.FRIENDS,
                title = "LỜI MỜI KẾT BẠN",
                badgeCount = "$pendingInvitesCount ĐANG CHỜ"
            ),
            NotificationItem(
                id = "f_inv_1",
                type = NotificationType.FRIEND_REQUEST,
                category = NotificationCategory.FRIENDS,
                title = "Ngọc Mai muốn kết bạn với bạn",
                subtitle = "Cùng chạy tại Hồ Gươm · 2 bạn chung",
                timeAgo = "5 PHÚT",
                avatarResId = R.drawable.ic_nav_profile,
                primaryActionText = "CHẤP NHẬN",
                secondaryActionText = "TỪ CHỐI"
            ),
            NotificationItem(
                id = "f_inv_2",
                type = NotificationType.FRIEND_REQUEST,
                category = NotificationCategory.FRIENDS,
                title = "Nguyễn Gia Phúc muốn kết bạn với bạn",
                subtitle = "Huế · pace 5’12” · 4 bạn chung",
                timeAgo = "2 GIỜ",
                avatarResId = R.drawable.ic_nav_profile,
                primaryActionText = "CHẤP NHẬN",
                secondaryActionText = "TỪ CHỐI"
            ),

            // SECTION 2: HOẠT ĐỘNG BẠN BÈ
            NotificationItem(
                id = "header_activities",
                type = NotificationType.HEADER,
                category = NotificationCategory.FRIENDS,
                title = "HOẠT ĐỘNG BẠN BÈ",
                badgeCount = "4 MỚI"
            ),
            NotificationItem(
                id = "f_act_1",
                type = NotificationType.FRIEND_ACTIVITY,
                category = NotificationCategory.FRIENDS,
                title = "Đức Huy vừa hoàn thành 8.2 KM",
                subtitle = "Vòng Hồ Tây · pace 5’20” · 43:44",
                timeAgo = "2 GIỜ",
                avatarResId = R.drawable.ic_nav_profile,
                primaryActionText = "CHÚC MỪNG"
            ),
            NotificationItem(
                id = "f_act_2",
                type = NotificationType.FRIEND_ACTIVITY,
                category = NotificationCategory.FRIENDS,
                title = "Lê Quốc Bảo thích buổi chạy của bạn",
                subtitle = "Buổi chạy sáng · 5.42 KM",
                timeAgo = "5 GIỜ",
                avatarResId = R.drawable.ic_nav_profile
            ),
            NotificationItem(
                id = "f_act_3",
                type = NotificationType.FRIEND_ACTIVITY,
                category = NotificationCategory.FRIENDS,
                title = "Phan Thu Trang phá kỷ lục 5 KM cá nhân",
                subtitle = "24:50 → 24:12 · Gửi lời chúc mừng nhé!",
                timeAgo = "HÔM QUA",
                avatarResId = R.drawable.ic_nav_profile,
                primaryActionText = "CHÚC MỪNG"
            ),
            NotificationItem(
                id = "f_act_4",
                type = NotificationType.FRIEND_ACTIVITY,
                category = NotificationCategory.FRIENDS,
                title = "Hoàng Nam bình luận: “Pace đẹp quá!”",
                subtitle = "Trên buổi chạy tối qua của bạn",
                timeAgo = "HÔM QUA",
                avatarResId = R.drawable.ic_nav_profile
            ),

            // SECTION 3: ĐÃ XỬ LÝ
            NotificationItem(
                id = "header_processed",
                type = NotificationType.HEADER,
                category = NotificationCategory.FRIENDS,
                title = "ĐÃ XỬ LÝ",
                badgeCount = "2"
            ),
            NotificationItem(
                id = "f_proc_1",
                type = NotificationType.FRIEND_PROCESSED,
                category = NotificationCategory.FRIENDS,
                title = "Bùi Tường Vy đã chấp nhận lời mời của bạn",
                subtitle = "Hai bạn giờ là bạn bè",
                timeAgo = "2 NGÀY",
                avatarResId = R.drawable.ic_nav_profile
            ),
            NotificationItem(
                id = "f_proc_2",
                type = NotificationType.FRIEND_PROCESSED,
                category = NotificationCategory.FRIENDS,
                title = "Bạn và Vũ Hải đã trở thành bạn bè",
                subtitle = "Cùng nhóm Sunrise Runners",
                timeAgo = "3 NGÀY",
                avatarResId = R.drawable.ic_nav_profile
            )
        )
    }

    private fun getSystemNotifications(): List<NotificationItem> {
        return listOf(
            // SECTION 1: BẢO MẬT & TÀI KHOẢN
            NotificationItem(
                id = "header_security",
                type = NotificationType.HEADER,
                category = NotificationCategory.SYSTEM,
                title = "BẢO MẬT & TÀI KHOẢN",
                badgeCount = "2 MỤC"
            ),
            NotificationItem(
                id = "sys_sec_1",
                type = NotificationType.SYSTEM_SECURITY,
                category = NotificationCategory.SYSTEM,
                title = "Đăng nhập từ thiết bị mới",
                subtitle = "MacBook Pro · Safari · Hà Nội. Nếu không phải bạn, hãy đổi mật khẩu ngay.",
                timeAgo = "10 PHÚT",
                avatarResId = R.drawable.ic_shield_security,
                primaryActionText = "XEM THIẾT BỊ",
                secondaryActionText = "ĐÂY LÀ TÔI"
            ),
            NotificationItem(
                id = "sys_sec_2",
                type = NotificationType.SYSTEM_SECURITY,
                category = NotificationCategory.SYSTEM,
                title = "Mật khẩu đã được đổi thành công",
                subtitle = "Lần đổi gần nhất: 12/10/2026",
                timeAgo = "3 NGÀY",
                avatarResId = R.drawable.ic_shield_security
            ),

            // SECTION 2: THIẾT BỊ & ĐỒNG BỘ
            NotificationItem(
                id = "header_devices",
                type = NotificationType.HEADER,
                category = NotificationCategory.SYSTEM,
                title = "THIẾT BỊ & ĐỒNG BỘ",
                badgeCount = "2 MỤC"
            ),
            NotificationItem(
                id = "sys_dev_1",
                type = NotificationType.SYSTEM_DEVICE,
                category = NotificationCategory.SYSTEM,
                title = "Đã đồng bộ dây đeo Polar H10",
                subtitle = "Nhịp tim và GPS hoạt động bình thường · Pin 82%",
                timeAgo = "1 GIỜ",
                avatarResId = R.drawable.ic_bluetooth
            ),
            NotificationItem(
                id = "sys_dev_2",
                type = NotificationType.SYSTEM_DEVICE,
                category = NotificationCategory.SYSTEM,
                title = "Pin Polar H10 còn 15%",
                subtitle = "Hãy sạc trước buổi chạy dài tiếp theo",
                timeAgo = "3 GIỜ",
                avatarResId = R.drawable.ic_bluetooth
            ),

            // SECTION 3: ỨNG DỤNG & CHÍNH SÁCH
            NotificationItem(
                id = "header_app_policy",
                type = NotificationType.HEADER,
                category = NotificationCategory.SYSTEM,
                title = "ỨNG DỤNG & CHÍNH SÁCH",
                badgeCount = "3 MỤC"
            ),
            NotificationItem(
                id = "sys_pol_1",
                type = NotificationType.SYSTEM_POLICY,
                category = NotificationCategory.SYSTEM,
                title = "RunTrack 2.4.1 đã sẵn sàng",
                subtitle = "Thêm chạy theo lộ trình chia sẻ, nhóm và thông báo mới",
                timeAgo = "1 NGÀY",
                avatarResId = R.drawable.ic_system_update,
                primaryActionText = "CẬP NHẬT"
            ),
            NotificationItem(
                id = "sys_pol_2",
                type = NotificationType.SYSTEM_POLICY,
                category = NotificationCategory.SYSTEM,
                title = "Điều khoản & chính sách riêng tư được cập nhật",
                subtitle = "Có hiệu lực từ 01/11/2026",
                timeAgo = "2 NGÀY",
                avatarResId = R.drawable.ic_system_update,
                primaryActionText = "XEM CHI TIẾT"
            ),
            NotificationItem(
                id = "sys_pol_3",
                type = NotificationType.SYSTEM_POLICY,
                category = NotificationCategory.SYSTEM,
                title = "Bảo trì hệ thống 02:00–03:00 ngày 25/10",
                subtitle = "Một số tính năng bản đồ có thể gián đoạn",
                timeAgo = "3 NGÀY",
                avatarResId = R.drawable.ic_system_update
            )
        )
    }

    private fun getAllNotifications(): List<NotificationItem> {
        val all = mutableListOf<NotificationItem>()
        all.addAll(getFriendsNotifications())
        all.addAll(getSystemNotifications())
        return all
    }
}
