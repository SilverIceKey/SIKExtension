package com.sik.sikencrypt.message_digest

import org.junit.Test


internal class SHA256MessageDigestTest {
    /**
     * a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3
     *
     */
    @Test
    fun digest() {
        println(SHA256MessageDigest().digestToHex("123".toByteArray()))
        println(SHA256MessageDigest().digestToBase64("123".toByteArray()))
    }
}