package com.sik.siknet

import android.annotation.SuppressLint
import android.content.Context
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
import java.util.*

/**
 * 网络工具类
 */
object NetUtil {
    private val wifiManager: WifiManager =
        SIKCore.getApplication().getSystemService(Context.WIFI_SERVICE) as WifiManager

    /**
     * 获取MAC地址（有网口的前提下）
     */
    fun getMacAddress(): String? {
        return try {
            FileUtils.loadFileAsString("/sys/class/net/eth0/address")
                .uppercase(Locale.ROOT).substring(0, 17)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取当前wifi名称
     */
    fun getWifiName(): String? {
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo != null) {
            return wifiInfo.ssid.replace("\"", "")
        }
        return null
    }

    /**
     * 连接到指定wifi
     */
    @JvmOverloads
    @SuppressLint("MissingPermission")
    fun connectToWifi(
        ssid: String, password: String = "",
        searchFailed: (String) -> Unit = {},
        connectSuccess: (String) -> Unit = {},
        connectFailed: (String) -> kotlin.Unit = {}
    ) {
        if (!openWifi()) {
            return
        }

        val scanResult = wifiManager.scanResults.singleOrNull { it.SSID == ssid }
        if (scanResult == null) {
            searchFailed("搜索wifi失败")
            return
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectByP2P(ssid, password)
                return
            }
            var isSuccess = false
            //如果找到了wifi了，从配置表中搜索该wifi的配置config，也就是以前有没有连接过
            //注意configuredNetworks中的ssid，系统源码中加上了双引号，这里比对的时候要去掉
            val config =
                wifiManager.configuredNetworks.singleOrNull { it.SSID.replace("\"", "") == ssid }
            isSuccess = if (config != null) {
                //如果找到了，那么直接连接，不要调用wifiManager.addNetwork  这个方法会更改config的！
                wifiManager.enableNetwork(config.networkId, true)
            } else {
                // 没找到的话，就创建一个新的配置，然后正常的addNetWork、enableNetwork即可
                val padWifiNetwork =
                    createWifiConfig(
                        scanResult?.SSID!!,
                        password,
                        getCipherType(scanResult.capabilities)
                    )
                val netId = wifiManager.addNetwork(padWifiNetwork)
                wifiManager.enableNetwork(netId, true)
            }
            if (isSuccess) {
                connectSuccess("连接成功")
            } else {
                connectFailed("连接失败")
            }
        }
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
        //初始化WifiConfiguration
        val config = WifiConfiguration()
        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear()
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()
        //指定对应的SSID
        config.SSID = "\"" + ssid + "\""
        //如果之前有类似的配置
        val tempConfig = wifiManager.configuredNetworks.singleOrNull { it.SSID == "\"$ssid\"" }
        if (tempConfig != null) {
            //则清除旧有配置  不是自己创建的network 这里其实是删不掉的
            wifiManager.removeNetwork(tempConfig.networkId)
            wifiManager.saveConfiguration()
        }
        //不需要密码的场景
        if (type == WifiCapability.WIFI_CIPHER_NO_PASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            //以WEP加密的场景
        } else if (type == WifiCapability.WIFI_CIPHER_WEP) {
            config.hiddenSSID = true
            config.wepKeys[0] = "\"" + password + "\""
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            config.wepTxKeyIndex = 0
            //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
        } else if (type == WifiCapability.WIFI_CIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\""
            config.hiddenSSID = true
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            config.status = WifiConfiguration.Status.ENABLED
        }
        return config
    }

    @JvmOverloads
    //Android10以上 通过P2P连接Wifi
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
            val request =
                NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(specifier)
                    .build()

            val connectivityManager =
                SIKCore.getApplication()
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    connectSuccess("连接成功")

                }

                override fun onUnavailable() {
                    connectFailed("连接失败")
                }
            }
            connectivityManager.requestNetwork(request, networkCallback)
        }
    }

    /**
     * 打开wifi
     */
    @JvmOverloads
    private fun openWifi(openFailed: (String) -> Unit = {}): Boolean {
        if (!wifiManager.isWifiEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //请用户手动打开wifi
                openFailed("请前往设置打开wifi")
                return false
            } else {
                wifiManager.isWifiEnabled = true
                return true
            }
        }
        return true
    }

    /**
     * 获取加密方法
     */
    private fun getCipherType(capabilities: String): WifiCapability {
        return when {
            capabilities.contains("WEB") -> {
                WifiCapability.WIFI_CIPHER_WEP
            }

            capabilities.contains("PSK") -> {
                WifiCapability.WIFI_CIPHER_WPA
            }

            capabilities.contains("WPS") -> {
                WifiCapability.WIFI_CIPHER_NO_PASS
            }

            else -> {
                WifiCapability.WIFI_CIPHER_NO_PASS
            }
        }
    }

    enum class WifiCapability {
        WIFI_CIPHER_WEP, WIFI_CIPHER_WPA, WIFI_CIPHER_NO_PASS
    }
}