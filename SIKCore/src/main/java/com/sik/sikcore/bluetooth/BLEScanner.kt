package com.sik.sikcore.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanResult
import android.util.Log

/**
 * BLEScanner 单例对象，用于执行蓝牙低功耗（BLE）扫描操作，
 * 支持可选扫描过滤规则和扫描设置。
 */
object BLEScanner {
    private const val TAG = "BLEScanner"

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    private var isScanning = false

    // 扫描结果回调
    private var callback: IBluetoothScanCallback? = null
    // 过滤已有设备地址列表，可在多次调用间累加
    private val existingDevices = mutableSetOf<String>()
    // 扫描时已发现的新设备地址
    private val foundDevices = mutableSetOf<String>()
    // 是否首次发现即停
    private var stopOnFirstFound = false

    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val address = device.address
                if (address !in existingDevices && address !in foundDevices) {
                    foundDevices.add(address)
                    Log.d(TAG, "BLE扫描发现新设备: ${device.name} - $address")
                    callback?.onDeviceFound(device)
                    if (stopOnFirstFound) {
                        stopScan()
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE扫描失败，错误码: $errorCode")
            if (isScanning) stopScan()
        }
    }

    /**
     * 开始 BLE 扫描
     * @param callback 扫描结果回调
     * @param knownDevices 已知设备地址列表，用于过滤，支持追加
     * @param stopFirst 是否首次发现新设备后立即停止扫描
     * @param filters 可选 ScanFilter 列表，用于系统层面过滤（如 UUID、名称）
     * @param settings 可选 ScanSettings，用于自定义扫描模式
     */
    @SuppressLint("MissingPermission")
    fun startScan(
        callback: IBluetoothScanCallback,
        knownDevices: Collection<String> = emptyList(),
        stopFirst: Boolean = false,
        filters: List<ScanFilter> = emptyList(),
        settings: ScanSettings? = null
    ) {
        if (isScanning) {
            Log.w(TAG, "扫描已在进行中，请先调用 stopScan()")
            return
        }
        this.callback = callback
        stopOnFirstFound = stopFirst
        existingDevices.addAll(knownDevices)
        foundDevices.clear()

        bluetoothLeScanner?.let { scanner ->
            Log.d(TAG, "开始BLE扫描，过滤已知：$existingDevices，首次停止：$stopOnFirstFound，系统过滤：${filters.map { it.deviceName ?: it.serviceUuid }}")
            if (filters.isEmpty() && settings == null) {
                scanner.startScan(bleScanCallback)
            } else if (settings == null) {
                scanner.startScan(filters, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), bleScanCallback)
            } else {
                scanner.startScan(filters, settings, bleScanCallback)
            }
            isScanning = true
        } ?: run {
            Log.e(TAG, "BLE扫描不可用或蓝牙未开启")
            callback.onScanFinished()
        }
    }

    /**
     * 停止 BLE 扫描，并通过回调通知扫描结束
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothLeScanner?.let { scanner ->
            if (isScanning) {
                scanner.stopScan(bleScanCallback)
                isScanning = false
                Log.d(TAG, "停止BLE扫描 完成，已发现设备：$foundDevices")
                callback?.onScanFinished()
            }
        }
    }
}
