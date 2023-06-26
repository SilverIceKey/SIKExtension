package com.sik.sikencrypt

/**
 * 加解密异常枚举
 *
 * @property message
 */
enum class EncryptExceptionEnums(val message: String) {
    KEY_SIZE_ERROR("密钥长度错误"),
    ENCRYPT_ERROR("加密失败"),
    DECRYPT_ERROR("解密失败"),
    NO_IV("该模式需要传入IV"),
    MODE_NOT_SUPPORT("当前模式尚未支持"),
    PADDING_NOT_SUPPORT_DATA_SIZE("当前数据的长度不符合填充模式"),
}