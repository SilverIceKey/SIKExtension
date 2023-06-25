package com.sik.sikencrypt

import com.sik.sikencrypt.enrypt.AESEncrypt
import com.sik.sikencrypt.enrypt.DESEncrypt
import com.sik.sikencrypt.enrypt.RSAEncrypt
import com.sik.sikencrypt.enrypt.SM4Encrypt
import com.sik.sikencrypt.enrypt.TripleDESEncrypt

/**
 * 加解密工具
 *
 */
object EncryptUtils {
    /**
     * 根据配置返回加解密工具
     *
     * @param iEncryptConfig
     * @return
     */
    @JvmStatic
    fun getAlgorithm(iEncryptConfig: IEncryptConfig): IEncrypt {
        return when (iEncryptConfig.algorithm()) {
            EncryptAlgorithm.AES -> {
                AESEncrypt(iEncryptConfig)
            }

            EncryptAlgorithm.DES -> {
                DESEncrypt(iEncryptConfig)
            }

            EncryptAlgorithm.TripleDES -> {
                TripleDESEncrypt(iEncryptConfig)
            }

            EncryptAlgorithm.SM4 -> {
                SM4Encrypt(iEncryptConfig)
            }

            EncryptAlgorithm.RSA -> {
                RSAEncrypt(iEncryptConfig)
            }
        }
    }

}