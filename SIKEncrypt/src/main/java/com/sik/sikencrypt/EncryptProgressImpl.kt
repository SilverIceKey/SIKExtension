package com.sik.sikencrypt

import android.util.Log

/**
 * 加解密进度监听器
 */
open class EncryptProgressImpl : IEncryptProgressListener {

    /**
     * 加密进度
     */
    override fun encryptProgress(progress: Int) {
        Log.d("EncryptProgressImpl","加密进度：$progress")
    }

    /**
     * 加密大小
     */
    override fun encryptBytes(bytes: Int) {
        Log.d("EncryptProgressImpl","加密大小：$bytes")
    }

    /**
     * 解密进度
     */
    override fun decryptProgress(progress: Int) {
        Log.d("EncryptProgressImpl","解密进度：$progress")
    }

    /**
     * 解密大小
     */
    override fun decryptBytes(bytes: Int) {
        Log.d("EncryptProgressImpl","解密大小：$bytes")
    }
}