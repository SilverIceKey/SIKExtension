package com.sik.sikencrypt

/**
 * 加密模式
 *
 */
enum class EncryptMode(val mode: String) {
    ECB("ECB"),
    CBC("CBC"),
    CTR("CTR"),
    GCM("GCM")
}