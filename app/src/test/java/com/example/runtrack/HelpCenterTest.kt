package com.example.runtrack

import com.example.runtrack.settings.FaqItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HelpCenterTest {

    private val testFaqs = listOf(
        FaqItem(
            id = "faq_1",
            category = "GPS & BẢN ĐỒ",
            question = "Làm sao để tối ưu độ chính xác GPS?",
            answer = "Bật bộ lọc Kalman trong cài đặt GPS."
        ),
        FaqItem(
            id = "faq_2",
            category = "THIẾT BỊ NGOẠI VI",
            question = "Tại sao nhịp tim BLE không nhận?",
            answer = "Kiểm tra pin và kết nối Bluetooth."
        ),
        FaqItem(
            id = "faq_3",
            category = "DỮ LIỆU & GPX",
            question = "Cách xuất file GPX sang Strava?",
            answer = "Vào mục Xuất dữ liệu & GPX để tải file."
        )
    )

    @Test
    fun testFaqItemCreationAndToggle() {
        val item = FaqItem(
            id = "test_id",
            category = "GENERAL",
            question = "Sample Question?",
            answer = "Sample Answer."
        )

        assertEquals("test_id", item.id)
        assertFalse(item.isExpanded)

        item.isExpanded = true
        assertTrue(item.isExpanded)
    }

    @Test
    fun testFilterFaqListByQuestion() {
        val query = "Strava"
        val filtered = testFaqs.filter {
            it.question.contains(query, ignoreCase = true) ||
            it.answer.contains(query, ignoreCase = true)
        }

        assertEquals(1, filtered.size)
        assertEquals("faq_3", filtered[0].id)
    }

    @Test
    fun testFilterFaqListByCategory() {
        val query = "NGOẠI VI"
        val filtered = testFaqs.filter {
            it.category.contains(query, ignoreCase = true)
        }

        assertEquals(1, filtered.size)
        assertEquals("faq_2", filtered[0].id)
    }

    @Test
    fun testFilterFaqListEmptyQuery() {
        val query = ""
        val filtered = if (query.isEmpty()) testFaqs else testFaqs.filter { it.question.contains(query) }

        assertEquals(3, filtered.size)
    }
}
