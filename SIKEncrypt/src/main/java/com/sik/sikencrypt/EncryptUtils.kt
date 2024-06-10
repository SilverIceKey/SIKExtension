package com.sik.sikencrypt

import com.sik.sikencrypt.enrypt.AESEncrypt
import com.sik.sikencrypt.enrypt.DESEncrypt
import com.sik.sikencrypt.enrypt.RSAEncrypt
import com.sik.sikencrypt.enrypt.SM4Encrypt
import com.sik.sikencrypt.enrypt.DESedeEncrypt
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

/**
 * 加解密工具
 *
 */
object EncryptUtils {
    init {
        /**
         * 移除旧的BC
         */
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        /**
         * 添加新的BC
         */
        Security.addProvider(BouncyCastleProvider())
    }

    /**
     * 根据配置返回加解密工具
     *
     * @param iEncryptConfig
     * @return
     */
    @JvmStatic
    fun <T : IEncryptConfig> getAlgorithm(iEncryptConfig: T): IEncrypt {
        return when (iEncryptConfig.algorithm()) {
            EncryptAlgorithm.AES -> {
                AESEncrypt(iEncryptConfig)
            }

            EncryptAlgorithm.DES -> {
                DESEncrypt(iEncryptConfig)
            }

            EncryptAlgorithm.DESede -> {
                DESedeEncrypt(iEncryptConfig)
            }

            EncryptAlgorithm.SM4 -> {
                SM4Encrypt(iEncryptConfig)
            }

            EncryptAlgorithm.RSA -> {
                if (iEncryptConfig is IRSAEncryptConfig) {
                    getAlgorithm(iEncryptConfig as IRSAEncryptConfig)
                } else {
                    throw EncryptException(EncryptExceptionEnums.CONFIG_ERROR)
                }
            }
        }
    }

    /**
     * 获取rsa加密工具
     *
     * @param iRSAEncryptConfig
     * @return
     */
    @JvmStatic
    fun <T : IRSAEncryptConfig> getAlgorithm(iRSAEncryptConfig: T): IRSAEncrypt {
        return RSAEncrypt(iRSAEncryptConfig)
    }


}