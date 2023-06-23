package com.sik.sikencrypt

import com.sik.sikencrypt.message_digest.MD5MessageDigest
import com.sik.sikencrypt.message_digest.SHA256MessageDigest
import com.sik.sikencrypt.message_digest.SM3MessageDigest

/**
 * 信息摘要生成工具类
 *
 */
object MessageDigestUtils {
    init {
        System.loadLibrary("gmssl")
        System.loadLibrary("SIKEncrypt")
    }
    /**
     * 获取信息摘要类型
     *
     * @param messageDigestTypes
     * @return
     */
    @JvmStatic
    fun getMode(messageDigestTypes: MessageDigestTypes): IMessageDigest {
        return when(messageDigestTypes){
            MessageDigestTypes.MD5->{
                MD5MessageDigest()
            }

            MessageDigestTypes.SHA256->{
                SHA256MessageDigest()
            }

            MessageDigestTypes.SM3->{
                SM3MessageDigest()
            }
        }
    }
}