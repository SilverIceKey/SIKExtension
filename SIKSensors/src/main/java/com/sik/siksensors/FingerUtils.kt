package com.sik.siksensors

import android.os.Build
import com.sik.siksensors.fingerImpl.NewFingerAuth
import com.sik.siksensors.fingerImpl.OldFingerAuth


/**
 * 指纹传感器工具类
 */
object FingerUtils {

    /**
     * 指纹认证
     */
    @JvmStatic
    fun authenticateFingerprint(matchCallback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NewFingerAuth().authenticateFingerprint { matchCallback(it) }
        } else {
            OldFingerAuth().authenticateFingerprint { matchCallback(it) }
        }
    }
}