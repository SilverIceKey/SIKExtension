package com.sik.skextensionsample

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikencrypt.EncryptAlgorithm
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.IEncryptConfig
import com.sik.sikencrypt.IRSAEncryptConfig
import java.util.UUID

class EncryptConfig : IRSAEncryptConfig {
    private val uuidKey = UUID.randomUUID().toString().replace("-", "").toByteArray()
    private val uuidIv = UUID.randomUUID().toString().replace("-", "").toByteArray()
    override fun publicKey(): ByteArray {
        return ConvertUtils.base64StringToBytes("""MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0ZfAchPsyDgPphYc4sx8Qj2a4OSbDnpNEz5QWSYMWsGYzIndRJrt0cb9pGMX/L/ayBlRQSGE2Vtj6OiLPLHJ8Oojy8fF6EpTTR4YgoosW3VdQAUUNaMhLT4YeE3guCNmGxB5SMRhcjTjYrNTIVBTmbw7ffrx4ntnwbZxh01/vlOkP7u8h3AwIfTt0its51hdvxgM4q33pRrSdZ0iFPNkAOhsoFrwS/5MXixL4leYWsRCCzqTqI3vKag/bbBFppIYaWEvOZhGhck1m2VReedyofG/w4lXdFqjb81wNWanRBai6JmHX3wbGsJ7C7bOEGLEkubvI1gAs++HGDjH0a9aXwIDAQAB""")
    }

    override fun privateKey(): ByteArray {
        return byteArrayOf()
    }

    override fun key(): ByteArray {
        return uuidKey
    }

    override fun iv(): ByteArray? {
        return uuidIv
    }

    override fun algorithm(): EncryptAlgorithm {
        return EncryptAlgorithm.RSA
    }

    override fun mode(): EncryptMode {
        return EncryptMode.CBC
    }

    override fun padding(): EncryptPadding {
        return EncryptPadding.PKCS5Padding
    }
}