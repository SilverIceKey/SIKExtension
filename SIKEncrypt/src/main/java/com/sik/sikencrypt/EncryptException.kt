package com.sik.sikencrypt

/**
 * 加解密异常
 *
 * @constructor
 * 加解密异常信息
 *
 * @param encryptExceptionEnums
 */
class EncryptException(
    encryptExceptionEnums: EncryptExceptionEnums,
    cause: Throwable? = null
) : Exception(encryptExceptionEnums.message, cause)
