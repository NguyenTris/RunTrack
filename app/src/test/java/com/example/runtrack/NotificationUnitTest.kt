package com.example.runtrack

import com.example.runtrack.notification.NotificationCategory
import com.example.runtrack.notification.NotificationItem
import com.example.runtrack.notification.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationUnitTest {

    @Test
    fun testNotificationItemInitialization() {
        val item = NotificationItem(
            id = "test_1",
            type = NotificationType.FRIEND_REQUEST,
            category = NotificationCategory.FRIENDS,
            title = "Ngọc Mai muốn kết bạn với bạn",
            subtitle = "Cùng chạy tại Hồ Gươm",
            timeAgo = "5 PHÚT",
            primaryActionText = "CHẤP NHẬN",
            secondaryActionText = "TỪ CHỐI"
        )

        assertEquals("test_1", item.id)
        assertEquals(NotificationType.FRIEND_REQUEST, item.type)
        assertEquals(NotificationCategory.FRIENDS, item.category)
        assertEquals("Ngọc Mai muốn kết bạn với bạn", item.title)
        assertFalse(item.isRead)
        assertFalse(item.isHandled)
        assertEquals("CHẤP NHẬN", item.primaryActionText)
        assertEquals("TỪ CHỐI", item.secondaryActionText)
    }

    @Test
    fun testFriendRequestHandling() {
        val item = NotificationItem(
            id = "inv_1",
            type = NotificationType.FRIEND_REQUEST,
            category = NotificationCategory.FRIENDS,
            title = "Ngọc Mai muốn kết bạn",
            primaryActionText = "CHẤP NHẬN",
            secondaryActionText = "TỪ CHỐI"
        )

        var pendingCount = 2
        // Simulate accept action
        item.isHandled = true
        pendingCount--

        assertTrue(item.isHandled)
        assertEquals(1, pendingCount)
    }

    @Test
    fun testSystemNotificationSecurityActions() {
        val secItem = NotificationItem(
            id = "sys_sec_1",
            type = NotificationType.SYSTEM_SECURITY,
            category = NotificationCategory.SYSTEM,
            title = "Đăng nhập từ thiết bị mới",
            subtitle = "MacBook Pro · Safari · Hà Nội",
            timeAgo = "10 PHÚT",
            primaryActionText = "XEM THIẾT BỊ",
            secondaryActionText = "ĐÂY LÀ TÔI"
        )

        assertEquals(NotificationCategory.SYSTEM, secItem.category)
        assertEquals(NotificationType.SYSTEM_SECURITY, secItem.type)
        assertEquals("XEM THIẾT BỊ", secItem.primaryActionText)
        assertEquals("ĐÂY LÀ TÔI", secItem.secondaryActionText)

        // Verify handling confirmation
        secItem.isHandled = true
        assertTrue(secItem.isHandled)
    }

    @Test
    fun testSystemDeviceAndPolicyNotifications() {
        val devItem = NotificationItem(
            id = "sys_dev_1",
            type = NotificationType.SYSTEM_DEVICE,
            category = NotificationCategory.SYSTEM,
            title = "Đã đồng bộ dây đeo Polar H10",
            timeAgo = "1 GIỜ"
        )
        val polItem = NotificationItem(
            id = "sys_pol_1",
            type = NotificationType.SYSTEM_POLICY,
            category = NotificationCategory.SYSTEM,
            title = "RunTrack 2.4.1 đã sẵn sàng",
            primaryActionText = "CẬP NHẬT"
        )

        assertEquals(NotificationType.SYSTEM_DEVICE, devItem.type)
        assertEquals(NotificationType.SYSTEM_POLICY, polItem.type)
        assertEquals("CẬP NHẬT", polItem.primaryActionText)
    }

    @Test
    fun testMarkAllAsRead() {
        val list = mutableListOf(
            NotificationItem(
                id = "1",
                type = NotificationType.FRIEND_ACTIVITY,
                category = NotificationCategory.FRIENDS,
                title = "Đức Huy vừa hoàn thành 8.2 KM",
                isRead = false
            ),
            NotificationItem(
                id = "2",
                type = NotificationType.SYSTEM_DEVICE,
                category = NotificationCategory.SYSTEM,
                title = "Pin Polar H10 còn 15%",
                isRead = false
            )
        )

        // Mark all as read
        list.forEach { it.isRead = true }

        assertTrue(list.all { it.isRead })
    }

    @Test
    fun testHeaderItemProperties() {
        val header = NotificationItem(
            id = "header_sec",
            type = NotificationType.HEADER,
            category = NotificationCategory.SYSTEM,
            title = "BẢO MẬT & TÀI KHOẢN",
            badgeCount = "2 MỤC"
        )

        assertEquals(NotificationType.HEADER, header.type)
        assertEquals("2 MỤC", header.badgeCount)
        assertNull(header.primaryActionText)
    }

    @Test
    fun testConsecutiveNotificationItemRemoval() {
        val list = mutableListOf(
            NotificationItem(id = "1", type = NotificationType.FRIEND_REQUEST, category = NotificationCategory.FRIENDS, title = "Item 1"),
            NotificationItem(id = "2", type = NotificationType.FRIEND_REQUEST, category = NotificationCategory.FRIENDS, title = "Item 2"),
            NotificationItem(id = "3", type = NotificationType.FRIEND_REQUEST, category = NotificationCategory.FRIENDS, title = "Item 3")
        )

        list.removeAt(1)
        assertEquals(2, list.size)
        assertEquals("3", list[1].id)

        list.removeAt(0)
        assertEquals(1, list.size)
        assertEquals("3", list[0].id)
    }
}
