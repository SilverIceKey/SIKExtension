package com.sik.siksensors.fingerprints

import android.os.Build
import com.sik.sikcore.log.LogUtils
import com.sik.siksensors.SensorErrorEnum
import com.sik.siksensors.fingerprints.fingerprintsImpl.NewFingerPrintsPrintsAuth
import com.sik.siksensors.fingerprints.fingerprintsImpl.OldFingerPrintsPrintsAuth


/**
 * 指纹传感器工具类
 */
object FingerPrintsUtils {
    /**
     * 指纹认证
     */
    @JvmStatic
    inline fun <reified T : FingerPrintsConfig> authenticateFingerprint(
        fingerConfig: T?,
        crossinline auth: (SensorErrorEnum) -> Unit
    ) {
        var tempFingerConfig = fingerConfig
        if (tempFingerConfig == null) {
            tempFingerConfig = if (FingerPrintsConfig.defaultConfig is T) {
                FingerPrintsConfig.defaultConfig as T
            } else {
                null
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && tempFingerConfig?.useSystemDialog == true) {
            NewFingerPrintsPrintsAuth(tempFingerConfig).authenticateFingerprint { auth(it) }
        } else {
            OldFingerPrintsPrintsAuth().authenticateFingerprint { auth(it) }
        }
    }
}