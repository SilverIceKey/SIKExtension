package com.sik.siksensors

/**
 * 指纹识别错误
 */
enum class FingerErrorEnum(val code: Int, val message: String) {
    AUTHENTICATION_SUCCESS(0,"验证成功"),
    NO_PERMISSION(1,"没有相关权限"),
    NO_ENROLLED_FINGERPRINTS(2,"没有注册的指纹"),
    NO_KEYGUARD_SECURE(3,"没有开启安全锁屏"),
    AUTHENTICATION_FAILED(4,"验证失败"),
    NO_HARDWARE(5,"没有设备"),
    HW_UNAVAILABLE(6,"设备暂不可用"),
}