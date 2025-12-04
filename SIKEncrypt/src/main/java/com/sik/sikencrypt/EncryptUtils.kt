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

    /**
     * 暴露一个可复用的 BC Provider 实例
     *
     * 注意：
     * - 不再移除系统自带的 BC
     * - 即使系统中已存在名为 "BC" 的 Provider，这里也始终有一个可用的 BouncyCastleProvider 实例
     */
    lateinit var bc: BouncyCastleProvider
        private set

    init {
        // 先看看系统里是否已经有名为 "BC" 的 Provider
        val installed = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)

        bc = // 系统里已经有 BouncyCastleProvider，就直接用它
            installed as? BouncyCastleProvider
                ?: // 否则使用我们依赖的 bcprov 生成一个实例
                        BouncyCastleProvider().also { provider ->
                            // 仅当系统里没有 "BC" 时才尝试注册，避免破坏系统 Provider 配置
                            if (Security.getProvider(provider.name) == null) {
                                Security.addProvider(provider)
                            }
                        }
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