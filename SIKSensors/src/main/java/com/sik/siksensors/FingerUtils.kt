package com.sik.siksensors

import android.os.Build
import com.sik.sikcore.log.LogUtils
import com.sik.siksensors.fingerImpl.NewFingerAuth
import com.sik.siksensors.fingerImpl.OldFingerAuth


/**
 * 指纹传感器工具类
 */
object FingerUtils {
    private val logger = LogUtils.getLogger(FingerUtils::class)

    /**
     * 指纹认证
     */
    @JvmStatic
    fun <T : FingerConfig> authenticateFingerprint(
        fingerConfig: T?,
        auth: (Boolean) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (fingerConfig == null) {
                throw FingerException("请传入指纹识别配置")
            } else {
                NewFingerAuth(fingerConfig).authenticateFingerprint { auth(it) }
            }
        } else {
            OldFingerAuth().authenticateFingerprint { auth(it) }
        }
    }
}