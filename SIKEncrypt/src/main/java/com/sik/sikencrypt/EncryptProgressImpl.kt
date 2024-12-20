package com.sik.sikencrypt

import org.slf4j.LoggerFactory

/**
 * 加解密进度监听器
 */
open class EncryptProgressImpl : IEncryptProgressListener {
    private val logger = LoggerFactory.getLogger(EncryptProgressImpl::class.java)

    /**
     * 加密进度
     */
    override fun encryptProgress(progress: Int) {
        logger.info("加密进度：$progress")
    }

    /**
     * 加密大小
     */
    override fun encryptBytes(bytes: Int) {
        logger.info("加密大小：$bytes")
    }

    /**
     * 解密进度
     */
    override fun decryptProgress(progress: Int) {
        logger.info("解密进度：$progress")
    }

    /**
     * 解密大小
     */
    override fun decryptBytes(bytes: Int) {
        logger.info("解密大小：$bytes")
    }
}