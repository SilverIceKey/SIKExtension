package com.sik.sikencrypt.message_digest

import org.junit.Test
import java.io.File

internal class SM3MessageDigestTest {
    @Test
    fun digest() {
        System.loadLibrary("SIKEncrypt")
        val sm3 = SM3MessageDigest()
        val file = File("/sdcard/Documents/123.jpg")
        println(file.exists())
        println(sm3.digestToHex(file.readBytes()))
        println(sm3.digestToBase64(file.readBytes()))
    }
}