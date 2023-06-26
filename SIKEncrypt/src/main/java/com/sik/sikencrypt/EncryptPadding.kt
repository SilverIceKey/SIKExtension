package com.sik.sikencrypt

/**
 * 填充模式
 *
 */
enum class EncryptPadding(val padding: String) {
    NonePadding("NonePadding"),
    PKCS5Padding("PKCS5Padding")
}