package com.example.runtrack

import com.example.runtrack.settings.DeviceType
import com.example.runtrack.settings.LoggedInDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoggedInDevicesTest {

    @Test
    fun testCurrentDeviceAttributes() {
        val currentDevice = LoggedInDevice(
            id = "current",
            name = "Samsung Galaxy S24 Ultra",
            deviceType = DeviceType.MOBILE,
            location = "Hà Nội, Việt Nam",
            clientApp = "RunTrack Android App v2.4",
            ipAddress = "118.70.14.92",
            lastActive = "Vừa xong",
            isCurrent = true
        )

        assertTrue(currentDevice.isCurrent)
        assertEquals(DeviceType.MOBILE, currentDevice.deviceType)
        assertEquals("Samsung Galaxy S24 Ultra", currentDevice.name)
    }

    @Test
    fun testRemoveSingleDeviceSession() {
        val devices = mutableListOf(
            LoggedInDevice(
                id = "dev_1",
                name = "MacBook Pro 16\"",
                deviceType = DeviceType.DESKTOP,
                location = "Hà Nội",
                clientApp = "Safari 18.1",
                ipAddress = "118.70.x.x",
                lastActive = "10 phút trước"
            ),
            LoggedInDevice(
                id = "dev_2",
                name = "iPad Pro 11\"",
                deviceType = DeviceType.TABLET,
                location = "TP. Hồ Chí Minh",
                clientApp = "RunTrack Tablet",
                ipAddress = "14.161.x.x",
                lastActive = "3 ngày trước"
            )
        )

        assertEquals(2, devices.size)

        // Remove device 1
        devices.removeAt(0)
        assertEquals(1, devices.size)
        assertEquals("dev_2", devices[0].id)
    }

    @Test
    fun testLogoutAllOtherDevices() {
        val otherDevices = mutableListOf(
            LoggedInDevice(
                id = "dev_1",
                name = "MacBook Pro",
                deviceType = DeviceType.DESKTOP,
                location = "Hà Nội",
                clientApp = "Safari",
                ipAddress = "1.1.1.1",
                lastActive = "1 giờ trước"
            ),
            LoggedInDevice(
                id = "dev_2",
                name = "Garmin Watch",
                deviceType = DeviceType.WATCH,
                location = "Cloud",
                clientApp = "Garmin Connect",
                ipAddress = "API",
                lastActive = "1 tuần trước"
            )
        )

        assertFalse(otherDevices.isEmpty())
        otherDevices.clear()
        assertTrue(otherDevices.isEmpty())
    }

    @Test
    fun testConsecutiveDeviceRemovalAndRangeSync() {
        val devices = mutableListOf(
            LoggedInDevice(id = "1", name = "Device 1", deviceType = DeviceType.MOBILE, location = "HN", clientApp = "App", ipAddress = "1.1.1.1", lastActive = "Vừa xong"),
            LoggedInDevice(id = "2", name = "Device 2", deviceType = DeviceType.TABLET, location = "HN", clientApp = "App", ipAddress = "1.1.1.2", lastActive = "Vừa xong"),
            LoggedInDevice(id = "3", name = "Device 3", deviceType = DeviceType.DESKTOP, location = "HN", clientApp = "App", ipAddress = "1.1.1.3", lastActive = "Vừa xong")
        )

        // Remove at index 0
        devices.removeAt(0)
        assertEquals(2, devices.size)
        assertEquals("2", devices[0].id)

        // Next remove at new index 0 (which was index 1)
        devices.removeAt(0)
        assertEquals(1, devices.size)
        assertEquals("3", devices[0].id)
    }
}
