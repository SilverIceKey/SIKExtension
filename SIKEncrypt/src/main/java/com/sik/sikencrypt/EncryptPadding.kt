package com.sik.sikencrypt

/**
 * 填充模式
 *
 */
enum class EncryptPadding(val padding: String) {
    NoPadding("NoPadding"),
    PKCS5Padding("PKCS5Padding"),
    PKCS7Padding("PKCS7Padding"),
    OAEPWithSHA256AndMGF1Padding("OAEPWithSHA-256AndMGF1Padding"),
}