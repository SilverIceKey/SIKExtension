package com.sik.sikencrypt

/**
 * 受保护加密配置。
 *
 * 该配置不向 SDK 暴露密钥本身，只暴露一个不透明的 keyId。
 * keyId 的业务含义和密钥来源由调用方自己的 Native KeyVault 负责。
 */
interface IProtectedEncryptConfig {
    /**
     * 不透明密钥标识。
     *
     * 不能包含业务密钥本身；具体映射由业务侧 Provider 维护。
     */
    fun keyId(): String

    fun algorithm(): EncryptAlgorithm

    fun mode(): EncryptMode

    fun padding(): EncryptPadding

    fun iv(): ByteArray?

    /**
     * true 时由 Native 使用当前 Key 的对应块长度作为 IV。
     *
     * 用于历史协议中 IV == Key 的场景，避免把 IV（实际就是 Key）传给 Java。
     */
    val ivFromKey: Boolean
        get() = false

    /**
     * 是否把 IV 放到密文头部。
     */
    val composeIV: Boolean
        get() = true
}
