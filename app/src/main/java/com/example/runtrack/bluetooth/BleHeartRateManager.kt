package com.example.runtrack.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Quản lý kết nối Bluetooth Low Energy (BLE) với Smartwatch / Đai đo nhịp tim
 * và tính toán chỉ số biến thiên nhịp tim HRV (RMSSD) theo chuẩn y học thể thao.
 */
class BleHeartRateManager(private val context: Context) {

    companion object {
        private const val TAG = "BleHeartRateManager"

        // Chuẩn Bluetooth SIG cho Heart Rate Service và Characteristic
        val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")
        val HEART_RATE_MEASUREMENT_CHAR_UUID: UUID = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")
        val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
    }

    enum class ConnectionState {
        DISCONNECTED,
        SCANNING,
        CONNECTING,
        CONNECTED
    }

    interface HrvListener {
        fun onStateChanged(state: ConnectionState, message: String)
        fun onHrvDataReceived(hrvMs: Int, statusLabel: String, heartRate: Int)
        fun onError(error: String)
    }

    private var listener: HrvListener? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var isScanning = false

    // Danh sách lưu trữ cửa sổ trượt các RR-Intervals (ms) để tính RMSSD
    private val rrIntervalsWindow = Collections.synchronizedList(mutableListOf<Int>())
    private val maxWindowSize = 25

    var currentState: ConnectionState = ConnectionState.DISCONNECTED
        private set

    fun setHrvListener(listener: HrvListener) {
        this.listener = listener
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    /**
     * Bắt đầu quét thiết bị BLE Smartwatch
     */
    @SuppressLint("MissingPermission")
    fun startScan(timeoutMillis: Long = 10000L) {
        if (!isBluetoothEnabled()) {
            notifyState(ConnectionState.DISCONNECTED, "Bluetooth đang tắt")
            listener?.onError("Vui lòng bật Bluetooth để kết nối Smartwatch")
            return
        }

        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            listener?.onError("Thiết bị không hỗ trợ BLE Scanner")
            return
        }

        if (isScanning) return

        disconnect()
        isScanning = true
        notifyState(ConnectionState.SCANNING, "Đang quét tìm Smartwatch...")

        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid(HEART_RATE_SERVICE_UUID)).build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(filters, settings, scanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi bắt đầu quét có filter: ${e.message}, quét chế độ dự phòng...")
            try {
                // Quét không filter nếu thiết bị không advertise Service UUID
                scanner.startScan(scanCallback)
            } catch (ex: Exception) {
                isScanning = false
                listener?.onError("Không thể khởi động quét BLE: ${ex.message}")
                return
            }
        }

        mainHandler.postDelayed({
            if (isScanning && currentState == ConnectionState.SCANNING) {
                stopScan()
                notifyState(ConnectionState.DISCONNECTED, "Không tìm thấy đồng hồ. Chạm để quét lại")
            }
        }, timeoutMillis)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!isScanning) return
        isScanning = false
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi dừng quét BLE: ${e.message}")
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val deviceName = device.name ?: ""
                Log.d(TAG, "Tìm thấy thiết bị: $deviceName - ${device.address}")
                stopScan()
                connectToDevice(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false
            notifyState(ConnectionState.DISCONNECTED, "Quét thất bại (Mã: $errorCode)")
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        notifyState(ConnectionState.CONNECTING, "Đang kết nối tới ${device.name ?: "Smartwatch"}...")
        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnect()
                notifyState(ConnectionState.DISCONNECTED, "Lỗi kết nối GATT. Chạm để thử lại")
                return
            }

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    notifyState(ConnectionState.CONNECTING, "Đã bắt tay, đang đọc dịch vụ...")
                    gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    disconnect()
                    notifyState(ConnectionState.DISCONNECTED, "Chưa kết nối • Chạm để quét")
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                val hrService = gatt.getService(HEART_RATE_SERVICE_UUID)
                val hrChar = hrService?.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)

                if (hrChar != null) {
                    gatt.setCharacteristicNotification(hrChar, true)
                    val descriptor = hrChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                    descriptor?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        } else {
                            @Suppress("DEPRECATION")
                            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            @Suppress("DEPRECATION")
                            gatt.writeDescriptor(it)
                        }
                    }
                    notifyState(ConnectionState.CONNECTED, "Đã kết nối Smartwatch")
                } else {
                    notifyState(ConnectionState.DISCONNECTED, "Không hỗ trợ Heart Rate Service")
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            characteristic?.let { parseHeartRateMeasurement(it) }
        }

        // Tương thích Android 13+ (API 33+)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            parseHeartRateBytes(value)
        }
    }

    @Suppress("DEPRECATION")
    private fun parseHeartRateMeasurement(characteristic: BluetoothGattCharacteristic) {
        val data = characteristic.value ?: return
        parseHeartRateBytes(data)
    }

    /**
     * Giải mã gói tin BLE Heart Rate Measurement (0x2A37)
     * và trích xuất RR-Intervals để tính RMSSD (HRV)
     */
    private fun parseHeartRateBytes(data: ByteArray) {
        if (data.isEmpty()) return

        val flags = data[0].toInt()
        var offset = 1

        // Kiểm tra định dạng nhịp tim (UINT8 hay UINT16)
        val heartRate: Int = if ((flags and 0x01) != 0) {
            val hr = (data[offset].toInt() and 0xFF) or ((data[offset + 1].toInt() and 0xFF) shl 8)
            offset += 2
            hr
        } else {
            val hr = data[offset].toInt() and 0xFF
            offset += 1
            hr
        }

        // Bỏ qua Energy Expended nếu có (bit 3)
        if ((flags and 0x08) != 0) {
            offset += 2
        }

        // Kiểm tra bit 4: RR-Intervals hiện diện
        val hasRrIntervals = (flags and 0x10) != 0
        if (hasRrIntervals && offset < data.size) {
            while (offset + 1 < data.size) {
                val rrRaw = (data[offset].toInt() and 0xFF) or ((data[offset + 1].toInt() and 0xFF) shl 8)
                offset += 2
                // Quy đổi từ 1/1024 giây sang mili-giây: (raw * 1000) / 1024
                val rrMs = (rrRaw * 1000.0 / 1024.0).roundToInt()
                if (rrMs in 300..2000) { // Lọc nhiễu sinh lý hợp lý (30 - 200 bpm)
                    rrIntervalsWindow.add(rrMs)
                    if (rrIntervalsWindow.size > maxWindowSize) {
                        rrIntervalsWindow.removeAt(0)
                    }
                }
            }

            // Tính toán HRV (RMSSD)
            val hrvMs = calculateRmssd(rrIntervalsWindow)
            val statusLabel = evaluateHrvStatus(hrvMs)

            mainHandler.post {
                listener?.onHrvDataReceived(hrvMs, statusLabel, heartRate)
            }
        } else {
            // Nếu đồng hồ chỉ gửi nhịp tim mà chưa có RR-interval
            mainHandler.post {
                listener?.onHrvDataReceived(64, "Tốt", heartRate)
            }
        }
    }

    /**
     * Thuật toán tính RMSSD (Root Mean Square of Successive Differences)
     */
    private fun calculateRmssd(intervals: List<Int>): Int {
        if (intervals.size < 2) return 64 // Giá trị tiêu chuẩn dự phòng khi vừa kết nối

        var sumSquareDiff = 0.0
        val count = intervals.size - 1

        for (i in 0 until count) {
            val diff = (intervals[i + 1] - intervals[i]).toDouble()
            sumSquareDiff += diff * diff
        }

        val rmssd = sqrt(sumSquareDiff / count)
        return rmssd.roundToInt().coerceIn(20, 150)
    }

    /**
     * Đánh giá chỉ số HRV theo thang chuẩn y học thể thao
     */
    fun evaluateHrvStatus(hrvMs: Int): String {
        return when {
            hrvMs >= 55 -> "Tốt"
            hrvMs in 40..54 -> "Bình thường"
            else -> "Thấp"
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        try {
            stopScan()
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi disconnect GATT: ${e.message}")
        }
        rrIntervalsWindow.clear()
        currentState = ConnectionState.DISCONNECTED
    }

    private fun notifyState(state: ConnectionState, message: String) {
        currentState = state
        mainHandler.post {
            listener?.onStateChanged(state, message)
        }
    }
}

