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
 * 支持按设备名称过滤，同时保留已知设备过滤、系统过滤和业务过滤。
 */
object BLEScanner {
    private const val TAG = "BLEScanner"

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    private var isScanning = false

    private var callback: IBluetoothScanCallback? = null
    // 过滤已有设备地址列表，可在多次调用间累加
    private val existingDevices = mutableSetOf<String>()
    // 扫描时已发现的新设备地址
    private val foundDevices = mutableSetOf<String>()
    // 是否首次发现即停
    private var stopOnFirstFound = false
    // 按名称过滤关键字，忽略大小写，高优先级过滤
    private var nameFilter: String? = null

    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                // 1. 名称过滤
                nameFilter?.let { filter ->
                    val advName = result.scanRecord?.deviceName
                    val btName = device.name
                    val name = advName ?: btName
                    if (name == null || !name.equals(filter, ignoreCase = true)) return
                }
                val address = device.address
                // 2. 业务过滤：过滤已知设备并去重
                if (address !in existingDevices && address !in foundDevices) {
                    foundDevices.add(address)
                    Log.d(TAG, "BLE扫描发现新设备: ${device.name} - $address")
                    callback?.onDeviceFound(device)
                    if (stopOnFirstFound) stopScan()
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
     * @param nameFilter 设备名称过滤，忽略大小写
     * @param filters 可选系统 ScanFilter 列表，用于广播包层面过滤
     * @param settings 可选 ScanSettings，用于自定义扫描模式
     */
    @SuppressLint("MissingPermission")
    fun startScan(
        callback: IBluetoothScanCallback,
        knownDevices: Collection<String> = emptyList(),
        stopFirst: Boolean = false,
        nameFilter: String? = null,
        filters: List<ScanFilter> = emptyList(),
        settings: ScanSettings? = null
    ) {
        if (isScanning) {
            Log.w(TAG, "扫描已在进行中，请先调用 stopScan()")
            return
        }
        this.callback = callback
        this.stopOnFirstFound = stopFirst
        this.nameFilter = nameFilter
        existingDevices.addAll(knownDevices)
        foundDevices.clear()

        bluetoothLeScanner?.let { scanner ->
            Log.d(TAG, "开始BLE扫描，名称过滤：$nameFilter，已知：$existingDevices，首次停止：$stopOnFirstFound，系统过滤：${filters.size} 条规则")
            when {
                filters.isEmpty() && settings == null -> scanner.startScan(bleScanCallback)
                settings == null -> scanner.startScan(filters, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), bleScanCallback)
                else -> scanner.startScan(filters, settings, bleScanCallback)
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
