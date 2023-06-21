package com.sik.sikencrypt.message_digest

import org.junit.Test


internal class MD5MessageDigestTest {
    /**
     * 202cb962ac59075b964b07152d234b70
    */
    @Test
    fun digest() {
        println(MD5MessageDigest().digestToHex("123".toByteArray()))
        println(MD5MessageDigest().digestToBase64("123".toByteArray()))
    }
}