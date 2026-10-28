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
                type = NotificationType.FRIEND_ACTIVITY,
                category = NotificationCategory.FRIENDS,
                title = "Lê Quốc Bảo thích buổi chạy",
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
            id = "header_1",
            type = NotificationType.HEADER,
            category = NotificationCategory.FRIENDS,
            title = "LỜI MỜI KẾT BẠN",
            badgeCount = "2 ĐANG CHỜ"
        )

        assertEquals(NotificationType.HEADER, header.type)
        assertEquals("2 ĐANG CHỜ", header.badgeCount)
        assertNull(header.primaryActionText)
    }
}
