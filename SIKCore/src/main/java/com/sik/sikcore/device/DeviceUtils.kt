package com.sik.sikcore.device

import android.annotation.SuppressLint
import android.content.Context
import android.net.Proxy
import android.os.Build
import android.os.Debug
import android.provider.Settings
import com.sik.sikcore.SIKCore
import com.sik.sikcore.shell.ShellUtils
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import kotlin.experimental.and


/**
 * 设备相关工具类
 */
object DeviceUtils {
    /**
     * 获取设备SN号码
     * @return
     */
    fun getSN(): String {
        return ShellUtils.execCmd("getprop ro.serialno", false).successMsg
    }

    /**
     * 获得设备硬件标识
     *
     * @param context 上下文
     * @return 设备硬件标识
     */
    @JvmOverloads
    fun getDeviceId(context: Context = SIKCore.getApplication(), length: Int = 32): String {
        val sbDeviceId = StringBuilder()

        //获得AndroidId（无需权限）
        val androidId = getAndroidId(context)
        //获得设备序列号（无需权限）
        val serial = getSERIAL()
        //获得硬件uuid（根据硬件相关属性，生成uuid）（无需权限）
        val uuid = getDeviceUUID().replace("-", "")

        //追加androidId
        if (!androidId.isNullOrEmpty()) {
            sbDeviceId.append(androidId)
            sbDeviceId.append("|")
        }
        //追加serial
        if (!serial.isNullOrEmpty()) {
            sbDeviceId.append(serial)
            sbDeviceId.append("|")
        }
        //追加硬件uuid
        if (uuid.isNotEmpty()) {
            sbDeviceId.append(uuid)
        }

        //生成SHA1，统一DeviceId长度
        if (sbDeviceId.isNotEmpty()) {
            try {
                val hash = getHashByString(sbDeviceId.toString())
                val sha256 = bytesToHex(hash)
                if (!sha256.isNullOrEmpty()) {
                    //返回最终的DeviceId
                    return sha256.take(length)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        //如果以上硬件标识数据均无法获得，
        //则DeviceId默认使用系统随机数，这样保证DeviceId不为空
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * 获得设备的AndroidId
     *
     * @param context 上下文
     * @return 设备的AndroidId
     */
    @SuppressLint("HardwareIds")
    private fun getAndroidId(context: Context): String? {
        try {
            return Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    /**
     * 获得设备序列号（如：WTK7N16923005607）, 个别设备无法获取
     *
     * @return 设备序列号
     */
    private fun getSERIAL(): String? {
        try {
            return Build.SERIAL
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    /**
     * 获得设备硬件uuid
     * 使用硬件信息，计算出一个随机数
     *
     * @return 设备硬件uuid
     */
    private fun getDeviceUUID(): String {
        return try {
            val dev =
                "3883756" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.DEVICE.length % 10 + Build.HARDWARE.length % 10 + Build.ID.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 + Build.SERIAL.length % 10
            UUID(
                dev.hashCode().toLong(),
                Build.SERIAL.hashCode().toLong()
            ).toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }

    /**
     * 取SHA1
     * @param data 数据
     * @return 对应的hash值
     */
    private fun getHashByString(data: String): ByteArray {
        return try {
            val messageDigest: MessageDigest = MessageDigest.getInstance("SHA256")
            messageDigest.reset()
            messageDigest.update(data.toByteArray(charset("UTF-8")))
            messageDigest.digest()
        } catch (e: Exception) {
            "".toByteArray()
        }
    }

    /**
     * 转16进制字符串
     * @param data 数据
     * @return 16进制字符串
     */
    private fun bytesToHex(data: ByteArray): String? {
        val sb = StringBuilder()
        var stmp: String
        for (n in data.indices) {
            stmp = Integer.toHexString(data[n].and(0xFF.toByte()).toInt())
            if (stmp.length == 1) sb.append("0")
            sb.append(stmp)
        }
        return sb.toString().toUpperCase(Locale.CHINA)
    }


    /**
     * 检测调试器是否已附着
     *
     * @return true 如果调试器已附着，否则false
     */
    fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected()
    }

    /**
     * 检测设备是否已Root
     *
     * @return true 如果设备已Root，否则false
     */
    fun isDeviceRooted(): Boolean {
        // 方式一：检查Build标签中是否包含test-keys
        if (Build.TAGS?.contains("test-keys") == true) {
            return true
        }

        // 方式二：检查常见的Root文件路径
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) {
                return true
            }
        }

        // 方式三：尝试通过Shell执行su命令检测（依赖ShellUtils工具类）
        val result = ShellUtils.execCmd("which su", false)
        return result.successMsg.isNotEmpty()
    }


    /**
     * 检测设备是否配置了代理
     *
     * @param context 上下文
     * @return true 如果设备配置了代理，否则false
     */
    @SuppressLint("NewApi")
    fun isUsingProxy(context: Context = SIKCore.getApplication()): Boolean {
        // 方法一：通过系统属性检测代理
        val proxyHost = System.getProperty("http.proxyHost")
        val proxyPort = System.getProperty("http.proxyPort")
        if (!proxyHost.isNullOrEmpty() && !proxyPort.isNullOrEmpty()) {
            return true
        }

        val httpsProxyHost = System.getProperty("https.proxyHost")
        val httpsProxyPort = System.getProperty("https.proxyPort")
        if (!httpsProxyHost.isNullOrEmpty() && !httpsProxyPort.isNullOrEmpty()) {
            return true
        }

        // 方法二：针对Android 4.0及以上版本，使用android.net.Proxy检测（已弃用，但作为补充检测）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            val defaultProxy = Proxy.getDefaultHost()
            if (!defaultProxy.isNullOrEmpty()) {
                return true
            }
        }

        return false
    }
}