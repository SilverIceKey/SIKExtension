package com.sik.sikencrypt

/**
 * 加解密异常枚举
 *
 * @property message
 */
enum class EncryptExceptionEnums(val message: String) {
    INIT_KEY_FIRST("请先初始化Key"),
    KEY_SIZE_ERROR("密钥长度错误"),
    ENCRYPT_ERROR("加密失败"),
    DECRYPT_ERROR("解密失败"),
    NO_IV("该模式需要传入IV"),
    MODE_NOT_SUPPORT("当前模式尚未支持"),
    PADDING_NOT_SUPPORT_DATA_SIZE("当前数据的长度不符合填充模式"),
    CONFIG_ERROR("配置异常"),
    PUBLIC_KEY_NOT_SET("公钥未设置"),
    PRIVATE_KEY_NOT_SET("私钥未设置"),
    PRIVATE_KEY_SIZE_ERROR("私钥长度错误"),
    PRIVATE_KEY_SIZE_SET_ERROR("私钥长度设置错误"),
    FILE_NOT_FOUND("文件未找到"),
    DECRYPT_BLOCK_SIZE_EXCEED("解密块超出长度限制"),
    ENCRYPT_BLOCK_SIZE_EXCEED("加密块超出长度限制"),
}