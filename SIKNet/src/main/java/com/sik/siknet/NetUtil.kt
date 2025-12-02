package com.sik.siknet

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import com.sik.sikcore.SIKCore
import com.sik.sikcore.file.FileUtils
import java.io.IOException
import java.net.NetworkInterface
import java.util.Locale

/**
 * 网络工具类
 */
object NetUtil {
    private val wifiManager: WifiManager by lazy {
        SIKCore.getApplication().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    /**
     * 获取MAC地址（有网口的前提下）
     */
    fun getMacAddress(): String? = try {
        FileUtils.loadFileAsString("/sys/class/net/eth0/address")
            .uppercase(Locale.ROOT).substring(0, 17)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    /**
     * 获取当前wifi名称
     */
    fun getWifiName(): String? =
        wifiManager.connectionInfo?.ssid?.replace("\"", "")

    /**
     * 连接到指定wifi
     */
    @JvmOverloads
    @SuppressLint("MissingPermission")
    fun connectToWifi(
        ssid: String, password: String = "",
        searchFailed: (String) -> Unit = {},
        searchSuccess: () -> Unit = {},
        connectSuccess: (String) -> Unit = {},
        connectFailed: (String) -> Unit = {}
    ) {
        if (!openWifi()) return

        // 注册广播接收器来监听扫描结果
        val context = SIKCore.getApplication().applicationContext
        val scanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val scanResult = wifiManager.scanResults.find { it.SSID == ssid }
                if (scanResult == null) {
                    searchFailed("搜索wifi失败")
                    context?.unregisterReceiver(this) // 取消注册广播接收器
                } else {
                    searchSuccess()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        connectByP2P(ssid, password, connectSuccess, connectFailed)
                    } else {
                        connectToWifiLegacy(
                            ssid,
                            password,
                            scanResult.capabilities,
                            connectSuccess,
                            connectFailed
                        )
                    }
                    context?.unregisterReceiver(this) // 取消注册广播接收器
                }
            }
        }

        context.registerReceiver(
            scanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )

        // 触发扫描
        wifiManager.startScan()
    }

    /**
     * 使用旧方法连接wifi (Android 10以下)
     */
    @SuppressLint("MissingPermission")
    private fun connectToWifiLegacy(
        ssid: String,
        password: String,
        capabilities: String,
        connectSuccess: (String) -> Unit,
        connectFailed: (String) -> Unit
    ) {
        val config =
            wifiManager.configuredNetworks.singleOrNull { it.SSID.replace("\"", "") == ssid }
                ?: createWifiConfig(ssid, password, getCipherType(capabilities))

        val isSuccess = wifiManager.enableNetwork(config.networkId, true)
        if (isSuccess) connectSuccess("连接成功") else connectFailed("连接失败")
    }

    /**
     * 创建连接配置
     */
    @SuppressLint("MissingPermission")
    private fun createWifiConfig(
        ssid: String,
        password: String,
        type: WifiCapability
    ): WifiConfiguration {
        return WifiConfiguration().apply {
            SSID = "\"$ssid\""
            clearWifiConfiguration()
            setSecurityType(this, password, type)
            removeExistingConfig(ssid)
        }
    }

    /**
     * 清除旧有配置
     */
    private fun WifiConfiguration.clearWifiConfiguration() {
        allowedAuthAlgorithms.clear()
        allowedGroupCiphers.clear()
        allowedKeyManagement.clear()
        allowedPairwiseCiphers.clear()
        allowedProtocols.clear()
    }

    /**
     * 根据安全类型设置Wifi配置
     */
    private fun setSecurityType(config: WifiConfiguration, password: String, type: WifiCapability) {
        when (type) {
            WifiCapability.WIFI_CIPHER_NO_PASS -> config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            WifiCapability.WIFI_CIPHER_WEP -> {
                config.hiddenSSID = true
                config.wepKeys[0] = "\"$password\""
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                config.wepTxKeyIndex = 0
            }

            WifiCapability.WIFI_CIPHER_WPA, WifiCapability.WIFI_CIPHER_WPA2 -> {
                config.preSharedKey = "\"$password\""
                config.hiddenSSID = true
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                config.status = WifiConfiguration.Status.ENABLED
            }

            WifiCapability.WIFI_CIPHER_WPA3 -> {
                // WPA3 configuration can vary by device
                config.preSharedKey = "\"$password\""
                config.hiddenSSID = true
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.SAE)
            }

            WifiCapability.WIFI_CIPHER_EAP -> {
                // Enterprise configuration might need additional setup
            }
        }
    }

    /**
     * 移除已存在的相同SSID配置
     */
    @SuppressLint("MissingPermission")
    private fun removeExistingConfig(ssid: String) {
        wifiManager.configuredNetworks.singleOrNull { it.SSID == "\"$ssid\"" }?.let {
            wifiManager.removeNetwork(it.networkId)
            wifiManager.saveConfiguration()
        }
    }

    /**
     * Android 10及以上通过P2P连接Wifi
     */
    @JvmOverloads
    private fun connectByP2P(
        ssid: String,
        password: String,
        connectSuccess: (String) -> Unit = {},
        connectFailed: (String) -> Unit = {}
    ) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build()
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build()

            val connectivityManager = SIKCore.getApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.requestNetwork(
                request,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) = connectSuccess("连接成功")
                    override fun onUnavailable() = connectFailed("连接失败")
                })
        }
    }

    /**
     * 打开wifi
     */
    @JvmOverloads
    private fun openWifi(openFailed: (String) -> Unit = {}): Boolean {
        if (!wifiManager.isWifiEnabled) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                openFailed("请前往设置打开wifi")
                false
            } else {
                wifiManager.isWifiEnabled = true
                true
            }
        }
        return true
    }

    /**
     * 获取加密方法
     */
    private fun getCipherType(capabilities: String): WifiCapability = when {
        "WEP" in capabilities -> WifiCapability.WIFI_CIPHER_WEP
        "WPA3-SAE" in capabilities -> WifiCapability.WIFI_CIPHER_WPA3
        "WPA2-PSK" in capabilities -> WifiCapability.WIFI_CIPHER_WPA2
        "WPA-PSK" in capabilities -> WifiCapability.WIFI_CIPHER_WPA
        "EAP" in capabilities -> WifiCapability.WIFI_CIPHER_EAP
        else -> WifiCapability.WIFI_CIPHER_NO_PASS
    }

    /**
     * 获取本机IPv4地址（WiFi/以太网/热点/数据流量均可）
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                // 跳过关闭或回环的网卡
                if (!intf.isUp || intf.isLoopback) continue

                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    // 跳过 IPv6 & 回环
                    if (addr.isLoopbackAddress) continue
                    val host = addr.hostAddress ?: continue
                    // 过滤 IPv6（包含冒号）
                    if (!host.contains(":")) {
                        return host
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取Wifi下的IPv4地址
     */
    @SuppressLint("MissingPermission")
    fun getWifiIpAddress(): String? {
        val info = wifiManager.connectionInfo ?: return null
        val ip = info.ipAddress
        if (ip == 0) return null

        return String.format(
            Locale.US,
            "%d.%d.%d.%d",
            (ip and 0xff),
            (ip shr 8 and 0xff),
            (ip shr 16 and 0xff),
            (ip shr 24 and 0xff)
        )
    }

    enum class WifiCapability {
        WIFI_CIPHER_WEP,
        WIFI_CIPHER_WPA,
        WIFI_CIPHER_WPA2,
        WIFI_CIPHER_WPA3,
        WIFI_CIPHER_EAP,
        WIFI_CIPHER_NO_PASS
    }
}
