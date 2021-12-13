package com.sk.skextension.utils.encrypt

/**
 * 加密工具类
 */
object EncryptUtil {
    /**
     * 加载so
     */
    init {
        System.loadLibrary("skextension")
    }
    /**
     * AES加密(AES_ECB_PKCS7)
     */
    external fun AESEncode(key:String,content:String):String

    /**
     * AES解密(AES_ECB_PKCS7)
     */
    external fun AESDecode(key:String,content:String):ByteArray

    /**
     * MD5加密
     */
    external fun MD5Encode(content: String):String
}