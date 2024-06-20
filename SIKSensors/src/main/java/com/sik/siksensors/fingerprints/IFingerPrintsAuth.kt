package com.sik.siksensors.fingerprints

import com.sik.siksensors.SensorErrorEnum

/**
 * 指纹认证接口
 */
fun interface IFingerPrintsAuth {
    /**
     * 指纹认证
     */
    fun authenticateFingerprint(auth: (SensorErrorEnum) -> Unit)
}