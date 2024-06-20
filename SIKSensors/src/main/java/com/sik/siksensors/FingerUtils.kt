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
    inline fun <reified T : FingerConfig> authenticateFingerprint(
        fingerConfig: T?,
        crossinline auth: (FingerErrorEnum) -> Unit
    ) {
        var tempFingerConfig = fingerConfig
        if (tempFingerConfig == null) {
            tempFingerConfig = if (FingerConfig.defaultConfig is T) {
                FingerConfig.defaultConfig as T
            } else {
                null
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && tempFingerConfig?.useSystemDialog == true) {
            NewFingerAuth(tempFingerConfig).authenticateFingerprint { auth(it) }
        } else {
            OldFingerAuth().authenticateFingerprint { auth(it) }
        }
    }
}