package com.sik.sikcore.generator

import java.security.SecureRandom

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
        val characterPool = buildString {
            if (useUpper) append(UPPERCASE)
            if (useLower) append(LOWERCASE)
            if (useDigits) append(DIGITS)
            if (useSpecial) append(SPECIAL_CHARACTERS)
        }

        require(characterPool.isNotEmpty()) { "至少选择一种文本类型" }

        return (1..length)
            .map { characterPool[random.nextInt(characterPool.length)] }
            .joinToString("")
    }
}
