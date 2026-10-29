package com.example.runtrack

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressAndRegistrationTest {

    @Test
    fun testAchievementModel() {
        val unlockedBadge = Achievement("1", "5K < 25'", R.drawable.ic_trophy, isUnlocked = true)
        assertEquals("1", unlockedBadge.id)
        assertEquals("5K < 25'", unlockedBadge.title)
        assertTrue(unlockedBadge.isUnlocked)

        val lockedBadge = Achievement("2", "100 KM/THÁNG", R.drawable.ic_lock, isUnlocked = false)
        assertFalse(lockedBadge.isUnlocked)
    }

    @Test
    fun testCalendarDayModel() {
        val emptyOffsetDay = CalendarDay(0, 0)
        assertEquals(0, emptyOffsetDay.dayNumber)
        assertEquals(0, emptyOffsetDay.heatLevel)
        assertFalse(emptyOffsetDay.isSelected)

        val activeDay = CalendarDay(dayNumber = 21, heatLevel = 3, isSelected = true)
        assertEquals(21, activeDay.dayNumber)
        assertEquals(3, activeDay.heatLevel)
        assertTrue(activeDay.isSelected)
    }

    @Test
    fun testMonthRecordModel() {
        val record = MonthRecord("1", "5 KM nhanh nhất", "04/10 · HỒ GƯƠM", "24:12", R.drawable.ic_trophy)
        assertEquals("1", record.id)
        assertEquals("5 KM nhanh nhất", record.title)
        assertEquals("04/10 · HỒ GƯƠM", record.subtitle)
        assertEquals("24:12", record.recordValue)
        assertEquals(R.drawable.ic_trophy, record.iconResId)
    }

    @Test
    fun testQuarterStatModel() {
        val stat = QuarterStat("Q4", distanceKm = 128.6f, maxKm = 400f, isCurrentQuarter = true)
        assertEquals("Q4", stat.quarterName)
        assertEquals(128.6f, stat.distanceKm, 0.01f)
        assertEquals(400f, stat.maxKm, 0.01f)
        assertTrue(stat.isCurrentQuarter)

        // Progress percentage calculation
        val progressPercentage = (stat.distanceKm / stat.maxKm) * 100
        assertEquals(32.15f, progressPercentage, 0.1f)
    }

    @Test
    fun testRecentActivityModel() {
        val activity = RecentActivity("1", "T4", "21/10", "Chạy sáng - Hồ GƯƠM", "28:47 · 5'18\"/KM", "5.42")
        assertEquals("1", activity.id)
        assertEquals("T4", activity.dayCode)
        assertEquals("21/10", activity.dateText)
        assertEquals("Chạy sáng - Hồ GƯƠM", activity.title)
        assertEquals("28:47 · 5'18\"/KM", activity.durationAndPace)
        assertEquals("5.42", activity.distanceKm)
    }
}
