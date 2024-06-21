package com.sik.sikcore.generator

import java.security.SecureRandom
import kotlin.random.Random

/**
 * 密码生成器
 */
object PasswordGenerator {

    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val DIGITS = "0123456789"
    private const val SPECIAL_CHARACTERS = "!@#\$%^&*()-_=+[]{}|;:'\",.<>?/\\`~"

    private val random = SecureRandom()

    /**
     * 生成指定长度的密码
     * @param length 密码长度
     * @param useUpper 是否使用大写字母
     * @param useLower 是否使用小写字母
     * @param useDigits 是否使用数字
     * @param useSpecial 是否使用特殊字符
     * @return 生成的密码
     */
    fun generatePassword(
        length: Int,
        useUpper: Boolean = true,
        useLower: Boolean = true,
        useDigits: Boolean = true,
        useSpecial: Boolean = true
    ): String {
        require(length > 0) { "密码长度必须大于0" }

        val random = Random.Default
        val characterPool = buildString {
            if (useUpper) append(UPPERCASE)
            if (useLower) append(LOWERCASE)
            if (useDigits) append(DIGITS)
            if (useSpecial) append(SPECIAL_CHARACTERS)
        }

        require(characterPool.isNotEmpty()) { "至少选择一种字符类型" }

        while (true) {
            val password = (1..length)
                .map { characterPool[random.nextInt(characterPool.length)] }
                .joinToString("")

            if (isValidPassword(password, useUpper, useLower, useDigits, useSpecial)) {
                return password
            }
        }
    }

    /**
     * 验证密码是否符合规则
     */
    fun isValidPassword(
        password: String,
        useUpper: Boolean,
        useLower: Boolean,
        useDigits: Boolean,
        useSpecial: Boolean
    ): Boolean {
        if (useUpper && !password.any { it in UPPERCASE }) return false
        if (useLower && !password.any { it in LOWERCASE }) return false
        if (useDigits && !password.any { it in DIGITS }) return false
        if (useSpecial && !password.any { it in SPECIAL_CHARACTERS }) return false
        return true
    }
}
