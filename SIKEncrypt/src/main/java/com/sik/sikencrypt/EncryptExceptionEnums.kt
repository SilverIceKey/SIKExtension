package com.sik.sikencrypt

/**
 * 加解密异常枚举
 *
 * @property message
 */
enum class EncryptExceptionEnums(val message: String) {
    KEY_SIZE_ERROR("密钥长度错误"),
    ENCRYPT_ERROR("加密失败"),
    DECRYPT_ERROR("解密失败")
}