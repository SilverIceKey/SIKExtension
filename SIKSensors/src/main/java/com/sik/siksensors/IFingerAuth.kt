package com.sik.siksensors

/**
 * 指纹认证接口
 */
fun interface IFingerAuth {
    /**
     * 指纹认证
     */
    fun authenticateFingerprint(auth: (Boolean) -> Unit)
}